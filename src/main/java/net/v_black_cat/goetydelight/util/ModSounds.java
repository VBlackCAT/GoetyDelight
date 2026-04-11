package net.v_black_cat.goetydelight.util;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import net.v_black_cat.goetydelight.GoetyDelight;

public class ModSounds {
    public static final DeferredRegister<SoundEvent> SOUND_EVENTS = DeferredRegister.create(ForgeRegistries.SOUND_EVENTS, GoetyDelight.MODID);

    public static RegistryObject<SoundEvent> TOUCH_DOLL = SOUND_EVENTS.register("block.touch_doll",
            () -> SoundEvent.createFixedRangeEvent(new ResourceLocation(GoetyDelight.MODID, "block.touch_doll"), 16));
}
