package net.v_black_cat.goetydelight.network;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkEvent;
import net.v_black_cat.goetydelight.visual.client.ClientEntityVisualEffectPackets;

import java.util.function.Supplier;

public class SyncEntityVisualEffectsPacket {
    private final int entityId;
    private final CompoundTag effects;

    public SyncEntityVisualEffectsPacket(int entityId, CompoundTag effects) {
        this.entityId = entityId;
        this.effects = effects.copy();
    }

    public static void encode(SyncEntityVisualEffectsPacket packet, FriendlyByteBuf buf) {
        buf.writeVarInt(packet.entityId);
        buf.writeNbt(packet.effects);
    }

    public static SyncEntityVisualEffectsPacket decode(FriendlyByteBuf buf) {
        return new SyncEntityVisualEffectsPacket(buf.readVarInt(), buf.readNbt());
    }

    public static void handle(SyncEntityVisualEffectsPacket packet, Supplier<NetworkEvent.Context> contextSupplier) {
        NetworkEvent.Context context = contextSupplier.get();
        context.enqueueWork(() -> DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> ClientEntityVisualEffectPackets.sync(packet.entityId, packet.effects)));
        context.setPacketHandled(true);
    }
}
