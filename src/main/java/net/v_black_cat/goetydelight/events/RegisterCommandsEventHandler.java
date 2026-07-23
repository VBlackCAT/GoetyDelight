package net.v_black_cat.goetydelight.events;

import net.neoforged.neoforge.event.RegisterCommandsEvent;
import net.v_black_cat.goetydelight.init.ModCommands;

public class RegisterCommandsEventHandler {
    public static void onRegisterCommands(RegisterCommandsEvent event) {
        ModCommands.register(event);
    }
}
