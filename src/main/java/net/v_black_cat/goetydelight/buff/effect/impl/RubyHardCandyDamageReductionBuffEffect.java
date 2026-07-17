package net.v_black_cat.goetydelight.buff.effect.impl;

import net.minecraft.world.entity.LivingEntity;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.v_black_cat.goetydelight.buff.effect.BuffEffect;
import net.v_black_cat.goetydelight.init.ModBuffTypes;
import net.v_black_cat.goetydelight.util.BuffUtil;

@Mod.EventBusSubscriber
public class RubyHardCandyDamageReductionBuffEffect implements BuffEffect {

    @SubscribeEvent
    public static void onLivingHurt(LivingHurtEvent event) {
        LivingEntity entity = event.getEntity();
        if (entity.level().isClientSide) return;

        if (BuffUtil.hasBuff(entity, ModBuffTypes.RUBY_HARD_CANDY_DAMAGE_REDUCTION.getId())) {
            float reducedDamage = event.getAmount() * 0.75f;
            event.setAmount(reducedDamage);
        }
    }

    @Override
    public void apply(LivingEntity entity, int amplifier) {}

    @Override
    public void onApply(LivingEntity entity, int amplifier) {}

    @Override
    public void onRemove(LivingEntity entity, int amplifier) {}
}
