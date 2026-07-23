package net.v_black_cat.goetydelight.network;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import net.v_black_cat.goetydelight.GoetyDelight;
import net.v_black_cat.goetydelight.visual.client.ClientEntityVisualEffectPackets;

public record SyncEntityVisualEffectsPayload(int entityId, CompoundTag effects) implements CustomPacketPayload {
    public static final Type<SyncEntityVisualEffectsPayload> TYPE = new Type<>(
            ResourceLocation.fromNamespaceAndPath(GoetyDelight.MODID, "sync_entity_visual_effects"));

    public static final StreamCodec<FriendlyByteBuf, SyncEntityVisualEffectsPayload> STREAM_CODEC =
            StreamCodec.composite(
                    ByteBufCodecs.VAR_INT, SyncEntityVisualEffectsPayload::entityId,
                    ByteBufCodecs.COMPOUND_TAG, SyncEntityVisualEffectsPayload::effects,
                    SyncEntityVisualEffectsPayload::new);

    public SyncEntityVisualEffectsPayload {
        effects = effects.copy();
    }

    public static void handleClient(SyncEntityVisualEffectsPayload payload, IPayloadContext context) {
        context.enqueueWork(() -> {
            ClientEntityVisualEffectPackets.sync(payload.entityId(), payload.effects());
        });
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
