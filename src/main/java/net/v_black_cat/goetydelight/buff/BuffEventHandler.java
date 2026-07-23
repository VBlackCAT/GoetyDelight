package net.v_black_cat.goetydelight.buff;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;
import net.neoforged.neoforge.event.tick.EntityTickEvent;
import net.v_black_cat.goetydelight.buff.effect.BuffEffect;
import net.v_black_cat.goetydelight.init.ModAttachments;
import net.v_black_cat.goetydelight.init.ModBuffTypes;

import java.util.Set;

public class BuffEventHandler {


    // 实体 tick 事件
    public static void onEntityTick(EntityTickEvent.Post event) {
        if (!(event.getEntity() instanceof LivingEntity livingEntity)) return;

        ActiveBuffs activeBuffs = livingEntity.getData(ModAttachments.ACTIVE_BUFFS);
        if (activeBuffs == null) return;

        // 记录 tick 前的活跃类型（用于检测移除）
        Set<ResourceLocation> beforeTick = activeBuffs.getActiveTypes();

        activeBuffs.tickAllAndRemove(livingEntity);


        // 对依然活跃的类型执行每 tick 效果
        Set<ResourceLocation> afterTick = activeBuffs.getActiveTypes();
        for (ResourceLocation typeId : afterTick) {
            int totalAmplifier = activeBuffs.getTotalAmplifier(typeId);
            BuffEffect effect = ModBuffTypes.getEffect(typeId);
            if (effect != null) {
                effect.apply(livingEntity, totalAmplifier);
            }
        }
    }

}