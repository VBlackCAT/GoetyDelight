package net.v_black_cat.goetydelight.compat.goetyrevelation;

import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.v_black_cat.goetydelight.GoetyDelight;

@Mod.EventBusSubscriber(
        modid = GoetyDelight.MODID,
        bus = Mod.EventBusSubscriber.Bus.MOD,
        value = Dist.CLIENT
)
public class ClientRegistery {

    @SubscribeEvent
    public static void onClientSetup(FMLClientSetupEvent event) {
        // 只在这里判断，不触发任何目标类的加载
        if (!GoetyRevelationCompat.IS_LOADED) {
            GoetyDelight.LOGGER.info("[compat] goety_revelation 未加载，跳过客户端注册");
            return;
        }

        // 延迟到 enqueueWork，且通过独立的 Holder 类访问目标类
        event.enqueueWork(ClientRegistery::doClientSetup);
    }

    private static void doClientSetup() {
        // 这个方法只在 IS_LOADED 为 true 时被调用，
        // 此时 JVM 才会解析 ClientHolder 及其引用的 RevelationCompatRegistry / Screen
        ClientHolder.registerScreens();
    }

    /**
     * 类隔离层：只有真正需要时才会被类加载。
     * 把对 RevelationCompatRegistry 和 ApocalyptiumCookingPotScreen 的直接引用放在这里。
     */
    private static final class ClientHolder {

        static void registerScreens() {
            if (!RevelationCompatRegistry.APOCALYPTIUM_POT_MENU.isPresent()) {
                GoetyDelight.LOGGER.warn("[compat] apocalyptium_cooking_pot menu 未注册，跳过 Screen 注册");
                return;
            }
            net.minecraft.client.gui.screens.MenuScreens.register(
                    RevelationCompatRegistry.APOCALYPTIUM_POT_MENU.get(),
                    net.v_black_cat.goetydelight.compat.goetyrevelation.block.ApocalyptiumCookingPotScreen::new
            );
        }
    }
}