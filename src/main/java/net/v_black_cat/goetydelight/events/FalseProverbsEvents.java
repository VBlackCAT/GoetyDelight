package net.v_black_cat.goetydelight.events;

import com.Polarice3.Goety.client.particles.ModParticleTypes;
import com.Polarice3.Goety.common.network.ModNetwork;
import com.Polarice3.Goety.common.network.server.SPlayPlayerSoundPacket;
import com.Polarice3.Goety.init.ModSounds;
import com.Polarice3.Goety.utils.MathHelper;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RenderArmEvent;
import net.neoforged.neoforge.client.event.RenderLivingEvent;
import net.neoforged.neoforge.event.entity.living.LivingDamageEvent;
import net.neoforged.neoforge.event.entity.living.LivingIncomingDamageEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;
import net.neoforged.neoforge.event.tick.ServerTickEvent;
import net.v_black_cat.goetydelight.GoetyDelight;
import net.v_black_cat.goetydelight.init.ModAttachments;
import net.v_black_cat.goetydelight.init.ModConfig;
import net.v_black_cat.goetydelight.item.FalseProverbsItem;
import net.v_black_cat.goetydelight.network.SyncBackModelPacket;
import net.v_black_cat.goetydelight.util.FoodState;
import vectorwing.farmersdelight.common.item.enchantment.BackstabbingEnchantment;

import java.util.UUID;

@EventBusSubscriber(modid = GoetyDelight.MODID)
public class FalseProverbsEvents {

    private static final ResourceLocation SHIFT_SPEED_MODIFIER_ID = ResourceLocation.withDefaultNamespace("shift_speed");
    private static final int SYNC_DISTANCE_SQR = 4096; // 64格内同步
    private static final int CLEANUP_INTERVAL = 1200; // 60秒清理一次
    private static int cleanupCounter = 0;

    @SubscribeEvent
    public static void onPlayerTick(PlayerTickEvent.Post event) {
        Player player = event.getEntity();
        if (!player.level().isClientSide) {
            UUID playerUUID = player.getUUID();

            if (player.getMainHandItem().getItem() instanceof FalseProverbsItem) {
                FoodState state = player.getData(ModAttachments.FOOD_STATE);

                if (player.isShiftKeyDown()) {
                    if (!state.isFalseProverbsShift()) {
                        // 第一次按下Shift
                        addBonusAttributes(player);
                        state.setFalseProverbsShift(true);

                        FalseProverbsItem.setOriginalPosition(playerUUID, player.position());
                        FalseProverbsItem.setWorldLevel(playerUUID, player.level());
                        FalseProverbsItem.setPlayerTeleportStatus(playerUUID, true);

                        player.setInvisible(true);

                        // 优化粒子效果
                        spawnShiftParticles(player);
                        ModNetwork.sendTo(player, new SPlayPlayerSoundPacket(ModSounds.END_WALK.get(), 0.5F, 1.0F));
                    }

                    // 检查维度变化
                    Level storedLevel = FalseProverbsItem.getWorldLevel(playerUUID);
                    if (storedLevel != null && player.level() != storedLevel) {
                        FalseProverbsItem.setOriginalPosition(playerUUID, null);
                    }
                } else {
                    // Shift释放时的处理
                    if (state.isFalseProverbsShift()) {
                        resetShiftState(player);
                    }
                }
            } else {
                // 主手不是FalseProverbsItem，但仍有Shift状态
                if (player.getData(ModAttachments.FOOD_STATE).isFalseProverbsShift()) {
                    resetShiftState(player);
                }
            }

            // 优化的背部模型同步
            syncBackModelStatus(player, playerUUID);
        }
    }

    private static void spawnShiftParticles(Player player) {
        if (!(player.level() instanceof ServerLevel serverLevel)) return;
        if (player.tickCount % 20 != 0) return; // 每20tick生成一次粒子，优化性能

        SimpleParticleType particleType = (SimpleParticleType) ModParticleTypes.CULT_SPELL.get();
        for (int i = 0; i < 2; ++i) { // 减少粒子数量，1.20.1版本优化
            double d0 = MathHelper.rgbToSpeed(96.0F);
            double d1 = MathHelper.rgbToSpeed(62.0F);
            double d2 = MathHelper.rgbToSpeed(92.0F);
            serverLevel.sendParticles(particleType,
                    player.getRandomX(1.0F), player.getRandomY(), player.getRandomZ(1.0F),
                    0, d0, d1, d2, 0.5F);
        }
    }

    private static void resetShiftState(Player player) {
        UUID playerUUID = player.getUUID();
        player.getData(ModAttachments.FOOD_STATE).setFalseProverbsShift(false);
        removeBonusAttributes(player);
        FalseProverbsItem.setOriginalPosition(playerUUID, null);
        FalseProverbsItem.setPlayerTeleportStatus(playerUUID, false);
        player.setInvisible(false);
    }

    private static void syncBackModelStatus(Player player, UUID playerUUID) {
        boolean newStatus = FalseProverbsItem.shouldShowBackModel(player);
        Boolean lastStatus = FalseProverbsItem.getLastSentBackModelStatus().get(playerUUID);

        if (lastStatus == null || lastStatus != newStatus) {
            FalseProverbsItem.setPlayerBackModelStatus(playerUUID, newStatus);
            FalseProverbsItem.getLastSentBackModelStatus().put(playerUUID, newStatus);

            SyncBackModelPacket packet = new SyncBackModelPacket(player.getId(), newStatus);
            if (player.level() instanceof ServerLevel serverLevel) {
                for (ServerPlayer serverPlayer : serverLevel.players()) {
                    // 距离优化，减少不必要的网络传输
                    if (serverPlayer.distanceToSqr(player) < SYNC_DISTANCE_SQR) {
                        SyncBackModelPacket.sendToClient(packet, serverPlayer);
                    }
                }
            }
        }
    }

