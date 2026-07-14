package net.v_black_cat.goetydelight.effect;

import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.RandomSource;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.client.extensions.common.IClientMobEffectExtensions;
import java.util.function.Consumer;

import static net.minecraft.data.models.model.TextureMapping.getItemTexture;

public class SoulConvergenceRoomEffect extends MobEffect {
    public SoulConvergenceRoomEffect() {
        super(MobEffectCategory.BENEFICIAL, 0x98D982);
    }

    @Override
    public boolean applyEffectTick(LivingEntity entity, int amplifier) {
        if (entity.level().isClientSide && entity.tickCount % 10 == 0) {
            spawnParticles(entity);
        }
        return true;
    }

    @Override
    public boolean shouldApplyEffectTickThisTick(int duration, int amplifier) {
        return true;
    }

    private void spawnParticles(LivingEntity entity) {
        Level level = entity.level();
        RandomSource random = level.random;
        for (int i = 0; i < 3; i++) {
            double x = entity.getX() + (random.nextDouble() - 0.5) * 2.0;
            double y = entity.getY() + random.nextDouble() * 2.0;
            double z = entity.getZ() + (random.nextDouble() - 0.5) * 2.0;
            level.addParticle(ParticleTypes.ENCHANT, x, y, z, 0, 0, 0);
        }
    }
}