package net.v_black_cat.goetydelight.item.food;

import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

public class RoastedCorpseMaggotsitem extends Item {
    public RoastedCorpseMaggotsitem(Properties pProperties) {
        super(pProperties);
    }

    @Override
    public int getUseDuration(ItemStack stack) {
        return (int) (32 * 1.5);
    }

}
