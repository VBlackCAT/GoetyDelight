package net.v_black_cat.goetydelight.capability;

import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.common.capabilities.*;
import net.minecraftforge.common.util.LazyOptional;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;

public class CustomerOrderItemProvider implements ICapabilityProvider, ICapabilitySerializable<CompoundTag> {
    public static Capability<ICustomerOrderItemList> CAPABILITY = CapabilityManager.get(new CapabilityToken<>() {});
    private final ICustomerOrderItemList instance = new CustomerOrderItemListImp();
    private final LazyOptional<ICustomerOrderItemList> holder = LazyOptional.of(() -> instance);

    @Nonnull
    @Override
    public <T> LazyOptional<T> getCapability(@Nonnull Capability<T> cap, @Nullable Direction side) {
        return cap == CAPABILITY ? holder.cast() : LazyOptional.empty();
    }

    @Override
    public CompoundTag serializeNBT() {
        CompoundTag nbt = new CompoundTag();
        ListTag listTag = new ListTag();
        for (ItemStack stack : instance.getItems()) {
            if (stack != null && !stack.isEmpty()) {
                listTag.add(stack.save(new CompoundTag()));
            }
        }
        nbt.put("CustomerOrderItems", listTag);
        return nbt;
    }

    @Override
    public void deserializeNBT(CompoundTag nbt) {
        ListTag listTag = nbt.getList("items", 10);
        List<ItemStack> items = new ArrayList<>();
        for (int i = 0; i < listTag.size(); i++) {
            items.add(ItemStack.of(listTag.getCompound(i)));
        }
        instance.setItems(items);
    }
}