package net.v_black_cat.goetydelight.entities.ai.customer;

import com.google.common.collect.ImmutableMap;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.behavior.EntityTracker;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.MemoryStatus;
import net.minecraft.world.item.ItemStack;
import net.v_black_cat.goetydelight.entities.ai.ModMemory;

import java.util.Map;

public class CustomerPayBehavior extends CustomerBehavior<PathfinderMob> {
    public CustomerPayBehavior() {
        super(ImmutableMap.of(
                ModMemory.FOOD_TO_PAY_LIST.get(), MemoryStatus.VALUE_PRESENT,
                ModMemory.IS_IN_PICKUP.get(), MemoryStatus.VALUE_PRESENT,
                MemoryModuleType.LOOK_TARGET, MemoryStatus.REGISTERED), 60, 120);
    }

    @Override
    protected boolean canStillUse(ServerLevel level, PathfinderMob entity, long gameTime) {
        return false;
    }

    @Override
    protected boolean checkExtraStartConditions(ServerLevel level, PathfinderMob owner) {
        return true;
    }

    @Override
    protected void start(ServerLevel level, PathfinderMob entity, long gameTime) {
        ICustomerEntity customer = (ICustomerEntity) entity;
        customer.goetyDelight$getCustomerBrain().getMemory(ModMemory.FOOD_TO_PAY_LIST.get()).ifPresent(foodList -> {
            processPayment(entity, foodList);
        });
        customer.goetyDelight$getCustomerBrain().setMemory(MemoryModuleType.LOOK_TARGET, new EntityTracker(entity, true));


        super.start(level, entity, gameTime);
    }

    private void processPayment(PathfinderMob entity, java.util.List<net.minecraft.world.item.ItemStack> foodList) {
        for (ItemStack food : foodList) {
            ItemStack emeraldStack = new ItemStack(net.minecraft.world.item.Items.EMERALD, 1);

            net.minecraft.world.entity.item.ItemEntity emeraldEntity = new net.minecraft.world.entity.item.ItemEntity(
                entity.level(),
                entity.getX() + (entity.getRandom().nextDouble() - 0.5) * 2.0,
                entity.getY() + 0.5,
                entity.getZ() + (entity.getRandom().nextDouble() - 0.5) * 2.0,
                emeraldStack
            );

            emeraldEntity.setDeltaMovement(
                (entity.getRandom().nextDouble() - 0.5) * 0.2,
                0.2,
                (entity.getRandom().nextDouble() - 0.5) * 0.2
            );

            entity.level().addFreshEntity(emeraldEntity);


        }
    }

    @Override
    protected void stop(ServerLevel level, PathfinderMob entity, long gameTime) {
        ICustomerEntity customer = (ICustomerEntity) entity;
        customer.goetyDelight$getCustomerBrain().eraseMemory(ModMemory.FOOD_TO_PAY_LIST.get());
        super.stop(level, entity, gameTime);
    }
}
