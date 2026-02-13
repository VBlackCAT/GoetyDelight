package net.v_black_cat.goetydelight.item;

import com.Polarice3.Goety.client.particles.ModParticleTypes;
import com.Polarice3.Goety.common.items.ModTiers;
import com.Polarice3.Goety.common.network.ModNetwork;
import com.Polarice3.Goety.common.network.server.SPlayPlayerSoundPacket;
import com.Polarice3.Goety.init.ModSounds;
import com.Polarice3.Goety.utils.MathHelper;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.SwordItem;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.client.event.RenderArmEvent;
import net.minecraftforge.client.event.RenderPlayerEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.living.LivingAttackEvent;
import net.minecraftforge.event.entity.living.LivingDamageEvent;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.items.wrapper.PlayerArmorInvWrapper;
import net.v_black_cat.goetydelight.event.PlayerLookHandler;
import vectorwing.farmersdelight.common.item.KnifeItem;

import java.util.Objects;
import java.util.UUID;


@Mod.EventBusSubscriber
public class FalseProverbsItem extends SwordItem {
    private static Vec3 originalPosition = null;
    private static final UUID Is_Shift_Key_UUID = UUID.fromString("4f5f5f5f-5f5f-5f5f-5f5f-5f5f5f5f5f5f");
    public static final String SHIFT_KEY_TAG = "IsShift";
    public static boolean WhetherInBack = false;

    public FalseProverbsItem(ModTiers tier, float attackDamage, float attackSpeed, Properties properties) {
        super(tier, (int) attackDamage, attackSpeed, properties);
    }

    // 监听每 tick 事件，检测 Shift 按键
    @SubscribeEvent
    public static void onPlayerTick(TickEvent.PlayerTickEvent event) {
        Player player = event.player;
        CompoundTag persistentData;
        if (!player.level().isClientSide) {
            if (player.getMainHandItem().getItem() instanceof FalseProverbsItem item) {
                persistentData = player.getPersistentData();
                if (player.isShiftKeyDown()) {
                    if (!persistentData.getBoolean(SHIFT_KEY_TAG)) {
                        item.addBonusAttributes(player);
                        persistentData.putBoolean(SHIFT_KEY_TAG, true);
                        originalPosition = player.position();
                        player.setInvisible(true); // 隐身
                        if (player.level() instanceof ServerLevel) {
                            for (int i = 0; i < 16; ++i) {
                                double d0 = MathHelper.rgbToSpeed((double) 96.0F);
                                double d1 = MathHelper.rgbToSpeed((double) 62.0F);
                                double d2 = MathHelper.rgbToSpeed((double) 92.0F);
                                ((ServerLevel) player.level()).sendParticles((SimpleParticleType) ModParticleTypes.CULT_SPELL.get(), player.getRandomX((double) 1.0F), player.getRandomY(), player.getRandomZ((double) 1.0F), 0, d0, d1, d2, (double) 0.5F);
                            }
                            ModNetwork.sendTo(player, new SPlayPlayerSoundPacket((SoundEvent) ModSounds.END_WALK.get(), 1.0F, 1.0F));
                        }
                    }
                } else {
                    // 松开 Shift 时取消状态
                    if (persistentData.getBoolean(SHIFT_KEY_TAG)) {
                        player.getPersistentData().remove(SHIFT_KEY_TAG);
                        item.removeBonusAttributes(player);
                        originalPosition = null;
                        player.setInvisible(false); // 取消隐身
                    }
                }
            }
            else {
                if (player.getPersistentData().getBoolean(SHIFT_KEY_TAG)) {
                    player.getPersistentData().remove(SHIFT_KEY_TAG);
                    Objects.requireNonNull(player.getAttribute(Attributes.MOVEMENT_SPEED)).removeModifier(Is_Shift_Key_UUID);
                    originalPosition = null;
                    player.setInvisible(false); // 取消隐身
                }
            }
        }
    }
    private void addBonusAttributes(Player player) {
        AttributeInstance speedAttribute = player.getAttribute(Attributes.MOVEMENT_SPEED);
        if (speedAttribute != null) {
            // 检查修饰符是否已存在
            if (speedAttribute.getModifier(Is_Shift_Key_UUID) == null) {
                AttributeModifier modifier = new AttributeModifier(
                        Is_Shift_Key_UUID,
                        "Shift speed",
                        2.0,
                        AttributeModifier.Operation.MULTIPLY_TOTAL
                );
                speedAttribute.addTransientModifier(modifier);
            }
        }
    }

