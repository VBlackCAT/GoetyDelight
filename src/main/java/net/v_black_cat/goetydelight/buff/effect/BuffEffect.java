package net.v_black_cat.goetydelight.buff.effect;

import net.minecraft.world.entity.LivingEntity;


public interface BuffEffect {
    /** 每 tick 执行的效果 */
    void apply(LivingEntity entity, int amplifier);

    /** 当 Buff 被添加时调用（包括覆盖旧实例的情况） */
    default void onApply(LivingEntity entity, int amplifier) {}

    /** 当 Buff 被移除时调用（包括过期、手动移除、被覆盖） */
    default void onRemove(LivingEntity entity, int amplifier) {}
}
