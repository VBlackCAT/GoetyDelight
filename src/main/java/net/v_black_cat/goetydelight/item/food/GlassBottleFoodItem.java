package net.v_black_cat.goetydelight.item.food;

import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.UseAnim;
import net.minecraft.world.level.Level;

public class GlassBottleFoodItem extends Item {
    public GlassBottleFoodItem(Properties pProperties) {
        super(pProperties);
    }
    @Override
    public UseAnim getUseAnimation(ItemStack stack) {
        return UseAnim.DRINK;
    }

    @Override
    public SoundEvent getDrinkingSound() {
        return SoundEvents.GENERIC_DRINK;
    }

    @Override
    public ItemStack finishUsingItem(ItemStack stack, Level level, LivingEntity entity) {
        // 调用父类方法处理食物效果
        ItemStack result = super.finishUsingItem(stack, level, entity);

        // 处理玻璃瓶的返还逻辑
        if (entity instanceof Player player) {
            if (player.getAbilities().instabuild) {
                return result; // 创造模式不消耗物品
            }

            // 尝试将玻璃瓶添加到玩家物品栏
            if (result.isEmpty()) {
                return new ItemStack(Items.GLASS_BOTTLE); // 如果原物品已消耗完，直接返还玻璃瓶
            } else if (!player.getInventory().add(new ItemStack(Items.GLASS_BOTTLE))) {
                player.drop(new ItemStack(Items.GLASS_BOTTLE), false); // 背包满时掉落玻璃瓶
            }
        }

        return result;
    }

}
