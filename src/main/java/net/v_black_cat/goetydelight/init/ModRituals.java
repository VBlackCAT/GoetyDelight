package net.v_black_cat.goetydelight.init;

import com.Polarice3.Goety.Goety;
import com.Polarice3.Goety.common.ritual.ModRitualFactory;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.v_black_cat.goetydelight.GoetyDelight;
import net.v_black_cat.goetydelight.ritual.DelightRitual;
import net.v_black_cat.goetydelight.ritual.DelightRitualType;

public class ModRituals {
    public static final ResourceKey<Registry<ModRitualFactory>> RITUAL_FACTORY_KEY =
            ResourceKey.createRegistryKey(Goety.location("ritual_factory"));

    public static final DeferredRegister<ModRitualFactory> RITUALS =
            DeferredRegister.create(RITUAL_FACTORY_KEY, GoetyDelight.MODID);

    public static final DeferredHolder<ModRitualFactory, ModRitualFactory> CULINARY =
            RITUALS.register(DelightRitualType.CULINARY, () -> new ModRitualFactory(DelightRitual::new));

    public static void register(IEventBus modEventBus) {
        RITUALS.register(modEventBus);
    }
}