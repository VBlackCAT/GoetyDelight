package net.v_black_cat.goetydelight.capability;

import net.minecraft.world.item.ItemStack;

import java.util.List;


public interface ICustomerOrderItemList {
    List<ItemStack> getItems();
    void setItems(List<ItemStack> items);
}


