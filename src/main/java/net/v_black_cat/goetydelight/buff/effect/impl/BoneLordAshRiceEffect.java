package net.v_black_cat.goetydelight.buff.effect.impl;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.v_black_cat.goetydelight.buff.effect.BuffEffect;

import static net.v_black_cat.goetydelight.GoetyDelight.MODID;

public class BoneLordAshRiceEffect implements BuffEffect {
    private static final ResourceLocation ARMOR_BONUS_ID =
            ResourceLocation.fromNamespaceAndPath(MODID,"bone_lord_ash_ricearmor");
    private static final ResourceLocation ARMOR_TOUGHNESS_BONUS_ID =
            ResourceLocation.fromNamespaceAndPath(MODID,"bone_lord_ash_ricetoughness");

    @Override
    public void apply(LivingEntity entity, int amplifier) {
        // 不需要每 tick 效果
    }

    @Override
    public void onApply(LivingEntity entity, int amplifier) {
        // 添加护甲 +15
        AttributeInstance armor = entity.getAttribute(Attributes.ARMOR);
        if (armor != null) {
            armor.addTransientModifier(new AttributeModifier(
                    ARMOR_BONUS_ID,
                    15.0,
                    AttributeModifier.Operation.ADD_VALUE
            ));
        }

        // 添加韧性 +10
        AttributeInstance toughness = entity.getAttribute(Attributes.ARMOR_TOUGHNESS);
        if (toughness != null) {
            toughness.addTransientModifier(new AttributeModifier(
                    ARMOR_TOUGHNESS_BONUS_ID,
                    10.0,
                    AttributeModifier.Operation.ADD_VALUE
            ));
        }
    }

    @Override
    public void onRemove(LivingEntity entity, int amplifier) {
        // 移除护甲修饰符
        AttributeInstance armor = entity.getAttribute(Attributes.ARMOR);
        if (armor != null && armor.getModifier(ARMOR_BONUS_ID) != null) {
            armor.removeModifier(ARMOR_BONUS_ID);
        }

        // 移除韧性修饰符
        AttributeInstance toughness = entity.getAttribute(Attributes.ARMOR_TOUGHNESS);
        if (toughness != null && toughness.getModifier(ARMOR_TOUGHNESS_BONUS_ID) != null) {
            toughness.removeModifier(ARMOR_TOUGHNESS_BONUS_ID);
        }
    }
}