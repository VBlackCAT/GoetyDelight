package net.v_black_cat.goetydelight.entities.ai.customer;

import com.google.common.collect.ImmutableMap;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.Brain;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.MemoryStatus;
import net.minecraft.world.entity.ai.memory.WalkTarget;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import java.util.List;

import net.v_black_cat.goetydelight.entities.ICustomerEntity;
import net.v_black_cat.goetydelight.entities.ai.ModMemory;

public class CustomerFindPickupAreaBehavior extends CustomerBehavior<PathfinderMob> {

    public CustomerFindPickupAreaBehavior() {
        super(ImmutableMap.of(
                ModMemory.IS_IN_RESTAURANT.get(), MemoryStatus.VALUE_PRESENT,
                ModMemory.IS_IN_PICKUP.get(), MemoryStatus.VALUE_ABSENT,
                ModMemory.PICKUP_RANGE.get(), MemoryStatus.VALUE_PRESENT,
                ModMemory.IS_HUNGRY_ON_ENTER.get(), MemoryStatus.VALUE_PRESENT,
                ModMemory.FOOD_TO_PAY_LIST.get(), MemoryStatus.REGISTERED,
                ModMemory.IS_FULL_AFTER_DINING_RESTAURANT.get(), MemoryStatus.REGISTERED,
                MemoryModuleType.WALK_TARGET, MemoryStatus.VALUE_ABSENT

        ), 60, 120);
    }

    @Override
    protected boolean checkExtraStartConditions(ServerLevel level, PathfinderMob owner) {
        ICustomerEntity owner1 = (ICustomerEntity) owner;
        SimpleContainer simpleContainer = owner1.goetyDelight$getCustomerInventory();
        List<ItemStack> itemStacks = owner1.goetyDelight$getOrder();
        if (!simpleContainer.isEmpty()
                && itemStacks.isEmpty()
        ) return false;
        Brain<PathfinderMob> pathfinderMobBrain = owner1.goetyDelight$getCustomerBrain();
        if(!pathfinderMobBrain.hasMemoryValue(ModMemory.FOOD_TO_PAY_LIST.get())
                && pathfinderMobBrain.hasMemoryValue(ModMemory.IS_FULL_AFTER_DINING_RESTAURANT.get())) {
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

        brain.getMemory(ModMemory.PICKUP_RANGE.get()).ifPresent(pickupAreaObj -> {
            if (pickupAreaObj instanceof AABB) {
                AABB pickupArea = (AABB) pickupAreaObj;

                Vec3 targetPos = AABBRandomPos.getPos(entity, pickupArea, 3);

                if (targetPos == null) {
                    targetPos = getAreaCenter(pickupArea);
                }
                
                WalkTarget walkTarget = new WalkTarget(targetPos, 1.0F, 2); // 2格的接近距离
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
