package net.v_black_cat.goetydelight.item.food;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.core.particles.ParticleTypes;

import java.util.ArrayList;
import java.util.List;

public class PoachedNetherWartEggItem extends Item {

    public PoachedNetherWartEggItem(Properties pProperties) {
        super(pProperties);
    }

    @Override
    public ItemStack finishUsingItem(ItemStack stack, Level level, LivingEntity entity) {
        // 首先调用父类方法处理正常的食用逻辑
        ItemStack result = super.finishUsingItem(stack, level, entity);

        // 只在服务器端执行效果移除
        if (!level.isClientSide && level instanceof ServerLevel serverLevel) {
            // 获取所有负面效果
            List<MobEffect> effectsToRemove = new ArrayList<>();

            // 遍历所有效果，找出负面效果
            for (MobEffectInstance effectInstance : entity.getActiveEffects()) {
                MobEffect effect = effectInstance.getEffect();

                // 检查是否为负面效果
                if (effect.getCategory() == MobEffectCategory.HARMFUL) {
                    effectsToRemove.add(effect);
                }
            }

            // 移除所有负面效果
            for (MobEffect effect : effectsToRemove) {
                entity.removeEffect(effect);
            }

            // 播放清除效果的声音
            serverLevel.playSound(null, entity.getX(), entity.getY(), entity.getZ(),
                    SoundEvents.BEACON_DEACTIVATE, SoundSource.PLAYERS, 1.0F, 1.5F);

            // 生成净化粒子效果
            for (int i = 0; i < 15; i++) {
                double x = entity.getX() + (level.random.nextDouble() - 0.5) * 2.0;
                double y = entity.getY() + level.random.nextDouble() * 2.0;
                double z = entity.getZ() + (level.random.nextDouble() - 0.5) * 2.0;

                serverLevel.sendParticles(ParticleTypes.ENCHANT,
                        x, y, z, 1, 0, 0, 0, 0.1);
            }

            // 如果移除了效果，播放额外的声音
            if (!effectsToRemove.isEmpty()) {
                serverLevel.playSound(null, entity.getX(), entity.getY(), entity.getZ(),
                        SoundEvents.EXPERIENCE_ORB_PICKUP, SoundSource.PLAYERS, 0.7F, 1.2F);
            }
        }

        return result;
    }
}