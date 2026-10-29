package net.v_black_cat.goetydelight.compat.farmersdelight;

import net.minecraft.world.item.ItemStack;

/**
 * 统一入口：判断物品是否属于 Farmer's Delight 的各类物品标签。
 * <p>
 * 内部走反射“先试 1.3.0+ 的 {@code ModTags$Items.SERVING_CONTAINERS}，
 * 失败再试 &lt;1.3.0 的 {@code ModTags.SERVING_CONTAINERS}”。
 */
public final class FarmersDelightItemTags {

    private FarmersDelightItemTags() {
    }

    /** 是否属于“可盛装食物的容器”标签（serving_containers）。 */
    public static boolean isServingContainer(ItemStack stack) {
        if (stack == null || stack.isEmpty()) {
            return false;
        }
        return FarmersDelightItemTagsLegacy.isServingContainer(stack);
    }
}