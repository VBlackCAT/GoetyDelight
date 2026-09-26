package net.v_black_cat.goetydelight.item.food;

import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.stats.Stats;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.animal.Fox;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.LivingDeathEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;
import net.v_black_cat.goetydelight.GoetyDelight;
import net.v_black_cat.goetydelight.init.ModAttachments;
import net.v_black_cat.goetydelight.init.ModBuffTypes;
import net.v_black_cat.goetydelight.init.ModItems;
import net.v_black_cat.goetydelight.util.BuffUtil;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

public class CherryBlossomCakeItem extends Item {

    public CherryBlossomCakeItem(Properties pProperties) {
        super(pProperties);
    }

    @Override
    public ItemStack finishUsingItem(ItemStack stack, Level level, LivingEntity entity) {
        ItemStack resultStack = super.finishUsingItem(stack, level, entity);

        if (!level.isClientSide && entity instanceof Player player) {
            ServerPlayer serverPlayer = (ServerPlayer) player;
            int foxKillCount = serverPlayer.getStats().getValue(Stats.ENTITY_KILLED.get(EntityType.FOX));

            if (foxKillCount == 0 || isMorningWindowActive(serverPlayer, level)) {
                // 使用 Buff 系统添加攻击力加成，持续 1200 ticks（1 分钟）
                BuffUtil.applyBuff(player, ModBuffTypes.CHERRY_BLOSSOM_ATTACK_BOOST.getId(), 1200, 0);

                if (foxKillCount > 0) {
                    clearLastUsageDay(serverPlayer);
                }
            } else {
                // 施加惩罚 Buff，持续 80 ticks（4 秒）
                BuffUtil.applyBuff(player, ModBuffTypes.CHERRY_BLOSSOM_PUNISHMENT.getId(), 80, 0);
            }
        }
        return resultStack;
    }

    private static boolean isMorningWindowActive(ServerPlayer player, Level level) {
        long lastUsageDay = player.getData(ModAttachments.CHERRY_BLOSSOM_LAST_USAGE_DAY);
        if (lastUsageDay == 0) return false;

        LocalDateTime now = LocalDateTime.now();
        LocalTime currentTime = now.toLocalTime();

        LocalTime windowStart = LocalTime.of(5, 20);
        LocalTime windowEnd = LocalTime.of(5, 22);

        boolean isInWindow = !currentTime.isBefore(windowStart) && currentTime.isBefore(windowEnd);
        if (!isInWindow) return false;

        long currentDay = level.getDayTime() / 24000L;
        return currentDay > lastUsageDay;
    }

    private static void clearLastUsageDay(ServerPlayer player) {
        player.setData(ModAttachments.CHERRY_BLOSSOM_LAST_USAGE_DAY, 0L);
    }


    @Override
    @OnlyIn(Dist.CLIENT)
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltipComponents, TooltipFlag isAdvanced) {
        LocalPlayer player = Minecraft.getInstance().player;
        if (player == null) return;

        int foxKillCount = player.getStats().getValue(Stats.ENTITY_KILLED.get(EntityType.FOX));
        boolean windowActive = isClientMorningWindowActive();

        if (foxKillCount == 0 || windowActive) {
            tooltipComponents.add(Component.translatable("tooltip.goetydelight.cherry_blossom_cake_good").withStyle(ChatFormatting.GOLD));
        } else {
            tooltipComponents.add(Component.translatable("tooltip.goetydelight.cherry_blossom_cake_bad").withStyle(ChatFormatting.DARK_RED));
        }
    }

    @OnlyIn(Dist.CLIENT)
    private boolean isClientMorningWindowActive() {
        LocalDateTime now = LocalDateTime.now();
        LocalTime currentTime = now.toLocalTime();

        LocalTime windowStart = LocalTime.of(5, 20);
        LocalTime windowEnd = LocalTime.of(5, 22);

        return !currentTime.isBefore(windowStart) && currentTime.isBefore(windowEnd);
    }

    // ==================== 事件处理器 ====================
    @EventBusSubscriber(modid = GoetyDelight.MODID)
    public static class CherryBlossomCakeEventHandler {


        @SubscribeEvent
        public static void onLivingDeath(LivingDeathEvent event) {
            if (event.getEntity().getType() == EntityType.FOX && event.getSource().getEntity() instanceof Player player) {
                if (player.level() instanceof ServerLevel) {
                    ServerPlayer serverPlayer = (ServerPlayer) player;
                    int foxKillCount = serverPlayer.getStats().getValue(Stats.ENTITY_KILLED.get(EntityType.FOX));

                    // 记录最后使用日
                    serverPlayer.setData(ModAttachments.CHERRY_BLOSSOM_LAST_USAGE_DAY, serverPlayer.level().getDayTime() / 24000L);

                    if (foxKillCount <= 1) {
                        serverPlayer.displayClientMessage(
                                Component.translatable("message.goetydelight.cherryblossomcake.angry").withStyle(ChatFormatting.RED), true);
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

                    if (serverPlayer.getStats().getValue(Stats.ENTITY_KILLED.get(EntityType.FOX)) == 0) {
                        // 使用狐狸的持久数据存储交互次数（简化处理，不用附件）
                        net.minecraft.nbt.CompoundTag foxData = fox.getPersistentData();
                        int interactionCount = foxData.getInt("cherry_blossom_interactions");

                        if (interactionCount < 5) {
                            if (!player.getAbilities().instabuild) {
                                stack.shrink(1);
                            }

                            foxData.putInt("cherry_blossom_interactions", interactionCount + 1);

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
                        if (interactionCount + 1 == 5) {
                            player.displayClientMessage(
                                    Component.translatable("message.goetydelight.cherryblossomcake.max_interactions").withStyle(ChatFormatting.GOLD), true);
                        } else if (interactionCount + 1 >= 5) {
                            player.displayClientMessage(
                                    Component.translatable("message.goetydelight.cherryblossomcake.already_max").withStyle(ChatFormatting.GOLD), true);
                        }
                        event.setCancellationResult(InteractionResult.sidedSuccess(level.isClientSide()));
                        event.setCanceled(true);
                    }
                }
            }
        }
    }
}