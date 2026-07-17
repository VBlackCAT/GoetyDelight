package net.v_black_cat.goetydelight.network;

import net.minecraft.client.Minecraft;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraftforge.network.NetworkEvent;
import net.v_black_cat.goetydelight.buff.IBuffHolder;

import java.util.function.Supplier;


public class SyncBuffPacket {
    private final int entityId;
    private final ResourceLocation typeId;
    private final boolean add;

    public SyncBuffPacket(int entityId, ResourceLocation typeId, boolean add) {
        this.entityId = entityId;
        this.typeId = typeId;
        this.add = add;
    }

    public static void encode(SyncBuffPacket msg, FriendlyByteBuf buffer) {
        buffer.writeInt(msg.entityId);
        buffer.writeResourceLocation(msg.typeId);
        buffer.writeBoolean(msg.add);
    }

    public static SyncBuffPacket decode(FriendlyByteBuf buffer) {
        return new SyncBuffPacket(buffer.readInt(), buffer.readResourceLocation(), buffer.readBoolean());
    }

    public static void handle(SyncBuffPacket msg, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            Minecraft mc = Minecraft.getInstance();
            if (mc.level == null) return;
            Entity entity = mc.level.getEntity(msg.entityId);

            if (entity instanceof LivingEntity livingEntity && livingEntity instanceof IBuffHolder holder) {
                var buffs = holder.goetydelight$getActiveBuffs();
                if (buffs == null) return;

                if (msg.add) {
                    buffs.addBuff(msg.typeId, -1, 0);
                } else {
                    buffs.removeBuff(msg.typeId);
                }
            }
        });
        ctx.get().setPacketHandled(true);
    }
}
