package net.v_black_cat.goetydelight.compat.goetyrevelation;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.nbt.Tag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.saveddata.SavedData;

import javax.annotation.Nullable;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class ApocalyptiumData extends SavedData {
    private static final String DATA_NAME = "apocalyptium_data";

    // ==================== 玩家 NBT 键 ====================
    private static final String ROOT = "GoetyDelightApocalyptium";
    private static final String SERVANT_EXPIRY = "ServantExpiry";
    private static final String APOLLYON_EXPIRY = "ApollyonExpiry";
    private static final String PREVENT_DROPS = "PreventDrops";

    // 只保留运行时缓存，不参与序列化
    private final ConcurrentHashMap<UUID, LivingEntity> liveEntityCache = new ConcurrentHashMap<>();

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
        return new ApocalyptiumData();
    }

    @Override
    public CompoundTag save(CompoundTag tag) {
        return tag;
    }

    // ==================== 玩家 NBT 读写工具 ====================

    private static CompoundTag getRoot(Player player) {
        CompoundTag persistent = player.getPersistentData();
        if (!persistent.contains(ROOT, Tag.TAG_COMPOUND)) {
            persistent.put(ROOT, new CompoundTag());
        }
        return persistent.getCompound(ROOT);
    }

    private static void removeEntry(ListTag list, UUID uuid) {
        for (int i = list.size() - 1; i >= 0; i--) {
            CompoundTag entry = list.getCompound(i);
            if (entry.hasUUID("UUID") && entry.getUUID("UUID").equals(uuid)) {
                list.remove(i);
            }
        }
    }

    private static CompoundTag findEntry(ListTag list, UUID uuid) {
        for (int i = 0; i < list.size(); i++) {
            CompoundTag entry = list.getCompound(i);
            if (entry.hasUUID("UUID") && entry.getUUID("UUID").equals(uuid)) {
                return entry;
            }
        }
        return null;
    }

    // ---- 仆从过期 ----

    public static void addServantExpiry(Player player, UUID uuid, long expiryTime, String entityTypeId) {
        CompoundTag root = getRoot(player);
        ListTag list = root.getList(SERVANT_EXPIRY, Tag.TAG_COMPOUND);
        removeEntry(list, uuid);
        CompoundTag entry = new CompoundTag();
        entry.putUUID("UUID", uuid);
        entry.putLong("Time", expiryTime);
        entry.putString("EntityType", entityTypeId == null ? "" : entityTypeId);
        list.add(entry);
        root.put(SERVANT_EXPIRY, list);
    }

    public static void removeServantExpiry(Player player, UUID uuid) {
        CompoundTag root = getRoot(player);
        ListTag list = root.getList(SERVANT_EXPIRY, Tag.TAG_COMPOUND);
        removeEntry(list, uuid);
        root.put(SERVANT_EXPIRY, list);
    }

    public static long getServantExpiry(Player player, UUID uuid) {
        CompoundTag entry = findEntry(getRoot(player).getList(SERVANT_EXPIRY, Tag.TAG_COMPOUND), uuid);
        return entry == null ? -1L : entry.getLong("Time");
    }

    public static Map<UUID, CompoundTag> getServantExpirySnapshot(Player player) {
        Map<UUID, CompoundTag> map = new HashMap<>();
        ListTag list = getRoot(player).getList(SERVANT_EXPIRY, Tag.TAG_COMPOUND);
        for (int i = 0; i < list.size(); i++) {
            CompoundTag entry = list.getCompound(i);
            CompoundTag value = new CompoundTag();
            value.putLong("Time", entry.getLong("Time"));
            value.putString("EntityType", entry.getString("EntityType"));
            map.put(entry.getUUID("UUID"), value);
        }
        return map;
    }

    // ---- 亚形态过期 ----

    public static void addApollyonExpiry(Player player, UUID uuid, long expiryTime, String entityTypeId) {
        CompoundTag root = getRoot(player);
        ListTag list = root.getList(APOLLYON_EXPIRY, Tag.TAG_COMPOUND);
        removeEntry(list, uuid);
        CompoundTag entry = new CompoundTag();
        entry.putUUID("UUID", uuid);
        entry.putLong("Time", expiryTime);
        entry.putString("EntityType", entityTypeId == null ? "" : entityTypeId);
        list.add(entry);
        root.put(APOLLYON_EXPIRY, list);
    }

    public static void removeApollyonExpiry(Player player, UUID uuid) {
        CompoundTag root = getRoot(player);
        ListTag list = root.getList(APOLLYON_EXPIRY, Tag.TAG_COMPOUND);
        removeEntry(list, uuid);
        root.put(APOLLYON_EXPIRY, list);
    }

    public static long getApollyonExpiry(Player player, UUID uuid) {
        CompoundTag entry = findEntry(getRoot(player).getList(APOLLYON_EXPIRY, Tag.TAG_COMPOUND), uuid);
        return entry == null ? -1L : entry.getLong("Time");
    }

    public static Map<UUID, CompoundTag> getApollyonExpirySnapshot(Player player) {
        Map<UUID, CompoundTag> map = new HashMap<>();
        ListTag list = getRoot(player).getList(APOLLYON_EXPIRY, Tag.TAG_COMPOUND);
        for (int i = 0; i < list.size(); i++) {
            CompoundTag entry = list.getCompound(i);
            CompoundTag value = new CompoundTag();
            value.putLong("Time", entry.getLong("Time"));
            value.putString("EntityType", entry.getString("EntityType"));
            map.put(entry.getUUID("UUID"), value);
        }
        return map;
    }

    // ---- 防止掉落 ----

    public static void addPreventDrop(Player player, UUID uuid) {
        CompoundTag root = getRoot(player);
        ListTag list = root.getList(PREVENT_DROPS, Tag.TAG_STRING);
        String s = uuid.toString();
        for (int i = 0; i < list.size(); i++) {
            if (list.getString(i).equals(s)) return;
        }
        list.add(StringTag.valueOf(s));
        root.put(PREVENT_DROPS, list);
    }

    public static void removePreventDrop(Player player, UUID uuid) {
        CompoundTag root = getRoot(player);
        ListTag list = root.getList(PREVENT_DROPS, Tag.TAG_STRING);
        String s = uuid.toString();
        for (int i = list.size() - 1; i >= 0; i--) {
            if (list.getString(i).equals(s)) list.remove(i);
        }
        root.put(PREVENT_DROPS, list);
    }

    public static boolean shouldPreventDrop(Player player, UUID uuid) {
        ListTag list = getRoot(player).getList(PREVENT_DROPS, Tag.TAG_STRING);
        String s = uuid.toString();
        for (int i = 0; i < list.size(); i++) {
            if (list.getString(i).equals(s)) return true;
        }
        return false;
    }

    public static boolean isEmpty(Player player) {
        CompoundTag root = getRoot(player);
        return root.getList(SERVANT_EXPIRY, Tag.TAG_COMPOUND).isEmpty()
                && root.getList(APOLLYON_EXPIRY, Tag.TAG_COMPOUND).isEmpty()
                && root.getList(PREVENT_DROPS, Tag.TAG_STRING).isEmpty();
    }

    public static void cleanupEntity(Player player, UUID uuid) {
        removeServantExpiry(player, uuid);
        removeApollyonExpiry(player, uuid);
        removePreventDrop(player, uuid);
    }

    // ==================== 根据 UUID 反查玩家 ====================

    /**
     * 在所有在线玩家中查找哪个玩家的 PreventDrops 列表锁定了该 UUID。
     * 找到后返回该玩家，未找到返回 null。
     */
    @Nullable
    public static Player findOwnerByPreventDrop(ServerLevel level, UUID entityUUID) {
        for (Player player : level.getServer().getPlayerList().getPlayers()) {
            if (shouldPreventDrop(player, entityUUID)) {
                return player;
            }
        }
        return null;
    }

    // ==================== 检查是否已存在转化后的使徒（同一玩家唯一） ====================

    /**
     * 检查指定玩家是否已经存在一个未过期的亚形态使徒。
     * @return 已存在的那个亚形态实体的 UUID，不存在返回 null
     */
    @Nullable
    public static UUID findExistingApollyonFor(Player player, long currentTime) {
        Map<UUID, CompoundTag> snapshot = getApollyonExpirySnapshot(player);
        for (Map.Entry<UUID, CompoundTag> entry : snapshot.entrySet()) {
            long expiry = entry.getValue().getLong("Time");
            if (expiry > 0 && currentTime < expiry) {
                return entry.getKey();
            }
        }
        return null;
    }
}