package net.v_black_cat.goetydelight.compat.goety_revelation;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.nbt.Tag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.saveddata.SavedData;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class ApocalyptiumData extends SavedData {
    private static final String DATA_NAME = "apocalyptium_data";

    // 值改为 CompoundTag，内部包含 "Time"(long) 和 "EntityType"(String)
    private final Map<UUID, CompoundTag> servantExpiryMap = new ConcurrentHashMap<>();
    private final Map<UUID, CompoundTag> apollyonExpiryMap = new ConcurrentHashMap<>();
    private final Set<UUID> preventDropsList = ConcurrentHashMap.newKeySet();
    private final Map<UUID, LivingEntity> liveEntityCache = new ConcurrentHashMap<>();

    public void cacheEntity(UUID uuid, LivingEntity entity) {
        liveEntityCache.put(uuid, entity);
    }

    public LivingEntity getCachedEntity(UUID uuid) {
        LivingEntity entity = liveEntityCache.get(uuid);
        if (entity == null) return null;
        if (entity.isRemoved() || !entity.isAlive()) {
            liveEntityCache.remove(uuid);
            return null;
        }
        return entity;
    }

    public void removeCachedEntity(UUID uuid) {
        liveEntityCache.remove(uuid);
    }

    public static ApocalyptiumData get(ServerLevel level) {
        return level.getServer().overworld().getDataStorage()
                .computeIfAbsent(ApocalyptiumData::load, ApocalyptiumData::new, DATA_NAME);
    }

    public static ApocalyptiumData load(CompoundTag tag) {
        ApocalyptiumData data = new ApocalyptiumData();

        // 加载 servant 过期时间
        if (tag.contains("ServantExpiry", Tag.TAG_LIST)) {
            ListTag servantList = tag.getList("ServantExpiry", Tag.TAG_COMPOUND);
            for (int i = 0; i < servantList.size(); i++) {
                CompoundTag entry = servantList.getCompound(i);
                UUID uuid = entry.getUUID("UUID");
                CompoundTag value = new CompoundTag();
                value.putLong("Time", entry.getLong("Time"));
                value.putString("EntityType", entry.getString("EntityType"));
                data.servantExpiryMap.put(uuid, value);
            }
        }

        // 加载 apollyon 过期时间
        if (tag.contains("ApollyonExpiry", Tag.TAG_LIST)) {
            ListTag apollyonList = tag.getList("ApollyonExpiry", Tag.TAG_COMPOUND);
            for (int i = 0; i < apollyonList.size(); i++) {
                CompoundTag entry = apollyonList.getCompound(i);
                UUID uuid = entry.getUUID("UUID");
                CompoundTag value = new CompoundTag();
                value.putLong("Time", entry.getLong("Time"));
                value.putString("EntityType", entry.getString("EntityType"));
                data.apollyonExpiryMap.put(uuid, value);
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
                    // 忽略无效的 UUID
                }
            }
        }

        return data;
    }

    @Override
    public CompoundTag save(CompoundTag tag) {
        // 保存 servant 过期时间
        ListTag servantList = new ListTag();
        for (Map.Entry<UUID, CompoundTag> entry : new ArrayList<>(servantExpiryMap.entrySet())) {
            CompoundTag entryTag = new CompoundTag();
            entryTag.putUUID("UUID", entry.getKey());
            entryTag.putLong("Time", entry.getValue().getLong("Time"));
            entryTag.putString("EntityType", entry.getValue().getString("EntityType"));
            servantList.add(entryTag);
        }
        tag.put("ServantExpiry", servantList);

        // 保存 apollyon 过期时间
        ListTag apollyonList = new ListTag();
        for (Map.Entry<UUID, CompoundTag> entry : new ArrayList<>(apollyonExpiryMap.entrySet())) {
            CompoundTag entryTag = new CompoundTag();
            entryTag.putUUID("UUID", entry.getKey());
            entryTag.putLong("Time", entry.getValue().getLong("Time"));
            entryTag.putString("EntityType", entry.getValue().getString("EntityType"));
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

    // ==================== 仆从过期时间 ====================

    public void addServantExpiry(UUID uuid, long expiryTime, String entityTypeId) {
        CompoundTag value = new CompoundTag();
        value.putLong("Time", expiryTime);
        value.putString("EntityType", entityTypeId == null ? "" : entityTypeId);
        servantExpiryMap.put(uuid, value);
        setDirty();
    }

    public void addServantExpiry(UUID uuid, long expiryTime) {
        addServantExpiry(uuid, expiryTime, "");
    }

    public void removeServantExpiry(UUID uuid) {
        servantExpiryMap.remove(uuid);
        setDirty();
    }

    public long getServantExpiry(UUID uuid) {
        CompoundTag tag = servantExpiryMap.get(uuid);
        return tag == null ? -1L : tag.getLong("Time");
    }

    public String getServantEntityType(UUID uuid) {
        CompoundTag tag = servantExpiryMap.get(uuid);
        return tag == null ? "" : tag.getString("EntityType");
    }

    // ==================== 亚形态过期时间 ====================

    public void addApollyonExpiry(UUID uuid, long expiryTime, String entityTypeId) {
        CompoundTag value = new CompoundTag();
        value.putLong("Time", expiryTime);
        value.putString("EntityType", entityTypeId == null ? "" : entityTypeId);
        apollyonExpiryMap.put(uuid, value);
        setDirty();
    }

    public void addApollyonExpiry(UUID uuid, long expiryTime) {
        addApollyonExpiry(uuid, expiryTime, "");
    }

    public void removeApollyonExpiry(UUID uuid) {
        apollyonExpiryMap.remove(uuid);
        setDirty();
    }

    public long getApollyonExpiry(UUID uuid) {
        CompoundTag tag = apollyonExpiryMap.get(uuid);
        return tag == null ? -1L : tag.getLong("Time");
    }

    public String getApollyonEntityType(UUID uuid) {
        CompoundTag tag = apollyonExpiryMap.get(uuid);
        return tag == null ? "" : tag.getString("EntityType");
    }

    // ==================== 防止掉落 ====================

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

        liveEntityCache.remove(uuid); // 不 setDirty，因为它不参与序列化

        if (changed) {
            setDirty();
        }
    }

    // ==================== 快照 ====================

    public Map<UUID, CompoundTag> getServantExpirySnapshot() {
        return new HashMap<>(servantExpiryMap);
    }

    public Map<UUID, CompoundTag> getApollyonExpirySnapshot() {
        return new HashMap<>(apollyonExpiryMap);
    }

    public Set<UUID> getPreventDropsSnapshot() {
        return new HashSet<>(preventDropsList);
    }

    public boolean isEmpty() {
        return servantExpiryMap.isEmpty() && apollyonExpiryMap.isEmpty() && preventDropsList.isEmpty();
    }
}