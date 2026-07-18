package net.v_black_cat.goetydelight.item.food;

import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LightningBolt;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraft.world.entity.animal.Fox;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.InteractionResult;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.event.entity.living.LivingDamageEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.v_black_cat.goetydelight.item.ModItems;
import net.v_black_cat.goetydelight.network.NetworkHandler;
import net.v_black_cat.goetydelight.network.SyncFoxKillCountPacket;
import net.v_black_cat.goetydelight.util.GetKillCount;

import javax.annotation.Nullable;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.*;

public class CherryBlossomCakeItem extends Item {
    private static final UUID ATTACK_DAMAGE_UUID = UUID.fromString("15f2e8d8-2ff0-4915-8039-a6807c993b51");
    private static final String ATTACK_DAMAGE_NAME = "Cherry Blossom Cake Attack Boost";

    // NBT键名常量
    private static final String NBT_LAST_USAGE_TIME = "LastUsageTime";
    private static final String NBT_PUNISHMENT_TIME = "CherryBlossomPunishmentTime";
    private static final String NBT_PUNISHMENT_COUNT = "CherryBlossomPunishmentCount";
    private static final String NBT_PENDING_DAMAGE = "CherryBlossomPendingDamage";
    private static final String NBT_HAS_DAMAGE = "HasCherryBlossomDamage";
    private static final String NBT_INTERACTIONS = "cherry_blossom_interactions";
    private static final String NBT_RECORDED_FOXES = "recorded_foxes";
    private static final String NBT_LAST_INTERACTION_DAY = "last_interaction_day";
    private static final String NBT_UUID = "uuid";

    // 常量定义
    private static final int MAX_FOX_INTERACTIONS = 5;
    private static final int MAX_PUNISHMENT_COUNT = 3;
    private static final int PUNISHMENT_INTERVAL_TICKS = 20;
    private static final int SYNC_INTERVAL_TICKS = 100;
    private static final int SERVER_TICK_INTERVAL = 20;
    private static final double DAMAGE_PERCENTAGE = 0.24;
    private static final double ATTACK_BOOST_MULTIPLIER = 0.002;
    private static final long DAY_CYCLE = 24000L;

    public CherryBlossomCakeItem(Properties pProperties) {
        super(pProperties);
    }

    @Override
    public ItemStack finishUsingItem(ItemStack stack, Level level, LivingEntity entity) {
        ItemStack resultStack = super.finishUsingItem(stack, level, entity);

        if (level.isClientSide || !(entity instanceof ServerPlayer serverPlayer)) {
            return resultStack;
        }

        int foxKillCount = GetKillCount.getKillCount(serverPlayer, EntityType.FOX);

        if (foxKillCount == 0 || isMorningWindowActive(serverPlayer)) {
            applyAttackBoost(entity);

            if (foxKillCount > 0) {
                clearLastUsageTimestamp(serverPlayer);
            }
        } else {
            // 初始化惩罚并立即执行第一次闪电
            CompoundTag tag = entity.getPersistentData();
            tag.putLong(NBT_PUNISHMENT_TIME, level.getGameTime());
            tag.putInt(NBT_PUNISHMENT_COUNT, 0);

            spawnLightningAndDamage(entity, serverPlayer, level);
        }
        return resultStack;
    }

    private void applyAttackBoost(LivingEntity entity) {
        removeAttackDamageBoost(entity);

        AttributeInstance attackAttribute = entity.getAttribute(Attributes.ATTACK_DAMAGE);
        if (attackAttribute == null) return;

        AttributeInstance luckAttribute = entity.getAttribute(Attributes.LUCK);
        double luckValue = luckAttribute != null ? luckAttribute.getValue() : 0;
        double attackBoost = luckValue + attackAttribute.getValue() * ATTACK_BOOST_MULTIPLIER;

        addAttackDamageBoost(entity, attackBoost);
    }

