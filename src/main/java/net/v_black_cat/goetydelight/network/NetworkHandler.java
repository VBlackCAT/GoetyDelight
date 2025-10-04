package net.v_black_cat.goetydelight.network;

import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.simple.SimpleChannel;
import net.v_black_cat.goetydelight.GoetyDelight;

public class NetworkHandler {
    private static final String PROTOCOL_VERSION = "1";
    public static final SimpleChannel INSTANCE = NetworkRegistry.newSimpleChannel(new ResourceLocation(GoetyDelight.MODID, "main"), () -> PROTOCOL_VERSION, PROTOCOL_VERSION::equals, PROTOCOL_VERSION::equals);
    public static void register() {
        int id = 0;
        INSTANCE.registerMessage(
                id++, ThrowSoupPacket.class, ThrowSoupPacket::encode, ThrowSoupPacket::decode, ThrowSoupPacket::handle);
        INSTANCE.registerMessage(
                id++, SyncAbilityPacket.class, SyncAbilityPacket::encode, SyncAbilityPacket::decode, SyncAbilityPacket::handle);

    }
    public static void sendToServer(ThrowSoupPacket packet) {
        INSTANCE.sendToServer(packet);
    }


}