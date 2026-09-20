package net.v_black_cat.goetydelight.buff;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;
import net.v_black_cat.goetydelight.buff.effect.BuffEffect;
import net.v_black_cat.goetydelight.init.ModBuffTypes;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.Set;


public class ActiveBuffs {

    private final Map<ResourceLocation, List<BuffInstance>> buffs = new LinkedHashMap<>();
    private final Map<ResourceLocation, Integer> amplifierCache = new HashMap<>();
    private final PriorityQueue<BuffInstance> expirationQueue = new PriorityQueue<>(
            Comparator.comparingLong(BuffInstance::getExpiresAtGameTime)
    );

    // ========== Buff 管理 ==========

    public void addBuff(ResourceLocation typeId, int duration, int amplifier, long gameTime) {
        BuffType type = ModBuffTypes.BUFF_REGISTRY.get().getValue(typeId);
        if (type == null) return;

        BuffInstance instance = new BuffInstance(typeId, duration, amplifier, gameTime);
        if (!type.stackable()) {
            buffs.remove(typeId);
            buffs.put(typeId, new ArrayList<>(List.of(instance)));
        } else {
            buffs.computeIfAbsent(typeId, k -> new ArrayList<>()).add(instance);
        }

        if (instance.getExpiresAtGameTime() != Long.MAX_VALUE) {
            expirationQueue.add(instance);
        }
        updateCache(typeId);
        compactExpirationQueueIfNeeded();
    }

    public void removeBuff(ResourceLocation typeId) {
        if (buffs.remove(typeId) != null) {
            amplifierCache.remove(typeId);
            compactExpirationQueueIfNeeded();
        }
    }

    public boolean hasBuff(ResourceLocation typeId) {
        List<BuffInstance> list = buffs.get(typeId);
        return list != null && !list.isEmpty();
    }

    public int getTotalAmplifier(ResourceLocation typeId) {
        return amplifierCache.getOrDefault(typeId, 0);
    }

    public Set<ResourceLocation> getActiveTypes() {
        return Collections.unmodifiableSet(buffs.keySet());
    }

    public List<BuffInstance> getInstances(ResourceLocation typeId) {
        return Collections.unmodifiableList(buffs.getOrDefault(typeId, Collections.emptyList()));
    }

    public boolean isEmpty() {
        return buffs.isEmpty();
    }

    public void clear() {
        buffs.clear();
        amplifierCache.clear();
        expirationQueue.clear();
    }

    // ========== Tick（只处理已经到期的 Buff） ==========

    public boolean tickAllAndRemove(LivingEntity entity, long gameTime) {
        boolean changed = false;
        while (!expirationQueue.isEmpty() && expirationQueue.peek().isExpired(gameTime)) {
            BuffInstance instance = expirationQueue.poll();
            ResourceLocation typeId = instance.getTypeId();
            List<BuffInstance> list = buffs.get(typeId);

            if (list == null || !list.remove(instance)) {
                continue;
            }

            changed = true;
            if (list.isEmpty()) {
                buffs.remove(typeId);
                amplifierCache.remove(typeId);
            } else {
                updateCache(typeId);
            }

            BuffEffect effect = ModBuffTypes.getEffect(typeId);
            if (effect != null) {
                effect.onRemove(entity, instance.getAmplifier());
            }
        }
        return changed;
    }

    private void updateCache(ResourceLocation typeId) {
        List<BuffInstance> list = buffs.get(typeId);
        if (list == null || list.isEmpty()) {
            amplifierCache.remove(typeId);
            return;
        }

        int total = 0;
        for (BuffInstance instance : list) {
            total += instance.getAmplifier();
        }
        amplifierCache.put(typeId, total);
    }

    private void rebuildCache() {
        amplifierCache.clear();
        for (ResourceLocation typeId : buffs.keySet()) {
            updateCache(typeId);
        }
    }

    private void compactExpirationQueueIfNeeded() {
        int activeInstances = 0;
        for (List<BuffInstance> instances : buffs.values()) {
            activeInstances += instances.size();
        }

        if (expirationQueue.size() <= activeInstances * 2 + 32) {
            return;
        }

        expirationQueue.clear();
        for (List<BuffInstance> instances : buffs.values()) {
            for (BuffInstance instance : instances) {
                if (instance.getExpiresAtGameTime() != Long.MAX_VALUE) {
                    expirationQueue.add(instance);
                }
            }
        }
    }

    // ========== 序列化 ==========

    public CompoundTag serializeNBT(long gameTime) {
        CompoundTag tag = new CompoundTag();
        ListTag listTag = new ListTag();
        for (Map.Entry<ResourceLocation, List<BuffInstance>> entry : buffs.entrySet()) {
            for (BuffInstance inst : entry.getValue()) {
                CompoundTag instTag = new CompoundTag();
                instTag.putString("type", entry.getKey().toString());
                instTag.putInt("duration", inst.getRemainingTicks(gameTime));
                instTag.putLong("expiresAtGameTime", inst.getExpiresAtGameTime());
                instTag.putInt("amplifier", inst.getAmplifier());
                listTag.add(instTag);
            }
        }
        tag.put("buffs", listTag);
        return tag;
    }

    public void deserializeNBT(CompoundTag tag, long gameTime) {
        buffs.clear();
        amplifierCache.clear();
        expirationQueue.clear();

        ListTag listTag = tag.getList("buffs", Tag.TAG_COMPOUND);
        for (int i = 0; i < listTag.size(); i++) {
            CompoundTag instTag = listTag.getCompound(i);
            ResourceLocation typeId = ResourceLocation.tryParse(instTag.getString("type"));
            if (typeId == null) continue;

            int duration = instTag.getInt("duration");
            int amplifier = instTag.getInt("amplifier");
            BuffInstance instance = BuffInstance.deserialize(typeId, duration, amplifier, gameTime, instTag);
            buffs.computeIfAbsent(typeId, key -> new ArrayList<>()).add(instance);
            if (instance.getExpiresAtGameTime() != Long.MAX_VALUE) {
                expirationQueue.add(instance);
            }
        }
        rebuildCache();
    }

    public CompoundTag serializeNBTForSync(long gameTime) {
        return serializeNBT(gameTime);
    }
}
