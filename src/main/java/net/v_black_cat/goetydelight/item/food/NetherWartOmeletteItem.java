package net.v_black_cat.goetydelight.item.food;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionHand;
import net.minecraft.network.chat.Component;

import java.util.ArrayList;
import java.util.List;

public class NetherWartOmeletteItem extends Item {
    public NetherWartOmeletteItem(Properties pProperties) {
        super(pProperties);
    }
    
    @Override
    public ItemStack finishUsingItem(ItemStack stack, Level world, LivingEntity entity) {
        ItemStack result = super.finishUsingItem(stack, world, entity);

        if (!world.isClientSide && world instanceof ServerLevel serverLevel) {
            // 获取所有当前的负面效果
            List<MobEffectInstance> harmfulEffects = new ArrayList<>();
            
            for (MobEffectInstance effectInstance : entity.getActiveEffects()) {
                if (effectInstance.getEffect().getCategory() == MobEffectCategory.HARMFUL) {
                    harmfulEffects.add(effectInstance);
                }
            }
            
            // 处理每个负面效果
            for (MobEffectInstance effectInstance : harmfulEffects) {
                int amplifier = effectInstance.getAmplifier();
                
                // 如果等级为1，则移除效果
                if (amplifier == 0) { // 等级从0开始计数
                    entity.removeEffect(effectInstance.getEffect());
                } 
                // 如果等级大于1，则降低1级
                else if (amplifier > 0) {
                    // 移除旧的效果
                    entity.removeEffect(effectInstance.getEffect());
                    entity.addEffect(new MobEffectInstance(
                        effectInstance.getEffect(),
                        effectInstance.getDuration(),
                        amplifier - 1,
                        effectInstance.isAmbient(),
                        effectInstance.isVisible(),
                        effectInstance.showIcon()
                    ));
                    

                }
            }
            
            // 播放声音和粒子效果
            serverLevel.playSound(null, entity.getX(), entity.getY(), entity.getZ(),
                    SoundEvents.BEACON_DEACTIVATE, SoundSource.PLAYERS, 1.0F, 1.5F);

            // 生成净化粒子效果
            for (int i = 0; i < 15; i++) {
                double x = entity.getX() + (world.random.nextDouble() - 0.5) * 2.0;
                double y = entity.getY() + world.random.nextDouble() * 2.0;
                double z = entity.getZ() + (world.random.nextDouble() - 0.5) * 2.0;

                serverLevel.sendParticles(ParticleTypes.ENCHANT,
                        x, y, z, 1, 0, 0, 0, 0.1);
            }
        }

        return result;
    }
    
    @Override
    public InteractionResult interactLivingEntity(ItemStack stack, Player player, LivingEntity target, InteractionHand hand) {
        if (!player.level().isClientSide) {

            finishUsingItem(stack,player.level(),target);
            return InteractionResult.SUCCESS;
        }
        
        return InteractionResult.PASS;
    }
}