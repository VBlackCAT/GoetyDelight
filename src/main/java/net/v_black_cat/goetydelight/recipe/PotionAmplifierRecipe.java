package net.v_black_cat.goetydelight.recipe;

import com.Polarice3.Goety.common.effects.brew.BrewEffectInstance;
import com.Polarice3.Goety.common.items.brew.BrewItem;
import com.google.gson.JsonObject;
import net.minecraft.client.Minecraft;
import net.minecraft.core.RegistryAccess;
import net.minecraft.nbt.CompoundTag;
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
import net.v_black_cat.goetydelight.item.food.EternalRefusalOfBlackMeatSoupItem;
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
        ItemStack brewStack = ItemStack.EMPTY;
        ItemStack amplifierStack = ItemStack.EMPTY;

        for (int i = 0; i < pContainer.getContainerSize(); i++) {
            ItemStack stack = pContainer.getItem(i);
            if (stack.isEmpty()) continue;

            if (stack.getItem() == BREW.get()) {
                if (hasNegativeEffects(stack)) {
                    hasWitchBrew = true;
                    brewStack = stack;
                }
            } else if (stack.getItem() == ModItems.REJECTED_DARK_MEAT_SOUP.get() ||
                    stack.getItem() == ModItems.CUP.get()) {
                if(stack.getItem() == ModItems.REJECTED_DARK_MEAT_SOUP.get()){
                    amplifierStack = stack;
                    hasAmplifier = true;
                }else if (stack.getItem() instanceof EternalRefusalOfBlackMeatSoupItem cup){
                    // 检查冷却状态
                    if (!cup.isOnCooldown(stack, pLevel)){
                        amplifierStack = stack;
                        hasAmplifier = true;
                    }
                }
            } else {
                return false;
            }
        }

        // 检查增强次数是否已满
        if (hasWitchBrew && hasAmplifier) {
            int maxAmplifications = getMaxAmplifications(amplifierStack);
            int currentAmplifications = getCurrentAmplifications(brewStack);

            return currentAmplifications < maxAmplifications;
        }

        return false;
    }

    @Override
    public ItemStack assemble(CraftingContainer pContainer, RegistryAccess pRegistryAccess) {
        ItemStack brewStack = ItemStack.EMPTY;
        ItemStack amplifierStack = ItemStack.EMPTY;

        // 获取药水和放大器
        for (int i = 0; i < pContainer.getContainerSize(); i++) {
            ItemStack stack = pContainer.getItem(i);
            if (stack.getItem() == BREW.get()) {
                brewStack = stack;
            } else if (stack.getItem() == ModItems.REJECTED_DARK_MEAT_SOUP.get() ||
                    stack.getItem() == ModItems.CUP.get()) {
                amplifierStack = stack;
            }
        }

        if (brewStack.isEmpty() || amplifierStack.isEmpty()) return ItemStack.EMPTY;

        // 创建增强后的药水
        ItemStack result = brewStack.copy();
        result.setCount(1);

        // 获取当前增强次数并增加
        int currentAmplifications = getCurrentAmplifications(result);
        setCurrentAmplifications(result, currentAmplifications + 1);

        // 增强负面效果
        ModBrewUtils.increaseNegativeEffects(result, 5);

        // 如果使用了永恒黑肉汤，标记需要返回冷却的汤，实际冷却处理在物品使用逻辑中
        if (amplifierStack.getItem() instanceof EternalRefusalOfBlackMeatSoupItem) {
            CompoundTag tag = result.getOrCreateTag();
            tag.putBoolean("ReturnCooledSoup", true);
        }

        return result;
    }

    // 获取当前增强次数
    private int getCurrentAmplifications(ItemStack brewStack) {
        CompoundTag tag = brewStack.getTag();
        if (tag != null && tag.contains("AmplifiedCount")) {
            return tag.getInt("AmplifiedCount");
        }
        return 0;
    }

    // 设置当前增强次数
    private void setCurrentAmplifications(ItemStack brewStack, int count) {
        CompoundTag tag = brewStack.getOrCreateTag();
        tag.putInt("AmplifiedCount", count);
    }

    // 根据放大器类型获取最大增强次数
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
        boolean hasNegativeMobEffects = PotionUtils.getMobEffects(brewStack).stream()
                .anyMatch(effect -> effect.getEffect().getCategory() == MobEffectCategory.HARMFUL);

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