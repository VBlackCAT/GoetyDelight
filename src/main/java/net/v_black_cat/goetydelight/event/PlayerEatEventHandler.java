package net.v_black_cat.goetydelight.event;

import com.Polarice3.Goety.utils.SEHelper;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.event.entity.living.LivingEntityUseItemEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.v_black_cat.goetydelight.config.FoodSoulEnergyConfig;
import net.v_black_cat.goetydelight.GoetyDelight;

@Mod.EventBusSubscriber(modid = GoetyDelight.MODID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class PlayerEatEventHandler {

    @SubscribeEvent
    public static void onPlayerFinishEating(LivingEntityUseItemEvent.Finish event) {
        if (!(event.getEntity() instanceof Player player) || player.level().isClientSide) {
            return;
        }

        ItemStack finishedItem = event.getItem();
        int soulEnergy = FoodSoulEnergyConfig.getSoulEnergyForItem(finishedItem.getItem());

        // 基础灵魂能量恢复
        if (soulEnergy > 0) {
            SEHelper.increaseSouls(player, soulEnergy);
        }

        // 检查食物是否带有"SoulInfused"标签
        CompoundTag tag = finishedItem.getTag();
        if (tag != null && tag.getBoolean("SoulInfused")) {
            // 获取食物属性
            FoodProperties foodProperties = finishedItem.getFoodProperties(player);
            if (foodProperties != null) {
                // 计算额外灵魂能量：5 * 饱食度
                int bonusSoulEnergy = 5 * foodProperties.getNutrition();
                SEHelper.increaseSouls(player, bonusSoulEnergy);
            }
        }

        // 更新玩家灵魂能量显示
        SEHelper.sendSEUpdatePacket(player);
    }
}