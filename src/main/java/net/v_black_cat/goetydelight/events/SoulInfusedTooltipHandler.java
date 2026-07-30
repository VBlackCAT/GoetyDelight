package net.v_black_cat.goetydelight.events;

import net.minecraft.ChatFormatting;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.ItemTooltipEvent;
import net.v_black_cat.goetydelight.GoetyDelight;
import net.v_black_cat.goetydelight.init.ModFoodSoulEnergyConfig;

import java.util.List;

@EventBusSubscriber(modid = GoetyDelight.MODID, value = Dist.CLIENT) // 省略 bus
public class SoulInfusedTooltipHandler {

    @SubscribeEvent
    public static void onItemTooltip(ItemTooltipEvent event) {
        ItemStack stack = event.getItemStack();
        if (stack.isEmpty()) {
            return;
        }

        CustomData customData = stack.get(DataComponents.CUSTOM_DATA);
        CompoundTag tag = customData != null ? customData.copyTag() : null;
        if (tag != null && tag.getBoolean("SoulInfused")) {
            List<Component> tooltip = event.getToolTip();

            tooltip.add(Component.translatable("tooltip.goetydelight.soul_infused")
                    .withStyle(ChatFormatting.GOLD));

            int baseEnergy = ModFoodSoulEnergyConfig.getSoulEnergyForItem(stack.getItem());
            int bonusEnergy = 0;
            FoodProperties foodProperties = stack.getFoodProperties(null);
            if (foodProperties != null) {
                bonusEnergy = 5 * foodProperties.nutrition(); // 改用 nutrition()
            }
            int totalEnergy = baseEnergy + bonusEnergy;

            if (totalEnergy > 0) {
                tooltip.add(Component.translatable("tooltip.goetydelight.soul_energy_restore", totalEnergy)
                        .withStyle(ChatFormatting.DARK_PURPLE));
            }
        }
    }
}