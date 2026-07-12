package net.v_black_cat.goetydelight.events;

import net.minecraft.world.item.CreativeModeTabs;
import net.neoforged.neoforge.event.BuildCreativeModeTabContentsEvent;
import net.v_black_cat.goetydelight.init.ModItems;

public class AddCreativeHandler {
    public static void onAddCreative(BuildCreativeModeTabContentsEvent event) {
        if (event.getTabKey() == CreativeModeTabs.BUILDING_BLOCKS) {
            event.accept(ModItems.EXAMPLE_ITEM);
        }
    }
}