package net.v_black_cat.goetydelight.events;

import net.minecraft.client.Minecraft;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.neoforge.client.event.RegisterClientReloadListenersEvent;
import net.v_black_cat.goetydelight.GoetyDelight;
import net.v_black_cat.goetydelight.init.ModItemBlockRender;
import net.v_black_cat.goetydelight.init.doll.CustomDollReloadListener;
import net.v_black_cat.goetydelight.visual.client.ScreenSpaceDepthEffectPostProcessor;

public class ClientSetupHandler {
    public static void onClientSetup(FMLClientSetupEvent event) {
        GoetyDelight.LOGGER.info("HELLO FROM CLIENT SETUP");
        GoetyDelight.LOGGER.info("MINECRAFT NAME >> {}", Minecraft.getInstance().getUser().getName());

        event.enqueueWork(() -> {
            ModItemBlockRender.setRenderLayer();
        });
    }
    public static void onRegisterClientReloadListeners(RegisterClientReloadListenersEvent event) {
        event.registerReloadListener(new CustomDollReloadListener());
        event.registerReloadListener(ScreenSpaceDepthEffectPostProcessor.reloadListener());
    }
}