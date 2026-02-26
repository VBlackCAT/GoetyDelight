package net.v_black_cat.goetydelight.entities.ai.customer;

import com.google.common.collect.ImmutableMap;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.MemoryStatus;
import net.v_black_cat.goetydelight.entities.ICustomerEntity;

public class CustomerLookAtTargetSink extends CustomerBehavior<Mob> {
    public CustomerLookAtTargetSink(int minDuration, int maxDuration) {
        super(ImmutableMap.of(MemoryModuleType.LOOK_TARGET, MemoryStatus.VALUE_PRESENT), minDuration, maxDuration);
    }

    protected boolean canStillUse(ServerLevel level, Mob entity, long gameTime) {
        return true;
    }

    protected void stop(ServerLevel level, Mob entity, long gameTime) {
        ((ICustomerEntity)entity).goetyDelight$getCustomerBrain().eraseMemory(MemoryModuleType.LOOK_TARGET);
    }

    protected void tick(ServerLevel level, Mob owner, long gameTime) {
        ((ICustomerEntity)owner).goetyDelight$getCustomerBrain().getMemory(MemoryModuleType.LOOK_TARGET).ifPresent((p_23486_) -> {
            owner.getLookControl().setLookAt(p_23486_.currentPosition());
        });
    }
}