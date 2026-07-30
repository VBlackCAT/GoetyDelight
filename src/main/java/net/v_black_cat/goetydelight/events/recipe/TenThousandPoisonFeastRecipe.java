package net.v_black_cat.goetydelight.recipe;

import net.minecraft.core.HolderLookup;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.alchemy.PotionContents;
import net.minecraft.world.item.component.SuspiciousStewEffects;
import net.minecraft.world.item.crafting.CraftingBookCategory;
import net.minecraft.world.item.crafting.CraftingInput;
import net.minecraft.world.item.crafting.CustomRecipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.level.Level;
import net.v_black_cat.goetydelight.init.ModConfig;
import net.v_black_cat.goetydelight.init.ModItems;
import net.v_black_cat.goetydelight.init.ModRecipeSerializers;
import org.jetbrains.annotations.NotNull;

import java.util.HashSet;
import java.util.Set;

public class TenThousandPoisonFeastRecipe extends CustomRecipe {

    public TenThousandPoisonFeastRecipe(CraftingBookCategory category) {
        super(category);
    }

    @Override
    public boolean matches(@NotNull CraftingInput input, @NotNull Level level) {
        Set<ResourceLocation> debuffIds = new HashSet<>();
        for (int i = 0; i < input.size(); i++) {
            ItemStack stack = input.getItem(i);
            if (!stack.isEmpty()) {
                collectDebuffs(stack, debuffIds);
            }
        }
        return debuffIds.size() >= ModConfig.getTenThousandPoisonFeastMinDebuffCount();
    }

    @Override
    public @NotNull ItemStack assemble(@NotNull CraftingInput input, @NotNull HolderLookup.Provider provider) {
        return new ItemStack(ModItems.TEN_THOUSAND_POISON_FEAST.get());
    }

    @Override
    public boolean canCraftInDimensions(int width, int height) {
        return width * height >= ModConfig.getTenThousandPoisonFeastMinItemCount();
    }

    @Override
    public @NotNull RecipeSerializer<?> getSerializer() {
        return ModRecipeSerializers.TEN_THOUSAND_POISON_FEAST.get();
    }

    // ==================== 效果收集 ====================

    private static void collectDebuffs(ItemStack stack, Set<ResourceLocation> debuffIds) {
        // 1. 食物属性效果
        var food = stack.getItem().getFoodProperties(stack, null);
        if (food != null) {
            for (var possibleEffect : food.effects()) {
                MobEffectInstance inst = possibleEffect.effect();
                MobEffect effect = inst.getEffect().value();
                if (effect.getCategory() == MobEffectCategory.HARMFUL) {
                    ResourceLocation id = BuiltInRegistries.MOB_EFFECT.getKey(effect);
                    if (id != null) debuffIds.add(id);
                }
            }
        }

        // 2. 药水效果
        PotionContents potionContents = stack.get(DataComponents.POTION_CONTENTS);
        if (potionContents != null) {
            // 自定义附加效果
            for (MobEffectInstance inst : potionContents.customEffects()) {
                MobEffect effect = inst.getEffect().value();
                if (effect.getCategory() == MobEffectCategory.HARMFUL) {
                    ResourceLocation id = BuiltInRegistries.MOB_EFFECT.getKey(effect);
                    if (id != null) debuffIds.add(id);
                }
            }
            // 药水基础效果
            potionContents.potion().ifPresent(potionHolder -> {
                for (MobEffectInstance inst : potionHolder.value().getEffects()) {
                    MobEffect effect = inst.getEffect().value();
                    if (effect.getCategory() == MobEffectCategory.HARMFUL) {
                        ResourceLocation id = BuiltInRegistries.MOB_EFFECT.getKey(effect);
                        if (id != null) debuffIds.add(id);
                    }
                }
            });
        }

        // 3. 谜之炖菜效果（使用 var 避免内部类名问题）
        SuspiciousStewEffects stewEffects = stack.get(DataComponents.SUSPICIOUS_STEW_EFFECTS);
        if (stewEffects != null) {
            for (var entry : stewEffects.effects()) {   //自动推断为 SuspiciousStewEffects.Entry
                MobEffect effect = entry.effect().value();
                if (effect.getCategory() == MobEffectCategory.HARMFUL) {
                    ResourceLocation id = BuiltInRegistries.MOB_EFFECT.getKey(effect);
                    if (id != null) debuffIds.add(id);
                }
            }
        }
    }
}