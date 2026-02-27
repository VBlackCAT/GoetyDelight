package net.v_black_cat.goetydelight.entities.ai.customer;

import com.google.common.collect.ImmutableMap;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.Brain;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.MemoryStatus;
import net.minecraft.world.entity.player.Player;
import net.v_black_cat.goetydelight.entities.ICustomerEntity;
import net.v_black_cat.goetydelight.entities.ai.ModMemory;

public class CustomerExitModeInRestaurantCoreBehavior extends CustomerBehavior<Mob>{

    public CustomerExitModeInRestaurantCoreBehavior() {
        super(ImmutableMap.of(
                ModMemory.IS_IN_RESTAURANT.get(), MemoryStatus.VALUE_PRESENT,
                MemoryModuleType.HURT_BY, MemoryStatus.VALUE_PRESENT
        ), 100, 600);
    }

    @Override
    protected boolean canStillUse(ServerLevel level, Mob entity, long gameTime) {
        return false;
    }

    @Override
    protected void start(ServerLevel level, Mob entity, long gameTime) {
        if (entity instanceof ICustomerEntity customer){
            Brain<PathfinderMob> brain = customer.goetyDelight$getCustomerBrain();
            DamageSource damageSource = brain.getMemory(MemoryModuleType.HURT_BY).get();
            if (damageSource.getEntity() instanceof Player player){
                if (entity.getMaxHealth()/ entity.getHealth()<0.9){
                    exitAndCoolDown(customer);
                }
            }else if (entity.getMaxHealth()/ entity.getHealth()<0.8){
                exitAndCoolDown(customer);
            }
            
        }

    }

    private static void exitAndCoolDown(ICustomerEntity customer) {
        customer.goetyDelight$setCustomerMode( false);
        customer.goetyDelight$setEnterCustomerModeCooldown(1000);
    }
}
