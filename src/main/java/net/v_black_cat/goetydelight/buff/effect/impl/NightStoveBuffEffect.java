package net.v_black_cat.goetydelight.buff.effect.impl;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.NeutralMob;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.monster.Enemy;
import net.minecraftforge.event.entity.living.LivingAttackEvent;
import net.minecraftforge.event.entity.living.LivingChangeTargetEvent;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.v_black_cat.goetydelight.buff.effect.BuffEffect;
import net.v_black_cat.goetydelight.init.ModBuffTypes;
import net.v_black_cat.goetydelight.util.BuffUtil;

@Mod.EventBusSubscriber
public class NightStoveBuffEffect implements BuffEffect {

    @SubscribeEvent
    public static void onLivingHurt(LivingHurtEvent event) {
        LivingEntity victim = event.getEntity();

        // 拥有 NightStove 的实体受到伤害减少 25%
        if (BuffUtil.hasBuff(victim, ModBuffTypes.NIGHT_STOVE.getId())) {
            event.setAmount(event.getAmount() * 0.75f);
        }

        // 拥有 NightStove 的实体造成伤害增加 25%
        if (event.getSource().getEntity() instanceof LivingEntity attacker) {
            if (BuffUtil.hasBuff(attacker, ModBuffTypes.NIGHT_STOVE.getId())) {
                event.setAmount(event.getAmount() * 1.25f);
            }
        }

        // 亡灵非中立非BOSS生物无法攻击拥有 NightStove 的玩家
        if (victim instanceof Player player &&
                event.getSource().getEntity() instanceof Mob mob) {

            if (mob.getMobType() == net.minecraft.world.entity.MobType.UNDEAD &&
                    !(mob instanceof NeutralMob) &&
                    !mob.getType().is(net.minecraftforge.common.Tags.EntityTypes.BOSSES)) {

                if (BuffUtil.hasBuff(player, ModBuffTypes.NIGHT_STOVE.getId())) {
                    if (mob.getLastHurtByMob() != player) {
                        event.setCanceled(true);

                        double d0 = mob.getX() - player.getX();
                        double d1 = mob.getZ() - player.getZ();
                        mob.getNavigation().stop();
                        mob.setDeltaMovement(mob.getDeltaMovement().add(
                                Math.signum(d0) * 0.1, 0, Math.signum(d1) * 0.1
                        ));
                    }
                }
            }
        }
    }

    @SubscribeEvent
    public static void onLivingChangeTarget(LivingChangeTargetEvent event) {
        if (!(event.getEntity() instanceof Mob mob)) return;

        LivingEntity newTarget = event.getNewTarget();
        if (!(newTarget instanceof Player player)) return;

        if (!(mob instanceof Enemy) ||
                mob.getMobType() != net.minecraft.world.entity.MobType.UNDEAD ||
                mob instanceof NeutralMob ||
                mob.getType().is(net.minecraftforge.common.Tags.EntityTypes.BOSSES)) {
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
