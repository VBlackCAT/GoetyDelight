package net.v_black_cat.goetydelight.util;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.registries.ForgeRegistries;

public class ToolUtils {
    public static boolean isPickaxe(ItemStack stack) {
        // 检查物品是否在镐子标签中
        return stack.is(ItemTags.PICKAXES);
    }

    /**
     * 根据给定的物品，返回其对应的贴图路径。
     * 此路径可直接用于效果图标的渲染。
     *
     * @param item 目标物品
     * @return 该物品贴图的 ResourceLocation
     */
    public ResourceLocation getItemTexture(Item item) {
        // 获取物品的注册名，例如 "minecraft:apple" 或 "your_mod:custom_item"
        ResourceLocation itemId = ForgeRegistries.ITEMS.getKey(item);
        if (itemId == null) {
            // 如果物品未注册（理论上不应发生），返回一个默认贴图（如泥土）以避免错误
            return new ResourceLocation("minecraft", "textures/item/dirt.png");
        }
        // 根据Minecraft资源路径约定，构造物品贴图的完整路径
        // 格式为：命名空间:textures/item/路径.png
        return new ResourceLocation(itemId.getNamespace(), "textures/item/" + itemId.getPath() + ".png");
    }

}