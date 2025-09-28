package net.v_black_cat.goetydelight.effect;

import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeMap;
import net.minecraft.world.level.Level;

public class ItStinksEffect extends MobEffect {


    public ItStinksEffect() {
        super(MobEffectCategory.HARMFUL, 0x8B4513);
    }

    @Override
    public void applyEffectTick(LivingEntity entity, int amplifier) {

        if (entity.level().isClientSide) {
            spawnParticles(entity, amplifier);
        }

        if (!entity.level().isClientSide) {
            if (entity.tickCount % 40 == 0) {
                entity.hurt(entity.damageSources().magic(), (float) (0.01 * (amplifier + 0.1)));
            }
        }


    }


    private void spawnParticles(LivingEntity entity, int amplifier) {
        Level world = entity.level();
        double radius = 0.5 + amplifier * 0.2;
        int density = amplifier; // 根据等级增加密度

        for (int i = 0; i < density; i++) {
            double offsetX = (world.random.nextDouble() - 0.5) * 2 * radius;
            double offsetY = world.random.nextDouble() * entity.getBbHeight();
            double offsetZ = (world.random.nextDouble() - 0.5) * 2 * radius;

            double speedX = (world.random.nextDouble() - 0.5) * 0.02;
            double speedY = 0.02 + world.random.nextDouble() * 0.03;
            double speedZ = (world.random.nextDouble() - 0.5) * 0.02;


            int col =0x8B4513;
            double d0 = (double)(col >> 16 & 255) / 255.0D;
            double d1 = (double)(col >> 8 & 255) / 255.0D;
            double d2 = (double)(col >> 0 & 255) / 255.0D;
            for(int j = 0; j < 2; ++j) {
                entity.level().addParticle(ParticleTypes.ENTITY_EFFECT,                     entity.getX() + offsetX,
                        entity.getY() + offsetY,
                        entity.getZ() + offsetZ,d0, d1, d2);
            }
        }
    }

    @Override
    public boolean isDurationEffectTick(int duration, int amplifier) {
        return true;
    }

    @Override
    public void removeAttributeModifiers(LivingEntity entity, AttributeMap map, int amplifier) {
        super.removeAttributeModifiers(entity, map, amplifier);
    }

}