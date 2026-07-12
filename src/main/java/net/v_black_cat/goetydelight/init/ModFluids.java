package net.v_black_cat.goetydelight.init;

import net.minecraft.world.level.material.Fluid;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.fluids.BaseFlowingFluid;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.v_black_cat.goetydelight.GoetyDelight;

public class ModFluids {
    public static final DeferredRegister<Fluid> FLUIDS =
            DeferredRegister.create(net.minecraft.core.registries.Registries.FLUID, GoetyDelight.MODID);

    // 示例流体（需替换为实际流体类）
    // public static final DeferredHolder<Fluid, BaseFlowingFluid.Source> EXAMPLE_FLUID_SOURCE =
    //         FLUIDS.register("example_fluid", () -> new BaseFlowingFluid.Source(ExampleFluidProperties.PROPERTIES));
    // public static final DeferredHolder<Fluid, BaseFlowingFluid.Flowing> EXAMPLE_FLUID_FLOWING =
    //         FLUIDS.register("example_fluid_flowing", () -> new BaseFlowingFluid.Flowing(ExampleFluidProperties.PROPERTIES));

    public static void register(IEventBus modEventBus) {
        FLUIDS.register(modEventBus);
    }
}