package net.v_black_cat.goetydelight.entities.ai;

import net.minecraft.world.entity.schedule.Activity;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

import static net.v_black_cat.goetydelight.GoetyDelight.MODID;


public class ModActivity {
    public static final DeferredRegister<Activity> MOD_ACTIVITY =
            DeferredRegister.create(ForgeRegistries.Keys.ACTIVITIES, MODID);

    public static final RegistryObject<Activity> CUSTOMER = getRegister("customer");

    private static RegistryObject<Activity> getRegister(String name) {
        return MOD_ACTIVITY.register(name, () -> new Activity(name));
    }


    public static void register(IEventBus eventBus) {
        MOD_ACTIVITY.register(eventBus);
    }

}
