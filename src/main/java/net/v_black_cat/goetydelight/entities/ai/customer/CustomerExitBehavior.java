package net.v_black_cat.goetydelight.entities.ai.customer;

import com.google.common.collect.ImmutableMap;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.MemoryStatus;
import net.minecraft.world.item.ItemStack;
import net.v_black_cat.goetydelight.entities.ai.ModMemory;

import java.util.List;
import java.util.Map;

public class CustomerExitBehavior extends CustomerBehavior<PathfinderMob>{
    public CustomerExitBehavior() {
        super(ImmutableMap.of(
                ModMemory.IS_IN_RESTAURANT.get(), MemoryStatus.VALUE_PRESENT,
                ModMemory.IS_IN_EXIT.get(), MemoryStatus.VALUE_PRESENT,
                ModMemory.FOOD_TO_PAY_LIST.get(), MemoryStatus.VALUE_PRESENT,
                MemoryModuleType.WALK_TARGET, MemoryStatus.VALUE_ABSENT
        ), 60, 120);
    }

    @Override
    protected boolean checkExtraStartConditions(ServerLevel level, PathfinderMob owner) {
        if (((ICustomerEntity) owner).goetyDelight$isHungry()){
            return false;
        }else {
            return true;
        }
    }
}
