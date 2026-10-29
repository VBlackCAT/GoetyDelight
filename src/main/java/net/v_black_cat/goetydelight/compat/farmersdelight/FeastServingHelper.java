package net.v_black_cat.goetydelight.compat.farmersdelight;

import net.minecraft.core.BlockPos;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.stats.Stats;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.state.BlockState;
import vectorwing.farmersdelight.common.block.FeastBlock;

/**
 * FeastBlock 通用取餐逻辑。
 */
public final class FeastServingHelper {

    @FunctionalInterface
    public interface ServingsSetter {
        void setServings(LevelAccessor level, BlockPos pos, BlockState state, int servings);
    }

    @FunctionalInterface
    public interface StructureBreaker {
        void breakStructure(LevelAccessor level, BlockPos pos, BlockState state, Player player);
    }

    private FeastServingHelper() {
    }

    public static InteractionResult takeServing(LevelAccessor level, BlockPos pos, BlockState state,
                                                Player player, InteractionHand hand,
                                                FeastBlock feastBlock,
                                                ServingsSetter setServings,
                                                StructureBreaker breakStructure) {
        int servings = state.getValue(feastBlock.getServingsProperty());

        // 份数为 0：播放音效并拆除整个结构
        if (servings == 0) {
            level.playSound(null, pos, SoundEvents.WOOD_BREAK, SoundSource.PLAYERS, 0.8F, 0.8F);
            breakStructure.breakStructure(level, pos, state, null);
            return InteractionResult.SUCCESS;
        }

        ItemStack serving = feastBlock.getServingItem(state);
        ItemStack heldStack = player.getItemInHand(hand);

        if (servings > 0) {
            // 判断容器是否匹配（例如碗）
            if (!serving.hasCraftingRemainingItem()
                    || ItemStack.isSameItem(heldStack, serving.getCraftingRemainingItem())) {

                // 1) 整结构份数 -1
                setServings.setServings(level, pos, state, servings - 1);
                // 2) 统计
                player.awardStat(Stats.ITEM_USED.get(heldStack.getItem()));
                // 3) 消耗容器（创造模式不消耗）
                if (!player.getAbilities().instabuild && serving.hasCraftingRemainingItem()) {
                    heldStack.shrink(1);
                }
                // 4) 发放食物（背包满则掉落到地面）
                if (!player.getInventory().add(serving)) {
                    player.drop(serving, false);
                }
                // 5) 最后一份且无残留：拆除整个结构
                if (servings - 1 == 0 && !feastBlock.hasLeftovers) {
                    breakStructure.breakStructure(level, pos, state, null);
                }

                // 6) 取餐音效（自动适配 1.2.x 和 1.3.0+）
                level.playSound(null, pos, FarmersDelightSoundsLegacy.getFoodTakePortion(),
                        SoundSource.BLOCKS, 1.0F, 1.0F);

                return InteractionResult.SUCCESS;
            } else {
                // 容器不匹配：提示需要什么容器
                player.displayClientMessage(
                        FarmersDelightTextUtilsLegacy.feastUseContainer(
                                serving.getCraftingRemainingItem()), true);
            }
        }
        return InteractionResult.PASS;
    }
}