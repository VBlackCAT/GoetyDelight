package net.v_black_cat.goetydelight.buff.effect.impl;

import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.entity.LivingEntity;
import net.neoforged.neoforge.event.entity.living.LivingIncomingDamageEvent;
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

    /**
     * 每 tick 清除冰冻状态。
     *
     * <p>旧实现单独订阅 {@code EntityTickEvent.Post}，于是<b>每个活体实体每 tick</b> 都要跑一次
     * {@code BuffUtil.hasBuff}（哪怕身上一个 buff 都没有）。现在放进 {@link BuffEffect#apply}：
     * {@code BuffEventHandler} 只在实体确实带着 buff 时才会分发，且只有带本 buff 的实体能走到这里
     * —— 空载零成本，而且不需要在存档重载后重建任何索引（BuffEffect 是跟着 buff 数据走的）。
     */
    @Override
    public void apply(LivingEntity entity, int amplifier) {
        if (entity.level().isClientSide) return;
        if (entity.isInPowderSnow) {
            entity.setIsInPowderSnow(false);
        }
        if (entity.isFullyFrozen()) {
            entity.setTicksFrozen(0);
        }
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