package net.v_black_cat.goetydelight.compat.goetyrevelation.item;

import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.food.FoodData;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.gameevent.GameEvent;

public class AscensionLollipopItem extends Item {
    public AscensionLollipopItem(Properties properties) {
        super(properties);
    }
    @Override
    public ItemStack finishUsingItem(ItemStack stack, Level level, LivingEntity entity) {
        if (stack.isEdible()) {
            level.playSound(
                    null,
                    entity.getX(), entity.getY(), entity.getZ(),
                    entity.getEatingSound(stack),
                    SoundSource.NEUTRAL,
                    1.0F,
                    1.0F + (level.random.nextFloat() - level.random.nextFloat()) * 0.4F
            );
            FoodProperties food = stack.getFoodProperties(entity);
            if (food != null) {
                for (var pair : food.getEffects()) {
                    MobEffectInstance effect = pair.getFirst();
                    float chance = pair.getSecond();
                    if (!level.isClientSide && effect != null && level.random.nextFloat() < chance) {
                        entity.addEffect(new MobEffectInstance(effect));
                    }
                }
                if (entity instanceof Player player) {
                    FoodData data = player.getFoodData();
                    float current = data.getSaturationLevel();
                    if (Float.isNaN(current) || Float.isInfinite(current) || current < 0) {
                        data.setSaturation(0.0F);
                    }
                    data.setSaturation(data.getSaturationLevel() + 20.0F);
                }
            }
            entity.gameEvent(GameEvent.EAT);
        }

        return stack;
    }

    @Override
    public InteractionResult interactLivingEntity(ItemStack stack, Player player, LivingEntity target, InteractionHand hand) {
        Level level = player.level();
        if (level.isClientSide) {
            return target instanceof Player ? InteractionResult.SUCCESS : InteractionResult.PASS;
        }

        if (!(target instanceof Player targetPlayer)) {
            return InteractionResult.PASS;
        }

        FoodData data = targetPlayer.getFoodData();
        data.setFoodLevel(0);
        data.setSaturation(0.0F);

        return InteractionResult.SUCCESS;
    }
}
