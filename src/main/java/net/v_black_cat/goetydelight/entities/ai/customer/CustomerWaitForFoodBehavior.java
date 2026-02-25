package net.v_black_cat.goetydelight.entities.ai.customer;

import com.google.common.collect.ImmutableMap;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.Brain;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.MemoryStatus;
import net.minecraft.world.entity.animal.Fox;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.v_black_cat.goetydelight.entities.ai.ModMemory;
import net.v_black_cat.goetydelight.item.ModItems;

import java.util.List;


@Mod.EventBusSubscriber
public class CustomerWaitForFoodBehavior extends CustomerBehavior<PathfinderMob> {

    public CustomerWaitForFoodBehavior() {
        super(ImmutableMap.of(
                ModMemory.IS_IN_RESTAURANT.get(), MemoryStatus.VALUE_PRESENT,
                ModMemory.IS_IN_PICKUP.get(), MemoryStatus.VALUE_PRESENT
        ), 100, 600); // 等待100-600 tick (5-30秒)
    }

    @Override
    protected boolean checkExtraStartConditions(ServerLevel level, PathfinderMob owner) {
        ICustomerEntity customer = (ICustomerEntity) owner;
        Brain<?> brain = customer.goetyDelight$getCustomerBrain();
        if (brain == null) return false;
        List<ItemStack> order = customer.goetyDelight$getOrder();
        return order != null && !order.isEmpty();
    }

    @Override
    protected boolean canStillUse(ServerLevel level, PathfinderMob entity, long gameTime) {
        ICustomerEntity customer = (ICustomerEntity) entity;
        List<ItemStack> order = customer.goetyDelight$getOrder();
        boolean hasOrder = order != null && !order.isEmpty();
        return hasOrder;
    }

    @Override
    protected void start(ServerLevel level, PathfinderMob entity, long gameTime) {
    }

    @Override
    protected void tick(ServerLevel level, PathfinderMob entity, long gameTime) {
    }

    @Override
    protected void stop(ServerLevel level, PathfinderMob entity, long gameTime) {
    }

    @SubscribeEvent
    public static void onEntityInteract(PlayerInteractEvent.EntityInteract event) {
        Player player = event.getEntity();
        ItemStack stack = event.getItemStack();
        Level level = player.level();
        Entity target = event.getTarget();
        if (target instanceof ICustomerEntity customer){
            List<ItemStack> order = customer.goetyDelight$getOrder();
            if (order != null && !order.isEmpty()){
                boolean found = false;
                for (ItemStack item : order) {
                    if (ItemStack.isSameItemSameTags(item, stack)) {
                        customer.goetyDelight$getCustomerInventory().addItem(stack.copy());
                        order.remove(item);
                        stack.shrink(1);
                        level.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.ITEM_PICKUP, player.getSoundSource(), 1.0F, 1.0F);
                        found = true;
                        break;
                    }
                }
            }
        }

    }

}