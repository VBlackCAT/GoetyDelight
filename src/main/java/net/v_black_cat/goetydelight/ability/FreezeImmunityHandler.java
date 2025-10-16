package net.v_black_cat.goetydelight.ability;

import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.entity.LivingEntity;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.event.entity.living.LivingEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber
public class FreezeImmunityHandler {

    @SubscribeEvent
    public static void onLivingHurt(LivingHurtEvent event) {
        LivingEntity entity = event.getEntity();
        DamageSource source = event.getSource();


        if (TimedAbilitySystem.hasAbility(entity, AbilityRegistry.FREEZE_IMMUNITY)) {
            if (source.is(DamageTypes.FREEZE) ||
                    source.getMsgId().equals("freeze") ||
                    isPowderSnowDamage(source)) {
                event.setCanceled(true);
                return;
            }
        }
    }

    @SubscribeEvent
    public static void onLivingTick(LivingEvent.LivingTickEvent event) {
        LivingEntity entity = event.getEntity();

         
        if (TimedAbilitySystem.hasAbility(entity, AbilityRegistry.FREEZE_IMMUNITY)) {
             
            if (entity.isInPowderSnow) {
                entity.setIsInPowderSnow(false);
                entity.clearFire();
            }

             
            if (entity.isFullyFrozen()) {
                entity.setTicksFrozen(0);
            }
        }
    }

     
    private static boolean isPowderSnowDamage(DamageSource source) {
        return source.getMsgId().equals("powder_snow") ||
                source.getMsgId().contains("powder") ||
                source.getMsgId().contains("freeze");
    }
}