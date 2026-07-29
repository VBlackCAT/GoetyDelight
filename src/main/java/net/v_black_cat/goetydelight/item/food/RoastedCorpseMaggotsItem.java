package net.v_black_cat.goetydelight.item.food;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

public class RoastedCorpseMaggotsItem extends Item {
    public RoastedCorpseMaggotsItem(Properties pProperties) {
        super(pProperties);
    }

    @Override
    public int getUseDuration(ItemStack stack, LivingEntity entity) {
        return (int) (32 * 1.5);
    }
}