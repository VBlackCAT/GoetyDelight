package net.v_black_cat.goetydelight.buff.effect.impl;

import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.entity.LivingEntity;
import net.minecraftforge.event.entity.living.LivingEvent;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.v_black_cat.goetydelight.buff.effect.BuffEffect;
import net.v_black_cat.goetydelight.init.ModBuffTypes;
import net.v_black_cat.goetydelight.util.BuffUtil;

@Mod.EventBusSubscriber
public class FreezeImmunityBuffEffect implements BuffEffect {

    @SubscribeEvent
    public static void onLivingHurt(LivingHurtEvent event) {
        LivingEntity entity = event.getEntity();
        if (BuffUtil.hasBuff(entity, ModBuffTypes.FREEZE_IMMUNITY.getId())) {
            if (event.getSource().is(DamageTypes.FREEZE) ||
                    event.getSource().getMsgId().contains("freeze") ||
                    event.getSource().getMsgId().contains("powder")) {
                event.setCanceled(true);
            }
        }
    }

    @SubscribeEvent
    public static void onLivingTick(LivingEvent.LivingTickEvent event) {
        LivingEntity entity = event.getEntity();
        if (BuffUtil.hasBuff(entity, ModBuffTypes.FREEZE_IMMUNITY.getId())) {
            if (entity.isInPowderSnow) {
                entity.setIsInPowderSnow(false);
            }
            if (entity.isFullyFrozen()) {
                entity.setTicksFrozen(0);
            }
        }
    }

    @Override
    public void apply(LivingEntity entity, int amplifier) {
        // 每 tick 清除冰冻状态（由事件处理器完成，此处留空）
    }

    @Override
    public void onApply(LivingEntity entity, int amplifier) {}

    @Override
    public void onRemove(LivingEntity entity, int amplifier) {}
}
