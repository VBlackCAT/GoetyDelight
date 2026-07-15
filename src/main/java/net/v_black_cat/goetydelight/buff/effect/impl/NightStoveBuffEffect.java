package net.v_black_cat.goetydelight.buff.effect.impl;

import net.minecraft.tags.EntityTypeTags;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.NeutralMob;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.neoforged.neoforge.common.Tags;
import net.neoforged.neoforge.event.entity.living.LivingChangeTargetEvent;
import net.neoforged.neoforge.event.entity.living.LivingIncomingDamageEvent;
import net.v_black_cat.goetydelight.buff.effect.BuffEffect;
import net.v_black_cat.goetydelight.init.ModBuffTypes;
import net.v_black_cat.goetydelight.util.BuffUtil;

public class NightStoveBuffEffect implements BuffEffect {
    public static void onLivingIncomingDamage(LivingIncomingDamageEvent event) {
        LivingEntity victim = event.getEntity();


        if (BuffUtil.hasBuff(victim, ModBuffTypes.NIGHT_STOVE.getId())) {
            event.setAmount(event.getAmount() * 0.75f);
        }


        if (event.getSource().getEntity() instanceof LivingEntity attacker) {
            if (BuffUtil.hasBuff(attacker, ModBuffTypes.NIGHT_STOVE.getId())) {
                event.setAmount(event.getAmount() * 1.25f);
            }
        }


        if (victim instanceof Player player &&
                event.getSource().getEntity() instanceof Mob mob) {


            if (mob.getType().is(EntityTypeTags.UNDEAD)
                    && !(mob instanceof NeutralMob)
                    && !mob.getType().is(Tags.EntityTypes.BOSSES)) {

                if (BuffUtil.hasBuff(player, ModBuffTypes.NIGHT_STOVE.getId())) {
                    if (mob.getLastHurtByMob() != player) {
                        event.setCanceled(true);
                        mob.getNavigation().stop();
                        double dx = mob.getX() - player.getX();
                        double dz = mob.getZ() - player.getZ();
                        mob.setDeltaMovement(
                                mob.getDeltaMovement().add(Math.signum(dx) * 0.1, 0, Math.signum(dz) * 0.1)
                        );
                    }
                }
            }
        }
    }

    public static void onLivingChangeTarget(LivingChangeTargetEvent event) {
        if (!(event.getEntity() instanceof Mob mob)) return;

        LivingEntity newTarget = event.getNewAboutToBeSetTarget();
        if (!(newTarget instanceof Player player)) return;


        if (!(mob instanceof Monster)
                || !mob.getType().is(EntityTypeTags.UNDEAD)
                || mob instanceof NeutralMob
                || mob.getType().is(Tags.EntityTypes.BOSSES)) {
            return;
        }

        if (BuffUtil.hasBuff(player, ModBuffTypes.NIGHT_STOVE.getId())) {
            if (mob.getLastHurtByMob() != player) {
                event.setCanceled(true);
            }
        }
    }

    @Override
    public void apply(LivingEntity entity, int amplifier) {}
    @Override
    public void onApply(LivingEntity entity, int amplifier) {}
    @Override
    public void onRemove(LivingEntity entity, int amplifier) {}
}