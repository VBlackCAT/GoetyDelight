package net.v_black_cat.goetydelight.structures;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureProcessor;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureProcessorType;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructurePlaceSettings;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;
import net.v_black_cat.goetydelight.init.ModStructureProcessorTypes;

import javax.annotation.Nullable;

public class SmartBottomProtectionProcessor extends StructureProcessor {
    public static final MapCodec<SmartBottomProtectionProcessor> CODEC = Codec.INT
            .fieldOf("layersToProtect")
            .xmap(SmartBottomProtectionProcessor::new, processor -> processor.layersToProtect);

    private final int layersToProtect;

    public SmartBottomProtectionProcessor(int layersToProtect) {
        this.layersToProtect = layersToProtect;
    }

    @Nullable
    @Override
    public StructureTemplate.StructureBlockInfo process(
            LevelReader level,
            BlockPos offset,
            BlockPos pos,
            StructureTemplate.StructureBlockInfo blockInfo,
            StructureTemplate.StructureBlockInfo relativeBlockInfo,
            StructurePlaceSettings settings,
            @Nullable StructureTemplate template) {

        if (relativeBlockInfo.state().is(Blocks.AIR) && blockInfo.pos().getY() < layersToProtect) {
            return null;
        }
        return relativeBlockInfo;
    }

    @Override
    protected StructureProcessorType<?> getType() {
        return ModStructureProcessorTypes.SMART_BOTTOM_PROTECTION.get();
    }
}