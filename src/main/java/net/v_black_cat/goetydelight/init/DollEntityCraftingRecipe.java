package net.v_black_cat.goetydelight.init;

import net.minecraft.core.RegistryAccess;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.inventory.CraftingContainer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CraftingBookCategory;
import net.minecraft.world.item.crafting.CustomRecipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.level.Level;
import net.v_black_cat.goetydelight.item.CustomDollItem;
import net.v_black_cat.goetydelight.item.DollEntityItem;
import net.v_black_cat.goetydelight.item.DollItem;
import net.v_black_cat.goetydelight.item.ModItems;
import net.v_black_cat.goetydelight.recipe.ModRecipeSerializers;
import org.apache.commons.lang3.StringUtils;

public class DollEntityCraftingRecipe extends CustomRecipe {
    public DollEntityCraftingRecipe(ResourceLocation id, CraftingBookCategory category) {
        super(id, category);
    }

    @Override
    public boolean matches(CraftingContainer container, Level level) {
        boolean hasDollItem = false;
        boolean hasBlockToEntityItem = false;
        int itemCount = 0;

        for (int i = 0; i < container.getContainerSize(); i++) {
            ItemStack stack = container.getItem(i);
            if (!stack.isEmpty()) {
                itemCount++;
                if (stack.getItem() instanceof DollItem) {
                    hasDollItem = true;
                } else if (stack.is(ModItems.CUSTOM_DOLL.get())) {
                    String modelId = CustomDollItem.getModelId(stack);
                    hasDollItem = StringUtils.isNotBlank(modelId);
                } else {
                    return false;
                }
            }
        }

        return itemCount == 2 && hasDollItem && hasBlockToEntityItem;
    }

    @Override
    public ItemStack assemble(CraftingContainer container, RegistryAccess registryAccess) {
        ItemStack dollItemStack = ItemStack.EMPTY;
        boolean isCustomDoll = false;

        for (int i = 0; i < container.getContainerSize(); i++) {
            ItemStack stack = container.getItem(i);
            if (stack.getItem() instanceof DollItem) {
                dollItemStack = stack;
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
            DollItem dollItem = (DollItem) dollItemStack.getItem();
            return DollEntityItem.createItemWithBlockState(dollItem.getBlock().defaultBlockState());
        }
    }

    @Override
    public boolean canCraftInDimensions(int width, int height) {
        return width >= 2 && height >= 2;
    }

    @Override
    public RecipeSerializer<?> getSerializer() {
        return ModRecipeSerializers.DOLL_ENTITY_CRAFTING.get();
    }
}
