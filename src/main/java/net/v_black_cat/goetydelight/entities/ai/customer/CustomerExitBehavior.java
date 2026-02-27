package net.v_black_cat.goetydelight.entities.ai.customer;

import com.google.common.collect.ImmutableMap;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.Brain;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.MemoryStatus;
import net.minecraft.world.entity.ai.memory.WalkTarget;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.v_black_cat.goetydelight.entities.ICustomerEntity;
import net.v_black_cat.goetydelight.entities.ai.ModMemory;

public class CustomerExitBehavior extends CustomerBehavior<PathfinderMob>{
    public CustomerExitBehavior() {
        super(ImmutableMap.of(
                ModMemory.IS_IN_RESTAURANT.get(), MemoryStatus.VALUE_PRESENT,
                ModMemory.IS_IN_EXIT.get(), MemoryStatus.VALUE_PRESENT,
                ModMemory.ALL_RANGE.get(), MemoryStatus.VALUE_PRESENT,
                ModMemory.FOOD_TO_PAY_LIST.get(), MemoryStatus.VALUE_ABSENT,
                ModMemory.IS_FULL_AFTER_DINING_RESTAURANT.get(), MemoryStatus.VALUE_PRESENT,
                MemoryModuleType.WALK_TARGET, MemoryStatus.VALUE_ABSENT
        ), 60, 120);
    }

    @Override
    protected boolean checkExtraStartConditions(ServerLevel level, PathfinderMob owner) {
        return true;
    }

    @Override
    protected boolean canStillUse(ServerLevel level, PathfinderMob entity, long gameTime) {
        return false;
    }

    @Override
    protected void start(ServerLevel level, PathfinderMob entity, long gameTime) {
        Brain<?> brain = ((ICustomerEntity) entity).goetyDelight$getCustomerBrain();
        if (brain == null) return;
        double desiredDistance = 10;

        brain.getMemory(ModMemory.ALL_RANGE.get()).ifPresent(exitAreaObj -> {
            if (exitAreaObj instanceof AABB) {
                AABB exitArea = (AABB) exitAreaObj;
                Vec3 targetPos = null;
               int horizontalRange = (int) ((exitArea.maxX - exitArea.minX) / 2);
               int verticalRange = (int) ((exitArea.maxY - exitArea.minY) / 2);
                targetPos = AABBRandomPos.getPosAwayFromAABB(
                        entity,
                        exitArea,
                        desiredDistance,
                        horizontalRange,
                        verticalRange
                );

                if (targetPos != null) {
                    WalkTarget walkTarget = new WalkTarget(targetPos, 1.0F, 2);
                    brain.setMemory(MemoryModuleType.WALK_TARGET, walkTarget);
                }
            }
        });
    }

    @Override
    protected void stop(ServerLevel level, PathfinderMob entity, long gameTime) {
        ICustomerEntity entity1 = (ICustomerEntity) entity;
        Brain<PathfinderMob> pathfinderMobBrain = entity1.goetyDelight$getCustomerBrain();
        pathfinderMobBrain.eraseMemory(ModMemory.IS_HUNGRY_ON_ENTER.get());
    }
}
