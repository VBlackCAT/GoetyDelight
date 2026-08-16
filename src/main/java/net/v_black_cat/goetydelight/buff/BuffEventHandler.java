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

        // tickAllAndRemove 内部已追踪是否发生变化，无需再额外做快照对比（避免每 tick 分配 HashMap）
        boolean changed = buffs.tickAllAndRemove(living);

        for (ResourceLocation typeId : buffs.getActiveTypes()) {
            BuffEffect effect = ModBuffTypes.getEffect(typeId);
            if (effect != null) {
                effect.apply(living, buffs.getTotalAmplifier(typeId));
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