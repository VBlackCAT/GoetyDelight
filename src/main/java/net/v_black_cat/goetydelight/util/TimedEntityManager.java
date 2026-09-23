package net.v_black_cat.goetydelight.util;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.v_black_cat.goetydelight.api.ITimedEntityManager;

import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;

/**
 * {@link ITimedEntityManager} 的默认实现。
 * <p>
 * 计时数据持久化在玩家的 persistentData 中：
 * <pre>
 * GoetyDelightApocalyptium
 *   ├── ServantExpiry : ListTag[Compound{UUID, Time, EntityType}]
 *   ├── ApollyonExpiry: ListTag[Compound{UUID, Time, EntityType}]
 *   └── PreventDrops  : ListTag[String(UUID)]
 * </pre>
 */
public class TimedEntityManager implements ITimedEntityManager {

    private static final String ROOT = "GoetyDelightApocalyptium";
    private static final String PREVENT_DROPS = "PreventDrops";

    /** 全局单例 */
    private static final TimedEntityManager INSTANCE = new TimedEntityManager();

    public static TimedEntityManager getInstance() {
        return INSTANCE;
    }

    // 运行时缓存，不参与序列化
    private final ConcurrentHashMap<UUID, LivingEntity> liveEntityCache = new ConcurrentHashMap<>();

    private TimedEntityManager() {
    }

    // ==================== 注册 / 取消 ====================

    @Override
    public UUID track(Player owner, LivingEntity entity, long durationTick, Category category) {
        return track(owner, entity, durationTick, category, null);
    }

    @Override
    public UUID track(Player owner, LivingEntity entity, long durationTick, Category category,
                      @Nullable ResourceLocation entityTypeId) {
        if (!(entity.level() instanceof ServerLevel serverLevel)) {
            throw new IllegalStateException("TimedEntityManager 只能在服务端使用");
        }

        UUID uuid = entity.getUUID();
        long expiry = serverLevel.getGameTime() + durationTick;
        String typeId = entityTypeId == null ? "" : entityTypeId.toString();

        writeEntry(owner, uuid, expiry, category, typeId);
        addPreventDrop(owner, uuid);
        cacheEntity(uuid, entity);

        return uuid;
    }

    @Override
    public UUID trackByExpiry(Player owner, UUID entityUUID, long expiryTime, Category category,
                              @Nullable String entityTypeId) {
        String typeId = entityTypeId == null ? "" : entityTypeId;
        writeEntry(owner, entityUUID, expiryTime, category, typeId);
        addPreventDrop(owner, entityUUID);

        // 尝试从世界补缓存
        if (owner.level() instanceof ServerLevel serverLevel) {
            Entity e = serverLevel.getEntity(entityUUID);
            if (e instanceof LivingEntity le) {
                cacheEntity(entityUUID, le);
            }
        }
        return entityUUID;
    }

    /** 内部：写入一条计时条目 */
    private void writeEntry(Player owner, UUID uuid, long expiryTime, Category category, String typeId) {
        CompoundTag root = getRoot(owner);
        ListTag list = root.getList(category.getNbtKey(), Tag.TAG_COMPOUND);
        removeEntry(list, uuid);

        CompoundTag entry = new CompoundTag();
        entry.putUUID("UUID", uuid);
        entry.putLong("Time", expiryTime);
        entry.putString("EntityType", typeId);
        list.add(entry);
        root.put(category.getNbtKey(), list);
    }

    @Override
    public void untrack(Player owner, UUID entityUUID, Category category) {
        CompoundTag root = getRoot(owner);
        ListTag list = root.getList(category.getNbtKey(), Tag.TAG_COMPOUND);
        removeEntry(list, entityUUID);
        root.put(category.getNbtKey(), list);
    }

    @Override
    public void untrackAll(Player owner, UUID entityUUID) {
        untrack(owner, entityUUID, Category.SERVANT);
        untrack(owner, entityUUID, Category.APOLLYON);
        removePreventDrop(owner, entityUUID);
        removeCachedEntity(entityUUID);
    }

    // ==================== 查询 ====================

    @Override
    public long getExpiry(Player owner, UUID entityUUID, Category category) {
        CompoundTag entry = findEntry(getRoot(owner).getList(category.getNbtKey(), Tag.TAG_COMPOUND), entityUUID);
        return entry == null ? -1L : entry.getLong("Time");
    }

