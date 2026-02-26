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
import net.v_black_cat.goetydelight.entities.ai.ModMemory;

import java.util.Map;

public class CustomerFindExitBehavior extends CustomerBehavior<PathfinderMob> {
    public CustomerFindExitBehavior() {
        super(ImmutableMap.of(
                ModMemory.IS_IN_RESTAURANT.get(), MemoryStatus.VALUE_PRESENT,
                ModMemory.IS_IN_EXIT.get(), MemoryStatus.VALUE_ABSENT,
                ModMemory.EXIT_RANGE.get(), MemoryStatus.VALUE_PRESENT,
                ModMemory.FOOD_TO_PAY_LIST.get(), MemoryStatus.VALUE_ABSENT,
                MemoryModuleType.WALK_TARGET, MemoryStatus.VALUE_ABSENT

        ), 60, 120);
    }

    @Override
    protected void start(ServerLevel level, PathfinderMob entity, long gameTime) {
        Brain brain = ((ICustomerEntity) entity).goetyDelight$getCustomerBrain();
        if (brain == null) return;

        brain.getMemory(ModMemory.EXIT_RANGE.get()).ifPresent(exitAreaObj -> {
            if (exitAreaObj instanceof AABB) {
                AABB exitArea = (AABB) exitAreaObj;

                Vec3 targetPos = AABBRandomPos.getPos(entity, exitArea, 3);

                if (targetPos == null) {
                    targetPos = getAreaCenter(exitArea);
                }

                WalkTarget walkTarget = new WalkTarget(targetPos, 1.0F, 2);
                brain.setMemory(MemoryModuleType.WALK_TARGET, walkTarget);
            }
        });
    }
    private Vec3 getAreaCenter(AABB area) {
        return new Vec3(
                (area.minX + area.maxX) / 2.0,
                (area.minY + area.maxY) / 2.0,
                (area.minZ + area.maxZ) / 2.0
        );
    }
    @Override
    protected boolean canStillUse(ServerLevel level, PathfinderMob entity, long gameTime) {
        return false;
    }
}
