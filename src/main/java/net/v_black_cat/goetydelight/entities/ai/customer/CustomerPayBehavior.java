package net.v_black_cat.goetydelight.entities.ai.customer;

import com.Polarice3.Goety.common.items.ModItems;
import com.google.common.collect.ImmutableMap;
import net.minecraft.core.GlobalPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.Brain;
import net.minecraft.world.entity.ai.behavior.EntityTracker;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.MemoryStatus;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.phys.AABB;
import net.v_black_cat.goetydelight.block.RestaurantBlockEntity;
import net.v_black_cat.goetydelight.entities.ICustomerEntity;
import net.v_black_cat.goetydelight.entities.ai.ModMemory;

import java.util.*;

public class CustomerPayBehavior extends CustomerBehavior<PathfinderMob> {

    public static final float EXPERIENCE = 25;

    public CustomerPayBehavior() {
        super(ImmutableMap.of(
                ModMemory.FOOD_TO_PAY_LIST.get(), MemoryStatus.VALUE_PRESENT,
                ModMemory.IS_IN_PICKUP.get(), MemoryStatus.VALUE_PRESENT,
                ModMemory.IS_FULL_AFTER_DINING_RESTAURANT.get(), MemoryStatus.REGISTERED,
                ModMemory.ALL_RANGE.get(), MemoryStatus.VALUE_PRESENT,
                MemoryModuleType.LOOK_TARGET, MemoryStatus.REGISTERED,
                ModMemory.PAID_LOOT_COUNT.get(), MemoryStatus.REGISTERED,
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


        Optional<GlobalPos> globalPos = ((ICustomerEntity) entity).goetyDelight$getCustomerBrain().getMemory(ModMemory.CURRENT_RESTAURANT_BLOCK_POSITION.get());
        BlockEntity blockEntity = null;
        if (globalPos.isPresent()) {
            GlobalPos blockPos = globalPos.get();
            blockEntity = entity.level().getBlockEntity(blockPos.pos());
        }
        pathfinderMobBrain.getMemory(ModMemory.FOOD_TO_PAY_LIST.get()).ifPresent(foodList -> {
            processPayment(entity, foodList);
        });

        Optional<Integer> countMemory = pathfinderMobBrain.getMemory(ModMemory.PAID_LOOT_COUNT.get());
        int paidLootCount = countMemory.orElse(0);
        if (paidLootCount <2){
            if (customer.goetyDelight$getCustomerSatietyValue()/customer.goetyDelight$getCustomerMaxSatietyValue()>0.3*(paidLootCount+1)){
                payLootPart(entity,pathfinderMobBrain,paidLootCount,blockEntity);
            }
        }
        if (customer.goetyDelight$isFull()){
            payLoot(entity,pathfinderMobBrain,paidLootCount,blockEntity);
        }


        super.start(level, entity, gameTime);
    }

    private void payLoot(PathfinderMob entity,
                         Brain<PathfinderMob> brain,
                         int paidLootCount,
                         BlockEntity blockEntity) {

        Optional<Map<ItemStack, Integer>> memory =
                brain.getMemory(ModMemory.ITEM_CONSUMPTION_COUNT.get());

        Map<ItemStack, Integer> consumptionMap =
                memory.orElse(new HashMap<>());

        Map<Rarity, Integer> rarityCountMap = new HashMap<>();
        for (Map.Entry<ItemStack, Integer> entry : consumptionMap.entrySet()) {
            Rarity rarity = entry.getKey().getRarity();
            rarityCountMap.merge(rarity, entry.getValue(), Integer::sum);
        }

        List<ItemStack> loot = getLoot(entity, brain);

        List<ItemStack> filteredLoot = new ArrayList<>();

        Map<Rarity, Integer> remaining = new HashMap<>(rarityCountMap);

        for (ItemStack stack : loot) {
            Rarity rarity = stack.getRarity();

            if (!remaining.containsKey(rarity)) continue;

            int allowed = remaining.get(rarity);
            if (allowed <= 0) continue;

            int giveCount = Math.min(stack.getCount(), allowed);

            if (giveCount > 0) {
                ItemStack copy = stack.copy();
                copy.setCount(giveCount);
                filteredLoot.add(copy);
                remaining.put(rarity, allowed - giveCount);
            }
        }

        if (blockEntity instanceof RestaurantBlockEntity restaurant) {

            float bonusMultiplier = getRestaurantLevelBonusMultiplier(restaurant);

            int totalCount = filteredLoot.stream()
                    .mapToInt(ItemStack::getCount)
                    .sum();

            int bonusCount = (int) (totalCount * bonusMultiplier) - totalCount;

            if (bonusCount > 0 && !filteredLoot.isEmpty()) {

                RandomSource random = entity.getRandom();

                for (int i = 0; i < bonusCount; i++) {
                    ItemStack base =
                            filteredLoot.get(random.nextInt(filteredLoot.size()));
                    ItemStack extra = base.copy();
                    extra.setCount(1);
                    filteredLoot.add(extra);
                }
            }
        }

        for (ItemStack stack : filteredLoot) {
            dropPay(entity, stack);
        }

        brain.setMemory(ModMemory.PAID_LOOT_COUNT.get(), paidLootCount + 1);
        brain.eraseMemory(ModMemory.ITEM_CONSUMPTION_COUNT.get());
    }
    private void payLootPart(PathfinderMob entity,
                             Brain<PathfinderMob> brain,
                             int paidLootCount,
                             BlockEntity blockEntity) {

        Optional<Map<ItemStack, Integer>> memory =
                brain.getMemory(ModMemory.ITEM_CONSUMPTION_COUNT.get());

        Map<ItemStack, Integer> consumptionMap =
                memory.orElse(new HashMap<>());

        Map<Rarity, Integer> rarityCountMap = new HashMap<>();
        for (Map.Entry<ItemStack, Integer> entry : consumptionMap.entrySet()) {
            Rarity rarity = entry.getKey().getRarity();
            rarityCountMap.merge(rarity, entry.getValue(), Integer::sum);
        }

        List<ItemStack> loot = getLoot(entity, brain);

        List<ItemStack> filteredLoot = new ArrayList<>();
        Map<Rarity, Integer> remaining = new HashMap<>(rarityCountMap);

        for (ItemStack stack : loot) {
            Rarity rarity = stack.getRarity();

            if (!remaining.containsKey(rarity)) continue;

            int allowed = remaining.get(rarity);
            if (allowed <= 0) continue;

            int giveCount = Math.min(stack.getCount(), allowed);

            if (giveCount > 0) {
                ItemStack copy = stack.copy();
                copy.setCount(giveCount);
                filteredLoot.add(copy);
                remaining.put(rarity, allowed - giveCount);
            }
        }

        if (filteredLoot.isEmpty()) return;

        float bonusMultiplier = 1.0f;
        if (blockEntity instanceof RestaurantBlockEntity restaurant) {
            bonusMultiplier = getRestaurantLevelBonusMultiplier(restaurant);
        }

        int totalCount = filteredLoot.stream()
                .mapToInt(ItemStack::getCount)
                .sum();

        int payCount = (int) (totalCount * 0.2f * bonusMultiplier);

        if (payCount <= 0) return;

        RandomSource random = entity.getRandom();

        // 打散成单个单位方便抽取
        List<ItemStack> singleItems = new ArrayList<>();
        for (ItemStack stack : filteredLoot) {
            for (int i = 0; i < stack.getCount(); i++) {
                ItemStack single = stack.copy();
                single.setCount(1);
                singleItems.add(single);
            }
        }

        Collections.shuffle(singleItems, new Random());

        for (int i = 0; i < Math.min(payCount, singleItems.size()); i++) {
            dropPay(entity, singleItems.get(i));
        }

        brain.setMemory(ModMemory.PAID_LOOT_COUNT.get(), paidLootCount + 1);
    }
    protected List<ItemStack> getLoot(PathfinderMob entity, Brain<PathfinderMob> brain) {
        Optional<List<UUID>> memory = brain.getMemory(ModMemory.RESTAURANT_OWNER_UUID_LIST.get());
        LivingEntity sourceEntity = null;
        if (memory.isPresent()) {
            List<UUID> uuids = memory.get();
            for (UUID uuid : uuids) {
                sourceEntity = entity.level().getPlayerByUUID(uuid);
                if (sourceEntity != null) {
                    break;
                }
            }
        }
        if (sourceEntity == null) {
            sourceEntity = entity;
        }
        LootTable loottable = entity.level().getServer().getLootData().getLootTable(entity.getLootTable());
        DamageSource starveDamageSource = entity.level().damageSources().starve();
        LootParams.Builder lootparams$builder = (new LootParams.Builder((ServerLevel)entity.level()))
                .withParameter(LootContextParams.THIS_ENTITY, entity)
                .withParameter(LootContextParams.ORIGIN, entity.position())
                .withParameter(LootContextParams.DAMAGE_SOURCE, starveDamageSource)
                .withOptionalParameter(LootContextParams.KILLER_ENTITY, sourceEntity)
                .withOptionalParameter(LootContextParams.DIRECT_KILLER_ENTITY, sourceEntity);
        if (sourceEntity instanceof Player player) {
            lootparams$builder = lootparams$builder
                    .withParameter(LootContextParams.LAST_DAMAGE_PLAYER, player)
                    .withLuck(player.getLuck());
        }
        LootParams lootparams = lootparams$builder.create(LootContextParamSets.ENTITY);
        return loottable.getRandomItems(lootparams, entity.getLootTableSeed());
    }

    private void processPayment(PathfinderMob entity, List<ItemStack> foodList) {

        Optional<GlobalPos> memory = ((ICustomerEntity) entity).goetyDelight$getCustomerBrain().getMemory(ModMemory.CURRENT_RESTAURANT_BLOCK_POSITION.get());
        BlockEntity blockEntity = null;
        if (memory.isPresent()) {
            GlobalPos blockPos = memory.get();
            blockEntity = entity.level().getBlockEntity(blockPos.pos());
        }
        for (ItemStack food : foodList) {

            ItemStack emeraldStack = new ItemStack(Items.EMERALD, 1);

            ItemEntity emeraldEntity = new ItemEntity(
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

            payMoney(entity, food, blockEntity);

            if (blockEntity != null){
                payResturantExp(blockEntity);
            }


            entity.level().addFreshEntity(emeraldEntity);


        }
    }
    private float getRestaurantLevelBonusMultiplier(RestaurantBlockEntity restaurantBlockEntity) {
        int restaurantLevel = restaurantBlockEntity.getRestaurantLevel();
        float bonusMultiplier = 1.0f + (restaurantLevel * 0.05f);
        return Math.min(bonusMultiplier, 2.0f);
    }

    private void payMoney(PathfinderMob entity, ItemStack food, BlockEntity blockEntity) {
        Map<ItemStack, Integer> moneyWeights = new HashMap<>();
        moneyWeights.put(new ItemStack(Items.EMERALD, 1), 60);
        moneyWeights.put(new ItemStack(Items.GOLD_INGOT, 1), 20);
        moneyWeights.put(new ItemStack(Items.DIAMOND, 1), 9);
        moneyWeights.put(new ItemStack(ModItems.ECTOPLASM.get(), 1), 10);
        moneyWeights.put(new ItemStack(ModItems.TREASURE_POUCH.get(), 1), 1);
        RandomSource random = entity.getRandom();
         int moneyCount = getMoneyCountByFood(food);
        ArrayList<ItemStack> itemStacks = drawItemsByWeight(moneyWeights, moneyCount, random);
        if (blockEntity != null && blockEntity instanceof RestaurantBlockEntity restaurantBlockEntity){
            float bonusMultiplier = getRestaurantLevelBonusMultiplier(restaurantBlockEntity);
            int size = itemStacks.size();
            int count = (int) ((size * bonusMultiplier)-size);
            if (!itemStacks.isEmpty()) {
                if (count > 0) {
                    ArrayList<ItemStack> originalItems = new ArrayList<>(itemStacks);
                    for (int i = 0; i < count; i++) {
                        int randomIndex = random.nextInt(originalItems.size());
                        itemStacks.add(originalItems.get(randomIndex).copy());
                    }
                }
            }
        }
        for (ItemStack itemStack : itemStacks) {
            dropPay(entity, itemStack);
        }





    }

    private static void dropPay(PathfinderMob entity, ItemStack itemStack) {
        ItemEntity entity1 = new ItemEntity(entity.level(), entity.getX(), entity.getY()+ entity.getEyeHeight(), entity.getZ(), itemStack);
        entity.level().addFreshEntity(entity1);
        entity1.setDeltaMovement(
                entity.getLookAngle().x * 0.1,
                0.1,
                entity.getLookAngle().z * 0.1
        );
    }

    private int getMoneyCountByFood(ItemStack food) {
        food.getItem().getRarity(food);
        switch (food.getRarity()) {
            case COMMON:
                return 5;
            case UNCOMMON:
                return 8;
            case RARE:
                return 10;
            case EPIC:
                return 15;
            default:
                return 5;
        }
    }

    private ArrayList<ItemStack> drawItemsByWeight(Map<ItemStack, Integer> weights, int count, RandomSource random) {
        ArrayList<ItemStack> result = new ArrayList<>();
        int totalWeight = weights.values().stream().mapToInt(Integer::intValue).sum();
        
        for (int i = 0; i < count; i++) {
            int randomValue = random.nextInt(totalWeight);
            int currentWeight = 0;
            
            for (Map.Entry<ItemStack, Integer> entry : weights.entrySet()) {
                currentWeight += entry.getValue();
                if (randomValue < currentWeight) {
                    result.add(entry.getKey().copy());
                    break;
                }
            }
        }
        
        return result;
    }




    private static void payResturantExp(BlockEntity blockEntity) {
            if (blockEntity instanceof RestaurantBlockEntity restaurantBlockEntity) {
                restaurantBlockEntity.addRestaurantExperience(EXPERIENCE);
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
            List<Player> playersInRange = entity.level().getEntitiesOfClass(
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
