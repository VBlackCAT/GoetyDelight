package net.v_black_cat.goetydelight.events;

import com.Polarice3.Goety.client.particles.ModParticleTypes;
import com.Polarice3.Goety.common.network.ModNetwork;
import com.Polarice3.Goety.common.network.server.SPlayPlayerSoundPacket;
import com.Polarice3.Goety.init.ModSounds;
import com.Polarice3.Goety.utils.MathHelper;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RenderArmEvent;
import net.neoforged.neoforge.client.event.RenderLivingEvent;
import net.neoforged.neoforge.event.entity.living.LivingDamageEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;
import net.v_black_cat.goetydelight.GoetyDelight;
import net.v_black_cat.goetydelight.init.ModConfig;
import net.v_black_cat.goetydelight.item.FalseProverbsItem;
import net.v_black_cat.goetydelight.network.SyncBackModelPacket;
import vectorwing.farmersdelight.common.item.enchantment.BackstabbingEnchantment;

import static net.v_black_cat.goetydelight.item.FalseProverbsItem.SHIFT_KEY_TAG;

@EventBusSubscriber(modid = GoetyDelight.MODID)
public class FalseProverbsEvents {

    private static final ResourceLocation SHIFT_SPEED_MODIFIER_ID = ResourceLocation.withDefaultNamespace("shift_speed");

