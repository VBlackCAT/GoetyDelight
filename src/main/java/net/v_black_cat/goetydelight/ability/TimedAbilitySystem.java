package net.v_black_cat.goetydelight.ability;

import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.common.capabilities.*;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.event.entity.living.LivingEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.network.PacketDistributor;
import net.v_black_cat.goetydelight.network.NetworkHandler;
import net.v_black_cat.goetydelight.network.SyncAbilityPacket;

import java.io.File;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

import static net.v_black_cat.goetydelight.ability.AbilityRegistry.INFINITE_DURATION;

@Mod.EventBusSubscriber
public class TimedAbilitySystem {
    // Capability 标识
    public static final Capability<EntityTimedAbilities> ENTITY_TIMED_ABILITIES =
            CapabilityManager.get(new CapabilityToken<>() {});

    // 能力注册表
    private static final Map<String, AbilityDefinition> ABILITY_REGISTRY = new ConcurrentHashMap<>();

    // 注册 Capability
    @SubscribeEvent
    public static void registerCapabilities(RegisterCapabilitiesEvent event) {
        event.register(EntityTimedAbilities.class);
    }

    // 注册能力
    public static void registerAbility(String abilityId, AbilityApplier applier, AbilityRemover remover) {
        ABILITY_REGISTRY.put(abilityId, new AbilityDefinition(applier, remover));
    }

    // 获取能力定义
    public static Optional<AbilityDefinition> getAbilityDefinition(String abilityId) {
        return Optional.ofNullable(ABILITY_REGISTRY.get(abilityId));
    }

    // 实体刻事件处理
    @SubscribeEvent
    public static void onLivingTick(LivingEvent.LivingTickEvent event) {
        if (event.getEntity().level().isClientSide) return;

        event.getEntity().getCapability(ENTITY_TIMED_ABILITIES).ifPresent(abilities -> {
            abilities.tick(event.getEntity());
        });
    }

    // 实体死亡事件处理
    @SubscribeEvent
    public static void onLivingDeath(LivingDeathEvent event) {
        if (!event.isCanceled() && event.getEntity().level().isClientSide) return;

        event.getEntity().getCapability(ENTITY_TIMED_ABILITIES).ifPresent(abilities -> {
            abilities.clearAbilitiesOnDeath(event.getEntity());
        });
    }

    // 玩家重生事件处理
    @SubscribeEvent
    public static void onPlayerRespawn(PlayerEvent.PlayerRespawnEvent event) {
        if (event.getEntity().level().isClientSide) return;

        event.getEntity().getCapability(ENTITY_TIMED_ABILITIES).ifPresent(abilities -> {
            abilities.clearAbilitiesOnRespawn(event.getEntity());
        });
    }

    // 玩家克隆事件处理（维度切换）
    @SubscribeEvent
    public static void onPlayerClone(PlayerEvent.Clone event) {
        LivingEntity original = event.getOriginal();
        LivingEntity newEntity = event.getEntity();

        // 修复玩家重生事件，确保数据正确转移
        if (event.isWasDeath()) {
            // 死亡重生情况
            original.reviveCaps();

            original.getCapability(ENTITY_TIMED_ABILITIES).ifPresent(oldAbilities -> {
                newEntity.getCapability(ENTITY_TIMED_ABILITIES).ifPresent(newAbilities -> {
                    // 复制能力数据到新实体
                    CompoundTag nbt = oldAbilities.serializeNBT();
                    newAbilities.deserializeNBT(nbt);

                    // 确保持久化数据也同步
                    CompoundTag originalData = original.getPersistentData();
                    if (originalData.contains("goetydelight_abilities")) {
                        CompoundTag abilitiesData = originalData.getCompound("goetydelight_abilities");
                        newEntity.getPersistentData().put("goetydelight_abilities", abilitiesData);
                    }
                });
            });

            original.invalidateCaps();
        } else {
            // 维度切换情况
            original.reviveCaps(); // 确保原实体的 Capability 可用

            original.getCapability(ENTITY_TIMED_ABILITIES).ifPresent(oldAbilities -> {
                newEntity.getCapability(ENTITY_TIMED_ABILITIES).ifPresent(newAbilities -> {
                    // 复制能力数据到新实体
                    CompoundTag nbt = oldAbilities.serializeNBT();
                    newAbilities.deserializeNBT(nbt);
                });
            });

            original.invalidateCaps();
        }
    }

