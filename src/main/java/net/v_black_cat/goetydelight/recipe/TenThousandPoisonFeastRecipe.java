package net.v_black_cat.goetydelight.recipe;

import net.minecraft.core.NonNullList;
import net.minecraft.core.RegistryAccess;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.inventory.CraftingContainer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.SuspiciousStewItem;
import net.minecraft.world.item.crafting.CraftingBookCategory;
import net.minecraft.world.item.crafting.CustomRecipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.level.Level;
import net.minecraftforge.registries.ForgeRegistries;
import net.v_black_cat.goetydelight.GoetyDelight;
import net.v_black_cat.goetydelight.config.Config;
import net.v_black_cat.goetydelight.item.ModItems;

import java.util.*;

public class TenThousandPoisonFeastRecipe extends CustomRecipe {

    public TenThousandPoisonFeastRecipe(ResourceLocation id, CraftingBookCategory category) {
        super(id, category);
    }

    @Override
    public boolean matches(CraftingContainer container, Level level) {
        if (level.isClientSide) {
            return false;
        }

        List<ItemStack> inputItems = new ArrayList<>();

        // 收集所有非空物品
        for (int i = 0; i < container.getContainerSize(); i++) {
            ItemStack stack = container.getItem(i);
            if (!stack.isEmpty()) {
                inputItems.add(stack);
            }
        }

        // 检查物品数量是否达到最小要求
        int minItemCount = Config.getTenThousandPoisonFeastMinItemCount();
        if (inputItems.size() < minItemCount) {
            return false;
        }

        // 计算所有物品提供的debuff总数
        int totalDebuffCount = countAllDebuffs(inputItems);
        int minDebuffCount = Config.getTenThousandPoisonFeastMinDebuffCount();
        return totalDebuffCount >= minDebuffCount;
    }

    @Override
    public ItemStack assemble(CraftingContainer container, RegistryAccess registryAccess) {
        return new ItemStack(ModItems.TEN_THOUSAND_POISON_FEAST.get());
    }

    @Override
    public boolean canCraftInDimensions(int width, int height) {
        return width * height >= Config.getTenThousandPoisonFeastMinItemCount();
    }

    @Override
    public RecipeSerializer<?> getSerializer() {
        return ModRecipeSerializers.TEN_THOUSAND_POISON_FEAST.get();
    }

    /**
     * 计算所有输入物品提供的所有debuff总数
     * 支持普通食物、谜之炖菜、药水等特殊物品
     */
    private static int countAllDebuffs(List<ItemStack> items) {
        Set<ResourceLocation> uniqueDebuffs = new HashSet<>();

        for (ItemStack stack : items) {
            collectDebuffsFromFoodProperties(stack, uniqueDebuffs);
            collectDebuffsFromNBT(stack, uniqueDebuffs);
            collectDebuffsFromSpecialItems(stack, uniqueDebuffs);
        }
        return uniqueDebuffs.size();
    }

    /**
     * 从FoodProperties中收集debuff
     */
    private static void collectDebuffsFromFoodProperties(ItemStack stack, Set<ResourceLocation> debuffs) {
        var foodProperties = stack.getItem().getFoodProperties();
        if (foodProperties != null) {
            foodProperties.getEffects().forEach(pair -> {
                MobEffect effect = pair.getFirst().getEffect();
                if (effect.getCategory() == MobEffectCategory.HARMFUL) {
                    ResourceLocation effectId = ForgeRegistries.MOB_EFFECTS.getKey(effect);
                    if (effectId != null) {
                        debuffs.add(effectId);
                    }
                }
            });
        }
    }

    /**
     * 从NBT中收集debuff（谜之炖菜、药水等）
     */
    private static void collectDebuffsFromNBT(ItemStack stack, Set<ResourceLocation> debuffs) {
        CompoundTag tag = stack.getTag();
        if (tag == null) return;

        if (tag.contains("Effects")) {
            ListTag effectsList = tag.getList("Effects", 10); //
            for (int i = 0; i < effectsList.size(); i++) {
                CompoundTag effectTag = effectsList.getCompound(i);
                if (effectTag.contains("EffectId") || effectTag.contains("id")) {
                    String effectIdStr = effectTag.contains("EffectId") ?
                            effectTag.getString("EffectId") :
                            effectTag.getString("id");

                    ResourceLocation effectId = ResourceLocation.tryParse(effectIdStr);
                    if (effectId != null) {
                        MobEffect effect = ForgeRegistries.MOB_EFFECTS.getValue(effectId);
                        if (effect != null && effect.getCategory() == MobEffectCategory.HARMFUL) {
                            debuffs.add(effectId);
                        }
                    }
                }
            }
        }

        if (tag.contains("Potion")) {
            String potionId = tag.getString("Potion");
            var potionEffects = net.minecraft.world.item.alchemy.PotionUtils.getMobEffects(stack);
            for (var effectInstance : potionEffects) {
                MobEffect effect = effectInstance.getEffect();
                if (effect.getCategory() == MobEffectCategory.HARMFUL) {
                    ResourceLocation effectId = ForgeRegistries.MOB_EFFECTS.getKey(effect);
                    if (effectId != null) {
                        debuffs.add(effectId);
                    }
                }
            }
        }

        if (tag.contains("PotionEffects")) {
            ListTag customEffects = tag.getList("PotionEffects", 10);
            for (int i = 0; i < customEffects.size(); i++) {
                CompoundTag effectTag = customEffects.getCompound(i);
                if (effectTag.contains("Id")) {
                    ResourceLocation effectId = ResourceLocation.tryParse(effectTag.getString("Id"));
                    if (effectId != null) {
                        MobEffect effect = ForgeRegistries.MOB_EFFECTS.getValue(effectId);
                        if (effect != null && effect.getCategory() == MobEffectCategory.HARMFUL) {
                            debuffs.add(effectId);
                        }
                    }
                }
            }
        }
        if (stack.getItem() instanceof SuspiciousStewItem) {
            collectDebuffsFromSuspiciousStew(stack, debuffs);
        }
    }

