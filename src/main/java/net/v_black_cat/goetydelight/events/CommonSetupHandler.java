package net.v_black_cat.goetydelight.events;

import net.minecraft.world.level.block.ComposterBlock;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.v_black_cat.goetydelight.GoetyDelight;
import net.v_black_cat.goetydelight.compat.curios.CuriosCompat;
import net.v_black_cat.goetydelight.init.ModBlocks;
import net.v_black_cat.goetydelight.init.ModItems;
import net.v_black_cat.goetydelight.ritual.DelightRitualType;

public class CommonSetupHandler {
    public static void onCommonSetup(FMLCommonSetupEvent event) {
        GoetyDelight.LOGGER.info("HELLO FROM COMMON SETUP");

        DelightRitualType.onCommonSetup(event);

        event.enqueueWork(() -> {
            // 注册堆肥桶配方
            ComposterBlock.COMPOSTABLES.put(ModItems.METAMORPHIC_SCENT_GRASS.get(), 0.2F);
            ComposterBlock.COMPOSTABLES.put(ModItems.METAMORPHIC_SCENT_FRUIT.get(), 0.75F);
            ComposterBlock.COMPOSTABLES.put(ModItems.METAMORPHIC_SCENT_GRASS_SEEDS.get(), 0.05F);
            ComposterBlock.COMPOSTABLES.put(ModItems.ECTOPLASMIC_MELON.get(), 0.50F);
            ComposterBlock.COMPOSTABLES.put(ModItems.ECTOPLASMIC_MELON_SEEDS.get(), 0.09F);
            ComposterBlock.COMPOSTABLES.put(ModBlocks.ECTOPLASMIC_MELON_BLOCK.get().asItem(), 0.95F);
        });

        event.enqueueWork(CuriosCompat::commonSetup);
    }
}