package net.v_black_cat.goetydelight.buff.effect.impl;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.v_black_cat.goetydelight.buff.effect.BuffEffect;

public class CherryBlossomPunishmentEffect implements BuffEffect {
    @Override
    public void apply(LivingEntity entity, int amplifier) {
        // 每 tick 检查是否需要触发下一次惩罚
        // 实际逻辑在事件处理器中处理，这里仅作标记
    }

    @Override
    public void onApply(LivingEntity entity, int amplifier) {
        // 首次触发：召唤闪电并造成第一次伤害
        if (entity.level() instanceof net.minecraft.server.level.ServerLevel serverLevel) {
            net.minecraft.world.entity.LightningBolt lightning = new net.minecraft.world.entity.LightningBolt(
                    net.minecraft.world.entity.EntityType.LIGHTNING_BOLT, serverLevel);
            lightning.setPos(entity.getX(), entity.getY(), entity.getZ());
            serverLevel.addFreshEntity(lightning);

            double maxHealth = entity.getAttribute(net.minecraft.world.entity.ai.attributes.Attributes.MAX_HEALTH).getValue();
            float damageAmount = (float) (maxHealth * 0.24);
            entity.hurt(entity.damageSources().magic(), damageAmount);

            if (entity instanceof Player player) {
                player.displayClientMessage(
                        net.minecraft.network.chat.Component.translatable("message.goetydelight.cherryblossomcake.punishment")
                                .withStyle(net.minecraft.ChatFormatting.DARK_RED), true);
            }
        }
    }

    @Override
    public void onRemove(LivingEntity entity, int amplifier) {
        // 移除时不处理
    }
}