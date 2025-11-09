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

public class NoGlassBottleDrinkItem extends Item {
    public NoGlassBottleDrinkItem(Properties pProperties) {
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

        if (entity instanceof Player player) {
            if (player.getAbilities().instabuild) {
                return result; // 创造模式不消耗物品
            }
        }
        return result;
    }
}
