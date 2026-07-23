package net.v_black_cat.goetydelight.init;

import net.minecraft.world.item.crafting.RecipeSerializer;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.v_black_cat.goetydelight.GoetyDelight;

public class ModRecipeSerializers {
    public static final DeferredRegister<RecipeSerializer<?>> RECIPE_SERIALIZERS =
            DeferredRegister.create(net.minecraft.core.registries.Registries.RECIPE_SERIALIZER, GoetyDelight.MODID);

    // 示例序列化器（需替换为实际序列化器类）
    // public static final DeferredHolder<RecipeSerializer<?>, RecipeSerializer<YourRecipe>> YOUR_SERIALIZER =
    //         RECIPE_SERIALIZERS.register("your_recipe", () -> new YourRecipe.Serializer());

    public static void register(IEventBus modEventBus) {
        RECIPE_SERIALIZERS.register(modEventBus);
    }
}