package net.v_black_cat.goetydelight.mixin.customer;

import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.Brain;
import net.minecraft.world.item.ItemStack;
import net.v_black_cat.goetydelight.entities.ai.customer.CustomerAi;
import net.v_black_cat.goetydelight.entities.ai.customer.ICustomerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.Inject;

import java.util.List;

@Mixin(PathfinderMob.class)
public abstract class PathFinderMobToCustomerMixin implements ICustomerEntity {
    @Unique
    SimpleContainer goetyDelight$customerInventory = new SimpleContainer(8);
    @Unique
    public Brain<PathfinderMob> goetyDelight$customerBrain = CustomerAi.makeBrain((PathfinderMob) (Object) this);
    @Unique
    private boolean goetyDelight$customerMode = false;
    @Unique
    private List<ItemStack> goetyDelight$CustomerOrder = null;

    @Override
    public void goetyDelight$setCustomerMode(boolean enabled) {
        goetyDelight$customerMode = enabled;
    }

    @Override
    public boolean goetyDelight$isCustomerMode() {
        return goetyDelight$customerMode;
    }

    @Override
    public Brain<PathfinderMob> goetyDelight$getCustomerBrain() {

        return goetyDelight$customerBrain;
    }

    @Override
    public void goetyDelight$setCustomerBrain(Brain<PathfinderMob> brain) {
        goetyDelight$customerBrain = brain;
    }

    @Override
    public SimpleContainer goetyDelight$getCustomerInventory() {
        return goetyDelight$customerInventory;
    }

    @Override
    public List<ItemStack> goetyDelight$getOrder() {
        PathfinderMob pathfinderMob = (PathfinderMob) (Object) this;
        if (pathfinderMob.level().isClientSide){
            return pathfinderMob.getEntityData().get(ICustomerEntity.ENTITY_DATA_ACCESSOR);
        }else {
            return goetyDelight$CustomerOrder;
        }

    }

    @Override
    public void goetyDelight$setOrder(List<ItemStack> order) {
        PathfinderMob pathfinderMob = (PathfinderMob) (Object) this;
        if ( !(pathfinderMob.level().isClientSide)){
            this.goetyDelight$CustomerOrder = order;
            pathfinderMob.getEntityData().set(ICustomerEntity.ENTITY_DATA_ACCESSOR, order);
        }

    }
}