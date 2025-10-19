package net.v_black_cat.goetydelight.structures;

import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureProcessorType;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.RegistryObject;
import net.v_black_cat.goetydelight.GoetyDelight;

public class ModStructureProcessorTypes {
    public static final DeferredRegister<StructureProcessorType<?>> STRUCTURE_PROCESSOR_TYPES =
            DeferredRegister.create(Registries.STRUCTURE_PROCESSOR, GoetyDelight.MODID);

    public static final RegistryObject<StructureProcessorType<SmartBottomProtectionProcessor>> SMART_BOTTOM_PROTECTION =
            STRUCTURE_PROCESSOR_TYPES.register("smart_bottom_protection",
                    () -> () -> SmartBottomProtectionProcessor.CODEC);

    public static final RegistryObject<StructureProcessorType<ChestLootProcessor>> CHEST_LOOT =
            STRUCTURE_PROCESSOR_TYPES.register("chest_loot",
                    () -> () -> ChestLootProcessor.CODEC);

    public static void register(IEventBus eventBus) {
        STRUCTURE_PROCESSOR_TYPES.register(eventBus);
    }
}