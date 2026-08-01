package net.v_black_cat.goetydelight.events;

import net.minecraft.core.component.DataComponents;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.v_black_cat.goetydelight.GoetyDelight;
import net.v_black_cat.goetydelight.init.ModItems;
import net.v_black_cat.goetydelight.item.food.EternalRefusalOfBlackMeatSoupItem;

/**
 * 1.21.1 移植版：使用"永恒黑肉汤"参与药水升级合成后，
 * 返还一杯带 60 秒冷却的永恒黑肉汤（对应 1.20.1 的 recipe/CraftingEventHandler）。
 */
@EventBusSubscriber(modid = GoetyDelight.MODID)
public class CraftingEventHandler {

    @SubscribeEvent
    public static void onItemCrafted(PlayerEvent.ItemCraftedEvent event) {
        Player player = event.getEntity();
        ItemStack crafted = event.getCrafting();

        CustomData data = crafted.get(DataComponents.CUSTOM_DATA);
        if (data == null || !data.copyTag().contains("ReturnCooledSoup")) {
            return;
        }

        CustomData.update(DataComponents.CUSTOM_DATA, crafted, tag -> tag.remove("ReturnCooledSoup"));

        ItemStack cooledSoup = new ItemStack(ModItems.CUP.get());
        if (cooledSoup.getItem() instanceof EternalRefusalOfBlackMeatSoupItem soupItem) {
            soupItem.setCooldown(cooledSoup, player.level(), 60 * 20);

            if (!player.getInventory().add(cooledSoup)) {
                player.drop(cooledSoup, false);
            }
        }
    }
}
