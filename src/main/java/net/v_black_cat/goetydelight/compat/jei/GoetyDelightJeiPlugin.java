package net.v_black_cat.goetydelight.compat.jei;

import mezz.jei.api.IModPlugin;
import mezz.jei.api.JeiPlugin;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.recipe.RecipeType;
import mezz.jei.api.registration.IRecipeCatalystRegistration;
import mezz.jei.api.registration.IRecipeCategoryRegistration;
import mezz.jei.api.registration.IRecipeRegistration;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.registries.ForgeRegistries;
import net.v_black_cat.goetydelight.GoetyDelight;
import net.v_black_cat.goetydelight.block.CursedIngotPotBlockEntity;
import net.v_black_cat.goetydelight.block.ModBlocks;
import net.v_black_cat.goetydelight.compat.goetyrevelation.GoetyRevelationCompat;
import net.v_black_cat.goetydelight.compat.goetyrevelation.RevelationCompatRegistry;
import net.v_black_cat.goetydelight.recipe.WorkstationCookingRecipe;
import vectorwing.farmersdelight.common.crafting.CookingPotRecipe;
import vectorwing.farmersdelight.common.registry.ModRecipeTypes;

import java.util.ArrayList;
import java.util.List;

/**
 * JEI 分区：
 * <ul>
 *     <li>诅咒烹饪：农夫乐事普通锅配方 + 诅咒金属锅专属配方；</li>
 *     <li>神灵烹饪：农夫乐事锅、诅咒金属锅和天启锅专属配方；</li>
 *     <li>神灵烹饪：亡灵火炉：由 goety:brazier 转换而来的天启锅配方。</li>
 * </ul>
 */
@JeiPlugin
public class GoetyDelightJeiPlugin implements IModPlugin {

    public static final RecipeType<CookingPotRecipe> CURSED_COOKING =
            RecipeType.create(GoetyDelight.MODID, "cursed_cooking", CookingPotRecipe.class);
    public static final RecipeType<CookingPotRecipe> CURSED_BRAZIER =
            RecipeType.create(GoetyDelight.MODID, "cursed_brazier", CookingPotRecipe.class);
    public static final RecipeType<CookingPotRecipe> APOCALYPTIUM_COOKING =
            RecipeType.create(GoetyDelight.MODID, "apocalyptium_cooking", CookingPotRecipe.class);
    public static final RecipeType<CookingPotRecipe> APOCALYPTIUM_BRAZIER =
            RecipeType.create(GoetyDelight.MODID, "apocalyptium_brazier", CookingPotRecipe.class);

    @Override
    public ResourceLocation getPluginUid() {
        return new ResourceLocation(GoetyDelight.MODID, "jei");
    }

    @Override
    public void registerCategories(IRecipeCategoryRegistration registration) {
        IGuiHelper guiHelper = registration.getJeiHelpers().getGuiHelper();

        registration.addRecipeCategories(new WorkstationCookingRecipeCategory(
                guiHelper,
                CURSED_COOKING,
                guiHelper.createDrawableItemStack(new ItemStack(ModBlocks.CURSED_INGOT_POT.get())),
                Component.translatable("jei.goetydelight.cursed_cooking")
        ));
        registration.addRecipeCategories(new WorkstationCookingRecipeCategory(
                guiHelper,
                CURSED_BRAZIER,
                guiHelper.createDrawableItemStack(infusedCursedPot()),
                Component.translatable("jei.goetydelight.cursed_brazier")
        ));

        if (GoetyRevelationCompat.IS_LOADED) {
            ApocCategory.addCategories(registration, guiHelper);
        }
    }

