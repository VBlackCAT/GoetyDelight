package net.v_black_cat.goetydelight.network;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public record SyncFoxKillCountPacket(int foxKillCount) {

    public static void encode(SyncFoxKillCountPacket msg, FriendlyByteBuf buffer) {
        buffer.writeInt(msg.foxKillCount);
    }

    public static SyncFoxKillCountPacket decode(FriendlyByteBuf buffer) {
        return new SyncFoxKillCountPacket(buffer.readInt());
    }

    public static void handle(SyncFoxKillCountPacket msg, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            ClientHandle.handleSyncFoxKillCountPacket(msg);
        });
        ctx.get().setPacketHandled(true);
    }
}
