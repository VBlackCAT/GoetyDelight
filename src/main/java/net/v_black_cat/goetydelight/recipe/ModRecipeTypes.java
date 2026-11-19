package net.v_black_cat.goetydelight.recipe;

import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.RegistryObject;
import net.v_black_cat.goetydelight.GoetyDelight;

/**
 * 本模组的自定义配方类型。
 */
public final class ModRecipeTypes {

    public static final DeferredRegister<RecipeType<?>> RECIPE_TYPES =
            DeferredRegister.create(Registries.RECIPE_TYPE, GoetyDelight.MODID);

    public static final RegistryObject<RecipeType<WorkstationCookingRecipe>> WORKSTATION_COOKING =
            RECIPE_TYPES.register("workstation_cooking",
                    () -> RecipeType.simple(new ResourceLocation(GoetyDelight.MODID, "workstation_cooking")));

    private ModRecipeTypes() {
    }
}
