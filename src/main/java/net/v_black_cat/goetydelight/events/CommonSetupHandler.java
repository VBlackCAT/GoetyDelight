package net.v_black_cat.goetydelight.events;

import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.v_black_cat.goetydelight.GoetyDelight;
import net.v_black_cat.goetydelight.compat.curios.CuriosCompat;
import net.v_black_cat.goetydelight.ritual.DelightRitualType;

public class CommonSetupHandler {
    public static void onCommonSetup(FMLCommonSetupEvent event) {
        GoetyDelight.LOGGER.info("HELLO FROM COMMON SETUP");
        DelightRitualType.onCommonSetup(event);
        event.enqueueWork(CuriosCompat::commonSetup);
    }
}