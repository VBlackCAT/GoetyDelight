package net.v_black_cat.goetydelight.block;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;

/**
 * 黑铁锅：
 *  - 烹饪时间 ×0.5（等效速度 ×2）
 *  - 灵魂消耗 ×0.5
 *  - 产物灵魂强度 ×2（SoulInfusedLevel = 2）
 *  - 必须存在真实灵魂源才能产出（避免产出无灵魂食物）
 * 其余逻辑全部复用 CursedIngotPotBlockEntity。
 */
public class BlackIronPotBlockEntity extends CursedIngotPotBlockEntity {

    /** 产物上记录灵魂强度的 NBT 键（2 倍强度对应等级 2）。 */
    public static final String SOUL_LEVEL_TAG = "SoulInfusedLevel";
    /** 灵魂强度等级：CursedIngotPot 为 1，黑铁锅为 2。 */
    public static final int SOUL_LEVEL = 2;

    public BlackIronPotBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.BLACK_IRON_POT_BE.get(), pos, state);
    }

    // ===================== 倍率钩子 =====================

    /** 烹饪时间 0.5 倍：总时长减半，等效烹饪速度 ×2。 */
    @Override
    protected int modifyCookTimeTotal(int original) {
        return Math.max(1, original / 2);
    }

    /** 灵魂消耗 0.5 倍：向下取整，至少 1。 */
    @Override
    protected int modifySoulCost(int original) {
        return Math.max(1, original / 2);
    }

    /** 产物灵魂强度 2 倍：在 SoulInfused 标记之外写入等级，供下游系统读取。 */
    @Override
    protected void applySoulInfusion(ItemStack resultStack) {
        CompoundTag tag = resultStack.getOrCreateTag();
        tag.putBoolean("SoulInfused", true);
        tag.putInt(SOUL_LEVEL_TAG, SOUL_LEVEL);
        resultStack.setTag(tag);
    }

    /** 黑铁锅灵魂消耗减半但仍需灵魂源：无灵魂时不产出无灵魂食物。 */
    @Override
    protected boolean requiresSoulSource() {
        return true;
    }
}