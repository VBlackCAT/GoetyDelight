package net.v_black_cat.goetydelight.entities.ai.customer.sensor;

import com.google.common.collect.ImmutableSet;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.Brain;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.sensing.Sensor;
import net.minecraft.world.phys.AABB;
import net.v_black_cat.goetydelight.entities.ai.ModMemory;
import net.v_black_cat.goetydelight.entities.ai.customer.ICustomerEntity;

import java.util.Set;

public class CustomerInRestaurantSensor extends Sensor<PathfinderMob> {

    public CustomerInRestaurantSensor() {
        super(20); 
    }

    @Override
    protected void doTick(ServerLevel level, PathfinderMob entity) {
        Brain<?> brain = ((ICustomerEntity) entity).goetyDelight$getCustomerBrain();
        if (brain == null) return;


        if (isInAnyRestaurantArea(entity, brain)) {
            brain.setMemory(ModMemory.IS_IN_RESTAURANT.get(), true);
        } else {
            brain.eraseMemory(ModMemory.IS_IN_RESTAURANT.get());
        }


        updateAreaStatus(entity, brain);
    }

    
    private boolean isInAnyRestaurantArea(PathfinderMob entity, Brain<?> brain) {
        return isInArea(entity, brain, ModMemory.ALL_RANGE.get()) ||
                isInArea(entity, brain, ModMemory.ENTRANCE_RANGE.get()) ||
                isInArea(entity, brain, ModMemory.PICKUP_RANGE.get()) ||
                isInArea(entity, brain, ModMemory.DINING_RANGE.get()) ||
                isInArea(entity, brain, ModMemory.EXIT_RANGE.get());
    }


    private boolean isInArea(PathfinderMob entity, Brain<?> brain, MemoryModuleType<AABB> areaMemory) {
        return brain.getMemory(areaMemory).map(area -> isEntityInBounds(entity, area)).orElse(false);
    }

    private boolean isEntityInBounds(PathfinderMob entity, AABB area) {
        
        AABB entityBox = entity.getBoundingBox();

        
        double entityFootY = entity.getY(); 

        
        double horizontalPadding = 0.1; 
        double verticalPadding = 0.1;   

        
        boolean isHorizontallyInside =
                (area.minX + horizontalPadding) <= entityBox.minX &&
                        (area.maxX - horizontalPadding) >= entityBox.maxX &&
                        (area.minZ + horizontalPadding) <= entityBox.minZ &&
                        (area.maxZ - horizontalPadding) >= entityBox.maxZ;

        
        
        boolean isVerticallyInside =
                (area.minY + verticalPadding) <= entityFootY &&
                        (area.maxY - verticalPadding) >= entityFootY;

        
        return isHorizontallyInside && isVerticallyInside;
    }
    
    private void updateAreaStatus(PathfinderMob entity, Brain<?> brain) {
        updateSingleMemory(entity, brain, ModMemory.ENTRANCE_RANGE.get(), ModMemory.IS_IN_ENTRANCE.get());
        updateSingleMemory(entity, brain, ModMemory.PICKUP_RANGE.get(), ModMemory.IS_IN_PICKUP.get());
        updateSingleMemory(entity, brain, ModMemory.DINING_RANGE.get(), ModMemory.IS_IN_DINING.get());
        updateSingleMemory(entity, brain, ModMemory.EXIT_RANGE.get(), ModMemory.IS_IN_EXIT.get());
    }

    
    private void updateSingleMemory(PathfinderMob entity, Brain<?> brain, MemoryModuleType<AABB> rangeKey, MemoryModuleType<Boolean> boolKey) {
        if (isInArea(entity, brain, rangeKey)) {
            brain.setMemory(boolKey, true);
        } else {
            brain.eraseMemory(boolKey);
        }
    }

    @Override
    public Set<MemoryModuleType<?>> requires() {
        return ImmutableSet.of(
                ModMemory.IS_IN_RESTAURANT.get(),
                ModMemory.IS_IN_ENTRANCE.get(),
                ModMemory.IS_IN_DINING.get(),
                ModMemory.IS_IN_PICKUP.get(),
                ModMemory.IS_IN_EXIT.get(),
                ModMemory.ENTRANCE_RANGE.get(),
                ModMemory.PICKUP_RANGE.get(),
                ModMemory.DINING_RANGE.get(),
                ModMemory.EXIT_RANGE.get()
        );
    }
}