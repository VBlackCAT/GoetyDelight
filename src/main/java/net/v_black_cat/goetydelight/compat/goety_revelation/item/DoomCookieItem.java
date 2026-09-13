package net.v_black_cat.goetydelight.compat.goety_revelation.item;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

public class DoomCookieItem extends Item {
    private static final String USAGE_COUNT_TAG = "DoomCookieUsageCount";

    public DoomCookieItem(Properties properties) {
        super(properties);
    }

    // 获取玩家NBT中的使用次数
    private static CompoundTag getPlayerPersistentData(Player player) {
        return player.getPersistentData();
    }

    public static int getUsageCount(Player player) {
        return getPlayerPersistentData(player).getInt(USAGE_COUNT_TAG);
    }

    public static int incrementUsageCount(Player player) {
        CompoundTag tag = getPlayerPersistentData(player);
        int current = tag.getInt(USAGE_COUNT_TAG);
        int newCount = current + 1;
        tag.putInt(USAGE_COUNT_TAG, newCount);
        return newCount;
    }

    public static void resetUsageCount(Player player) {
        getPlayerPersistentData(player).remove(USAGE_COUNT_TAG);
    }

    public static void setUsageCount(Player player, int count) {
        CompoundTag tag = getPlayerPersistentData(player);
        if (count <= 0) {
            tag.remove(USAGE_COUNT_TAG);
        } else {
            tag.putInt(USAGE_COUNT_TAG, count);
        }
    }

    public static boolean hasReachedCount(Player player, int threshold) {
        return getUsageCount(player) >= threshold;
    }

    @Override
    public ItemStack finishUsingItem(ItemStack stack, Level level, LivingEntity entity) {
        if (!level.isClientSide && entity instanceof Player player) {
            incrementUsageCount(player);
        }
        return super.finishUsingItem(stack, level, entity);
    }

    @Override
    public InteractionResult interactLivingEntity(ItemStack stack, Player player, LivingEntity interactionTarget, InteractionHand usedHand) {
        Level level = player.level();

        if (interactionTarget instanceof Player targetPlayer && targetPlayer != player) {
            if (!level.isClientSide) {
                incrementUsageCount(targetPlayer);
            }
            return InteractionResult.sidedSuccess(level.isClientSide);
        }

        return super.interactLivingEntity(stack, player, interactionTarget, usedHand);
    }
}