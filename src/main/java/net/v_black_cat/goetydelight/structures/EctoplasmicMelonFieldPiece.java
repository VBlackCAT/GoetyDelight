package net.v_black_cat.goetydelight.structures;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.StructureManager;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.levelgen.structure.StructurePiece;
import net.minecraft.world.level.levelgen.structure.pieces.StructurePieceSerializationContext;
import net.minecraft.world.level.levelgen.structure.pieces.StructurePieceType;
import net.minecraft.world.level.levelgen.structure.templatesystem.*;
import net.v_black_cat.goetydelight.GoetyDelight;
import net.v_black_cat.goetydelight.init.ModStructurePieceTypes;
import net.v_black_cat.goetydelight.structures.ChestLootProcessor;
import net.v_black_cat.goetydelight.structures.SmartBottomProtectionProcessor;

import java.util.Objects;
import java.util.Optional;

public class EctoplasmicMelonFieldPiece extends StructurePiece {
    private final int variant;
    private final ResourceLocation templateLocation;

    public EctoplasmicMelonFieldPiece(StructureTemplateManager pTemplateManager, BlockPos pPos, int variant) {
        super(ModStructurePieceTypes.ECTOPLASMIC_MELON_FIELD_PIECE.get(), 0,
                createBoundingBox(pTemplateManager, pPos, variant));
        this.variant = variant;
        this.templateLocation = getTemplateForVariant(variant);
        this.setOrientation(Direction.NORTH);
    }

    public EctoplasmicMelonFieldPiece(StructurePieceSerializationContext context, CompoundTag tag) {
        super(ModStructurePieceTypes.ECTOPLASMIC_MELON_FIELD_PIECE.get(), tag);
        this.variant = tag.getInt("Variant");
        this.templateLocation = getTemplateForVariant(this.variant);
    }

    private static BoundingBox createBoundingBox(StructureTemplateManager templateManager,
                                                 BlockPos pPos, int variant) {
        ResourceLocation templateLocation = getTemplateForVariant(variant);
        Optional<StructureTemplate> template = templateManager.get(templateLocation);

        if (template.isPresent()) {
            StructureTemplate structureTemplate = template.get();
            int sizeX = structureTemplate.getSize().getX();
            int sizeZ = structureTemplate.getSize().getZ();
            return new BoundingBox(pPos.getX(), pPos.getY(), pPos.getZ(),
                    pPos.getX() + sizeX - 1, pPos.getY() + structureTemplate.getSize().getY(), pPos.getZ() + sizeZ - 1);
        }

        return new BoundingBox(pPos.getX(), pPos.getY(), pPos.getZ(),
                pPos.getX() + 16 - 1, pPos.getY() + 10, pPos.getZ() + 10 - 1);
    }

    @Override
    protected void addAdditionalSaveData(StructurePieceSerializationContext context, CompoundTag tag) {
        tag.putInt("Variant", this.variant);
    }

    @Override
    public void postProcess(WorldGenLevel level, StructureManager structureManager,
                            ChunkGenerator chunkGenerator, RandomSource random,
                            BoundingBox box, ChunkPos chunkPos, BlockPos pos) {

        // 1.21中获取StructureTemplateManager的方式改变了
        StructureTemplateManager templateManager = level.getLevel().getStructureManager();
        ResourceLocation templateLocation = getTemplateForVariant(this.variant);
        Optional<StructureTemplate> templateOptional = templateManager.get(templateLocation);

        if (templateOptional.isEmpty()) {
            GoetyDelight.LOGGER.error("Template not found: {}", templateLocation);
            return;
        }

        StructureTemplate template = templateOptional.get();

        StructurePlaceSettings placementSettings = new StructurePlaceSettings()
                .setRotation(this.getRotation())
                .setMirror(this.getMirror())
                .setBoundingBox(box)
                .setIgnoreEntities(false)
                .addProcessor(BlockIgnoreProcessor.STRUCTURE_BLOCK)
                .addProcessor(new SmartBottomProtectionProcessor(2))
                .addProcessor(new ChestLootProcessor())
                .setFinalizeEntities(true);

        BlockPos placementPos = new BlockPos(
                this.boundingBox.minX(),
                this.boundingBox.minY() - 2,
                this.boundingBox.minZ()
        );

        template.placeInWorld(level, placementPos, placementPos, placementSettings, random, 2);
    }

    private static ResourceLocation getTemplateForVariant(int variant) {
        return switch (variant) {
            case 0 -> ResourceLocation.fromNamespaceAndPath(GoetyDelight.MODID, "ectoplasmic_melon_field_variant_a");
            case 1 -> ResourceLocation.fromNamespaceAndPath(GoetyDelight.MODID, "ectoplasmic_melon_field_variant_b");
            case 2 -> ResourceLocation.fromNamespaceAndPath(GoetyDelight.MODID, "ectoplasmic_melon_field_variant_c");
            default -> ResourceLocation.fromNamespaceAndPath(GoetyDelight.MODID, "ectoplasmic_melon_field_variant_a");
        };
    }
}