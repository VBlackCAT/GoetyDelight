package net.v_black_cat.goetydelight.structures;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
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

import java.util.List;

public class ChestLootProcessor extends StructureProcessor {

    /** 默认会被替换战利品表的方块列表（含木桶） */
    private static final List<ResourceLocation> DEFAULT_CHEST_BLOCKS = List.of(
            new ResourceLocation("minecraft", "barrel"),
            new ResourceLocation("minecraft", "chest"),
            new ResourceLocation("minecraft", "trapped_chest"),
            new ResourceLocation("goety", "rotten_chest")
    );

    public static final Codec<ChestLootProcessor> CODEC = RecordCodecBuilder.create(instance ->
            instance.group(
                    ResourceLocation.CODEC.listOf()
                            .fieldOf("chest_blocks")
                            .orElse(DEFAULT_CHEST_BLOCKS)
                            .forGetter(p -> p.chestBlocks),
                    ResourceLocation.CODEC
                            .fieldOf("loot_table")
                            .forGetter(p -> p.lootTable)
            ).apply(instance, ChestLootProcessor::new)
    );

    private final List<ResourceLocation> chestBlocks;
    private final ResourceLocation lootTable;

    /** 无参构造：使用默认方块列表 + 瓜农小屋战利品表（保持旧行为） */
    public ChestLootProcessor() {
        this(DEFAULT_CHEST_BLOCKS,
                new ResourceLocation(GoetyDelight.MODID, "chests/ectoplasmic_melon_field"));
    }

    /** 便捷构造：使用默认方块列表 + 指定战利品表 */
    public ChestLootProcessor(ResourceLocation lootTable) {
        this(DEFAULT_CHEST_BLOCKS, lootTable);
    }

    /** 完整构造：自定义方块列表 + 战利品表 */
    public ChestLootProcessor(List<ResourceLocation> chestBlocks, ResourceLocation lootTable) {
        this.chestBlocks = chestBlocks;
        this.lootTable = lootTable;
    }

    @Override
    public StructureTemplate.StructureBlockInfo processBlock(LevelReader level, BlockPos worldPos, BlockPos pivotPos,
                                                             StructureTemplate.StructureBlockInfo blockInfoLocal,
                                                             StructureTemplate.StructureBlockInfo blockInfoGlobal,
                                                             StructurePlaceSettings settings) {
        Block block = blockInfoGlobal.state().getBlock();
        ResourceLocation blockName = BuiltInRegistries.BLOCK.getKey(block);

        if (blockName != null && chestBlocks.contains(blockName)) {
            CompoundTag nbt = blockInfoGlobal.nbt() != null
                    ? blockInfoGlobal.nbt().copy()
                    : new CompoundTag();

            nbt.putString("LootTable", lootTable.toString());

            long seed = settings.getRandom(worldPos).nextLong();
            nbt.putLong("LootTableSeed", seed);

            return new StructureTemplate.StructureBlockInfo(
                    blockInfoGlobal.pos(), blockInfoGlobal.state(), nbt);
        }

        return blockInfoGlobal;
    }

    @Override
    protected StructureProcessorType<?> getType() {
        return ModStructureProcessorTypes.CHEST_LOOT.get();
    }
}