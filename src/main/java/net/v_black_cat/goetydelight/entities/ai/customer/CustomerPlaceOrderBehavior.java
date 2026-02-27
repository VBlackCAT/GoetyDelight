package net.v_black_cat.goetydelight.entities.ai.customer;

import com.google.common.collect.ImmutableMap;
import net.minecraft.core.GlobalPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.Brain;
import net.minecraft.world.entity.ai.memory.MemoryStatus;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.v_black_cat.goetydelight.block.RestaurantBlockEntity;
import net.v_black_cat.goetydelight.entities.ICustomerEntity;
import net.v_black_cat.goetydelight.entities.ai.ModMemory;
import net.v_black_cat.goetydelight.item.ModItems;

import java.util.*;
import java.util.concurrent.ThreadLocalRandom;


public class CustomerPlaceOrderBehavior extends CustomerBehavior<PathfinderMob> {

    public CustomerPlaceOrderBehavior() {
        super(ImmutableMap.of(
                ModMemory.IS_IN_RESTAURANT.get(), MemoryStatus.VALUE_PRESENT,
                ModMemory.IS_IN_PICKUP.get(), MemoryStatus.VALUE_PRESENT,
                ModMemory.IS_HUNGRY_ON_ENTER.get(), MemoryStatus.VALUE_PRESENT,
                ModMemory.ITEM_CONSUMPTION_COUNT.get(), MemoryStatus.REGISTERED,
                ModMemory.IS_FULL_AFTER_DINING_RESTAURANT.get(), MemoryStatus.VALUE_ABSENT

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
        ArrayList<ItemStack> dishesList = new ArrayList<>();
        Optional<GlobalPos> memory = brain.getMemory(ModMemory.CURRENT_RESTAURANT_BLOCK_POSITION.get());
        if (memory.isPresent()) {
            GlobalPos blockPos = memory.get();
            BlockEntity blockEntity = entity.level().getBlockEntity(blockPos.pos());
            if (blockEntity instanceof RestaurantBlockEntity restaurantBlockEntity) {
                dishesList = restaurantBlockEntity.getDishesList();
            }
        }

        Map<ItemStack, Integer> foodWeights = calculateFoodWeights(dishesList, entity);
        if (foodWeights.isEmpty()) {
            return getDefaultOrder();
        }

        return selectWeightedOrder(foodWeights);
    }

    private Map<ItemStack, Integer> calculateFoodWeights(List<ItemStack> dishesList, PathfinderMob entity) {
        Map<ItemStack, Integer> foodWeights = new HashMap<>();
        if (dishesList != null && !dishesList.isEmpty()) {
            for (ItemStack dish : dishesList) {
                int weight = calculateWeight(dish, entity);
                if (weight > 0) {
                    foodWeights.put(dish, weight);
                }
            }
        }
        return foodWeights;
    }

    private List<ItemStack> selectWeightedOrder(Map<ItemStack, Integer> foodWeights) {
        List<ItemStack> weightedOrder = new ArrayList<>();
        ThreadLocalRandom random = ThreadLocalRandom.current();
        int totalWeight = foodWeights.values().stream().mapToInt(Integer::intValue).sum();

        if (totalWeight <= 0) {
            return getDefaultOrder();
        }

        while (weightedOrder.size() < 4 && !foodWeights.isEmpty()) {
            int randomValue = random.nextInt(totalWeight);
            int currentWeight = 0;

            Iterator<Map.Entry<ItemStack, Integer>> iterator = foodWeights.entrySet().iterator();
            while (iterator.hasNext()) {
                Map.Entry<ItemStack, Integer> entry = iterator.next();
                currentWeight += entry.getValue();
                if (randomValue < currentWeight) {
                    weightedOrder.add(entry.getKey());
                    iterator.remove();
                    totalWeight -= entry.getValue();
                    break;
                }
            }

            if (totalWeight <= 0) {
                break;
            }
        }

        return weightedOrder;
    }

    private List<ItemStack> getDefaultOrder() {
        List<ItemStack> order = new ArrayList<>();
        order.add(new ItemStack(ModItems.ECTOPLASMIC_MELON.get(), 2));
        order.add(new ItemStack(ModItems.SEVEN_LEAF_PUDDING.get(), 1));
        order.add(new ItemStack(ModItems.OMINOUS_ICE_CREAM.get(), 1));
        order.add(new ItemStack(ModItems.TOXIC_MEAL.get(), 1));
        return order;
    }


    private int calculateWeight(ItemStack dish, PathfinderMob entity) {
        ICustomerEntity customer = (ICustomerEntity) entity;
        Brain<PathfinderMob> brain = customer.goetyDelight$getCustomerBrain();

        int baseWeight = 10;

        if (!dish.isEdible()) {
            return 0;
        }

        FoodProperties foodProps = dish.getItem().getFoodProperties();
        if (foodProps == null) {
            return baseWeight;
        }

        int maxSatietyWeightBonus = getMaxSatietyWeightBonus(foodProps, customer);
        float repeatOrderReductionMultiplier = getRepeatOrderReductionMultiplier(dish, brain);
        float itemRarityImprovementMultiplier = getItemRarityImprovementMultiplier(dish);
        float specialIndependentMultiplier = getSpecialIndependentMultiplier(entity, dish);
        int specialBaseAdditive = getSpecialBaseAdditive(entity, dish);
        float specialPublicMultiplier = getSpecialPublicMultiplier(entity, dish);

        int adjustedWeight = (int) ((baseWeight + maxSatietyWeightBonus + specialBaseAdditive) 
                                  * repeatOrderReductionMultiplier 
                                  * itemRarityImprovementMultiplier 
                                  * specialIndependentMultiplier 
                                  * specialPublicMultiplier);
        
        return adjustedWeight;
    }

    private int getSpecialBaseAdditive(PathfinderMob entity, ItemStack dish) {
        return 1;
    }

    private float getSpecialPublicMultiplier(PathfinderMob entity, ItemStack dish) {
        return 1;
    }

    private float getSpecialIndependentMultiplier(PathfinderMob entity, ItemStack dish) {
        return 1;
    }

    private float getItemRarityImprovementMultiplier(ItemStack dish) {
        dish.getItem().getRarity(dish);
        switch (dish.getRarity()) {
            case COMMON:
                return 1.0f;
            case UNCOMMON:
                return 1.1f;
            case RARE:
                return 1.2f;
            case EPIC:
                return 1.3f;
            default:
                return 1.0f;
        }
    }

    private static float getRepeatOrderReductionMultiplier(ItemStack dish, Brain<PathfinderMob> brain) {
        float reductionMultiplier = 1.0f;
        Optional<Map<ItemStack, Integer>> memory = brain.getMemory(ModMemory.ITEM_CONSUMPTION_COUNT.get());
        if (memory.isPresent()) {
            Map<ItemStack, Integer> consumptionCount = memory.get();
            Integer count = consumptionCount.get(dish);
            if (count != null && count > 0) {
                reductionMultiplier = (float) Math.pow(0.9, count);
            }
        }
        return reductionMultiplier;
    }

    private static int getMaxSatietyWeightBonus(FoodProperties foodProps, ICustomerEntity customer) {
        int nutrition = foodProps.getNutrition();
        float saturationModifier = foodProps.getSaturationModifier();
        float foodValue = nutrition + (nutrition * saturationModifier * 2.0f);

        float appetiteMultiplier = 1.5f;
        float maxSatiety = customer.goetyDelight$getCustomerMaxSatietyValue();
        int weightBonus = (int) (maxSatiety * foodValue * appetiteMultiplier);

        if (maxSatiety > 40.0f && foodValue < 5.0f) {
            weightBonus /= 2;
        }
        return weightBonus;
    }


}