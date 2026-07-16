package net.v_black_cat.goetydelight.init;

import net.minecraft.client.Minecraft;
import net.minecraft.world.inventory.CraftingMenu;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.tags.ItemTags;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.ScreenEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.ForgeRegistries;
import net.v_black_cat.goetydelight.item.ModItems;

import java.util.List;
import java.util.stream.Collectors;

@Mod.EventBusSubscriber(modid = "goetydelight", value = Dist.CLIENT)
public class CraftingDisplayHandler {
    private static int tickCounter = 0;
    private static final int UPDATE_INTERVAL = 100; // 5秒 = 100 ticks
    private static ItemStack lastDisplayResult = ItemStack.EMPTY;

    @SubscribeEvent
    public static void onClientTick(TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.END) return;

        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null || mc.level == null) return;

        if (mc.player.containerMenu instanceof CraftingMenu craftingMenu) {
            tickCounter++;
            if (tickCounter >= UPDATE_INTERVAL) {
                tickCounter = 0;
                updateCraftingDisplay(craftingMenu);
            }
        }
    }

    private static void updateCraftingDisplay(CraftingMenu menu) {
        // 检查是否是船盘配方
        if (isBoatPlateInCraftingGrid(menu)) {
            List<Item> logs = getLogItems();
            if (!logs.isEmpty()) {
                int index = (int) ((System.currentTimeMillis() / 5000) % logs.size());
                ItemStack newResult = new ItemStack(logs.get(Math.abs(index)), 5);

                // 更新结果槽
                menu.getSlot(0).set(newResult);
            }
        }
    }

    private static boolean isBoatPlateInCraftingGrid(CraftingMenu menu) {
        int nonEmptyCount = 0;
        boolean hasBoatPlate = false;

        for (int i = 1; i <= 9; i++) { // 合成格是槽位1-9
            ItemStack stack = menu.getSlot(i).getItem();
            if (!stack.isEmpty()) {
                nonEmptyCount++;
                if (stack.getItem() == ModItems.BOAT_PLATE.get()) {
                    hasBoatPlate = true;
                }
            }
        }

        return nonEmptyCount == 1 && hasBoatPlate;
    }

    private static List<Item> getLogItems() {
        return ForgeRegistries.ITEMS.getValues().stream()
                .filter(item -> item.getDefaultInstance().is(ItemTags.LOGS))
                .collect(Collectors.toList());
    }
}