    /**
     * 专门处理谜之炖菜
     */
    private static void collectDebuffsFromSuspiciousStew(ItemStack stack, Set<ResourceLocation> debuffs) {
        CompoundTag tag = stack.getTag();
        if (tag == null) return;

        if (tag.contains("suspicious_stew_effects")) {
            ListTag stewEffects = tag.getList("suspicious_stew_effects", 10);
            for (int i = 0; i < stewEffects.size(); i++) {
                CompoundTag effectTag = stewEffects.getCompound(i);
                if (effectTag.contains("id")) {
                    String effectIdStr = effectTag.getString("id");
                    ResourceLocation effectId = ResourceLocation.tryParse(effectIdStr);
                    if (effectId != null) {
                        MobEffect effect = ForgeRegistries.MOB_EFFECTS.getValue(effectId);
                        if (effect != null && effect.getCategory() == MobEffectCategory.HARMFUL) {
                            debuffs.add(effectId);
                        }
                    }
                }
            }
        }

        // 旧版谜之炖菜效果存储（通过Effects标签）
        if (tag.contains("Effects")) {
            ListTag effectsList = tag.getList("Effects", 10);
            for (int i = 0; i < effectsList.size(); i++) {
                CompoundTag effectTag = effectsList.getCompound(i);
                if (effectTag.contains("EffectId")) {
                    byte effectIdByte = effectTag.getByte("EffectId");
                    // 旧版使用数字ID，需要转换
                    MobEffect effect = net.minecraft.world.effect.MobEffect.byId(effectIdByte);
                    if (effect != null && effect.getCategory() == MobEffectCategory.HARMFUL) {
                        ResourceLocation effectId = ForgeRegistries.MOB_EFFECTS.getKey(effect);
                        if (effectId != null) {
                            debuffs.add(effectId);
                        }
                    }
                }
            }
        }
    }

    /**
     * 处理其他特殊物品
     */
    private static void collectDebuffsFromSpecialItems(ItemStack stack, Set<ResourceLocation> debuffs) {
        // 处理药水类物品
        if (stack.getItem() instanceof net.minecraft.world.item.PotionItem ||
                stack.getItem() instanceof net.minecraft.world.item.LingeringPotionItem ||
                stack.getItem() instanceof net.minecraft.world.item.SplashPotionItem) {

            var potionEffects = net.minecraft.world.item.alchemy.PotionUtils.getMobEffects(stack);
            for (var effectInstance : potionEffects) {
                MobEffect effect = effectInstance.getEffect();
                if (effect.getCategory() == MobEffectCategory.HARMFUL) {
                    ResourceLocation effectId = ForgeRegistries.MOB_EFFECTS.getKey(effect);
                    if (effectId != null) {
                        debuffs.add(effectId);
                    }
                }
            }
        }

        // 处理 tipped arrow
        if (stack.getItem() instanceof net.minecraft.world.item.TippedArrowItem) {
            var potionEffects = net.minecraft.world.item.alchemy.PotionUtils.getMobEffects(stack);
            for (var effectInstance : potionEffects) {
                MobEffect effect = effectInstance.getEffect();
                if (effect.getCategory() == MobEffectCategory.HARMFUL) {
                    ResourceLocation effectId = ForgeRegistries.MOB_EFFECTS.getKey(effect);
                    if (effectId != null) {
                        debuffs.add(effectId);
                    }
                }
            }
        }
    }

    @Override
    public NonNullList<ItemStack> getRemainingItems(CraftingContainer container) {
        NonNullList<ItemStack> remainingItems = NonNullList.withSize(container.getContainerSize(), ItemStack.EMPTY);

        for (int i = 0; i < remainingItems.size(); i++) {
            ItemStack stack = container.getItem(i);
            if (stack.hasCraftingRemainingItem()) {
                remainingItems.set(i, stack.getCraftingRemainingItem());
            } else if (stack.getItem().getCraftingRemainingItem() != null) {
                remainingItems.set(i, new ItemStack(stack.getItem().getCraftingRemainingItem()));
            }
        }
        return remainingItems;
    }
}