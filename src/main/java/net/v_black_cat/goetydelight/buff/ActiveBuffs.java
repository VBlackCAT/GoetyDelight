package net.v_black_cat.goetydelight.buff;

import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;
import net.neoforged.neoforge.common.util.INBTSerializable;
import net.v_black_cat.goetydelight.buff.effect.BuffEffect;
import net.v_black_cat.goetydelight.init.ModBuffTypes;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 管理实体身上所有 Buff 实例，提供增删改查、持久化、网络同步功能。
 *
 * <p>运行时代理：直接通过 Mixin 字段访问，避免 Attachment Map 查找。 持久化：通过 INBTSerializable 配合 Attachment 系统保存/加载。
 * 全局计数器：ACTIVE_ENTITY_COUNT 用于快速判断是否有任何 Buff 存在。
 */
public class ActiveBuffs implements INBTSerializable<CompoundTag> {

    // ==================== 数据容器 ====================
    private final Map<ResourceLocation, List<BuffInstance>> buffs = new LinkedHashMap<>();
    // 放大器缓存：避免每次计算时 stream 遍历
    private final Map<ResourceLocation, Integer> amplifierCache = new HashMap<>();

    // ==================== 全局计数器 ====================
    public static final AtomicInteger ACTIVE_ENTITY_COUNT = new AtomicInteger(0);

    // ==================== 缓存更新辅助 ====================
    private void updateCache(ResourceLocation typeId) {
        List<BuffInstance> list = buffs.get(typeId);
        int total = (list == null || list.isEmpty()) ? 0 : list.stream().mapToInt(BuffInstance
                        ::getAmplifier).sum();
        if (total == 0) {
            amplifierCache.remove(typeId);
        } else {
            amplifierCache.put(typeId, total);
        }
    }

    private void rebuildCache() {
        amplifierCache.clear();
        for (ResourceLocation typeId : buffs.keySet()) {
            updateCache(typeId);
        }
    }

    // ==================== 核心 API ====================

    /** 添加 Buff 实例。若类型不可叠加，则覆盖原有实例。 当从无到有时，全局计数器增加。 */
    public void addBuff(ResourceLocation typeId, int duration, int amplifier) {
        BuffType type = ModBuffTypes.BUFF_REGISTRY.get(typeId);
        if (type == null) return;

        boolean wasEmpty = buffs.isEmpty();

        if (!type.stackable()) {
            buffs.remove(typeId);
            buffs.put(typeId, new ArrayList<>(List.of(new BuffInstance(typeId, duration, amplifier))));
        } else {
            buffs.computeIfAbsent(typeId, k -> new ArrayList<>())
                    .add(new BuffInstance(typeId, duration, amplifier));
        }

        updateCache(typeId);

        if (wasEmpty && !buffs.isEmpty()) {
            ACTIVE_ENTITY_COUNT.incrementAndGet();
        }
    }

    /** 移除指定类型的所有 Buff 实例。 如果清空则全局计数器递减。 */
    public void removeBuff(ResourceLocation typeId) {
        if (buffs.containsKey(typeId) && !buffs.get(typeId).isEmpty()) {
            buffs.remove(typeId);
            amplifierCache.remove(typeId);
            if (buffs.isEmpty()) {
                ACTIVE_ENTITY_COUNT.decrementAndGet();
            }
        }
    }

    /** 检查是否存在指定类型的 Buff。 */
    public boolean hasBuff(ResourceLocation typeId) {
        List<BuffInstance> list = buffs.get(typeId);
        return list != null && !list.isEmpty();
    }

    /** 获取指定类型所有实例的总放大器（从缓存读取，O(1)）。 */
    public int getTotalAmplifier(ResourceLocation typeId) {
        return amplifierCache.getOrDefault(typeId, 0);
    }

    /** 返回当前所有活跃的 Buff 类型集合（不可修改）。 */
    public Set<ResourceLocation> getActiveTypes() {
        return Collections.unmodifiableSet(buffs.keySet());
    }

    /** 返回指定类型的所有 Buff 实例列表（不可修改）。 */
    public List<BuffInstance> getInstances(ResourceLocation typeId) {
        return Collections.unmodifiableList(buffs.getOrDefault(typeId, Collections.emptyList()));
    }

    public boolean isEmpty() {
        return buffs.isEmpty();
    }

    /** 清除所有 Buff，并递减全局计数器。 */
    public void clear() {
        if (!buffs.isEmpty()) {
            buffs.clear();
            amplifierCache.clear();
            ACTIVE_ENTITY_COUNT.decrementAndGet();
        }
    }

    // ==================== Tick 更新 ====================
    public boolean tickAllAndRemove(LivingEntity entity) {
        boolean wasEmpty = buffs.isEmpty();
        boolean changed = false;

        Iterator<Map.Entry<ResourceLocation, List<BuffInstance>>> it = buffs.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry<ResourceLocation, List<BuffInstance>> entry = it.next();
            ResourceLocation typeId = entry.getKey();
            List<BuffInstance> list = entry.getValue();

            Iterator<BuffInstance> listIt = list.iterator();
            boolean listChanged = false;
            while (listIt.hasNext()) {
                BuffInstance inst = listIt.next();
                inst.tick();
                if (inst.isExpired()) {
                    listIt.remove();
                    changed = true;
                    listChanged = true;
                    BuffEffect effect = ModBuffTypes.getEffect(typeId);
                    if (effect != null) {
                        effect.onRemove(entity, inst.getAmplifier());
                    }
                }
            }
            if (list.isEmpty()) {
                it.remove();
                amplifierCache.remove(typeId);
                changed = true;
            } else if (listChanged) {
                // 有移除且列表未空，更新该类型的缓存
                updateCache(typeId);
            }
        }

        boolean nowEmpty = buffs.isEmpty();
        if (!wasEmpty && nowEmpty) {
            ACTIVE_ENTITY_COUNT.decrementAndGet();
        }
        return changed;
    }

    // ==================== NBT 持久化 ====================

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
        amplifierCache.clear();
        ListTag listTag = tag.getList("buffs", Tag.TAG_COMPOUND);
        for (int i = 0; i < listTag.size(); i++) {
            CompoundTag instTag = listTag.getCompound(i);
            ResourceLocation typeId = ResourceLocation.tryParse(instTag.getString("type"));
            if (typeId == null) continue;
            int duration = instTag.getInt("duration");
            int amplifier = instTag.getInt("amplifier");
            // 直接添加，但 addBuff 会更新缓存和计数器，我们希望在反序列化完后统一处理
            // 先添加到 buffs 列表（绕过 addBuff，以免计数器被错误修改）
            BuffType type = ModBuffTypes.BUFF_REGISTRY.get(typeId);
            if (type == null) continue;
            List<BuffInstance> list = buffs.computeIfAbsent(typeId, k -> new ArrayList<>());
            list.add(new BuffInstance(typeId, duration, amplifier));
        }
        // 重建缓存
        rebuildCache();
        // 如果反序列化后有数据但计数器为0，修正（因为反序列化时 addBuff 未触发递增）
        if (!buffs.isEmpty() && ACTIVE_ENTITY_COUNT.get() == 0) {
            ACTIVE_ENTITY_COUNT.incrementAndGet();
        }
    }

    /** 用于网络同步的快照，与完整 NBT 相同（但可重写以裁剪数据）。 */
    public CompoundTag serializeNBTForSync() {
        return serializeNBT(null);
    }
}