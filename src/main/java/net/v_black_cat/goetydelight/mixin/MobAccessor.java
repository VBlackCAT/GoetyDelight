package net.v_black_cat.goetydelight.mixin;

import net.minecraft.world.entity.Mob;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(Mob.class)
public interface MobAccessor {
    @Invoker("customServerAiStep")
    void callCustomServerAiStep();
}
