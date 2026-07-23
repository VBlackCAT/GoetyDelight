package net.v_black_cat.goetydelight.events;

import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.v_black_cat.goetydelight.init.ModPayloadHandlers;

public class RegisterPayloadHandlersEventHandler {

    public static void register(RegisterPayloadHandlersEvent event) {
        ModPayloadHandlers.register(event);
    }
}
