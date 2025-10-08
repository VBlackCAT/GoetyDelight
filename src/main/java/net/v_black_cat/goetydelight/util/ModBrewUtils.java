package net.v_black_cat.goetydelight.util;

import com.Polarice3.Goety.common.effects.brew.BrewEffectInstance;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.alchemy.PotionUtils;

import java.util.ArrayList;
import java.util.List;

import static com.Polarice3.Goety.utils.BrewUtils.getBrewEffects;
import static com.Polarice3.Goety.utils.BrewUtils.setCustomEffects;

public class ModBrewUtils {

    public static void increaseNegativeEffects(ItemStack brewStack) {
        increaseNegativeEffects(brewStack, 3);
    }

    public static void increaseNegativeEffects(ItemStack brewStack, int maxAmplifier) {
        if (brewStack.isEmpty()) return;
        List<MobEffectInstance> mobEffects = PotionUtils.getMobEffects(brewStack);
        List<BrewEffectInstance> brewEffects = getBrewEffects(brewStack);
        // 清除所有现有效果
        clearAllEffects(brewStack);

        // 创建处理后的效果列表
        List<MobEffectInstance> finalMobEffects = new ArrayList<>();
        List<BrewEffectInstance> finalBrewEffects = new ArrayList<>();

        // 处理原版药水效果
        for (MobEffectInstance effect : mobEffects) {
            if (effect != null) {
                if (effect.getEffect().getCategory() == MobEffectCategory.HARMFUL) {
                    // 增强负面效果
                    int newAmplifier = Math.min(effect.getAmplifier() + 1, maxAmplifier);
                    finalMobEffects.add(new MobEffectInstance(
                            effect.getEffect(),
                            effect.getDuration(),
                            newAmplifier,
                            effect.isAmbient(),
                            effect.isVisible(),
                            effect.showIcon()
                    ));
                } else {
                    // 保留非负面效果
                    finalMobEffects.add(effect);
                }
            }
        }

        // 处理Brew效果
        if (brewEffects != null) {
            for (BrewEffectInstance effect : brewEffects) {
                if (effect != null) {
                    if (effect.getEffect().getCategory() == MobEffectCategory.HARMFUL) {
                        // 增强负面效果
                        int newAmplifier = Math.min(effect.getAmplifier() + 1, maxAmplifier);
                        finalBrewEffects.add(new BrewEffectInstance(
                                effect.getEffect(),
                                effect.getDuration(),
                                newAmplifier
                        ));
                    } else {
                        // 保留非负面效果
                        finalBrewEffects.add(effect);
                    }
                }
            }
        }

        // 设置最终效果到物品
        setCustomEffects(brewStack, finalMobEffects, finalBrewEffects);
    }

    // 新增方法：清除所有效果
    private static void clearAllEffects(ItemStack brewStack) {
        CompoundTag tag = brewStack.getOrCreateTag();

        // 清除原版药水效果
        tag.remove("CustomPotionEffects");

        // 清除Brew效果
        tag.remove("CustomBrewEffects");


        tag.remove("Potion");
    }
}