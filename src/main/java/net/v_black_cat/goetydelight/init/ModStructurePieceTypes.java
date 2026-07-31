package net.v_black_cat.goetydelight.init;

import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.levelgen.structure.pieces.StructurePieceType;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.v_black_cat.goetydelight.GoetyDelight;
import net.v_black_cat.goetydelight.structures.EctoplasmicMelonFieldPiece;

import java.util.function.Supplier;

public class ModStructurePieceTypes {
    public static final DeferredRegister<StructurePieceType> STRUCTURE_PIECE_TYPES =
            DeferredRegister.create(Registries.STRUCTURE_PIECE, GoetyDelight.MODID);

    public static final Supplier<StructurePieceType> ECTOPLASMIC_MELON_FIELD_PIECE =
            STRUCTURE_PIECE_TYPES.register("ectoplasmic_melon_field_piece",
                    () -> EctoplasmicMelonFieldPiece::new);

    public static void register(IEventBus eventBus) {
        STRUCTURE_PIECE_TYPES.register(eventBus);
    }
}