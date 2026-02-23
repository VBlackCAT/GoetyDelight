package net.v_black_cat.goetydelight.mixin.customer;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.goal.GoalSelector;
import net.v_black_cat.goetydelight.entities.ai.customer.CustomerAi;
import net.v_black_cat.goetydelight.entities.ai.customer.ICustomerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(Mob.class)
public abstract class MobAiRedirectMixin {

    @Shadow
    protected abstract void customServerAiStep();


    @Redirect(method = "serverAiStep", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/ai/goal/GoalSelector;tick()V"))
    private void goetyDelight$redirectTargetTick(GoalSelector instance) {
        if (!(this instanceof ICustomerEntity customer && customer.goetyDelight$isCustomerMode())) {
            instance.tick();
        }
    }

    @Redirect(method = "serverAiStep", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/ai/goal/GoalSelector;tickRunningGoals(Z)V"))
    private void goetyDelight$redirectTickRunning(GoalSelector instance, boolean p_25355_) {
        if (!(this instanceof ICustomerEntity customer && customer.goetyDelight$isCustomerMode())) {
            instance.tickRunningGoals(p_25355_);
        }
    }

    @Redirect(
            method = "serverAiStep",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/Mob;customServerAiStep()V")
    )
    private void goetyDelight$redirectCustomAiStep(Mob instance) {
        if (instance instanceof ICustomerEntity customer && customer.goetyDelight$isCustomerMode()) {

            if (customer.goetyDelight$getCustomerBrain() != null) {
                 customer.goetyDelight$getCustomerBrain().tick((ServerLevel)instance.level(), (PathfinderMob)instance);
                CustomerAi.updateActivity((PathfinderMob)instance);
            }

        } else {
            customServerAiStep();
        }
    }
}