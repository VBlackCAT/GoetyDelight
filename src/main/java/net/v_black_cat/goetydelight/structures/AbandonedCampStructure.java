package net.v_black_cat.goetydelight.structures;

import com.mojang.serialization.Codec;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraft.world.level.levelgen.structure.StructureType;
import net.minecraft.world.level.levelgen.structure.pieces.StructurePiecesBuilder;
import net.v_black_cat.goetydelight.structures.pieces.AbandonedCampPiece;
import java.util.Optional;

public class AbandonedCampStructure extends Structure {
    public static final Codec<AbandonedCampStructure> CODEC =
            simpleCodec(AbandonedCampStructure::new);

    public AbandonedCampStructure(StructureSettings settings) {
        super(settings);
    }

    @Override
    public Optional<GenerationStub> findGenerationPoint(GenerationContext context) {
        // 下界生成：不使用 onTopOfChunkCenter (那是主世界逻辑)
        // 简单起见，固定在下界顶部基岩层下方一定高度，或使用固定 Y
        int y = 32 + context.random().nextInt(32); // 示例：Y 32~64 之间

        BlockPos pos = new BlockPos(
                context.chunkPos().getMinBlockX(),
                y,
                context.chunkPos().getMinBlockZ()
        );

        return Optional.of(new GenerationStub(pos, (builder) -> {
            builder.addPiece(new AbandonedCampPiece(
                    context.structureTemplateManager(), pos, 0)); // variant 0
        }));
    }

    @Override
    public StructureType<?> type() {
        return ModStructures.ABANDONED_CAMP.get();
    }
}