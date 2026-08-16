package net.v_black_cat.goetydelight.item.food;

import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.util.RandomSource;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class MagicQuartzCookieItem extends Item {
     
    private static List<MobEffect> positiveEffects = null;

    public MagicQuartzCookieItem(Properties properties) {
        super(properties);
    }

     
    private static List<MobEffect> getPositiveEffects() {
        if (positiveEffects == null) {
             
            positiveEffects = BuiltInRegistries.MOB_EFFECT.stream()
                    .filter(effect -> effect.getCategory() == MobEffectCategory.BENEFICIAL)
                    .collect(Collectors.toList());

             
            if (positiveEffects.isEmpty()) {
                positiveEffects.add(net.minecraft.world.effect.MobEffects.MOVEMENT_SPEED);
                positiveEffects.add(net.minecraft.world.effect.MobEffects.DIG_SPEED);
                positiveEffects.add(net.minecraft.world.effect.MobEffects.DAMAGE_BOOST);
            }
        }
        return positiveEffects;
    }

@Override
public ItemStack finishUsingItem(ItemStack stack, Level level, LivingEntity entity) {
    ItemStack result = super.finishUsingItem(stack, level, entity);

    if (!level.isClientSide && entity instanceof net.minecraft.world.entity.player.Player) {
        net.minecraft.world.entity.player.Player player = (net.minecraft.world.entity.player.Player) entity;
        

        if (player.getCooldowns().isOnCooldown(this)) {
            return result;
        }
        

        player.getCooldowns().addCooldown(this, 60 * 20);
         
        entity.addEffect(new MobEffectInstance(
                net.minecraft.world.effect.MobEffects.DAMAGE_RESISTANCE,
                30 * 20,  
                1,  
                false,  
                true  
        ));

         
        List<MobEffect> availableEffects = new ArrayList<>(getPositiveEffects());

         
        availableEffects.remove(net.minecraft.world.effect.MobEffects.DAMAGE_RESISTANCE);

        if (availableEffects.size() >= 2) {
            RandomSource random = level.random;

             
            int firstIndex = random.nextInt(availableEffects.size());
            MobEffect firstEffect = availableEffects.get(firstIndex);
            availableEffects.remove(firstIndex);

             
            int secondIndex = random.nextInt(availableEffects.size());
            MobEffect secondEffect = availableEffects.get(secondIndex);

             
            entity.addEffect(new MobEffectInstance(
                    firstEffect,
                    30 * 20,  
                    1,  
                    false,
                    true
            ));

             
            entity.addEffect(new MobEffectInstance(
                    secondEffect,
                    30 * 20,  
                    2,  
                    false,
                    true
            ));

             
//            player.sendSystemMessage(net.minecraft.network.chat.Component.literal("你获得了抗性提升和随机增益效果！"));

             
            if (level.getGameRules().getBoolean(net.minecraft.world.level.GameRules.RULE_SENDCOMMANDFEEDBACK)) {
//                player.sendSystemMessage(net.minecraft.network.chat.Component.literal("获得效果: " +
//                        firstEffect.getDisplayName().getString() + " II, " +
//                        secondEffect.getDisplayName().getString() + " III"));
            }
        } else {
             
            entity.addEffect(new MobEffectInstance(
                    net.minecraft.world.effect.MobEffects.MOVEMENT_SPEED,
                    30 * 20,
                    1,
                    false,
                    true
            ));

            entity.addEffect(new MobEffectInstance(
                    net.minecraft.world.effect.MobEffects.DIG_SPEED,
                    30 * 20,
                    2,
                    false,
                    true
            ));
        }
    }

    return result;
}
}