    private static boolean isMorningWindowActive(ServerPlayer player) {
        CompoundTag tag = player.getPersistentData();
        if (!tag.contains(NBT_LAST_USAGE_TIME)) {
            return false;
        }

        long lastUsageTime = tag.getLong(NBT_LAST_USAGE_TIME);
        LocalDateTime now = LocalDateTime.now();
        LocalTime currentTime = now.toLocalTime();

        LocalTime windowStart = LocalTime.of(5, 20);
        LocalTime windowEnd = LocalTime.of(5, 21);

        if (currentTime.isBefore(windowStart) || !currentTime.isBefore(windowEnd)) {
            return false;
        }

        LocalDateTime lastUsage = LocalDateTime.ofInstant(
                java.time.Instant.ofEpochMilli(lastUsageTime),
                java.time.ZoneId.systemDefault()
        );

        return !lastUsage.toLocalDate().isEqual(now.toLocalDate());
    }

    private static void clearLastUsageTimestamp(ServerPlayer player) {
        player.getPersistentData().remove(NBT_LAST_USAGE_TIME);
    }

    private static void spawnLightningAndDamage(LivingEntity entity, @Nullable Player player, Level level) {
        if (level.isClientSide) return;

        LightningBolt lightning = new LightningBolt(EntityType.LIGHTNING_BOLT, level);
        lightning.setPos(entity.getX(), entity.getY(), entity.getZ());
        level.addFreshEntity(lightning);

        AttributeInstance maxHealthAttr = entity.getAttribute(Attributes.MAX_HEALTH);
        if (maxHealthAttr == null) return;

        double damageAmount = maxHealthAttr.getValue() * DAMAGE_PERCENTAGE;

        CompoundTag tag = entity.getPersistentData();
        tag.putDouble(NBT_PENDING_DAMAGE, damageAmount);
        tag.putBoolean(NBT_HAS_DAMAGE, true);

        if (player != null) {
            player.displayClientMessage(
                    Component.translatable("message.goetydelight.cherryblossomcake.punishment")
                            .withStyle(ChatFormatting.DARK_RED),
                    true
            );
        }
    }

    private void addAttackDamageBoost(LivingEntity entity, double boostAmount) {
        if (boostAmount <= 0) return;

        AttributeInstance attackDamage = entity.getAttribute(Attributes.ATTACK_DAMAGE);
        if (attackDamage != null) {
            AttributeModifier modifier = new AttributeModifier(
                    ATTACK_DAMAGE_UUID,
                    ATTACK_DAMAGE_NAME,
                    boostAmount,
                    AttributeModifier.Operation.ADDITION
            );
            attackDamage.addTransientModifier(modifier);
        }
    }

