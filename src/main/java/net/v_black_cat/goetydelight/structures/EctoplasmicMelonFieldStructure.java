package net.v_black_cat.goetydelight.structures;

import com.mojang.serialization.Codec;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraft.world.level.levelgen.structure.StructureType;
import net.minecraft.world.level.levelgen.structure.pieces.StructurePiecesBuilder;
import net.v_black_cat.goetydelight.structures.pieces.EctoplasmicMelonFieldPiece;
import java.util.Optional;

public class EctoplasmicMelonFieldStructure extends Structure {
    public static final Codec<EctoplasmicMelonFieldStructure> CODEC =
            simpleCodec(EctoplasmicMelonFieldStructure::new);

    public EctoplasmicMelonFieldStructure(StructureSettings settings) {
        super(settings);
    }

    @Override
    public Optional<GenerationStub> findGenerationPoint(GenerationContext context) {
        return onTopOfChunkCenter(context, Heightmap.Types.WORLD_SURFACE_WG,
                (builder) -> generatePieces(builder, context));
    }

    private void generatePieces(StructurePiecesBuilder builder, GenerationContext context) {
        BlockPos blockpos = new BlockPos(context.chunkPos().getMinBlockX(),
                0,
                context.chunkPos().getMinBlockZ());

        // 随机选择变体 (0, 1, 2)
        int variant = context.random().nextInt(3);
        builder.addPiece(new EctoplasmicMelonFieldPiece(context.structureTemplateManager(),
                blockpos, variant));
    }

    @Override
    public StructureType<?> type() {
        return ModStructures.ECTOPLASMIC_MELON_FIELD.get();
    }
}
