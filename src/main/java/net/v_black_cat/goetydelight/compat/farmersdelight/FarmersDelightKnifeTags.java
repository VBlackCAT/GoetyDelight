package net.v_black_cat.goetydelight.compat.farmersdelight;

import net.minecraft.world.item.ItemStack;

/**
 * 统一入口：判断物品是否为 Farmer's Delight 的小刀。
 * <p>
 * 直接使用数据包标签 {@code farmersdelight:tools/knives}，不再反射
 * Farmer's Delight 的 {@code ModTags} 字段或 Minecraft 的 {@code ItemStack#is}。
 * 这样既兼容 1.2.x / 1.3.x，也能在生产环境（方法名被重映射为 SRG）正常工作。
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