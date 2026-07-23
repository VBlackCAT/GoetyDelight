package net.v_black_cat.goetydelight.effect;

import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.neoforged.neoforge.event.entity.player.AttackEntityEvent;
import net.v_black_cat.goetydelight.init.ModEffects;

import java.util.Objects;

public class VoidAffixEffect extends MobEffect {
    public VoidAffixEffect() {
        super(MobEffectCategory.BENEFICIAL, 0x000000);
    }

    public static void onAttackEntity(AttackEntityEvent event) {
        Player attacker = event.getEntity();
        Entity target = event.getTarget();
        if (target instanceof LivingEntity livingTarget) {
            if (attacker.hasEffect(ModEffects.VOID_AFFIX)) {
                DamageSource voidDamage = attacker.damageSources().fellOutOfWorld();
                int amplifier = Objects.requireNonNull(attacker.getEffect(ModEffects.VOID_AFFIX)).getAmplifier();
                float damage = 1.0F * (amplifier + 1);
                livingTarget.hurt(voidDamage, damage);
            }
        }
    }
}