package net.v_black_cat.goetydelight.ability;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import static net.v_black_cat.goetydelight.GoetyDelight.LOGGER;
import static net.v_black_cat.goetydelight.GoetyDelight.MODID;
import static net.v_black_cat.goetydelight.effect.WardenEffect.getDamageBoostMultiplier;
import static net.v_black_cat.goetydelight.effect.WardenEffect.hasDamageBoostAgainst;

@Mod.EventBusSubscriber(modid = "goetydelight")
public class WardenDetectedHandle {
    @SubscribeEvent
    public static void onLivingHurt(net.minecraftforge.event.entity.living.LivingHurtEvent event) {
        LivingEntity target = event.getEntity();
        Entity source = event.getSource().getEntity();


        if (source instanceof LivingEntity attacker && hasDamageBoostAgainst(attacker, target)) {
            float originalDamage = event.getAmount();
            float boostedDamage = originalDamage * getDamageBoostMultiplier();
            event.setAmount(boostedDamage);

            LOGGER.debug("[WardenEffect] {} dealt {} damage (originally {}) to {}",
                    attacker.getName().getString(), boostedDamage, originalDamage, target.getName().getString());
        }
    }
}
