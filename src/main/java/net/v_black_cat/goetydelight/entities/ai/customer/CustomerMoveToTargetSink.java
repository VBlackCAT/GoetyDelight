package net.v_black_cat.goetydelight.entities.ai.customer;

import com.google.common.collect.ImmutableMap;
import java.util.Optional;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.Brain;
import net.minecraft.world.entity.ai.behavior.EntityTracker;
import net.minecraft.world.entity.ai.behavior.PositionTracker;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.MemoryStatus;
import net.minecraft.world.entity.ai.memory.WalkTarget;
import net.minecraft.world.entity.ai.navigation.PathNavigation;
import net.minecraft.world.entity.ai.util.DefaultRandomPos;
import net.minecraft.world.level.pathfinder.Path;
import net.minecraft.world.phys.Vec3;
import net.v_black_cat.goetydelight.entities.ICustomerEntity;

public class CustomerMoveToTargetSink extends CustomerBehavior<PathfinderMob> {
    private static final int MAX_COOLDOWN_BEFORE_RETRYING = 40;
    private int remainingCooldown;
    @Nullable
    private Path path;
    @Nullable
    private BlockPos lastTargetPos;
    private float speedModifier;

    public CustomerMoveToTargetSink() {
        this(150, 250);
    }

    public CustomerMoveToTargetSink(int minDuration, int maxDuration) {
        super(ImmutableMap.of(
                MemoryModuleType.CANT_REACH_WALK_TARGET_SINCE, MemoryStatus.REGISTERED,
                MemoryModuleType.PATH, MemoryStatus.VALUE_ABSENT,
                MemoryModuleType.WALK_TARGET, MemoryStatus.VALUE_PRESENT), minDuration, maxDuration);
    }

    protected boolean checkExtraStartConditions(ServerLevel level, PathfinderMob owner) {
        if (this.remainingCooldown > 0) {
            --this.remainingCooldown;
            return false;
        } else {
            Brain<?> brain = ((ICustomerEntity) owner).goetyDelight$getCustomerBrain();
            WalkTarget walktarget = (WalkTarget)brain.getMemory(MemoryModuleType.WALK_TARGET).get();
            boolean flag = this.reachedTarget(owner, walktarget);
            if (!flag && this.tryComputePath(owner, walktarget, level.getGameTime())) {
                this.lastTargetPos = walktarget.getTarget().currentBlockPosition();
                return true;
            } else {
                brain.eraseMemory(MemoryModuleType.WALK_TARGET);
                if (flag) {
                    brain.eraseMemory(MemoryModuleType.CANT_REACH_WALK_TARGET_SINCE);
                }

                return false;
            }
        }
    }

    protected boolean canStillUse(ServerLevel level, PathfinderMob entity, long gameTime) {
        if (this.path != null && this.lastTargetPos != null) {
            Optional<WalkTarget> optional = ((ICustomerEntity) entity).goetyDelight$getCustomerBrain().getMemory(MemoryModuleType.WALK_TARGET);
            boolean flag = (Boolean)optional.map(CustomerMoveToTargetSink::isWalkTargetSpectator).orElse(false);
            PathNavigation pathnavigation = entity.getNavigation();
            return !pathnavigation.isDone() && optional.isPresent() && !this.reachedTarget(entity, (WalkTarget)optional.get()) && !flag;
        } else {
            return false;
        }
    }

    protected void stop(ServerLevel level, PathfinderMob entity, long gameTime) {
        if (((ICustomerEntity) entity).goetyDelight$getCustomerBrain().hasMemoryValue(MemoryModuleType.WALK_TARGET) && !this.reachedTarget(entity, (WalkTarget)((ICustomerEntity) entity).goetyDelight$getCustomerBrain().getMemory(MemoryModuleType.WALK_TARGET).get()) && entity.getNavigation().isStuck()) {
            this.remainingCooldown = level.getRandom().nextInt(40);
        }

        entity.getNavigation().stop();
        ((ICustomerEntity) entity).goetyDelight$getCustomerBrain().eraseMemory(MemoryModuleType.WALK_TARGET);
        ((ICustomerEntity) entity).goetyDelight$getCustomerBrain().eraseMemory(MemoryModuleType.PATH);
        this.path = null;
    }

    protected void start(ServerLevel level, PathfinderMob entity, long gameTime) {
        ((ICustomerEntity) entity).goetyDelight$getCustomerBrain().setMemory(MemoryModuleType.PATH, this.path);
        entity.getNavigation().moveTo(this.path, (double)this.speedModifier);
    }

    protected void tick(ServerLevel level, PathfinderMob owner, long gameTime) {
        Path path = owner.getNavigation().getPath();
        Brain<?> brain = ((ICustomerEntity) owner).goetyDelight$getCustomerBrain();
        if (this.path != path) {
            this.path = path;
            brain.setMemory(MemoryModuleType.PATH, path);
        }

        if (path != null && this.lastTargetPos != null) {
            WalkTarget walktarget = (WalkTarget)brain.getMemory(MemoryModuleType.WALK_TARGET).get();
            if (walktarget.getTarget().currentBlockPosition().distSqr(this.lastTargetPos) > 4.0 && this.tryComputePath(owner, walktarget, level.getGameTime())) {
                this.lastTargetPos = walktarget.getTarget().currentBlockPosition();
                this.start(level, owner, gameTime);
            }
        }

    }

    private boolean tryComputePath(PathfinderMob mob, WalkTarget target, long time) {
        BlockPos blockpos = target.getTarget().currentBlockPosition();
        this.path = mob.getNavigation().createPath(blockpos, 0);
        this.speedModifier = target.getSpeedModifier();
        Brain<?> brain = ((ICustomerEntity) mob).goetyDelight$getCustomerBrain();
        if (this.reachedTarget(mob, target)) {
            brain.eraseMemory(MemoryModuleType.CANT_REACH_WALK_TARGET_SINCE);
        } else {
            boolean flag = this.path != null && this.path.canReach();
            if (flag) {
                brain.eraseMemory(MemoryModuleType.CANT_REACH_WALK_TARGET_SINCE);
            } else if (!brain.hasMemoryValue(MemoryModuleType.CANT_REACH_WALK_TARGET_SINCE)) {
                brain.setMemory(MemoryModuleType.CANT_REACH_WALK_TARGET_SINCE, time);
            }

            if (this.path != null) {
                return true;
            }

            Vec3 vec3 = DefaultRandomPos.getPosTowards((PathfinderMob)mob, 10, 7, Vec3.atBottomCenterOf(blockpos), 1.5707963705062866);
            if (vec3 != null) {
                this.path = mob.getNavigation().createPath(vec3.x, vec3.y, vec3.z, 0);
                return this.path != null;
            }
        }

        return false;
    }

    private boolean reachedTarget(PathfinderMob mob, WalkTarget target) {
        return target.getTarget().currentBlockPosition().distManhattan(mob.blockPosition()) <= target.getCloseEnoughDist();
    }

    private static boolean isWalkTargetSpectator(WalkTarget walkTarget) {
        PositionTracker positiontracker = walkTarget.getTarget();
        if (positiontracker instanceof EntityTracker entitytracker) {
            return entitytracker.getEntity().isSpectator();
        } else {
            return false;
        }
    }
}
