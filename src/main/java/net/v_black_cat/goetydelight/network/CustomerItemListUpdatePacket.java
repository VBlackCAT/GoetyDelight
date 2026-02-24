package net.v_black_cat.goetydelight.network;

import net.minecraft.client.Minecraft;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraftforge.network.NetworkEvent;
import net.v_black_cat.goetydelight.capability.CustomerOrderItemProvider;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

public class CustomerItemListUpdatePacket {
    private final int entityId;
    private final CompoundTag tag;

    public CustomerItemListUpdatePacket(int entityId, CompoundTag tag) {
        this.entityId = entityId;
        this.tag = tag;
    }

    // 编码与解码
    public static void encode(CustomerItemListUpdatePacket packet, FriendlyByteBuf buffer) {
        buffer.writeInt(packet.entityId);
        buffer.writeNbt(packet.tag);
    }

    public static CustomerItemListUpdatePacket decode(FriendlyByteBuf buffer) {
        return new CustomerItemListUpdatePacket(buffer.readInt(), buffer.readNbt());
    }

    // 核心处理逻辑
    public static void consume(CustomerItemListUpdatePacket packet, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            // 必须在客户端执行
            Level level = Minecraft.getInstance().level;
            if (level != null) {
                Entity entity = level.getEntity(packet.entityId); // 通过 ID 获取实体
                if (entity != null) {
                    entity.getCapability(CustomerOrderItemProvider.CAPABILITY).ifPresent(cap -> {
                        // 重用 Provider 的反序列化逻辑更新客户端数据
                        ListTag listTag = packet.tag.getList("items", 10);
                        List<ItemStack> items = new ArrayList<>();
                        for (int i = 0; i < listTag.size(); i++) {
                            items.add(ItemStack.of(listTag.getCompound(i)));
                        }
                        cap.setItems(items);
                    });
                }
            }
        });
        ctx.get().setPacketHandled(true);
    }
}