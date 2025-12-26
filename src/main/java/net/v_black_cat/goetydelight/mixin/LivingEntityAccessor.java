package net.v_black_cat.goetydelight.mixin;

import net.minecraft.world.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(LivingEntity.class)
public interface LivingEntityAccessor {
    @Accessor("dead")
    boolean isDead();

    @Accessor("dead")
    void setDead(boolean dead);
}
