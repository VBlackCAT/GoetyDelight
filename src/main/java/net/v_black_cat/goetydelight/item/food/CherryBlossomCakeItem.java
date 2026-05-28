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
import net.minecraft.stats.Stat;
import net.minecraft.stats.Stats;
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
import net.minecraftforge.network.NetworkDirection;
import net.v_black_cat.goetydelight.item.ModItems;
import net.v_black_cat.goetydelight.network.NetworkHandler;
import net.v_black_cat.goetydelight.network.SyncFoxKillCountPacket;
import net.v_black_cat.goetydelight.util.EntityUtil;
import net.v_black_cat.goetydelight.util.GetKillCount;

import javax.annotation.Nullable;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

public class CherryBlossomCakeItem extends Item {
    // UUID用于攻击力修饰符
    private static final UUID ATTACK_DAMAGE_UUID = UUID.fromString("15f2e8d8-2ff0-4915-8039-a6807c993b51");
    // 用于标识临时攻击力加成的名称
    private static final String ATTACK_DAMAGE_NAME = "Cherry Blossom Cake Attack Boost";

    public CherryBlossomCakeItem(Properties pProperties) {
        super(pProperties);
    }

    @Override
    public ItemStack finishUsingItem(ItemStack stack, Level level, LivingEntity entity) {
        ItemStack resultStack = super.finishUsingItem(stack, level, entity);

        if (!level.isClientSide && entity instanceof Player player) {
            ServerPlayer serverPlayer = (ServerPlayer) player;
            int foxKillCount = GetKillCount.getKillCount(serverPlayer, EntityType.FOX);

            if (foxKillCount == 0 || isMorningWindowActive(serverPlayer, level)) {
                removeAttackDamageBoost(entity);

                AttributeInstance luckAttribute = entity.getAttribute(Attributes.LUCK);
                AttributeInstance attackAttribute = entity.getAttribute(Attributes.ATTACK_DAMAGE);
                double luckValue = luckAttribute != null ? luckAttribute.getValue() : 0;

                double attackBoost = 0;
                if (attackAttribute != null) {
                    attackBoost = luckValue + attackAttribute.getValue() * 0.002;
                }

                addAttackDamageBoost(entity, attackBoost);

                if (foxKillCount > 0) {
                    clearLastUsageTimestamp(serverPlayer);
                }
            } else {
                spawnLightningAndDamage(entity, player, level);

                CompoundTag tag = entity.getPersistentData();
                tag.putLong("CherryBlossomPunishmentTime", level.getGameTime());
                tag.putInt("CherryBlossomPunishmentCount", 0);
            }
        }
        return resultStack;
    }

    private static boolean isMorningWindowActive(ServerPlayer player, Level level) {
        CompoundTag tag = player.getPersistentData();
        if (!tag.contains("LastUsageTime")) {
            return false;
        }

        long lastUsageTime = tag.getLong("LastUsageTime");
        LocalDateTime now = LocalDateTime.now();
        LocalTime currentTime = now.toLocalTime();

        LocalTime windowStart = LocalTime.of(5, 20);
        LocalTime windowEnd = LocalTime.of(5, 21);

        boolean isInWindow = !currentTime.isBefore(windowStart) && currentTime.isBefore(windowEnd);

        if (!isInWindow) {
            return false;
        }

        LocalDateTime lastUsage = LocalDateTime.ofInstant(
                java.time.Instant.ofEpochMilli(lastUsageTime),
                java.time.ZoneId.systemDefault()
        );

        return !lastUsage.toLocalDate().isEqual(now.toLocalDate());
    }

    private static void clearLastUsageTimestamp(ServerPlayer player) {
        CompoundTag tag = player.getPersistentData();
        tag.remove("LastUsageTime");
    }