    private void removeAttackDamageBoost(LivingEntity entity) {
        AttributeInstance attackDamage = entity.getAttribute(Attributes.ATTACK_DAMAGE);
        if (attackDamage != null && attackDamage.getModifier(ATTACK_DAMAGE_UUID) != null) {
            attackDamage.removeModifier(ATTACK_DAMAGE_UUID);
        }
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public void appendHoverText(ItemStack stack, Level world, List<Component> tooltipComponents, TooltipFlag isAdvanced) {
        LocalPlayer player = Minecraft.getInstance().player;
        if (player == null) return;

        int foxKillCount = net.v_black_cat.goetydelight.network.ClientHandle.getCachedFoxKillCount();
        boolean windowActive = isClientMorningWindowActive(player);

        String translationKey = (foxKillCount == 0 || windowActive)
                ? "tooltip.goetydelight.cherry_blossom_cake_good"
                : "tooltip.goetydelight.cherry_blossom_cake_bad";

        ChatFormatting color = (foxKillCount == 0 || windowActive)
                ? ChatFormatting.GOLD
                : ChatFormatting.DARK_RED;

        tooltipComponents.add(Component.translatable(translationKey).withStyle(color));
    }

    @OnlyIn(Dist.CLIENT)
    private boolean isClientMorningWindowActive(LocalPlayer player) {
        CompoundTag tag = player.getPersistentData();
        if (!tag.contains(NBT_LAST_USAGE_TIME)) {
            return false;
        }

        long lastUsageTime = tag.getLong(NBT_LAST_USAGE_TIME);
        LocalDateTime now = LocalDateTime.now();
        LocalTime currentTime = now.toLocalTime();

        LocalTime windowStart = LocalTime.of(5, 20);
        LocalTime windowEnd = LocalTime.of(5, 21);

        if (currentTime.isBefore(windowStart) || !currentTime.isBefore(windowEnd)) {
            return false;
        }

        LocalDateTime lastUsage = LocalDateTime.ofInstant(
                java.time.Instant.ofEpochMilli(lastUsageTime),
                java.time.ZoneId.systemDefault()
        );

        return !lastUsage.toLocalDate().isEqual(now.toLocalDate());
    }

    @Mod.EventBusSubscriber
    public static class CherryBlossomCakeEventHandler {
        // 缓存有惩罚状态的实体UUID，减少NBT查询
        private static final Set<UUID> activePunishments = new HashSet<>();

        @SubscribeEvent(priority = EventPriority.LOWEST)
        public static void onLivingDamage(LivingDamageEvent event) {
            CompoundTag tag = event.getEntity().getPersistentData();
            if (tag.getBoolean(NBT_HAS_DAMAGE)) {
                double pendingDamage = tag.getDouble(NBT_PENDING_DAMAGE);
                event.setAmount((float)(event.getAmount() + pendingDamage));
                tag.remove(NBT_PENDING_DAMAGE);
                tag.remove(NBT_HAS_DAMAGE);
            }
        }

        @SubscribeEvent
        public static void onPlayerLoggedIn(PlayerEvent.PlayerLoggedInEvent event) {
            if (event.getEntity() instanceof ServerPlayer player && !player.level().isClientSide) {
                syncFoxKillCount(player);
            }
        }

        @SubscribeEvent
        public static void onPlayerTick(TickEvent.PlayerTickEvent event) {
            if (event.phase != TickEvent.Phase.END || event.player.level().isClientSide) return;
            if (!(event.player instanceof ServerPlayer player)) return;

            if (player.tickCount % SYNC_INTERVAL_TICKS == 0) {
                syncFoxKillCount(player);
            }
        }

        private static void syncFoxKillCount(ServerPlayer player) {
            int foxKillCount = GetKillCount.getKillCount(player, EntityType.FOX);
            NetworkHandler.sendToClient(new SyncFoxKillCountPacket(foxKillCount), player);
        }

        @SubscribeEvent
        public static void onPlayerAttack(LivingHurtEvent event) {
            if (!(event.getSource().getEntity() instanceof ServerPlayer player)) return;
            if (!(player.level() instanceof ServerLevel)) return;
            if (GetKillCount.getKillCount(player, EntityType.FOX) != 0) return;

            AttributeInstance attackDamage = player.getAttribute(Attributes.ATTACK_DAMAGE);
            if (attackDamage != null && attackDamage.getModifier(ATTACK_DAMAGE_UUID) != null) {
                attackDamage.removeModifier(ATTACK_DAMAGE_UUID);
            }
        }

        @SubscribeEvent
        public static void onLivingDeath(LivingDeathEvent event) {
            if (event.getEntity().getType() != EntityType.FOX) return;
            if (!(event.getSource().getEntity() instanceof ServerPlayer player)) return;

            int foxKillCount = GetKillCount.getKillCount(player, EntityType.FOX);

            // 记录击杀时的现实时间
            CompoundTag tag = player.getPersistentData();
            tag.putLong(NBT_LAST_USAGE_TIME, System.currentTimeMillis());

            if (foxKillCount <= 1) {
                player.displayClientMessage(
                        Component.translatable("message.goetydelight.cherryblossomcake.angry")
                                .withStyle(ChatFormatting.RED),
                        true
                );
                syncFoxKillCount(player);
            }
        }

        @SubscribeEvent
        public static void onEntityInteract(PlayerInteractEvent.EntityInteract event) {
            Player player = event.getEntity();
            ItemStack stack = event.getItemStack();

            if (stack.getItem() != net.minecraft.world.item.Items.PINK_PETALS) return;
            if (!(event.getTarget() instanceof Fox fox)) return;

            // 客户端提前返回
            if (player.level().isClientSide()) {
                event.setCancellationResult(InteractionResult.sidedSuccess(true));
                event.setCanceled(true);
                return;
            }

            ServerPlayer serverPlayer = (ServerPlayer) player;

            // 击杀过狐狸的玩家不能互动
            if (GetKillCount.getKillCount(serverPlayer, EntityType.FOX) != 0) return;

            int interactionCount = getInteractionCount(fox);

            if (interactionCount < MAX_FOX_INTERACTIONS) {
                // 消耗花瓣
                if (!player.getAbilities().instabuild) {
                    stack.shrink(1);
                }

                incrementInteractionCount(fox, serverPlayer);

                // 生成蛋糕
                fox.spawnAtLocation(new ItemStack(ModItems.CHERRY_BLOSSOM_CAKE.get()));

                // 粒子效果
                spawnHeartParticles(fox);

                // 声音效果
                player.level().playSound(null, fox.getX(), fox.getY(), fox.getZ(),
                        SoundEvents.FOX_EAT, fox.getSoundSource(), 1.0F, 1.0F);
            }

            // 交互消息
            sendInteractionMessage(player, interactionCount);

            event.setCancellationResult(InteractionResult.sidedSuccess(false));
            event.setCanceled(true);
        }

        private static void spawnHeartParticles(Fox fox) {
            Level level = fox.level();
            for (int i = 0; i < 7; i++) {
                level.addParticle(ParticleTypes.HEART,
                        fox.getX() + level.random.nextFloat() * fox.getBbWidth() * 2.0F - fox.getBbWidth(),
                        fox.getY() + 0.5D + level.random.nextFloat() * fox.getBbHeight(),
                        fox.getZ() + level.random.nextFloat() * fox.getBbWidth() * 2.0F - fox.getBbWidth(),
                        level.random.nextGaussian() * 0.02D,
                        level.random.nextGaussian() * 0.02D,
                        level.random.nextGaussian() * 0.02D
                );
            }
        }

        private static void sendInteractionMessage(Player player, int interactionCount) {
            int nextCount = interactionCount + 1;
            if (nextCount == MAX_FOX_INTERACTIONS) {
                player.displayClientMessage(
                        Component.translatable("message.goetydelight.cherryblossomcake.max_interactions")
                                .withStyle(ChatFormatting.GOLD), true);
            } else if (nextCount > MAX_FOX_INTERACTIONS) {
                player.displayClientMessage(
                        Component.translatable("message.goetydelight.cherryblossomcake.already_max")
                                .withStyle(ChatFormatting.GOLD), true);
            }
        }

        @SubscribeEvent
        public static void onServerTick(TickEvent.ServerTickEvent event) {
            if (event.phase != TickEvent.Phase.START) return;
            if (event.getServer().getTickCount() % SERVER_TICK_INTERVAL != 0) return;

            ServerLevel overworld = event.getServer().overworld();
            if (overworld == null) return;

            long currentDay = overworld.getDayTime() / DAY_CYCLE;

            for (ServerLevel level : event.getServer().getAllLevels()) {
                if (level.players().isEmpty()) continue;

                long levelGameTime = level.getGameTime();

                for (net.minecraft.world.entity.Entity entity : level.getAllEntities()) {
                    CompoundTag persistentData = entity.getPersistentData();

                    // 处理狐狸交互重置
                    if (entity instanceof Fox && persistentData.contains(NBT_RECORDED_FOXES)) {
                        processFoxReset((Fox) entity, persistentData, currentDay);
                    }

                    // 处理惩罚闪电
                    if (persistentData.contains(NBT_PUNISHMENT_TIME)) {
                        processPunishmentLightning(entity, persistentData, level, levelGameTime);
                    }
                }
            }
        }

        private static void processFoxReset(Fox fox, CompoundTag persistentData, long currentDay) {
            ListTag recordedFoxes = persistentData.getList(NBT_RECORDED_FOXES, Tag.TAG_COMPOUND);

            if (recordedFoxes.isEmpty()) {
                persistentData.remove(NBT_RECORDED_FOXES);
                return;
            }

            // 检查是否有过期记录
            for (int i = 0; i < recordedFoxes.size(); i++) {
                CompoundTag foxData = recordedFoxes.getCompound(i);
                if (currentDay > foxData.getLong(NBT_LAST_INTERACTION_DAY)) {
                    persistentData.putInt(NBT_INTERACTIONS, 0);
                    persistentData.remove(NBT_RECORDED_FOXES);
                    return;
                }
            }
        }

        private static void processPunishmentLightning(net.minecraft.world.entity.Entity entity,
                                                       CompoundTag tag,
                                                       ServerLevel level,
                                                       long currentTime) {
            long startTime = tag.getLong(NBT_PUNISHMENT_TIME);
            int count = tag.getInt(NBT_PUNISHMENT_COUNT);

            // 清理已完成的惩罚
            if (count >= MAX_PUNISHMENT_COUNT) {
                tag.remove(NBT_PUNISHMENT_TIME);
                tag.remove(NBT_PUNISHMENT_COUNT);
                activePunishments.remove(entity.getUUID());
                return;
            }

            // 检查是否到达触发时间
            long nextTriggerTime = startTime + (long) (count + 1) * PUNISHMENT_INTERVAL_TICKS;

            if (currentTime >= nextTriggerTime) {
                if (entity instanceof LivingEntity livingEntity) {
                    Player targetPlayer = entity instanceof Player ? (Player) entity : null;
                    spawnLightningAndDamage(livingEntity, targetPlayer, level);
                }

                count++;

                if (count >= MAX_PUNISHMENT_COUNT) {
                    tag.remove(NBT_PUNISHMENT_TIME);
                    tag.remove(NBT_PUNISHMENT_COUNT);
                    activePunishments.remove(entity.getUUID());
                } else {
                    tag.putInt(NBT_PUNISHMENT_COUNT, count);
                    activePunishments.add(entity.getUUID());
                }
            }
        }

        private static int getInteractionCount(Fox fox) {
            return fox.getPersistentData().getInt(NBT_INTERACTIONS);
        }

        private static void incrementInteractionCount(Fox fox, ServerPlayer player) {
            CompoundTag persistentData = fox.getPersistentData();
            int count = persistentData.getInt(NBT_INTERACTIONS);
            persistentData.putInt(NBT_INTERACTIONS, count + 1);

            UUID foxUUID = fox.getUUID();
            long currentDay = player.level().getDayTime() / DAY_CYCLE;

            ListTag recordedFoxes = persistentData.getList(NBT_RECORDED_FOXES, Tag.TAG_COMPOUND);

            // 更新已存在的记录
            for (int i = 0; i < recordedFoxes.size(); i++) {
                CompoundTag foxData = recordedFoxes.getCompound(i);
                if (foxUUID.toString().equals(foxData.getString(NBT_UUID))) {
                    foxData.putLong(NBT_LAST_INTERACTION_DAY, currentDay);
                    return;
                }
            }

            // 添加新记录
            CompoundTag foxData = new CompoundTag();
            foxData.putString(NBT_UUID, foxUUID.toString());
            foxData.putLong(NBT_LAST_INTERACTION_DAY, currentDay);
            recordedFoxes.add(foxData);
            persistentData.put(NBT_RECORDED_FOXES, recordedFoxes);
        }
    }
}