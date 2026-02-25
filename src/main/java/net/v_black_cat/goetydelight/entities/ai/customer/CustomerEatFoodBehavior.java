package net.v_black_cat.goetydelight.entities.ai.customer;

import com.google.common.collect.ImmutableMap;
import net.minecraft.core.particles.ItemParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.MemoryStatus;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.v_black_cat.goetydelight.entities.ai.ModMemory;

import java.util.Collections;

public class CustomerEatFoodBehavior extends CustomerBehavior<PathfinderMob> {
    private ItemStack currentFood = ItemStack.EMPTY;
    private int useDuration;
    private int useTick = 0;
    private int eatingSlot = -1; 

    public CustomerEatFoodBehavior() {
        super(ImmutableMap.of(
                ModMemory.IS_IN_RESTAURANT.get(), MemoryStatus.VALUE_PRESENT,
                ModMemory.IS_IN_DINING.get(), MemoryStatus.VALUE_PRESENT,
                MemoryModuleType.WALK_TARGET, MemoryStatus.VALUE_ABSENT
        ), 5000, 5000);
    }

    @Override
    protected boolean checkExtraStartConditions(ServerLevel level, PathfinderMob owner) {
        ICustomerEntity customer = (ICustomerEntity) owner;

        
        if (!customer.goetyDelight$getOrder().isEmpty()) {
            return false;
        }

        
        var inventory = customer.goetyDelight$getCustomerInventory();
        for (int i = 0; i < inventory.getContainerSize(); i++) {
            ItemStack stack = inventory.getItem(i);
            if (!stack.isEmpty() && stack.getItem().isEdible()) {
                return true;
            }
        }

        return false;
    }

    @Override
    public boolean canStillUse(ServerLevel level, PathfinderMob owner, long gameTime) {
        return eatingSlot != -1 && !currentFood.isEmpty();
    }

    @Override
    protected void start(ServerLevel level, PathfinderMob owner, long gameTime) {
        ICustomerEntity customer = (ICustomerEntity) owner;
        var inventory = customer.goetyDelight$getCustomerInventory();

        for (int i = 0; i < inventory.getContainerSize(); i++) {
            ItemStack item = inventory.getItem(i);
            if (!item.isEmpty() && item.getItem().isEdible()) {
                this.currentFood = item.copy(); 
                this.useDuration = item.getItem().getUseDuration(item);
                this.eatingSlot = i;
                this.useTick = 0;
                break;
            }
        }
    }

    @Override
    protected void tick(ServerLevel level, PathfinderMob owner, long gameTime) {
        if (eatingSlot == -1 || currentFood.isEmpty()) return;

        if (useTick < useDuration) {
            useTick++;

            
            if (level.random.nextFloat() < 0.5f) {
                double offsetX = (level.random.nextDouble() - 0.5) * 0.5;
                double offsetY = (level.random.nextDouble() - 0.5) * 0.5;
                double offsetZ = (level.random.nextDouble() - 0.5) * 0.5;

                
                float yawRads = owner.getYRot() * ((float)Math.PI / 180F);
                double px = owner.getX() + Math.cos(yawRads) * 0.3;
                double pz = owner.getZ() + Math.sin(yawRads) * 0.3;

                level.sendParticles(
                        new ItemParticleOption(ParticleTypes.ITEM, currentFood),
                        px + offsetX,
                        owner.getY() + owner.getEyeHeight() + offsetY,
                        pz + offsetZ,
                        5, 0, 0, 0, 0.05
                );
                owner.playSound(owner.getEatingSound(currentFood), 1.0F, 1.0F);
            }

            
            if (useTick >= useDuration) {
                ICustomerEntity customer = (ICustomerEntity) owner;
                var inventory = customer.goetyDelight$getCustomerInventory();

                
                ItemStack realStack = inventory.getItem(eatingSlot);
                if (!realStack.isEmpty()) {
                    var foodToPayList = customer.goetyDelight$getCustomerBrain().getMemory(ModMemory.FOOD_TO_PAY_LIST.get())
                            .orElse(Collections.emptyList());
                    var newList = new java.util.ArrayList<>(foodToPayList);
                    newList.add(realStack);
                    customer.goetyDelight$getCustomerBrain().setMemory(ModMemory.FOOD_TO_PAY_LIST.get(), newList);

                    ItemStack result = owner.eat(level, realStack);
                    inventory.setItem(eatingSlot, result);
                }

                
                if (inventory.isEmpty()) {
                    spawnPayment(level, owner);
                }

                
                stop(level, owner, gameTime);
            }
        }
    }

    private void spawnPayment(ServerLevel level, PathfinderMob owner) {
        ItemStack emeraldStack = new ItemStack(Items.EMERALD, 1);
        ItemEntity emeraldEntity = new ItemEntity(
                level, owner.getX(), owner.getY() + 1.5, owner.getZ(), emeraldStack
        );
        emeraldEntity.setPickUpDelay(10);
        emeraldEntity.setDeltaMovement(
                level.random.nextGaussian() * 0.1,
                level.random.nextDouble() * 0.3 + 0.2,
                level.random.nextGaussian() * 0.1
        );
        level.addFreshEntity(emeraldEntity);
    }

    @Override
    protected void stop(ServerLevel level, PathfinderMob owner, long gameTime) {
        this.currentFood = ItemStack.EMPTY;
        this.eatingSlot = -1;
        this.useTick = 0;
    }
}