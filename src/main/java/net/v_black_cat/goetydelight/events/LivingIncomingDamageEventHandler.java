package net.v_black_cat.goetydelight.events;

import net.neoforged.neoforge.event.entity.living.LivingIncomingDamageEvent;
import net.v_black_cat.goetydelight.buff.effect.impl.*;

public class LivingIncomingDamageEventHandler {
    public static void onLivingIncomingDamage(LivingIncomingDamageEvent event) {
        CrimsonMemoriesBuffEffect.onLivingIncomingDamage(event);
        FreezeImmunityBuffEffect.onLivingIncomingDamage(event);
        NightStoveBuffEffect.onLivingIncomingDamage(event);
        RubyHardCandyDamageReductionBuffEffect.onLivingIncomingDamage(event);
        SugarScepterImmunityBuffEffect.onLivingIncomingDamage(event);
    }

}
