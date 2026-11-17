package net.v_black_cat.goetydelight.compat.farmersdelight;

import net.minecraft.world.item.ItemStack;

/**
 * 统一入口：判断物品是否属于 Farmer's Delight 的各类物品标签。
 * <p>
 * 直接使用数据包标签 {@code farmersdelight:serving_containers}，不依赖版本号，
 * 也不会在生产环境因为反射 Minecraft 方法名（SRG 重映射）而失效。
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