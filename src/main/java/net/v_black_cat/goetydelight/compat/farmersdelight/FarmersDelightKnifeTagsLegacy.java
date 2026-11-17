package net.v_black_cat.goetydelight.compat.farmersdelight;

import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

/**
 * 刀具标签判定。
 * <p>
 * 直接使用 Farmer's Delight 的数据包标签 {@code farmersdelight:tools/knives}。
 * <p>
 * 注意：不要在这里通过反射调用 {@link ItemStack#is(TagKey)}。Forge 生产环境会把
 * Minecraft 的方法名重映射为 SRG 名（如 {@code m_204117_}），而开发环境使用官方名
 * （{@code is}），按字符串反射 {@code "is"} 只在开发环境可用，导出后会静默失败，
 * 导致小刀切耳朵等功能在实机失效。直接编译期调用则由 ForgeGradle 自动完成重映射。
 */
final class FarmersDelightKnifeTagsLegacy {

    private static final TagKey<Item> KNIVES = TagKey.create(
            Registries.ITEM,
            new ResourceLocation("farmersdelight", "tools/knives"));

    private FarmersDelightKnifeTagsLegacy() {
    }

    static boolean isKnife(ItemStack stack) {
        return stack != null && !stack.isEmpty() && stack.is(KNIVES);
    }
}
