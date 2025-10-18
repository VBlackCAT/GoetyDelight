package net.v_black_cat.goetydelight.ability;

import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraftforge.event.entity.living.LivingEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber
public class PermanentFireResistanceHandler {

    private static final int REFRESH_INTERVAL = 100; 

    @SubscribeEvent
    public static void onLivingTick(LivingEvent.LivingTickEvent event) {
        LivingEntity entity = event.getEntity();

        if (entity.level().isClientSide) return;

        if (TimedAbilitySystem.hasAbility(entity, AbilityRegistry.PERMANENT_FIRE_RESISTANCE)) {
            
            if (entity.tickCount % REFRESH_INTERVAL == 0) {
                
                MobEffectInstance fireResistance = new MobEffectInstance(
                        MobEffects.FIRE_RESISTANCE,
                        -1, 
                        0,
                        false, 
                        true
                );

                entity.addEffect(fireResistance);
            }
        }
    }


}