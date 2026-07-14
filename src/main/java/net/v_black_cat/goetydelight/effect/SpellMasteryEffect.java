package net.v_black_cat.goetydelight.effect;

import com.Polarice3.Goety.init.ModAttributes;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.level.Level;

public class SpellMasteryEffect extends MobEffect {
    private static final ResourceLocation SPELL_POTENCY_ID = ResourceLocation.parse("8b4513a0-4e2a-11ee-be56-0242ac120003");

    public SpellMasteryEffect() {
        super(MobEffectCategory.BENEFICIAL, 0x8B4513);
        Attribute attr = ModAttributes.SPELL_POTENCY.get();;
        if (attr != null) {
            this.addAttributeModifier(
                    BuiltInRegistries.ATTRIBUTE.wrapAsHolder(attr),
                    SPELL_POTENCY_ID,
                    1.0, // 基础值，自动乘以(等级+1)
                    AttributeModifier.Operation.ADD_VALUE
            );
        }
    }

    @Override
    public boolean applyEffectTick(LivingEntity entity, int amplifier) {
        Level world = entity.level();
        if (world.isClientSide) {
            double x = entity.getX() + (world.random.nextDouble() - 0.5) * entity.getBbWidth();
            double y = entity.getY() + world.random.nextDouble() * entity.getBbHeight();
            double z = entity.getZ() + (world.random.nextDouble() - 0.5) * entity.getBbWidth();
            world.addParticle(ParticleTypes.ENCHANT, x, y, z, 0.0D, 0.1D, 0.0D);
        }
        return true;
    }

    @Override
    public boolean shouldApplyEffectTickThisTick(int duration, int amplifier) {
        return duration % 10 == 0;
    }
}