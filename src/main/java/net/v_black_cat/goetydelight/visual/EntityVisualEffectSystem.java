package net.v_black_cat.goetydelight.visual;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.attachment.AttachmentSync;
import net.v_black_cat.goetydelight.init.ModAttachments;

import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import java.util.PriorityQueue;

public final class EntityVisualEffectSystem {
    /**
     * 到期队列（按维度分桶）。
     * <p>
     * 演进过程：最初每 tick 对 {@code level.getAllEntities()} 全维度遍历（采样显示耗时 100% 落在
     * {@code Iterators$1.hasNext/next}）；随后改成「活跃实体索引」，空载零成本但仍要
     * <b>每个带特效的实体每 tick</b> 调一次 {@code tick()}。
     * <p>
     * 现在只为有限时长的效果登记一条到期记录，tick 时只处理已到期的条目 —— 代价与实际到期数成正比，
     * 而不是与「正在播放特效的实体数」成正比。跨维度迁移也天然没问题：条目里拿的是实体本身，
     * 到期时按实体当前所在维度的时间判断。
     */
    private static final Map<ResourceKey<Level>, PriorityQueue<Expiry>> EXPIRIES = new HashMap<>();

    /** 一条到期记录。持有实体引用，最迟到该效果到期时释放。 */
    private record Expiry(long dueTick, Entity entity, ResourceLocation effectId) {
    }

    private EntityVisualEffectSystem() {
    }

    private static PriorityQueue<Expiry> expiryQueue(ResourceKey<Level> dimension) {
        return EXPIRIES.computeIfAbsent(dimension,
                key -> new PriorityQueue<>(Comparator.comparingLong(Expiry::dueTick)));
    }

    /**
     * 获取实体的视觉特效容器。
     * <p>
     * 服务端：优先读取 mixin 接口缓存（{@link IVisualEffectHolder}），未命中时从附件惰性加载并回填缓存，
     * 之后每 tick 都是纯字段访问，避免像之前那样每 tick 都做附件 Map 查找。
     * <p>
     * 客户端：附件同步会用新对象覆盖旧对象，混入缓存会过期，因此直接读附件。
     */
    public static EntityVisualEffects getEffects(Entity entity) {
        if (entity.level().isClientSide) {
            return entity.getExistingDataOrNull(ModAttachments.VISUAL_EFFECTS);
        }
        if (entity instanceof IVisualEffectHolder holder) {
            EntityVisualEffects effects = holder.goetydelight$getVisualEffects();
            if (effects == null) {
                effects = entity.getData(ModAttachments.VISUAL_EFFECTS.get());
                holder.goetydelight$setVisualEffects(effects);
            }
            return effects;
        }
        return null;
    }

    public static boolean addEffect(Entity entity, ResourceLocation effectId, int durationTicks) {
        return addEffect(entity, effectId, durationTicks, new CompoundTag());
    }

    public static boolean addEffect(Entity entity, ResourceKey<EntityVisualEffectType> effectKey, int durationTicks) {
        return addEffect(entity, effectKey.location(), durationTicks, new CompoundTag());
    }

    public static boolean addEffect(Entity entity, ResourceKey<EntityVisualEffectType> effectKey,
                                    int durationTicks, CompoundTag data) {
        return addEffect(entity, effectKey.location(), durationTicks, data);
    }

    public static boolean addEffect(Entity entity, ResourceLocation effectId,
                                    int durationTicks, CompoundTag data) {
        EntityVisualEffectType type = GDVisualEffects.get(effectId);
        if (entity.level().isClientSide || type == null) return false;

        EntityVisualEffects effects = getEffects(entity);
        if (effects == null) return false;

        CompoundTag effectData = data.copy();
        if (!effectData.contains("StartGameTime")) {
            effectData.putLong("StartGameTime", entity.level().getGameTime());
        }
        effects.add(type, effectId, durationTicks, effectData);
        // 登记到期时间；INFINITE(-1) 或未给时长且类型默认也是无限时不登记（只能显式移除）
        ActiveEntityVisualEffect added = effects.get(effectId);
        if (added != null && added.initialDuration() > 0) {
            expiryQueue(entity.level().dimension())
                    .add(new Expiry(entity.level().getGameTime() + added.initialDuration(), entity, effectId));
        }
        sync(entity);
        return true;
    }

    public static boolean removeEffect(Entity entity, ResourceLocation effectId) {
        if (entity.level().isClientSide) return false;
        EntityVisualEffects effects = getEffects(entity);
        if (effects == null || !effects.remove(effectId)) return false;
        sync(entity);
        return true;
    }

    public static boolean removeEffect(Entity entity, ResourceKey<EntityVisualEffectType> effectKey) {
        return removeEffect(entity, effectKey.location());
    }

    public static boolean hasEffect(Entity entity, ResourceLocation effectId) {
        EntityVisualEffects effects = getEffects(entity);
        return effects != null && effects.has(effectId);
    }

    public static boolean clearEffects(Entity entity) {
        if (entity.level().isClientSide) return false;
        EntityVisualEffects effects = getEffects(entity);
        if (effects == null || effects.isEmpty()) return false;
        effects.clear();
        sync(entity);
        return true;
    }

    /**
     * 只处理「本 tick 到期」的效果（此前是每 tick 遍历所有带特效的实体）。
     * <p>
     * 队列里可能有失效条目：效果被显式移除、被重新添加（刷新）、或是被复制的实体 ——
     * 这些都在到期时按实体当前状态核对一遍再决定要不要真移除，因此不需要在增删时清理队列。
     */
    public static void tick(ServerLevel level) {
        PriorityQueue<Expiry> queue = EXPIRIES.get(level.dimension());
        if (queue == null || queue.isEmpty()) {
            return; // 本维度没有待到期效果：零成本
        }

        long gameTime = level.getGameTime();
        while (!queue.isEmpty() && queue.peek().dueTick() <= gameTime) {
            Expiry expiry = queue.poll();
            Entity entity = expiry.entity();
            if (entity.isRemoved()) {
                continue;
            }
            EntityVisualEffects effects = getEffects(entity);
            if (effects == null || !effects.has(expiry.effectId())) {
                continue; // 已被显式移除
            }
            ActiveEntityVisualEffect active = effects.get(expiry.effectId());
            long start = active.startGameTime();
            if (start >= 0 && start + active.initialDuration() > entity.level().getGameTime()) {
                continue; // 被刷新过：等新登记的那条到期记录
            }
            effects.remove(expiry.effectId());
            sync(entity);
        }

        if (queue.isEmpty()) {
            EXPIRIES.remove(level.dimension());
        }
    }

    public static void sync(Entity entity) {
        if (entity.level().isClientSide) return;
        AttachmentSync.syncEntityUpdate(entity, ModAttachments.VISUAL_EFFECTS.get());
    }
}
