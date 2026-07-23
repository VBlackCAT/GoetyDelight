package net.v_black_cat.goetydelight.events;

import net.neoforged.neoforge.event.entity.living.MobEffectEvent;
import net.v_black_cat.goetydelight.effect.CrimsonMemoriesEffect;
import net.v_black_cat.goetydelight.effect.TaintedDrinkEffect;

public class MobEffectEventHandler {
    public static void onEffectAdded(MobEffectEvent.Added event) {
        CrimsonMemoriesEffect.onEffectApplied(event);
    }

    public static void onEffectApplicable(MobEffectEvent.Applicable event) {
        TaintedDrinkEffect.onEffectApplicable(event);
    }
}
