package net.v_black_cat.goetydelight.init;

import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.common.world.BiomeModifier;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.v_black_cat.goetydelight.GoetyDelight;

public class ModBiomeModifiers {
    public static final DeferredRegister<BiomeModifier> BIOME_MODIFIERS =
            DeferredRegister.create(net.neoforged.neoforge.registries.NeoForgeRegistries.Keys.BIOME_MODIFIERS, GoetyDelight.MODID);

    // 示例（需替换为实际修改器）
    // public static final DeferredHolder<BiomeModifier, BiomeModifier> EXAMPLE_MODIFIER =
    //         BIOME_MODIFIERS.register("example_modifier", () -> new ExampleBiomeModifier(...));

    public static void register(IEventBus modEventBus) {
        BIOME_MODIFIERS.register(modEventBus);
    }
}