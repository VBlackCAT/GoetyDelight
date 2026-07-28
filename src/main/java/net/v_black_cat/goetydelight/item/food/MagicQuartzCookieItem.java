package net.v_black_cat.goetydelight.item.food;

import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;

public class MagicQuartzCookieItem extends Item {

    private static List<Holder<MobEffect>> positiveEffects = null;

    public MagicQuartzCookieItem(Properties properties) {
        super(properties);
    }

    private static List<Holder<MobEffect>> getPositiveEffects() {
        if (positiveEffects == null) {
            positiveEffects = BuiltInRegistries.MOB_EFFECT.holders()
                    .filter(holder -> holder.value().getCategory() == MobEffectCategory.BENEFICIAL)
                    .collect(Collectors.toList());

            if (positiveEffects.isEmpty()) {
                positiveEffects.add(MobEffects.MOVEMENT_SPEED);
                positiveEffects.add(MobEffects.DIG_SPEED);
                positiveEffects.add(MobEffects.DAMAGE_BOOST);
            }
        }
        return positiveEffects;
    }

    @Override
    public ItemStack finishUsingItem(ItemStack stack, Level level, LivingEntity entity) {
        ItemStack result = super.finishUsingItem(stack, level, entity);

        if (!level.isClientSide && entity instanceof Player player) {
            if (player.getCooldowns().isOnCooldown(this)) {
                return result;
            }

            player.getCooldowns().addCooldown(this, 60 * 20);

            entity.addEffect(new MobEffectInstance(
                    MobEffects.DAMAGE_RESISTANCE,
                    30 * 20,
                    1,
                    false,
                    true
            ));

            // 随机两个正面效果
            List<Holder<MobEffect>> availableEffects = new ArrayList<>(getPositiveEffects());
            availableEffects.remove(MobEffects.DAMAGE_RESISTANCE);  // 避免重复

            if (availableEffects.size() >= 2) {
                Random random = new Random();

                int firstIndex = random.nextInt(availableEffects.size());
                Holder<MobEffect> firstEffect = availableEffects.get(firstIndex);
                availableEffects.remove(firstIndex);

                int secondIndex = random.nextInt(availableEffects.size());
                Holder<MobEffect> secondEffect = availableEffects.get(secondIndex);

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
            } else {
                // 保底：速度 I 和急迫 II
                entity.addEffect(new MobEffectInstance(
                        MobEffects.MOVEMENT_SPEED,
                        30 * 20,
                        1,
                        false,
                        true
                ));
                entity.addEffect(new MobEffectInstance(
                        MobEffects.DIG_SPEED,
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