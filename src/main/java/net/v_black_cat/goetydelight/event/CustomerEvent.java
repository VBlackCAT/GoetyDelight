package net.v_black_cat.goetydelight.event;

import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraftforge.common.Tags;
import net.minecraftforge.event.entity.living.LivingChangeTargetEvent;
import net.minecraftforge.event.entity.living.LivingDropsEvent;
import net.minecraftforge.event.entity.living.LivingEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.v_black_cat.goetydelight.entities.ICustomerEntity;
import net.v_black_cat.goetydelight.entities.ai.ModMemory;
import net.v_black_cat.goetydelight.util.EntityTagChecker;

import java.util.*;

import static net.v_black_cat.goetydelight.item.ItemBlackList.isBlackListed;

@Mod.EventBusSubscriber
public class CustomerEvent {
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
                if (stack.isEmpty()){
                    customer.goetyDelight$setOrder(order);
                }else {
                    for (ItemStack item : order) {
                        if (ItemStack.isSameItemSameTags(item, stack)) {
                            customer.goetyDelight$getCustomerInventory().addItem(stack.split(1));
                            order.remove(item);
                            customer.goetyDelight$setOrder(order);
                            level.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.ITEM_PICKUP, player.getSoundSource(), 1.0F, 1.0F);
                            found = true;
                            break;
                        }
                    }
                }

            }
        }

    }
    @SubscribeEvent
    static void onLivingTick(LivingEvent.LivingTickEvent event) {
        if (event.getEntity().level().isClientSide()) {
            return;
        }
        if (event.getEntity() instanceof ICustomerEntity customerEntity) {
            LivingEntity livingEntity = (LivingEntity) customerEntity;
            if (livingEntity.tickCount % 240 == 0) {
                customerEntity.goetyDelight$SubtractionCustomerSatietyValue(customerEntity.goetyDelight$getCustomerMaxSatietyValue() * 0.01f);
            }
        }
    }

    @SubscribeEvent
    public static void onLivingDrops(LivingDropsEvent event) {
        if (event.getEntity() instanceof ICustomerEntity entity){
            if (entity.goetyDelight$isCustomerMode()) {
                event.getDrops().clear();
            }
        }
    }

    @SubscribeEvent
    public static void onLivingChangeTarget(LivingChangeTargetEvent event) {
        LivingEntity entity1 = event.getEntity();
        if (entity1 instanceof ICustomerEntity customer){
            LivingEntity target = event.getNewTarget();
            if (entity1 instanceof Mob mob){
                if (mob.getLastHurtByMob() != target) {
                    Optional<List<UUID>> memory = customer.goetyDelight$getCustomerBrain().getMemory(ModMemory.RESTAURANT_OWNER_UUID_LIST.get());
                    if (memory.isPresent() && memory.get().contains(target.getUUID())) {
                        if (event.getTargetType() == LivingChangeTargetEvent.LivingTargetType.MOB_TARGET) {
                            event.setNewTarget(null);
                        } else {
                            event.setCanceled(true);
                        }
                    }
                }
            }

        }
    }



}
