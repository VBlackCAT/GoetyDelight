package net.v_black_cat.goetydelight.buff.effect.impl;

import net.minecraft.world.entity.LivingEntity;
import net.v_black_cat.goetydelight.buff.effect.BuffEffect;

public class BurningEffect implements BuffEffect {
    @Override
    public void apply(LivingEntity entity, int amplifier) {
        // 每 tick 造成 1 + 等级*0.5 火焰伤害
        float damage = 1.0f + amplifier * 0.5f;
        entity.hurt(entity.damageSources().onFire(), damage);
    }
}