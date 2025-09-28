package net.v_black_cat.goetydelight.item;

import net.minecraft.world.item.BrushItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

public class CursedIngotBrushItem extends BrushItem {
    public CursedIngotBrushItem(Properties pProperties) {
        super(pProperties);
    }
    public int getUseDuration(ItemStack pStack) {
        return (int) (200/1.2);
    }

}
