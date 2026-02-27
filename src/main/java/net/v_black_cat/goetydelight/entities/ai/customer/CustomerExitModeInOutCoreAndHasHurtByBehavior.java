package net.v_black_cat.goetydelight.entities.ai.customer;

import com.google.common.collect.ImmutableMap;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.MemoryStatus;
import net.v_black_cat.goetydelight.entities.ICustomerEntity;
import net.v_black_cat.goetydelight.entities.ai.ModMemory;

public class CustomerExitModeInOutCoreAndHasHurtByBehavior extends CustomerBehavior<PathfinderMob>{
    public CustomerExitModeInOutCoreAndHasHurtByBehavior() {
        super(ImmutableMap.of(
                ModMemory.IS_IN_RESTAURANT.get(), MemoryStatus.VALUE_ABSENT,
                MemoryModuleType.HURT_BY, MemoryStatus.VALUE_PRESENT
        ), 100, 600);
    }

    @Override
    protected boolean canStillUse(ServerLevel level, PathfinderMob entity, long gameTime) {
        return false;
    }

    @Override
    protected void start(ServerLevel level, PathfinderMob entity, long gameTime) {
        if (entity instanceof ICustomerEntity customer){
            exitAndCoolDown(customer);
        }

    }

    private static void exitAndCoolDown(ICustomerEntity customer) {
        customer.goetyDelight$setCustomerMode( false);
        customer.goetyDelight$setEnterCustomerModeCooldown(1000);
    }
}