    @SubscribeEvent
    public static void onPlayerTick(PlayerTickEvent.Post event) {
        Player player = event.getEntity();
        if (!player.level().isClientSide) {
            if (player.getMainHandItem().getItem() instanceof FalseProverbsItem) {
                CompoundTag persistentData = player.getPersistentData();
                if (player.isShiftKeyDown()) {
                    if (!persistentData.getBoolean(SHIFT_KEY_TAG)) {
                        addBonusAttributes(player);
                        persistentData.putBoolean(SHIFT_KEY_TAG, true);
                        FalseProverbsItem.originalPosition = player.position();
                        FalseProverbsItem.setPlayerTeleportStatus(player.getUUID(), true);
                        FalseProverbsItem.worldLevel = player.level();
                        player.setInvisible(true);
                        if (player.level() instanceof ServerLevel serverLevel) {
                            for (int i = 0; i < 16; ++i) {
                                double d0 = MathHelper.rgbToSpeed(96.0F);
                                double d1 = MathHelper.rgbToSpeed(62.0F);
                                double d2 = MathHelper.rgbToSpeed(92.0F);
                                serverLevel.sendParticles((SimpleParticleType) ModParticleTypes.CULT_SPELL.get(),
                                        player.getRandomX(1.0F), player.getRandomY(), player.getRandomZ(1.0F),
                                        0, d0, d1, d2, 0.5F);
                            }
                            ModNetwork.sendTo(player, new SPlayPlayerSoundPacket(ModSounds.END_WALK.get(), 0.5F, 1.0F));
                        }
                    }
                    if (FalseProverbsItem.worldLevel != null && player.level() != FalseProverbsItem.worldLevel) {
                        FalseProverbsItem.originalPosition = null;
                    }
                } else {
                    if (persistentData.getBoolean(SHIFT_KEY_TAG)) {
                        player.getPersistentData().remove(SHIFT_KEY_TAG);
                        removeBonusAttributes(player);
                        FalseProverbsItem.originalPosition = null;
                        FalseProverbsItem.setPlayerTeleportStatus(player.getUUID(), false);
                        player.setInvisible(false);
                    }
                }
            } else {
                if (player.getPersistentData().getBoolean(SHIFT_KEY_TAG)) {
                    player.getPersistentData().remove(SHIFT_KEY_TAG);
                    removeBonusAttributes(player);
                    FalseProverbsItem.originalPosition = null;
                    FalseProverbsItem.setPlayerTeleportStatus(player.getUUID(), false);
                    player.setInvisible(false);
                }
            }

            // 背部模型同步（仅在状态变化时发送）
            boolean newStatus = FalseProverbsItem.shouldShowBackModel(player);
            boolean oldStatus = FalseProverbsItem.getPlayerBackModelStatus(player.getUUID());

            if (newStatus != oldStatus) {
                FalseProverbsItem.setPlayerBackModelStatus(player.getUUID(), newStatus);
                SyncBackModelPacket packet = new SyncBackModelPacket(player.getId(), newStatus);
                if (player.level() instanceof ServerLevel serverLevel) {
                    for (ServerPlayer serverPlayer : serverLevel.players()) {
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
        player.getPersistentData().remove(SHIFT_KEY_TAG);
    }

    @SubscribeEvent
    public static void onLivingHurt(LivingDamageEvent.Pre event) {
        if (event.getSource().getEntity() instanceof Player player) {
            ItemStack mainHand = player.getMainHandItem();
            if (mainHand.getItem() instanceof FalseProverbsItem) {
                float amount = event.getOriginalDamage();

                if (!player.isShiftKeyDown() && !player.isUsingItem() && amount > 0.0F) {
                    event.setNewDamage(amount * ModConfig.getFalseProverbsNormalDamageMultiplier());
                }
                if (player.isShiftKeyDown() && !player.isUsingItem() &&
                        !FalseProverbsItem.getPlayerTeleportStatus(player.getUUID())) {
                    event.setNewDamage(amount * ModConfig.getFalseProverbsShiftDamageMultiplier());
                }
                if (player.isShiftKeyDown() && !player.isUsingItem() &&
                        FalseProverbsItem.getPlayerTeleportStatus(player.getUUID()) &&
                        !BackstabbingEnchantment.isLookingBehindTarget(event.getEntity(), player.getEyePosition())) {
                    event.setNewDamage(amount * ModConfig.getFalseProverbsShiftDamageMultiplier());
                }
            }
        }
    }

    @SubscribeEvent
    public static void onLivingDamage(LivingDamageEvent.Pre event) {
        if (event.getSource().getEntity() instanceof Player player) {
            ItemStack mainHand = player.getMainHandItem();
            if (mainHand.getItem() instanceof FalseProverbsItem) {
                if (FalseProverbsItem.getPlayerTeleportStatus(player.getUUID()) &&
                        player.isShiftKeyDown() && !player.isUsingItem()) {
                    if (event.getOriginalDamage() > 0.0F) {
                        if (BackstabbingEnchantment.isLookingBehindTarget(event.getEntity(), player.getEyePosition())) {

                            event.setNewDamage(event.getOriginalDamage() * ModConfig.getFalseProverbsBackstabDamageMultiplier());
                            FalseProverbsItem.setPlayerTeleportStatus(player.getUUID(), false);
                        }
                        if (FalseProverbsItem.originalPosition != null) {
                            player.teleportTo(FalseProverbsItem.originalPosition.x,
                                    FalseProverbsItem.originalPosition.y,
                                    FalseProverbsItem.originalPosition.z);
                            FalseProverbsItem.originalPosition = null;
                        }
                    }
                }
            }
        }
    }

    @EventBusSubscriber(modid = GoetyDelight.MODID, value = Dist.CLIENT)
    public static class ClientEvents {
        @SubscribeEvent
        public static void onPlayerRenderPre(RenderLivingEvent.Pre event) {
            if (event.getEntity() instanceof Player player && event.getEntity().level()
                            instanceof ClientLevel) {
                if (player.getMainHandItem().getItem() instanceof FalseProverbsItem) {
                    if (player.isShiftKeyDown()) {
                        event.setCanceled(true);
                    }
                }
            }
        }

        @SubscribeEvent
        public static void renderArm(RenderArmEvent event) {
            AbstractClientPlayer player = event.getPlayer();
            if (player.getMainHandItem().getItem() instanceof FalseProverbsItem) {
                if (player.isShiftKeyDown()) {
                    if (player.getMainHandItem().isEmpty() && event.getArm() == player.getMainArm()) {
                        event.setCanceled(true);
                    } else if (player.getOffhandItem().isEmpty() && event.getArm() != player.getMainArm()) {
                        event.setCanceled(true);
                    }
                }
            }
        }
    }
}