package net.v_black_cat.goetydelight.compat.goety_revelation.item;

import com.mega.endinglib.api.client.text.TextColorUtils;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;

import javax.annotation.Nullable;
import java.util.List;

public class AtonementVoucherWrapedCodItem extends Item {
    private static final String USAGE_COUNT_TAG = "AtonementVoucherUsageCount";

    public AtonementVoucherWrapedCodItem(Properties properties) {
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

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltip, TooltipFlag flag) {
        tooltip.add(Component.literal(""));
        if (Screen.hasShiftDown()) {
            tooltip.add(Component.translatable("item.goetydelight.atonement_voucher_wraped_cod.tooltip.1")
                    .withStyle(ChatFormatting.LIGHT_PURPLE));
        } else {
            tooltip.add(Component.translatable("item.goetydelight.tooltip.shift"));
        }
    }
}