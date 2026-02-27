package net.v_black_cat.goetydelight.entities.ai.customer;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Dynamic;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.Tag;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.Brain;
import net.minecraft.world.entity.ai.behavior.*;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.MemoryStatus;
import net.minecraft.world.entity.ai.sensing.Sensor;
import net.minecraft.world.entity.ai.sensing.SensorType;
import net.minecraft.world.entity.schedule.Activity;
import net.minecraftforge.event.entity.living.LivingEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.v_black_cat.goetydelight.entities.ICustomerEntity;
import net.v_black_cat.goetydelight.entities.ai.ModActivity;
import net.v_black_cat.goetydelight.entities.ai.ModMemory;
import net.v_black_cat.goetydelight.entities.ai.ModSensor;

import java.util.Set;

import static net.v_black_cat.goetydelight.GoetyDelight.LOGGER;

public class CustomerAi {

    protected static ImmutableList<MemoryModuleType<?>> MEMORY_TYPES;
    protected static ImmutableList<SensorType<? extends Sensor<? super PathfinderMob>>> SENSOR_TYPES;
    static {
        {//传感器
            SENSOR_TYPES = ImmutableList.of(
                    ModSensor.CUSTOMER_RESTAURANT_SENSOR.get(),
                    ModSensor.CUSTOMER_NEAREST_LIVING_ENTITY_SENSOR.get(),
                    ModSensor.CUSTOMER_IN_RESTAURANT_SENSOR.get(),
                    ModSensor.CUSTOMER_NEAREST_LIVING_ENTITY_HAND_DESIRED_ITEM_SENSOR.get(),
                    ModSensor.CUSTOMER_HURT_BY_SENSOR.get()
            );
        }
        {
            MEMORY_TYPES=ImmutableList.of(
                    MemoryModuleType.LOOK_TARGET,                           // 存储实体需要看向的目标
                    MemoryModuleType.DOORS_TO_CLOSE,                        // 存储需要关闭的门
                    MemoryModuleType.NEAREST_LIVING_ENTITIES,              // 存储最近的存活实体列表
                    MemoryModuleType.NEAREST_VISIBLE_LIVING_ENTITIES,      // 存储最近可见的存活实体列表
                    MemoryModuleType.NEAREST_VISIBLE_PLAYER,               // 存储最近可见的玩家
                    MemoryModuleType.NEAREST_VISIBLE_ATTACKABLE_PLAYER,    // 存储最近可见的可攻击玩家
                    MemoryModuleType.NEAREST_VISIBLE_ADULT_PIGLINS,        // 存储最近可见的成年猪灵
                    MemoryModuleType.NEARBY_ADULT_PIGLINS,                 // 存储附近的成年猪灵
                    MemoryModuleType.NEAREST_VISIBLE_WANTED_ITEM,          // 存储最近可见的想要的物品
                    MemoryModuleType.ITEM_PICKUP_COOLDOWN_TICKS,           // 存储物品拾取冷却时间
                    MemoryModuleType.HURT_BY,                              // 存储造成伤害的来源
                    MemoryModuleType.HURT_BY_ENTITY,
                    MemoryModuleType.WALK_TARGET,                  // 存储行走目标位置
                    MemoryModuleType.CANT_REACH_WALK_TARGET_SINCE, // 存储无法到达行走目标的开始时间
                    MemoryModuleType.ATTACK_TARGET,                // 存储攻击目标
                    MemoryModuleType.ATTACK_COOLING_DOWN,          // 存储攻击冷却时间
                    MemoryModuleType.INTERACTION_TARGET,           // 存储交互目标
                    MemoryModuleType.PATH,                         // 存储当前路径信息
                    MemoryModuleType.ANGRY_AT,                     // 存储愤怒目标（特定于通用愤怒）
                    MemoryModuleType.UNIVERSAL_ANGER,              // 存储通用愤怒状态
                    MemoryModuleType.AVOID_TARGET,                 // 存储需要避免的目标
                    MemoryModuleType.ADMIRING_ITEM,                // 存储正在欣赏的物品
                    MemoryModuleType.TIME_TRYING_TO_REACH_ADMIRE_ITEM, // 存储尝试到达欣赏物品的时间
                    MemoryModuleType.ADMIRING_DISABLED,            // 存储物品欣赏功能是否被禁用
                    MemoryModuleType.DISABLE_WALK_TO_ADMIRE_ITEM,  // 存储是否禁用走向欣赏物品
                    MemoryModuleType.CELEBRATE_LOCATION,           // 存储庆祝位置
                    MemoryModuleType.DANCING,                      // 存储跳舞状态
                    MemoryModuleType.HUNTED_RECENTLY,              // 存储最近狩猎状态
                    MemoryModuleType.NEAREST_VISIBLE_BABY_HOGLIN,  // 存储最近可见的幼年疣猪兽
                    MemoryModuleType.NEAREST_VISIBLE_NEMESIS,      // 存储最近可见的宿敌(例如:卫道士对玩家)
                    MemoryModuleType.NEAREST_VISIBLE_ZOMBIFIED,    // 存储最近可见的僵尸化实体
                    MemoryModuleType.RIDE_TARGET,                  // 存储骑乘目标
                    MemoryModuleType.NEAREST_VISIBLE_HUNTABLE_HOGLIN, // 存储最近可见的可狩猎疣猪兽
                    MemoryModuleType.NEAREST_TARGETABLE_PLAYER_NOT_WEARING_GOLD, // 存储最近可见的未穿戴黄金的可攻击玩家
                    MemoryModuleType.NEAREST_PLAYER_HOLDING_WANTED_ITEM, // 存储最近持有想要物品的玩家
                    MemoryModuleType.ATE_RECENTLY,                 // 存储最近进食状态
                    MemoryModuleType.NEAREST_REPELLENT,         // 存储最近的驱避物
                    ModMemory.ENTRANCE_RANGE.get(),
                    ModMemory.DINING_RANGE.get(),
                    ModMemory.PICKUP_RANGE.get(),
                    ModMemory.EXIT_RANGE.get(),
                    ModMemory.ALL_RANGE.get(),
                    ModMemory.NEARBY_RESTAURANT.get(),
                    ModMemory.CUSTOMER_PREFERENCE_LIST.get(),
                    ModMemory.IS_IN_RESTAURANT.get(),
                    ModMemory.IS_IN_ENTRANCE.get(),
                    ModMemory.IS_IN_DINING.get(),
                    ModMemory.IS_IN_PICKUP.get(),
                    ModMemory.IS_IN_EXIT.get(),
                    ModMemory.FOOD_TO_PAY_LIST.get(),
                    ModMemory.RESTAURANT_OWNER_UUID_LIST.get(),
                    ModMemory.NEAREST_ENTITY_HOLDING_THE_DESIRED_ITEM.get(),
                    ModMemory.ITEM_CONSUMPTION_COUNT.get(),
                    ModMemory.IS_HUNGRY_ON_ENTER.get(),
                    ModMemory.IS_FULL_AFTER_DINING_RESTAURANT.get(),
                    ModMemory.PAID_LOOT_COUNT.get()
            );
        }

    }


