package net.v_black_cat.goetydelight.effect;

import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeMap;
import net.minecraft.world.entity.player.Player;
import net.v_black_cat.goetydelight.util.SpellPotencyUtil;

public class SpellMasteryEffect extends MobEffect {

    public SpellMasteryEffect() {
        super(MobEffectCategory.BENEFICIAL, 0x8B4513);
    }

    @Override
    public void addAttributeModifiers(LivingEntity livingEntity, AttributeMap attributeMap, int amplifier) {
        if (livingEntity instanceof Player player && !player.level().isClientSide) {
            // 设置效果的临时加成
            double bonus = amplifier + 1;
            SpellPotencyUtil.setEffectBonus(player, bonus);

            // 重新计算并应用总加成
            SpellPotencyUtil.recalculateAndApply(player);
        }
    }

    @Override
    public void removeAttributeModifiers(LivingEntity livingEntity, AttributeMap attributeMap, int amplifier) {
        if (livingEntity instanceof Player player && !player.level().isClientSide) {
            // 清除效果的临时加成
            SpellPotencyUtil.setEffectBonus(player, 0);

            // 重新计算并应用总加成（只保留硬糖的加成）
            SpellPotencyUtil.recalculateAndApply(player);
        }
    }

    @Override
    public void applyEffectTick(LivingEntity entity, int amplifier) {
        super.applyEffectTick(entity, amplifier);

        if (entity.level().isClientSide) {
            double x = entity.getX() + (entity.level().random.nextDouble() - 0.5) * entity.getBbWidth();
            double y = entity.getY() + entity.level().random.nextDouble() * entity.getBbHeight();
            double z = entity.getZ() + (entity.level().random.nextDouble() - 0.5) * entity.getBbWidth();

            entity.level().addParticle(ParticleTypes.ENCHANT, x, y, z, 0.0D, 0.1D, 0.0D);
        }
    }

    @Override
    public boolean isDurationEffectTick(int duration, int amplifier) {
        return duration % 10 == 0;
    }
}