package net.v_black_cat.goetydelight.init;

import net.minecraft.world.item.alchemy.Potion;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.v_black_cat.goetydelight.GoetyDelight;

public class ModPotions {
    public static final DeferredRegister<Potion> POTIONS =
            DeferredRegister.create(net.minecraft.core.registries.Registries.POTION, GoetyDelight.MODID);

    // 示例药水
    // public static final DeferredHolder<Potion, Potion> EXAMPLE_POTION =
    //         POTIONS.register("example_potion", () -> new Potion(new MobEffectInstance(ModEffects.EXAMPLE_EFFECT.get(), 3600)));

    public static void register(IEventBus modEventBus) {
        POTIONS.register(modEventBus);
    }
}