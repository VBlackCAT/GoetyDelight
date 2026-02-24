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

import java.util.Optional;
import net.v_black_cat.goetydelight.entities.ai.ModMemory;

public class CustomerFindPickupAreaBehavior extends CustomerBehavior<PathfinderMob> {

    public CustomerFindPickupAreaBehavior() {
        super(ImmutableMap.of(
                ModMemory.IS_IN_RESTAURANT.get(), MemoryStatus.VALUE_PRESENT,
                ModMemory.IS_IN_PICKUP.get(), MemoryStatus.VALUE_PRESENT,
                ModMemory.PICKUP_RANGE.get(), MemoryStatus.VALUE_PRESENT,
                MemoryModuleType.WALK_TARGET, MemoryStatus.VALUE_ABSENT
        ), 60, 120);
    }

    @Override
    protected boolean checkExtraStartConditions(ServerLevel level, PathfinderMob owner) {
        ICustomerEntity customerEntity = (ICustomerEntity) owner;
        Brain brain = customerEntity.goetyDelight$getCustomerBrain();
        if (brain == null) return false;
        // 检查是否在餐厅内
        Optional<?> memoryOpt = brain.getMemory(ModMemory.IS_IN_RESTAURANT.get());
        boolean isInRestaurant = false;
        if (memoryOpt.isPresent()) {
            Object memoryObj = memoryOpt.get();
            if (memoryObj instanceof Boolean) {
                isInRestaurant = (Boolean) memoryObj;
            }
        }
        if (!isInRestaurant) {
            return false;
        }

        // 检查是否有取餐区域信息
        return brain.getMemory(ModMemory.PICKUP_RANGE.get()).isPresent();
    }

    @Override
    protected boolean canStillUse(ServerLevel level, PathfinderMob entity, long gameTime) {
        Brain brain = ((ICustomerEntity) entity).goetyDelight$getCustomerBrain();
        if (brain == null) return false;

        // 检查是否仍在餐厅内且取餐区域存在
        Optional<?> memoryOpt = brain.getMemory(ModMemory.IS_IN_RESTAURANT.get());
        boolean isInRestaurant = false;
        if (memoryOpt.isPresent()) {
            Object memoryObj = memoryOpt.get();
            if (memoryObj instanceof Boolean) {
                isInRestaurant = (Boolean) memoryObj;
            }
        }
        boolean hasPickupRange = brain.getMemory(ModMemory.PICKUP_RANGE.get()).isPresent();

        if (!isInRestaurant || !hasPickupRange) {
            return false;
        }


        Optional<Boolean> isInPickupOpt = brain.getMemory(ModMemory.IS_IN_PICKUP.get());
        if (isInPickupOpt.isPresent()) {
            return !isInPickupOpt.get();
        }
        return false;
    }

    @Override
    protected void start(ServerLevel level, PathfinderMob entity, long gameTime) {
        Brain brain = ((ICustomerEntity) entity).goetyDelight$getCustomerBrain();
        if (brain == null) return;

        // 获取取餐区域范围
        brain.getMemory(ModMemory.PICKUP_RANGE.get()).ifPresent(pickupAreaObj -> {
            if (pickupAreaObj instanceof AABB) {
                AABB pickupArea = (AABB) pickupAreaObj;
                // 计算取餐区域的中心点作为目标
                Vec3 centerPos = getAreaCenter(pickupArea);
                WalkTarget walkTarget = new WalkTarget(centerPos, 1.0F, 2); // 2格的接近距离
                brain.setMemory(MemoryModuleType.WALK_TARGET, walkTarget);
            }
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

    /**
     * 获取区域的中心点
     */
    private Vec3 getAreaCenter(AABB area) {
        return new Vec3(
                (area.minX + area.maxX) / 2.0,
                (area.minY + area.maxY) / 2.0,
                (area.minZ + area.maxZ) / 2.0
        );
    }
}
