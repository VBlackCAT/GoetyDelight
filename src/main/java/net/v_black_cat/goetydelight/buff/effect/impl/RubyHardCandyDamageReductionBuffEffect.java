package net.v_black_cat.goetydelight.buff.effect.impl;

import net.minecraft.world.entity.LivingEntity;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.event.entity.living.LivingIncomingDamageEvent;
import net.v_black_cat.goetydelight.buff.effect.BuffEffect;
import net.v_black_cat.goetydelight.init.ModBuffTypes;
import net.v_black_cat.goetydelight.util.BuffUtil;

public class RubyHardCandyDamageReductionBuffEffect implements BuffEffect {
    @SubscribeEvent
    public static void onLivingIncomingDamage(LivingIncomingDamageEvent event) {
        LivingEntity entity = event.getEntity();
        if (BuffUtil.hasBuff(entity, ModBuffTypes.RUBY_HARD_CANDY_DAMAGE_REDUCTION.getId())) {
            event.setAmount(event.getAmount() * 0.5f);
        }
    }

    @Override
    public void apply(LivingEntity entity, int amplifier) {}
    @Override
    public void onApply(LivingEntity entity, int amplifier) {}
    @Override
    public void onRemove(LivingEntity entity, int amplifier) {}
}