package net.v_black_cat.goetydelight.entities.ai.customer.sensor;

import com.google.common.collect.ImmutableSet;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.Brain;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.NearestVisibleLivingEntities;
import net.minecraft.world.entity.ai.sensing.Sensor;
import net.minecraft.world.phys.AABB;
import net.v_black_cat.goetydelight.entities.ai.customer.ICustomerEntity;

import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.Set;

public class CustomerNearestLivingEntitySensor<T extends LivingEntity> extends Sensor<T> {
    public CustomerNearestLivingEntitySensor() {
    }

    protected void doTick(ServerLevel level, T entity) {
        AABB aabb = entity.getBoundingBox().inflate((double)this.radiusXZ(), (double)this.radiusY(), (double)this.radiusXZ());
        List<LivingEntity> list = level.getEntitiesOfClass(LivingEntity.class, aabb, (p_26717_) -> {
            return p_26717_ != entity && p_26717_.isAlive();
        });
        Objects.requireNonNull(entity);
        list.sort(Comparator.comparingDouble(entity::distanceToSqr));
        Brain<?> brain = ((ICustomerEntity)entity).goetyDelight$getCustomerBrain();
        brain.setMemory(MemoryModuleType.NEAREST_LIVING_ENTITIES, list);
        brain.setMemory(MemoryModuleType.NEAREST_VISIBLE_LIVING_ENTITIES, new NearestVisibleLivingEntities(entity, list));
    }

    protected int radiusXZ() {
        return 16;
    }

    protected int radiusY() {
        return 16;
    }

    public Set<MemoryModuleType<?>> requires() {
        return ImmutableSet.of(MemoryModuleType.NEAREST_LIVING_ENTITIES, MemoryModuleType.NEAREST_VISIBLE_LIVING_ENTITIES);
    }
}
