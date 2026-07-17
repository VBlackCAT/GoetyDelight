package net.v_black_cat.goetydelight.buff.effect.impl;

import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.v_black_cat.goetydelight.buff.effect.BuffEffect;

public class PermanentFireResistanceBuffEffect implements BuffEffect {
    private static final int REFRESH_INTERVAL = 100;

    @Override
    public void apply(LivingEntity entity, int amplifier) {
        if (!entity.level().isClientSide && entity.tickCount % REFRESH_INTERVAL == 0) {
            entity.addEffect(new MobEffectInstance(MobEffects.FIRE_RESISTANCE, -1, 0, false, false));
        }
    }

    @Override
    public void onApply(LivingEntity entity, int amplifier) {
        entity.addEffect(new MobEffectInstance(MobEffects.FIRE_RESISTANCE, -1, 0, false, false));
    }

    @Override
    public void onRemove(LivingEntity entity, int amplifier) {
        // 不移除原版效果，让它自然消失
    }
}
