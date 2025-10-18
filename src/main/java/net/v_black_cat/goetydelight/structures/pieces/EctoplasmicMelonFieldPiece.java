package net.v_black_cat.goetydelight.structures.pieces;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.StructureManager;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.levelgen.structure.StructurePiece;
import net.minecraft.world.level.levelgen.structure.pieces.StructurePieceSerializationContext;
import net.minecraft.world.level.levelgen.structure.pieces.StructurePieceType;
import net.minecraft.world.level.levelgen.structure.templatesystem.BlockIgnoreProcessor;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructurePlaceSettings;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplateManager;
import net.v_black_cat.goetydelight.GoetyDelight;
import net.v_black_cat.goetydelight.structures.ModStructurePieceTypes;

import java.util.Objects;

public class EctoplasmicMelonFieldPiece extends StructurePiece {
    private final int variant;
    private final ResourceLocation templateLocation;

    public EctoplasmicMelonFieldPiece(StructureTemplateManager pTemplateManager, BlockPos pPos, int variant) {
        super(ModStructurePieceTypes.ECTOPLASMIC_MELON_FIELD_PIECE.get(), 0,
                createBoundingBox(pPos, 16, 10));
        this.variant = variant;
        this.templateLocation = getTemplateForVariant(variant);
        this.setOrientation(Direction.NORTH);
    }

    public EctoplasmicMelonFieldPiece(StructurePieceSerializationContext context, CompoundTag tag) {
        super(ModStructurePieceTypes.ECTOPLASMIC_MELON_FIELD_PIECE.get(), tag);
        this.variant = tag.getInt("Variant");
        this.templateLocation = getTemplateForVariant(this.variant);
    }

    private static BoundingBox createBoundingBox(BlockPos pPos, int sizeX, int sizeZ) {
        return new BoundingBox(pPos.getX(), pPos.getY(), pPos.getZ(),
                pPos.getX() + sizeX - 1, pPos.getY() + 10, pPos.getZ() + sizeZ - 1);
    }

    @Override
    protected void addAdditionalSaveData(StructurePieceSerializationContext context, CompoundTag tag) {
        tag.putInt("Variant", this.variant);
    }


    @Override
    public void postProcess(WorldGenLevel level, StructureManager structureManager,
                            ChunkGenerator chunkGenerator, RandomSource random,
                            BoundingBox box, ChunkPos chunkPos, BlockPos pos) {

        // 正确获取 StructureTemplateManager
        StructureTemplateManager templateManager = level.getLevel().getServer().getStructureManager();

        // 根据变体选择模板
        ResourceLocation templateLocation = getTemplateForVariant(this.variant);

        // 加载模板
        StructureTemplate template = templateManager.get(templateLocation).orElse(null);

        if (template == null) {
            // 处理模板不存在的情况
            GoetyDelight.LOGGER.error("Template not found: {}", templateLocation);
            return;
        }

        // 创建放置设置
        StructurePlaceSettings placementSettings = new StructurePlaceSettings()
                .setRotation(this.getRotation())
                .setMirror(this.getMirror())
                .setBoundingBox(box)
                .setIgnoreEntities(false)
                .addProcessor(BlockIgnoreProcessor.STRUCTURE_BLOCK);

        // 计算放置位置（使用边界框的最小角）
        BlockPos placementPos = new BlockPos(
                this.boundingBox.minX(),
                this.boundingBox.minY(),
                this.boundingBox.minZ()
        );

        // 放置结构
        template.placeInWorld(level, placementPos, placementPos, placementSettings, random, 2);
    }

    private static ResourceLocation getTemplateForVariant(int variant) {
        return switch (variant) {
            case 0 -> new ResourceLocation(GoetyDelight.MODID, "ectoplasmic_melon_field_variant_a");
            case 1 -> new ResourceLocation(GoetyDelight.MODID, "ectoplasmic_melon_field_variant_b");
            case 2 -> new ResourceLocation(GoetyDelight.MODID, "ectoplasmic_melon_field_variant_c");
            default -> new ResourceLocation(GoetyDelight.MODID, "ectoplasmic_melon_field_variant_a");
        };
    }
}