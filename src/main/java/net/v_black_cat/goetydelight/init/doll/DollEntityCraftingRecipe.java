package net.v_black_cat.goetydelight.init.doll;

import net.minecraft.core.HolderLookup;
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
                    // 检查是否是有效的 custom doll
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

        // 先找到玩偶物品
        for (int i = 0; i < input.size(); i++) {
            ItemStack stack = input.getItem(i);
            if (!stack.isEmpty()) {
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
        }

        if (dollItemStack.isEmpty()) {
            return ItemStack.EMPTY;
        }

        // 创建 DollEntityItem
        if (isCustomDoll) {
            String modelId = CustomDollItem.getModelId(dollItemStack);
            return DollEntityItem.createItemWithCustomDollId(modelId);
        } else if (dollItemStack.getItem() instanceof DollItem dollItem) {
            return DollEntityItem.createItemWithBlockState(dollItem.getBlock().defaultBlockState());
        }

        return ItemStack.EMPTY;
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