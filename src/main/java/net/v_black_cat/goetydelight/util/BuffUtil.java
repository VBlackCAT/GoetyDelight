package net.v_black_cat.goetydelight.util;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.resources.ResourceLocation;
import net.v_black_cat.goetydelight.buff.ActiveBuffs;
import net.v_black_cat.goetydelight.buff.effect.BuffEffect;
import net.v_black_cat.goetydelight.init.ModAttachments;
import net.v_black_cat.goetydelight.init.ModBuffTypes;

public class BuffUtil {

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
}