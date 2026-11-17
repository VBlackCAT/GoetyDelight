package net.v_black_cat.goetydelight.compat.farmersdelight;

import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

/**
 * 餐具/容器标签判定。
 * <p>
 * 直接使用 Farmer's Delight 的数据包标签 {@code farmersdelight:serving_containers}，
 * 不再反射 Minecraft 的 {@code ItemStack#is}（生产环境方法名会被重映射为 SRG 名）。
 */
final class FarmersDelightItemTagsLegacy {

    private static final TagKey<Item> SERVING_CONTAINERS = TagKey.create(
            Registries.ITEM,
            new ResourceLocation("farmersdelight", "serving_containers"));

    private FarmersDelightItemTagsLegacy() {
    }

    static boolean isServingContainer(ItemStack stack) {
        return stack != null && !stack.isEmpty() && stack.is(SERVING_CONTAINERS);
    }
}
