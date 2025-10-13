package net.v_black_cat.goetydelight.api.impl;


import com.Polarice3.Goety.init.ModAttributes;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.v_black_cat.goetydelight.api.IGetSpellAttributeImplementation;

public class GoetyGetSpellAttributeModifierImpl implements IGetSpellAttributeImplementation {

    private static final GoetyGetSpellAttributeModifierImpl INSTANCE = new GoetyGetSpellAttributeModifierImpl();

    @Override
    public Attribute getSpellPotencyAttributeModifier() {
        return ModAttributes.SPELL_POTENCY.get();
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
        return ModAttributes.CASTING_SPEED.get();
    }

    @Override
    public Attribute getCooldownDiscountAttributeModifier() {
        return ModAttributes.COOLDOWN_DISCOUNT.get();
    }

    @Override
    public Attribute getSoulDiscountAttributeModifier() {
        return ModAttributes.SOUL_DISCOUNT.get();
    }

    @Override
    public Attribute getAbyssPotencyAttributeModifier() {
        return ModAttributes.ABYSS_POTENCY.get();
    }

    @Override
    public Attribute getFrostPotencyAttributeModifier() {
        return ModAttributes.FROST_POTENCY.get();
    }

    @Override
    public Attribute getGeomancyPotencyAttributeModifier() {
        return ModAttributes.GEOMANCY_POTENCY.get();
    }

    @Override
    public Attribute getNecromancyPotencyAttributeModifier() {
        return ModAttributes.NECROMANCY_POTENCY.get();
    }

    @Override
    public Attribute getNetherPotencyAttributeModifier() {
        return ModAttributes.NETHER_POTENCY.get();
    }

    @Override
    public Attribute getStormPotencyAttributeModifier() {
        return ModAttributes.STORM_POTENCY.get();
    }

    @Override
    public Attribute getVoidPotencyAttributeModifier() {
        return ModAttributes.VOID_POTENCY.get();
    }

    @Override
    public Attribute getWildPotencyAttributeModifier() {
        return ModAttributes.WILD_POTENCY.get();
    }

    @Override
    public Attribute getWindPotencyAttributeModifier() {
        return ModAttributes.WIND_POTENCY.get();
    }

    public static IGetSpellAttributeImplementation getInstance() {
        return INSTANCE;
    }
}