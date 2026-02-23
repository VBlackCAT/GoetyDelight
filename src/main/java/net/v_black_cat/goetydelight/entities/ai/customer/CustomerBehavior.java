package net.v_black_cat.goetydelight.entities.ai.customer;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.behavior.Behavior;
import net.minecraft.world.entity.ai.behavior.BehaviorControl;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.MemoryStatus;

import java.util.Iterator;
import java.util.Map;

public class CustomerBehavior<E extends LivingEntity> implements BehaviorControl<E> {
    public static final int DEFAULT_DURATION = 60;
    protected final Map<MemoryModuleType<?>, MemoryStatus> entryCondition;
    private Behavior.Status status;
    private long endTimestamp;
    private final int minDuration;
    private final int maxDuration;

    public CustomerBehavior(Map<MemoryModuleType<?>, MemoryStatus> entryCondition) {
        this(entryCondition, 60);
    }

    public CustomerBehavior(Map<MemoryModuleType<?>, MemoryStatus> entryCondition, int duration) {
        this(entryCondition, duration, duration);
    }

    public CustomerBehavior(Map<MemoryModuleType<?>, MemoryStatus> entryCondition, int minDuration, int maxDuration) {
        this.status = Behavior.Status.STOPPED;
        this.minDuration = minDuration;
        this.maxDuration = maxDuration;
        this.entryCondition = entryCondition;
    }

    public Behavior.Status getStatus() {
        return this.status;
    }

    public final boolean tryStart(ServerLevel level, E owner, long gameTime) {
        if (this.hasRequiredMemories(owner) && this.checkExtraStartConditions(level, owner)) {
            this.status = Behavior.Status.RUNNING;
            int i = this.minDuration + level.getRandom().nextInt(this.maxDuration + 1 - this.minDuration);
            this.endTimestamp = gameTime + (long)i;
            this.start(level, owner, gameTime);
            return true;
        } else {
            return false;
        }
    }

    protected void start(ServerLevel level, E entity, long gameTime) {
    }

    public final void tickOrStop(ServerLevel level, E entity, long gameTime) {
        if (!this.timedOut(gameTime) && this.canStillUse(level, entity, gameTime)) {
            this.tick(level, entity, gameTime);
        } else {
            this.doStop(level, entity, gameTime);
        }

    }

    protected void tick(ServerLevel level, E owner, long gameTime) {
    }

    public final void doStop(ServerLevel level, E entity, long gameTime) {
        this.status = Behavior.Status.STOPPED;
        this.stop(level, entity, gameTime);
    }

    protected void stop(ServerLevel level, E entity, long gameTime) {
    }

    protected boolean canStillUse(ServerLevel level, E entity, long gameTime) {
        return false;
    }

    protected boolean timedOut(long gameTime) {
        return gameTime > this.endTimestamp;
    }

    protected boolean checkExtraStartConditions(ServerLevel level, E owner) {
        return true;
    }

    public String debugString() {
        return this.getClass().getSimpleName();
    }

    protected boolean hasRequiredMemories(E owner) {
        Iterator var2 = this.entryCondition.entrySet().iterator();

        MemoryModuleType memorymoduletype;
        MemoryStatus memorystatus;
        do {
            if (!var2.hasNext()) {
                return true;
            }

            Map.Entry<MemoryModuleType<?>, MemoryStatus> entry = (Map.Entry)var2.next();
            memorymoduletype = (MemoryModuleType)entry.getKey();
            memorystatus = (MemoryStatus)entry.getValue();
        } while(((ICustomerEntity)owner).goetyDelight$getCustomerBrain().checkMemory(memorymoduletype, memorystatus));

        return false;
    }

    public static enum Status {
        STOPPED,
        RUNNING;

        private Status() {
        }
    }
}
