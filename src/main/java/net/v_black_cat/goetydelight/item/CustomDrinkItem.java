package net.v_black_cat.goetydelight.item;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.stats.Stats;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemUtils;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.UseAnim;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.gameevent.GameEvent;
import net.v_black_cat.goetydelight.init.ModDataComponents;
import net.v_black_cat.goetydelight.util.BuffUtil;

/**
 * 通用饮品
 */
public class CustomDrinkItem extends Item {

    public CustomDrinkItem(Properties properties) {
        super(properties);
    }

    @Override
    public int getUseDuration(ItemStack stack, LivingEntity entity) {

        return 32;
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
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        return ItemUtils.startUsingInstantly(level, player, hand);
    }

    @Override
    public ItemStack finishUsingItem(ItemStack stack, Level level, LivingEntity entity) {
        super.finishUsingItem(stack,level,entity);
        if (!level.isClientSide) {
            ModDataComponents.BuffData buffData = stack.get(ModDataComponents.ITEM_BUFF);
            if (buffData != null && entity instanceof ServerPlayer player) {
                BuffUtil.applyBuff(player, buffData.buffTypeId(), buffData.duration(), buffData.amplifier());
            }
        }

        if (entity instanceof Player player) {
            player.awardStat(Stats.ITEM_USED.get(this));
            stack.consume(1, player);

            if (!player.hasInfiniteMaterials()) {
                if (stack.isEmpty()) {
                    return new ItemStack(Items.GLASS_BOTTLE);
                }
                player.getInventory().add(new ItemStack(Items.GLASS_BOTTLE));
            }
        }

        entity.gameEvent(GameEvent.DRINK);
        return stack;
    }
}