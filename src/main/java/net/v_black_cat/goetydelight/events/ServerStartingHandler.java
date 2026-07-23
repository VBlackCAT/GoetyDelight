package net.v_black_cat.goetydelight.events;

import net.neoforged.neoforge.event.server.ServerStartingEvent;
import net.v_black_cat.goetydelight.GoetyDelight;

public class ServerStartingHandler {


    public static void onServerStarting(ServerStartingEvent event) {
        GoetyDelight.LOGGER.info("HELLO from server starting");
    }
}