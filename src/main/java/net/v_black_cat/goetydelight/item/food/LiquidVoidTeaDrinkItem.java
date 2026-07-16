package net.v_black_cat.goetydelight.item.food;

import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.UseAnim;
import net.minecraft.world.level.Level;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionHand;

import static com.Polarice3.Goety.common.effects.GoetyEffects.VOID_TOUCHED;
import static net.v_black_cat.goetydelight.util.TickConverterUtil.sToTick;

public class LiquidVoidTeaDrinkItem extends GlassBottleFoodItem {
    public LiquidVoidTeaDrinkItem(Properties pProperties) {
        super(pProperties);
    }


    @Override
    public UseAnim getUseAnimation(ItemStack stack) {
        return UseAnim.DRINK;
    }

    @Override
    public SoundEvent getDrinkingSound() {
        return SoundEvents.GENERIC_DRINK;
    }

    @Override
    public ItemStack finishUsingItem(ItemStack stack, Level level, LivingEntity entity) {
        ItemStack result = super.finishUsingItem(stack, level, entity);
        entity.removeEffect(VOID_TOUCHED);
        
        // 造成5点虚空伤害
        entity.hurt(level.damageSources().genericKill(), 5.0F);
        if (entity instanceof Player player){
            player.getFoodData().setSaturation(0f);
        }
        
        // 添加VOID_TOUCHED效果，持续30秒，等级2
        entity.addEffect(new MobEffectInstance(VOID_TOUCHED, sToTick(30), 2));
        
        return result;
    }

    @Override
    public InteractionResult interactLivingEntity(ItemStack stack, Player player, LivingEntity target, InteractionHand hand) {
        if (!player.level().isClientSide&&!target.isInvisible()) {

            // 让目标实体吃下这个物品
            target.eat(player.level(), stack.copy());
            target.removeEffect(VOID_TOUCHED);
            // 造成5点虚空伤害
            target.hurt(player.level().damageSources().genericKill(), 5.0F);
            
            // 添加VOID_TOUCHED效果，持续30秒，等级2
            target.addEffect(new MobEffectInstance(VOID_TOUCHED, sToTick(30), 2));

            
            // 消耗物品（如果不是创造模式）
            if (!player.getAbilities().instabuild) {
                stack.shrink(1);
            }
            
            return InteractionResult.SUCCESS;
        }
        
        return InteractionResult.PASS;
    }
}
