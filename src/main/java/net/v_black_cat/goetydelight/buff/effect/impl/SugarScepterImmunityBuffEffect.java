package net.v_black_cat.goetydelight.buff.effect.impl;

import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.LivingEntity;
import net.neoforged.neoforge.event.entity.living.LivingIncomingDamageEvent;
import net.v_black_cat.goetydelight.buff.effect.BuffEffect;
import net.v_black_cat.goetydelight.init.ModBuffTypes;
import net.v_black_cat.goetydelight.util.BuffUtil;

public class SugarScepterImmunityBuffEffect implements BuffEffect {
    public static void onLivingIncomingDamage(LivingIncomingDamageEvent event) {
        LivingEntity entity = event.getEntity();
        if (BuffUtil.hasBuff(entity, ModBuffTypes.SUGAR_SCEPTER_IMMUNITY.getId())) {
            // 击退攻击者
            if (event.getSource().getEntity() instanceof LivingEntity attacker) {
                double dx = attacker.getX() - entity.getX();
                double dz = attacker.getZ() - entity.getZ();
                double length = Math.sqrt(dx * dx + dz * dz);
                if (length > 0) {
                    dx /= length;
                    dz /= length;
                }
                attacker.push(dx * 5.0, 0.2, dz * 5.0);
                attacker.hurtMarked = true;
            }

            // 播放音效
            entity.level().playSound(
                    null,
                    entity.getX(), entity.getY(), entity.getZ(),
                    SoundEvents.TURTLE_EGG_CRACK,
                    SoundSource.PLAYERS,
                    8.0F,
                    1F
            );

            // 取消伤害并移除 Buff
            event.setCanceled(true);
            BuffUtil.removeBuff(entity, ModBuffTypes.SUGAR_SCEPTER_IMMUNITY.getId());
        }
    }

    @Override
    public void apply(LivingEntity entity, int amplifier) {}
    @Override
    public void onApply(LivingEntity entity, int amplifier) {}
    @Override
    public void onRemove(LivingEntity entity, int amplifier) {}
}