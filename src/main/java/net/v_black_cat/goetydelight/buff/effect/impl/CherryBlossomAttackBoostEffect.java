package net.v_black_cat.goetydelight.buff.effect.impl;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.v_black_cat.goetydelight.buff.effect.BuffEffect;

public class CherryBlossomAttackBoostEffect implements BuffEffect {
    private static final ResourceLocation ATTACK_DAMAGE_ID =
            ResourceLocation.parse("goetydelight:cherry_blossom_attack_boost");

    @Override
    public void apply(LivingEntity entity, int amplifier) {
        AttributeInstance attackDamage = entity.getAttribute(Attributes.ATTACK_DAMAGE);
        if (attackDamage != null && attackDamage.getModifier(ATTACK_DAMAGE_ID) == null) {
            double luckValue = entity.getAttribute(Attributes.LUCK) != null ?
                    entity.getAttribute(Attributes.LUCK).getValue() : 0;
            double baseAttack = attackDamage.getBaseValue();
            double boostAmount = luckValue + baseAttack * 0.002;
            attackDamage.addTransientModifier(new AttributeModifier(
                    ATTACK_DAMAGE_ID, boostAmount, AttributeModifier.Operation.ADD_VALUE
            ));
        }
    }

    @Override
    public void onApply(LivingEntity entity, int amplifier) {
        AttributeInstance attackDamage = entity.getAttribute(Attributes.ATTACK_DAMAGE);
        if (attackDamage != null) {
            double luckValue = entity.getAttribute(Attributes.LUCK) != null ?
                    entity.getAttribute(Attributes.LUCK).getValue() : 0;
            double baseAttack = attackDamage.getBaseValue();
            double boostAmount = luckValue + baseAttack * 0.002;
            attackDamage.addTransientModifier(new AttributeModifier(
                    ATTACK_DAMAGE_ID, boostAmount, AttributeModifier.Operation.ADD_VALUE
            ));
        }
    }

    @Override
    public void onRemove(LivingEntity entity, int amplifier) {
        AttributeInstance attackDamage = entity.getAttribute(Attributes.ATTACK_DAMAGE);
        if (attackDamage != null && attackDamage.getModifier(ATTACK_DAMAGE_ID) != null) {
            attackDamage.removeModifier(ATTACK_DAMAGE_ID);
        }
    }
}