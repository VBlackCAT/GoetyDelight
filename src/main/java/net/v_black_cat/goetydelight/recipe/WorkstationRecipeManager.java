package net.v_black_cat.goetydelight.recipe;

import com.Polarice3.Goety.common.crafting.BrazierRecipe;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import net.minecraft.core.NonNullList;
import net.minecraft.core.RegistryAccess;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeManager;
import net.minecraft.world.item.crafting.ShapedRecipe;
import net.minecraftforge.event.server.ServerStartedEvent;
import net.minecraftforge.registries.ForgeRegistries;
import net.v_black_cat.goetydelight.GoetyDelight;
import vectorwing.farmersdelight.client.recipebook.CookingPotRecipeBookTab;
import vectorwing.farmersdelight.common.crafting.CookingPotRecipe;

import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * 在服务端启动后，把两类配方转换成带工作方块要求的配方：
 * <ul>
 *     <li>{@code goety:brazier} 且原料 <= 6 的配方 -> 天启锅配方；</li>
 *     <li>goetydelight 的 cooking 配方中，含 unholy_blood 的 -> 诅咒金属锅，
 *         含 apocalyptium_ingot / doom_medal 的 -> 天启锅。</li>
 * </ul>
 * 天启锅继承诅咒金属锅的配方接受逻辑，因此诅咒金属锅配方也能由天启锅烹饪。
 */
public final class WorkstationRecipeManager {

    private static final ResourceLocation UNHOLY_BLOOD =
            new ResourceLocation("goety", "unholy_blood");
    private static final ResourceLocation APOCALYPTIUM_INGOT =
            new ResourceLocation("goety_revelation", "apocalyptium_ingot");
    private static final ResourceLocation DOOM_MEDAL =
            new ResourceLocation("goety_revelation", "doom_medal");

    private WorkstationRecipeManager() {
    }

