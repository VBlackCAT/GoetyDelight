package net.v_black_cat.goetydelight.api;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;

import javax.annotation.Nullable;
import java.util.Map;
import java.util.UUID;
import java.util.function.Consumer;

/**
 * 计时实体管理器接口。
 * <p>
 * 负责为指定实体绑定一个「过期时间」，并在到点后执行移除/回调操作。
 * 计时数据以玩家为单位持久化（存在玩家 persistentData 中），
 * 因此每个实体的计时都归属于某个「所有者玩家」。
 */
public interface ITimedEntityManager {

    /**
     * 计时实体的类别。不同类别使用不同的 NBT 列表键，互不干扰。
     */
    enum Category {
        /** 使徒仆从：到点直接移除实体 */
        SERVANT("ServantExpiry"),
        /** 亚形态使徒：到点执行回调（恢复形态），不直接移除 */
        APOLLYON("ApollyonExpiry");

        private final String nbtKey;

        Category(String nbtKey) {
            this.nbtKey = nbtKey;
        }

        public String getNbtKey() {
            return nbtKey;
        }
    }

    // ==================== 注册 / 取消 ====================

    /**
     * 为一个实体注册计时。
     *
     * @param owner        计时归属的玩家（用于持久化与反查）
     * @param entity       目标实体
     * @param durationTick 持续时长（tick）
     * @param category     计时类别
     * @return 该实体的 UUID
     */
    UUID track(Player owner, LivingEntity entity, long durationTick, Category category);

    /**
     * 注册计时并绑定一个「归属类型 ID」。
     */
    UUID track(Player owner, LivingEntity entity, long durationTick, Category category,
               @Nullable ResourceLocation entityTypeId);

    /**
     * 以「绝对过期时间」注册计时（用于兼容旧接口，或从外部已知过期时间戳的场景）。
     *
     * @param owner        计时归属的玩家
     * @param entityUUID   目标实体 UUID
     * @param expiryTime   绝对过期时间戳（游戏刻）
     * @param category     计时类别
     * @param entityTypeId 实体类型 ID 字符串，可为 null
     * @return 该实体的 UUID
     */
    UUID trackByExpiry(Player owner, UUID entityUUID, long expiryTime, Category category,
                       @Nullable String entityTypeId);

    /**
     * 取消一个实体的计时（不会移除实体本身）。
     */
    void untrack(Player owner, UUID entityUUID, Category category);

    /**
     * 取消某玩家下某实体的所有类别计时，并清理防掉落标记等关联数据。
     */
    void untrackAll(Player owner, UUID entityUUID);

    // ==================== 查询 ====================

    /**
     * 获取某实体的过期时间戳（游戏刻）。不存在返回 -1。
     */
    long getExpiry(Player owner, UUID entityUUID, Category category);

    /**
     * 判断某实体是否已过期。
     */
    default boolean isExpired(Player owner, UUID entityUUID, Category category, long currentTime) {
        long expiry = getExpiry(owner, entityUUID, category);
        return expiry > 0 && currentTime >= expiry;
    }

    /**
     * 获取某玩家下某类别的所有计时快照（UUID -> 附加数据）。
     */
    Map<UUID, CompoundTag> getSnapshot(Player owner, Category category);

    /**
     * 判断某玩家是否没有任何计时数据。
     */
    boolean isEmpty(Player owner);

    // ==================== 到点处理 ====================

    /**
     * 处理单个实体的过期。根据类别执行不同动作：
     * <ul>
     *     <li>SERVANT：直接 {@link LivingEntity#remove} 移除</li>
     *     <li>APOLLYON：执行 {@code onExpire} 回调（如恢复形态），然后清理计时</li>
     * </ul>
     *
     * @param onExpire 过期回调，可为 null。仅在 APOLLYON 类别下会被调用。
     * @return 是否确实处理了过期（即该实体已过期）
     */
    boolean handleExpiry(Player owner, UUID entityUUID, Category category,
                         @Nullable Consumer<LivingEntity> onExpire);

    // ==================== 实体缓存 ====================

    void cacheEntity(UUID uuid, LivingEntity entity);

    @Nullable
    LivingEntity getCachedEntity(UUID uuid);

    void removeCachedEntity(UUID uuid);
}