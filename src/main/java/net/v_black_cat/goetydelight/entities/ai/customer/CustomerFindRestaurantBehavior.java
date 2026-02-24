package net.v_black_cat.goetydelight.entities.ai.customer;

import com.google.common.collect.ImmutableMap;
import net.minecraft.core.GlobalPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.Brain;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.MemoryStatus;
import net.minecraft.world.entity.ai.memory.WalkTarget;
import net.minecraft.world.phys.Vec3;
import net.v_black_cat.goetydelight.entities.ai.ModMemory;

public class CustomerFindRestaurantBehavior extends CustomerBehavior<PathfinderMob> {
    
    public CustomerFindRestaurantBehavior() {
        super(ImmutableMap.of(
                ModMemory.IS_IN_RESTAURANT.get(), MemoryStatus.VALUE_ABSENT,
                ModMemory.NEARBY_RESTAURANT.get(), MemoryStatus.VALUE_PRESENT,
                MemoryModuleType.WALK_TARGET, MemoryStatus.VALUE_ABSENT
        ), 60, 120);
    }
    
    @Override
    protected boolean checkExtraStartConditions(ServerLevel level, PathfinderMob owner) {
        Brain brain = ((ICustomerEntity) owner).goetyDelight$getCustomerBrain();
        if (brain == null) return false;
        
        // 检查是否不在餐厅内
        boolean isInRestaurant = (Boolean) brain.getMemory(ModMemory.IS_IN_RESTAURANT.get()).orElse(false);
        if (isInRestaurant) {
            return false;
        }
        
        // 检查是否有附近的餐厅
        return brain.getMemory(ModMemory.NEARBY_RESTAURANT.get()).isPresent();
    }
    
    @Override
    protected boolean canStillUse(ServerLevel level, PathfinderMob entity, long gameTime) {
        Brain brain = ((ICustomerEntity) entity).goetyDelight$getCustomerBrain();
        if (brain == null) return false;
        
        // 检查是否仍在寻找餐厅（还未进入餐厅）
        boolean isInRestaurant = (Boolean) brain.getMemory(ModMemory.IS_IN_RESTAURANT.get()).orElse(false);
        return !isInRestaurant && brain.getMemory(ModMemory.NEARBY_RESTAURANT.get()).isPresent();
    }
    
    @Override
    protected void start(ServerLevel level, PathfinderMob entity, long gameTime) {
        Brain brain = ((ICustomerEntity) entity).goetyDelight$getCustomerBrain();
        if (brain == null) return;
        
        // 获取最近的餐厅位置
        brain.getMemory(ModMemory.NEARBY_RESTAURANT.get()).ifPresent(restaurantPos -> {
            GlobalPos globalPos = (GlobalPos) restaurantPos;
            // 设置走向餐厅入口的目标
            Vec3 targetPos = Vec3.atBottomCenterOf(globalPos.pos());
            WalkTarget walkTarget = new WalkTarget(targetPos, 1.0F, 0);
            brain.setMemory(MemoryModuleType.WALK_TARGET, walkTarget);
        });
    }
    
    @Override
    protected void tick(ServerLevel level, PathfinderMob entity, long gameTime) {
        // 行为在运行期间持续更新目标
        start(level, entity, gameTime);
    }
    
    @Override
    protected void stop(ServerLevel level, PathfinderMob entity, long gameTime) {
        Brain brain = ((ICustomerEntity) entity).goetyDelight$getCustomerBrain();
        if (brain != null) {
            // 清除行走目标
            brain.eraseMemory(MemoryModuleType.WALK_TARGET);
        }
    }
}
