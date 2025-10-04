package net.v_black_cat.goetydelight.item.food;

import com.Polarice3.Goety.common.effects.GoetyEffects;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

public class FrenziedFungusPopRocksItem extends Item {

    // 定义效果持续时间
    private static final int DAMAGE_BOOST_DURATION = 150 * 20; // 150秒
    private static final int MOVEMENT_SPEED_DURATION = 3000; // 150秒
    private static final int CHARGED_EFFECT_DURATION = 3000; // 150秒

    public FrenziedFungusPopRocksItem(Properties pProperties) {
        super(pProperties);
    }

    @Override
    public ItemStack finishUsingItem(ItemStack stack, Level level, LivingEntity entity) {
        ItemStack resultStack = super.finishUsingItem(stack, level, entity);

        if (!level.isClientSide) {
            // 处理伤害提升效果
            MobEffectInstance damageBoostEffect = entity.getEffect(MobEffects.DAMAGE_BOOST);
            int newDamageBoostDuration = damageBoostEffect != null ?
                    damageBoostEffect.getDuration() + DAMAGE_BOOST_DURATION :
                    DAMAGE_BOOST_DURATION;

            entity.addEffect(new MobEffectInstance(
                    MobEffects.DAMAGE_BOOST,
                    newDamageBoostDuration,
                    0, // 等级0
                    false, // 不是环境效果
                    true // 显示粒子效果
            ));

            // 处理移动速度效果
            MobEffectInstance movementSpeedEffect = entity.getEffect(MobEffects.MOVEMENT_SPEED);
            int newMovementSpeedDuration = movementSpeedEffect != null ?
                    movementSpeedEffect.getDuration() + MOVEMENT_SPEED_DURATION :
                    MOVEMENT_SPEED_DURATION;

            entity.addEffect(new MobEffectInstance(
                    MobEffects.MOVEMENT_SPEED,
                    newMovementSpeedDuration,
                    0, // 等级0
                    false, // 不是环境效果
                    true // 显示粒子效果
            ));

            // 处理带电效果
            MobEffectInstance chargedEffect = entity.getEffect(GoetyEffects.CHARGED.get());
            int newChargedDuration = chargedEffect != null ?
                    chargedEffect.getDuration() + CHARGED_EFFECT_DURATION :
                    CHARGED_EFFECT_DURATION;

            entity.addEffect(new MobEffectInstance(
                    GoetyEffects.CHARGED.get(),
                    newChargedDuration,
                    0, // 等级0
                    false, // 不是环境效果
                    true // 显示粒子效果
            ));
        }

        return resultStack;
    }
}