package net.v_black_cat.goetydelight.entities.ai.customer;

import com.google.common.collect.ImmutableMap;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.Brain;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.MemoryStatus;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.v_black_cat.goetydelight.entities.ai.ModMemory;
import net.v_black_cat.goetydelight.item.ModItems;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;


public class CustomerPlaceOrderBehavior extends CustomerBehavior<PathfinderMob> {

    public CustomerPlaceOrderBehavior() {
        super(ImmutableMap.of(
                ModMemory.IS_IN_RESTAURANT.get(), MemoryStatus.VALUE_PRESENT,
                ModMemory.PICKUP_RANGE.get(), MemoryStatus.VALUE_PRESENT
        ));
    }
    @Override
    protected boolean checkExtraStartConditions(ServerLevel level, PathfinderMob owner) {
        ICustomerEntity owner1 = (ICustomerEntity) owner;
        Brain<?> brain = owner1.goetyDelight$getCustomerBrain();
        if (brain == null) return false;


        boolean isInRestaurant = brain.getMemory(ModMemory.IS_IN_RESTAURANT.get())
                .map(Boolean.class::cast)
                .orElse(false);
        
        if (!isInRestaurant) {
            return false;
        }
        List<ItemStack> itemStacks = owner1.goetyDelight$getOrder();
        if (itemStacks != null && !itemStacks.isEmpty()) {
            return false;
        }


        Optional<Boolean> isInPickupOpt = brain.getMemory(ModMemory.IS_IN_PICKUP.get());
        if (isInPickupOpt.isPresent()) {
            return isInPickupOpt.get();
        }
        return false;
    }

    @Override
    protected boolean canStillUse(ServerLevel level, PathfinderMob entity, long gameTime) {
        return false;
    }

    @Override
    protected void start(ServerLevel level, PathfinderMob entity, long gameTime) {
        // 生成订单
        List<ItemStack> order = generateOrder();
        
        // 设置订单到顾客实体
        ((ICustomerEntity) entity).goetyDelight$setOrder(order);
    }


    /**
     * 生成订单 - 固定点4种食物用于测试
     */
    private List<ItemStack> generateOrder() {
        List<ItemStack> order = new ArrayList<>();

        order.add(new ItemStack(ModItems.ECTOPLASMIC_MELON.get(), 2));
        order.add(new ItemStack(ModItems.SEVEN_LEAF_PUDDING.get(), 1));
        order.add(new ItemStack(ModItems.OMINOUS_ICE_CREAM.get(), 1));
        order.add(new ItemStack(ModItems.TOXIC_MEAL.get(), 1));
        
        return order;
    }


}