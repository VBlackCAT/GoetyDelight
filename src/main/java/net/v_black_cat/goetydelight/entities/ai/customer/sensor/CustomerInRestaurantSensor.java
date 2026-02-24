package net.v_black_cat.goetydelight.entities.ai.customer.sensor;

import com.google.common.collect.ImmutableSet;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.Brain;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.sensing.Sensor;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.v_black_cat.goetydelight.entities.ai.ModMemory;
import net.v_black_cat.goetydelight.entities.ai.customer.ICustomerEntity;

import java.util.Set;

public class CustomerInRestaurantSensor extends Sensor<PathfinderMob> {
    
    public CustomerInRestaurantSensor() {
        super(20); // 每20tick更新一次
    }
    
    @Override
    protected void doTick(ServerLevel level, PathfinderMob entity) {
        Brain<?> brain = ((ICustomerEntity) entity).goetyDelight$getCustomerBrain();
        if (brain == null) return;
        
        Vec3 entityPos = entity.position();
        boolean isInRestaurant = false;
        
        // 检查是否在餐厅的任何区域内
        if (isInAnyRestaurantArea(brain, entityPos)) {
            isInRestaurant = true;
        }
        
        // 更新总体餐厅状态
        brain.setMemory(ModMemory.IS_IN_RESTAURANT.get(), isInRestaurant);
        
        // 分别更新各个区域的状态
        updateAreaStatus(brain, entityPos);
    }
    
    /**
     * 检查实体是否在餐厅的任何区域内
     */
    private boolean isInAnyRestaurantArea(Brain<?> brain, Vec3 entityPos) {
        if (isInArea(brain, ModMemory.ALL_RANGE.get(), entityPos)) {
            return true;
        }
        // 检查入口区域
        if (isInArea(brain, ModMemory.ENTRANCE_RANGE.get(), entityPos)) {
            return true;
        }
        
        // 检查取餐区域
        if (isInArea(brain, ModMemory.PICKUP_RANGE.get(), entityPos)) {
            return true;
        }
        
        // 检查用餐区域
        if (isInArea(brain, ModMemory.DINING_RANGE.get(), entityPos)) {
            return true;
        }
        
        // 检查出口区域
        if (isInArea(brain, ModMemory.EXIT_RANGE.get(), entityPos)) {
            return true;
        }
        
        return false;
    }
    
    /**
     * 检查实体是否在指定区域内
     */
    private boolean isInArea(Brain<?> brain, MemoryModuleType<AABB> areaMemory, Vec3 entityPos) {
        return brain.getMemory(areaMemory)
                .map(area -> area.contains(entityPos))
                .orElse(false);
    }
    
    /**
     * 更新各个区域的状态
     */
    private void updateAreaStatus(Brain<?> brain, Vec3 entityPos) {
        // 更新入口区域状态
        boolean isInEntrance = isInArea(brain, ModMemory.ENTRANCE_RANGE.get(), entityPos);
        brain.setMemory(ModMemory.ENTRANCE_RANGE.get(), isInEntrance ? getAreaAABB(brain, ModMemory.ENTRANCE_RANGE.get()) : null);
        
        // 更新取餐区域状态
        boolean isInPickup = isInArea(brain, ModMemory.PICKUP_RANGE.get(), entityPos);
        brain.setMemory(ModMemory.PICKUP_RANGE.get(), isInPickup ? getAreaAABB(brain, ModMemory.PICKUP_RANGE.get()) : null);
        
        // 更新用餐区域状态
        boolean isInDining = isInArea(brain, ModMemory.DINING_RANGE.get(), entityPos);
        brain.setMemory(ModMemory.DINING_RANGE.get(), isInDining ? getAreaAABB(brain, ModMemory.DINING_RANGE.get()) : null);
        
        // 更新出口区域状态
        boolean isInExit = isInArea(brain, ModMemory.EXIT_RANGE.get(), entityPos);
        brain.setMemory(ModMemory.EXIT_RANGE.get(), isInExit ? getAreaAABB(brain, ModMemory.EXIT_RANGE.get()) : null);
    }
    
    /**
     * 获取区域的AABB范围
     */
    private AABB getAreaAABB(Brain<?> brain, MemoryModuleType<AABB> areaMemory) {
        return brain.getMemory(areaMemory).orElse(null);
    }
    
    @Override
    public Set<MemoryModuleType<?>> requires() {
        return ImmutableSet.of(
                ModMemory.IS_IN_RESTAURANT.get(),
                ModMemory.IS_IN_ENTRANCE.get(),
                ModMemory.IS_IN_DINING.get(),
                ModMemory.IS_IN_PICKUP.get(),
                ModMemory.IS_IN_EXIT.get(),
                ModMemory.ALL_RANGE.get(),
                ModMemory.ENTRANCE_RANGE.get(),
                ModMemory.PICKUP_RANGE.get(),
                ModMemory.DINING_RANGE.get(),
                ModMemory.EXIT_RANGE.get()
        );
    }
}
