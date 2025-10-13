package net.v_black_cat.goetydelight.api;

import net.minecraft.world.entity.ai.attributes.Attribute;
import net.v_black_cat.goetydelight.api.impl.GoetyGetSpellAttributeModifierImpl;

public interface IGetSpellAttributeImplementation {

    Attribute getSpellPotencyAttributeModifier();

    default Attribute getSpellDurationAttributeModifier() {
        return null;
    }

    default Attribute getSpellRangeAttributeModifier() {
        return null;
    }

    default Attribute getSpellRadiusAttributeModifier() {
        return null;
    }

    default Attribute getSpellBurningAttributeModifier() {
        return null;
    }

    default Attribute getSpellVelocityAttributeModifier() {
        return null;
    }

    default Attribute getCastingSpeedAttributeModifier() {
        return null;
    }

    default Attribute getCooldownDiscountAttributeModifier() {
        return null;
    }

    default Attribute getSoulDiscountAttributeModifier() {
        return null;
    }

    default Attribute getAbyssPotencyAttributeModifier() {
        return null;
    }

    default Attribute getFrostPotencyAttributeModifier() {
        return null;
    }

    default Attribute getGeomancyPotencyAttributeModifier() {
        return null;
    }

    default Attribute getNecromancyPotencyAttributeModifier() {
        return null;
    }

    default Attribute getNetherPotencyAttributeModifier() {
        return null;
    }

    default Attribute getStormPotencyAttributeModifier() {
        return null;
    }

    default Attribute getVoidPotencyAttributeModifier() {
        return null;
    }

    default Attribute getWildPotencyAttributeModifier() {
        return null;
    }

    default Attribute getWindPotencyAttributeModifier() {
        return null;
    }
}