package net.v_black_cat.goetydelight.item;

import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

/**
 * 幻味草物品 - 迁移自 1.20.1（含 DataComponents API 迁移）
 * ScreenMixin 需要使用 MetamorphicScentGrassRenderItem
 */
public class MetamorphicScentGrassItem extends Item {
    public MetamorphicScentGrassItem(Properties properties) {
        super(properties);
    }

    public static ItemStack MetamorphicScentGrassRenderItem(ItemStack grassStack) {
        return grassStack;
    }
}
