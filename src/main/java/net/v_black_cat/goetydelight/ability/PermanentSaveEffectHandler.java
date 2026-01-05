package net.v_black_cat.goetydelight.ability;

import com.Polarice3.Goety.common.effects.GoetyEffects;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraftforge.event.entity.living.LivingEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber
public class PermanentSaveEffectHandler {

    private static final int REFRESH_INTERVAL = 100; 

    @SubscribeEvent
    public static void onLivingTick(LivingEvent.LivingTickEvent event) {
        LivingEntity entity = event.getEntity();

        if (entity.level().isClientSide) return;

        if (TimedAbilitySystem.hasAbility(entity, AbilityRegistry.PERMANENT_SAVE_EFFECTS)) {
            
            if (entity.tickCount % REFRESH_INTERVAL == 0) {
                
                MobEffectInstance fireResistance = new MobEffectInstance(
                        GoetyEffects.SAVE_EFFECTS.get(),
                        -1, 
                        0,
                        false,
                        false
                );
                entity.addEffect(fireResistance);
            }
        }
    }


}