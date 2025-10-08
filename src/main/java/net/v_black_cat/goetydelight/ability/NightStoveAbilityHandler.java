package net.v_black_cat.goetydelight.ability;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.ai.targeting.TargetingConditions;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.event.entity.EntityJoinLevelEvent;
import net.minecraftforge.event.entity.living.LivingDamageEvent;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.WeakHashMap;

@Mod.EventBusSubscriber
public class NightStoveAbilityHandler {

    @SubscribeEvent
    public static void onLivingHurt(LivingHurtEvent event) {
        LivingEntity entity = event.getEntity();

        // 检查实体是否有 NightStove 能力
        if (TimedAbilitySystem.hasAbility(entity, AbilityRegistry.NIGHT_STOVE)) {
            // 减少25%受到的伤害
            float reducedDamage = event.getAmount() * 0.75f; // 只承受75%的伤害，即减少25%
            event.setAmount(reducedDamage);
        }
    }

    @SubscribeEvent
    public static void onLivingDamage(LivingDamageEvent event) {
        LivingEntity attacker = event.getSource().getEntity() instanceof LivingEntity ?
                (LivingEntity) event.getSource().getEntity() : null;

        // 检查攻击者是否有 NightStove 能力
        if (attacker != null && TimedAbilitySystem.hasAbility(attacker, AbilityRegistry.NIGHT_STOVE)) {
            // 增加25%造成的伤害
            float increasedDamage = event.getAmount() * 1.25f; // 增加25%伤害
            event.setAmount(increasedDamage);
        }
    }

    @Mod.EventBusSubscriber(modid = "goetydelight")
    public static class NightStoveAISystem {


        private static final WeakHashMap<Mob, Boolean> modifiedUndeadMobs = new WeakHashMap<>();

        @SubscribeEvent
        public static void onEntityJoinLevel(EntityJoinLevelEvent event) {
            if (!(event.getEntity() instanceof Mob mob) || event.getLevel().isClientSide()) {
                return;
            }


            if (!(mob instanceof Monster) || mob.getType().is(net.minecraftforge.common.Tags.EntityTypes.BOSSES)) {
                return;
            }

            if (modifiedUndeadMobs.containsKey(mob)) {
                return;
            }


            mob.targetSelector.addGoal(0, new NightStoveAITargetGoal(mob));

            modifiedUndeadMobs.put(mob, true);
        }

        public static class NightStoveAITargetGoal extends NearestAttackableTargetGoal<Player> {
            public NightStoveAITargetGoal(Mob mob) {
                super(mob, Player.class, true);
                this.targetConditions = TargetingConditions.forCombat()
                        .range(this.getFollowDistance())
                        .selector(player -> {

                            return !TimedAbilitySystem.hasAbility(player, AbilityRegistry.NIGHT_STOVE);
                        });
            }

            @Override
            public boolean canUse() {

                LivingEntity lastAttacker = this.mob.getLastHurtByMob();
                if (lastAttacker instanceof Player) {
                    Player player = (Player) lastAttacker;
                    if (TimedAbilitySystem.hasAbility(player, AbilityRegistry.NIGHT_STOVE)) {

                        if (this.mob.tickCount - this.mob.getLastHurtByMobTimestamp() < 100) {
                            this.target = player;
                            return true;
                        }
                    }
                }


                if (this.mob.getTarget() != null) {

                    LivingEntity currentTarget = this.mob.getTarget();
                    if (currentTarget instanceof Player) {
                        Player player = (Player) currentTarget;

                        if (TimedAbilitySystem.hasAbility(player, AbilityRegistry.NIGHT_STOVE)) {
                            this.mob.setTarget(null);
                        }
                    }
                    return false;
                }


                this.findTarget();
                return this.target != null;
            }

            @Override
            public boolean canContinueToUse() {
                if (this.target == null) return false;


                if (this.target == this.mob.getLastHurtByMob()) {

                    return this.mob.tickCount - this.mob.getLastHurtByMobTimestamp() < 100;
                }


                return !this.target.isRemoved() &&
                        !TimedAbilitySystem.hasAbility((LivingEntity) this.target, AbilityRegistry.NIGHT_STOVE) &&
                        super.canContinueToUse();
            }


            @Override
            public void setTarget(LivingEntity target) {
                if (target instanceof Player) {
                    Player player = (Player) target;

                    if (TimedAbilitySystem.hasAbility(player, AbilityRegistry.NIGHT_STOVE)) {

                        if (this.mob.getLastHurtByMob() != player) {
                            return;
                        }
                    }
                }
                super.setTarget(target);
            }
        }
    }
}