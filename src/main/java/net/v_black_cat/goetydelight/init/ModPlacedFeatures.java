package net.v_black_cat.goetydelight.init;

import net.minecraft.world.level.levelgen.placement.PlacedFeature;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.v_black_cat.goetydelight.GoetyDelight;

public class ModPlacedFeatures {
    public static final DeferredRegister<PlacedFeature> PLACED_FEATURES =
            DeferredRegister.create(net.minecraft.core.registries.Registries.PLACED_FEATURE, GoetyDelight.MODID);

    // 示例
    // public static final DeferredHolder<PlacedFeature, PlacedFeature> EXAMPLE_PLACED =
    //         PLACED_FEATURES.register("example_placed", () -> new PlacedFeature(
    //                 ModConfiguredFeatures.EXAMPLE_CONFIGURED.getHolder().orElseThrow(),
    //                 List.of(RarityFilter.onAverageOnceEvery(10))
    //         ));

    public static void register(IEventBus modEventBus) {
        PLACED_FEATURES.register(modEventBus);
    }
}