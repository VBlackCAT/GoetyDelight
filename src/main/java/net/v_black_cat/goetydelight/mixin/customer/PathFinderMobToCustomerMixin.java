package net.v_black_cat.goetydelight.mixin.customer;

import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.Brain;
import net.v_black_cat.goetydelight.entities.ai.customer.CustomerAi;
import net.v_black_cat.goetydelight.entities.ai.customer.ICustomerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

@Mixin(PathfinderMob.class)
public abstract class PathFinderMobToCustomerMixin implements ICustomerEntity {
    @Unique
    public Brain<PathfinderMob> goetyDelight$customerBrain = CustomerAi.makeBrain((PathfinderMob) (Object) this);



    @Override
    public void goetyDelight$setCustomerMode(boolean enabled) {
        PathfinderMob self = (PathfinderMob) (Object) this;
        self.getPersistentData().putBoolean("GoetyDelightCustomerMode", enabled);
    }

    @Override
    public boolean goetyDelight$isCustomerMode() {
        return ((PathfinderMob) (Object) this).getPersistentData().getBoolean("GoetyDelightCustomerMode");
    }

    @Override
    public Brain<PathfinderMob> goetyDelight$getCustomerBrain() {

        return goetyDelight$customerBrain;
    }

    @Override
    public void goetyDelight$setCustomerBrain(Brain<PathfinderMob> brain) {
        goetyDelight$customerBrain = brain;
    }



}