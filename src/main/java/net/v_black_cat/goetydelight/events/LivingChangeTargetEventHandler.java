package net.v_black_cat.goetydelight.events;

import net.neoforged.neoforge.event.entity.living.LivingChangeTargetEvent;
import net.v_black_cat.goetydelight.buff.effect.impl.NightStoveBuffEffect;

public class LivingChangeTargetEventHandler {
    public static void onLivingChangeTarget(LivingChangeTargetEvent event) {
        NightStoveBuffEffect.onLivingChangeTarget(event);
    }
}
