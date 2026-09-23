package net.v_black_cat.goetydelight.compat.goetyrevelation;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.saveddata.SavedData;
import net.v_black_cat.goetydelight.api.ITimedEntityManager;
import net.v_black_cat.goetydelight.util.TimedEntityManager;

import javax.annotation.Nullable;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 亚形态使徒 / 使徒仆从的数据管理。
 * <p>
 * 本类只负责：
 * <ul>
 *     <li>运行时实体引用缓存（{@link #liveEntityCache}，不参与序列化）</li>
 *     <li>作为兼容层，把旧的静态调用转发到 {@link ITimedEntityManager}</li>
 * </ul>
 * 所有计时数据的持久化、查询、清理均委托给 {@link TimedEntityManager}。
 */
public class ApocalyptiumData extends SavedData {

    private static final String DATA_NAME = "apocalyptium_data";

    /** 运行时实体缓存，不参与序列化 */
    private final ConcurrentHashMap<UUID, LivingEntity> liveEntityCache = new ConcurrentHashMap<>();

    private static final TimedEntityManager MANAGER = TimedEntityManager.getInstance();

    // ==================== SavedData 生命周期 ====================

    public static ApocalyptiumData get(ServerLevel level) {
        return level.getServer().overworld().getDataStorage()
                .computeIfAbsent(ApocalyptiumData::load, ApocalyptiumData::new, DATA_NAME);
    }

    public static ApocalyptiumData load(CompoundTag tag) {
        return new ApocalyptiumData();
    }

    @Override
    public CompoundTag save(CompoundTag tag) {
        // 计时数据在玩家 persistentData 中，这里不需要保存任何东西
        return tag;
    }

    // ==================== 实体缓存（运行时） ====================

    public void cacheEntity(UUID uuid, LivingEntity entity) {
        liveEntityCache.put(uuid, entity);
    }

    @Nullable
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

    // ==================== 兼容层：转发到 ITimedEntityManager ====================

    // ---- 仆从过期 ----

    public static void addServantExpiry(Player player, UUID uuid, long expiryTime, String entityTypeId) {
        // 旧接口传入的是绝对时间，这里换算成相对时长
        long duration = expiryTime - player.level().getGameTime();
        if (duration < 0) duration = 0;
        MANAGER.trackByExpiry(player, uuid, expiryTime, ITimedEntityManager.Category.SERVANT, entityTypeId);
    }

    public static void removeServantExpiry(Player player, UUID uuid) {
        MANAGER.untrack(player, uuid, ITimedEntityManager.Category.SERVANT);
    }

    public static long getServantExpiry(Player player, UUID uuid) {
        return MANAGER.getExpiry(player, uuid, ITimedEntityManager.Category.SERVANT);
    }

    public static Map<UUID, CompoundTag> getServantExpirySnapshot(Player player) {
        return MANAGER.getSnapshot(player, ITimedEntityManager.Category.SERVANT);
    }

    // ---- 亚形态过期 ----

    public static void addApollyonExpiry(Player player, UUID uuid, long expiryTime, String entityTypeId) {
        MANAGER.trackByExpiry(player, uuid, expiryTime, ITimedEntityManager.Category.APOLLYON, entityTypeId);
    }

    public static void removeApollyonExpiry(Player player, UUID uuid) {
        MANAGER.untrack(player, uuid, ITimedEntityManager.Category.APOLLYON);
    }

    public static long getApollyonExpiry(Player player, UUID uuid) {
        return MANAGER.getExpiry(player, uuid, ITimedEntityManager.Category.APOLLYON);
    }

    public static Map<UUID, CompoundTag> getApollyonExpirySnapshot(Player player) {
        return MANAGER.getSnapshot(player, ITimedEntityManager.Category.APOLLYON);
    }

    // ---- 防止掉落 ----

    public static void addPreventDrop(Player player, UUID uuid) {
        MANAGER.addPreventDrop(player, uuid);
    }

    public static void removePreventDrop(Player player, UUID uuid) {
        MANAGER.removePreventDrop(player, uuid);
    }

    public static boolean shouldPreventDrop(Player player, UUID uuid) {
        return MANAGER.shouldPreventDrop(player, uuid);
    }

    public static boolean isEmpty(Player player) {
        return MANAGER.isEmpty(player);
    }

    public static void cleanupEntity(Player player, UUID uuid) {
        MANAGER.untrackAll(player, uuid);
    }

    // ==================== 反查 / 唯一性检查 ====================

    @Nullable
    public static Player findOwnerByPreventDrop(ServerLevel level, UUID entityUUID) {
        return TimedEntityManager.findOwnerByPreventDrop(level, entityUUID);
    }

    @Nullable
    public static UUID findExistingApollyonFor(Player player, long currentTime) {
        return MANAGER.findExistingTrackedFor(player, ITimedEntityManager.Category.APOLLYON, currentTime);
    }

    // ==================== 便捷：带缓存的实体获取 ====================

    /**
     * 优先从缓存拿实体，缓存没有再按 UUID 从世界找并补缓存。
     */
    @Nullable
    public LivingEntity getOrLookupEntity(ServerLevel level, UUID uuid) {
        LivingEntity cached = getCachedEntity(uuid);
        if (cached != null) return cached;

        Entity entity = level.getEntity(uuid);
        if (entity instanceof LivingEntity le) {
            cacheEntity(uuid, le);
            return le;
        }
        return null;
    }
}