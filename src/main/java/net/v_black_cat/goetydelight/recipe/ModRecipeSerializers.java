package net.v_black_cat.goetydelight.recipe;

import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import net.v_black_cat.goetydelight.GoetyDelight;

public class ModRecipeSerializers {
    public static final DeferredRegister<RecipeSerializer<?>> SERIALIZERS =
            DeferredRegister.create(ForgeRegistries.RECIPE_SERIALIZERS, GoetyDelight.MODID);

    public static final RegistryObject<RecipeSerializer<?>> POTION_AMPLIFIER =
            SERIALIZERS.register("potion_amplifier", () -> new PotionAmplifierRecipe.Serializer());
}