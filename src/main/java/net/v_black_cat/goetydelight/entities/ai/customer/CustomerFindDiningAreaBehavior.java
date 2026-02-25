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
import net.v_black_cat.goetydelight.entities.ai.ModMemory;

import java.util.List;
import java.util.Map;

public class CustomerFindDiningAreaBehavior extends CustomerBehavior<PathfinderMob>{
    public CustomerFindDiningAreaBehavior() {
        super(ImmutableMap.of(
                ModMemory.IS_IN_RESTAURANT.get(), MemoryStatus.VALUE_PRESENT,
                ModMemory.IS_IN_DINING.get(), MemoryStatus.VALUE_ABSENT,
                ModMemory.DINING_RANGE.get(), MemoryStatus.VALUE_PRESENT,
                MemoryModuleType.WALK_TARGET, MemoryStatus.VALUE_ABSENT
        ), 60, 120);
    }
    @Override
    protected boolean checkExtraStartConditions(ServerLevel level, PathfinderMob owner) {
        ICustomerEntity owner1 = (ICustomerEntity) owner;
        List<ItemStack> itemStacks = owner1.goetyDelight$getOrder();
        SimpleContainer simpleContainer = owner1.goetyDelight$getCustomerInventory();
        if (itemStacks.isEmpty()&&!simpleContainer.isEmpty()) return true;
        return false;
    }

    @Override
    protected boolean canStillUse(ServerLevel level, PathfinderMob entity, long gameTime) {
        return false;
    }

    @Override
    protected void start(ServerLevel level, PathfinderMob entity, long gameTime) {
        Brain brain = ((ICustomerEntity) entity).goetyDelight$getCustomerBrain();
        if (brain == null) return;

        brain.getMemory(ModMemory.DINING_RANGE.get()).ifPresent(diningAreaObj -> {
            if (diningAreaObj instanceof AABB) {
                AABB diningArea = (AABB) diningAreaObj;

                Vec3 targetPos = AABBRandomPos.getPos(entity, diningArea, 3);

                if (targetPos == null) {
                    targetPos = getAreaCenter(diningArea);
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
