package net.v_black_cat.goetydelight.init;

import net.minecraft.world.item.armortrim.TrimPattern;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.v_black_cat.goetydelight.GoetyDelight;

public class ModTrimPatterns {
    public static final DeferredRegister<TrimPattern> TRIM_PATTERNS =
            DeferredRegister.create(net.minecraft.core.registries.Registries.TRIM_PATTERN, GoetyDelight.MODID);

    // 示例（需替换为实际数据）
    // public static final DeferredHolder<TrimPattern, TrimPattern> EXAMPLE_PATTERN =
    //         TRIM_PATTERNS.register("example_pattern", () -> new TrimPattern(
    //                 net.minecraft.resources.ResourceLocation.fromNamespaceAndPath(GoetyDelight.MODID, "example_pattern"),
    //                 ModItems.EXAMPLE_ITEM.get(),
    //                 Component.translatable("trim_pattern.goetydelight.example_pattern")
    //         ));

    public static void register(IEventBus modEventBus) {
        TRIM_PATTERNS.register(modEventBus);
    }
}