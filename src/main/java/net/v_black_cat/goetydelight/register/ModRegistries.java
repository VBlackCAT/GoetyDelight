package net.v_black_cat.goetydelight.register;

import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.DataPackRegistryEvent;
import net.v_black_cat.goetydelight.entities.ai.customer.preference.EntityPreference;

import static net.v_black_cat.goetydelight.GoetyDelight.MODID;

public class ModRegistries {
    public static final ResourceKey<Registry<EntityPreference>> PREFERENCE_KEY =
            ResourceKey.createRegistryKey(new ResourceLocation(MODID, "entity_preferences"));

    @Mod.EventBusSubscriber(bus = Mod.EventBusSubscriber.Bus.MOD)
    public static class ModRegistriesEvents {
        @SubscribeEvent
        public static void onNewRegistry(DataPackRegistryEvent.NewRegistry event) {
            System.out.println("GoetyDelight: 正在向系统申请创建动态注册表...");
            event.dataPackRegistry(ModRegistries.PREFERENCE_KEY, EntityPreference.CODEC, EntityPreference.CODEC);
        }
    }
}
