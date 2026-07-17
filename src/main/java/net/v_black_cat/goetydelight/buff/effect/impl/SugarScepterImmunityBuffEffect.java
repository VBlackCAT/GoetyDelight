package net.v_black_cat.goetydelight.buff.effect.impl;

import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.event.entity.living.LivingAttackEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.v_black_cat.goetydelight.buff.effect.BuffEffect;
import net.v_black_cat.goetydelight.init.ModBuffTypes;
import net.v_black_cat.goetydelight.util.BuffUtil;

@Mod.EventBusSubscriber
public class SugarScepterImmunityBuffEffect implements BuffEffect {

    @SubscribeEvent
    public static void onLivingAttack(LivingAttackEvent event) {
        LivingEntity entity = event.getEntity();
        if (entity.level().isClientSide) return;

        if (BuffUtil.hasBuff(entity, ModBuffTypes.SUGAR_SCEPTER_IMMUNITY.getId())) {
            // 添加击退效果
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

            // 取消伤害事件
            event.setCanceled(true);

            // 移除免疫能力（一次性使用）
            BuffUtil.removeBuff(entity, ModBuffTypes.SUGAR_SCEPTER_IMMUNITY.getId());

            Vec3 pos = entity.position();
            entity.level().playSound(
                    null,
                    pos.x, pos.y, pos.z,
                    SoundEvents.TURTLE_EGG_CRACK,
                    SoundSource.PLAYERS,
                    8.0F,
                    1F
            );
        }
    }

    @Override
    public void apply(LivingEntity entity, int amplifier) {}

    @Override
    public void onApply(LivingEntity entity, int amplifier) {}

    @Override
    public void onRemove(LivingEntity entity, int amplifier) {}
}
