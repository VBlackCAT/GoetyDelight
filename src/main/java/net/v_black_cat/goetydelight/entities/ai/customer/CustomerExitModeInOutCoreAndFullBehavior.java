package net.v_black_cat.goetydelight.entities.ai.customer;

import com.google.common.collect.ImmutableMap;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.MemoryStatus;
import net.v_black_cat.goetydelight.entities.ICustomerEntity;
import net.v_black_cat.goetydelight.entities.ai.ModMemory;

public class CustomerExitModeInOutCoreAndFullBehavior extends CustomerBehavior<PathfinderMob>{
    public static final int DEFAULT_DURATION = 200;
    int durationTick = 0;
    public CustomerExitModeInOutCoreAndFullBehavior() {
        super(ImmutableMap.of(
                ModMemory.IS_IN_RESTAURANT.get(), MemoryStatus.VALUE_ABSENT
        ), DEFAULT_DURATION*2, DEFAULT_DURATION*3);
    }

    @Override
    protected boolean checkExtraStartConditions(ServerLevel level, PathfinderMob entity) {
        ICustomerEntity entity1 = (ICustomerEntity) entity;
        return !entity1.goetyDelight$isHungry();
    }

    @Override
    protected boolean canStillUse(ServerLevel level, PathfinderMob entity, long gameTime) {
        ICustomerEntity entity1 = (ICustomerEntity) entity;
        return !entity1.goetyDelight$isHungry();
    }

    @Override
    protected void start(ServerLevel level, PathfinderMob entity, long gameTime) {
        durationTick=0;

    }

    @Override
    protected void tick(ServerLevel level, PathfinderMob owner, long gameTime) {
        durationTick++;
        if (durationTick >= DEFAULT_DURATION){
            ICustomerEntity entity1 = (ICustomerEntity) owner;
            exitAndCoolDown(entity1);
        }
    }



    private static void exitAndCoolDown(ICustomerEntity customer) {
        customer.goetyDelight$setCustomerMode( false);
        customer.goetyDelight$setEnterCustomerModeCooldown(1000);
    }
}
