package net.v_black_cat.goetydelight.patchouli;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.level.Level;
import vazkii.patchouli.api.IComponentProcessor;
import vazkii.patchouli.api.IVariable;
import vazkii.patchouli.api.IVariableProvider;
import vectorwing.farmersdelight.common.crafting.CookingPotRecipe;

import java.util.List;

public class CookingPotRecipeProcessor implements IComponentProcessor {

    private CookingPotRecipe recipe;

    @Override
    public void setup(Level level, IVariableProvider variables) {
        String recipeIdStr = variables.get("recipe", level.registryAccess()).asString();
        ResourceLocation id = ResourceLocation.parse(recipeIdStr);

        RecipeHolder<?> holder = level.getRecipeManager().byKey(id).orElse(null);
        if (holder != null && holder.value() instanceof CookingPotRecipe cookingRecipe) {
            recipe = cookingRecipe;
        }
    }

    @Override
    public IVariable process(Level level, String key) {
        if (recipe == null) return null;

        switch (key) {
            case "input0": return getIngredientStack(level, 0);
            case "input1": return getIngredientStack(level, 1);
            case "input2": return getIngredientStack(level, 2);
            case "input3": return getIngredientStack(level, 3);
            case "input4": return getIngredientStack(level, 4);
            case "input5": return getIngredientStack(level, 5);

            case "output":
                return IVariable.from(recipe.getResultItem(level.registryAccess()), level.registryAccess());

            case "container":
                return IVariable.from(recipe.getOutputContainer(), level.registryAccess());

            case "cookTime":
                return IVariable.wrap(recipe.getCookTime(), level.registryAccess());

            case "experience":
                return IVariable.wrap(recipe.getExperience(), level.registryAccess());

            default:
                return null;
        }
    }

    private IVariable getIngredientStack(Level level, int index) {
        List<Ingredient> ingredients = recipe.getIngredients();
        if (index >= ingredients.size()) {
            return IVariable.from(ItemStack.EMPTY, level.registryAccess());
        }

        Ingredient ingredient = ingredients.get(index);
        ItemStack[] items = ingredient.getItems();
        if (items.length > 0) {
            return IVariable.from(items[0], level.registryAccess());
        }
        return IVariable.from(ItemStack.EMPTY, level.registryAccess());
    }
}