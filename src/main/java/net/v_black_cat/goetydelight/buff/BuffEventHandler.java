package net.v_black_cat.goetydelight.buff;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.event.entity.living.LivingEntityUseItemEvent;
import net.neoforged.neoforge.event.tick.EntityTickEvent;
import net.v_black_cat.goetydelight.buff.effect.BuffEffect;
import net.v_black_cat.goetydelight.init.ModAttachments;
import net.v_black_cat.goetydelight.init.ModBuffTypes;
import net.v_black_cat.goetydelight.init.ModDataComponents;

import java.util.Set;

public class BuffEventHandler {

    // 物品使用完成事件
    public static void onItemUseFinish(LivingEntityUseItemEvent.Finish event) {
        LivingEntity entity = event.getEntity();
        ItemStack stack = event.getItem();
        if (!(entity instanceof ServerPlayer serverPlayer)) return;

        ModDataComponents.BuffData buffData = stack.get(ModDataComponents.ITEM_BUFF);
        if (buffData == null) return;

        ActiveBuffs activeBuffs = serverPlayer.getData(ModAttachments.ACTIVE_BUFFS);
        if (activeBuffs == null) {
            activeBuffs = new ActiveBuffs();
            serverPlayer.setData(ModAttachments.ACTIVE_BUFFS, activeBuffs);
        }

        // 处理旧 Buff 的移除回调（如果不可叠加且已存在）
        ResourceLocation typeId = buffData.buffTypeId();
        if (activeBuffs.hasBuff(typeId)) {
            // 获取旧实例的等级（用于回调）
            int oldAmplifier = activeBuffs.getTotalAmplifier(typeId);
            BuffEffect oldEffect = ModBuffTypes.getEffect(typeId);
            if (oldEffect != null) {
                oldEffect.onRemove(serverPlayer, oldAmplifier);
            }
        }

        // 添加新 Buff
        activeBuffs.addBuff(typeId, buffData.duration(), buffData.amplifier());

        // 触发新 Buff 的 onApply 回调
        BuffEffect newEffect = ModBuffTypes.getEffect(typeId);
        if (newEffect != null) {
            newEffect.onApply(serverPlayer, buffData.amplifier());
        }

        stack.remove(ModDataComponents.ITEM_BUFF);
    }

    // 实体 tick 事件
    public static void onEntityTick(EntityTickEvent.Post event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) return;

        ActiveBuffs activeBuffs = player.getData(ModAttachments.ACTIVE_BUFFS);
        if (activeBuffs == null) return;

        // 记录 tick 前的活跃类型（用于检测移除）
        Set<ResourceLocation> beforeTick = activeBuffs.getActiveTypes();

        // 执行 tick 并获取被移除的类型
        Set<ResourceLocation> removed = activeBuffs.tickAllAndGetRemoved();

        // 对每个被移除的类型调用 onRemove
        for (ResourceLocation typeId : removed) {
            BuffEffect effect = ModBuffTypes.getEffect(typeId);
            if (effect != null) {
                // 注意：此时实例已被移除，无法获取 amplifier，可以使用默认值或最后一次记录的 amplifier
                // 简单起见，我们传递 0 或从其他方式获取（可以改进）
                effect.onRemove(player, 0);
            }
        }

        // 对依然活跃的类型执行每 tick 效果
        Set<ResourceLocation> afterTick = activeBuffs.getActiveTypes();
        for (ResourceLocation typeId : afterTick) {
            int totalAmplifier = activeBuffs.getTotalAmplifier(typeId);
            BuffEffect effect = ModBuffTypes.getEffect(typeId);
            if (effect != null) {
                effect.apply(player, totalAmplifier);
            }
        }
    }

}