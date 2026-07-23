package net.v_black_cat.goetydelight.effect;

import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import net.neoforged.neoforge.event.entity.living.MobEffectEvent;

public class CrimsonMemoriesEffect extends MobEffect {
    public CrimsonMemoriesEffect() {
        super(MobEffectCategory.BENEFICIAL, 0xFF0000);
    }

    public static void onEffectApplied(MobEffectEvent.Added event) {
        LivingEntity entity = event.getEntity();
        MobEffectInstance effectInstance = event.getEffectInstance();
        if (effectInstance.getEffect() instanceof CrimsonMemoriesEffect && !entity.level().isClientSide()) {
            int duration = effectInstance.getDuration();
//            TimedAbilitySystem.addAbilityToEntity(entity, AbilityRegistry.CRIMSON_MEMORIES, duration);
        }
    }
}