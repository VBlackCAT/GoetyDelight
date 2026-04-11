package net.v_black_cat.goetydelight.network;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.Entity;
import net.minecraftforge.network.NetworkEvent;
import net.v_black_cat.goetydelight.item.FalseProverbsItem;

import java.util.UUID;
import java.util.function.Supplier;

public class SyncBackModelPacket {
    private final int entityId;
    private final boolean shouldRender;

    public SyncBackModelPacket(int entityId, boolean shouldRender) {
        this.entityId = entityId;
        this.shouldRender = shouldRender;
    }

    public static void encode(SyncBackModelPacket msg, FriendlyByteBuf buffer) {
        buffer.writeInt(msg.entityId);
        buffer.writeBoolean(msg.shouldRender);
    }

    public static SyncBackModelPacket decode(FriendlyByteBuf buffer) {
        return new SyncBackModelPacket(buffer.readInt(), buffer.readBoolean());
    }

    public static void handle(SyncBackModelPacket msg, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            net.minecraft.client.Minecraft mc = net.minecraft.client.Minecraft.getInstance();
            Entity entity = mc.level.getEntity(msg.entityId);

            if (entity != null) {
                UUID playerUUID = entity.getUUID();
                FalseProverbsItem.setPlayerBackModelStatus(playerUUID, msg.shouldRender);
            }
        });
        ctx.get().setPacketHandled(true);
    }
}
