package net.v_black_cat.goetydelight.effect;

import com.Polarice3.Goety.init.ModAttributes;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeMap;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.level.Level;
import net.v_black_cat.goetydelight.api.GetSpellAttributeFactory;

import java.util.UUID;

public class SpellMasteryEffect extends MobEffect {

    // 为法术强度属性创建唯一的UUID
    private static final UUID SPELL_POTENCY_UUID = UUID.fromString("8b4513a0-4e2a-11ee-be56-0242ac120003");

    public SpellMasteryEffect() {
        super(MobEffectCategory.BENEFICIAL, 0x8B4513);
        Attribute attribute = GetSpellAttributeFactory.createGetSpellAttributeImplementation().getSpellPotencyAttributeModifier();

        // 只有当attribute不为null时才添加属性修改器
        if (attribute != null) {
            // 使用父类的属性修改器注册机制
            this.addAttributeModifier(
                    attribute,
                    SPELL_POTENCY_UUID.toString(),
                    1, // 基础值（会被等级放大）
                    AttributeModifier.Operation.ADDITION
            );
        }
    }

    @Override
    public double getAttributeModifierValue(int amplifier, AttributeModifier modifier) {
        // 每级效果增加1点法术强度
        // 公式：基础值 * (等级 + 1)
        return modifier.getAmount() * (amplifier + 1);
    }

    @Override
    public void applyEffectTick(LivingEntity entity, int amplifier) {
        super.applyEffectTick(entity, amplifier);

        Level world = entity.level();
        if (world.isClientSide) {
            // 客户端粒子效果
            double x = entity.getX() + (world.random.nextDouble() - 0.5) * entity.getBbWidth();
            double y = entity.getY() + world.random.nextDouble() * entity.getBbHeight();
            double z = entity.getZ() + (world.random.nextDouble() - 0.5) * entity.getBbWidth();

            world.addParticle(ParticleTypes.ENCHANT, x, y, z, 0.0D, 0.1D, 0.0D);
        }
    }

    @Override
    public boolean isDurationEffectTick(int duration, int amplifier) {
        // 每10tick执行一次（0.5秒）
        return duration % 10 == 0;
    }
}