    public static void onServerStarted(ServerStartedEvent event) {
        MinecraftServer server = event.getServer();
        RecipeManager recipeManager = server.getRecipeManager();
        RegistryAccess registryAccess = server.registryAccess();

        List<Recipe<?>> recipes = new ArrayList<>(recipeManager.getRecipes());
        Set<ResourceLocation> ids = new HashSet<>();
        for (Recipe<?> recipe : recipes) {
            ids.add(recipe.getId());
        }
        Set<ResourceLocation> generatedBrazierIds = new HashSet<>();
        Set<ResourceLocation> brazierSourceIds = new HashSet<>();
        Set<ResourceLocation> convertedIds = new HashSet<>();

        boolean changed = false;

        // 1) 将已有的特殊 goetydelight cooking 配方转换为带工作方块要求的配方。
        List<Recipe<?>> converted = new ArrayList<>();
        for (Recipe<?> recipe : recipes) {
            if (recipe instanceof WorkstationCookingRecipe
                    || !(recipe instanceof CookingPotRecipe cookingPotRecipe)
                    || !recipe.getId().getNamespace().equals(GoetyDelight.MODID)) {
                continue;
            }

            ResourceLocation workstation = requiredWorkstation(cookingPotRecipe);
            if (workstation == null) {
                continue;
            }

            converted.add(new WorkstationCookingRecipe(
                    recipe.getId(),
                    cookingPotRecipe.getGroup(),
                    cookingPotRecipe.getRecipeBookTab(),
                    cookingPotRecipe.getIngredients(),
                    cookingPotRecipe.getResultItem(registryAccess).copy(),
                    cookingPotRecipe.getOutputContainer().copy(),
                    cookingPotRecipe.getExperience(),
                    cookingPotRecipe.getCookTime(),
                    workstation
            ));
            convertedIds.add(recipe.getId());
            changed = true;
        }
        if (!converted.isEmpty()) {
            recipes.removeIf(recipe -> convertedIds.contains(recipe.getId()));
            recipes.addAll(converted);
        }

        // 2) 扫描 goety:brazier JSON，转换为天启锅/亡灵注入锅可用的配方。
        Map<ResourceLocation, Resource> resources = server.getResourceManager()
                .listResources("recipes", id -> true);

        for (Map.Entry<ResourceLocation, Resource> entry : resources.entrySet()) {
                ResourceLocation sourceId = entry.getKey();
                // goety:brazier 是配方类型，不限定命名空间；生成 ID 带上来源命名空间避免冲突。
                String sourcePath = normalizeSourcePath(sourceId);
                String recipePath = sourceId.getNamespace() + "_"
                        + sourcePath.replace('/', '_');
                ResourceLocation generatedId = new ResourceLocation(
                        GoetyDelight.MODID, "apocalyptium_brazier/" + recipePath);

                if (ids.contains(generatedId) || generatedBrazierIds.contains(generatedId)) {
                    continue;
                }

                try (InputStreamReader reader = new InputStreamReader(
                        entry.getValue().open(), StandardCharsets.UTF_8)) {
                    JsonElement root = JsonParser.parseReader(reader);
                    if (!root.isJsonObject()) {
                        continue;
                    }
                    JsonObject json = root.getAsJsonObject();
                    if (!"goety:brazier".equals(GsonHelper.getAsString(json, "type", ""))) {
                        continue;
                    }

                    JsonArray ingredientArray = GsonHelper.getAsJsonArray(json, "ingredients", null);
                    if (ingredientArray == null || ingredientArray.size() == 0
                            || ingredientArray.size() > CookingPotRecipe.INPUT_SLOTS) {
                        continue;
                    }

                    NonNullList<Ingredient> ingredients =
                            NonNullList.withSize(ingredientArray.size(), Ingredient.EMPTY);
                    for (int i = 0; i < ingredientArray.size(); i++) {
                        ingredients.set(i, Ingredient.fromJson(ingredientArray.get(i)));
                    }

                    JsonObject resultJson = GsonHelper.getAsJsonObject(json, "result", null);
                    if (resultJson == null) {
                        continue;
                    }
                    ItemStack result = ShapedRecipe.itemStackFromJson(resultJson);
                    if (result.isEmpty()) {
                        continue;
                    }

                    int soulCost = GsonHelper.getAsInt(json, "soulCost", 1000);
                    int cookTime = Math.max(1, soulCost / 5);
                    float experience = soulCost / 2.0F;

                    recipes.add(new WorkstationCookingRecipe(
                            generatedId,
                            "brazier",
                            CookingPotRecipeBookTab.MISC,
                            ingredients,
                            result,
                            ItemStack.EMPTY,
                            experience,
                            cookTime,
                            WorkstationCookingRecipe.APOCALYPTIUM_COOKING_POT,
                            soulCost
                    ));
                    generatedBrazierIds.add(generatedId);
                    brazierSourceIds.add(new ResourceLocation(sourceId.getNamespace(), sourcePath));
                    changed = true;
                } catch (Exception exception) {
                    GoetyDelight.LOGGER.warn("[compat] 无法转换 brazier 配方 {}: {}",
                            sourceId, exception.toString());
                }
            }

        // 3) 兼容运行时注册的 goety:brazier 配方（包括其他模组以代码方式添加的配方）。
        //    仅扫描 JSON 会漏掉这类配方，因此再从 RecipeManager 中读取实际的 BrazierRecipe。
        for (Recipe<?> existing : recipeManager.getRecipes()) {
            if (!(existing instanceof BrazierRecipe brazierRecipe)) {
                continue;
            }

            ResourceLocation sourceId = brazierRecipe.getId();
            String sourcePath = normalizeSourcePath(sourceId);
            String recipePath = sourceId.getNamespace() + "_"
                    + sourcePath.replace('/', '_');
            ResourceLocation generatedId = new ResourceLocation(
                    GoetyDelight.MODID, "apocalyptium_brazier/" + recipePath);
            ResourceLocation normalizedSourceId = new ResourceLocation(
                    sourceId.getNamespace(), sourcePath);

            if (brazierSourceIds.contains(normalizedSourceId)
                    || ids.contains(generatedId)
                    || generatedBrazierIds.contains(generatedId)) {
                continue;
            }

            NonNullList<Ingredient> ingredients = brazierRecipe.getIngredients();
            if (ingredients.isEmpty() || ingredients.size() > CookingPotRecipe.INPUT_SLOTS) {
                continue;
            }
            ItemStack result = brazierRecipe.getResultItem(registryAccess).copy();
            if (result.isEmpty()) {
                continue;
            }

            int soulCost = Math.max(1, brazierRecipe.getSoulCost());
            int cookTime = Math.max(1, soulCost / 5);
            float experience = soulCost / 2.0F;

            recipes.add(new WorkstationCookingRecipe(
                    generatedId,
                    "brazier",
                    CookingPotRecipeBookTab.MISC,
                    ingredients,
                    result,
                    ItemStack.EMPTY,
                    experience,
                    cookTime,
                    WorkstationCookingRecipe.APOCALYPTIUM_COOKING_POT,
                    soulCost
            ));
            generatedBrazierIds.add(generatedId);
            brazierSourceIds.add(normalizedSourceId);
            changed = true;
        }

        if (changed) {
            recipeManager.replaceRecipes(recipes);
            GoetyDelight.LOGGER.info("[compat] 已转换 {} 个特殊烹饪配方，生成 {} 个 brazier 配方",
                    converted.size(), generatedBrazierIds.size());
        }
    }

    /**
     * 判断一个普通烹饪配方需要哪个锅。天启材料优先于 unholy_blood，
     * 因为天启锅是诅咒金属锅的升级版本。
     */
    /** 统一 datapack resource id（可能带 recipes/ 前缀和 .json 后缀）与运行时 Recipe id。 */
    private static String normalizeSourcePath(ResourceLocation sourceId) {
        String path = sourceId.getPath();
        if (path.startsWith("recipes/")) {
            path = path.substring("recipes/".length());
        }
        if (path.endsWith(".json")) {
            path = path.substring(0, path.length() - 5);
        }
        return path;
    }

    private static ResourceLocation requiredWorkstation(CookingPotRecipe recipe) {        boolean hasApocalyptium = false;
        boolean hasUnholyBlood = false;

        for (Ingredient ingredient : recipe.getIngredients()) {
            for (ItemStack stack : ingredient.getItems()) {
                ResourceLocation itemId = ForgeRegistries.ITEMS.getKey(stack.getItem());
                if (itemId == null) {
                    continue;
                }
                if (APOCALYPTIUM_INGOT.equals(itemId) || DOOM_MEDAL.equals(itemId)) {
                    hasApocalyptium = true;
                } else if (UNHOLY_BLOOD.equals(itemId)) {
                    hasUnholyBlood = true;
                }
            }
        }

        if (hasApocalyptium) {
            return WorkstationCookingRecipe.APOCALYPTIUM_COOKING_POT;
        }
        if (hasUnholyBlood) {
            return WorkstationCookingRecipe.CURSED_INGOT_POT;
        }
        return null;
    }
}
