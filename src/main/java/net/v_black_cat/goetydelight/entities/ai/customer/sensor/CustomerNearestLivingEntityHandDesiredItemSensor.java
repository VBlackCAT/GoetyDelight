package net.v_black_cat.goetydelight.entities.ai.customer.sensor;

import com.google.common.collect.ImmutableSet;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.sensing.Sensor;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.v_black_cat.goetydelight.entities.ai.ModMemory;
import net.v_black_cat.goetydelight.entities.ai.customer.ICustomerEntity;

import java.util.List;
import java.util.Set;

public class CustomerNearestLivingEntityHandDesiredItemSensor extends Sensor<PathfinderMob> {
    public CustomerNearestLivingEntityHandDesiredItemSensor() {
        super(20);
    }

    @Override
    protected void doTick(ServerLevel serverLevel, PathfinderMob pathfinderMob) {
        ICustomerEntity customer = (ICustomerEntity) pathfinderMob;
        List<LivingEntity> livingEntities = customer.goetyDelight$getCustomerBrain().getMemory(MemoryModuleType.NEAREST_LIVING_ENTITIES).orElse(List.of());
        List<ItemStack> itemStacks = customer.goetyDelight$getOrder();
        LivingEntity nearestEntityWithDesiredItem = null;
        double closestDistance = Double.MAX_VALUE;
        double maxDistanceSquared = 25.0;

        for (LivingEntity livingEntity : livingEntities) {
            if (livingEntity instanceof Player player) {
                for (ItemStack itemStack : itemStacks) {
                    if ((player.getMainHandItem().is(itemStack.getItem()) || player.getOffhandItem().is(itemStack.getItem()))) {
                        double distance = pathfinderMob.distanceToSqr(livingEntity);
                        if (distance <= maxDistanceSquared && distance < closestDistance) {
                            closestDistance = distance;
                            nearestEntityWithDesiredItem = livingEntity;
                        }
                    }
                }
            }
        }

        if (nearestEntityWithDesiredItem != null) {
            customer.goetyDelight$getCustomerBrain().setMemory(ModMemory.NEAREST_ENTITY_HOLDING_THE_DESIRED_ITEM.get(), nearestEntityWithDesiredItem);
        } else {
            customer.goetyDelight$getCustomerBrain().eraseMemory(ModMemory.NEAREST_ENTITY_HOLDING_THE_DESIRED_ITEM.get());
        }
    }

    @Override
    public Set<MemoryModuleType<?>> requires() {
        return ImmutableSet.of(
                MemoryModuleType.NEAREST_LIVING_ENTITIES,
                ModMemory.NEAREST_ENTITY_HOLDING_THE_DESIRED_ITEM.get()
        );
    }
}
