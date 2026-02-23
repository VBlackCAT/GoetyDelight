package net.v_black_cat.goetydelight.entities.ai.customer;

import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.Brain;

public interface ICustomerEntity {

    void goetyDelight$setCustomerMode(boolean enabled);
    boolean goetyDelight$isCustomerMode();
    Brain<PathfinderMob> goetyDelight$getCustomerBrain();
    void goetyDelight$setCustomerBrain(Brain<PathfinderMob> brain);
}
