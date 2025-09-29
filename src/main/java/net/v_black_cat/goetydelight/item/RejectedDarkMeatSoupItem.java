package net.v_black_cat.goetydelight.item;

import net.minecraft.stats.Stats;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.ThrownPotion;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ThrowablePotionItem;
import net.minecraft.world.item.alchemy.PotionUtils;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;

import static net.v_black_cat.goetydelight.util.TimeConverter.sToTick;

public class RejectedDarkMeatSoupItem extends Item {

    public RejectedDarkMeatSoupItem(Properties pProperties) {
        super(pProperties);
    }

    public @NotNull String getDescriptionId(ItemStack stack) {
        return this.getOrCreateDescriptionId();
    }

    @Override
    public int getUseDuration(ItemStack stack) {
        return (int) (32 * 2.5);
    }



    @Override
    public boolean hurtEnemy(ItemStack stack, LivingEntity target, LivingEntity attacker) {
        if (attacker instanceof Player player) {
            throwPotion(stack, player.level(), player);
            if (!player.getAbilities().instabuild) {
                stack.shrink(1);
            }
            return true;
        }
        return false;
    }


    // 投掷逻辑封装
    private void throwPotion(ItemStack stack, Level level, Player player) {
        if (!level.isClientSide) {
            // 复制物品堆以避免修改原始物品
            ItemStack throwStack = stack.copy();
            throwStack.setCount(1);

            // 设置随机效果
            int randomAmplifier = level.random.nextInt(5);
            PotionUtils.setCustomEffects(throwStack, Arrays.asList(
                    new MobEffectInstance(MobEffects.CONFUSION, sToTick(30)),
                    new MobEffectInstance(MobEffects.POISON, sToTick(30), randomAmplifier),
                    new MobEffectInstance(MobEffects.WEAKNESS, sToTick(30), 1)
            ));

            // 创建投掷物
            ThrownPotion thrownPotion = new ThrownPotion(level, player);
            thrownPotion.setItem(throwStack);
            thrownPotion.shootFromRotation(player, player.getXRot(), player.getYRot(), -20.0F, 0.5F, 1.0F);
            level.addFreshEntity(thrownPotion);
        }

        player.awardStat(Stats.ITEM_USED.get(this));
    }

    public void throwSoup(ItemStack stack, LivingEntity attacker) {
        if (attacker instanceof Player player) {
            throwPotion(stack, player.level(), player);
            if (!player.getAbilities().instabuild) {
                stack.shrink(1);
            }
        }
    }
}