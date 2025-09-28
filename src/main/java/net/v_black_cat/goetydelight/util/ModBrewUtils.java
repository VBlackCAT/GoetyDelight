package net.v_black_cat.goetydelight.util;

import com.Polarice3.Goety.common.effects.brew.BrewEffectInstance;
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
        // 获取所有效果
        List<MobEffectInstance> mobEffects = PotionUtils.getMobEffects(brewStack);
        List<BrewEffectInstance> brewEffects = getBrewEffects(brewStack);

        // 创建新的效果列表
        List<MobEffectInstance> newMobEffects = new ArrayList<>();
        List<BrewEffectInstance> newBrewEffects = new ArrayList<>();

        // 处理原版负面效果
        for (MobEffectInstance effect : mobEffects) {
            if (effect.getEffect().getCategory() == MobEffectCategory.HARMFUL) {
                // 提高负面效果等级（不超过最大等级3）
                int newAmplifier = Math.min(effect.getAmplifier() + 1, 3);
                newMobEffects.add(new MobEffectInstance(
                        effect.getEffect(),
                        effect.getDuration(),
                        newAmplifier,
                        effect.isAmbient(),
                        effect.isVisible(),
                        effect.showIcon()
                ));
            } else {
                newMobEffects.add(effect);
            }
        }

        // 处理自定义负面效果
        for (BrewEffectInstance effect : brewEffects) {
            if (effect.getEffect().getCategory()==MobEffectCategory.HARMFUL) { // 假设BrewEffect有一个isHarmful方法
                int newAmplifier = Math.min(effect.getAmplifier() + 1, 3);
                newBrewEffects.add(new BrewEffectInstance(
                        effect.getEffect(),
                        effect.getDuration(),
                        newAmplifier
                ));
            } else {
                newBrewEffects.add(effect);
            }
        }

        // 更新物品的效果
        setCustomEffects(brewStack, newMobEffects, newBrewEffects);
    }
}
