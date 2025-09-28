package net.v_black_cat.goetydelight.recipe;

import com.Polarice3.Goety.common.effects.brew.BrewEffectInstance;
import com.Polarice3.Goety.common.items.brew.BrewItem;
import com.google.gson.JsonObject;
import net.minecraft.core.RegistryAccess;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.inventory.CraftingContainer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.alchemy.PotionUtils;
import net.minecraft.world.item.crafting.CraftingBookCategory;
import net.minecraft.world.item.crafting.CustomRecipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.level.Level;
import net.v_black_cat.goetydelight.item.ModItems;
import net.v_black_cat.goetydelight.util.ModBrewUtils;

import java.util.List;

import static com.Polarice3.Goety.common.items.ModItems.BREW;
import static com.Polarice3.Goety.utils.BrewUtils.getBrewEffects;

public class PotionAmplifierRecipe extends CustomRecipe {
    public PotionAmplifierRecipe(ResourceLocation pId, CraftingBookCategory pCategory) {
        super(pId, pCategory);
    }

    @Override
    public boolean matches(CraftingContainer pContainer, Level pLevel) {
        boolean hasWitchBrew = false;
        boolean hasAmplifier = false;

        for (int i = 0; i < pContainer.getContainerSize(); i++) {
            ItemStack stack = pContainer.getItem(i);
            if (stack.isEmpty()) continue;

            // 只检查女巫精酿（BrewItem），不再检查原版药水
            if (stack.getItem() == BREW.get()) {
                // 检查女巫精酿是否有负面效果
                if (hasNegativeEffects(stack)) {
                    hasWitchBrew = true;
                }
            } else if (stack.getItem() == ModItems.REJECTED_DARK_MEAT_SOUP.get()) {
                hasAmplifier = true;
            } else {
                // 如果有其他非空物品，则不匹配
                return false;
            }
        }

        return hasWitchBrew && hasAmplifier;
    }

    @Override
    public ItemStack assemble(CraftingContainer pContainer, RegistryAccess pRegistryAccess) {
        ItemStack brewStack = ItemStack.EMPTY;

        // 找到女巫精酿堆栈
        for (int i = 0; i < pContainer.getContainerSize(); i++) {
            ItemStack stack = pContainer.getItem(i);
            if (stack.getItem() == BREW.get()) {
                brewStack = stack;
                break;
            }
        }

        if (brewStack.isEmpty()) return ItemStack.EMPTY;

        // 创建新的女巫精酿堆栈，并增强负面效果
        ItemStack result = brewStack.copy();
        result.setCount(1);

        // 使用ModBrewUtils增强负面效果（包括原版和自定义效果）
        ModBrewUtils.increaseNegativeEffects(result);

        return result;
    }

    @Override
    public boolean canCraftInDimensions(int pWidth, int pHeight) {
        return pWidth * pHeight >= 2;
    }

    @Override
    public RecipeSerializer<?> getSerializer() {
        return ModRecipeSerializers.POTION_AMPLIFIER.get();
    }

    // 检查女巫精酿是否有负面效果（包括原版效果和自定义效果）
    private boolean hasNegativeEffects(ItemStack brewStack) {
        // 检查原版负面效果
        boolean hasNegativeMobEffects = PotionUtils.getMobEffects(brewStack).stream()
                .anyMatch(effect -> effect.getEffect().getCategory() == MobEffectCategory.HARMFUL);

        // 检查自定义负面效果
        boolean hasNegativeBrewEffects = false;
        List<BrewEffectInstance> brewEffects = getBrewEffects(brewStack);
        if (brewEffects != null) {
            hasNegativeBrewEffects = brewEffects.stream()
                    .anyMatch(effect -> effect.getEffect().getCategory() == MobEffectCategory.HARMFUL);
        }

        return hasNegativeMobEffects || hasNegativeBrewEffects;
    }

    public static class Serializer implements RecipeSerializer<PotionAmplifierRecipe> {
        @Override
        public PotionAmplifierRecipe fromJson(ResourceLocation pRecipeId, JsonObject pSerializedRecipe) {
            CraftingBookCategory category = CraftingBookCategory.CODEC.byName(
                    GsonHelper.getAsString(pSerializedRecipe, "category", null), CraftingBookCategory.MISC);
            return new PotionAmplifierRecipe(pRecipeId, category);
        }

        @Override
        public PotionAmplifierRecipe fromNetwork(ResourceLocation pRecipeId, FriendlyByteBuf pBuffer) {
            CraftingBookCategory category = pBuffer.readEnum(CraftingBookCategory.class);
            return new PotionAmplifierRecipe(pRecipeId, category);
        }

        @Override
        public void toNetwork(FriendlyByteBuf pBuffer, PotionAmplifierRecipe pRecipe) {
            pBuffer.writeEnum(pRecipe.category());
        }
    }
}