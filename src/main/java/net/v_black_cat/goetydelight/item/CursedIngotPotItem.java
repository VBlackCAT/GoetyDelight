package net.v_black_cat.goetydelight.item;

import net.minecraft.util.Mth;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.v_black_cat.goetydelight.block.CursedIngotPotBlockEntity;

/**
 * 诅咒金属锅方块物品：与农夫乐事原版 CookingPotItem 完全同款，
 * 当物品携带暂存餐数据时，在物品图标上显示一条蓝色"耐久条"，
 * 条宽随暂存餐份数变化（getServingCount / 64，与原版一致）。
 */
public class CursedIngotPotItem extends BlockItem {

    private static final int BAR_COLOR = Mth.color(0.4F, 0.4F, 1.0F);

    public CursedIngotPotItem(Block block, Properties properties) {
        super(block, properties);
    }

    /** 暂存餐份数（堆叠数量），从物品携带的 MEAL 数据组件读取 */
    private static int getServingCount(ItemStack stack) {
        ItemStack mealStack = CursedIngotPotBlockEntity.getMealFromItem(stack);
        return mealStack.getCount();
    }

    @Override
    public boolean isBarVisible(ItemStack stack) {
        return getServingCount(stack) > 0;
    }

    @Override
    public int getBarWidth(ItemStack stack) {
        return Math.min(1 + 12 * getServingCount(stack) / 64, 13);
    }

    @Override
    public int getBarColor(ItemStack stack) {
        return BAR_COLOR;
    }
}
