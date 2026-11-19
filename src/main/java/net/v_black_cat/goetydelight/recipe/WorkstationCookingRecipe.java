package net.v_black_cat.goetydelight.recipe;

import com.google.gson.JsonObject;
import net.minecraft.core.NonNullList;
import net.minecraft.core.RegistryAccess;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.block.Block;
import net.minecraftforge.registries.ForgeRegistries;
import net.v_black_cat.goetydelight.GoetyDelight;
import vectorwing.farmersdelight.client.recipebook.CookingPotRecipeBookTab;
import vectorwing.farmersdelight.common.crafting.CookingPotRecipe;

import javax.annotation.Nullable;

/**
 * 带工作方块限制的农夫乐事锅配方。
 * <p>
 * 普通农夫乐事锅配方没有“必须由哪个锅烹饪”的概念；这里通过自定义配方类型承载
 * {@code workstation} 字段，让诅咒金属锅/天启锅在匹配配方时自行校验方块。
 */
public class WorkstationCookingRecipe extends CookingPotRecipe {

    public static final String WORKSTATION = "workstation";

    public static final ResourceLocation CURSED_INGOT_POT =
            new ResourceLocation(GoetyDelight.MODID, "cursed_ingot_pot");
    public static final ResourceLocation APOCALYPTIUM_COOKING_POT =
            new ResourceLocation(GoetyDelight.MODID, "apocalyptium_cooking_pot");

    private final ResourceLocation workstation;

    public WorkstationCookingRecipe(ResourceLocation id, String group,
                                    @Nullable CookingPotRecipeBookTab tab,
                                    NonNullList<Ingredient> inputItems,
                                    ItemStack output, ItemStack container,
                                    float experience, int cookTime,
                                    ResourceLocation workstation) {
        super(id, group, tab, inputItems, output, container, experience, cookTime);
        this.workstation = workstation;
    }

    public ResourceLocation getWorkstation() {
        return workstation;
    }

    public boolean isCursedIngotPot() {
        return CURSED_INGOT_POT.equals(workstation);
    }

    public boolean isApocalyptiumPot() {
        return APOCALYPTIUM_COOKING_POT.equals(workstation);
    }

    public boolean matchesWorkstation(Block block) {
        return workstation.equals(ForgeRegistries.BLOCKS.getKey(block));
    }

    @Override
    public RecipeSerializer<?> getSerializer() {
        return ModRecipeSerializers.WORKSTATION_COOKING.get();
    }

    @Override
    public RecipeType<?> getType() {
        return ModRecipeTypes.WORKSTATION_COOKING.get();
    }

    public static class Serializer implements RecipeSerializer<WorkstationCookingRecipe> {

        private final CookingPotRecipe.Serializer delegate = new CookingPotRecipe.Serializer();

        @Override
        public WorkstationCookingRecipe fromJson(ResourceLocation id, JsonObject json) {
            CookingPotRecipe base = delegate.fromJson(id, json);
            ResourceLocation workstation = new ResourceLocation(
                    GsonHelper.getAsString(json, WORKSTATION,
                            APOCALYPTIUM_COOKING_POT.toString()));

            return new WorkstationCookingRecipe(
                    id,
                    base.getGroup(),
                    base.getRecipeBookTab(),
                    base.getIngredients(),
                    base.getResultItem(RegistryAccess.EMPTY).copy(),
                    base.getOutputContainer().copy(),
                    base.getExperience(),
                    base.getCookTime(),
                    workstation
            );
        }

        @Override
        public WorkstationCookingRecipe fromNetwork(ResourceLocation id, FriendlyByteBuf buffer) {
            CookingPotRecipe base = delegate.fromNetwork(id, buffer);
            ResourceLocation workstation = buffer.readResourceLocation();

            return new WorkstationCookingRecipe(
                    id,
                    base.getGroup(),
                    base.getRecipeBookTab(),
                    base.getIngredients(),
                    base.getResultItem(RegistryAccess.EMPTY).copy(),
                    base.getOutputContainer().copy(),
                    base.getExperience(),
                    base.getCookTime(),
                    workstation
            );
        }

        @Override
        public void toNetwork(FriendlyByteBuf buffer, WorkstationCookingRecipe recipe) {
            // 先写农夫乐事的标准字段，再追加工作方块字段。
            delegate.toNetwork(buffer, recipe);
            buffer.writeResourceLocation(recipe.getWorkstation());
        }
    }
}