    private static void spawnLightningAndDamage(LivingEntity entity, Player player, Level level) {
        if (!level.isClientSide) {
            LightningBolt lightning = new LightningBolt(EntityType.LIGHTNING_BOLT, level);
            lightning.setPos(entity.getX(), entity.getY(), entity.getZ());
            level.addFreshEntity(lightning);

            double maxHealth = Objects.requireNonNull(entity.getAttribute(Attributes.MAX_HEALTH)).getValue();
            double damageAmount = maxHealth * 0.24;

            CompoundTag tag = entity.getPersistentData();
            tag.putDouble("CherryBlossomPendingDamage", damageAmount);
            tag.putBoolean("HasCherryBlossomDamage", true);

            if (entity instanceof Player p) {
                p.displayClientMessage(Component.translatable("message.goetydelight.cherryblossomcake.punishment").withStyle(ChatFormatting.DARK_RED), true);
            }
        }
    }


    private void addAttackDamageBoost(LivingEntity entity, double boostAmount) {
        AttributeInstance attackDamage = entity.getAttribute(Attributes.ATTACK_DAMAGE);
        if (attackDamage != null && boostAmount > 0) {
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
        if (player == null) {
            return;
        }

        int foxKillCount = net.v_black_cat.goetydelight.network.ClientHandle.getCachedFoxKillCount();
        boolean windowActive = isClientMorningWindowActive(player);

        if (foxKillCount == 0 || windowActive) {
            tooltipComponents.add(Component.translatable("tooltip.goetydelight.cherry_blossom_cake_good").withStyle(ChatFormatting.GOLD));
        } else {
            tooltipComponents.add(Component.translatable("tooltip.goetydelight.cherry_blossom_cake_bad").withStyle(ChatFormatting.DARK_RED));
        }
    }

    @OnlyIn(Dist.CLIENT)
    private boolean isClientMorningWindowActive(LocalPlayer player) {
        CompoundTag tag = player.getPersistentData();
        if (!tag.contains("LastUsageTime")) {
            return false;
        }

        long lastUsageTime = tag.getLong("LastUsageTime");
        LocalDateTime now = LocalDateTime.now();
        LocalTime currentTime = now.toLocalTime();

        LocalTime windowStart = LocalTime.of(5, 20);
        LocalTime windowEnd = LocalTime.of(5, 21);

        boolean isInWindow = !currentTime.isBefore(windowStart) && currentTime.isBefore(windowEnd);

        if (!isInWindow) {
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
        @SubscribeEvent(priority = EventPriority.LOWEST)
        public static void onLivingDamage(LivingDamageEvent event) {
            LivingEntity entity = event.getEntity();
            CompoundTag tag = entity.getPersistentData();

            if (tag.getBoolean("HasCherryBlossomDamage")) {
                double pendingDamage = tag.getDouble("CherryBlossomPendingDamage");

                event.setAmount((float)(event.getAmount() + pendingDamage));

                tag.remove("CherryBlossomPendingDamage");
                tag.remove("HasCherryBlossomDamage");
            }
        }

        @SubscribeEvent
        public static void onPlayerLoggedIn(PlayerEvent.PlayerLoggedInEvent event) {
            if (!event.getEntity().level().isClientSide && event.getEntity() instanceof ServerPlayer player) {
                int foxKillCount = GetKillCount.getKillCount(player, EntityType.FOX);
                NetworkHandler.sendToClient(
                        new SyncFoxKillCountPacket(foxKillCount),
                        player
                );
            }
        }
        @SubscribeEvent
        public static void onPlayerTick(TickEvent.PlayerTickEvent event) {
            if (!event.player.level().isClientSide && event.player instanceof ServerPlayer player && event.phase == TickEvent.Phase.END) {
                if (player.tickCount % 100 == 0) {
                    int foxKillCount = GetKillCount.getKillCount(player, EntityType.FOX);
                    NetworkHandler.sendToClient(
                            new SyncFoxKillCountPacket(foxKillCount),
                            player
                    );
                }
            }
        }
        @SubscribeEvent
        public static void onPlayerAttack(LivingHurtEvent event) {
            if (event.getSource().getEntity() instanceof Player && event.getSource().getEntity().level() instanceof ServerLevel){
                if (GetKillCount.getKillCount((ServerPlayer) event.getSource().getEntity(), EntityType.FOX) == 0){
                    // 检查攻击者是否有樱桃蛋糕的攻击力加成
                    if (event.getSource().getEntity() instanceof LivingEntity attacker) {
                        AttributeInstance attackDamage = attacker.getAttribute(Attributes.ATTACK_DAMAGE);
                        if (attackDamage != null && attackDamage.getModifier(ATTACK_DAMAGE_UUID) != null) {
                            // 移除加成（仅对一次攻击生效）
                            attackDamage.removeModifier(ATTACK_DAMAGE_UUID);
                        }
                    }}}
        }

        @SubscribeEvent
        public static void onLivingDeath(LivingDeathEvent event) {
            if (event.getEntity().getType() == EntityType.FOX && event.getSource().getEntity() instanceof Player) {
                Player player = (Player) event.getSource().getEntity();
                if (player.level() instanceof ServerLevel serverLevel) {
                    ServerPlayer serverPlayer = (ServerPlayer) player;
                    int foxKillCount = GetKillCount.getKillCount(serverPlayer, EntityType.FOX);

                    CompoundTag tag = player.getPersistentData();
                    tag.putLong("LastUsageTime", System.currentTimeMillis());

                    if (foxKillCount <= 1) {
                        player.displayClientMessage(Component.translatable("message.goetydelight.cherryblossomcake.angry").withStyle(ChatFormatting.RED), true);
                        NetworkHandler.sendToClient(
                                new SyncFoxKillCountPacket(foxKillCount),
                                serverPlayer
                        );
                    }
                }
            }
        }

        @SubscribeEvent
        public static void onEntityInteract(PlayerInteractEvent.EntityInteract event) {
            Player player = event.getEntity();
            ItemStack stack = event.getItemStack();
            Level level = player.level();

            if (stack.getItem() == net.minecraft.world.item.Items.PINK_PETALS &&
                    event.getTarget() instanceof Fox fox) {
                if (!level.isClientSide()) {
                    ServerPlayer serverPlayer = (ServerPlayer) player;

                    if (GetKillCount.getKillCount(serverPlayer, EntityType.FOX) == 0) {
                        int interactionCount = getInteractionCount(fox);

                        if (interactionCount < 5) {
                            if (!player.getAbilities().instabuild) {
                                stack.shrink(1);
                            }

                            incrementInteractionCount(fox, serverPlayer);

                            ItemStack cherryBlossomCake = new ItemStack(ModItems.CHERRY_BLOSSOM_CAKE.get());
                            fox.spawnAtLocation(cherryBlossomCake);

                            for (int i = 0; i < 7; ++i) {
                                double d0 = level.random.nextGaussian() * 0.02D;
                                double d1 = level.random.nextGaussian() * 0.02D;
                                double d2 = level.random.nextGaussian() * 0.02D;
                                level.addParticle(ParticleTypes.HEART,
                                        fox.getX() + level.random.nextFloat() * fox.getBbWidth() * 2.0F - fox.getBbWidth(),
                                        fox.getY() + 0.5D + level.random.nextFloat() * fox.getBbHeight(),
                                        fox.getZ() + level.random.nextFloat() * fox.getBbWidth() * 2.0F - fox.getBbWidth(),
                                        d0, d1, d2);
                            }

                            level.playSound(null, fox.getX(), fox.getY(), fox.getZ(),
                                    SoundEvents.FOX_EAT, fox.getSoundSource(), 1.0F, 1.0F);
                        }
                        if (interactionCount + 1 == 5) {player.displayClientMessage(Component.translatable("message.goetydelight.cherryblossomcake.max_interactions").withStyle(ChatFormatting.GOLD), true);}
                        else if (interactionCount + 1 >= 5){player.displayClientMessage(Component.translatable("message.goetydelight.cherryblossomcake.already_max").withStyle(ChatFormatting.GOLD), true);}
                        event.setCancellationResult(InteractionResult.sidedSuccess(level.isClientSide()));
                        event.setCanceled(true);
                    }
                }
            }
        }

        @SubscribeEvent
        public static void onServerTick(TickEvent.ServerTickEvent event) {
            if (event.phase != TickEvent.Phase.START) return;

            long currentDayTime = event.getServer().overworld().getDayTime();
            long dayCycle = 24000L;
            long currentDay = currentDayTime / dayCycle;

            for (ServerLevel level : event.getServer().getAllLevels()) {
                for (net.minecraft.world.entity.Entity entity : level.getAllEntities()) {
                    if (entity instanceof Fox fox) {
                        CompoundTag persistentData = fox.getPersistentData();
                        ListTag recordedFoxes = persistentData.getList("recorded_foxes", Tag.TAG_COMPOUND);

                        if (!recordedFoxes.isEmpty()) {
                            boolean hasExpired = false;
                            for (int i = 0; i < recordedFoxes.size(); i++) {
                                CompoundTag foxData = recordedFoxes.getCompound(i);
                                long lastInteractionDay = foxData.getLong("last_interaction_day");

                                if (currentDay > lastInteractionDay) {
                                    hasExpired = true;
                                    break;
                                }
                            }
                            if (hasExpired) {
                                persistentData.putInt("cherry_blossom_interactions", 0);
                                persistentData.remove("recorded_foxes");
                            }
                        }
                    }

                    if (entity instanceof LivingEntity livingEntity) {
                        CompoundTag tag = livingEntity.getPersistentData();
                        if (tag.contains("CherryBlossomPunishmentTime")) {
                            long startTime = tag.getLong("CherryBlossomPunishmentTime");
                            int count = tag.getInt("CherryBlossomPunishmentCount");
                            long currentTime = level.getGameTime();

                            if (count < 3 && currentTime >= startTime + (count + 1) * 20) {
                                if (livingEntity instanceof Player player) {
                                    spawnLightningAndDamage(livingEntity, player, level);
                                } else {
                                    spawnLightningAndDamage(livingEntity, null, level);
                                }
                                tag.putInt("CherryBlossomPunishmentCount", count + 1);

                                if (count + 1 >= 3) {
                                    tag.remove("CherryBlossomPunishmentTime");
                                    tag.remove("CherryBlossomPunishmentCount");
                                }
                            }
                        }
                    }
                }
            }
        }

        private static int getInteractionCount(Fox fox) {
            CompoundTag persistentData = fox.getPersistentData();
            return persistentData.getInt("cherry_blossom_interactions");
        }

        private static void incrementInteractionCount(Fox fox, ServerPlayer player) {
            CompoundTag persistentData = fox.getPersistentData();
            int count = persistentData.getInt("cherry_blossom_interactions");
            persistentData.putInt("cherry_blossom_interactions", count + 1);

            UUID foxUUID = fox.getUUID();
            long currentDay = player.level().getDayTime() / 24000L;
            ListTag recordedFoxes = persistentData.getList("recorded_foxes", Tag.TAG_COMPOUND);
            boolean alreadyRecorded = false;
            for (int i = 0; i < recordedFoxes.size(); i++) {
                CompoundTag foxData = recordedFoxes.getCompound(i);
                if (foxData.getString("uuid").equals(foxUUID.toString())) {
                    alreadyRecorded = true;
                    foxData.putLong("last_interaction_day", currentDay);
                    break;
                }
            }

            if (!alreadyRecorded) {
                CompoundTag foxData = new CompoundTag();
                foxData.putString("uuid", foxUUID.toString());
                foxData.putLong("last_interaction_day", player.level().getDayTime() / 24000L);
                recordedFoxes.add(foxData);
                persistentData.put("recorded_foxes", recordedFoxes);
            }
        }
    }
}
