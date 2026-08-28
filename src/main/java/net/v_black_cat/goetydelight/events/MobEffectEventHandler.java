package net.v_black_cat.goetydelight.events;

import net.neoforged.neoforge.event.entity.living.MobEffectEvent;
import net.v_black_cat.goetydelight.effect.CrimsonMemoriesEffect;
import net.v_black_cat.goetydelight.effect.TaintedDrinkEffect;

public class MobEffectEventHandler {
    public static void onEffectAdded(MobEffectEvent.Added event) {
        CrimsonMemoriesEffect.onEffectApplied(event);
    }

    public static void onEffectApplicable(MobEffectEvent.Applicable event) {
        // 【优化】仅服务端裁决：客户端重复执行既浪费，也可能与服务端结果不一致（影响显示）
        if (event.getEntity().level().isClientSide) return;
        TaintedDrinkEffect.onEffectApplicable(event);
    }
}
