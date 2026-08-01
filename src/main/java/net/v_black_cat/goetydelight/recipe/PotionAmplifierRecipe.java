package net.v_black_cat.goetydelight.recipe;

import com.Polarice3.Goety.common.effects.brew.BrewEffectInstance;
import com.Polarice3.Goety.utils.ModPotionUtil;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CraftingBookCategory;
import net.minecraft.world.item.crafting.CraftingInput;
import net.minecraft.world.item.crafting.CustomRecipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.level.Level;
import net.v_black_cat.goetydelight.init.ModItems;
import net.v_black_cat.goetydelight.init.ModRecipeSerializers;
import net.v_black_cat.goetydelight.item.food.EternalRefusalOfBlackMeatSoupItem;
import net.v_black_cat.goetydelight.util.ModBrewUtils;

import java.util.List;

import static com.Polarice3.Goety.common.items.ModItems.BREW;
import static com.Polarice3.Goety.common.items.ModItems.GAS_BREW;
import static com.Polarice3.Goety.common.items.ModItems.LINGERING_BREW;
import static com.Polarice3.Goety.common.items.ModItems.SPLASH_BREW;
import static com.Polarice3.Goety.utils.BrewUtils.getBrewEffects;

/**
 * 1.21.1 移植版：将带负面效果的酿造瓶与"被拒黑肉汤/永恒黑肉汤"合成，
 * 每次合成把负面效果振幅 +1（最多 5 级），并在物品上累计 AmplifiedCount。
 */
public class PotionAmplifierRecipe extends CustomRecipe {
    public PotionAmplifierRecipe(CraftingBookCategory pCategory) {
        super(pCategory);
    }

    @Override
    public boolean matches(CraftingInput pContainer, Level pLevel) {
        boolean hasWitchBrew = false;
        boolean hasAmplifier = false;
        ItemStack brewStack = ItemStack.EMPTY;
        ItemStack amplifierStack = ItemStack.EMPTY;

        for (int i = 0; i < pContainer.size(); i++) {
            ItemStack stack = pContainer.getItem(i);
            if (stack.isEmpty()) continue;

            if (stack.getItem() == BREW.get() ||
                    stack.getItem() == SPLASH_BREW.get() ||
                    stack.getItem() == LINGERING_BREW.get() ||
                    stack.getItem() == GAS_BREW.get()) {
                if (hasNegativeEffects(stack)) {
                    hasWitchBrew = true;
                    brewStack = stack;
                }
            } else if (stack.getItem() == ModItems.REJECTED_DARK_MEAT_SOUP.get() ||
                    stack.getItem() == ModItems.CUP.get()) {
                if (stack.getItem() == ModItems.REJECTED_DARK_MEAT_SOUP.get()) {
                    amplifierStack = stack;
                    hasAmplifier = true;
                } else if (stack.getItem() instanceof EternalRefusalOfBlackMeatSoupItem cup) {
                    if (!cup.isOnCooldown(stack, pLevel)) {
                        amplifierStack = stack;
                        hasAmplifier = true;
                    }
                }
            } else {
                return false;
            }
        }

        if (hasWitchBrew && hasAmplifier) {
            int maxAmplifications = getMaxAmplifications(amplifierStack);
            int currentAmplifications = getCurrentAmplifications(brewStack);
            return currentAmplifications < maxAmplifications;
        }

        return false;
    }

    @Override
    public ItemStack assemble(CraftingInput pContainer, HolderLookup.Provider pRegistryAccess) {
        ItemStack brewStack = ItemStack.EMPTY;
        ItemStack amplifierStack = ItemStack.EMPTY;

        for (int i = 0; i < pContainer.size(); i++) {
            ItemStack stack = pContainer.getItem(i);
            if (stack.getItem() == BREW.get() ||
                    stack.getItem() == SPLASH_BREW.get() ||
                    stack.getItem() == LINGERING_BREW.get() ||
                    stack.getItem() == GAS_BREW.get()) {
                brewStack = stack;
            } else if (stack.getItem() == ModItems.REJECTED_DARK_MEAT_SOUP.get() ||
                    stack.getItem() == ModItems.CUP.get()) {
                amplifierStack = stack;
            }
        }

        if (brewStack.isEmpty() || amplifierStack.isEmpty()) return ItemStack.EMPTY;

        ItemStack result = brewStack.copy();
        result.setCount(1);

        int currentAmplifications = getCurrentAmplifications(result);
        setCurrentAmplifications(result, currentAmplifications + 1);

        ModBrewUtils.increaseNegativeEffects(result, 5);

        if (amplifierStack.getItem() instanceof EternalRefusalOfBlackMeatSoupItem) {
            CustomData.update(DataComponents.CUSTOM_DATA, result, tag -> tag.putBoolean("ReturnCooledSoup", true));
        }

        return result;
    }

    private int getCurrentAmplifications(ItemStack brewStack) {
        CompoundTag tag = brewStack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag();
        return tag.contains("AmplifiedCount") ? tag.getInt("AmplifiedCount") : 0;
    }

    private void setCurrentAmplifications(ItemStack brewStack, int count) {
        CustomData.update(DataComponents.CUSTOM_DATA, brewStack, tag -> tag.putInt("AmplifiedCount", count));
    }

    private int getMaxAmplifications(ItemStack amplifierStack) {
        if (amplifierStack.getItem() == ModItems.REJECTED_DARK_MEAT_SOUP.get()) {
            return 3;
        } else if (amplifierStack.getItem() == ModItems.CUP.get()) {
            return 5;
        }
        return 0;
    }

    @Override
    public boolean canCraftInDimensions(int pWidth, int pHeight) {
        return pWidth * pHeight >= 2;
    }

    @Override
    public RecipeSerializer<?> getSerializer() {
        return ModRecipeSerializers.POTION_AMPLIFIER.get();
    }

    private boolean hasNegativeEffects(ItemStack brewStack) {
        boolean hasNegativeMobEffects = ModPotionUtil.getMobEffects(brewStack).stream()
                .anyMatch(effect -> effect.getEffect().value().getCategory() == MobEffectCategory.HARMFUL);

        boolean hasNegativeBrewEffects = false;
        List<BrewEffectInstance> brewEffects = getBrewEffects(brewStack);
        if (brewEffects != null) {
            hasNegativeBrewEffects = brewEffects.stream()
                    .anyMatch(effect -> effect.getEffect().getCategory() == MobEffectCategory.HARMFUL);
        }

        return hasNegativeMobEffects || hasNegativeBrewEffects;
    }
}
