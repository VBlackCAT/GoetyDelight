package net.v_black_cat.goetydelight.compat.goety_revelation.item;

import com.Polarice3.Goety.common.effects.GoetyEffects;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.ThrownTrident;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.living.LivingAttackEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Map;

public class SharkGummyItem extends Item {

    private static final String NBT_KEY = "SharkGummyEffect";
    private static final int EFFECT_DURATION = 10 * 20;

    private static Field activeEffectsField;
    private static Method onEffectAddedMethod;
    private static Method onEffectUpdatedMethod;

    static {
        try {
            // 初始化反射字段和方法
            activeEffectsField = LivingEntity.class.getDeclaredField("f_20945_"); // activeEffects
            activeEffectsField.setAccessible(true);

            onEffectAddedMethod = LivingEntity.class.getDeclaredMethod("m_142540_",
                    MobEffectInstance.class, net.minecraft.world.entity.Entity.class); // onEffectAdded
            onEffectAddedMethod.setAccessible(true);

            onEffectUpdatedMethod = LivingEntity.class.getDeclaredMethod("m_141973_",
                    MobEffectInstance.class, boolean.class, net.minecraft.world.entity.Entity.class); // onEffectUpdated
            onEffectUpdatedMethod.setAccessible(true);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public SharkGummyItem(Properties properties) {
        super(properties);
        MinecraftForge.EVENT_BUS.register(this);
    }

    @Override
    public ItemStack finishUsingItem(ItemStack stack, Level level, LivingEntity entity) {
        ItemStack result = super.finishUsingItem(stack, level, entity);

        if (entity instanceof Player player && !level.isClientSide) {
            CompoundTag playerData = player.getPersistentData();
            playerData.putLong(NBT_KEY, level.getGameTime() + EFFECT_DURATION);
        }

        return result;
    }

    @SubscribeEvent
    public void onLivingAttack(LivingAttackEvent event) {
        if (event.getEntity() instanceof Player targetPlayer && targetPlayer.isCreative()) {
            return;
        }

        if (event.getSource().getDirectEntity() instanceof ThrownTrident trident) {
            if (trident.getOwner() instanceof Player player) {
                if (event.getEntity() == player) {
                    return;
                }

                CompoundTag playerData = player.getPersistentData();
                if (playerData.contains(NBT_KEY)) {
                    long effectEndTime = playerData.getLong(NBT_KEY);
                    if (effectEndTime > player.level().getGameTime()) {
                        LivingEntity target = event.getEntity();

                        forceAddEffect(target, new MobEffectInstance(
                                MobEffects.DARKNESS,
                                EFFECT_DURATION,
                                0,
                                false,
                                true
                        ));

                        forceAddEffect(target, new MobEffectInstance(
                                GoetyEffects.STUNNED.get(),
                                EFFECT_DURATION,
                                0,
                                false,
                                true
                        ));

                        forceAddEffect(target, new MobEffectInstance(
                                GoetyEffects.CURSED.get(),
                                EFFECT_DURATION,
                                0,
                                false,
                                true
                        ));

                    } else {
                        playerData.remove(NBT_KEY);
                    }
                }
            }
        }
    }

    /**
     * 使用反射绕过 canBeAffected 检查，强制添加药水效果
     */
    private void forceAddEffect(LivingEntity target, MobEffectInstance effectInstance) {
        try {
            @SuppressWarnings("unchecked")
            Map<MobEffect, MobEffectInstance> activeEffects = (Map<MobEffect, MobEffectInstance>) activeEffectsField.get(target);
            MobEffect effect = effectInstance.getEffect();

            MobEffectInstance existingEffect = activeEffects.get(effect);
            if (existingEffect == null) {
                activeEffects.put(effect, effectInstance);
                onEffectAddedMethod.invoke(target, effectInstance, null);
            } else if (existingEffect.update(effectInstance)) {
                onEffectUpdatedMethod.invoke(target, existingEffect, true, null);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}