package net.v_black_cat.goetydelight.buff.effect.impl;

import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.entity.LivingEntity;
import net.neoforged.neoforge.event.entity.living.LivingIncomingDamageEvent;
import net.neoforged.neoforge.event.tick.EntityTickEvent;
import net.v_black_cat.goetydelight.buff.effect.BuffEffect;
import net.v_black_cat.goetydelight.init.ModBuffTypes;
import net.v_black_cat.goetydelight.util.BuffUtil;

public class FreezeImmunityBuffEffect implements BuffEffect {
    public static void onLivingIncomingDamage(LivingIncomingDamageEvent event) {
        LivingEntity entity = event.getEntity();
        if (BuffUtil.hasBuff(entity, ModBuffTypes.FREEZE_IMMUNITY.getId())) {
            if (event.getSource().is(DamageTypes.FREEZE) ||
                    event.getSource().getMsgId().contains("freeze") ||
                    event.getSource().getMsgId().contains("powder")) {
                event.setCanceled(true);
            }
        }
    }

    public static void onEntityTickPost(EntityTickEvent.Post event) {
        if (event.getEntity() instanceof LivingEntity entity) {
            if (BuffUtil.hasBuff(entity, ModBuffTypes.FREEZE_IMMUNITY.getId())) {
                if (entity.isInPowderSnow) {
                    entity.setIsInPowderSnow(false);
                }
                if (entity.isFullyFrozen()) {
                    entity.setTicksFrozen(0);
                }
            }
        }
    }

    @Override
    public void apply(LivingEntity entity, int amplifier) {
        // 每 tick 清除冰冻状态（由事件处理器完成，此处留空）
    }

    @Override
    public void onApply(LivingEntity entity, int amplifier) {
        // 无需额外操作
    }

    @Override
    public void onRemove(LivingEntity entity, int amplifier) {
        // 无需额外操作
    }
}