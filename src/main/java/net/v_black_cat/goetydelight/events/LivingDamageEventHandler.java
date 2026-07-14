package net.v_black_cat.goetydelight.events;

import net.neoforged.neoforge.event.entity.living.LivingDamageEvent;
import net.v_black_cat.goetydelight.effect.TaintedDrinkEffect;
import net.v_black_cat.goetydelight.effect.TaintedPigEffect;

public class LivingDamageEventHandler {
    public static void onLivingHurtPre(LivingDamageEvent.Pre event) {
        TaintedDrinkEffect.onLivingHurt(event);

    }
    public static void onLivingHurtPost(LivingDamageEvent.Post event) {
        TaintedPigEffect.onLivingHurt(event);
    }

}
