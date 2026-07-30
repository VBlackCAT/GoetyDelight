package net.v_black_cat.goetydelight.events;

import com.Polarice3.Goety.utils.SEHelper;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.LivingEntityUseItemEvent;
import net.v_black_cat.goetydelight.GoetyDelight;
import net.v_black_cat.goetydelight.api.ISoulFood;
import net.v_black_cat.goetydelight.init.ModFoodSoulEnergyConfig;

@EventBusSubscriber(modid = GoetyDelight.MODID)
public class PlayerEatEventHandler {

    @SubscribeEvent
    public static void onPlayerFinishEating(LivingEntityUseItemEvent.Finish event) {
        if (!(event.getEntity() instanceof Player player) || player.level().isClientSide) {
            return;
        }

        ItemStack finishedItem = event.getItem();
        int soulEnergy = ModFoodSoulEnergyConfig.getSoulEnergyForItem(finishedItem.getItem());

        if (soulEnergy > 0) {
            SEHelper.increaseSouls(player, soulEnergy);
        }

        // 使用 DataComponents 读取自定义数据
        CustomData customData = finishedItem.get(DataComponents.CUSTOM_DATA);
        CompoundTag tag = customData != null ? customData.copyTag() : null;
        if (tag != null && tag.getBoolean("SoulInfused")) {
            FoodProperties foodProperties = finishedItem.getFoodProperties(player);
            if (foodProperties != null) {
                int bonusSoulEnergy = 5 * foodProperties.nutrition();
                SEHelper.increaseSouls(player, bonusSoulEnergy);
            }
        }

        if (finishedItem.getItem() instanceof ISoulFood soulFood) {
            int soulValue = soulFood.getSoulValue();
            if (soulValue > 0) {
                SEHelper.increaseSouls(player, soulValue);
            }
        }

        SEHelper.sendSEUpdatePacket(player);
    }
}