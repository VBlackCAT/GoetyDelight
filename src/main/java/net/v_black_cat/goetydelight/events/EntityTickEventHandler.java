package net.v_black_cat.goetydelight.events;

import net.neoforged.neoforge.event.tick.EntityTickEvent;
import net.v_black_cat.goetydelight.buff.BuffEventHandler;
import net.v_black_cat.goetydelight.buff.effect.impl.FreezeImmunityBuffEffect;

public class EntityTickEventHandler {
    public static void onEntityTick(EntityTickEvent.Post event) {
        BuffEventHandler.onEntityTick(event);
        FreezeImmunityBuffEffect.onEntityTickPost(event);
    }
}
