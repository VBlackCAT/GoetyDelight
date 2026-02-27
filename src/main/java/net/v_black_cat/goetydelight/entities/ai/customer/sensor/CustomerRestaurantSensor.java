package net.v_black_cat.goetydelight.entities.ai.customer.sensor;

import com.google.common.collect.ImmutableSet;
import net.minecraft.core.BlockPos;
import net.minecraft.core.GlobalPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.Brain;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.sensing.Sensor;
import net.minecraft.world.phys.AABB;
import net.v_black_cat.goetydelight.block.RestaurantBlockEntity;
import net.v_black_cat.goetydelight.entities.ai.ModMemory;
import net.v_black_cat.goetydelight.entities.ICustomerEntity;

import java.util.Set;

public class CustomerRestaurantSensor extends Sensor<PathfinderMob> {
    private static final int SCAN_RADIUS_SQR = 128*128;

    public CustomerRestaurantSensor() {
        super(40);
    }

    @Override
    protected void doTick(ServerLevel level, PathfinderMob entity) {
        Brain<?> brain = ((ICustomerEntity) entity).goetyDelight$getCustomerBrain();
        if (brain == null) return;

        BlockPos mobPos = entity.blockPosition();
        RestaurantBlockEntity closestBE = null;
        double minCheckedDist = Double.MAX_VALUE;

        for (GlobalPos gPos : RestaurantBlockEntity.getRestaurantPositions()) {
            if (gPos.dimension() == level.dimension()) {
                BlockPos bePos = gPos.pos();
                double distSqr = bePos.distSqr(mobPos);

                if (distSqr < SCAN_RADIUS_SQR && distSqr < minCheckedDist) {
                    if (level.getBlockEntity(bePos) instanceof RestaurantBlockEntity be) {
                        closestBE = be;
                        minCheckedDist = distSqr;
                    }
                }
            }
        }

        if (closestBE != null) {
            brain.setMemory(ModMemory.NEARBY_RESTAURANT.get(), GlobalPos.of(level.dimension(), closestBE.getBlockPos()));

            brain.setMemory(ModMemory.ENTRANCE_RANGE.get(), createAABB(closestBE.getEntranceAreaRange()));
            brain.setMemory(ModMemory.ALL_RANGE.get(), createAABB(closestBE.getRangeMarker()));
            brain.setMemory(ModMemory.PICKUP_RANGE.get(), createAABB(closestBE.getPickupAreaRange()));
            brain.setMemory(ModMemory.DINING_RANGE.get(), createAABB(closestBE.getDiningAreaRange()));
            brain.setMemory(ModMemory.EXIT_RANGE.get(), createAABB(closestBE.getExitAreaRange()));
            brain.setMemory(ModMemory.CURRENT_RESTAURANT_BLOCK_POSITION.get(), GlobalPos.of(level.dimension(), closestBE.getBlockPos()));
        } else {
            eraseMemories(brain);
        }
    }

    private AABB createAABB(BlockPos[] range) {
        if (range == null || range.length < 2 || range[0] == null || range[1] == null) {
            return null;
        }
        return new AABB(range[0], range[1]).expandTowards(1, 1, 1);
    }

    private void eraseMemories(Brain<?> brain) {
        brain.eraseMemory(ModMemory.NEARBY_RESTAURANT.get());
        brain.eraseMemory(ModMemory.ENTRANCE_RANGE.get());
        brain.eraseMemory(ModMemory.PICKUP_RANGE.get());
        brain.eraseMemory(ModMemory.DINING_RANGE.get());
        brain.eraseMemory(ModMemory.EXIT_RANGE.get());
        brain.eraseMemory(ModMemory.ALL_RANGE.get());
        brain.eraseMemory(ModMemory.CURRENT_RESTAURANT_BLOCK_POSITION.get());
    }

    @Override
    public Set<MemoryModuleType<?>> requires() {
        return ImmutableSet.of(
                ModMemory.NEARBY_RESTAURANT.get(),
                ModMemory.ENTRANCE_RANGE.get(),
                ModMemory.ALL_RANGE.get(),
                ModMemory.PICKUP_RANGE.get(),
                ModMemory.DINING_RANGE.get(),
                ModMemory.EXIT_RANGE.get(),
                ModMemory.CURRENT_RESTAURANT_BLOCK_POSITION.get()
        );
    }
}