package net.v_black_cat.goetydelight.init;

import net.minecraft.world.effect.MobEffect;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.v_black_cat.goetydelight.GoetyDelight;

public class ModEffects {
    public static final DeferredRegister<MobEffect> EFFECTS =
            DeferredRegister.create(net.minecraft.core.registries.Registries.MOB_EFFECT, GoetyDelight.MODID);

    // 示例效果（需替换为实际效果类）
    // public static final DeferredHolder<MobEffect, MobEffect> EXAMPLE_EFFECT =
    //         EFFECTS.register("example_effect", () -> new YourEffect());

    public static void register(IEventBus modEventBus) {
        EFFECTS.register(modEventBus);
    }
}