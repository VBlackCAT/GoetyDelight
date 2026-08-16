package net.v_black_cat.goetydelight.buff;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;
import net.v_black_cat.goetydelight.buff.effect.BuffEffect;
import net.v_black_cat.goetydelight.init.ModBuffTypes;

import java.util.*;


public class ActiveBuffs {

    private final Map<ResourceLocation, List<BuffInstance>> buffs = new LinkedHashMap<>();

    // ========== Buff 管理 ==========

    public void addBuff(ResourceLocation typeId, int duration, int amplifier) {
        BuffType type = ModBuffTypes.BUFF_REGISTRY.get().getValue(typeId);
        if (type == null) return;

        if (!type.stackable()) {
            buffs.remove(typeId);
            buffs.put(typeId, new ArrayList<>(List.of(new BuffInstance(typeId, duration, amplifier))));
        } else {
            buffs.computeIfAbsent(typeId, k -> new ArrayList<>())
                    .add(new BuffInstance(typeId, duration, amplifier));
        }
    }

    public void removeBuff(ResourceLocation typeId) {
        buffs.remove(typeId);
    }

    public boolean hasBuff(ResourceLocation typeId) {
        List<BuffInstance> list = buffs.get(typeId);
        return list != null && !list.isEmpty();
    }

    public int getTotalAmplifier(ResourceLocation typeId) {
        List<BuffInstance> list = buffs.get(typeId);
        if (list == null || list.isEmpty()) return 0;
        int sum = 0;
        for (BuffInstance inst : list) {
            sum += inst.getAmplifier();
        }
        return sum;
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
    }

    // ========== Tick（1.21.1 模式：过期时触发 onRemove） ==========

    /**
     * 执行所有 Buff 的 tick，过期时自动触发 effect.onRemove
     * @return true 表示有变化
     */
    public boolean tickAllAndRemove(LivingEntity entity) {
        boolean changed = false;
        Iterator<Map.Entry<ResourceLocation, List<BuffInstance>>> it = buffs.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry<ResourceLocation, List<BuffInstance>> entry = it.next();
            ResourceLocation typeId = entry.getKey();
            List<BuffInstance> list = entry.getValue();

            Iterator<BuffInstance> listIt = list.iterator();
            while (listIt.hasNext()) {
                BuffInstance inst = listIt.next();
                inst.tick();
                if (inst.isExpired()) {
                    listIt.remove();
                    changed = true;
                    BuffEffect effect = ModBuffTypes.getEffect(typeId);
                    if (effect != null) {
                        effect.onRemove(entity, inst.getAmplifier());
                    }
                }
            }
            if (list.isEmpty()) {
                it.remove();
            }
        }
        return changed;
    }

    // ========== 序列化 ==========

    public CompoundTag serializeNBT() {
        CompoundTag tag = new CompoundTag();
        ListTag listTag = new ListTag();
        for (Map.Entry<ResourceLocation, List<BuffInstance>> entry : buffs.entrySet()) {
            for (BuffInstance inst : entry.getValue()) {
                CompoundTag instTag = new CompoundTag();
                instTag.putString("type", entry.getKey().toString());
                instTag.putInt("duration", inst.getDuration());
                instTag.putInt("amplifier", inst.getAmplifier());
                listTag.add(instTag);
            }
        }
        tag.put("buffs", listTag);
        return tag;
    }

    public void deserializeNBT(CompoundTag tag) {
        buffs.clear();
        ListTag listTag = tag.getList("buffs", Tag.TAG_COMPOUND);
        for (int i = 0; i < listTag.size(); i++) {
            CompoundTag instTag = listTag.getCompound(i);
            ResourceLocation typeId = ResourceLocation.tryParse(instTag.getString("type"));
            if (typeId == null) continue;
            int duration = instTag.getInt("duration");
            int amplifier = instTag.getInt("amplifier");
            addBuff(typeId, duration, amplifier);
        }
    }

    public CompoundTag serializeNBTForSync() {
        return serializeNBT();
    }
}
