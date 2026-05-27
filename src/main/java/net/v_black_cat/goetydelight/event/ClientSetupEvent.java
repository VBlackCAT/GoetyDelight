package net.v_black_cat.goetydelight.event;

import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RegisterClientReloadListenersEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.v_black_cat.goetydelight.init.CustomDollReloadListener;
import net.v_black_cat.goetydelight.visual.client.ScreenSpaceDepthEffectPostProcessor;

@Mod.EventBusSubscriber(value = Dist.CLIENT, bus = Mod.EventBusSubscriber.Bus.MOD)
public class ClientSetupEvent {

    @SubscribeEvent
    public static void onRegisterClientReloadListeners(RegisterClientReloadListenersEvent event) {
        event.registerReloadListener(new CustomDollReloadListener());
        event.registerReloadListener(ScreenSpaceDepthEffectPostProcessor.reloadListener());
    }
}
