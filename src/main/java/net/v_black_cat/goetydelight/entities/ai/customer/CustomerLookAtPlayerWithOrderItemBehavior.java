package net.v_black_cat.goetydelight.entities.ai.customer;

import com.google.common.collect.ImmutableMap;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.MemoryStatus;
import net.minecraft.world.entity.ai.behavior.EntityTracker;
import net.v_black_cat.goetydelight.entities.ICustomerEntity;
import net.v_black_cat.goetydelight.entities.ai.ModMemory;


public class CustomerLookAtPlayerWithOrderItemBehavior extends CustomerBehavior<Mob> {
    
    public CustomerLookAtPlayerWithOrderItemBehavior() {
        super(ImmutableMap.of(
            ModMemory.NEAREST_ENTITY_HOLDING_THE_DESIRED_ITEM.get(), MemoryStatus.VALUE_PRESENT
        ), 60, 120);
    }

    @Override
    protected boolean checkExtraStartConditions(ServerLevel level, Mob entity) {
        return true;
    }

    @Override
    protected void start(ServerLevel level, Mob entity, long gameTime) {
        ICustomerEntity customer = (ICustomerEntity) entity;
        LivingEntity livingEntity = customer.goetyDelight$getCustomerBrain().getMemory(ModMemory.NEAREST_ENTITY_HOLDING_THE_DESIRED_ITEM.get()).get();
        customer.goetyDelight$getCustomerBrain().setMemory(MemoryModuleType.LOOK_TARGET, new EntityTracker(livingEntity, true));
    }
}