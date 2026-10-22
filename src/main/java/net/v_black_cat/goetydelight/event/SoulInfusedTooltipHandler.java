package net.v_black_cat.goetydelight.event;

import net.minecraft.ChatFormatting;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.event.entity.player.ItemTooltipEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.v_black_cat.goetydelight.GoetyDelight;
import net.v_black_cat.goetydelight.config.FoodSoulEnergyConfig;

import java.util.List;

/**
 * 为"已注入灵魂"的食物追加物品提示：
 * 已注入灵魂（金色）
 * 食用后恢复 X 点灵魂能量（暗紫）
 *
 * 数值与 PlayerEatEventHandler 的实际结算严格保持一致：
 *   total = FoodSoulEnergyConfig 基础值 + 5 × 饱食度 × SoulInfusedLevel
 * 其中 SoulInfusedLevel 缺省为 1，且最小钳制为 1（防止损坏数据导致负数）。
 */
@Mod.EventBusSubscriber(modid = GoetyDelight.MODID, bus = Mod.EventBusSubscriber.Bus.FORGE, value = Dist.CLIENT)
public class SoulInfusedTooltipHandler {

    @SubscribeEvent
    public static void onItemTooltip(ItemTooltipEvent event) {
        ItemStack stack = event.getItemStack();
        if (stack.isEmpty()) {
            return;
        }

        CompoundTag tag = stack.getTag();
        if (tag != null && tag.getBoolean("SoulInfused")) {
            List<Component> tooltip = event.getToolTip();

            tooltip.add(Component.translatable("tooltip.goetydelight.soul_infused")
                    .withStyle(ChatFormatting.GOLD));

            // 读取灵魂强度等级：普通锅=1，黑铁锅=2，天启锅=10；旧存档无等级回退到 1
            int soulLevel = tag.contains("SoulInfusedLevel") ? tag.getInt("SoulInfusedLevel") : 1;
            if (soulLevel < 1) {
                soulLevel = 1;
            }

            int baseEnergy = FoodSoulEnergyConfig.getSoulEnergyForItem(stack.getItem());
            int bonusEnergy = 0;
            FoodProperties foodProperties = stack.getFoodProperties(null);
            if (foodProperties != null) {
                // 与 PlayerEatEventHandler 一致：5 × 饱食度 × 灵魂强度等级
                bonusEnergy = 5 * foodProperties.getNutrition() * soulLevel;
            }
            int totalEnergy = baseEnergy + bonusEnergy;

            if (totalEnergy > 0) {
                tooltip.add(Component.translatable("tooltip.goetydelight.soul_energy_restore", totalEnergy)
                        .withStyle(ChatFormatting.DARK_PURPLE));
            }
        }
    }
}