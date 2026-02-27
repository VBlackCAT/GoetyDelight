package net.v_black_cat.goetydelight.entities.ai.customer.sensor;

import com.google.common.collect.ImmutableSet;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.Brain;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.sensing.Sensor;
import net.v_black_cat.goetydelight.entities.ICustomerEntity;

import java.util.Set;

public class CustomerHurtBySensor extends Sensor<LivingEntity> {
    public CustomerHurtBySensor() {
    }

    public Set<MemoryModuleType<?>> requires() {
        return ImmutableSet.of(MemoryModuleType.HURT_BY, MemoryModuleType.HURT_BY_ENTITY);
    }

    protected void doTick(ServerLevel level, LivingEntity p_entity) {
        if(p_entity instanceof ICustomerEntity customer){
            Brain<?> brain = customer.goetyDelight$getCustomerBrain();
            DamageSource damagesource = p_entity.getLastDamageSource();
            if (damagesource != null) {
                brain.setMemory(MemoryModuleType.HURT_BY, p_entity.getLastDamageSource());
                Entity entity = damagesource.getEntity();
                if (entity instanceof LivingEntity) {
                    brain.setMemory(MemoryModuleType.HURT_BY_ENTITY, (LivingEntity)entity);
                }
            } else {
                brain.eraseMemory(MemoryModuleType.HURT_BY);
            }

            brain.getMemory(MemoryModuleType.HURT_BY_ENTITY).ifPresent((p_289407_) -> {
                if (!p_289407_.isAlive() || p_289407_.level() != level) {
                    brain.eraseMemory(MemoryModuleType.HURT_BY_ENTITY);
                }

            });
        }

    }
}
