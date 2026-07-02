package net.v_black_cat.goetydelight.network;

import net.minecraft.client.Minecraft;
import net.minecraft.nbt.ListTag;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
//import net.v_black_cat.goetydelight.capability.CustomerOrderItemProvider;

import java.util.ArrayList;
import java.util.List;

public class ClientHandle {
    private static int cachedFoxKillCount = 0;

    static void handleSyncFoxKillCountPacket(SyncFoxKillCountPacket packet) {
        cachedFoxKillCount = packet.foxKillCount();
    }

    public static int getCachedFoxKillCount() {
        return cachedFoxKillCount;
    }
}
