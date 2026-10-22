package net.v_black_cat.goetydelight.compat.goetyrevelation.util;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.level.saveddata.SavedData;
import org.jetbrains.annotations.NotNull;

import java.util.*;

public class CrystalCandySavedData extends SavedData {

    public static final String DATA_NAME = "goetydelight_crystal_candy";

    public final Map<UUID, List<ConsumedCurio>> consumedMap = new HashMap<>();
    // 存糖的加成 [hp, atk, spd, res]
    public final Map<UUID, double[]> bonusMap = new HashMap<>();
    // 布尔标记：当内部（吃糖/登录）刷新属性时设为 true，防止 Mixin 重复叠加
    public final Set<UUID> isRefreshingSet = new HashSet<>();

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

            if (pt.contains("CandyBonus")) {
                CompoundTag b = pt.getCompound("CandyBonus");
                data.bonusMap.put(id, new double[]{
                        b.getDouble("hp"), b.getDouble("atk"),
                        b.getDouble("spd"), b.getDouble("res")
                });
            }
        }
        return data;
    }

    @Override
    public @NotNull CompoundTag save(@NotNull CompoundTag tag) {
        ListTag players = new ListTag();
        Set<UUID> all = new LinkedHashSet<>(consumedMap.keySet());
        all.addAll(bonusMap.keySet());

        for (UUID id : all) {
            CompoundTag pt = new CompoundTag();
            pt.putUUID("PlayerID", id);

            ListTag curios = new ListTag();
            for (ConsumedCurio cc : consumedMap.getOrDefault(id, Collections.emptyList())) {
                curios.add(cc.toTag());
            }
            pt.put("ConsumedCurios", curios);

            double[] b = bonusMap.get(id);
            if (b != null) {
                CompoundTag bt = new CompoundTag();
                bt.putDouble("hp", b[0]); bt.putDouble("atk", b[1]);
                bt.putDouble("spd", b[2]); bt.putDouble("res", b[3]);
                pt.put("CandyBonus", bt);
            }
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

    public double[] getCandyBonus(UUID id) {
        return bonusMap.getOrDefault(id, new double[]{0, 0, 0, 0});
    }

    public void addCandyBonus(UUID id, double hp, double atk, double spd, double res) {
        double[] b = bonusMap.computeIfAbsent(id, k -> new double[]{0, 0, 0, 0});
        b[0] += hp; b[1] += atk; b[2] += spd; b[3] += res;
        this.setDirty();
    }

    // ======= 补充丢失的饰品记录方法 =======
    public List<ConsumedCurio> getConsumed(UUID playerId) {
        return consumedMap.getOrDefault(playerId, Collections.emptyList());
    }

    public void addConsumed(UUID playerId, ConsumedCurio cc) {
        consumedMap.computeIfAbsent(playerId, k -> new ArrayList<>()).add(cc);
        this.setDirty();
    }

    public boolean hasConsumedItem(UUID playerId, String itemId) {
        for (ConsumedCurio cc : getConsumed(playerId)) {
            if (cc.itemId != null && cc.itemId.toString().equals(itemId)) return true;
        }
        return false;
    }
}