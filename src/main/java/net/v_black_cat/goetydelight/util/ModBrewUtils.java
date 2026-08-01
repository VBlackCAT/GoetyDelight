package net.v_black_cat.goetydelight.util;

import com.Polarice3.Goety.common.effects.brew.BrewEffectInstance;
import com.Polarice3.Goety.utils.BrewUtils;
import com.Polarice3.Goety.utils.ModPotionUtil;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;

import java.util.ArrayList;
import java.util.List;

/**
 * 1.21.1 移植版：对 Goety 酿造瓶的负面效果进行强化（振幅 +1）。
 * 数据存储已从 NBT 键迁移到 DataComponents（PotionContents + CustomData），
 * 这里与 1.20.1 的行为保持一致：只加强负面效果，保留非负面效果。
 */
public class ModBrewUtils {

    public static void increaseNegativeEffects(ItemStack brewStack) {
        increaseNegativeEffects(brewStack, 3);
    }

    public static void increaseNegativeEffects(ItemStack brewStack, int maxAmplifier) {
        if (brewStack.isEmpty()) return;

        List<MobEffectInstance> mobEffects = ModPotionUtil.getMobEffects(brewStack);
        List<BrewEffectInstance> brewEffects = BrewUtils.getBrewEffects(brewStack);

        clearAllEffects(brewStack);

        List<MobEffectInstance> finalMobEffects = new ArrayList<>();
        List<BrewEffectInstance> finalBrewEffects = new ArrayList<>();

        for (MobEffectInstance effect : mobEffects) {
            if (effect != null) {
                if (effect.getEffect().value().getCategory() == MobEffectCategory.HARMFUL) {
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
                    finalMobEffects.add(effect);
                }
            }
        }

        if (brewEffects != null) {
            for (BrewEffectInstance effect : brewEffects) {
                if (effect != null) {
                    if (effect.getEffect().getCategory() == MobEffectCategory.HARMFUL) {
                        int newAmplifier = Math.min(effect.getAmplifier() + 1, maxAmplifier);
                        finalBrewEffects.add(new BrewEffectInstance(
                                effect.getEffect(),
                                effect.getDuration(),
                                newAmplifier
                        ));
                    } else {
                        finalBrewEffects.add(effect);
                    }
                }
            }
        }

        BrewUtils.setCustomEffects(brewStack, finalMobEffects, finalBrewEffects);
    }

    /**
     * 清除酿造瓶上的所有效果：PotionContents 组件 + CustomData 里的遗留键。
     */
    private static void clearAllEffects(ItemStack brewStack) {
        brewStack.remove(DataComponents.POTION_CONTENTS);
        CustomData.update(DataComponents.CUSTOM_DATA, brewStack, (CompoundTag tag) -> {
            tag.remove("CustomPotionEffects");
            tag.remove("CustomBrewEffects");
            tag.remove("Potion");
        });
    }
}
