package net.v_black_cat.goetydelight.item.food;

import net.minecraft.world.item.ItemStack;

public class FoiledBowlFoodItem extends BowlFoodItem{
    public FoiledBowlFoodItem(Properties pProperties) {
        super(pProperties);
    }

    public boolean isFoil(ItemStack stack) {
        return true;
    }
}
