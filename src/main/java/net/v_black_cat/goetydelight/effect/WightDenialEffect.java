package net.v_black_cat.goetydelight.effect;

import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;

public class WightDenialEffect extends MobEffect {

    protected WightDenialEffect(MobEffectCategory category, int color) {
        super(category, color);
    }
    protected WightDenialEffect() {
        super(MobEffectCategory.BENEFICIAL, 0x8B4513);
    }

}
