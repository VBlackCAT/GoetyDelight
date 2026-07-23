package net.v_black_cat.goetydelight.init;

import net.minecraft.world.item.crafting.RecipeType;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.v_black_cat.goetydelight.GoetyDelight;

public class ModRecipeTypes {
    public static final DeferredRegister<RecipeType<?>> RECIPE_TYPES =
            DeferredRegister.create(net.minecraft.core.registries.Registries.RECIPE_TYPE, GoetyDelight.MODID);

    // 示例配方类型
    // public static final DeferredHolder<RecipeType<?>, RecipeType<YourRecipe>> YOUR_RECIPE_TYPE =
    //         RECIPE_TYPES.register("your_recipe_type", () -> new RecipeType<>() {});

    public static void register(IEventBus modEventBus) {
        RECIPE_TYPES.register(modEventBus);
    }
}