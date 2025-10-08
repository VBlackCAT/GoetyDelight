package net.v_black_cat.goetydelight.screen;

import net.minecraft.world.Container;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import com.Polarice3.Goety.api.items.magic.ITotem;
import com.Polarice3.Goety.common.items.ModItems;

public class SoulFuelSlot extends Slot {
    public SoulFuelSlot(Container container, int index, int x, int y) {
        super(container, index, x, y);
    }

    @Override
    public boolean mayPlace(ItemStack stack) {
        return stack.getItem() instanceof ITotem ||
                stack.getItem() == ModItems.SOUL_TRANSFER.get();
    }
}