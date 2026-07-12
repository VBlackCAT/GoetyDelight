package net.v_black_cat.goetydelight.init;

import net.minecraft.world.item.ArmorMaterial;
import net.minecraft.world.item.armortrim.TrimMaterial;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.v_black_cat.goetydelight.GoetyDelight;

public class ModTrimMaterials {
    public static final DeferredRegister<TrimMaterial> TRIM_MATERIALS =
            DeferredRegister.create(net.minecraft.core.registries.Registries.TRIM_MATERIAL, GoetyDelight.MODID);

    // 示例（需替换为实际数据）
    // public static final DeferredHolder<TrimMaterial, TrimMaterial> EXAMPLE_TRIM =
    //         TRIM_MATERIALS.register("example_trim", () -> new TrimMaterial(
    //                 "goetydelight:example_trim",
    //                 ModItems.EXAMPLE_ITEM.get(),
    //                 0.8F,
    //                 Map.of(),
    //                 Component.translatable("trim_material.goetydelight.example_trim")
    //         ));

    public static void register(IEventBus modEventBus) {
        TRIM_MATERIALS.register(modEventBus);
    }
}