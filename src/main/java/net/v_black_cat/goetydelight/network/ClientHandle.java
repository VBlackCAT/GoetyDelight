package net.v_black_cat.goetydelight.network;

public class ClientHandle {
    private static int cachedFoxKillCount = 0;

    static void handleSyncFoxKillCountPacket(SyncFoxKillCountPacket packet) {
        cachedFoxKillCount = packet.foxKillCount();
    }

    public static int getCachedFoxKillCount() {
        return cachedFoxKillCount;
    }
}
