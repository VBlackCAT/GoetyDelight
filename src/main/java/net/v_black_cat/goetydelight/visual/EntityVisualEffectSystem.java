package net.v_black_cat.goetydelight.visual;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.neoforged.neoforge.attachment.AttachmentSync;
import net.v_black_cat.goetydelight.init.ModAttachments;

public final class EntityVisualEffectSystem {
    private EntityVisualEffectSystem() {
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

    public static void tick(ServerLevel level) {
        // 【优化】空特效实体直接跳过：避免为每个无特效实体每 tick 创建迭代器/遍历空容器
        for (Entity entity : level.getAllEntities()) {
            EntityVisualEffects effects = getEffects(entity);
            if (effects != null && !effects.isEmpty() && effects.tick()) sync(entity);
        }
    }

    public static void sync(Entity entity) {
        if (entity.level().isClientSide) return;
        AttachmentSync.syncEntityUpdate(entity, ModAttachments.VISUAL_EFFECTS.get());
    }
}
