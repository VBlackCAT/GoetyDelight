package net.v_black_cat.goetydelight.compat.goety_revelation;

import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.v_black_cat.goetydelight.GoetyDelight;
import net.v_black_cat.goetydelight.compat.goety_revelation.item.*;

@Mod.EventBusSubscriber(modid = GoetyDelight.MODID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class RevelationCompat {
    public static final String ID = "goety_revelation";
    public static final boolean IS_REVELATION_LOADED;
    static {
        IS_REVELATION_LOADED = ModList.get().isLoaded(ID);
    }
    @SubscribeEvent
    public static void onCommonSetup(FMLCommonSetupEvent event) {
        if (RevelationCompat.IS_REVELATION_LOADED) {
            MinecraftForge.EVENT_BUS.register(ApocalyptiumKnifeItem.class);
            MinecraftForge.EVENT_BUS.register(AscensionMooncakeItem.class);
            MinecraftForge.EVENT_BUS.register(PiPieItem.class);
            MinecraftForge.EVENT_BUS.register(QuietusMarrowItem.class);
            MinecraftForge.EVENT_BUS.register(StoneSwordSkewerItem.class);
        }
    }
}
