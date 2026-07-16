package net.v_black_cat.goetydelight.item.food;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.v_black_cat.goetydelight.init.ModBuffTypes;
import net.v_black_cat.goetydelight.util.BuffUtil;

public class SnapUnholyTripeItem extends PotFoodItem {
    public SnapUnholyTripeItem(Properties pProperties) {
        super(pProperties);
    }

    @Override
    public ItemStack finishUsingItem(ItemStack stack, Level level, LivingEntity entity) {

        ItemStack result = super.finishUsingItem(stack, level, entity);

        if (!level.isClientSide) {

            int durationTicks = 36000;


            BuffUtil.applyBuff(entity, ModBuffTypes.FREEZE_IMMUNITY,durationTicks,0);
            if (entity instanceof net.minecraft.world.entity.player.Player player) {
                //player.sendSystemMessage(net.minecraft.network.chat.Component.literal("你获得了30分钟的冰冻免疫效果！"));
            }
        }

        return result;
    }
}