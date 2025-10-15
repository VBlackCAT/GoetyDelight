package net.v_black_cat.goetydelight.mixin;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeManager;
import net.minecraftforge.common.crafting.conditions.ICondition;
import net.v_black_cat.goetydelight.item.ItemBlackList;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import javax.annotation.Nullable;

@Mixin(RecipeManager.class)
public class RecipeManagerMixin {

    @Inject(
            method = "fromJson(Lnet/minecraft/resources/ResourceLocation;Lcom/google/gson/JsonObject;Lnet/minecraftforge/common/crafting/conditions/ICondition$IContext;)Lnet/minecraft/world/item/crafting/Recipe;",
            at = @At("HEAD"),
            cancellable = true,
            remap = false
    )
    private static void skipBlacklistedRecipes(ResourceLocation recipeId, JsonObject json, ICondition.IContext context, CallbackInfoReturnable<Recipe<?>> cir) {
        if (isRecipeBlacklisted(json)) {
            cir.setReturnValue(null);
        }
    }

    
    private static boolean isRecipeBlacklisted(JsonObject json) {
        
        if (json.has("result")) {
            JsonElement resultElement = json.get("result");
            String resultItem = getItemIdFromJsonElement(resultElement);
            if (resultItem != null && ItemBlackList.isBlackListed(resultItem)) {
                return true;
            }

            
            if (resultElement.isJsonObject()) {
                JsonObject resultObj = resultElement.getAsJsonObject();
                if (resultObj.has("item")) {
                    String itemFromObj = getItemIdFromJsonElement(resultObj.get("item"));
                    if (itemFromObj != null && ItemBlackList.isBlackListed(itemFromObj)) {
                        return true;
                    }
                }
            }
        }

        
        if (json.has("ingredients") && json.get("ingredients").isJsonArray()) {
            JsonArray ingredientsArray = json.getAsJsonArray("ingredients");
            for (JsonElement ingredientElement : ingredientsArray) {
                String itemId = getItemIdFromJsonElement(ingredientElement);
                if (itemId != null && ItemBlackList.isBlackListed(itemId)) {
                    return true;
                }

                
                if (ingredientElement.isJsonObject()) {
                    JsonObject ingredientObj = ingredientElement.getAsJsonObject();
                    if (ingredientObj.has("item")) {
                        String nestedItem = getItemIdFromJsonElement(ingredientObj.get("item"));
                        if (nestedItem != null && ItemBlackList.isBlackListed(nestedItem)) {
                            return true;
                        }
                    }
                }
            }
        }

        
        if (json.has("ingredient")) {
            JsonElement ingredientElement = json.get("ingredient");
            String itemId = getItemIdFromJsonElement(ingredientElement);
            if (itemId != null && ItemBlackList.isBlackListed(itemId)) {
                return true;
            }

            
            if (ingredientElement.isJsonObject()) {
                JsonObject ingredientObj = ingredientElement.getAsJsonObject();
                if (ingredientObj.has("item")) {
                    String nestedItem = getItemIdFromJsonElement(ingredientObj.get("item"));
                    if (nestedItem != null && ItemBlackList.isBlackListed(nestedItem)) {
                        return true;
                    }
                }
            }
        }

        
        if (json.has("key") && json.get("key").isJsonObject()) {
            JsonObject keyObj = json.getAsJsonObject("key");
            for (String key : keyObj.keySet()) {
                JsonElement keyElement = keyObj.get(key);
                String itemId = getItemIdFromJsonElement(keyElement);
                if (itemId != null && ItemBlackList.isBlackListed(itemId)) {
                    return true;
                }
            }
        }

        
        if (json.has("ingredients") && json.get("ingredients").isJsonArray()) {
            JsonArray ingredientsArray = json.getAsJsonArray("ingredients");
            for (JsonElement ingredientElement : ingredientsArray) {
                String itemId = getItemIdFromJsonElement(ingredientElement);
                if (itemId != null && ItemBlackList.isBlackListed(itemId)) {
                    return true;
                }
            }
        }

        return false;
    }

    
    @Nullable
    private static String getItemIdFromJsonElement(JsonElement element) {
        if (element == null || element.isJsonNull()) {
            return null;
        }

        
        if (element.isJsonPrimitive() && element.getAsJsonPrimitive().isString()) {
            return element.getAsString();
        }

        
        if (element.isJsonObject()) {
            JsonObject jsonObj = element.getAsJsonObject();

            
            if (jsonObj.has("item") && jsonObj.get("item").isJsonPrimitive()) {
                return jsonObj.get("item").getAsString();
            }

            
            if (jsonObj.has("item") && jsonObj.get("item").isJsonObject()) {
                JsonObject nestedItem = jsonObj.getAsJsonObject("item");
                if (nestedItem.has("item") && nestedItem.get("item").isJsonPrimitive()) {
                    return nestedItem.get("item").getAsString();
                }
            }

            
            if (jsonObj.has("name") && jsonObj.get("name").isJsonPrimitive()) {
                return jsonObj.get("name").getAsString();
            }
        }

        return null;
    }
}