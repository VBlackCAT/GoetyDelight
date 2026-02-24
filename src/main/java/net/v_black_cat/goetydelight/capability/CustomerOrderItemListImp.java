package net.v_black_cat.goetydelight.capability;

import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.List;

public class CustomerOrderItemListImp implements ICustomerOrderItemList {
    private List<ItemStack> items = new ArrayList<>();
    @Override public List<ItemStack> getItems() { return this.items; }
    @Override public void setItems(List<ItemStack> items) { this.items = items; }
}