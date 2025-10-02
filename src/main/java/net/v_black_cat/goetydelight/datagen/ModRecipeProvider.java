package net.v_black_cat.goetydelight.datagen;

import net.v_black_cat.goetydelight.GoetyDelight;
import net.v_black_cat.goetydelight.item.ModItems;
import net.minecraft.data.PackOutput;
import net.minecraft.data.recipes.*;
import net.minecraft.world.item.crafting.AbstractCookingRecipe;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.level.ItemLike;
import net.minecraftforge.common.crafting.conditions.IConditionBuilder;

import java.util.List;
import java.util.function.Consumer;

public class ModRecipeProvider extends RecipeProvider implements IConditionBuilder {
    private static final List<ItemLike>  Marble_OP_SWORD_SMELTABLES = List.of(ModItems.MARBLE_OP_SWORD.get());

    public ModRecipeProvider(PackOutput pOutput) {
        super(pOutput);
    }

    @Override
    protected void buildRecipes(Consumer<FinishedRecipe> pWriter) {
//        oreSmelting(pWriter,  Marble_OP_SWORD_SMELTABLES, RecipeCategory.MISC, ModItems. Marble_OP_SWORD.get(), 0.25f, 200, " Marble_OP_SWORD");
//        oreBlasting(pWriter,  Marble_OP_SWORD_SMELTABLES, RecipeCategory.MISC, ModItems. Marble_OP_SWORD.get(), 0.25f, 100, " Marble_OP_SWORD");


//        ItemStack potionStack = new ItemStack(Items.POTION);
//
//        // 2. 将这个药水设置为“瞬间治疗”药水
//        PotionUtils.setPotion(potionStack, Potions.HEALING);
//        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, ModBlocks.MARBLE.get())
//                .pattern("SSS")
//                .pattern("SSS")
//                .pattern("SSS")
//                .define('S', potionStack.getItem())
//                .unlockedBy(getHasName(ModItems. Marble_OP_SWORD.get()), has(ModItems. Marble_OP_SWORD.get()))
//                .save(pWriter);
//
//        ShapelessRecipeBuilder.shapeless(RecipeCategory.MISC, ModItems. Marble_OP_SWORD.get(), 9)
//                .requires(ModBlocks.MARBLE.get())
//                .unlockedBy(getHasName(ModBlocks.MARBLE.get()), has(ModBlocks. MARBLE.get()))
//                .save(pWriter);
    }

    protected static void oreSmelting(Consumer<FinishedRecipe> pFinishedRecipeConsumer, List<ItemLike> pIngredients, RecipeCategory pCategory, ItemLike pResult, float pExperience, int pCookingTIme, String pGroup) {
        oreCooking(pFinishedRecipeConsumer, RecipeSerializer.SMELTING_RECIPE, pIngredients, pCategory, pResult, pExperience, pCookingTIme, pGroup, "_from_smelting");
    }

    protected static void oreBlasting(Consumer<FinishedRecipe> pFinishedRecipeConsumer, List<ItemLike> pIngredients, RecipeCategory pCategory, ItemLike pResult, float pExperience, int pCookingTime, String pGroup) {
        oreCooking(pFinishedRecipeConsumer, RecipeSerializer.BLASTING_RECIPE, pIngredients, pCategory, pResult, pExperience, pCookingTime, pGroup, "_from_blasting");
    }

    protected static void oreCooking(Consumer<FinishedRecipe> pFinishedRecipeConsumer, RecipeSerializer<? extends AbstractCookingRecipe> pCookingSerializer, List<ItemLike> pIngredients, RecipeCategory pCategory, ItemLike pResult, float pExperience, int pCookingTime, String pGroup, String pRecipeName) {
        for(ItemLike itemlike : pIngredients) {
            SimpleCookingRecipeBuilder.generic(Ingredient.of(itemlike), pCategory, pResult,
                    pExperience, pCookingTime, pCookingSerializer)
                    .group(pGroup).unlockedBy(getHasName(itemlike), has(itemlike))
                    .save(pFinishedRecipeConsumer,  GoetyDelight.MODID + ":" + getItemName(pResult) + pRecipeName + "_" + getItemName(itemlike));
        }
    }
}
