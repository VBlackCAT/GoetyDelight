package net.v_black_cat.goetydelight.item.food;

import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

import static net.v_black_cat.goetydelight.util.TimeConverter.minToTick;

public class StuffedTallSkullRiceItem extends BowlFoodItem{

    public StuffedTallSkullRiceItem(Properties pProperties) {
        super(pProperties);
    }
    
    @Override
    public ItemStack finishUsingItem(ItemStack stack, Level level, LivingEntity entity) {
        ItemStack result = super.finishUsingItem(stack, level, entity);
        
        if (!level.isClientSide && entity instanceof Player player) {
            MobEffectInstance currentBadOmen = player.getEffect(MobEffects.BAD_OMEN);
            
            
            if (currentBadOmen != null) {
                int newAmplifier = Math.min(currentBadOmen.getAmplifier() + 1, 4); 
                int newDuration = currentBadOmen.getDuration() + 6000;
                
                player.removeEffect(MobEffects.BAD_OMEN);

                player.addEffect(new MobEffectInstance(MobEffects.BAD_OMEN, newDuration, newAmplifier));
            }else {
                player.addEffect(new MobEffectInstance(MobEffects.BAD_OMEN, minToTick(10)));
            }
        }
        
        return result;
    }

}