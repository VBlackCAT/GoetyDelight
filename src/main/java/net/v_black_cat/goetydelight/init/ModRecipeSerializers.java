package net.v_black_cat.goetydelight.init;

import net.minecraft.core.registries.Registries;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.SimpleCraftingRecipeSerializer;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.v_black_cat.goetydelight.GoetyDelight;
import net.v_black_cat.goetydelight.init.doll.DollEntityCraftingRecipe;

public class ModRecipeSerializers {
    public static final DeferredRegister<RecipeSerializer<?>> RECIPE_SERIALIZERS =
            DeferredRegister.create(Registries.RECIPE_SERIALIZER, GoetyDelight.MODID);

    public static final DeferredHolder<RecipeSerializer<?>, RecipeSerializer<DollEntityCraftingRecipe>> DOLL_ENTITY_CRAFTING =
            RECIPE_SERIALIZERS.register("doll_entity_crafting",
                    () -> new SimpleCraftingRecipeSerializer<>(DollEntityCraftingRecipe::new));

    public static void register(IEventBus modEventBus) {
        RECIPE_SERIALIZERS.register(modEventBus);
    }
}