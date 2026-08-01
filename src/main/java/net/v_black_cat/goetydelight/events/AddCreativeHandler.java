package net.v_black_cat.goetydelight.events;

import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.CreativeModeTabs;
import net.neoforged.neoforge.event.BuildCreativeModeTabContentsEvent;
import net.v_black_cat.goetydelight.item.ItemBlackList;
import net.v_black_cat.goetydelight.init.ModItems;

import java.util.ArrayList;
import java.util.List;

public class AddCreativeHandler {

    public static void onAddCreative(BuildCreativeModeTabContentsEvent event) {
        if (event.getTabKey() == CreativeModeTabs.BUILDING_BLOCKS) {
            event.accept(ModItems.EXAMPLE_ITEM);
        }

        // 从创造模式标签页移除黑名单物品（对应 1.20.1 ItemBlackList.CreativeTabHandler）
        List<ItemStack> parentEntries = new ArrayList<>(event.getParentEntries());
        for (ItemStack stack : parentEntries) {
            if (ItemBlackList.isBlackListed(stack.getItem())) {
                event.remove(stack, CreativeModeTab.TabVisibility.PARENT_AND_SEARCH_TABS);
            }
        }
        List<ItemStack> searchEntries = new ArrayList<>(event.getSearchEntries());
        for (ItemStack stack : searchEntries) {
            if (ItemBlackList.isBlackListed(stack.getItem())) {
                event.remove(stack, CreativeModeTab.TabVisibility.PARENT_AND_SEARCH_TABS);
            }
        }
    }
}
