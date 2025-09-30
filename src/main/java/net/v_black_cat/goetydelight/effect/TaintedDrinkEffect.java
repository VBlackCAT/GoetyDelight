package net.v_black_cat.goetydelight.effect;

import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeMap;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.event.entity.living.MobEffectEvent;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.event.entity.player.AttackEntityEvent;
import net.minecraftforge.eventbus.api.Event;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.v_black_cat.goetydelight.entities.OwnedHellfire;

import static net.minecraft.data.models.model.TextureMapping.getItemTexture;

public class TaintedDrinkEffect extends MobEffect {

    public TaintedDrinkEffect() {
        super(MobEffectCategory.BENEFICIAL, 0x8B4513);
    }

    @Override
    public void applyEffectTick(LivingEntity entity, int amplifier) {
        Level level = entity.level();

        if (level.isClientSide) {
            for (int i = 0; i < 1; i++) {
                double x = entity.getX() + (level.random.nextDouble() - 0.5) * 2.0;
                double y = entity.getY() + entity.getEyeHeight() + (level.random.nextDouble() - 0.5);
                double z = entity.getZ() + (level.random.nextDouble() - 0.5) * 2.0;
                level.addParticle(ParticleTypes.SOUL_FIRE_FLAME, x, y, z, 0, 0.05, 0);
            }
        }

        super.applyEffectTick(entity, amplifier);
    }

    @Override
    public boolean isDurationEffectTick(int duration, int amplifier) {
        return true;
    }

    @Override
    public void removeAttributeModifiers(LivingEntity entity, AttributeMap map, int amplifier) {
        super.removeAttributeModifiers(entity, map, amplifier);
    }

    @Mod.EventBusSubscriber(modid = "goetydelight")
    public static class TaintedDrinkEventHandler {

        @SubscribeEvent
        public static void onEffectApplicable(MobEffectEvent.Applicable event) {
            LivingEntity entity = event.getEntity();
            MobEffectInstance effectInstance = event.getEffectInstance();

            if (entity != null &&
                    entity.hasEffect(ModEffects.THE_PALE_MESSRNGER.get()) &&
                    effectInstance != null &&
                    effectInstance.getEffect().getCategory() == MobEffectCategory.HARMFUL) {
                event.setResult(Event.Result.DENY);
            }
        }

        @SubscribeEvent
        public static void onAttackEntity(AttackEntityEvent event) {
            Player attacker = event.getEntity();
            LivingEntity livingAttacker = attacker;
            Entity target = event.getTarget();
            if (target instanceof LivingEntity livingTarget) {
                if (livingAttacker.hasEffect(ModEffects.THE_PALE_MESSRNGER.get())) {
                    BlockPos targetPos = livingTarget.blockPosition();
                    Level level = livingTarget.level();
                    Vec3 vec3 = new Vec3(targetPos.getX() + 0.5, targetPos.getY(), targetPos.getZ() + 0.5);
                    Entity hellfire = new OwnedHellfire(level, vec3, attacker);
                    level.addFreshEntity(hellfire);
                }
            }
        }


        @SubscribeEvent
        public static void onLivingHurt(LivingHurtEvent event) {
            LivingEntity entity = event.getEntity();
            if (entity.hasEffect(ModEffects.THE_PALE_MESSRNGER.get())) {
                float originalDamage = event.getAmount();
                float reducedDamage = originalDamage * 0.5f;
                event.setAmount(reducedDamage);
            }
        }
    }
}