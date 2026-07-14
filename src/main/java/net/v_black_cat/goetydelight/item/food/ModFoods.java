package net.v_black_cat.goetydelight.item.food;

import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.food.FoodProperties;
import net.v_black_cat.goetydelight.init.ModEffects;

public class ModFoods {
    public static final FoodProperties TAINTED_DRINK;

    static {
        TAINTED_DRINK = new FoodProperties.Builder()
                .nutrition(4)
                .saturationModifier(1.0F)
                .alwaysEdible()
                .effect(() -> new MobEffectInstance(MobEffects.REGENERATION, 150, 1), 1.0F)
                .effect(() -> new MobEffectInstance(MobEffects.DAMAGE_RESISTANCE, 300, 1), 1.0F)
                .effect(() -> new MobEffectInstance(ModEffects.THE_PALE_MESSENGER, 3600, 0), 1.0F)
                .effect(() -> new MobEffectInstance(ModEffects.ZOMBIFIED_PIGLIN_BRUTE_SERVANT_SUPPORT, 7200, 0), 1.0F)
                .build();
    }
}
