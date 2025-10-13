package net.v_black_cat.goetydelight.api.impl;


import net.minecraft.world.entity.ai.attributes.Attribute;
import net.v_black_cat.goetydelight.api.IGetSpellAttributeImplementation;
import com.mega.revelationfix.common.init.ModAttributes;

public class RevelationGetSpellAttributeModifierImpl implements IGetSpellAttributeImplementation {


    private static final RevelationGetSpellAttributeModifierImpl INSTANCE = new RevelationGetSpellAttributeModifierImpl();

    public RevelationGetSpellAttributeModifierImpl() {
    }


    @Override
    public Attribute getSpellPotencyAttributeModifier() {
        return ModAttributes.SPELL_POWER.get();
    }

    @Override
    public Attribute getSpellDurationAttributeModifier() {
        return ModAttributes.SPELL_DURATION.get();
    }

    @Override
    public Attribute getSpellRangeAttributeModifier() {
        return ModAttributes.SPELL_RANGE.get();
    }

    @Override
    public Attribute getSpellRadiusAttributeModifier() {
        return ModAttributes.SPELL_RADIUS.get();
    }

    @Override
    public Attribute getSpellBurningAttributeModifier() {
        return ModAttributes.SPELL_BURNING.get();
    }

    @Override
    public Attribute getSpellVelocityAttributeModifier() {
        return ModAttributes.SPELL_VELOCITY.get();
    }

    @Override
    public Attribute getCastingSpeedAttributeModifier() {
        return ModAttributes.SPELL_VELOCITY.get();
    }

    @Override
    public Attribute getCooldownDiscountAttributeModifier() {
        return ModAttributes.SPELL_COOLDOWN.get();
    }

    @Override
    public Attribute getSoulDiscountAttributeModifier() {
        return ModAttributes.SOUL_DECREASE_EFFICIENCY.get();
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

    public static IGetSpellAttributeImplementation getInstance() {
        return INSTANCE;
    }
}