    @SubscribeEvent
    public static void onAttachCapabilities(AttachCapabilitiesEvent<Entity> event) {
        if (event.getObject() instanceof LivingEntity) {
            event.addCapability(
                    new ResourceLocation("goetydelight", "timed_abilities"),
                    new ICapabilityProvider() {
                        private final LazyOptional<EntityTimedAbilities> instance = LazyOptional.of(EntityTimedAbilities::new);

                        @Override
                        public <T> LazyOptional<T> getCapability(Capability<T> cap, Direction side) {
                            return ENTITY_TIMED_ABILITIES.orEmpty(cap, instance.cast());
                        }
                    }
            );
        }
    }


    // 检查实体是否有能力 (优化版本)
    public static boolean hasAbility(LivingEntity entity, String abilityId) {
        // 先检查实体的持久化数据，如果存在直接返回结果，避免Capability查询开销
        if (entity.getPersistentData().contains("has" + abilityId.substring(0, 1).toUpperCase() + abilityId.substring(1))) {
            return entity.getPersistentData().getBoolean("has" + abilityId.substring(0, 1).toUpperCase() + abilityId.substring(1));
        }
        
        // 回退到Capability查询
        LazyOptional<EntityTimedAbilities> capabilities = entity.getCapability(ENTITY_TIMED_ABILITIES);
        if (capabilities.isPresent()) {
            EntityTimedAbilities abilities = capabilities.orElseThrow(IllegalStateException::new);
            return abilities.hasAbility(abilityId);
        }
        return false;
    }

    // 获取实体能力的剩余时间
    public static int getAbilityRemainingTime(LivingEntity entity, String abilityId) {
        LazyOptional<EntityTimedAbilities> capabilities = entity.getCapability(ENTITY_TIMED_ABILITIES);
        if (capabilities.isPresent()) {
            EntityTimedAbilities abilities = capabilities.orElseThrow(IllegalStateException::new);
            return abilities.getRemainingTime(abilityId);
        }
        return 0;
    }

    // 实体能力数据类
    public static class EntityTimedAbilities implements ICapabilitySerializable<CompoundTag> {
        private final Map<String, TimedAbility> activeAbilities = new HashMap<>();
        private final List<Runnable> pendingRemovals = new ArrayList<>();
        @Override
        public <T> LazyOptional<T> getCapability(Capability<T> cap, Direction side) {
            return cap == ENTITY_TIMED_ABILITIES ? LazyOptional.of(() -> this).cast() : LazyOptional.empty();
        }

        // 添加定时能力
        public void addAbility(String abilityId, int durationTicks, AbilityApplier applier, AbilityRemover remover) {
            TimedAbility ability = new TimedAbility(abilityId, durationTicks, applier, remover);
            activeAbilities.put(abilityId, ability);

            // 立即应用效果
            applier.apply(null); // 注意：这里需要传入实体，但实体在tick时才会应用
        }

        // 移除能力
        public void removeAbility(String abilityId, LivingEntity entity) {
            TimedAbility ability = activeAbilities.get(abilityId);
            if (ability != null) {
                ability.remover.remove(entity);
                activeAbilities.remove(abilityId);
                
                // 添加同步到客户端的逻辑
                if (entity instanceof Player) {
                    syncAbilityWithClient(entity, abilityId, false);
                }
            }
        }

        // 检查是否有激活的能力
        public boolean hasAbility(String abilityId) {
            return activeAbilities.containsKey(abilityId);
        }

