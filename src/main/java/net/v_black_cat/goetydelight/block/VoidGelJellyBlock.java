package net.v_black_cat.goetydelight.block;

import net.minecraft.world.item.Item;
import vectorwing.farmersdelight.common.block.FeastBlock;

import java.util.function.Supplier;

public class VoidGelJellyBlock extends FeastBlock {
    public VoidGelJellyBlock(Properties properties, Supplier<Item> servingItem, boolean hasLeftovers) {
        super(properties, servingItem, hasLeftovers);
    }
    public int getMaxServings() {
        return 3;
    }
}
