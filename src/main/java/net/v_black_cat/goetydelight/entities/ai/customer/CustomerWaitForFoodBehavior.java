package net.v_black_cat.goetydelight.entities.ai.customer;

import com.google.common.collect.ImmutableMap;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.Brain;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.MemoryStatus;
import net.minecraft.world.item.ItemStack;
import net.v_black_cat.goetydelight.entities.ai.ModMemory;

import java.util.List;


public class CustomerWaitForFoodBehavior extends CustomerBehavior<PathfinderMob> {

    public CustomerWaitForFoodBehavior() {
        super(ImmutableMap.of(
                ModMemory.IS_IN_RESTAURANT.get(), MemoryStatus.VALUE_PRESENT,
                ModMemory.IS_IN_PICKUP.get(), MemoryStatus.VALUE_PRESENT
        ), 100, 600); // 等待100-600 tick (5-30秒)
    }

    @Override
    protected boolean checkExtraStartConditions(ServerLevel level, PathfinderMob owner) {
        ICustomerEntity customer = (ICustomerEntity) owner;
        Brain<?> brain = customer.goetyDelight$getCustomerBrain();
        if (brain == null) return false;

        // 检查是否在餐厅内
        boolean isInRestaurant = brain.getMemory(ModMemory.IS_IN_RESTAURANT.get())
                .map(Boolean.class::cast)
                .orElse(false);
        
        if (!isInRestaurant) {
            return false;
        }

        // 检查是否在取餐区
        boolean isInPickup = brain.getMemory(ModMemory.IS_IN_PICKUP.get())
                .map(Boolean.class::cast)
                .orElse(false);
        
        if (!isInPickup) {
            return false;
        }

        // 检查是否有订单
        List<ItemStack> order = customer.goetyDelight$getOrder();
        return order != null && !order.isEmpty();
    }

    @Override
    protected boolean canStillUse(ServerLevel level, PathfinderMob entity, long gameTime) {
        ICustomerEntity customer = (ICustomerEntity) entity;
        Brain<?> brain = customer.goetyDelight$getCustomerBrain();
        if (brain == null) return false;

        // 检查是否仍在餐厅内且在取餐区域
        boolean isInRestaurant = brain.getMemory(ModMemory.IS_IN_RESTAURANT.get())
                .map(Boolean.class::cast)
                .orElse(false);
        
        boolean isInPickup = brain.getMemory(ModMemory.IS_IN_PICKUP.get())
                .map(Boolean.class::cast)
                .orElse(false);

        // 检查是否还有订单（可能已经被取消或完成）
        List<ItemStack> order = customer.goetyDelight$getOrder();
        boolean hasOrder = order != null && !order.isEmpty();

        return isInRestaurant && isInPickup && hasOrder;
    }

    @Override
    protected void start(ServerLevel level, PathfinderMob entity, long gameTime) {
    }

    @Override
    protected void tick(ServerLevel level, PathfinderMob entity, long gameTime) {
    }

    @Override
    protected void stop(ServerLevel level, PathfinderMob entity, long gameTime) {
    }
}