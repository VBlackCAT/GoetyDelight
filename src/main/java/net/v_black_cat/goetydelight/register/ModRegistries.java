package net.v_black_cat.goetydelight.register;

import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.DataPackRegistryEvent;
import net.v_black_cat.goetydelight.visual.EntityVisualEffectType;

import static net.v_black_cat.goetydelight.GoetyDelight.MODID;

public class ModRegistries {


    public static final ResourceKey<Registry<EntityVisualEffectType>> ENTITY_VISUAL_EFFECT_TYPE_KEY =
            ResourceKey.createRegistryKey(new ResourceLocation(MODID, "entity_visual_effect_types"));

    @Mod.EventBusSubscriber(bus = Mod.EventBusSubscriber.Bus.MOD, modid = MODID)
    public static class ModRegistriesEvents {
        @SubscribeEvent
        public static void onNewRegistry(DataPackRegistryEvent.NewRegistry event) {
        }
    }
}
