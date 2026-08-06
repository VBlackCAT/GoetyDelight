package net.v_black_cat.goetydelight.util;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;
import net.neoforged.neoforge.attachment.AttachmentSync;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.v_black_cat.goetydelight.buff.ActiveBuffs;
import net.v_black_cat.goetydelight.buff.BuffInstance;
import net.v_black_cat.goetydelight.buff.BuffType;
import net.v_black_cat.goetydelight.buff.IBuffHolder;
import net.v_black_cat.goetydelight.buff.effect.BuffEffect;
import net.v_black_cat.goetydelight.init.ModAttachments;
import net.v_black_cat.goetydelight.init.ModBuffTypes;

import java.util.List;

public class BuffUtil {

    // ========== 公开的同步方法（供 BuffEventHandler 等调用） ==========

    public static ActiveBuffs getBuffs(LivingEntity entity) {
        ActiveBuffs buffs = ((IBuffHolder) entity).goetydelight$getActiveBuffs();
        if (buffs == null) {
            buffs = entity.getData(ModAttachments.ACTIVE_BUFFS.get());
            if (buffs == null) {
                buffs = new ActiveBuffs();
                entity.setData(ModAttachments.ACTIVE_BUFFS.get(), buffs);
            }
            ((IBuffHolder) entity).goetydelight$setActiveBuffs(buffs);
        }
        return buffs;
    }

    public static void setBuffs(LivingEntity entity, ActiveBuffs buffs) {
        ((IBuffHolder) entity).goetydelight$setActiveBuffs(buffs);
        entity.setData(ModAttachments.ACTIVE_BUFFS.get(), buffs);
    }

    // ========== 公共 API ==========

    public static void applyBuff(LivingEntity entity, DeferredHolder<BuffType, BuffType> deferredHolder, int duration, int amplifier) {
        applyBuff(entity, deferredHolder.getId(), duration, amplifier);
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

    public static void applyBuff(LivingEntity entity, ResourceLocation typeId, int duration, int amplifier) {
        ActiveBuffs buffs = getBuffs(entity);

        if (buffs.hasBuff(typeId)) {
            int oldAmplifier = buffs.getTotalAmplifier(typeId);
            BuffEffect oldEffect = ModBuffTypes.getEffect(typeId);
            if (oldEffect != null) {
                oldEffect.onRemove(entity, oldAmplifier);
            }
        }

        buffs.addBuff(typeId, duration, amplifier);

        BuffEffect newEffect = ModBuffTypes.getEffect(typeId);
        if (newEffect != null) {
            newEffect.onApply(entity, amplifier);
        }

        setBuffs(entity, buffs);
        syncToClients(entity);
    }

    public static void removeBuff(LivingEntity entity, ResourceLocation typeId) {
        ActiveBuffs buffs = getBuffs(entity);
        if (buffs == null || !buffs.hasBuff(typeId)) return;

        int amplifier = buffs.getTotalAmplifier(typeId);
        buffs.removeBuff(typeId);

        BuffEffect effect = ModBuffTypes.getEffect(typeId);
        if (effect != null) {
            effect.onRemove(entity, amplifier);
        }

        setBuffs(entity, buffs);
        syncToClients(entity);
    }

    public static boolean hasBuff(LivingEntity entity, ResourceLocation typeId) {
        ActiveBuffs buffs = ((IBuffHolder) entity).goetydelight$getActiveBuffs();
        if (buffs == null) {
            buffs = entity.getData(ModAttachments.ACTIVE_BUFFS.get());
            if (buffs == null) return false;
            ((IBuffHolder) entity).goetydelight$setActiveBuffs(buffs);
        }
        return buffs.hasBuff(typeId);
    }

    public static int getTotalAmplifier(LivingEntity entity, ResourceLocation typeId) {
        ActiveBuffs buffs = ((IBuffHolder) entity).goetydelight$getActiveBuffs();
        if (buffs == null) {
            buffs = entity.getData(ModAttachments.ACTIVE_BUFFS.get());
            if (buffs == null) return 0;
            ((IBuffHolder) entity).goetydelight$setActiveBuffs(buffs);
        }
        return buffs.getTotalAmplifier(typeId);
    }

    public static int getBuffAmplifier(LivingEntity entity, ResourceLocation buffId) {
        ActiveBuffs buffs = ((IBuffHolder) entity).goetydelight$getActiveBuffs();
        if (buffs == null) {
            buffs = entity.getData(ModAttachments.ACTIVE_BUFFS.get());
            if (buffs == null) return 0;
            ((IBuffHolder) entity).goetydelight$setActiveBuffs(buffs);
        }
        List<BuffInstance> instances = buffs.getInstances(buffId);
        if (instances.isEmpty()) return 0;
        return instances.get(instances.size() - 1).getAmplifier();
    }

    private static void syncToClients(LivingEntity entity) {
        if (entity.level().isClientSide) return;
        AttachmentSync.syncEntityUpdate(entity, ModAttachments.ACTIVE_BUFFS.get());
    }
}