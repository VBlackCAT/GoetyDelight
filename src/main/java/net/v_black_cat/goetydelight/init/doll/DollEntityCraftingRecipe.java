package net.v_black_cat.goetydelight.init.doll;

import net.minecraft.core.HolderLookup;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.CraftingBookCategory;
import net.minecraft.world.item.crafting.CraftingInput;
import net.minecraft.world.item.crafting.CustomRecipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.level.Level;
import net.v_black_cat.goetydelight.init.ModItems;
import net.v_black_cat.goetydelight.init.ModRecipeSerializers;
import net.v_black_cat.goetydelight.item.CustomDollItem;
import net.v_black_cat.goetydelight.item.DollEntityItem;
import net.v_black_cat.goetydelight.item.DollItem;
import org.apache.commons.lang3.StringUtils;

public class DollEntityCraftingRecipe extends CustomRecipe {

    public DollEntityCraftingRecipe(CraftingBookCategory category) {
        super(category);
    }

    @Override
    public boolean matches(CraftingInput input, Level level) {
        boolean hasDollItem = false;
        boolean hasEctoplasm = false;
        boolean hasSlimeBall = false;
        int itemCount = 0;

        for (int i = 0; i < input.size(); i++) {
            ItemStack stack = input.getItem(i);
            if (!stack.isEmpty()) {
                itemCount++;
                if (stack.getItem() instanceof DollItem) {
                    hasDollItem = true;
                } else if (stack.is(ModItems.CUSTOM_DOLL.get())) {
                    String modelId = CustomDollItem.getModelId(stack);
                    if (StringUtils.isNotBlank(modelId)) {
                        hasDollItem = true;
                    } else {
                        return false;
                    }
                } else if (stack.is(com.Polarice3.Goety.common.items.ModItems.ECTOPLASM.get())) {
                    hasEctoplasm = true;
                } else if (stack.is(Items.SLIME_BALL)) {
                    hasSlimeBall = true;
                } else {
                    return false;
                }
            }
        }

        return itemCount == 3 && hasDollItem && hasEctoplasm && hasSlimeBall;
    }

    @Override
    public ItemStack assemble(CraftingInput input, HolderLookup.Provider registries) {
        ItemStack dollItemStack = ItemStack.EMPTY;
        boolean isCustomDoll = false;
        String extractedId = null;

        for (int i = 0; i < input.size(); i++) {
            ItemStack stack = input.getItem(i);
            if (stack.getItem() instanceof DollItem dollItem) {
                dollItemStack = stack;
                ResourceLocation registryName = net.minecraft.core.registries.BuiltInRegistries.ITEM.getKey(dollItem);
                if (registryName != null) {
                    extractedId = registryName.getPath();
                }
                break;
            } else if (stack.is(ModItems.CUSTOM_DOLL.get())) {
                String modelId = CustomDollItem.getModelId(stack);
                if (StringUtils.isNotBlank(modelId)) {
                    dollItemStack = stack;
                    isCustomDoll = true;
                    break;
                }
            }
        }

        if (dollItemStack.isEmpty()) {
            return ItemStack.EMPTY;
        }

        if (isCustomDoll) {
            String modelId = CustomDollItem.getModelId(dollItemStack);
            return DollEntityItem.createItemWithCustomDollId(modelId);
        } else {
            if (extractedId != null) {
                return DollEntityItem.createItemWithCustomDollId(extractedId);
            } else {
                DollItem dollItem = (DollItem) dollItemStack.getItem();
                return DollEntityItem.createItemWithBlockState(dollItem.getBlock().defaultBlockState());
            }
        }
    }

    @Override
    public boolean canCraftInDimensions(int width, int height) {
        return width * height >= 3;
    }

    @Override
    public RecipeSerializer<?> getSerializer() {
        return ModRecipeSerializers.DOLL_ENTITY_CRAFTING.get();
    }
}