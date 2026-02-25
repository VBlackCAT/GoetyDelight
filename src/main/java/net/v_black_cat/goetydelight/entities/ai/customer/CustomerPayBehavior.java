package net.v_black_cat.goetydelight.entities.ai.customer;

import com.google.common.collect.ImmutableMap;
import net.minecraft.core.GlobalPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.Brain;
import net.minecraft.world.entity.ai.behavior.EntityTracker;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.MemoryStatus;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.phys.AABB;
import net.v_black_cat.goetydelight.block.RestaurantBlockEntity;
import net.v_black_cat.goetydelight.entities.ai.ModMemory;

import java.util.*;

public class CustomerPayBehavior extends CustomerBehavior<PathfinderMob> {

    public static final float EXPERIENCE = 25;

    public CustomerPayBehavior() {
        super(ImmutableMap.of(
                ModMemory.FOOD_TO_PAY_LIST.get(), MemoryStatus.VALUE_PRESENT,
                ModMemory.IS_IN_PICKUP.get(), MemoryStatus.VALUE_PRESENT,
                ModMemory.ALL_RANGE.get(), MemoryStatus.VALUE_PRESENT,
                MemoryModuleType.LOOK_TARGET, MemoryStatus.REGISTERED,
                ModMemory.CURRENT_RESTAURANT_BLOCK_POSITION.get(), MemoryStatus.VALUE_PRESENT,
                ModMemory.RESTAURANT_OWNER_UUID_LIST.get(),MemoryStatus.REGISTERED), 60, 120);
    }

    @Override
    protected boolean canStillUse(ServerLevel level, PathfinderMob entity, long gameTime) {
        return false;
    }

    @Override
    protected boolean checkExtraStartConditions(ServerLevel level, PathfinderMob owner) {
        return true;
    }

    @Override
    protected void start(ServerLevel level, PathfinderMob entity, long gameTime) {
        ICustomerEntity customer = (ICustomerEntity) entity;

        Brain<PathfinderMob> pathfinderMobBrain = customer.goetyDelight$getCustomerBrain();


        pathfinderMobBrain.getMemory(ModMemory.FOOD_TO_PAY_LIST.get()).ifPresent(foodList -> {
            processPayment(entity, foodList);
        });
        pathfinderMobBrain.setMemory(MemoryModuleType.LOOK_TARGET, new EntityTracker(entity, true));

        super.start(level, entity, gameTime);
    }

    private void processPayment(PathfinderMob entity, java.util.List<net.minecraft.world.item.ItemStack> foodList) {


        for (ItemStack food : foodList) {

            ItemStack emeraldStack = new ItemStack(net.minecraft.world.item.Items.EMERALD, 1);

            net.minecraft.world.entity.item.ItemEntity emeraldEntity = new net.minecraft.world.entity.item.ItemEntity(
                entity.level(),
                entity.getX() + (entity.getRandom().nextDouble() - 0.5) * 2.0,
                entity.getY() + 0.5,
                entity.getZ() + (entity.getRandom().nextDouble() - 0.5) * 2.0,
                emeraldStack
            );

            emeraldEntity.setDeltaMovement(
                (entity.getRandom().nextDouble() - 0.5) * 0.2,
                0.2,
                (entity.getRandom().nextDouble() - 0.5) * 0.2
            );
            Optional<GlobalPos> memory = ((ICustomerEntity)entity).goetyDelight$getCustomerBrain().getMemory(ModMemory.CURRENT_RESTAURANT_BLOCK_POSITION.get());
            if (memory.isPresent()) {
                GlobalPos blockPos = memory.get();
                BlockEntity blockEntity = entity.level().getBlockEntity(blockPos.pos());
                if (blockEntity instanceof RestaurantBlockEntity restaurantBlockEntity) {
                    restaurantBlockEntity.addRestaurantExperience(EXPERIENCE);
                }
            }
            entity.level().addFreshEntity(emeraldEntity);


        }
    }

    @Override
    protected void stop(ServerLevel level, PathfinderMob entity, long gameTime) {
        ICustomerEntity customer = (ICustomerEntity) entity;
        Brain<PathfinderMob> pathfinderMobBrain = customer.goetyDelight$getCustomerBrain();
        pathfinderMobBrain.eraseMemory(ModMemory.FOOD_TO_PAY_LIST.get());
        Optional<AABB> memory = pathfinderMobBrain.getMemory(ModMemory.ALL_RANGE.get());
        if (memory.isPresent()) {
            AABB range = memory.get();
            java.util.List<Player> playersInRange = entity.level().getEntitiesOfClass(
                Player.class,
                range
            );
            Optional<List<UUID>> existingUuids = pathfinderMobBrain.getMemory(ModMemory.RESTAURANT_OWNER_UUID_LIST.get());
            Set<UUID> uuidSet = new HashSet<>(existingUuids.orElse(new ArrayList<>()));
            for (Player player : playersInRange) {
                uuidSet.add(player.getUUID());
            }
            pathfinderMobBrain.setMemory(ModMemory.RESTAURANT_OWNER_UUID_LIST.get(), new ArrayList<>(uuidSet));
        }
        super.stop(level, entity, gameTime);
    }
}
