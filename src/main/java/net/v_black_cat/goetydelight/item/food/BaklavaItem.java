package net.v_black_cat.goetydelight.item.food;

import com.Polarice3.Goety.common.entities.boss.Vizier;
import com.Polarice3.Goety.common.items.ModItems;
import com.Polarice3.Goety.init.ModSounds;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;

import static net.v_black_cat.goetydelight.item.ModItems.COMFORT_EFFECT_SUPPLIER;
import static net.v_black_cat.goetydelight.item.ModItems.NOURISHMENT_EFFECT_SUPPLIER;
import static net.v_black_cat.goetydelight.util.TimeConverter.sToTick;

public class BaklavaItem extends Item {
    private static final long VIZIER_COOLDOWN = 36000L;
    private static final String COOLDOWN_KEY = "baklava_vizier_cooldown";

    public BaklavaItem(Properties properties) {
        super(properties);
    }

    @Override
    public int getUseDuration(ItemStack stack) {
        return (int) (8);
    }

    @Override
    public @NotNull InteractionResult interactLivingEntity(@NotNull ItemStack stack, Player player, @NotNull LivingEntity target, @NotNull InteractionHand hand) {
        if (!player.level().isClientSide) {
            CompoundTag persistentData = player.getPersistentData();
            long lastUseTime = persistentData.getLong(COOLDOWN_KEY);
            long currentTime = player.level().getGameTime();

            if (currentTime - lastUseTime < 0) {
                lastUseTime = 0;
                persistentData.putLong(COOLDOWN_KEY, lastUseTime);
            }

            if (currentTime - lastUseTime < VIZIER_COOLDOWN) {
                long remainingTime = (VIZIER_COOLDOWN - (currentTime - lastUseTime)) / 20;
                player.displayClientMessage(Component.translatable("message.goetydelight.baklava.cooldown" + remainingTime), true);
                return InteractionResult.FAIL;
            }
            if (target instanceof Vizier && currentTime - lastUseTime > VIZIER_COOLDOWN) {
                target.setHealth(0);
                ((Vizier) target).setAnimationState("death");
                player.playSound(ModSounds.VIZIER_DEATH.get(), 1.0F, 1.0F);

                ItemEntity itemEntity = new ItemEntity(target.level(),
                        target.getX(),
                        target.getY() + 2,
                        target.getZ(),
                        new ItemStack(ModItems.SOUL_RUBY.get()));
                target.level().addFreshEntity(itemEntity);
                if (!player.isCreative()) {
                    stack.shrink(1);
                }
                player.displayClientMessage(Component.translatable("message.goetydelight.baklava.vizierspoken"), true);

                // 更新冷却时间到 PersistentData
                persistentData.putLong(COOLDOWN_KEY, currentTime);
                return InteractionResult.SUCCESS;
            } else {
                // 对非 Vizier 目标添加常规效果
                target.addEffect(new MobEffectInstance(MobEffects.REGENERATION, sToTick(10), 1));
                target.addEffect(new MobEffectInstance(NOURISHMENT_EFFECT_SUPPLIER.get(), sToTick(30), 0));
                target.addEffect(new MobEffectInstance(COMFORT_EFFECT_SUPPLIER.get(), sToTick(15), 0));

                if (!player.isCreative()) {
                    stack.shrink(1);
                }

                player.displayClientMessage(Component.translatable("message.goetydelight.baklava.successs"), true);
                return InteractionResult.SUCCESS;
            }
        }

        return InteractionResult.PASS;
    }
}