    public static Brain<PathfinderMob> makeBrain(PathfinderMob mob, Dynamic<?> dynamic){
        Brain.Provider<PathfinderMob> provider = Brain.provider(MEMORY_TYPES, SENSOR_TYPES);

        Brain<PathfinderMob> brain = provider.makeBrain(dynamic);
        addCoreActivities(brain);
        addIdleActivities(brain, mob);

        addCustomerActivities(brain, mob);
        brain.setCoreActivities(ImmutableSet.of(
                Activity.CORE
        ));
        brain.setDefaultActivity(Activity.IDLE);


        brain.useDefaultActivity();

        return brain;
    }

    private static void addCustomerActivities(Brain<PathfinderMob> brain, PathfinderMob mob) {
        Set<Pair<MemoryModuleType<?>, MemoryStatus>> customerConditions = ImmutableSet.of(
                Pair.of(ModMemory.IS_IN_RESTAURANT.get(), MemoryStatus.VALUE_PRESENT)
        );
        brain.addActivityWithConditions(
                ModActivity.CUSTOMER.get(),
                ImmutableList.of(
                        Pair.of(1,  new CustomerFindPickupAreaBehavior()),
                        Pair.of(1, new CustomerPlaceOrderBehavior()),
                        Pair.of(1, new CustomerWaitForFoodBehavior()),
                        Pair.of(1, new CustomerFindDiningAreaBehavior()),
                        Pair.of(1, new CustomerEatFoodBehavior()),
                        Pair.of(1, new CustomerPayBehavior()),
                        Pair.of(1, new CustomerLookAtPlayerWithOrderItemBehavior()),
                        Pair.of(2, new CustomerFindExitBehavior()),
                        Pair.of(1, new CustomerExitBehavior())
                ),
                customerConditions
        );
    }

