package net.v_black_cat.goetydelight.effect;

import com.Polarice3.Goety.common.entities.projectiles.Hellfire;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.event.entity.living.LivingDamageEvent;
import net.neoforged.neoforge.event.entity.living.MobEffectEvent;
import net.neoforged.neoforge.event.entity.player.AttackEntityEvent;
import net.v_black_cat.goetydelight.init.ModEffects;

public class TaintedDrinkEffect extends MobEffect {
    public TaintedDrinkEffect() {
        super(MobEffectCategory.BENEFICIAL, 0x8B4513);
    }

    @Override
    public boolean applyEffectTick(LivingEntity entity, int amplifier) {
        Level level = entity.level();
        if (level.isClientSide) {
            for (int i = 0; i < 1; i++) {
                double x = entity.getX() + (level.random.nextDouble() - 0.5) * 2.0;
                double y = entity.getY() + entity.getEyeHeight() + (level.random.nextDouble() - 0.5);
                double z = entity.getZ() + (level.random.nextDouble() - 0.5) * 2.0;
                level.addParticle(ParticleTypes.SOUL_FIRE_FLAME, x, y, z, 0, 0.05, 0);
            }
        }
        return true;
    }

    @Override
    public boolean shouldApplyEffectTickThisTick(int duration, int amplifier) {
        return true;
    }

    public static void onEffectApplicable(MobEffectEvent.Applicable event) {
        LivingEntity entity = event.getEntity();
        MobEffectInstance effectInstance = event.getEffectInstance();

        if (entity != null &&
                entity.hasEffect(ModEffects.THE_PALE_MESSENGER) &&
                effectInstance != null &&
                effectInstance.getEffect().value().getCategory()  == MobEffectCategory.HARMFUL) {
            event.setResult(MobEffectEvent.Applicable.Result.DO_NOT_APPLY);
        }
    }

    public static void onAttackEntity(AttackEntityEvent event) {
        Player attacker = event.getEntity();
        LivingEntity livingAttacker = attacker;
        Entity target = event.getTarget();
        if (target instanceof LivingEntity livingTarget) {
            if (livingAttacker.hasEffect(ModEffects.THE_PALE_MESSENGER)) {
                BlockPos targetPos = livingTarget.blockPosition();
                Level level = livingTarget.level();
                Vec3 vec3 = new Vec3(targetPos.getX() + 0.5, targetPos.getY(), targetPos.getZ() + 0.5);
                Entity hellfire = new Hellfire(level, vec3, attacker);
                level.addFreshEntity(hellfire);
            }
        }
    }

    public static void onLivingHurt(LivingDamageEvent.Pre event) {
        LivingEntity entity = event.getEntity();
        if (entity.hasEffect(ModEffects.THE_PALE_MESSENGER)) {
            float originalDamage = event.getOriginalDamage();
            float reducedDamage = originalDamage * 0.5f;
            event.setNewDamage(reducedDamage);
        }
    }
}