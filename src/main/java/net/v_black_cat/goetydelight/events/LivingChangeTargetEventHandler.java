package net.v_black_cat.goetydelight.events;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.monster.AbstractSkeleton;
import net.minecraft.world.entity.player.Player;
import net.neoforged.neoforge.event.entity.living.LivingChangeTargetEvent;
import net.v_black_cat.goetydelight.buff.effect.impl.NightStoveBuffEffect;
import net.v_black_cat.goetydelight.visual.EntityVisualEffectSystem;
import net.v_black_cat.goetydelight.visual.GDVisualEffects;

public class LivingChangeTargetEventHandler {
    public static void onLivingChangeTarget(LivingChangeTargetEvent event) {
        NightStoveBuffEffect.onLivingChangeTarget(event);
        // 红眼开关属于客户端配置：服务端照常挂载视觉特效，是否渲染由 RedEyeFlashRenderer 决定
        if (event.getEntity() instanceof AbstractSkeleton skeleton
                && event.getNewAboutToBeSetTarget() instanceof Player player
                && player.getHealth() < 6.0F) {
            EntityVisualEffectSystem.addEffect(
                    skeleton, GDVisualEffects.RED_EYE_FLASH_KEY, 50, new CompoundTag());
        }
    }
}
