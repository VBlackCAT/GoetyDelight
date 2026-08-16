package net.v_black_cat.goetydelight.util;

import net.minecraft.core.particles.ItemParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;

public class ParticlesUtil {
    public static void spawnItemParticles(ItemStack stack, int amount, LivingEntity entity) {
        for(int i = 0; i < amount; ++i) {
            Vec3 vec3 = new Vec3(((double)entity.getRandom().nextFloat() - 0.5D) * 0.1D, entity.getRandom().nextDouble() * 0.1D + 0.1D, 0.0D);
            vec3 = vec3.xRot(-entity.getXRot() * ((float)Math.PI / 180F));
            vec3 = vec3.yRot(-entity.getYRot() * ((float)Math.PI / 180F));
            double d0 = (double)(-entity.getRandom().nextFloat()) * 0.6D - 0.3D;
            Vec3 vec31 = new Vec3(((double)entity.getRandom().nextFloat() - 0.5D) * 0.3D, d0, 0.6D);
            vec31 = vec31.xRot(-entity.getXRot() * ((float)Math.PI / 180F));
            vec31 = vec31.yRot(-entity.getYRot() * ((float)Math.PI / 180F));
            vec31 = vec31.add(entity.getX(), entity.getEyeY(), entity.getZ());
            if (entity.level() instanceof ServerLevel) //Forge: Fix MC-2518 spawnParticle is nooped on server, need to use server specific variant
                ((ServerLevel) entity.level()).sendParticles(new ItemParticleOption(ParticleTypes.ITEM, stack), vec31.x, vec31.y, vec31.z, 1, vec3.x, vec3.y + 0.05D, vec3.z, 0.0D);
            else
                entity.level().addParticle(new ItemParticleOption(ParticleTypes.ITEM, stack), vec31.x, vec31.y, vec31.z, vec3.x, vec3.y + 0.05D, vec3.z);
        }

    }
}
