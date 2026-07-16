package net.v_black_cat.goetydelight.item.food;

import com.Polarice3.Goety.common.entities.boss.Vizier;
import com.Polarice3.Goety.init.ModSounds;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.v_black_cat.goetydelight.buff.ActiveBuffs;
import net.v_black_cat.goetydelight.buff.BuffInstance;
import net.v_black_cat.goetydelight.init.ModAttachments;
import net.v_black_cat.goetydelight.init.ModBuffTypes;
import net.v_black_cat.goetydelight.util.BuffUtil;
import net.v_black_cat.goetydelight.util.TickConverterUtil;
import vectorwing.farmersdelight.common.registry.ModEffects;

import java.util.List;

public class BaklavaItem extends Item {
    private static final long VIZIER_COOLDOWN = 36000L; // 30分钟

    public BaklavaItem(Properties properties) {
        super(properties);
    }

    @Override
    public int getUseDuration(ItemStack stack, LivingEntity entity) {
        return 8;
    }

    @Override
    public InteractionResult interactLivingEntity(ItemStack stack, Player player, LivingEntity target, InteractionHand hand) {
        if (!player.level().isClientSide) {
            ResourceLocation cooldownId = ModBuffTypes.BAKLAVA_VIZIER_COOLDOWN.getId();

            // 检查冷却状态
            if (BuffUtil.hasBuff(player, cooldownId)) {
                // 获取剩余时间（从 ActiveBuffs 附件中读取）
                ActiveBuffs activeBuffs = player.getData(ModAttachments.ACTIVE_BUFFS);
                long remainingTicks = 0;
                if (activeBuffs != null) {
                    List<BuffInstance> instances = activeBuffs.getInstances(cooldownId);
                    if (!instances.isEmpty()) {
                        remainingTicks = instances.get(0).getDuration();
                    }
                }
                long remainingSeconds = remainingTicks / 20;
                player.displayClientMessage(
                        Component.translatable("message.goetydelight.baklava.cooldown", remainingSeconds),
                        true
                );
                return InteractionResult.FAIL;
            }

            if (target instanceof Vizier vizier) {
                vizier.hurt(player.damageSources().magic(), Float.MAX_VALUE);
                player.level().playSound(null, target.blockPosition(),
                        ModSounds.VIZIER_DEATH.get(), target.getSoundSource(), 1.0F, 1.0F);
                ItemEntity itemEntity = new ItemEntity(
                        target.level(),
                        target.getX(),
                        target.getY() + 2,
                        target.getZ(),
                        new ItemStack(com.Polarice3.Goety.common.items.ModItems.SOUL_RUBY.get())
                );
                target.level().addFreshEntity(itemEntity);
                if (!player.isCreative()) {
                    stack.shrink(1);
                }
                player.displayClientMessage(
                        Component.translatable("message.goetydelight.baklava.vizierspoken"),
                        true
                );
                // 施加冷却 Buff（持续 VIZIER_COOLDOWN ticks）
                BuffUtil.applyBuff(player, cooldownId, (int) VIZIER_COOLDOWN, 0);
                return InteractionResult.SUCCESS;
            }

            // 普通目标：添加原版效果
            target.addEffect(new MobEffectInstance(MobEffects.REGENERATION,
                    TickConverterUtil.sToTick(10), 1));
            target.addEffect(new MobEffectInstance(
                    ModEffects.NOURISHMENT,
                    TickConverterUtil.sToTick(30), 0));
            target.addEffect(new MobEffectInstance(
                    ModEffects.COMFORT,
                    TickConverterUtil.sToTick(15), 0));

            if (!player.isCreative()) {
                stack.shrink(1);
            }
            player.displayClientMessage(
                    Component.translatable("message.goetydelight.baklava.successs"),
                    true
            );
            return InteractionResult.SUCCESS;
        }
        return InteractionResult.PASS;
    }
}