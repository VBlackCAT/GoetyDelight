package net.v_black_cat.goetydelight.compat.jei;

import mezz.jei.api.gui.builder.ITooltipBuilder;
import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.gui.ingredient.IRecipeSlotsView;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.recipe.RecipeType;
import net.minecraft.network.chat.Component;
import net.v_black_cat.goetydelight.recipe.WorkstationCookingRecipe;
import vectorwing.farmersdelight.common.crafting.CookingPotRecipe;
import vectorwing.farmersdelight.integration.jei.category.CookingRecipeCategory;

/**
 * 复用农夫乐事锅的 JEI 界面，但分类、标题和图标都换成对应的工作方块。
 */
public class WorkstationCookingRecipeCategory extends CookingRecipeCategory {

    private final RecipeType<CookingPotRecipe> recipeType;
    private final IDrawable icon;
    private final Component title;
    private final boolean showSoulCost;

    public WorkstationCookingRecipeCategory(IGuiHelper guiHelper,
                                            RecipeType<CookingPotRecipe> recipeType,
                                            IDrawable icon,
                                            Component title) {
        this(guiHelper, recipeType, icon, title, false);
    }

    public WorkstationCookingRecipeCategory(IGuiHelper guiHelper,
                                            RecipeType<CookingPotRecipe> recipeType,
                                            IDrawable icon,
                                            Component title,
                                            boolean showSoulCost) {
        super(guiHelper);
        this.recipeType = recipeType;
        this.icon = icon;
        this.title = title;
        this.showSoulCost = showSoulCost;
    }

    @Override
    public RecipeType<CookingPotRecipe> getRecipeType() {
        return recipeType;
    }

    @Override
    public Component getTitle() {
        return title;
    }

    @Override
    public IDrawable getIcon() {
        return icon;
    }

    @Override
    public void getTooltip(ITooltipBuilder tooltip, CookingPotRecipe recipe,
                           IRecipeSlotsView recipeSlotsView, double mouseX, double mouseY) {
        super.getTooltip(tooltip, recipe, recipeSlotsView, mouseX, mouseY);
        if (this.showSoulCost
                && recipe instanceof WorkstationCookingRecipe workstationRecipe
                && workstationRecipe.getSoulCost() > 0) {
            tooltip.add(Component.translatable("jei.goetydelight.soul_cost",
                    workstationRecipe.getSoulCost()));
        }
    }
}
