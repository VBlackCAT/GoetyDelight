package net.v_black_cat.goetydelight.buff.effect.impl;

import net.minecraft.world.entity.LivingEntity;
import net.neoforged.neoforge.event.entity.living.LivingIncomingDamageEvent;
import net.v_black_cat.goetydelight.buff.effect.BuffEffect;
import net.v_black_cat.goetydelight.init.ModBuffTypes;
import net.v_black_cat.goetydelight.util.BuffUtil;

public class SugarScepterImmunityBuffEffect implements BuffEffect {
    public static void onLivingIncomingDamage(LivingIncomingDamageEvent event) {
        LivingEntity entity = event.getEntity();
        if (BuffUtil.hasBuff(entity, ModBuffTypes.SUGAR_SCEPTER_IMMUNITY.getId())) {
                event.setCanceled(true);
                BuffUtil.removeBuff(entity,ModBuffTypes.SUGAR_SCEPTER_IMMUNITY.getId());
        }
    }

    @Override
    public void apply(LivingEntity entity, int amplifier) {}
    @Override
    public void onApply(LivingEntity entity, int amplifier) {}
    @Override
    public void onRemove(LivingEntity entity, int amplifier) {}
}