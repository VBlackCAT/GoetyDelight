package net.v_black_cat.goetydelight.structures;

import com.mojang.serialization.Codec;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureProcessor;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureProcessorType;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructurePlaceSettings;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;

public class SmartBottomProtectionProcessor extends StructureProcessor {
    public static final Codec<SmartBottomProtectionProcessor> CODEC = Codec.INT
            .fieldOf("layersToProtect")
            .xmap(SmartBottomProtectionProcessor::new, processor -> processor.layersToProtect)
            .codec();

    private final int layersToProtect;

    public SmartBottomProtectionProcessor(int layersToProtect) {
        this.layersToProtect = layersToProtect;
    }

    @Override
    public StructureTemplate.StructureBlockInfo processBlock(LevelReader level, BlockPos worldPos, BlockPos pivotPos,
                                                             StructureTemplate.StructureBlockInfo blockInfoLocal,
                                                             StructureTemplate.StructureBlockInfo blockInfoGlobal,
                                                             StructurePlaceSettings settings) {
        if (blockInfoGlobal.state().is(Blocks.AIR) && blockInfoLocal.pos().getY() < layersToProtect) {
            return null;
        }
        return blockInfoGlobal;
    }

    @Override
    protected StructureProcessorType<?> getType() {
        return ModStructureProcessorTypes.SMART_BOTTOM_PROTECTION.get();
    }
}