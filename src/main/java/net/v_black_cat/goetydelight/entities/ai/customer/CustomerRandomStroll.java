package net.v_black_cat.goetydelight.entities.ai.customer;

import com.google.common.collect.ImmutableMap;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.Brain;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.MemoryStatus;
import net.minecraft.world.entity.ai.memory.WalkTarget;
import net.minecraft.world.entity.ai.util.LandRandomPos;
import net.minecraft.world.phys.Vec3;
import net.v_black_cat.goetydelight.entities.ICustomerEntity;

public class CustomerRandomStroll extends CustomerBehavior<PathfinderMob> {
    private final float speedModifier;
    private final int maxHorizontalDistance;
    private final int maxVerticalDistance;
    private final boolean mayStrollFromWater;

    public CustomerRandomStroll(float speedModifier) {
        this(speedModifier, 10, 7, false);
    }

    public CustomerRandomStroll(float speedModifier, int horizontalDist, int verticalDist, boolean mayStrollFromWater) {
        super(ImmutableMap.of(MemoryModuleType.WALK_TARGET, MemoryStatus.VALUE_ABSENT));
        this.speedModifier = speedModifier;
        this.maxHorizontalDistance = horizontalDist;
        this.maxVerticalDistance = verticalDist;
        this.mayStrollFromWater = mayStrollFromWater;
    }

    @Override
    protected boolean checkExtraStartConditions(ServerLevel level, PathfinderMob owner) {

        if (!this.mayStrollFromWater && owner.isInWaterOrBubble()) {
            return false;
        }

        Brain<?> brain = ((ICustomerEntity) owner).goetyDelight$getCustomerBrain();
        return brain != null && !brain.hasMemoryValue(MemoryModuleType.WALK_TARGET);
    }

    @Override
    protected void start(ServerLevel level, PathfinderMob entity, long gameTime) {

        Vec3 targetPos = LandRandomPos.getPos(entity, this.maxHorizontalDistance, this.maxVerticalDistance);

        if (targetPos != null) {
            Brain<?> brain = ((ICustomerEntity) entity).goetyDelight$getCustomerBrain();
            if (brain != null) {
                brain.setMemory(MemoryModuleType.WALK_TARGET, new WalkTarget(targetPos, this.speedModifier, 0));
            }
        }
    }
}