package net.v_black_cat.goetydelight.events;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.level.block.Blocks;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.v_black_cat.goetydelight.init.ModConfig;
import net.v_black_cat.goetydelight.GoetyDelight;

public class CommonSetupHandler {
    public static void onCommonSetup(FMLCommonSetupEvent event) {
        GoetyDelight.LOGGER.info("HELLO FROM COMMON SETUP");

        if (ModConfig.LOG_DIRT_BLOCK.getAsBoolean()) {
            GoetyDelight.LOGGER.info("DIRT BLOCK >> {}", BuiltInRegistries.BLOCK.getKey(Blocks.DIRT));
        }

        GoetyDelight.LOGGER.info("{}{}", ModConfig.MAGIC_NUMBER_INTRODUCTION.get(), ModConfig.MAGIC_NUMBER.getAsInt());

        ModConfig.ITEM_STRINGS.get().forEach(item -> GoetyDelight.LOGGER.info("ITEM >> {}", item));
    }
}