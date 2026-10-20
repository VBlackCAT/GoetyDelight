package net.v_black_cat.goetydelight.compat.goetyrevelation.util;

import com.mojang.logging.LogUtils;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.level.saveddata.SavedData;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;

import java.util.*;

public class CrystalCandySavedData extends SavedData {

    public static final String DATA_NAME = "goetydelight_crystal_candy";

    public final Map<UUID, List<ConsumedCurio>> consumedMap = new HashMap<>();

    public static CrystalCandySavedData create(CompoundTag tag) {
        CrystalCandySavedData data = new CrystalCandySavedData();
        ListTag players = tag.getList("Players", 10);
        for (int i = 0; i < players.size(); i++) {
            CompoundTag pt = players.getCompound(i);
            UUID id = pt.getUUID("PlayerID");
            ListTag curios = pt.getList("ConsumedCurios", 10);
            List<ConsumedCurio> list = new ArrayList<>(curios.size());
            for (int j = 0; j < curios.size(); j++) {
                list.add(ConsumedCurio.fromTag(curios.getCompound(j)));
            }
            data.consumedMap.put(id, list);
        }
        return data;
    }

    @Override
    public @NotNull CompoundTag save(@NotNull CompoundTag tag) {
        ListTag players = new ListTag();
        for (Map.Entry<UUID, List<ConsumedCurio>> e : consumedMap.entrySet()) {
            CompoundTag pt = new CompoundTag();
            pt.putUUID("PlayerID", e.getKey());
            ListTag curios = new ListTag();
            for (ConsumedCurio cc : e.getValue()) {
                curios.add(cc.toTag());
            }
            pt.put("ConsumedCurios", curios);
            players.add(pt);
        }
        tag.put("Players", players);
        return tag;
    }

    public static CrystalCandySavedData readOrCreate(MinecraftServer server) {
        return server.overworld().getDataStorage().computeIfAbsent(
                CrystalCandySavedData::create,
                CrystalCandySavedData::new,
                DATA_NAME);
    }

    public List<ConsumedCurio> getConsumed(UUID playerId) {
        return consumedMap.getOrDefault(playerId, Collections.emptyList());
    }

    public void addConsumed(UUID playerId, ConsumedCurio cc) {
        consumedMap.computeIfAbsent(playerId, k -> new ArrayList<>()).add(cc);
        this.setDirty();
    }

    public boolean hasConsumedItem(UUID playerId, String itemId) {
        List<ConsumedCurio> list = getConsumed(playerId);
        for (ConsumedCurio cc : list) {
            if (cc.itemId != null && cc.itemId.toString().equals(itemId)) {
                return true;
            }
        }
        return false;
    }

    public int[] countRarities(UUID playerId) {
        int[] counts = new int[4];
        for (ConsumedCurio cc : getConsumed(playerId)) {
            if (cc.rarityOrdinal >= 0 && cc.rarityOrdinal < 4) counts[cc.rarityOrdinal]++;
        }
        return counts;
    }
}