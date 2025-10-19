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


    private int getAverageHeight(GenerationContext context) {
        int totalHeight = 0;
        int sampleCount = 0;
        
        
        for (int x = 0; x < 16; x++) {
            for (int z = 0; z < 16; z++) {
                int worldX = context.chunkPos().getMinBlockX() + x;
                int worldZ = context.chunkPos().getMinBlockZ() + z;
                
                int height = context.chunkGenerator().getFirstFreeHeight(
                        worldX,
                        worldZ,
                        Heightmap.Types.WORLD_SURFACE_WG,
                        context.heightAccessor(),
                        context.randomState()
                );
                
                totalHeight += height;
                sampleCount++;
            }
        }
        
        return sampleCount > 0 ? totalHeight / sampleCount : 0; 
    }

    private void generatePieces(StructurePiecesBuilder builder, GenerationContext context) {
        
        int y = getAverageHeight(context);
        BlockPos blockpos = new BlockPos(
                context.chunkPos().getMinBlockX(),
                y,
                context.chunkPos().getMinBlockZ()
        );

        
        int variant = context.random().nextInt(3);
        builder.addPiece(new EctoplasmicMelonFieldPiece(context.structureTemplateManager(),
                blockpos, variant));
    }

    @Override
    public StructureType<?> type() {
        return ModStructures.ECTOPLASMIC_MELON_FIELD.get();
    }
}