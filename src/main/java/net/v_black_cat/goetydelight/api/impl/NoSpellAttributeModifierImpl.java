package net.v_black_cat.goetydelight.api.impl;

import net.minecraft.world.entity.ai.attributes.Attribute;
import net.v_black_cat.goetydelight.api.IGetSpellAttributeImplementation;

public class NoSpellAttributeModifierImpl implements IGetSpellAttributeImplementation {


    private static final NoSpellAttributeModifierImpl INSTANCE = new NoSpellAttributeModifierImpl();

    private NoSpellAttributeModifierImpl() {
    }


    public static IGetSpellAttributeImplementation getInstance() {
        return INSTANCE;
    }

    @Override
    public Attribute getSpellPotencyAttributeModifier() {
        return null;
    }

    @Override
    public Attribute getSpellDurationAttributeModifier() {
        return null;
    }

    @Override
    public Attribute getSpellRangeAttributeModifier() {
        return null;
    }

    @Override
    public Attribute getSpellRadiusAttributeModifier() {
        return null;
    }

    @Override
    public Attribute getSpellBurningAttributeModifier() {
        return null;
    }

    @Override
    public Attribute getSpellVelocityAttributeModifier() {
        return null;
    }

    @Override
    public Attribute getCastingSpeedAttributeModifier() {
        return null;
    }

    @Override
    public Attribute getCooldownDiscountAttributeModifier() {
        return null;
    }

    @Override
    public Attribute getSoulDiscountAttributeModifier() {
        return null;
    }

    @Override
    public Attribute getAbyssPotencyAttributeModifier() {
        return null;
    }

    @Override
    public Attribute getFrostPotencyAttributeModifier() {
        return null;
    }

    @Override
    public Attribute getGeomancyPotencyAttributeModifier() {
        return null;
    }

    @Override
    public Attribute getNecromancyPotencyAttributeModifier() {
        return null;
    }

    @Override
    public Attribute getNetherPotencyAttributeModifier() {
        return null;
    }

    @Override
    public Attribute getStormPotencyAttributeModifier() {
        return null;
    }

    @Override
    public Attribute getVoidPotencyAttributeModifier() {
        return null;
    }

    @Override
    public Attribute getWildPotencyAttributeModifier() {
        return null;
    }

    @Override
    public Attribute getWindPotencyAttributeModifier() {
        return null;
    }
}