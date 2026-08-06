package net.v_black_cat.goetydelight.buff;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.neoforged.neoforge.attachment.AttachmentSync;
import net.neoforged.neoforge.event.tick.EntityTickEvent;
import net.v_black_cat.goetydelight.buff.effect.BuffEffect;
import net.v_black_cat.goetydelight.init.ModAttachments;
import net.v_black_cat.goetydelight.init.ModBuffTypes;
import net.v_black_cat.goetydelight.util.BuffUtil;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class BuffEventHandler {

    public static void onEntityTick(EntityTickEvent.Post event) {
        if (!(event.getEntity() instanceof LivingEntity living) || living.level().isClientSide) {
            return;
        }

        if (ActiveBuffs.ACTIVE_ENTITY_COUNT.get() == 0) {
            return;
        }

        ActiveBuffs buffs = BuffUtil.getBuffs(living);
        if (buffs == null || buffs.isEmpty()) {
            return;
        }

        Map<ResourceLocation, Integer> beforeSnapshot = new HashMap<>();
        for (ResourceLocation type : buffs.getActiveTypes()) {
            beforeSnapshot.put(type, buffs.getTotalAmplifier(type));
        }

        buffs.tickAllAndRemove(living);

        Set<ResourceLocation> afterTypes = buffs.getActiveTypes();
        for (ResourceLocation typeId : afterTypes) {
            BuffEffect effect = ModBuffTypes.getEffect(typeId);
            if (effect != null) {
                effect.apply(living, buffs.getTotalAmplifier(typeId));
            }
        }

        boolean changed = false;
        if (beforeSnapshot.size() != afterTypes.size()) {
            changed = true;
        } else {
            for (ResourceLocation type : afterTypes) {
                Integer oldAmp = beforeSnapshot.get(type);
                int newAmp = buffs.getTotalAmplifier(type);
                if (oldAmp == null || oldAmp != newAmp) {
                    changed = true;
                    break;
                }
            }
        }

        if (changed) {
            BuffUtil.setBuffs(living, buffs);
            if (living instanceof ServerPlayer) {
                AttachmentSync.syncEntityUpdate(living, ModAttachments.ACTIVE_BUFFS.get());
            }
        }
    }
}