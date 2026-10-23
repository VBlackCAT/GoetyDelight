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
    // 语义变更：存"上次实际写入 Modifier 的糖量" [hp, atk, spd, res]，用于剥离
    // （不再是"糖的原始总加成"——原始总加成由 computeCandyFromConsumed 从 consumedMap 现算）
    public final Map<UUID, double[]> bonusMap = new HashMap<>();
    // 纯内存防重入标记，不持久化（重启清空无影响）
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

    /** 返回"上次写入 Modifier 的糖量"，用于剥离。无记录则为 0。 */
    public double[] getCandyBonus(UUID id) {
        return bonusMap.getOrDefault(id, new double[]{0, 0, 0, 0});
    }

    /** 覆盖写入"本次实际写入 Modifier 的糖量"（不再累加）。 */
    public void setCandyBonus(UUID id, double hp, double atk, double spd, double res) {
        bonusMap.put(id, new double[]{hp, atk, spd, res});
        this.setDirty();
    }

    /**
     * 从 consumedMap 现算"糖的原始总加成"，按 rarity ordinal 累加。
     * 与 CrystalCandyItem.applyCrystalCandyBonuses 的 switch 保持一致。
     */
    public double[] computeCandyFromConsumed(UUID id) {
        double hp = 0, atk = 0, spd = 0, res = 0;
        for (ConsumedCurio cc : getConsumed(id)) {
            switch (cc.rarityOrdinal) {
                case 0 -> { hp += 0.01; atk += 0.01; }                                  // COMMON
                case 1 -> { hp += 0.02; atk += 0.02; spd += 0.02; }                     // UNCOMMON
                case 2 -> { hp += 0.04; atk += 0.04; spd += 0.04; res += 0.04; }        // RARE
                case 3 -> { hp += 0.24; atk += 0.24; spd += 0.24; res += 0.24; }        // EPIC
                default -> {}
            }
        }
        return new double[]{hp, atk, spd, res};
    }

    // ======= 饰品记录方法 =======
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