    @Override
    public void registerRecipes(IRecipeRegistration registration) {
        ClientLevel level = Minecraft.getInstance().level;
        if (level == null) {
            return;
        }

        List<CookingPotRecipe> farmerCooking = new ArrayList<>();
        List<CookingPotRecipe> cursed = new ArrayList<>();
        List<CookingPotRecipe> apocalyptium = new ArrayList<>();
        List<CookingPotRecipe> brazier = new ArrayList<>();

        for (var recipeObject : level.getRecipeManager().getRecipes()) {
            if (!(recipeObject instanceof CookingPotRecipe recipe)) {
                continue;
            }

            if (recipe instanceof WorkstationCookingRecipe workstationRecipe) {
                if (workstationRecipe.isCursedIngotPot()) {
                    cursed.add(recipe);
                } else if (workstationRecipe.isApocalyptiumPot()) {
                    if (recipeObject.getId().getPath().startsWith("apocalyptium_brazier/")) {
                        brazier.add(recipe);
                    } else {
                        apocalyptium.add(recipe);
                    }
                }
                continue;
            }

            // 原版农夫乐事锅配方：归入诅咒烹饪/神灵烹饪的基础内容。
            if (recipe.getType() == ModRecipeTypes.COOKING.get()) {
                farmerCooking.add(recipe);
            }
        }

        // 诅咒烹饪：农夫乐事锅 + 诅咒金属锅专属
        List<CookingPotRecipe> cursedRecipes = new ArrayList<>(farmerCooking);
        cursedRecipes.addAll(cursed);
        if (!cursedRecipes.isEmpty()) {
            registration.addRecipes(CURSED_COOKING, cursedRecipes);
        }
        if (!brazier.isEmpty()) {
            registration.addRecipes(CURSED_BRAZIER, brazier);
        }

        if (GoetyRevelationCompat.IS_LOADED) {
            // 神灵烹饪：在诅咒烹饪基础上追加天启锅专属（不含亡灵火炉）
            List<CookingPotRecipe> apocRecipes = new ArrayList<>(cursedRecipes);
            apocRecipes.addAll(apocalyptium);
            if (!apocRecipes.isEmpty()) {
                registration.addRecipes(APOCALYPTIUM_COOKING, apocRecipes);
            }
            if (!brazier.isEmpty()) {
                registration.addRecipes(APOCALYPTIUM_BRAZIER, brazier);
            }
        }
    }

    @Override
    public void registerRecipeCatalysts(IRecipeCatalystRegistration registration) {
        registration.addRecipeCatalyst(
                new ItemStack(ModBlocks.CURSED_INGOT_POT.get()), CURSED_COOKING);
        registration.addRecipeCatalyst(infusedCursedPot(), CURSED_BRAZIER);

        if (GoetyRevelationCompat.IS_LOADED) {
            ApocCategory.addCatalysts(registration);
        }
    }

    private static ItemStack infusedCursedPot() {
        ItemStack stack = new ItemStack(ModBlocks.CURSED_INGOT_POT.get());
        stack.getOrCreateTag().putBoolean(CursedIngotPotBlockEntity.NECROTIC_INFUSED_TAG, true);
        return stack;
    }

    private static ItemStack necroBrazierIcon() {
        Item item = ForgeRegistries.ITEMS.getValue(new ResourceLocation("goety", "necro_brazier"));
        return item == null
                ? new ItemStack(RevelationCompatRegistry.APOCALYPTIUM_POT_ITEM.get())
                : new ItemStack(item);
    }

    /**
     * 类隔离：未安装 goety_revelation 时不要解析天启锅相关类。
     */
    private static final class ApocCategory {
        static void addCategories(IRecipeCategoryRegistration registration, IGuiHelper guiHelper) {
            registration.addRecipeCategories(
                    new WorkstationCookingRecipeCategory(
                            guiHelper,
                            APOCALYPTIUM_COOKING,
                            guiHelper.createDrawableItemStack(
                                    new ItemStack(RevelationCompatRegistry.APOCALYPTIUM_POT_ITEM.get())),
                            Component.translatable("jei.goetydelight.apocalyptium_cooking")
                    ),
                    new WorkstationCookingRecipeCategory(
                            guiHelper,
                            APOCALYPTIUM_BRAZIER,
                            guiHelper.createDrawableItemStack(necroBrazierIcon()),
                            Component.translatable("jei.goetydelight.apocalyptium_brazier")
                    )
            );
        }

        static void addCatalysts(IRecipeCatalystRegistration registration) {
            ItemStack apocPot = new ItemStack(RevelationCompatRegistry.APOCALYPTIUM_POT_ITEM.get());
            registration.addRecipeCatalyst(apocPot, APOCALYPTIUM_COOKING);
            registration.addRecipeCatalyst(apocPot, APOCALYPTIUM_BRAZIER);
        }
    }
}
