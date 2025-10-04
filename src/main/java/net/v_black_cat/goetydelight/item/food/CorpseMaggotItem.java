package net.v_black_cat.goetydelight.item.food;

import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

public class CorpseMaggotItem extends Item {
    public CorpseMaggotItem(Properties pProperties) {
        super(pProperties);
    }

    @Override
    public int getUseDuration(ItemStack stack) {
        return (int) (32 * 2);
    }

}
