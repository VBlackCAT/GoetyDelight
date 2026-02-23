package net.v_black_cat.goetydelight.entities.ai.customer;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Dynamic;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.Tag;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.Brain;
import net.minecraft.world.entity.ai.behavior.*;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.sensing.Sensor;
import net.minecraft.world.entity.ai.sensing.SensorType;
import net.minecraft.world.entity.schedule.Activity;

public class CustomerAi {

    protected static ImmutableList<MemoryModuleType<?>> MEMORY_TYPES;
    protected static ImmutableList<SensorType<? extends Sensor<? super PathfinderMob>>> SENSOR_TYPES;
    static {
        {//传感器
            SENSOR_TYPES = ImmutableList.of(

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
                    MemoryModuleType.NEAREST_REPELLENT         // 存储最近的驱避物
            );
        }

    }


    static int idleStartPriority = 100;

    public static Brain<PathfinderMob> makeBrain(PathfinderMob mob, Dynamic<?> dynamic){
        Brain.Provider<PathfinderMob> provider = Brain.provider(MEMORY_TYPES, SENSOR_TYPES);

        Brain<PathfinderMob> brain = provider.makeBrain(dynamic);
        addCoreActivities(brain);
        addIdleActivities(brain, mob);
        brain.setCoreActivities(ImmutableSet.of(
                Activity.CORE
        ));
        brain.setDefaultActivity(Activity.IDLE);


        brain.useDefaultActivity();

        return brain;
    }
    public static Brain<PathfinderMob> makeBrain(PathfinderMob mob){
        NbtOps nbtops = NbtOps.INSTANCE;
        Dynamic<Tag> dyn = new Dynamic(nbtops, (Tag)nbtops.createMap(ImmutableMap.of(nbtops.createString("memories"), (Tag)nbtops.emptyMap())));

        Brain<PathfinderMob> brain = makeBrain(mob, dyn);
        return brain;
    }
    public static void enableCustomerMode(PathfinderMob mob, boolean enabled){
        mob.getPersistentData().putBoolean("GoetyDelightCustomerMode", enabled);
    }
    public static boolean isCustomerMode(PathfinderMob mob){
        return mob.getPersistentData().getBoolean("GoetyDelightCustomerMode");
    }


    private static void addIdleActivities(Brain<PathfinderMob> brain, PathfinderMob pathfinderMob) {
        brain.addActivity(Activity.IDLE, 10, ImmutableList.of(
                new CustomerRandomStroll(1.0F)
        ));
    }
    private static void addCoreActivities(Brain<PathfinderMob> brain) {
        brain.addActivity(Activity.CORE, 0, ImmutableList.of(
                new Swim(0.8F),
                new AnimalPanic(2.0F),
                new LookAtTargetSink(45, 90),
                new CustomerMoveToTargetSink(100, 200)
        ));

    }
    public static void updateActivity(PathfinderMob pathfinderMob) {
        ((ICustomerEntity)pathfinderMob).goetyDelight$getCustomerBrain().setActiveActivityToFirstValid(
                ImmutableList.of(
                        Activity.IDLE
                )
        );
    }

}
