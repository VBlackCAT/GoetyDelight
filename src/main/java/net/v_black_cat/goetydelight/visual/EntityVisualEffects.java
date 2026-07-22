package net.v_black_cat.goetydelight.visual;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

public class EntityVisualEffects {
    public static final int INFINITE = -1;
    private static final String EFFECTS = "Effects";

    private final Map<ResourceLocation, ActiveEntityVisualEffect> activeEffects = new LinkedHashMap<>();

    public void add(ResourceLocation id, int durationTicks, CompoundTag data) {
        activeEffects.put(id, new ActiveEntityVisualEffect(id, durationTicks, data));
    }

    public void add(EntityVisualEffectType type, ResourceLocation id, int durationTicks, CompoundTag data) {
        add(id, durationTicks == 0 ? type.defaultDuration() : durationTicks, data);
    }

    public boolean remove(ResourceLocation id) {
        return activeEffects.remove(id) != null;
    }

    public boolean has(ResourceLocation id) {
        return activeEffects.containsKey(id);
    }

    public ActiveEntityVisualEffect get(ResourceLocation id) {
        return activeEffects.get(id);
    }

    public boolean isEmpty() {
        return activeEffects.isEmpty();
    }

    public void clear() {
        activeEffects.clear();
    }

    public Collection<ActiveEntityVisualEffect> effects() {
        return Collections.unmodifiableCollection(activeEffects.values());
    }

    public boolean tick() {
        boolean changed = false;
        var iterator = activeEffects.values().iterator();
        while (iterator.hasNext()) {
            if (iterator.next().tick()) {
                iterator.remove();
                changed = true;
            }
        }
        return changed;
    }

    public CompoundTag serializeNBT() {
        return serializeNBT(false);
    }

    public CompoundTag serializeNBTForSync() {
        return serializeNBT(true);
    }

    private CompoundTag serializeNBT(boolean includeTransient) {
        CompoundTag tag = new CompoundTag();
        CompoundTag effectsTag = new CompoundTag();
        activeEffects.forEach((id, effect) -> {
            EntityVisualEffectType type = GDVisualEffects.get(id);
            if (type != null && (includeTransient || type.persistent() || effect.initialDuration() == INFINITE)) {
                effectsTag.put(id.toString(), effect.serializeNBT());
            }
        });
        tag.put(EFFECTS, effectsTag);
        return tag;
    }

    public void deserializeNBT(CompoundTag tag) {
        activeEffects.clear();
        if (!tag.contains(EFFECTS)) {
            return;
        }

        CompoundTag effectsTag = tag.getCompound(EFFECTS);
        for (String key : effectsTag.getAllKeys()) {
            ActiveEntityVisualEffect effect = ActiveEntityVisualEffect.deserializeNBT(effectsTag.getCompound(key));
            activeEffects.put(effect.id(), effect);
        }
    }
}
