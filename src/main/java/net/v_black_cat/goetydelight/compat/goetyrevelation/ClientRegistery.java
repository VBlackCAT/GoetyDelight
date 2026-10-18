package net.v_black_cat.goetydelight.compat.goetyrevelation;

import net.minecraft.client.gui.screens.MenuScreens;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.v_black_cat.goetydelight.GoetyDelight;
import net.v_black_cat.goetydelight.compat.goetyrevelation.block.ApocalyptiumCookingPotScreen;

@Mod.EventBusSubscriber(
        modid = GoetyDelight.MODID,
        bus = Mod.EventBusSubscriber.Bus.MOD,
        value = Dist.CLIENT
)
public class ClientRegistery {

    @SubscribeEvent
    public static void onClientSetup(FMLClientSetupEvent event) {
        // 必须在 enqueueWork 中执行，避免类加载/注册时机问题
        event.enqueueWork(() -> {
            MenuScreens.register(
                    RevelationCompatRegistry.APOCALYPTIUM_POT_MENU.get(),
                    ApocalyptiumCookingPotScreen::new
            );
        });
    }
}