        // 获取剩余时间
        public int getRemainingTime(String abilityId) {
            TimedAbility ability = activeAbilities.get(abilityId);
            return ability != null ? ability.remainingTicks : 0;
        }

        // 游戏刻更新
        public void tick(LivingEntity entity) {
            pendingRemovals.clear();

            for (TimedAbility ability : activeAbilities.values()) {
                // 如果是第一次应用，确保应用效果
                if (ability.remainingTicks == ability.initialDuration) {
                    ability.applier.apply(entity);
                }

                // 无限时间的能力不减少时间 (-1 表示无限)
                if (ability.initialDuration != INFINITE_DURATION) {
                    ability.remainingTicks--;

                    if (ability.remainingTicks <= 0) {
                        ability.remover.remove(entity);
                        pendingRemovals.add(() -> activeAbilities.remove(ability.abilityId));

                        if (entity instanceof Player player) {
                            syncAbilityWithClient(entity, ability.abilityId, false);
                        }
                    }
                }
            }

            pendingRemovals.forEach(Runnable::run);
        }

        // 死亡时清除所有能力
        public void clearAbilitiesOnDeath(LivingEntity entity) {
            /*
            for (TimedAbility ability : activeAbilities.values()) {
                ability.remover.remove(entity);
                pendingRemovals.add(() -> activeAbilities.remove(ability.abilityId));

                // 添加同步到客户端的逻辑
                if (entity instanceof Player) {
                    syncAbilityWithClient(entity, ability.abilityId, false);
                }
            }
            pendingRemovals.forEach(Runnable::run);
            */
        }

        // 重生时清除所有能力
        public void clearAbilitiesOnRespawn(LivingEntity entity) {
            clearAbilitiesOnDeath(entity);
        }

        // 序列化
        public CompoundTag serializeNBT() {
            CompoundTag tag = new CompoundTag();
            CompoundTag abilitiesTag = new CompoundTag();

            for (TimedAbility ability : activeAbilities.values()) {
                CompoundTag abilityTag = new CompoundTag();
                abilityTag.putInt("remainingTicks", ability.remainingTicks);
                abilityTag.putInt("initialDuration", ability.initialDuration);
                abilitiesTag.put(ability.abilityId, abilityTag);
            }

            tag.put("Abilities", abilitiesTag);
            return tag;
        }

        // 反序列化
        public void deserializeNBT(CompoundTag tag) {
            activeAbilities.clear();

            if (tag.contains("Abilities")) {
                CompoundTag abilitiesTag = tag.getCompound("Abilities");

                for (String abilityId : abilitiesTag.getAllKeys()) {
                    CompoundTag abilityTag = abilitiesTag.getCompound(abilityId);
                    int remainingTicks = abilityTag.getInt("remainingTicks");
                    int initialDuration = abilityTag.getInt("initialDuration");

                    // 从注册表获取能力定义
                    Optional<AbilityDefinition> definition = getAbilityDefinition(abilityId);
                    if (definition.isPresent()) {
                        AbilityDefinition def = definition.get();
                        TimedAbility ability = new TimedAbility(
                                abilityId,
                                initialDuration,
                                def.applier,
                                def.remover
                        );
                        ability.remainingTicks = remainingTicks;

                        activeAbilities.put(abilityId, ability);
                    }
                }
            }
        }
    }

    // 定时能力类
    private static class TimedAbility {
        public final String abilityId;
        public final int initialDuration;
        public int remainingTicks;
        public final AbilityApplier applier;
        public final AbilityRemover remover;

        public TimedAbility(String abilityId, int durationTicks, AbilityApplier applier, AbilityRemover remover) {
            this.abilityId = abilityId;
            this.initialDuration = durationTicks;
            this.remainingTicks = durationTicks;
            this.applier = applier;
            this.remover = remover;
        }
    }

    // 能力定义类
    public static class AbilityDefinition {
        public final AbilityApplier applier;
        public final AbilityRemover remover;

        public AbilityDefinition(AbilityApplier applier, AbilityRemover remover) {
            this.applier = applier;
            this.remover = remover;
        }
    }

