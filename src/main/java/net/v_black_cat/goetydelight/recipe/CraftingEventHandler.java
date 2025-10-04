package net.v_black_cat.goetydelight.recipe;

import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.v_black_cat.goetydelight.item.ModItems;
import net.v_black_cat.goetydelight.item.food.EternalRefusalOfBlackMeatSoupItem;

@Mod.EventBusSubscriber(modid = "goetydelight")
public class CraftingEventHandler {

    @SubscribeEvent
    public static void onItemCrafted(PlayerEvent.ItemCraftedEvent event) {
        Player player = event.getEntity();
        ItemStack crafted = event.getCrafting();

        // 检查是否需要返还冷却的永恒黑肉汤
        if (crafted.hasTag() && crafted.getTag().contains("ReturnCooledSoup")) {
            // 移除标记
            crafted.getTag().remove("ReturnCooledSoup");

            // 给玩家一个冷却的永恒黑肉汤
            ItemStack cooledSoup = new ItemStack(ModItems.CUP.get());
            if (cooledSoup.getItem() instanceof EternalRefusalOfBlackMeatSoupItem soupItem) {
                soupItem.setCooldown(cooledSoup, player.level(), 60 * 20);

                // 尝试添加到玩家物品栏
                if (!player.getInventory().add(cooledSoup)) {
                    // 如果物品栏已满，掉落在地上
                    player.drop(cooledSoup, false);
                }
            }
        }
        


    }
}