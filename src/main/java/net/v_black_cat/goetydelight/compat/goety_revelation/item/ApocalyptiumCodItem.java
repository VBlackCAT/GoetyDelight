package net.v_black_cat.goetydelight.compat.goety_revelation.item;

import com.Polarice3.Goety.common.entities.boss.Apostle;
import com.mega.endinglib.api.client.text.TextColorUtils;
import com.mega.revelationfix.common.entity.boss.ApostleServant;
import com.mega.revelationfix.common.init.ModEntities;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.EntityJoinLevelEvent;
import net.minecraftforge.event.entity.living.LivingDropsEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.registries.ForgeRegistries;
import net.v_black_cat.goetydelight.compat.goety_revelation.ApocalyptiumData;
import top.theillusivec4.curios.api.CuriosApi;
import z1gned.goetyrevelation.util.ApollyonAbilityHelper;

import javax.annotation.Nullable;
import java.lang.ref.WeakReference;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class ApocalyptiumCodItem extends Item {

    private static final String PREVENT_DROPS_TAG = "PreventDrops";
    private static final String HALO_OF_THE_END_ID = "goety_revelation:halo_of_the_end";

    public static int SERVANT_LIFETIME = 30 * 60 * 20;
    public static int APOLLYON_DURATION = 5 * 20;

    public static Set<WeakReference<LivingEntity>> TRACKED_ENTITIES = ConcurrentHashMap.newKeySet();

    public static boolean isTrackingActive = false;

    public ApocalyptiumCodItem(Properties properties) {
        super(properties);
        MinecraftForge.EVENT_BUS.register(this);
    }

    @Override
    public ItemStack finishUsingItem(ItemStack stack, Level level, LivingEntity entity) {
        if (!level.isClientSide && entity instanceof Player player) {
            summonApostleServant(player);
            if (!player.getAbilities().instabuild) {
                stack.shrink(1);
            }
        }
        return super.finishUsingItem(stack, level, entity);
    }

    @Override
    public InteractionResult interactLivingEntity(ItemStack stack, Player player, LivingEntity target, InteractionHand hand) {
        Level level = player.level();

        if (!level.isClientSide && target != null) {
            ResourceLocation targetType = ForgeRegistries.ENTITY_TYPES.getKey(target.getType());

            if (targetType != null) {
                String entityId = targetType.toString();

                if (entityId.equals("revelationfix:apostle_servant")) {
                    // 仆从可以直接转化
                    convertToApollyon(target, player);
                    if (!player.getAbilities().instabuild) {
                        stack.shrink(1);
                    }
                    return InteractionResult.SUCCESS;
                } else if (entityId.equals("goety:apostle")) {
                    // 使徒需要特殊条件才能转化
                    if (canConvertApostle(player)) {
                        convertToApollyon(target, player);
                        if (!player.getAbilities().instabuild) {
                            stack.shrink(1);
                        }
                        return InteractionResult.SUCCESS;
                    } else {
                        return InteractionResult.FAIL;
                    }
                }
            }
        }

        return super.interactLivingEntity(stack, player, target, hand);
    }

    private boolean canConvertApostle(Player player) {
        // 创造模式直接允许
        if (player.getAbilities().instabuild) {
            return true;
        }

        // 检查 curios 饰品栏
        return hasHaloOfTheEndInSpecificSlots(player);
    }

    private boolean hasHaloOfTheEndInSpecificSlots(Player player) {
        return CuriosApi.getCuriosInventory(player)
                .resolve()
                .map(handler -> {
                    Set<String> targetSlots = Set.of("head", "curio");

                    for (Map.Entry<String, top.theillusivec4.curios.api.type.inventory.ICurioStacksHandler> entry : handler.getCurios().entrySet()) {
                        if (targetSlots.contains(entry.getKey())) {
                            top.theillusivec4.curios.api.type.inventory.ICurioStacksHandler stacksHandler = entry.getValue();

                            for (int i = 0; i < stacksHandler.getSlots(); i++) {
                                ItemStack stack = stacksHandler.getStacks().getStackInSlot(i);
                                if (!stack.isEmpty()) {
                                    ResourceLocation itemId = ForgeRegistries.ITEMS.getKey(stack.getItem());
                                    if (itemId != null && itemId.toString().equals(HALO_OF_THE_END_ID)) {
                                        return true;
                                    }
                                }
                            }
                        }
                    }
                    return false;
                })
                .orElse(false);
    }

    private void summonApostleServant(Player player) {
        ServerLevel serverLevel = (ServerLevel) player.level();
        ApocalyptiumData data = ApocalyptiumData.get(serverLevel);

        BlockPos spawnPos = player.blockPosition().offset(1, 0, 1);

        ApostleServant servant = new ApostleServant(ModEntities.APOSTLE_SERVANT.get(), serverLevel);
        servant.setPos(spawnPos.getX() + 0.5, spawnPos.getY(), spawnPos.getZ() + 0.5);
        servant.setTrueOwner(player);

        UUID servantUUID = servant.getUUID();
        long expiryTime = serverLevel.getGameTime() + SERVANT_LIFETIME;

        String servantTypeId = ForgeRegistries.ENTITY_TYPES.getKey(servant.getType()).toString();

        ApocalyptiumData.addServantExpiry(player, servantUUID, expiryTime, servantTypeId);
        ApocalyptiumData.addPreventDrop(player, servantUUID);
        data.cacheEntity(servantUUID, servant);

        servant.getPersistentData().putBoolean(PREVENT_DROPS_TAG, true);

        serverLevel.addFreshEntity(servant);
        TRACKED_ENTITIES.add(new WeakReference<>(servant));
        activateTracking();

        player.sendSystemMessage(Component.literal("§6使徒仆从已召唤，将持续30分钟"));
    }

    private void convertToApollyon(LivingEntity target, Player owner) {
        ServerLevel serverLevel = (ServerLevel) target.level();
        ApocalyptiumData data = ApocalyptiumData.get(serverLevel);

        UUID entityUUID = target.getUUID();
        String targetTypeId = ForgeRegistries.ENTITY_TYPES.getKey(target.getType()).toString();

        if (owner != null) {
            ApocalyptiumData.addApollyonExpiry(
                    owner,
                    entityUUID,
                    serverLevel.getGameTime() + APOLLYON_DURATION,
                    targetTypeId
            );
            ApocalyptiumData.addPreventDrop(owner, entityUUID);
        }
        // 关键：缓存 convertToApollyon 中的那个实体，后续 onLivingDrops 就用它
        data.cacheEntity(entityUUID, target);

        // ↓↓↓ 以下转化逻辑完全不变 ↓↓↓
        if (target instanceof Apostle apostle) {
            ApollyonAbilityHelper helper = (ApollyonAbilityHelper) apostle;
            helper.allTitlesApostle_1_20_1$setApollyon(true);
        }

        TRACKED_ENTITIES.add(new WeakReference<>(target));
        activateTracking();

        if (target.level() instanceof ServerLevel level) {
            level.sendParticles(
                    ParticleTypes.ENCHANTED_HIT,
                    target.getX(), target.getY() + 1, target.getZ(),
                    50, 0.5, 0.5, 0.5, 0.1
            );
        }

        if (!target.level().isClientSide) {
            Player nearestPlayer = target.level().getNearestPlayer(target, 10);
            if (nearestPlayer != null) {
                nearestPlayer.sendSystemMessage(Component.literal("§c使徒已暂时转化为亚形态，将持续5分钟"));
            }
        }
        isTrackingActive = true;
    }

    private void restoreFromApollyon(LivingEntity target, Player owner) {
        ServerLevel serverLevel = (ServerLevel) target.level();
        ApocalyptiumData data = ApocalyptiumData.get(serverLevel);
        UUID entityUUID = target.getUUID();

        if (target instanceof Apostle apostle) {
            ApollyonAbilityHelper helper = (ApollyonAbilityHelper) apostle;
            helper.setDoom(false);
            helper.allTitlesApostle_1_20_1$setApollyon(false);
        }

        if (owner != null) {
            ApocalyptiumData.removeApollyonExpiry(owner, entityUUID);
        }
        data.removeCachedEntity(entityUUID);
    }

    private void activateTracking() {
        if (!isTrackingActive) {
            isTrackingActive = true;
        }
    }

    private void deactivateTracking() {
        if (isTrackingActive) {
            isTrackingActive = false;
        }
    }

    /**
     * 寻找实体归属玩家（仅作为兜底，优先用 ApocalyptiumData.findOwnerByPreventDrop）
     */
    private Player findOwnerFor(LivingEntity entity) {
        if (entity instanceof ApostleServant servant) {
            LivingEntity owner = servant.getTrueOwner();
            if (owner instanceof Player player) {
                return player;
            }
        }
        return entity.level().getNearestPlayer(entity, 32);
    }

    // ==================== 掉落阻止：改为遍历在线玩家查 PreventDrops ====================

    @SubscribeEvent
    public void onLivingDrops(LivingDropsEvent event) {
        LivingEntity entity = event.getEntity();
        UUID entityUUID = entity.getUUID();

        if (entity.level() instanceof ServerLevel serverLevel) {
            ApocalyptiumData data = ApocalyptiumData.get(serverLevel);

            // 1. 遍历在线玩家，找出哪个玩家的 PreventDrops 锁定了这个 UUID
            Player owner = ApocalyptiumData.findOwnerByPreventDrop(serverLevel, entityUUID);
            if (owner == null) {
                // 没有玩家锁定这个实体，不做处理
                cleanupDeadReferences();
                return;
            }

            // 2. 确认要阻止掉落
            if (ApocalyptiumData.shouldPreventDrop(owner, entityUUID)) {
                event.setCanceled(true);
            }

            // 3. 拿到 convertToApollyon / summonApostleServant 中保存的那个实体
            //    优先用运行时缓存，缓存没有再按 UUID 从世界拿
            LivingEntity lockedEntity = data.getCachedEntity(entityUUID);
            if (lockedEntity == null) {
                Entity worldEntity = serverLevel.getEntity(entityUUID);
                if (worldEntity instanceof LivingEntity le) {
                    lockedEntity = le;
                    data.cacheEntity(entityUUID, le);
                }
            }

            // 4. 清理该玩家身上关于这个实体的所有记录
            ApocalyptiumData.cleanupEntity(owner, entityUUID);
            data.removeCachedEntity(entityUUID);
        }
        cleanupDeadReferences();
    }

    // ==================== 实体重新加载：同样用 PreventDrops 反查 owner ====================

    @SubscribeEvent
    public void onEntityJoinLevel(EntityJoinLevelEvent event) {
        if (event.getLevel().isClientSide()) return;
        if (!(event.getEntity() instanceof LivingEntity living)) return;
        if (!(event.getLevel() instanceof ServerLevel serverLevel)) return;

        UUID uuid = living.getUUID();
        ApocalyptiumData data = ApocalyptiumData.get(serverLevel);

        // 用 PreventDrops 反查 owner，而不是按距离找
        Player owner = ApocalyptiumData.findOwnerByPreventDrop(serverLevel, uuid);
        if (owner == null) return;

        // 补缓存（重新加载时把世界里的实体引用补进缓存）
        data.cacheEntity(uuid, living);

        // 如果加载时已经过期，立刻处理
        long apollyonExpiry = ApocalyptiumData.getApollyonExpiry(owner, uuid);
        if (apollyonExpiry > 0 && serverLevel.getGameTime() >= apollyonExpiry) {
            restoreFromApollyon(living, owner);
            serverLevel.sendParticles(
                    ParticleTypes.SMOKE,
                    living.getX(), living.getY() + 1, living.getZ(),
                    30, 0.5, 0.5, 0.5, 0.05
            );
        }

        long servantExpiry = ApocalyptiumData.getServantExpiry(owner, uuid);
        if (servantExpiry > 0 && serverLevel.getGameTime() >= servantExpiry) {
            living.remove(Entity.RemovalReason.DISCARDED);
            ApocalyptiumData.cleanupEntity(owner, uuid);
            data.removeCachedEntity(uuid);
        }
    }

    private void cleanupDeadReferences() {
        Iterator<WeakReference<LivingEntity>> iterator = TRACKED_ENTITIES.iterator();
        while (iterator.hasNext()) {
            WeakReference<LivingEntity> ref = iterator.next();
            LivingEntity entity = ref.get();
            if (entity == null || entity.isRemoved() || !entity.isAlive()) {
                iterator.remove();
            }
        }

        if (TRACKED_ENTITIES.isEmpty()) {
            deactivateTracking();
        }
    }

    @SubscribeEvent
    public void onLevelTick(TickEvent.LevelTickEvent event) {
        if (!isTrackingActive) {
            return;
        }

        if (event.phase != TickEvent.Phase.END || event.level.isClientSide) {
            return;
        }

        ServerLevel serverLevel = (ServerLevel) event.level;
        ApocalyptiumData data = ApocalyptiumData.get(serverLevel);
        long currentTime = serverLevel.getGameTime();

        for (Player player : serverLevel.getServer().getPlayerList().getPlayers()) {
            if (ApocalyptiumData.isEmpty(player)) continue;

            // 处理仆从过期
            for (Map.Entry<UUID, CompoundTag> entry : ApocalyptiumData.getServantExpirySnapshot(player).entrySet()) {
                long expiry = entry.getValue().getLong("Time");
                if (currentTime >= expiry) {
                    UUID entityUUID = entry.getKey();

                    LivingEntity livingEntity = data.getCachedEntity(entityUUID);
                    if (livingEntity == null) {
                        Entity entity = serverLevel.getEntity(entityUUID);
                        if (entity instanceof LivingEntity le) {
                            livingEntity = le;
                        }
                    }

                    if (livingEntity != null) {
                        livingEntity.remove(Entity.RemovalReason.DISCARDED);
                    }
                    ApocalyptiumData.cleanupEntity(player, entityUUID);
                    data.removeCachedEntity(entityUUID);
                }
            }

            // 处理亚形态过期
            for (Map.Entry<UUID, CompoundTag> entry : ApocalyptiumData.getApollyonExpirySnapshot(player).entrySet()) {
                long expiry = entry.getValue().getLong("Time");
                if (currentTime >= expiry) {
                    UUID entityUUID = entry.getKey();

                    LivingEntity livingEntity = data.getCachedEntity(entityUUID);
                    if (livingEntity == null) {
                        Entity entity = serverLevel.getEntity(entityUUID);
                        if (entity instanceof LivingEntity le) {
                            livingEntity = le;
                            data.cacheEntity(entityUUID, le);
                        }
                    }

                    if (livingEntity != null) {
                        restoreFromApollyon(livingEntity, player);
                        if (livingEntity.level() instanceof ServerLevel entityLevel) {
                            entityLevel.sendParticles(
                                    ParticleTypes.SMOKE,
                                    livingEntity.getX(), livingEntity.getY() + 1, livingEntity.getZ(),
                                    30, 0.5, 0.5, 0.5, 0.05
                            );
                        }
                    }
                    ApocalyptiumData.removeApollyonExpiry(player, entityUUID);
                    data.removeCachedEntity(entityUUID);
                }
            }
        }

        // 清理无效引用
        Iterator<WeakReference<LivingEntity>> iterator = TRACKED_ENTITIES.iterator();
        while (iterator.hasNext()) {
            WeakReference<LivingEntity> ref = iterator.next();
            LivingEntity livingEntity = ref.get();

            if (livingEntity == null || livingEntity.isRemoved() || !livingEntity.isAlive()) {
                iterator.remove();
            }
        }

        // 检查是否需要停用追踪
        if (TRACKED_ENTITIES.isEmpty()) {
            deactivateTracking();
        }
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltip, TooltipFlag flag) {
        tooltip.add(Component.literal(""));
        if (Screen.hasShiftDown()) {
            tooltip.add(Component.translatable("item.goetydelight.apocalyptium_cod.tooltip.1")
                    .withStyle(ChatFormatting.LIGHT_PURPLE));
            tooltip.add(Component.translatable("item.goetydelight.apocalyptium_cod.tooltip.2")
                    .withStyle(ChatFormatting.LIGHT_PURPLE));
            tooltip.add(Component.translatable("item.goetydelight.apocalyptium_cod.tooltip.3")
                    .withStyle(ChatFormatting.LIGHT_PURPLE));
        } else {
            tooltip.add(Component.translatable("item.goetydelight.tooltip.shift"));
        }
        tooltip.add(Component.literal(""));
        tooltip.add(Component.translatable("item.goetydelight.apocalyptium_cod.tooltip.4")
                .withStyle(TextColorUtils.MIDDLE)
                .withStyle(ChatFormatting.DARK_GRAY));
        tooltip.add(Component.translatable("item.goetydelight.apocalyptium_cod.tooltip.5")
                .withStyle(TextColorUtils.MIDDLE)
                .withStyle(ChatFormatting.DARK_GRAY));
        tooltip.add(Component.translatable("item.goetydelight.apocalyptium_cod.tooltip.6")
                .withStyle(TextColorUtils.MIDDLE)
                .withStyle(ChatFormatting.DARK_GRAY));
        tooltip.add(Component.translatable("item.goetydelight.apocalyptium_cod.tooltip.7")
                .withStyle(TextColorUtils.MIDDLE)
                .withStyle(ChatFormatting.DARK_GRAY));
    }
}