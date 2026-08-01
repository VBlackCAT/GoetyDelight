package net.v_black_cat.goetydelight.structures;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureProcessor;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureProcessorType;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructurePlaceSettings;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;
import net.v_black_cat.goetydelight.GoetyDelight;
import net.v_black_cat.goetydelight.init.ModStructureProcessorTypes;

public class ChestLootProcessor extends StructureProcessor {
    public static final MapCodec<ChestLootProcessor> CODEC = MapCodec.unit(ChestLootProcessor::new);

    public ChestLootProcessor() {
    }

    @Override
    public StructureTemplate.StructureBlockInfo processBlock(LevelReader level, BlockPos worldPos, BlockPos pivotPos,
                                                             StructureTemplate.StructureBlockInfo blockInfoLocal,
                                                             StructureTemplate.StructureBlockInfo blockInfoGlobal,
                                                             StructurePlaceSettings settings) {
        // 获取方块实例
        Block block = blockInfoGlobal.state().getBlock();

        ResourceLocation blockName = BuiltInRegistries.BLOCK.getKey(block);

        // 检查是否是goety:rotten_chest箱子
        if (blockName != null && blockName.toString().equals("goety:rotten_chest")) {

            // 创建或获取NBT标签
            CompoundTag nbt = blockInfoGlobal.nbt() != null ? blockInfoGlobal.nbt().copy() : new CompoundTag();

            // 设置战利品表
            ResourceLocation lootTable = ResourceLocation.fromNamespaceAndPath(GoetyDelight.MODID, "chests/ectoplasmic_melon_field");
            nbt.putString("LootTable", lootTable.toString());

            // 使用worldPos的坐标生成确定性种子
            long seed = ((long)worldPos.getX() * 31L + (long)worldPos.getY()) * 31L + (long)worldPos.getZ();
            nbt.putLong("LootTableSeed", seed);

            // 返回带有战利品表信息的方块信息
            return new StructureTemplate.StructureBlockInfo(blockInfoGlobal.pos(), blockInfoGlobal.state(), nbt);
        }

        return blockInfoGlobal;
    }

    @Override
    protected StructureProcessorType<?> getType() {
        return ModStructureProcessorTypes.CHEST_LOOT.get();
    }
}