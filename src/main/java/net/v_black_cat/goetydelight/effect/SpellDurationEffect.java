package net.v_black_cat.goetydelight.effect;

import com.Polarice3.Goety.init.ModAttributes;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;


public class SpellDurationEffect extends MobEffect {
    private static final ResourceLocation SPELL_DURATION_ID = ResourceLocation.parse("8b4513a0-4e2a-11ee-be56-0242ac120002");

    public SpellDurationEffect() {
        super(MobEffectCategory.BENEFICIAL, 0x8B4513);
        Attribute attr = ModAttributes.SPELL_DURATION.get();
        this.addAttributeModifier(
                BuiltInRegistries.ATTRIBUTE.wrapAsHolder(attr),
                SPELL_DURATION_ID,
                2.0,
                AttributeModifier.Operation.ADD_VALUE
        );
    }
}