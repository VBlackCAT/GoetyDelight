package net.v_black_cat.goetydelight.buff.effect.impl;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.neoforged.neoforge.event.entity.living.LivingIncomingDamageEvent;
import net.v_black_cat.goetydelight.effect.WardenEffect;

import static net.v_black_cat.goetydelight.GoetyDelight.LOGGER;

public class WardenBuffEffect {

    public static void onLivingIncomingDamage(LivingIncomingDamageEvent event) {
        LivingEntity target = event.getEntity();
        Entity source = event.getSource().getEntity();

        if (source instanceof LivingEntity attacker && WardenEffect.hasDamageBoostAgainst(attacker, target)) {
            float originalDamage = event.getAmount();
            float boostedDamage = originalDamage * WardenEffect.getDamageBoostMultiplier();
            event.setAmount(boostedDamage);

            LOGGER.debug("[WardenEffect] {} dealt {} damage (originally {}) to {}",
                    attacker.getName().getString(), boostedDamage, originalDamage, target.getName().getString());
        }
    }
}