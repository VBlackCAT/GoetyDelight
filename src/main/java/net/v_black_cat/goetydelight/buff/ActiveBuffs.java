package net.v_black_cat.goetydelight.buff;


import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.common.util.INBTSerializable;
import net.v_black_cat.goetydelight.init.ModBuffTypes;

import java.util.*;

public class ActiveBuffs implements INBTSerializable<CompoundTag> {
    private final Map<ResourceLocation, List<BuffInstance>> buffs = new HashMap<>();

    public void addBuff(ResourceLocation typeId, int duration, int amplifier) {
        BuffType type = ModBuffTypes.BUFF_REGISTRY.get(typeId);
        if (type == null) return;

        if (!type.stackable()) {
            buffs.remove(typeId);
            buffs.put(typeId, new ArrayList<>(List.of(new BuffInstance(typeId, duration, amplifier))));
        } else {
            buffs.computeIfAbsent(typeId, k -> new ArrayList<>())
                    .add(new BuffInstance(typeId, duration, amplifier));
        }
    }

    /**
     * 执行所有 Buff 的 tick，并返回本次被完全移除的 Buff 类型列表
     */
    public Set<ResourceLocation> tickAllAndGetRemoved() {
        Set<ResourceLocation> removed = new HashSet<>();
        Iterator<Map.Entry<ResourceLocation, List<BuffInstance>>> it = buffs.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry<ResourceLocation, List<BuffInstance>> entry = it.next();
            List<BuffInstance> list = entry.getValue();
            list.removeIf(inst -> {
                inst.tick();
                return inst.isExpired();
            });
            if (list.isEmpty()) {
                removed.add(entry.getKey());
                it.remove();
            }
        }
        return removed;
    }

    public void removeBuff(ResourceLocation typeId) {
        buffs.remove(typeId);
    }

    public boolean hasBuff(ResourceLocation typeId) {
        return buffs.containsKey(typeId) && !buffs.get(typeId).isEmpty();
    }

    public int getTotalAmplifier(ResourceLocation typeId) {
        List<BuffInstance> list = buffs.get(typeId);
        if (list == null || list.isEmpty()) return 0;
        return list.stream().mapToInt(BuffInstance::getAmplifier).sum();
    }
    public Set<ResourceLocation> getActiveTypes() {
        return buffs.keySet();
    }
    public List<BuffInstance> getInstances(ResourceLocation typeId) {
        return buffs.getOrDefault(typeId, Collections.emptyList());
    }


    @Override
    public CompoundTag serializeNBT(HolderLookup.Provider provider) {
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

    @Override
    public void deserializeNBT(HolderLookup.Provider provider, CompoundTag tag) {
        buffs.clear();
        ListTag listTag = tag.getList("buffs", Tag.TAG_COMPOUND);
        for (int i = 0; i < listTag.size(); i++) {
            CompoundTag instTag = listTag.getCompound(i);
            ResourceLocation typeId = ResourceLocation.parse(instTag.getString("type"));
            int duration = instTag.getInt("duration");
            int amplifier = instTag.getInt("amplifier");
            addBuff(typeId, duration, amplifier);
        }
    }
}