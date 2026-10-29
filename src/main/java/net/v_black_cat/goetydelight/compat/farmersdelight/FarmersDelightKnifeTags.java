package net.v_black_cat.goetydelight.compat.farmersdelight;

import net.minecraft.world.item.ItemStack;

/**
 * 统一入口：判断物品是否为 Farmer's Delight 的小刀。
 * <p>
 * 内部走反射"先试 1.3.0+ 的 {@code ModTags$Items.KNIVES}，
 * 失败再试 &lt;1.3.0 的 {@code ModTags.KNIVES}"，
 * 不依赖版本号，也不会因 import/类引用导致 NoClassDefFoundError。
 */
public final class FarmersDelightKnifeTags {

    private FarmersDelightKnifeTags() {
    }

    /** 是否属于"小刀"标签（knives）。 */
    public static boolean isKnife(ItemStack stack) {
        if (stack == null || stack.isEmpty()) {
            return false;
        }
        return FarmersDelightKnifeTagsLegacy.isKnife(stack);
    }
}