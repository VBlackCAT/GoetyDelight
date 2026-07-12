package net.v_black_cat.goetydelight.init;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.v_black_cat.goetydelight.GoetyDelight;

public class ModSounds {
    public static final DeferredRegister<SoundEvent> SOUNDS =
            DeferredRegister.create(net.minecraft.core.registries.Registries.SOUND_EVENT, GoetyDelight.MODID);

    // 示例音效
    public static final DeferredHolder<SoundEvent, SoundEvent> EXAMPLE_SOUND =
            SOUNDS.register("example_sound", () -> SoundEvent.createVariableRangeEvent(
                    ResourceLocation.fromNamespaceAndPath(GoetyDelight.MODID, "example_sound")));

    public static void register(IEventBus modEventBus) {
        SOUNDS.register(modEventBus);
    }
}