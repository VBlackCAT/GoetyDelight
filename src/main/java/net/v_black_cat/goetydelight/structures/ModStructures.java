package net.v_black_cat.goetydelight.structures;

import com.mojang.serialization.Codec;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraft.world.level.levelgen.structure.StructureType;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.RegistryObject;
import net.v_black_cat.goetydelight.GoetyDelight;

public class ModStructures {
    
    public static final DeferredRegister<StructureType<?>> STRUCTURES =
            DeferredRegister.create(Registries.STRUCTURE_TYPE, GoetyDelight.MODID);

    
    public static final RegistryObject<StructureType<EctoplasmicMelonFieldStructure>> ECTOPLASMIC_MELON_FIELD =
            STRUCTURES.register("ectoplasmic_melon_field",
                    () -> explicitStructureTypeTyping(EctoplasmicMelonFieldStructure.CODEC));

    
    private static <T extends Structure> StructureType<T> explicitStructureTypeTyping(Codec<T> structureCodec) {
        return () -> structureCodec;
    }

    public static void register(IEventBus eventBus) {
        STRUCTURES.register(eventBus);
    }
}
