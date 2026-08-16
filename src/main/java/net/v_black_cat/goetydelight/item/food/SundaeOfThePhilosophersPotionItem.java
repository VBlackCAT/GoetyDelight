package net.v_black_cat.goetydelight.item.food;

import com.Polarice3.Goety.utils.LichdomHelper;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.v_black_cat.goetydelight.init.ModAttachments;
import net.v_black_cat.goetydelight.util.FoodState;

public class SundaeOfThePhilosophersPotionItem extends Item {

    private static final int MAX_MINING_BOOST_COUNT = 3;
    private static final int MAX_MAGIC_RESISTANCE_COUNT = 1;

    public SundaeOfThePhilosophersPotionItem(Properties properties) {
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
        ItemStack result = super.finishUsingItem(stack, level, livingEntity);

        if (!level.isClientSide && livingEntity instanceof Player player) {
            applyMiningSpeedBoost(player);
            applyMagicResistanceBoost(player);
        }

        if (livingEntity instanceof Player player) {
            if (player.getAbilities().instabuild) {
                return result;
            }

            if (result.isEmpty()) {
                return new ItemStack(Items.BOWL);
            } else if (!player.getInventory().add(new ItemStack(Items.BOWL))) {
                player.drop(new ItemStack(Items.BOWL), false);
            }
        }

        return result;
    }

    private void applyMiningSpeedBoost(Player player) {
        int currentCount = getMiningSpeedBoostCount(player);
        if (currentCount < MAX_MINING_BOOST_COUNT) {
            player.getData(ModAttachments.FOOD_STATE).setPhilosopherMiningBoost(currentCount + 1);
        }
    }

    private void applyMagicResistanceBoost(Player player) {
        int currentCount = getMagicResistanceBoostCount(player);
        if (currentCount < MAX_MAGIC_RESISTANCE_COUNT) {
            player.getData(ModAttachments.FOOD_STATE).setPhilosopherMagicResistance(currentCount + 1);
        }
    }

    public static int getMiningSpeedBoostCount(Player player) {
        return player.getData(ModAttachments.FOOD_STATE).getPhilosopherMiningBoost();
    }

    public static int getMagicResistanceBoostCount(Player player) {
        return player.getData(ModAttachments.FOOD_STATE).getPhilosopherMagicResistance();
    }

    /**
     * 挖掘速度加成 — 由 BreakSpeed 事件处理器调用
     */
    public static float applyMiningSpeedBoost(Player player, float originalSpeed) {
        int boostCount = getMiningSpeedBoostCount(player);
        if (boostCount > 0) {
            return originalSpeed * (1.0f + 0.1f * boostCount);
        }
        return originalSpeed;
    }

    /**
     * 魔法伤害抵抗 — 由 LivingDamage 事件处理器调用
     */
    public static float applyMagicResistance(Player player, float originalDamage, net.minecraft.world.damagesource.DamageSource source) {
        int resistanceCount = getMagicResistanceBoostCount(player);
        if (resistanceCount > 0 && isMagicDamage(source)) {
            return originalDamage * 0.5f;
        }
        return originalDamage;
    }

    private static boolean isMagicDamage(net.minecraft.world.damagesource.DamageSource source) {
        return source.is(net.minecraft.tags.DamageTypeTags.WITCH_RESISTANT_TO);
    }
}
