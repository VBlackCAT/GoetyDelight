package net.v_black_cat.goetydelight.util;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.attachment.AttachmentSync;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.v_black_cat.goetydelight.buff.ActiveBuffs;
import net.v_black_cat.goetydelight.buff.BuffInstance;
import net.v_black_cat.goetydelight.buff.BuffType;
import net.v_black_cat.goetydelight.buff.effect.BuffEffect;
import net.v_black_cat.goetydelight.init.ModAttachments;
import net.v_black_cat.goetydelight.init.ModBuffTypes;

import java.util.List;

public class BuffUtil {

    public static void applyBuff(LivingEntity entity, DeferredHolder <BuffType,BuffType> deferredHolder, int duration, int amplifier) {
        applyBuff(entity,deferredHolder.getId(),duration,amplifier);
    }
    public static void removeBuff(LivingEntity entity, DeferredHolder<BuffType, BuffType> deferredHolder) {
        removeBuff(entity, deferredHolder.getId());
    }

    public static boolean hasBuff(LivingEntity entity, DeferredHolder<BuffType, BuffType> deferredHolder) {
        return hasBuff(entity, deferredHolder.getId());
    }

    public static int getTotalAmplifier(LivingEntity entity, DeferredHolder<BuffType, BuffType> deferredHolder) {
        return getTotalAmplifier(entity, deferredHolder.getId());
    }
    /**
     * 为实体添加一个 Buff，并触发 onApply / onRemove 回调。
     */
    public static void applyBuff(LivingEntity entity, ResourceLocation typeId, int duration, int amplifier) {
        ActiveBuffs buffs = entity.getData(ModAttachments.ACTIVE_BUFFS);
        if (buffs == null) {
            buffs = new ActiveBuffs();
            entity.setData(ModAttachments.ACTIVE_BUFFS, buffs);
        }

        // 如果已有同类型 Buff，先触发旧 Buff 的 onRemove
        if (buffs.hasBuff(typeId)) {
            int oldAmplifier = buffs.getTotalAmplifier(typeId);
            BuffEffect oldEffect = ModBuffTypes.getEffect(typeId);
            if (oldEffect != null) {
                oldEffect.onRemove(entity, oldAmplifier);
            }
        }

        // 添加新 Buff
        buffs.addBuff(typeId, duration, amplifier);

        // 触发新 Buff 的 onApply
        BuffEffect newEffect = ModBuffTypes.getEffect(typeId);
        if (newEffect != null) {
            newEffect.onApply(entity, amplifier);
        }

        syncToClients(entity);
    }

    /**
     * 移除实体上的指定 Buff，并触发 onRemove 回调。
     */
    public static void removeBuff(LivingEntity entity, ResourceLocation typeId) {
        ActiveBuffs buffs = entity.getData(ModAttachments.ACTIVE_BUFFS);
        if (buffs == null || !buffs.hasBuff(typeId)) return;

        int amplifier = buffs.getTotalAmplifier(typeId);
        buffs.removeBuff(typeId);

        BuffEffect effect = ModBuffTypes.getEffect(typeId);
        if (effect != null) {
            effect.onRemove(entity, amplifier);
        }

        syncToClients(entity);
    }

    // 以下两个方法不变（仅查询）
    public static boolean hasBuff(LivingEntity entity, ResourceLocation typeId) {
        ActiveBuffs buffs = entity.getData(ModAttachments.ACTIVE_BUFFS);
        return buffs != null && buffs.hasBuff(typeId);
    }

    public static int getTotalAmplifier(LivingEntity entity, ResourceLocation typeId) {
        ActiveBuffs buffs = entity.getData(ModAttachments.ACTIVE_BUFFS);
        return buffs == null ? 0 : buffs.getTotalAmplifier(typeId);
    }

    /**
     * 获取指定 Buff 的 amplifier 值
     * @param entity 实体
     * @param buffId Buff 类型的 ResourceLocation
     * @return amplifier 值，如果没有该 Buff 则返回 0
     */
    public static int getBuffAmplifier(LivingEntity entity, ResourceLocation buffId) {
        ActiveBuffs activeBuffs = entity.getData(ModAttachments.ACTIVE_BUFFS);
        if (activeBuffs == null) return 0;

        List<BuffInstance> instances = activeBuffs.getInstances(buffId);
        if (instances.isEmpty()) return 0;

        // 取最后一个实例的 amplifier（通常只有一个）
        return instances.get(instances.size() - 1).getAmplifier();
    }

    /**
     * 将 ActiveBuffs 附件同步到客户端（初始跟踪时 NeoForge 会自动同步，
     * 这里负责运行时增删/过期后的即时同步）。
     */
    private static void syncToClients(LivingEntity entity) {
        if (entity.level().isClientSide) return;
        AttachmentSync.syncEntityUpdate(entity, ModAttachments.ACTIVE_BUFFS.get());
    }

}
