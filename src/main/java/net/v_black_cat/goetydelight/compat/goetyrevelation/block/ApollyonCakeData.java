package net.v_black_cat.goetydelight.compat.goetyrevelation.block;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.saveddata.SavedData;

import java.util.*;

/**
 * 世界数据：记录所有 ApollyonCakeBlock 的位置与维度。
 * 挂载在主世界（overworld）的 DataStorage 上，跨维度共享 [citation:4]。
 */
public class ApollyonCakeData extends SavedData {

    public static final String DATA_NAME = "goetydelight_apollyon_cakes";

    /** 维度 ID -> 位置集合 */
    private final Map<ResourceLocation, Set<BlockPos>> cakes = new HashMap<>();

    public ApollyonCakeData() {}

    // ---------- 单例获取 ----------

    /**
     * 从 ServerLevel 获取数据实例（挂载在主世界）[citation:4]。
     */
    public static ApollyonCakeData get(ServerLevel level) {
        return level.getServer().overworld().getDataStorage()
                .computeIfAbsent(
                        ApollyonCakeData::load,
                        ApollyonCakeData::new,
                        DATA_NAME
                );
    }

    // ---------- 增删查 ----------

    /** 注册蛋糕位置 */
    public void addCake(ResourceKey<Level> dimension, BlockPos pos) {
        cakes.computeIfAbsent(dimension.location(), k -> new HashSet<>())
                .add(pos.immutable());
        setDirty();
    }

    /** 注销蛋糕位置 */
    public void removeCake(ResourceKey<Level> dimension, BlockPos pos) {
        Set<BlockPos> set = cakes.get(dimension.location());
        if (set != null) {
            set.remove(pos);
            if (set.isEmpty()) {
                cakes.remove(dimension.location());
            }
        }
        setDirty();
    }

    /**
     * 查询指定维度、距离 center 不超过 radius 的所有蛋糕位置。
     */
    public List<BlockPos> getCakesNear(ResourceKey<Level> dimension, BlockPos center, int radius) {
        List<BlockPos> result = new ArrayList<>();
        Set<BlockPos> set = cakes.get(dimension.location());
        if (set == null) {
            return result;
        }

        double r2 = (double) radius * radius;
        for (BlockPos pos : set) {
            if (pos.distSqr(center) <= r2) {
                result.add(pos);
            }
        }
        return result;
    }

    // ---------- 序列化 [citation:4] ----------

    @Override
    public CompoundTag save(CompoundTag tag) {
        ListTag list = new ListTag();
        for (Map.Entry<ResourceLocation, Set<BlockPos>> entry : cakes.entrySet()) {
            for (BlockPos pos : entry.getValue()) {
                CompoundTag c = new CompoundTag();
                c.putString("dim", entry.getKey().toString());
                c.putLong("pos", pos.asLong());
                list.add(c);
            }
        }
        tag.put("cakes", list);
        return tag;
    }

    public static ApollyonCakeData load(CompoundTag tag) {
        ApollyonCakeData data = new ApollyonCakeData();
        ListTag list = tag.getList("cakes", Tag.TAG_COMPOUND);
        for (int i = 0; i < list.size(); i++) {
            CompoundTag c = list.getCompound(i);
            ResourceLocation dim = ResourceLocation.tryParse(c.getString("dim"));
            if (dim == null) {
                continue;
            }
            BlockPos pos = BlockPos.of(c.getLong("pos"));
            data.cakes.computeIfAbsent(dim, k -> new HashSet<>()).add(pos);
        }
        return data;
    }
}