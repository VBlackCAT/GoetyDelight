package net.v_black_cat.goetydelight.network;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.network.NetworkEvent;
import net.v_black_cat.goetydelight.item.RejectedDarkMeatSoupItem;

import java.util.UUID;
import java.util.function.Supplier;

public class ThrowSoupPacket {
    private final UUID playerUUID;

    public ThrowSoupPacket(UUID playerUUID) {
        this.playerUUID = playerUUID;
    }

    public static void encode(ThrowSoupPacket msg, FriendlyByteBuf buffer) {
        buffer.writeUUID(msg.playerUUID);
    }

    public static ThrowSoupPacket decode(FriendlyByteBuf buffer) {
        return new ThrowSoupPacket(buffer.readUUID());
    }

    public static void handle(ThrowSoupPacket msg, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            ServerPlayer player = ctx.get().getSender();
            if (player != null && player.getUUID().equals(msg.playerUUID)) {
                ItemStack stack = player.getMainHandItem();
                if (stack.getItem() instanceof RejectedDarkMeatSoupItem soupItem) {
                    soupItem.throwSoup(stack, player);
                }
            }
        });
        ctx.get().setPacketHandled(true);
    }
}
