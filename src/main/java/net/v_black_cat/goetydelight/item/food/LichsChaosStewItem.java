package net.v_black_cat.goetydelight.item.food;

import com.Polarice3.Goety.utils.LichdomHelper;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.v_black_cat.goetydelight.init.ModBuffTypes;
import net.v_black_cat.goetydelight.ability.MinionBoost;
import net.v_black_cat.goetydelight.util.BuffUtil;

public class LichsChaosStewItem extends BowlFoodItem {

    private static final int FIRE_RESISTANCE_DURATION = -1;

    public LichsChaosStewItem(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand usedHand) {
        if (LichdomHelper.isLich(player)) {
            return super.use(level, player, usedHand);
        } else {
            return InteractionResultHolder.fail(player.getItemInHand(usedHand));
        }
    }

    @Override
    public ItemStack finishUsingItem(ItemStack stack, Level level, LivingEntity livingEntity) {
        if (LichdomHelper.isLich(livingEntity)) {
            ItemStack result = super.finishUsingItem(stack, level, livingEntity);

            if (!level.isClientSide && livingEntity instanceof Player player) {
                MinionBoost.increaseStewBoostCount(player);
                MinionBoost.applyMinionBoosts(player);

                BuffUtil.applyBuff(player, ModBuffTypes.PERMANENT_FIRE_RESISTANCE.getId(), FIRE_RESISTANCE_DURATION, 0);
                BuffUtil.applyBuff(player, ModBuffTypes.PERMANENT_SAVE_EFFECTS.getId(), FIRE_RESISTANCE_DURATION, 0);

                int currentAmplifier = BuffUtil.getBuffAmplifier(player, ModBuffTypes.MINION_BOOST.getId());
                BuffUtil.applyBuff(player, ModBuffTypes.MINION_BOOST.getId(), FIRE_RESISTANCE_DURATION, currentAmplifier + 1);
            }

            return result;
        }

        return stack;
    }
}