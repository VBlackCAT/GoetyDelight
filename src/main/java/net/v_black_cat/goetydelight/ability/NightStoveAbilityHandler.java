package net.v_black_cat.goetydelight.ability;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MobType;
import net.minecraft.world.entity.NeutralMob;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.event.entity.living.LivingChangeTargetEvent;
import net.minecraftforge.event.entity.living.LivingDamageEvent;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = "goetydelight")
public class NightStoveAbilityHandler {

    @SubscribeEvent
    public static void onLivingHurt(LivingHurtEvent event) {
        LivingEntity entity = event.getEntity();

        if (TimedAbilitySystem.hasAbility(entity, AbilityRegistry.NIGHT_STOVE)) {
            float reducedDamage = event.getAmount() * 0.75f;
            event.setAmount(reducedDamage);
        }
    }

    @SubscribeEvent
    public static void onLivingDamage(LivingDamageEvent event) {
        LivingEntity attacker = event.getSource().getEntity() instanceof LivingEntity ?
                (LivingEntity) event.getSource().getEntity() : null;

        if (attacker != null && TimedAbilitySystem.hasAbility(attacker, AbilityRegistry.NIGHT_STOVE)) {
            float increasedDamage = event.getAmount() * 1.25f;
            event.setAmount(increasedDamage);
        }
    }

    @SubscribeEvent
    public static void onLivingChangeTarget(LivingChangeTargetEvent event) {
        LivingEntity attacker = event.getEntity();
        LivingEntity target = event.getNewTarget();

        
        if (!(attacker instanceof Mob) || !(target instanceof Player)) {
            return;
        }

        Mob mob = (Mob) attacker;
        Player player = (Player) target;

        
        if (!(mob instanceof Monster) ||
                mob.getType().is(net.minecraftforge.common.Tags.EntityTypes.BOSSES) ||
                mob instanceof NeutralMob ||
                mob.getMobType() != MobType.UNDEAD) {
            return;
        }

        
        if (TimedAbilitySystem.hasAbility(player, AbilityRegistry.NIGHT_STOVE)) {
            
            
            if (mob.getLastHurtByMob() != player) {
                if (event.getTargetType() == LivingChangeTargetEvent.LivingTargetType.MOB_TARGET) {
                    event.setNewTarget(null);
                } else {
                    event.setCanceled(true);
                }
            }
            
        }
    }

    @SubscribeEvent
    public static void onLivingAttack(net.minecraftforge.event.entity.living.LivingAttackEvent event) {
        if (event.getEntity() instanceof Player player) {
            LivingEntity attacker = event.getSource().getEntity() instanceof LivingEntity ?
                    (LivingEntity) event.getSource().getEntity() : null;

            if (attacker instanceof Mob mob) {
                
                if (mob.getMobType() == MobType.UNDEAD &&
                        !(mob instanceof NeutralMob) &&
                        !mob.getType().is(net.minecraftforge.common.Tags.EntityTypes.BOSSES)) {

                    
                    if (TimedAbilitySystem.hasAbility(player, AbilityRegistry.NIGHT_STOVE)) {
                        
                        
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
    }
}