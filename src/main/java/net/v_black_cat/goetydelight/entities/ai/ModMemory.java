package net.v_black_cat.goetydelight.entities.ai;

import com.mojang.serialization.Codec;
import net.minecraft.core.GlobalPos;
import net.minecraft.core.UUIDUtil;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.AABB;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import static net.v_black_cat.goetydelight.GoetyDelight.MODID;

public class ModMemory {
    public static final DeferredRegister<MemoryModuleType<?>> MEMORY_MODULES =
            DeferredRegister.create(ForgeRegistries.Keys.MEMORY_MODULE_TYPES, MODID);


    private static final Codec<AABB> AABB_CODEC = Codec.DOUBLE.listOf().xmap(
            l -> new AABB(l.get(0), l.get(1), l.get(2), l.get(3), l.get(4), l.get(5)),
            a -> List.of(a.minX, a.minY, a.minZ, a.maxX, a.maxY, a.maxZ)
    );

    public static final RegistryObject<MemoryModuleType<GlobalPos>> NEARBY_RESTAURANT =
            MEMORY_MODULES.register("nearby_restaurant", () -> new MemoryModuleType<>(Optional.of(GlobalPos.CODEC)));

    public static final RegistryObject<MemoryModuleType<AABB>> ENTRANCE_RANGE =
            MEMORY_MODULES.register("entrance_range", () -> new MemoryModuleType<>(Optional.of(AABB_CODEC)));

    public static final RegistryObject<MemoryModuleType<AABB>> ALL_RANGE =
            MEMORY_MODULES.register("all_range", () -> new MemoryModuleType<>(Optional.of(AABB_CODEC)));

    public static final RegistryObject<MemoryModuleType<AABB>> PICKUP_RANGE =
            MEMORY_MODULES.register("pickup_range", () -> new MemoryModuleType<>(Optional.of(AABB_CODEC)));

    public static final RegistryObject<MemoryModuleType<AABB>> DINING_RANGE =
            MEMORY_MODULES.register("dining_range", () -> new MemoryModuleType<>(Optional.of(AABB_CODEC)));

    public static final RegistryObject<MemoryModuleType<AABB>> EXIT_RANGE =
            MEMORY_MODULES.register("exit_range", () -> new MemoryModuleType<>(Optional.of(AABB_CODEC)));

    public static final RegistryObject<MemoryModuleType<List<ItemStack>>> CUSTOMER_PREFERENCE_LIST =
            MEMORY_MODULES.register("customer_preference_list",
            () -> new MemoryModuleType<>(Optional.of(ItemStack.CODEC.listOf())));

    public static final RegistryObject<MemoryModuleType<Boolean>> IS_IN_RESTAURANT =
            MEMORY_MODULES.register("is_in_restaurant",
            () -> new MemoryModuleType<>(Optional.of(Codec.BOOL)));

    public static final RegistryObject<MemoryModuleType<Boolean>> IS_IN_ENTRANCE =
            MEMORY_MODULES.register("is_in_entrance",
                    () -> new MemoryModuleType<>(Optional.of(Codec.BOOL)));

    public static final RegistryObject<MemoryModuleType<Boolean>> IS_IN_DINING =
            MEMORY_MODULES.register("is_in_dining",
                    () -> new MemoryModuleType<>(Optional.of(Codec.BOOL)));

    public static final RegistryObject<MemoryModuleType<Boolean>> IS_IN_PICKUP =
            MEMORY_MODULES.register("is_in_pickup",
                    () -> new MemoryModuleType<>(Optional.of(Codec.BOOL)));

    public static final RegistryObject<MemoryModuleType<Boolean>> IS_IN_EXIT =
            MEMORY_MODULES.register("is_in_exit",
                    () -> new MemoryModuleType<>(Optional.of(Codec.BOOL)));

    public static final RegistryObject<MemoryModuleType<List<ItemStack>>> FOOD_TO_PAY_LIST =
            MEMORY_MODULES.register("food_to_pay_list",
            () -> new MemoryModuleType<>(Optional.of(ItemStack.CODEC.listOf())));

    public static final RegistryObject<MemoryModuleType<List<UUID>>> RESTAURANT_OWNER_UUID_LIST =
            MEMORY_MODULES.register("restaurant_owner_uuid_list",
            () -> new MemoryModuleType<>(Optional.of(UUIDUtil.CODEC.listOf())));

    public static final RegistryObject<MemoryModuleType<LivingEntity>> NEAREST_ENTITY_HOLDING_THE_DESIRED_ITEM =
            MEMORY_MODULES.register("nearest_entity_holding_the_desired_item",
                    () -> new MemoryModuleType<>(Optional.empty()));

    public static final RegistryObject<MemoryModuleType<GlobalPos>> CURRENT_RESTAURANT_BLOCK_POSITION =
            MEMORY_MODULES.register("current_restaurant_block_position",
                    () -> new MemoryModuleType<>(Optional.of(GlobalPos.CODEC)));

    public static final RegistryObject<MemoryModuleType<Map<ItemStack, Integer>>> ITEM_CONSUMPTION_COUNT =
            MEMORY_MODULES.register("item_consumption_count",
                    () -> new MemoryModuleType<>(Optional.of(Codec.unboundedMap(ItemStack.CODEC, Codec.INT))));

    public static final RegistryObject<MemoryModuleType<Boolean>> IS_HUNGRY_ON_ENTER =
            MEMORY_MODULES.register("is_hungry_on_enter",
                    () -> new MemoryModuleType<>(Optional.of(Codec.BOOL)));
    public static final RegistryObject<MemoryModuleType<Boolean>> IS_FULL_AFTER_DINING_RESTAURANT =
            MEMORY_MODULES.register("is_full_after_dining_restaurant",
                    () -> new MemoryModuleType<>(Optional.of(Codec.BOOL)));

    public static final RegistryObject<MemoryModuleType<Integer>> PAID_LOOT_COUNT =
            MEMORY_MODULES.register("paid_loot_count",
                    () -> new MemoryModuleType<>(Optional.of(Codec.INT)));



    public static void register(IEventBus eventBus) {
        MEMORY_MODULES.register(eventBus);
    }
}