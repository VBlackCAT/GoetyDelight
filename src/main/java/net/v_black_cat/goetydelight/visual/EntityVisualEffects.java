package net.v_black_cat.goetydelight.visual;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.common.util.INBTSerializable;

import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.PriorityQueue;

public class EntityVisualEffects implements INBTSerializable<CompoundTag> {
    public static final int INFINITE = -1;
    private static final String EFFECTS = "Effects";

    private final Map<ResourceLocation, ActiveEntityVisualEffect> activeEffects = new LinkedHashMap<>();
    private final PriorityQueue<ActiveEntityVisualEffect> expirationQueue = new PriorityQueue<>(
            Comparator.comparingLong(ActiveEntityVisualEffect::expiresAtGameTime)
    );
    private long revision;

    public void add(ResourceLocation id, int durationTicks, CompoundTag data, long gameTime) {
        ActiveEntityVisualEffect effect = new ActiveEntityVisualEffect(id, durationTicks, data, gameTime);
        activeEffects.put(id, effect);
        if (effect.expiresAtGameTime() != Long.MAX_VALUE) {
            expirationQueue.add(effect);
        }
        revision++;
        compactExpirationQueueIfNeeded();
    }

    public void add(
            EntityVisualEffectType type,
            ResourceLocation id,
            int durationTicks,
            CompoundTag data,
            long gameTime
    ) {
        add(id, durationTicks == 0 ? type.defaultDuration() : durationTicks, data, gameTime);
    }

    public boolean remove(ResourceLocation id) {
        boolean removed = activeEffects.remove(id) != null;
        if (removed) {
            revision++;
            compactExpirationQueueIfNeeded();
        }
        return removed;
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
        expirationQueue.clear();
        revision++;
    }

    public Collection<ActiveEntityVisualEffect> effects() {
        return Collections.unmodifiableCollection(activeEffects.values());
    }

    public long revision() {
        return revision;
    }

    public long nextExpiration() {
        discardStaleEntries();
        return expirationQueue.isEmpty() ? Long.MAX_VALUE : expirationQueue.peek().expiresAtGameTime();
    }

    public boolean tick(long gameTime) {
        boolean changed = false;
        while (!expirationQueue.isEmpty() && expirationQueue.peek().isExpired(gameTime)) {
            ActiveEntityVisualEffect effect = expirationQueue.poll();
            if (activeEffects.get(effect.id()) == effect) {
                activeEffects.remove(effect.id());
                changed = true;
            }
        }

        if (changed) {
            revision++;
        }
        return changed;
    }

    private void discardStaleEntries() {
        while (!expirationQueue.isEmpty()) {
            ActiveEntityVisualEffect effect = expirationQueue.peek();
            if (activeEffects.get(effect.id()) == effect) {
                return;
            }
            expirationQueue.poll();
        }
    }

    private void compactExpirationQueueIfNeeded() {
        int activeInstances = 0;
        for (ActiveEntityVisualEffect effect : activeEffects.values()) {
            if (effect.expiresAtGameTime() != Long.MAX_VALUE) {
                activeInstances++;
            }
        }

        if (expirationQueue.size() <= activeInstances * 2 + 32) {
            return;
        }

        expirationQueue.clear();
        for (ActiveEntityVisualEffect effect : activeEffects.values()) {
            if (effect.expiresAtGameTime() != Long.MAX_VALUE) {
                expirationQueue.add(effect);
            }
        }
    }

    @Override
    public CompoundTag serializeNBT() {
        return serializeNBT(ActiveEntityVisualEffect.NO_GAME_TIME, false);
    }

    public CompoundTag serializeNBT(long gameTime) {
        return serializeNBT(gameTime, false);
    }

    public CompoundTag serializeNBTForSync() {
        return serializeNBT(ActiveEntityVisualEffect.NO_GAME_TIME, true);
    }

    public CompoundTag serializeNBTForSync(long gameTime) {
        return serializeNBT(gameTime, true);
    }

    private CompoundTag serializeNBT(long gameTime, boolean includeTransient) {
        CompoundTag tag = new CompoundTag();
        CompoundTag effectsTag = new CompoundTag();
        activeEffects.forEach((id, effect) -> {
            EntityVisualEffectType type = GDVisualEffects.get(id);
            if (includeTransient || type == null || type.persistent() || effect.initialDuration() == INFINITE) {
                CompoundTag effectTag = gameTime == ActiveEntityVisualEffect.NO_GAME_TIME
                        ? effect.serializeNBTStored()
                        : effect.serializeNBT(gameTime);
                effectsTag.put(id.toString(), effectTag);
            }
        });
        tag.put(EFFECTS, effectsTag);
        return tag;
    }

    @Override
    public void deserializeNBT(CompoundTag tag) {
        deserializeNBT(tag, ActiveEntityVisualEffect.NO_GAME_TIME);
    }

    public void deserializeNBT(CompoundTag tag, long gameTime) {
        activeEffects.clear();
        expirationQueue.clear();

        if (tag.contains(EFFECTS)) {
            CompoundTag effectsTag = tag.getCompound(EFFECTS);
            for (String key : effectsTag.getAllKeys()) {
                ActiveEntityVisualEffect effect = ActiveEntityVisualEffect.deserializeNBT(
                        effectsTag.getCompound(key),
                        gameTime
                );
                activeEffects.put(effect.id(), effect);
                if (effect.expiresAtGameTime() != Long.MAX_VALUE
                        && effect.expiresAtGameTime() != ActiveEntityVisualEffect.NO_GAME_TIME) {
                    expirationQueue.add(effect);
                }
            }
        }

        revision++;
    }
}
