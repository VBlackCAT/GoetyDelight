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

    public static EntityVisualEffects getEffects(Entity entity) {
        return entity.getExistingDataOrNull(ModAttachments.VISUAL_EFFECTS);
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
        if (effects == null) {
            effects = new EntityVisualEffects();
            entity.setData(ModAttachments.VISUAL_EFFECTS, effects);
        }

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
        for (Entity entity : level.getAllEntities()) {
            EntityVisualEffects effects = getEffects(entity);
            if (effects != null && effects.tick()) sync(entity);
        }
    }

    public static void sync(Entity entity) {
        if (entity.level().isClientSide) return;
        AttachmentSync.syncEntityUpdate(entity, ModAttachments.VISUAL_EFFECTS.get());
    }
}
