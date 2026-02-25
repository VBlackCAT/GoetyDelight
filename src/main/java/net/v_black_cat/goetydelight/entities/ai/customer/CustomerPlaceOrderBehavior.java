package net.v_black_cat.goetydelight.entities.ai.customer;

import com.google.common.collect.ImmutableMap;
import com.mojang.datafixers.kinds.IdF;
import net.minecraft.client.gui.font.glyphs.BakedGlyph;
import net.minecraft.core.GlobalPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.Brain;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.MemoryStatus;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.v_black_cat.goetydelight.block.RestaurantBlockEntity;
import net.v_black_cat.goetydelight.entities.ai.ModMemory;
import net.v_black_cat.goetydelight.item.ModItems;

import java.util.*;


public class CustomerPlaceOrderBehavior extends CustomerBehavior<PathfinderMob> {

    public CustomerPlaceOrderBehavior() {
        super(ImmutableMap.of(
                ModMemory.IS_IN_RESTAURANT.get(), MemoryStatus.VALUE_PRESENT,
                ModMemory.IS_IN_PICKUP.get(), MemoryStatus.VALUE_PRESENT
        ));
    }
    @Override
    protected boolean checkExtraStartConditions(ServerLevel level, PathfinderMob owner) {
        ICustomerEntity owner1 = (ICustomerEntity) owner;
        List<ItemStack> itemStacks = owner1.goetyDelight$getOrder();
        if (itemStacks != null && !itemStacks.isEmpty()) {
            return false;
        }
        SimpleContainer simpleContainer = owner1.goetyDelight$getCustomerInventory();
        if (!simpleContainer.isEmpty()) return false;
        return true;
    }

    @Override
    protected boolean canStillUse(ServerLevel level, PathfinderMob entity, long gameTime) {
        return false;
    }

    @Override
    protected void start(ServerLevel level, PathfinderMob entity, long gameTime) {
        // 生成订单
        List<ItemStack> order = generateOrder(entity);
        
        // 设置订单到顾客实体
        ((ICustomerEntity) entity).goetyDelight$setOrder(order);
    }



    private List<ItemStack> generateOrder(PathfinderMob entity) {

        Brain<PathfinderMob> brain = ((ICustomerEntity) entity).goetyDelight$getCustomerBrain();
        ArrayList<ItemStack> dishesList;
        Optional<GlobalPos> memory = brain.getMemory(ModMemory.CURRENT_RESTAURANT_BLOCK_POSITION.get());
        if (memory.isPresent()) {
            GlobalPos blockPos = memory.get();
            BlockEntity blockEntity = entity.level().getBlockEntity(blockPos.pos());
            if (blockEntity instanceof RestaurantBlockEntity restaurantBlockEntity) {
                dishesList= restaurantBlockEntity.getDishesList();
            }
        }


        if(dishesList != null&& dishesList.size()>30){
            Map<ItemStack, Integer> foodWeights = new HashMap<>();
            for (ItemStack dish : dishesList) {
                int weight = calculateInitialWeight(dish);




                foodWeights.put(dish, weight);
            }
        }

        List<ItemStack> order = new ArrayList<>();

        order.add(new ItemStack(ModItems.ECTOPLASMIC_MELON.get(), 2));
        order.add(new ItemStack(ModItems.SEVEN_LEAF_PUDDING.get(), 1));
        order.add(new ItemStack(ModItems.OMINOUS_ICE_CREAM.get(), 1));
        order.add(new ItemStack(ModItems.TOXIC_MEAL.get(), 1));
        
        return order;
    }

    private int calculateInitialWeight(ItemStack dish) {
        return 0;
    }


}