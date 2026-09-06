package net.v_black_cat.goetydelight.compat.goety_revelation;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.nbt.Tag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.saveddata.SavedData;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class ApocalyptiumData extends SavedData {
    private static final String DATA_NAME = "apocalyptium_data";

    // 使用线程安全的集合
    private final Map<UUID, Long> servantExpiryMap = new ConcurrentHashMap<>();
    private final Map<UUID, Long> apollyonExpiryMap = new ConcurrentHashMap<>();
    private final Set<UUID> preventDropsList = ConcurrentHashMap.newKeySet();

    public static ApocalyptiumData get(ServerLevel level) {
        return level.getServer().overworld().getDataStorage()
                .computeIfAbsent(ApocalyptiumData::load, ApocalyptiumData::new, DATA_NAME);
    }

    public static ApocalyptiumData load(CompoundTag tag) {
        ApocalyptiumData data = new ApocalyptiumData();

        // 加载servant过期时间
        if (tag.contains("ServantExpiry", Tag.TAG_LIST)) {
            ListTag servantList = tag.getList("ServantExpiry", Tag.TAG_COMPOUND);
            for (int i = 0; i < servantList.size(); i++) {
                CompoundTag entry = servantList.getCompound(i);
                UUID uuid = entry.getUUID("UUID");
                long time = entry.getLong("Time");
                // 直接操作，因为在加载时对象还未被共享
                data.servantExpiryMap.put(uuid, time);
            }
        }

        // 加载apollyon过期时间
        if (tag.contains("ApollyonExpiry", Tag.TAG_LIST)) {
            ListTag apollyonList = tag.getList("ApollyonExpiry", Tag.TAG_COMPOUND);
            for (int i = 0; i < apollyonList.size(); i++) {
                CompoundTag entry = apollyonList.getCompound(i);
                UUID uuid = entry.getUUID("UUID");
                long time = entry.getLong("Time");
                data.apollyonExpiryMap.put(uuid, time);
            }
        }

        // 加载防止掉落列表
        if (tag.contains("PreventDrops", Tag.TAG_LIST)) {
            ListTag dropsList = tag.getList("PreventDrops", Tag.TAG_STRING);
            for (int i = 0; i < dropsList.size(); i++) {
                try {
                    UUID uuid = UUID.fromString(dropsList.getString(i));
                    data.preventDropsList.add(uuid);
                } catch (IllegalArgumentException e) {
                    // 忽略无效的UUID
                }
            }
        }

        return data;
    }

    @Override
    public CompoundTag save(CompoundTag tag) {
        // 保存servant过期时间 - 使用快照避免并发修改
        ListTag servantList = new ListTag();
        for (Map.Entry<UUID, Long> entry : new ArrayList<>(servantExpiryMap.entrySet())) {
            CompoundTag entryTag = new CompoundTag();
            entryTag.putUUID("UUID", entry.getKey());
            entryTag.putLong("Time", entry.getValue());
            servantList.add(entryTag);
        }
        tag.put("ServantExpiry", servantList);

        // 保存apollyon过期时间
        ListTag apollyonList = new ListTag();
        for (Map.Entry<UUID, Long> entry : new ArrayList<>(apollyonExpiryMap.entrySet())) {
            CompoundTag entryTag = new CompoundTag();
            entryTag.putUUID("UUID", entry.getKey());
            entryTag.putLong("Time", entry.getValue());
            apollyonList.add(entryTag);
        }
        tag.put("ApollyonExpiry", apollyonList);

        // 保存防止掉落列表
        ListTag dropsList = new ListTag();
        for (UUID uuid : new ArrayList<>(preventDropsList)) {
            dropsList.add(StringTag.valueOf(uuid.toString()));
        }
        tag.put("PreventDrops", dropsList);

        return tag;
    }

    // 线程安全的操作方法
    public void addServantExpiry(UUID uuid, long expiryTime) {
        servantExpiryMap.put(uuid, expiryTime);
        setDirty();
    }

    public void removeServantExpiry(UUID uuid) {
        servantExpiryMap.remove(uuid);
        setDirty();
    }

    public long getServantExpiry(UUID uuid) {
        return servantExpiryMap.getOrDefault(uuid, -1L);
    }

    public void addApollyonExpiry(UUID uuid, long expiryTime) {
        apollyonExpiryMap.put(uuid, expiryTime);
        setDirty();
    }

    public void removeApollyonExpiry(UUID uuid) {
        apollyonExpiryMap.remove(uuid);
        setDirty();
    }

    public long getApollyonExpiry(UUID uuid) {
        return apollyonExpiryMap.getOrDefault(uuid, -1L);
    }

    public void addPreventDrop(UUID uuid) {
        preventDropsList.add(uuid);
        setDirty();
    }

    public void removePreventDrop(UUID uuid) {
        preventDropsList.remove(uuid);
        setDirty();
    }

    public boolean shouldPreventDrop(UUID uuid) {
        return preventDropsList.contains(uuid);
    }

    public void cleanupEntity(UUID uuid) {
        boolean changed = false;
        changed |= servantExpiryMap.remove(uuid) != null;
        changed |= apollyonExpiryMap.remove(uuid) != null;
        changed |= preventDropsList.remove(uuid);

        if (changed) {
            setDirty();
        }
    }

    // 获取快照用于迭代
    public Map<UUID, Long> getServantExpirySnapshot() {
        return new HashMap<>(servantExpiryMap);
    }

    public Map<UUID, Long> getApollyonExpirySnapshot() {
        return new HashMap<>(apollyonExpiryMap);
    }

    public Set<UUID> getPreventDropsSnapshot() {
        return new HashSet<>(preventDropsList);
    }

    public boolean isEmpty() {
        return servantExpiryMap.isEmpty() && apollyonExpiryMap.isEmpty() && preventDropsList.isEmpty();
    }
}