package net.v_black_cat.goetydelight.init;

import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.levelgen.structure.StructureType;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.v_black_cat.goetydelight.GoetyDelight;
import net.v_black_cat.goetydelight.structures.EctoplasmicMelonFieldStructure;

import java.util.function.Supplier;

public class ModStructures {

    public static final DeferredRegister<StructureType<?>> STRUCTURES =
            DeferredRegister.create(Registries.STRUCTURE_TYPE, GoetyDelight.MODID);

    public static final Supplier<StructureType<EctoplasmicMelonFieldStructure>> ECTOPLASMIC_MELON_FIELD =
            STRUCTURES.register("ectoplasmic_melon_field",
                    () -> () -> EctoplasmicMelonFieldStructure.CODEC);

    public static void register(IEventBus eventBus) {
        STRUCTURES.register(eventBus);
    }
}