    @Override
    public Map<UUID, CompoundTag> getSnapshot(Player owner, Category category) {
        Map<UUID, CompoundTag> map = new HashMap<>();
        ListTag list = getRoot(owner).getList(category.getNbtKey(), Tag.TAG_COMPOUND);
        for (int i = 0; i < list.size(); i++) {
            CompoundTag entry = list.getCompound(i);
            CompoundTag value = new CompoundTag();
            value.putLong("Time", entry.getLong("Time"));
            value.putString("EntityType", entry.getString("EntityType"));
            map.put(entry.getUUID("UUID"), value);
        }
        return map;
    }

    @Override
    public boolean isEmpty(Player owner) {
        CompoundTag root = getRoot(owner);
        return root.getList(Category.SERVANT.getNbtKey(), Tag.TAG_COMPOUND).isEmpty()
                && root.getList(Category.APOLLYON.getNbtKey(), Tag.TAG_COMPOUND).isEmpty()
                && root.getList(PREVENT_DROPS, Tag.TAG_STRING).isEmpty();
    }

    // ==================== 到点处理 ====================

    @Override
    public boolean handleExpiry(Player owner, UUID entityUUID, Category category,
                                @Nullable Consumer<LivingEntity> onExpire) {
        if (!(owner.level() instanceof ServerLevel serverLevel)) return false;

        long currentTime = serverLevel.getGameTime();
        long expiry = getExpiry(owner, entityUUID, category);
        if (expiry <= 0 || currentTime < expiry) return false;

        LivingEntity living = getCachedEntity(entityUUID);
        if (living == null) {
            Entity e = serverLevel.getEntity(entityUUID);
            if (e instanceof LivingEntity le) {
                living = le;
                cacheEntity(entityUUID, le);
            }
        }

        if (living != null) {
            if (category == Category.SERVANT) {
                living.remove(Entity.RemovalReason.DISCARDED);
            } else if (category == Category.APOLLYON && onExpire != null) {
                onExpire.accept(living);
            }
        }

        untrack(owner, entityUUID, category);
        removeCachedEntity(entityUUID);
        return true;
    }

    // ==================== 实体缓存 ====================

    @Override
    public void cacheEntity(UUID uuid, LivingEntity entity) {
        liveEntityCache.put(uuid, entity);
    }

    @Override
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

    @Override
    public void removeCachedEntity(UUID uuid) {
        liveEntityCache.remove(uuid);
    }

    // ==================== 防掉落 ====================

    public void addPreventDrop(Player player, UUID uuid) {
        CompoundTag root = getRoot(player);
        ListTag list = root.getList(PREVENT_DROPS, Tag.TAG_STRING);
        String s = uuid.toString();
        for (int i = 0; i < list.size(); i++) {
            if (list.getString(i).equals(s)) return;
        }
        list.add(StringTag.valueOf(s));
        root.put(PREVENT_DROPS, list);
    }

    public void removePreventDrop(Player player, UUID uuid) {
        CompoundTag root = getRoot(player);
        ListTag list = root.getList(PREVENT_DROPS, Tag.TAG_STRING);
        String s = uuid.toString();
        for (int i = list.size() - 1; i >= 0; i--) {
            if (list.getString(i).equals(s)) list.remove(i);
        }
        root.put(PREVENT_DROPS, list);
    }

    public boolean shouldPreventDrop(Player player, UUID uuid) {
        ListTag list = getRoot(player).getList(PREVENT_DROPS, Tag.TAG_STRING);
        String s = uuid.toString();
        for (int i = 0; i < list.size(); i++) {
            if (list.getString(i).equals(s)) return true;
        }
        return false;
    }

    @Nullable
    public static Player findOwnerByPreventDrop(ServerLevel level, UUID entityUUID) {
        for (Player player : level.getServer().getPlayerList().getPlayers()) {
            if (getInstance().shouldPreventDrop(player, entityUUID)) {
                return player;
            }
        }
        return null;
    }

    @Nullable
    public UUID findExistingTrackedFor(Player player, Category category, long currentTime) {
        Map<UUID, CompoundTag> snapshot = getSnapshot(player, category);
        for (Map.Entry<UUID, CompoundTag> entry : snapshot.entrySet()) {
            long expiry = entry.getValue().getLong("Time");
            if (expiry > 0 && currentTime < expiry) {
                return entry.getKey();
            }
        }
        return null;
    }

    // ==================== NBT 工具 ====================

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

    @Nullable
    private static CompoundTag findEntry(ListTag list, UUID uuid) {
        for (int i = 0; i < list.size(); i++) {
            CompoundTag entry = list.getCompound(i);
            if (entry.hasUUID("UUID") && entry.getUUID("UUID").equals(uuid)) {
                return entry;
            }
        }
        return null;
    }
}