    private static void addBonusAttributes(Player player) {
        AttributeInstance speedAttribute = player.getAttribute(Attributes.MOVEMENT_SPEED);
        if (speedAttribute != null && speedAttribute.getModifier(SHIFT_SPEED_MODIFIER_ID) == null) {
            AttributeModifier modifier = new AttributeModifier(
                    SHIFT_SPEED_MODIFIER_ID,
                    ModConfig.getShiftSpeedMultiplier(),
                    AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL
            );
            speedAttribute.addTransientModifier(modifier);
        }
    }

    private static void removeBonusAttributes(Player player) {
        AttributeInstance speedAttribute = player.getAttribute(Attributes.MOVEMENT_SPEED);
        if (speedAttribute != null) {
            speedAttribute.removeModifier(SHIFT_SPEED_MODIFIER_ID);
        }
    }

    // 统一的伤害处理
    @SubscribeEvent
    public static void onLivingHurt(LivingIncomingDamageEvent event) {
        if (!(event.getSource().getEntity() instanceof Player player)) return;
        if (!(player.getMainHandItem().getItem() instanceof FalseProverbsItem)) return;
        if (player.isUsingItem()) return;

        float amount = event.getAmount();
        UUID playerUUID = player.getUUID();

        if (amount > 0.0F) {
            if (player.isShiftKeyDown()) {
                if (FalseProverbsItem.getPlayerTeleportStatus(playerUUID)) {
                    // 传送状态下的背刺检查
                    if (!BackstabbingEnchantment.isLookingBehindTarget(event.getEntity(), player.getEyePosition())) {
                        event.setAmount(amount * ModConfig.getFalseProverbsShiftDamageMultiplier());
                    }
                    // 如果是背刺，在onLivingDamage中处理
                } else {
                    // 非传送状态下的Shift伤害
                    event.setAmount(amount * ModConfig.getFalseProverbsShiftDamageMultiplier());
                }
            } else {
                // 普通攻击
                event.setAmount(amount * ModConfig.getFalseProverbsNormalDamageMultiplier());
            }
        }
    }

    @SubscribeEvent
    public static void onLivingDamage(LivingDamageEvent.Pre event) {
        if (!(event.getSource().getEntity() instanceof Player player)) return;
        if (!(player.getMainHandItem().getItem() instanceof FalseProverbsItem)) return;
        if (player.isUsingItem()) return;

        UUID playerUUID = player.getUUID();

        if (FalseProverbsItem.getPlayerTeleportStatus(playerUUID) && player.isShiftKeyDown()) {
            if (event.getOriginalDamage() > 0.0F) {
                // 背刺额外伤害
                if (BackstabbingEnchantment.isLookingBehindTarget(event.getEntity(), player.getEyePosition())) {
                    event.setNewDamage(event.getOriginalDamage() * ModConfig.getFalseProverbsBackstabDamageMultiplier());
                }

                // 传送回原位
                Vec3 originalPos = FalseProverbsItem.getOriginalPosition(playerUUID);
                if (originalPos != null) {
                    player.teleportTo(originalPos.x, originalPos.y, originalPos.z);
                }

                // 清除传送状态
                FalseProverbsItem.removePlayerTeleportStatus(playerUUID);
                player.setInvisible(false);
            }
        }
    }

    // 玩家登出清理
    @SubscribeEvent
    public static void onPlayerLogout(PlayerEvent.PlayerLoggedOutEvent event) {
        FalseProverbsItem.clearPlayerData(event.getEntity().getUUID());
    }

    // 定期清理过期数据
    @SubscribeEvent
    public static void onServerTick(ServerTickEvent.Post event) {
        cleanupCounter++;
        if (cleanupCounter >= CLEANUP_INTERVAL) {
            cleanupCounter = 0;

            long currentTick = event.getServer().getTickCount();
            FalseProverbsItem.cleanupExpiredData(currentTick, uuid ->
                    event.getServer().getPlayerList().getPlayer(uuid) != null
            );
        }
    }

    @EventBusSubscriber(modid = GoetyDelight.MODID, value = Dist.CLIENT)
    public static class ClientEvents {
        @SubscribeEvent
        public static void onPlayerRenderPre(RenderLivingEvent.Pre event) {
            if (!(event.getEntity() instanceof Player player)) return;
            if (!(event.getEntity().level() instanceof ClientLevel)) return;
            if (!(player.getMainHandItem().getItem() instanceof FalseProverbsItem)) return;

            if (player.isShiftKeyDown()) {
                event.setCanceled(true);
            }
        }

        @SubscribeEvent
        public static void renderArm(RenderArmEvent event) {
            AbstractClientPlayer player = event.getPlayer();
            if (!(player.getMainHandItem().getItem() instanceof FalseProverbsItem)) return;
            if (!player.isShiftKeyDown()) return;

            if (player.getMainHandItem().isEmpty() && event.getArm() == player.getMainArm()) {
                event.setCanceled(true);
            } else if (player.getOffhandItem().isEmpty() && event.getArm() != player.getMainArm()) {
                event.setCanceled(true);
            }
        }
    }
}