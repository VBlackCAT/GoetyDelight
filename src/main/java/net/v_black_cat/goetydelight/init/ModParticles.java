package net.v_black_cat.goetydelight.init;

import net.minecraft.core.particles.ParticleType;
import net.minecraft.core.particles.SimpleParticleType;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.v_black_cat.goetydelight.GoetyDelight;

public class ModParticles {
    public static final DeferredRegister<ParticleType<?>> PARTICLES =
            DeferredRegister.create(net.minecraft.core.registries.Registries.PARTICLE_TYPE, GoetyDelight.MODID);

    // 示例粒子
    public static final DeferredHolder<ParticleType<?>, SimpleParticleType> EXAMPLE_PARTICLE =
            PARTICLES.register("example_particle", () -> new SimpleParticleType(false));

    public static void register(IEventBus modEventBus) {
        PARTICLES.register(modEventBus);
    }
}