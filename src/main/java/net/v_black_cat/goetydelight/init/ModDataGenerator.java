package net.v_black_cat.goetydelight.init;

import net.minecraft.core.HolderLookup;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.PackOutput;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.data.event.GatherDataEvent;
import net.v_black_cat.goetydelight.GoetyDelight;
import net.v_black_cat.goetydelight.block.DollBlock;
import net.v_black_cat.goetydelight.datagen.ModRecipeProvider;
import net.v_black_cat.goetydelight.events.DollRegisterEventHandler;

import java.util.concurrent.CompletableFuture;

@EventBusSubscriber(modid = GoetyDelight.MODID)
public class ModDataGenerator {

    @SubscribeEvent
    public static void gatherData(GatherDataEvent event) {
        DataGenerator generator = event.getGenerator();
        PackOutput packOutput = generator.getPackOutput();
        CompletableFuture<HolderLookup.Provider> lookupProvider = event.getLookupProvider();

        populateDollBlocks();

        generator.addProvider(
                event.includeServer(),
                new ModRecipeProvider(packOutput, lookupProvider)
        );
    }

    private static void populateDollBlocks() {
        if (!DollRegisterEventHandler.DOLL_BLOCKS.isEmpty()) return;

        for (String dollName : DollRegisterEventHandler.SPECIAL_DOLL_NAMES) {
            ResourceLocation name = ResourceLocation.fromNamespaceAndPath(GoetyDelight.MODID, dollName);
            DollRegisterEventHandler.DOLL_BLOCKS.put(name, new DollBlock());
        }
        System.out.println("Populated " + DollRegisterEventHandler.DOLL_BLOCKS.size() + " doll blocks for DataGen");
    }
}
