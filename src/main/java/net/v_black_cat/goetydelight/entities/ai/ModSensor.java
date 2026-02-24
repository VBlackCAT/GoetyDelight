package net.v_black_cat.goetydelight.entities.ai;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.sensing.SensorType;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import net.v_black_cat.goetydelight.entities.ai.customer.sensor.CustomerInRestaurantSensor;
import net.v_black_cat.goetydelight.entities.ai.customer.sensor.CustomerNearestLivingEntitySensor;
import net.v_black_cat.goetydelight.entities.ai.customer.sensor.CustomerRestaurantSensor;

import static net.v_black_cat.goetydelight.GoetyDelight.MODID;

public class ModSensor {
    public static final DeferredRegister<SensorType<?>> MOD_SENSORS =
            DeferredRegister.create(ForgeRegistries.Keys.SENSOR_TYPES, MODID);

    public static final RegistryObject<SensorType<CustomerRestaurantSensor>> CUSTOMER_RESTAURANT_SENSOR =
            MOD_SENSORS.register("customer_restaurant_sensor", () -> new SensorType<>(CustomerRestaurantSensor::new));
    public static final RegistryObject<SensorType<CustomerNearestLivingEntitySensor<LivingEntity>>> CUSTOMER_NEAREST_LIVING_ENTITY_SENSOR =
            MOD_SENSORS.register("customer_nearest_living_entity_sensor", () -> new SensorType<>(CustomerNearestLivingEntitySensor::new));
    public static final RegistryObject<SensorType<CustomerInRestaurantSensor>> CUSTOMER_IN_RESTAURANT_SENSOR =
            MOD_SENSORS.register("customer_in_restaurant_sensor", () -> new SensorType<>(CustomerInRestaurantSensor::new));

    public static void register(IEventBus eventBus) {
        MOD_SENSORS.register(eventBus);
    }
}