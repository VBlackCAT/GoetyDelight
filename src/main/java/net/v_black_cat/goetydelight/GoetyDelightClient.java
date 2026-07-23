package net.v_black_cat.goetydelight;

import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.client.event.RegisterClientReloadListenersEvent;
import net.neoforged.neoforge.client.event.RegisterShadersEvent;
import net.neoforged.neoforge.client.gui.ConfigurationScreen;
import net.neoforged.neoforge.client.gui.IConfigScreenFactory;
import net.neoforged.neoforge.common.NeoForge;
import net.v_black_cat.goetydelight.events.ClientSetupHandler;
import net.v_black_cat.goetydelight.events.RegisterClientExtensionsEventHandler;
import net.v_black_cat.goetydelight.render.test.ModShaderReg;
import net.v_black_cat.goetydelight.visual.client.ClientEntityVisualEffectPackets;
import net.v_black_cat.goetydelight.visual.client.EntityVisualEffectRenderDispatcher;
import net.v_black_cat.goetydelight.visual.client.ScreenSpaceDepthEffectPostProcessor;

import java.io.IOException;

@Mod(value = GoetyDelight.MODID, dist = Dist.CLIENT)
public class GoetyDelightClient {

    public GoetyDelightClient(IEventBus modEventBus, ModContainer container) {
        // 注册配置界面扩展点
        container.registerExtensionPoint(IConfigScreenFactory.class, ConfigurationScreen::new);

        // 手动注册客户端设置事件监听器
        modEventBus.addListener(ClientSetupHandler::onClientSetup);
        modEventBus.addListener(RegisterClientExtensionsEventHandler::onRegisterClientExtensions);
        modEventBus.addListener((RegisterShadersEvent event) -> {
            try {
                ModShaderReg.registerShaders(event);
            } catch (IOException exception) {
                throw new IllegalStateException("Failed to register Goety Delight shaders", exception);
            }
        });
        modEventBus.addListener((RegisterClientReloadListenersEvent event) ->
                event.registerReloadListener(ScreenSpaceDepthEffectPostProcessor.reloadListener()));
        NeoForge.EVENT_BUS.addListener(EventPriority.HIGHEST,
                EntityVisualEffectRenderDispatcher::onRenderLevelStage);
        NeoForge.EVENT_BUS.addListener(EventPriority.LOWEST,
                ScreenSpaceDepthEffectPostProcessor::onRenderLevelStage);
        NeoForge.EVENT_BUS.addListener(ClientEntityVisualEffectPackets::onClientTick);
    }
}
