package net.v_black_cat.goetydelight.util;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;
import net.minecraftforge.registries.RegistryObject;
import net.v_black_cat.goetydelight.buff.BuffSystem;
import net.v_black_cat.goetydelight.buff.BuffType;

public class BuffUtil {

    // ========== RegistryObject 重载（与 1.21.1 DeferredHolder 对齐） ==========

    public static void applyBuff(LivingEntity entity, RegistryObject<BuffType> holder, int duration, int amplifier) {
        applyBuff(entity, holder.getId(), duration, amplifier);
    }

    public static void removeBuff(LivingEntity entity, RegistryObject<BuffType> holder) {
        removeBuff(entity, holder.getId());
    }

    public static boolean hasBuff(LivingEntity entity, RegistryObject<BuffType> holder) {
        return hasBuff(entity, holder.getId());
    }

    public static int getTotalAmplifier(LivingEntity entity, RegistryObject<BuffType> holder) {
        return getTotalAmplifier(entity, holder.getId());
    }

    // ========== ResourceLocation 核心方法 ==========

    public static void applyBuff(LivingEntity entity, ResourceLocation typeId, int duration, int amplifier) {
        BuffSystem.applyBuff(entity, typeId, duration, amplifier);
    }

    public static void removeBuff(LivingEntity entity, ResourceLocation typeId) {
        BuffSystem.removeBuff(entity, typeId);
    }

    public static boolean hasBuff(LivingEntity entity, ResourceLocation typeId) {
        return BuffSystem.hasBuff(entity, typeId);
    }

    public static int getTotalAmplifier(LivingEntity entity, ResourceLocation typeId) {
        return BuffSystem.getTotalAmplifier(entity, typeId);
    }

    public static int getBuffAmplifier(LivingEntity entity, ResourceLocation typeId) {
        return BuffSystem.getBuffAmplifier(entity, typeId);
    }
}
