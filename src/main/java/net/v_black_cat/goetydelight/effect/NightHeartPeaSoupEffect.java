package net.v_black_cat.goetydelight.effect;

import com.Polarice3.Goety.api.items.magic.IFocus;
import com.Polarice3.Goety.api.items.magic.IWand;
import com.Polarice3.Goety.common.items.magic.DarkWand;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.util.RandomSource;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraftforge.event.entity.EntityJoinLevelEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

public class NightHeartPeaSoupEffect extends MobEffect {
    public NightHeartPeaSoupEffect() {
        super(MobEffectCategory.BENEFICIAL, 0x98D982);
    }

    @Override
    public void applyEffectTick(LivingEntity entity, int amplifier) {
        // 每tick应用效果（如果需要）
        // 这里可以添加粒子效果或其他视觉效果
        Level level = entity.level();
        if (level.isClientSide) {
            RandomSource random = level.random;
            for (int i = 0; i < 2; i++) {
                double x = entity.getX() + (random.nextDouble() - 0.5) * 2.0;
                double y = entity.getY() + random.nextDouble() * 1.5;
                double z = entity.getZ() + (random.nextDouble() - 0.5) * 2.0;

                level.addParticle(ParticleTypes.ENCHANT,
                        x, y, z,
                        0, 0.1, 0);
            }
        }
    }

    @Override
    public boolean isDurationEffectTick(int duration, int amplifier) {
        // 每10tick执行一次效果应用
        return duration % 10 == 0;
    }

}