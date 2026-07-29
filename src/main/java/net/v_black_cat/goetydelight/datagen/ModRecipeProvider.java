package net.v_black_cat.goetydelight.datagen;

import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.minecraft.data.recipes.RecipeCategory;
import net.minecraft.data.recipes.RecipeOutput;
import net.minecraft.data.recipes.RecipeProvider;
import net.minecraft.world.level.block.Blocks;
import net.v_black_cat.goetydelight.events.DollRegisterEventHandler;

import java.util.concurrent.CompletableFuture;

public class ModRecipeProvider extends RecipeProvider {

    public ModRecipeProvider(PackOutput output, CompletableFuture<HolderLookup.Provider> registries) {
        super(output, registries);
    }

    @Override
    protected void buildRecipes(RecipeOutput recipeOutput) {
        // 遍历所有 Doll 方块，注册切石机配方
        for (var entry : DollRegisterEventHandler.DOLL_BLOCKS.entrySet()) {
            stonecutterResultFromBase(
                    recipeOutput,
                    RecipeCategory.DECORATIONS,
                    entry.getValue(),        // DollBlock
                    Blocks.WHITE_WOOL,       // 原料：白色羊毛
                    1                        // 产出 1 个
            );
        }
    }
}