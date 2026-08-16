package net.v_black_cat.goetydelight.buff.effect.impl;

import net.minecraft.world.entity.LivingEntity;
import net.v_black_cat.goetydelight.buff.effect.BuffEffect;
import net.v_black_cat.goetydelight.init.ModAttachments;

public class CrimsonMemoriesEffect implements BuffEffect {
    @Override
    public void apply(LivingEntity entity, int amplifier) {}

    @Override
    public void onApply(LivingEntity entity, int amplifier) {
        entity.getData(ModAttachments.FOOD_STATE).setCrimsonMemories(true);
    }

    @Override
    public void onRemove(LivingEntity entity, int amplifier) {
        entity.getData(ModAttachments.FOOD_STATE).setCrimsonMemories(false);
    }
}
