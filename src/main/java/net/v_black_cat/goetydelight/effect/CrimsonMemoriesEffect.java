package net.v_black_cat.goetydelight.effect;

import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.event.entity.living.MobEffectEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.v_black_cat.goetydelight.ability.AbilityRegistry;
import net.v_black_cat.goetydelight.ability.TimedAbilitySystem;

@Mod.EventBusSubscriber(modid = "goetydelight")
public class CrimsonMemoriesEffect extends MobEffect {
    public CrimsonMemoriesEffect() {
        super(MobEffectCategory.BENEFICIAL, 0xFF0000);
    }

    public CrimsonMemoriesEffect(MobEffectCategory category, int color) {
        super(category, color);
    }

    @SubscribeEvent
    public static void onEffectApplied(MobEffectEvent.Added event) {
        LivingEntity entity = event.getEntity();
        MobEffectInstance effectInstance = event.getEffectInstance();

        if (effectInstance.getEffect() instanceof CrimsonMemoriesEffect && !entity.level().isClientSide()) {

            int duration = effectInstance.getDuration();

            TimedAbilitySystem.addAbilityToEntity(entity, AbilityRegistry.CRIMSON_MEMORIES, duration);
        }
    }
}