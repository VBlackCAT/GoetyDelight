package net.v_black_cat.goetydelight.entities.ai.customer;

import com.google.common.collect.ImmutableMap;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.Brain;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.MemoryStatus;
import net.minecraft.world.entity.ai.memory.WalkTarget;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.v_black_cat.goetydelight.entities.ICustomerEntity;
import net.v_black_cat.goetydelight.entities.ai.ModMemory;

public class CustomerFindRestaurantBehavior extends CustomerBehavior<PathfinderMob> {
    
    public CustomerFindRestaurantBehavior() {
        super(ImmutableMap.of(
                ModMemory.IS_IN_RESTAURANT.get(), MemoryStatus.VALUE_ABSENT,
                ModMemory.NEARBY_RESTAURANT.get(), MemoryStatus.VALUE_PRESENT,
                ModMemory.ENTRANCE_RANGE.get(), MemoryStatus.VALUE_PRESENT,
                MemoryModuleType.WALK_TARGET, MemoryStatus.VALUE_ABSENT
        ), 60, 120);
    }
    
    @Override
    protected boolean checkExtraStartConditions(ServerLevel level, PathfinderMob owner) {
        ICustomerEntity owner1 = (ICustomerEntity) owner;
        if (!owner1.goetyDelight$isHungry()){
            return false;
        }
        return true;
    }
    
    @Override
    protected boolean canStillUse(ServerLevel level, PathfinderMob entity, long gameTime) {
        return false;
    }
    
    @Override
    protected void start(ServerLevel level, PathfinderMob entity, long gameTime) {
        Brain brain = ((ICustomerEntity) entity).goetyDelight$getCustomerBrain();
        if (brain == null) return;
        
        
        brain.getMemory(ModMemory.ENTRANCE_RANGE.get()).ifPresent(entranceAreaObj -> {
            if (entranceAreaObj instanceof AABB) {
                AABB entranceArea = (AABB) entranceAreaObj;

                Vec3 targetPos = AABBRandomPos.getPos(entity, entranceArea, 3);

                if (targetPos == null) {
                    targetPos = getAreaCenter(entranceArea);
                }
                
                WalkTarget walkTarget = new WalkTarget(targetPos, 1.0F, 2); 
                brain.setMemory(MemoryModuleType.WALK_TARGET, walkTarget);
            }
        });
    }
    
    @Override
    protected void tick(ServerLevel level, PathfinderMob entity, long gameTime) {

    }
    
    @Override
    protected void stop(ServerLevel level, PathfinderMob entity, long gameTime) {

    }
    
    
    private Vec3 getAreaCenter(AABB area) {
        return new Vec3(
                (area.minX + area.maxX) / 2.0,
                (area.minY + area.maxY) / 2.0,
                (area.minZ + area.maxZ) / 2.0
        );
    }
}