    // 能力应用接口
    @FunctionalInterface
    public interface AbilityApplier {
        void apply(LivingEntity entity);
    }

    // 能力移除接口
    @FunctionalInterface
    public interface AbilityRemover {
        void remove(LivingEntity entity);
    }

    // 工具方法：获取实体能力
    public static LazyOptional<EntityTimedAbilities> getAbilities(LivingEntity entity) {
        return entity.getCapability(ENTITY_TIMED_ABILITIES);
    }

    // 在 TimedAbilitySystem 类中添加以下方法

    public static void syncAbilityWithClient(LivingEntity entity, String abilityId, boolean added) {
        if (!entity.level().isClientSide) {
            // 只从服务端发送
            NetworkHandler.INSTANCE.send(
                    PacketDistributor.TRACKING_ENTITY.with(() -> entity),
                    new SyncAbilityPacket(entity.getId(), abilityId, added)
            );
            // 如果实体是玩家自己，也需要给自己发送一份
            if (entity instanceof ServerPlayer serverPlayer) {
                NetworkHandler.INSTANCE.send(
                        PacketDistributor.PLAYER.with(() -> serverPlayer),
                        new SyncAbilityPacket(entity.getId(), abilityId, added)
                );
            }
        }
    }


    public static boolean addAbilityToEntity(LivingEntity entity, String abilityId, int durationTicks) {
        LazyOptional<EntityTimedAbilities> capabilities = entity.getCapability(ENTITY_TIMED_ABILITIES);
        if (capabilities.isPresent()) {
            EntityTimedAbilities abilities = capabilities.orElseThrow(IllegalStateException::new);
            Optional<AbilityDefinition> definition = getAbilityDefinition(abilityId);
            if (definition.isPresent()) {
                AbilityDefinition def = definition.get();
                abilities.addAbility(abilityId, durationTicks, def.applier, def.remover);
                // +++ 新增：同步到客户端 +++ 
                // 只在玩家实体上同步，避免过多网络包
                if (entity instanceof Player) {
                    syncAbilityWithClient(entity, abilityId, true);
                }
                // +++++++++++++++++++++++
                return true;
            }
        }
        return false;
    }

    public static boolean removeAbilityFromEntity(LivingEntity entity, String abilityId) {
        LazyOptional<EntityTimedAbilities> capabilities = entity.getCapability(ENTITY_TIMED_ABILITIES);
        if (capabilities.isPresent()) {
            EntityTimedAbilities abilities = capabilities.orElseThrow(IllegalStateException::new);
            abilities.removeAbility(abilityId, entity);

            // 只在玩家实体上同步，避免过多网络包
            if (entity instanceof Player) {
                syncAbilityWithClient(entity, abilityId, false);
            }

            return true;
        }
        return false;
    }
    // 添加玩家数据持久化事件监听
    @SubscribeEvent
    public static void onPlayerSave(PlayerEvent.SaveToFile event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            // 获取玩家文件
            File playerDir = event.getPlayerDirectory();
            File playerDataFile = new File(playerDir, event.getPlayerUUID().toString() + ".dat");

            // 保存能力数据到玩家NBT
            player.getCapability(ENTITY_TIMED_ABILITIES).ifPresent(abilities -> {
                CompoundTag playerData = player.getPersistentData();
                CompoundTag abilitiesData = abilities.serializeNBT();
                playerData.put("goetydelight_abilities", abilitiesData);
            });
        }
    }

    @SubscribeEvent
    public static void onPlayerLoad(PlayerEvent.LoadFromFile event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            // 从玩家NBT加载能力数据
            CompoundTag playerData = player.getPersistentData();
            if (playerData.contains("goetydelight_abilities")) {
                CompoundTag abilitiesData = playerData.getCompound("goetydelight_abilities");
                player.getCapability(ENTITY_TIMED_ABILITIES).ifPresent(abilities -> {
                    abilities.deserializeNBT(abilitiesData);
                });
            }
        }
    }
}