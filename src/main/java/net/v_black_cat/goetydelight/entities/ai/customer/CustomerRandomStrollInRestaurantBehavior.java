package net.v_black_cat.goetydelight.entities.ai.customer;

import com.google.common.collect.ImmutableMap;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.MemoryStatus;
import net.v_black_cat.goetydelight.entities.ai.ModMemory;

import java.util.Map;

public class CustomerRandomStrollInRestaurantBehavior extends CustomerBehavior<PathfinderMob>{

    public CustomerRandomStrollInRestaurantBehavior() {
        super(ImmutableMap.of(
                ModMemory.IS_IN_RESTAURANT.get(), MemoryStatus.VALUE_PRESENT,
                MemoryModuleType.WALK_TARGET, MemoryStatus.VALUE_ABSENT
        ), 60, 120);
    }



}
