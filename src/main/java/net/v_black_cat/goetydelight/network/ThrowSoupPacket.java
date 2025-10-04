package net.v_black_cat.goetydelight.network;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.network.NetworkEvent;
import net.v_black_cat.goetydelight.item.food.EternalRefusalOfBlackMeatSoupItem;
import net.v_black_cat.goetydelight.item.food.RejectedDarkMeatSoupItem;

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
                    // 检查冷却
                    if (soupItem instanceof EternalRefusalOfBlackMeatSoupItem eternalSoup) {
                        if (eternalSoup.isOnCooldown(stack, player.level())) {
                            return;
                        }
                    }

                    soupItem.throwSoup(stack, player);

                    // 如果是永恒拒绝黑肉汤，设置冷却
                    if (stack.getItem() instanceof EternalRefusalOfBlackMeatSoupItem eternalSoup) {
                        eternalSoup.setCooldown(stack, player.level(), 10 * 20);
                    }
                }
            }
        });
        ctx.get().setPacketHandled(true);
    }
}
