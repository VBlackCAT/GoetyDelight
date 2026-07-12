package net.v_black_cat.goetydelight;

import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.client.gui.ConfigurationScreen;
import net.neoforged.neoforge.client.gui.IConfigScreenFactory;
import net.v_black_cat.goetydelight.events.ClientSetupHandler;

@Mod(value = GoetyDelight.MODID, dist = Dist.CLIENT)
public class GoetyDelightClient {

    public GoetyDelightClient(IEventBus modEventBus, ModContainer container) {
        // 注册配置界面扩展点
        container.registerExtensionPoint(IConfigScreenFactory.class, ConfigurationScreen::new);

        // 手动注册客户端设置事件监听器
        modEventBus.addListener(ClientSetupHandler::onClientSetup);
    }
}