    public static Brain<PathfinderMob> makeBrain(PathfinderMob mob){
        NbtOps nbtops = NbtOps.INSTANCE;
        Dynamic<Tag> dyn = new Dynamic(nbtops, (Tag)nbtops.createMap(ImmutableMap.of(nbtops.createString("memories"), (Tag)nbtops.emptyMap())));

        Brain<PathfinderMob> brain = makeBrain(mob, dyn);
        return brain;
    }
    public static void enableCustomerMode(PathfinderMob mob){
        if (mob instanceof ICustomerEntity customer){
            customer.goetyDelight$enterCustomerModeAndCheckCoolDown();
        }
    }
    public static boolean isCustomerMode(PathfinderMob mob){
        return mob instanceof ICustomerEntity customer && customer.goetyDelight$isCustomerMode();
    }


    private static void addIdleActivities(Brain<PathfinderMob> brain, PathfinderMob pathfinderMob) {
        brain.addActivity(Activity.IDLE, 10, ImmutableList.of(
                new CustomerFindRestaurantBehavior(),
                new CustomerLookAtPlayerWithOrderItemBehavior(),
                new RunOne<>(ImmutableList.of(
                        Pair.of(new CustomerRandomStroll(1.0F), 2),
                        Pair.of(new DoNothing(10, 20), 1)
                ))
        ));
    }
    private static void addCoreActivities(Brain<PathfinderMob> brain) {
        brain.addActivity(Activity.CORE, 0, ImmutableList.of(
                new Swim(0.8F),
                new CustomerLookAtTargetSink(45, 90),
                new CustomerMoveToTargetSink(100, 200),
                new CustomerExitModeInOutCoreAndFullBehavior(),
                new CustomerExitModeInOutCoreAndHasHurtByBehavior(),
                new CustomerExitModeInRestaurantCoreBehavior()
        ));

    }
    public static void updateActivity(PathfinderMob pathfinderMob) {
        Brain<PathfinderMob> pathfinderMobBrain = ((ICustomerEntity) pathfinderMob).goetyDelight$getCustomerBrain();
        pathfinderMobBrain.setActiveActivityToFirstValid(
                ImmutableList.of(
                        ModActivity.CUSTOMER.get(),
                        Activity.IDLE
                )
        );
/*
        LOGGER.debug("=========================实体信息========================");
        ImmutableList<MemoryModuleType<?>> memoryModuleTypes = ImmutableList.of(
                ModMemory.ENTRANCE_RANGE.get(),
                ModMemory.DINING_RANGE.get(),
                ModMemory.PICKUP_RANGE.get(),
                ModMemory.EXIT_RANGE.get(),
                ModMemory.ALL_RANGE.get(),
                ModMemory.NEARBY_RESTAURANT.get(),
                ModMemory.CUSTOMER_PREFERENCE_LIST.get(),
                ModMemory.IS_IN_RESTAURANT.get(),
                ModMemory.IS_IN_ENTRANCE.get(),
                ModMemory.IS_IN_DINING.get(),
                ModMemory.IS_IN_PICKUP.get(),
                ModMemory.IS_IN_EXIT.get()
                );
//        ImmutableList<MemoryModuleType<?>> memoryModuleTypes = ImmutableList.of(
//                ModMemory.IS_IN_RESTAURANT.get(),
//                ModMemory.IS_IN_EXIT.get(),
//                ModMemory.ALL_RANGE.get(),
//                ModMemory.FOOD_TO_PAY_LIST.get(),
//                MemoryModuleType.WALK_TARGET
//                );
        LOGGER.debug("当前activity: {}", pathfinderMobBrain.getActiveNonCoreActivity());


        for (MemoryModuleType<?> memoryType : memoryModuleTypes) {
            if (pathfinderMobBrain.hasMemoryValue(memoryType)) {
                LOGGER.debug("记忆 {} 存在: {}", memoryType, pathfinderMobBrain.getMemory(memoryType).orElse(null));
            } else {
                LOGGER.debug("记忆 {} 不存在", memoryType);
            }
        }
*/



    }




}
