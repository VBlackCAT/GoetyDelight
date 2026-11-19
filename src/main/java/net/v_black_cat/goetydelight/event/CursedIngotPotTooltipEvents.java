package net.v_black_cat.goetydelight.event;

import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.event.entity.player.ItemTooltipEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.v_black_cat.goetydelight.block.CursedIngotPotBlockEntity;
import net.v_black_cat.goetydelight.block.ModBlocks;

/**
 * 亡灵注入诅咒金属锅的物品提示。
 */
public final class CursedIngotPotTooltipEvents {

    private CursedIngotPotTooltipEvents() {
    }

    @SubscribeEvent
    public static void onItemTooltip(ItemTooltipEvent event) {
        ItemStack stack = event.getItemStack();
        if (!stack.is(ModBlocks.CURSED_INGOT_POT.get().asItem())) {
            return;
        }
        if (stack.getTag() != null
                && stack.getTag().getBoolean(CursedIngotPotBlockEntity.NECROTIC_INFUSED_TAG)) {
            event.getToolTip().add(Component.translatable(
                    "tooltip.goetydelight.cursed_ingot_pot.necrotic_infused"));
        }
    }
}
