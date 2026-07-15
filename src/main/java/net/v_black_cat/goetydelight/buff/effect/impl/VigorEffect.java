package net.v_black_cat.goetydelight.buff.effect.impl;

import net.minecraft.world.entity.LivingEntity;
import net.v_black_cat.goetydelight.buff.effect.BuffEffect;

public class VigorEffect implements BuffEffect {
    @Override
    public void apply(LivingEntity entity, int amplifier) {
        // 每 tick 回复 0.5 + 等级*0.25 生命
        float heal = 0.5f + amplifier * 0.25f;
        entity.heal(heal);
    }
}