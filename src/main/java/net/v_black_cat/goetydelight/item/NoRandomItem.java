package net.v_black_cat.goetydelight.item;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.living.LivingAttackEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.event.entity.living.LivingEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.registries.RegistryObject;
import net.v_black_cat.goetydelight.block.ModBlocks;

import java.util.HashSet;
import java.util.List;
import java.util.ArrayList;
import java.util.Set;

import static net.v_black_cat.goetydelight.GoetyDelight.MODID;

@Mod.EventBusSubscriber(modid = MODID)
public class NoRandomItem {

    // 指定的物品名单
    private static final Set<RegistryObject<?>> BANNED_ITEMS = new HashSet<>();

    static {
        BANNED_ITEMS.add(ModItems.EXAMPLE_ITEM);
        BANNED_ITEMS.add(ModItems.MARBLE_OP_SWORD);
        BANNED_ITEMS.add(ModItems.GOETYDELIGHT_ICON);
        BANNED_ITEMS.add(ModItems.SPIDER_EGG_BUBBLE_TEA_2);
        BANNED_ITEMS.add(ModItems.APOCALYPTIUM_KNIFE);
        BANNED_ITEMS.add(ModItems.SPECTRE_KNIFE);
        BANNED_ITEMS.add(ModItems.APOCALYPTIUM_INGOT_BRUSH);
        BANNED_ITEMS.add(ModItems.VENOMOUS_SPIDER_KNIFE);
        BANNED_ITEMS.add(ModItems.ASCENSION_MOONCAKE);
        BANNED_ITEMS.add(ModItems.PROMOTION_HARD_CANDY);
        BANNED_ITEMS.add(ModItems.NOT_ANYTHING);


        BANNED_ITEMS.add(ModBlocks.APOCALYPTIUM_POT);

        BANNED_ITEMS.add(ModBlocks.EXAMPLE_BLOCK);
        BANNED_ITEMS.add(ModBlocks.NETHER_MARBLE);
        BANNED_ITEMS.add(ModBlocks.POINTED_DRIPMARBLE);
        BANNED_ITEMS.add(ModBlocks.DRIPMARBLE_BLOCK);
        BANNED_ITEMS.add(ModBlocks.MARBLE_STAIRS);
        BANNED_ITEMS.add(ModBlocks.MARBLE_SLAB);
        BANNED_ITEMS.add(ModBlocks.MARBLE_BUTTON);
        BANNED_ITEMS.add(ModBlocks.MARBLE);
        BANNED_ITEMS.add(ModBlocks.MARBLE_PRESSURE_PLATE);
        BANNED_ITEMS.add(ModBlocks.MARBLE_FENCE);
        BANNED_ITEMS.add(ModBlocks.MARBLE_WALL);
        BANNED_ITEMS.add(ModBlocks.MARBLE_FENCE_GATE);
        BANNED_ITEMS.add(ModBlocks.MARBLE_DOOR);
        BANNED_ITEMS.add(ModBlocks.SILT_MARBLE_HEAVY);
        BANNED_ITEMS.add(ModBlocks.BLUE_MARBLE);
        BANNED_ITEMS.add(ModBlocks.JUNGLE_MARBLE);
        BANNED_ITEMS.add(ModBlocks.MARBLE_TRAPDOOR);
        BANNED_ITEMS.add(ModBlocks.RENDER_BLOCK);

    }

    public static void removeBannedItems(Player player) {
        if (player == null || BANNED_ITEMS.isEmpty() || player.isCreative()) return;

        // 优化主背包检查
        for (int i = 0; i < player.getInventory().items.size(); i++) {
            ItemStack stack = player.getInventory().items.get(i);
            if (isBannedItem(stack)) {
                player.getInventory().items.set(i, ItemStack.EMPTY);
            }
        }

        // 副手检查
        for (int i = 0; i < player.getInventory().offhand.size(); i++) {
            ItemStack offhandStack = player.getInventory().offhand.get(i);
            if (isBannedItem(offhandStack)) {
                player.getInventory().offhand.set(i, ItemStack.EMPTY);
            }
        }

        // 盔甲栏检查
        for (int i = 0; i < player.getInventory().armor.size(); i++) {
            ItemStack armorStack = player.getInventory().armor.get(i);
            if (isBannedItem(armorStack)) {
                player.getInventory().armor.set(i, ItemStack.EMPTY);
            }
        }
    }


    private static boolean isBannedItem(ItemStack stack) {
        if (stack.isEmpty()) return false;

        Item item = stack.getItem();
        for (RegistryObject<?> registryObject : BANNED_ITEMS) {
            if (registryObject.isPresent() && registryObject.get() == item) {
                return true;
            }
        }
        return false;
    }

    @SubscribeEvent
    public void onPlayerTick(TickEvent.PlayerTickEvent event) {
        Player player = event.player;
        if (player != null && !player.isCreative()) {
            removeBannedItems(player);
        }
    }

}