    private void removeBonusAttributes(Player player) {
        AttributeInstance speedAttribute = player.getAttribute(Attributes.MOVEMENT_SPEED);
        if (speedAttribute != null && speedAttribute.getModifier(Is_Shift_Key_UUID) != null) {
            speedAttribute.removeModifier(Is_Shift_Key_UUID);
        }
        player.getPersistentData().remove(SHIFT_KEY_TAG);
    }

    @SubscribeEvent
    public static void onLivingHurt(LivingHurtEvent event) {
        if (event.getSource().getEntity() instanceof Player player) {
            if (player.getMainHandItem().getItem() instanceof FalseProverbsItem) {
                    if (event.getAmount() > 0.0F  && !WhetherInBack) {
                        event.setAmount(event.getAmount() * 2.0f);
                        if(originalPosition != null){
                          player.teleportTo(originalPosition.x, originalPosition.y, originalPosition.z);
                          originalPosition = null;
                        }
                    }
            }
        }
    }

    @SubscribeEvent
    public static void onLivingDamage(LivingDamageEvent event){
        if (event.getSource().getEntity() instanceof Player player) {
            if (player.getMainHandItem().getItem() instanceof FalseProverbsItem && player.getPersistentData().getBoolean(SHIFT_KEY_TAG)){
                if (event.getAmount() > 0.0F) {
                    event.setAmount(event.getAmount() * 3.0F);
                    if(originalPosition != null){
                        player.teleportTo(originalPosition.x, originalPosition.y, originalPosition.z);
                        originalPosition = null;
                    }
                }
            }
        }
    }

    @SubscribeEvent
    public static void onPlayerRenderPre(RenderPlayerEvent.Pre event) {
        if(event.getEntity().level() instanceof ClientLevel){
        Player player = event.getEntity();
        if (player.getPersistentData().getBoolean(SHIFT_KEY_TAG)) {
            event.setCanceled(true);
        }
     }
    }

    @SubscribeEvent
    public static void renderArm(RenderArmEvent event) {
        AbstractClientPlayer player = event.getPlayer();
        if (player.getPersistentData().getBoolean(SHIFT_KEY_TAG)) {
            if (player.getMainHandItem().isEmpty() && event.getArm() == player.getMainArm()) {
                event.setCanceled(true);
            } else if (player.getOffhandItem().isEmpty() && event.getArm() != player.getMainArm()) {
                event.setCanceled(true);
            }
        }
    }

    @SubscribeEvent
    public static void onAttackEvent(LivingAttackEvent event) {
        if (event.getSource().getEntity() instanceof Player player) {
            if (player.getMainHandItem().getItem() instanceof FalseProverbsItem) {
                // 获取攻击目标实体
                Entity target = event.getEntity();
                if (target != null) {
                    // 计算玩家视线方向向量
                    Vec3 playerLookVec = player.getViewVector(1.0F);
                    // 计算从玩家指向目标的向量
                    Vec3 playerToTarget = target.position().subtract(player.position()).normalize();
                    // 计算点积
                    double dotProduct = playerLookVec.dot(playerToTarget);
                    // 判断是否在背后（点积小于 -0.5）
                    if (dotProduct < -0.5) {
                        WhetherInBack = true;
                    }
                }
            }
        }
    }



    // 监听攻击事件
//    @SubscribeEvent
//    public static void onAttack(AttackEntityEvent event) {
//        Player player = event.getEntity();
//        if (player.getMainHandItem().getItem() instanceof FalseProverbsItem) {
//            FalseProverbsItem item = (FalseProverbsItem) player.getMainHandItem().getItem();
//
//            if (item.isInvisibleAndFast) {
//                // 判断是否为背部攻击（简化判断：目标背对玩家）
//                Vec3 lookVec = player.getViewVector(1.0F);
//                Vec3 targetPos = event.getTarget().position();
//                Vec3 playerToTarget = targetPos.subtract(player.position()).normalize();
//
//                double dotProduct = lookVec.dot(playerToTarget);
//                if (dotProduct < -0.5) { // 背部攻击条件
//                    // 应用三倍伤害
//                    event.getTarget().hurt(player.damageSources().playerAttack(player), player.getAttackStrengthScale(0.5F) * 3.0F);
//
//                    // 传送回原始位置
//                    if (item.originalPosition != null) {
//                        player.teleportTo(item.originalPosition.x, item.originalPosition.y, item.originalPosition.z);
//                    }
//                }
//            }
//        }
//    }
}
