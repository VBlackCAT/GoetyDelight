package net.v_black_cat.goetydelight.datagen;


import net.minecraft.data.PackOutput;
import net.minecraft.world.level.block.*;
import net.minecraftforge.client.model.generators.BlockStateProvider;
import net.minecraftforge.common.data.ExistingFileHelper;
import net.minecraftforge.registries.RegistryObject;
import net.v_black_cat.goetydelight.GoetyDelight;
import net.v_black_cat.goetydelight.block.ModBlocks;

public class ModBlockStateProvider extends BlockStateProvider {
    public ModBlockStateProvider(PackOutput output, ExistingFileHelper exFileHelper) {
        super(output, GoetyDelight.MODID, exFileHelper);
    }

    @Override
    protected void registerStatesAndModels() {



        blockWithItem(ModBlocks.SILT_MARBLE_HEAVY);


        stairsBlock(((StairBlock) ModBlocks.MARBLE_STAIRS.get()), blockTexture(ModBlocks.MARBLE.get()));
        slabBlock(((SlabBlock) ModBlocks.MARBLE_SLAB.get()), blockTexture(ModBlocks.MARBLE.get()), blockTexture(ModBlocks.MARBLE.get()));

        buttonBlock(((ButtonBlock) ModBlocks.MARBLE_BUTTON.get()), blockTexture(ModBlocks.MARBLE.get()));
        pressurePlateBlock(((PressurePlateBlock) ModBlocks.MARBLE_PRESSURE_PLATE.get()), blockTexture(ModBlocks.MARBLE.get()));

        fenceBlock(((FenceBlock) ModBlocks.MARBLE_FENCE.get()), blockTexture(ModBlocks.MARBLE.get()));
        fenceGateBlock(((FenceGateBlock) ModBlocks.MARBLE_FENCE_GATE.get()), blockTexture(ModBlocks.MARBLE.get()));
        wallBlock(((WallBlock) ModBlocks.MARBLE_WALL.get()), blockTexture(ModBlocks.MARBLE.get()));

        doorBlockWithRenderType(((DoorBlock) ModBlocks.MARBLE_DOOR.get()), modLoc("block/marble_door_bottom"), modLoc("block/marble_door_top"), "cutout");
        trapdoorBlockWithRenderType(((TrapDoorBlock) ModBlocks.MARBLE_TRAPDOOR.get()), modLoc("block/marble_trapdoor"), true, "cutout");
    }

    private void blockWithItem(RegistryObject<Block> blockRegistryObject) {
        simpleBlockWithItem(blockRegistryObject.get(), cubeAll(blockRegistryObject.get()));
    }

}
