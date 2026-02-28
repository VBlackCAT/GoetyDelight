package net.v_black_cat.goetydelight.network;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkDirection;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class CustomerItemListUpdatePacket {
    final int entityId;
    final CompoundTag tag;

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
            if (ctx.get().getDirection() == NetworkDirection.PLAY_TO_CLIENT) {
                ClientHandle.handleCustomerItemListUpdatePacket(packet);
            }
        });
        ctx.get().setPacketHandled(true);
    }

}