package net.v_black_cat.goetydelight.network;

import net.minecraft.client.Minecraft;
import net.minecraft.nbt.ListTag;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.v_black_cat.goetydelight.capability.CustomerOrderItemProvider;

import java.util.ArrayList;
import java.util.List;

public class ClientHandle {
    static void handleCustomerItemListUpdatePacket(CustomerItemListUpdatePacket packet) {
        Level level = Minecraft.getInstance().level;
        if (level != null) {
            Entity entity = level.getEntity(packet.entityId); // 通过 ID 获取实体
            if (entity != null) {
                entity.getCapability(CustomerOrderItemProvider.CAPABILITY).ifPresent(cap -> {
                    ListTag listTag = packet.tag.getList("CustomerOrderItems", 10);
                    List<ItemStack> items = new ArrayList<>();
                    for (int i = 0; i < listTag.size(); i++) {
                        items.add(ItemStack.of(listTag.getCompound(i)));
                    }
                    cap.setItems(items);
                });
            }
        }
    }
}
