package net.v_black_cat.goetydelight.effect;

import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.util.Mth;

public class HydrationEffect extends MobEffect {

    private static final int WATER_COLOR = 0x3F76E4;

    public HydrationEffect() {
        super(MobEffectCategory.BENEFICIAL, WATER_COLOR);
    }

    @Override
    public void applyEffectTick(LivingEntity entity, int amplifier) {
        Level world = entity.level();


        boolean inWater = entity.isInWater() || entity.level().getFluidState(entity.blockPosition()).is(Fluids.FLOWING_WATER);
        boolean inRain = world.isRainingAt(entity.blockPosition());

        if ((inWater || inRain) && entity.getHealth() < entity.getMaxHealth()) {

            entity.heal(1.0F);
        }
    }

    @Override
    public boolean isDurationEffectTick(int duration, int amplifier) {

        int interval = 50 >> amplifier;
        if (interval <= 0) {
            interval = 1;
        }
        return duration % interval == 0;
    }
}