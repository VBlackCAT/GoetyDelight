package net.v_black_cat.goetydelight.item.food;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.v_black_cat.goetydelight.init.ModBuffTypes;
import net.v_black_cat.goetydelight.util.BuffUtil;

public class RubyHardCandyItem extends Item {
    // 免伤持续时间（10分钟，以tick为单位）
    private static final int DAMAGE_REDUCTION_DURATION = 20 * 60 * 10;

    public RubyHardCandyItem(Properties pProperties) {
        super(pProperties);
    }

    @Override
    public ItemStack finishUsingItem(ItemStack stack, Level level, LivingEntity entity) {
        stack = super.finishUsingItem(stack, level, entity);

        if (!level.isClientSide && entity instanceof Player) {
            // 应用伤害减免 Buff
            BuffUtil.applyBuff(
                    entity,
                    ModBuffTypes.RUBY_HARD_CANDY_DAMAGE_REDUCTION.getId(),
                    DAMAGE_REDUCTION_DURATION,
                    0
            );
        }

        return stack;
    }
}
