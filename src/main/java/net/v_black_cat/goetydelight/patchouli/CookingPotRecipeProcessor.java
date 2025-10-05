package net.v_black_cat.goetydelight.patchouli;

import net.minecraft.client.Minecraft;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
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
        ResourceLocation id = new ResourceLocation(variables.get("recipe").asString());
        recipe = (CookingPotRecipe) level.getRecipeManager()
                .byKey(id)
                .filter(r -> r.getType() == vectorwing.farmersdelight.common.registry.ModRecipeTypes.COOKING.get())
                .map(r -> (CookingPotRecipe) r)
                .orElse(null);
    }

    @Override
    public IVariable process(Level level, String key) {
        if (recipe == null) return null;

        switch (key) {
            // 处理6个输入槽位
            case "input0": return getIngredientStack(0);
            case "input1": return getIngredientStack(1);
            case "input2": return getIngredientStack(2);
            case "input3": return getIngredientStack(3);
            case "input4": return getIngredientStack(4);
            case "input5": return getIngredientStack(5);

            // 输出物品
            case "output":
                return IVariable.from(recipe.getResultItem(level.registryAccess()));

            // 容器物品
            case "container":
                return IVariable.from(recipe.getOutputContainer());

            // 烹饪时间
            case "cookTime":
                return IVariable.wrap(recipe.getCookTime());

            // 经验值
            case "experience":
                return IVariable.wrap(recipe.getExperience());

            default:
                return null;
        }
    }

    private IVariable getIngredientStack(int index) {
        List<Ingredient> ingredients = recipe.getIngredients();
        if (index >= ingredients.size()) {
            return IVariable.from(ItemStack.EMPTY);
        }

        Ingredient ingredient = ingredients.get(index);
        ItemStack[] items = ingredient.getItems();
        if (items.length > 0) {
            return IVariable.from(items[0]);
        }
        return IVariable.from(ItemStack.EMPTY);
    }
}