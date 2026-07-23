package net.v_black_cat.goetydelight.network;

import net.minecraft.client.Minecraft;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import net.v_black_cat.goetydelight.GoetyDelight;
import net.v_black_cat.goetydelight.item.FalseProverbsItem;

public record SyncBackModelPacket(int entityId, boolean shouldRender) implements CustomPacketPayload {

    public static final CustomPacketPayload.Type<SyncBackModelPacket> TYPE =
            new CustomPacketPayload.Type<>(ResourceLocation.parse(GoetyDelight.MODID + ":sync_back_model"));

    public static final StreamCodec<FriendlyByteBuf, SyncBackModelPacket> STREAM_CODEC =
            StreamCodec.composite(
                    ByteBufCodecs.INT, SyncBackModelPacket::entityId,
                    ByteBufCodecs.BOOL, SyncBackModelPacket::shouldRender,
                    SyncBackModelPacket::new
            );

    @Override
    public CustomPacketPayload.Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handleClient(final SyncBackModelPacket packet, final IPayloadContext context) {
        context.enqueueWork(() -> {
            Minecraft mc = Minecraft.getInstance();
            if (mc.level != null) {
                Entity entity = mc.level.getEntity(packet.entityId());
                if (entity != null) {
                    FalseProverbsItem.setPlayerBackModelStatus(entity.getUUID(), packet.shouldRender());
                }
            }
        });
    }

    public static void sendToClient(SyncBackModelPacket packet, net.minecraft.server.level.ServerPlayer player) {
        player.connection.send(packet);
    }
}