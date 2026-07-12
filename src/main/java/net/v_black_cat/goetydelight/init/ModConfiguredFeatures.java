package net.v_black_cat.goetydelight.init;

import net.minecraft.world.level.levelgen.feature.ConfiguredFeature;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.v_black_cat.goetydelight.GoetyDelight;

public class ModConfiguredFeatures {
    public static final DeferredRegister<ConfiguredFeature<?, ?>> CONFIGURED_FEATURES =
            DeferredRegister.create(net.minecraft.core.registries.Registries.CONFIGURED_FEATURE, GoetyDelight.MODID);

    // 示例（通常通过 JSON 数据驱动，此处仅为注册占位）
    // public static final DeferredHolder<ConfiguredFeature<?, ?>, ConfiguredFeature<?, ?>> EXAMPLE_CONFIGURED =
    //         CONFIGURED_FEATURES.register("example_configured", () -> new ConfiguredFeature<>(ModFeatures.EXAMPLE_FEATURE.get(), ...));

    public static void register(IEventBus modEventBus) {
        CONFIGURED_FEATURES.register(modEventBus);
    }
}