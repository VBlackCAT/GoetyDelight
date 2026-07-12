package net.v_black_cat.goetydelight.events;

import net.neoforged.neoforge.event.entity.living.LivingEntityUseItemEvent;
import net.v_black_cat.goetydelight.buff.BuffEventHandler;

public class LivingEntityUseItemEventHandler {
    public static void onItemUseFinish(LivingEntityUseItemEvent.Finish event) {
        BuffEventHandler.onItemUseFinish(event);
    }

}
