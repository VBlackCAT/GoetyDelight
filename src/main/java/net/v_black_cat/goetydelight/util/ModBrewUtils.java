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
        increaseNegativeEffects(brewStack, 3); 
    }

    
    public static void increaseNegativeEffects(ItemStack brewStack, int maxAmplifier) {
        if (brewStack.isEmpty()) return;

        
        List<MobEffectInstance> mobEffects = PotionUtils.getMobEffects(brewStack);
        List<BrewEffectInstance> brewEffects = getBrewEffects(brewStack);

        
        List<MobEffectInstance> newMobEffects = new ArrayList<>();
        List<BrewEffectInstance> newBrewEffects = new ArrayList<>();

        
        for (MobEffectInstance effect : mobEffects) {
            if (effect != null && effect.getEffect().getCategory() == MobEffectCategory.HARMFUL) {
                
                int newAmplifier = Math.min(effect.getAmplifier() + 1, maxAmplifier);
                newMobEffects.add(new MobEffectInstance(
                        effect.getEffect(),
                        effect.getDuration(),
                        newAmplifier,
                        effect.isAmbient(),
                        effect.isVisible(),
                        effect.showIcon()
                ));
            } else if (effect != null) {
                
                newMobEffects.add(effect);
            }
        }

        
        if (brewEffects != null) {
            for (BrewEffectInstance effect : brewEffects) {
                if (effect != null && effect.getEffect().getCategory() == MobEffectCategory.HARMFUL) {
                    int newAmplifier = Math.min(effect.getAmplifier() + 1, maxAmplifier);
                    newBrewEffects.add(new BrewEffectInstance(
                            effect.getEffect(),
                            effect.getDuration(),
                            newAmplifier
                    ));
                } else if (effect != null) {
                    
                    newBrewEffects.add(effect);
                }
            }
        }

        
        setCustomEffects(brewStack, newMobEffects, newBrewEffects);
    }
}