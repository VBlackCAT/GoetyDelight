package net.v_black_cat.goetydelight.events;

import net.minecraft.client.Minecraft;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.v_black_cat.goetydelight.GoetyDelight;

public class ClientSetupHandler {
    public static void onClientSetup(FMLClientSetupEvent event) {
        GoetyDelight.LOGGER.info("HELLO FROM CLIENT SETUP");
        GoetyDelight.LOGGER.info("MINECRAFT NAME >> {}", Minecraft.getInstance().getUser().getName());
    }
}