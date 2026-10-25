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
import net.minecraft.world.level.levelgen.structure.templatesystem.*;
import net.v_black_cat.goetydelight.GoetyDelight;
import net.v_black_cat.goetydelight.structures.ChestLootProcessor;
import net.v_black_cat.goetydelight.structures.ModStructurePieceTypes;
import net.v_black_cat.goetydelight.structures.SmartBottomProtectionProcessor;

public class AbandonedCampPiece extends StructurePiece {
    private final int variant;
    private final ResourceLocation templateLocation;

    public AbandonedCampPiece(StructureTemplateManager pTemplateManager, BlockPos pPos, int variant) {
        super(ModStructurePieceTypes.ABANDONED_CAMP_PIECE.get(), 0,
                createBoundingBox(pTemplateManager, pPos, variant));
        this.variant = variant;
        this.templateLocation = getTemplateForVariant(variant);
        this.setOrientation(Direction.NORTH);
    }

    public AbandonedCampPiece(StructurePieceSerializationContext context, CompoundTag tag) {
        super(ModStructurePieceTypes.ABANDONED_CAMP_PIECE.get(), tag);
        this.variant = tag.getInt("Variant");
        this.templateLocation = getTemplateForVariant(this.variant);
    }

    private static BoundingBox createBoundingBox(StructureTemplateManager templateManager,
                                                 BlockPos pPos, int variant) {
        ResourceLocation templateLocation = getTemplateForVariant(variant);
        StructureTemplate template = templateManager.get(templateLocation).orElse(null);

        if (template != null) {
            int sizeX = template.getSize().getX();
            int sizeZ = template.getSize().getZ();
            return new BoundingBox(pPos.getX(), pPos.getY(), pPos.getZ(),
                    pPos.getX() + sizeX - 1, pPos.getY() + template.getSize().getY(),
                    pPos.getZ() + sizeZ - 1);
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

        StructureTemplateManager templateManager = level.getLevel().getServer().getStructureManager();
        ResourceLocation templateLocation = getTemplateForVariant(this.variant);
        StructureTemplate template = templateManager.get(templateLocation).orElse(null);

        if (template == null) {
            GoetyDelight.LOGGER.error("Template not found: {}", templateLocation);
            return;
        }

        StructurePlaceSettings placementSettings = new StructurePlaceSettings()
                .setRotation(this.getRotation())
                .setMirror(this.getMirror())
                .setBoundingBox(box)
                .setIgnoreEntities(false)
                .addProcessor(BlockIgnoreProcessor.STRUCTURE_BLOCK)
                .addProcessor(new SmartBottomProtectionProcessor(2))
                .addProcessor(new ChestLootProcessor(
                        new ResourceLocation(GoetyDelight.MODID, "chests/abandoned_camp")))
                .setFinalizeEntities(true);

        BlockPos placementPos = new BlockPos(
                this.boundingBox.minX(),
                this.boundingBox.minY() - 2,
                this.boundingBox.minZ()
        );

        template.placeInWorld(level, placementPos, placementPos, placementSettings, random, 2);
    }

    private static ResourceLocation getTemplateForVariant(int variant) {
        return new ResourceLocation(GoetyDelight.MODID, "abandoned_camp");
    }
}