package net.v_black_cat.goetydelight.network;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkDirection;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.simple.SimpleChannel;
import net.v_black_cat.goetydelight.GoetyDelight;

import java.util.Optional;

public class NetworkHandler {
    private static final String PROTOCOL_VERSION = "1";
    public static final SimpleChannel INSTANCE = NetworkRegistry.newSimpleChannel(new ResourceLocation(GoetyDelight.MODID, "main"), () -> PROTOCOL_VERSION, PROTOCOL_VERSION::equals, PROTOCOL_VERSION::equals);
    public static void register() {
        int id = 0;
        INSTANCE.registerMessage(
                id++, ThrowSoupPacket.class, ThrowSoupPacket::encode, ThrowSoupPacket::decode, ThrowSoupPacket::handle);
        INSTANCE.registerMessage(
                id++, SyncAbilityPacket.class, SyncAbilityPacket::encode, SyncAbilityPacket::decode, SyncAbilityPacket::handle);

        INSTANCE.registerMessage(
                id++,
                SyncFoxKillCountPacket.class,
                SyncFoxKillCountPacket::encode,
                SyncFoxKillCountPacket::decode,
                SyncFoxKillCountPacket::handle,
                Optional.of(NetworkDirection.PLAY_TO_CLIENT)
        );
        INSTANCE.registerMessage(
                id++,
                SyncBackModelPacket.class,
                SyncBackModelPacket::encode,
                SyncBackModelPacket::decode,
                SyncBackModelPacket::handle,
                Optional.of(NetworkDirection.PLAY_TO_CLIENT)
        );
        INSTANCE.registerMessage(
                id++,
                CustomDollReloadMessage.class,
                CustomDollReloadMessage::encode,
                CustomDollReloadMessage::decode,
                CustomDollReloadMessage::handle,
                Optional.of(NetworkDirection.PLAY_TO_CLIENT)
        );
        INSTANCE.registerMessage(
                id++,
                SyncEntityVisualEffectsPacket.class,
                SyncEntityVisualEffectsPacket::encode,
                SyncEntityVisualEffectsPacket::decode,
                SyncEntityVisualEffectsPacket::handle,
                Optional.of(NetworkDirection.PLAY_TO_CLIENT)
        );
    }
    public static void sendToClient(SyncFoxKillCountPacket packet, ServerPlayer player) {
        INSTANCE.sendTo(packet, player.connection.connection, NetworkDirection.PLAY_TO_CLIENT);
    }

    public static void sendToServer(ThrowSoupPacket packet) {
        INSTANCE.sendToServer(packet);
    }

    public static void sendToClient(SyncBackModelPacket packet, net.minecraft.server.level.ServerPlayer player) {
        INSTANCE.sendTo(packet, player.connection.connection, NetworkDirection.PLAY_TO_CLIENT);
    }

    public static void init() {
    }

}
