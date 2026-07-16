package net.v_black_cat.goetydelight.item.food;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;

public class BowlFoodItem extends Item {
    public BowlFoodItem(Properties pProperties) {
        super(pProperties);
    }

    @Override
    public ItemStack finishUsingItem(ItemStack stack, Level level, LivingEntity entity) {
        // 调用父类方法处理食物效果
        ItemStack result = super.finishUsingItem(stack, level, entity);

        // 处理碗的返还逻辑
        if (entity instanceof Player player) {
            if (player.getAbilities().instabuild) {
                return result; // 创造模式不消耗物品
            }

            // 尝试将碗添加到玩家物品栏
            if (result.isEmpty()) {
                return new ItemStack(Items.BOWL); // 如果原物品已消耗完，直接返还碗
            } else if (!player.getInventory().add(new ItemStack(Items.BOWL))) {
                player.drop(new ItemStack(Items.BOWL), false); // 背包满时掉落碗
            }
        }

        return result;
    }

}
