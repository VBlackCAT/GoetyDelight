package net.v_black_cat.goetydelight.init;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureProcessor;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureProcessorType;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.v_black_cat.goetydelight.GoetyDelight;
import net.v_black_cat.goetydelight.structures.ChestLootProcessor;
import net.v_black_cat.goetydelight.structures.SmartBottomProtectionProcessor;

import java.util.function.Supplier;

public class ModStructureProcessorTypes {
    public static final DeferredRegister<StructureProcessorType<?>> STRUCTURE_PROCESSOR_TYPES =
            DeferredRegister.create(Registries.STRUCTURE_PROCESSOR, GoetyDelight.MODID);

    public static final Supplier<StructureProcessorType<SmartBottomProtectionProcessor>> SMART_BOTTOM_PROTECTION =
            STRUCTURE_PROCESSOR_TYPES.register("smart_bottom_protection",
                    () -> () -> SmartBottomProtectionProcessor.CODEC);

    public static final Supplier<StructureProcessorType<ChestLootProcessor>> CHEST_LOOT =
            STRUCTURE_PROCESSOR_TYPES.register("chest_loot",
                    () -> () -> ChestLootProcessor.CODEC);

    public static void register(IEventBus eventBus) {
        STRUCTURE_PROCESSOR_TYPES.register(eventBus);
    }
}