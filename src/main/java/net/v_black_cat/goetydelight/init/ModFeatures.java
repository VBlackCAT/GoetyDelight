package net.v_black_cat.goetydelight.init;

import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.configurations.FeatureConfiguration;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.v_black_cat.goetydelight.GoetyDelight;

public class ModFeatures {
    public static final DeferredRegister<Feature<?>> FEATURES =
            DeferredRegister.create(net.minecraft.core.registries.Registries.FEATURE, GoetyDelight.MODID);

    // 示例（需替换为实际 Feature 类）
    // public static final DeferredHolder<Feature<?>, Feature<YourFeatureConfig>> EXAMPLE_FEATURE =
    //         FEATURES.register("example_feature", () -> new YourFeature(YourFeatureConfig.CODEC));

    public static void register(IEventBus modEventBus) {
        FEATURES.register(modEventBus);
    }
}