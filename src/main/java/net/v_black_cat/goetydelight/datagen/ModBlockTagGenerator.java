package net.v_black_cat.goetydelight.datagen;

import net.v_black_cat.goetydelight.GoetyDelight;
import net.v_black_cat.goetydelight.block.ModBlocks;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.minecraft.tags.BlockTags;
import net.minecraftforge.common.data.BlockTagsProvider;
import net.minecraftforge.common.data.ExistingFileHelper;
import org.jetbrains.annotations.Nullable;

import java.util.concurrent.CompletableFuture;

public class ModBlockTagGenerator extends BlockTagsProvider {
    public ModBlockTagGenerator(PackOutput output, CompletableFuture<HolderLookup.Provider> lookupProvider, @Nullable ExistingFileHelper existingFileHelper) {
        super(output, lookupProvider, GoetyDelight.MODID, existingFileHelper);
    }

    @Override
    protected void addTags(HolderLookup.Provider pProvider) {
//        this.tag(ModTags.Blocks.METAL_DETECTOR_VALUABLES)
//                .add(ModBlocks.MARBLE_ORE.get()).addTag(Tags.Blocks.ORES);

//        this.tag(BlockTags.MINEABLE_WITH_PICKAXE)
//                .add(ModBlocks.MARBLE_BLOCK.get(),
//                        ModBlocks.RAW_MARBLE_BLOCK.get(),
//                        ModBlocks.MARBLE_ORE.get(),
//                        ModBlocks.DEEPSLATE_MARBLE_ORE.get(),
//                        ModBlocks.NETHER_MARBLE_ORE.get(),
//                        ModBlocks.END_STONE_MARBLE_ORE.get(),
//                        ModBlocks.SOUND_BLOCK.get());


//        this.tag(BlockTags.NEEDS_IRON_TOOL)
//                .add(ModBlocks.MARBLE_BLOCK.get());
//
//        this.tag(BlockTags.NEEDS_DIAMOND_TOOL)
//                .add(ModBlocks.RAW_MARBLE_BLOCK.get());
//
//        this.tag(BlockTags.NEEDS_STONE_TOOL)
//                .add(ModBlocks.NETHER_MARBLE_ORE.get());
//
//        this.tag(Tags.Blocks.NEEDS_NETHERITE_TOOL)
//                .add(ModBlocks.END_STONE_MARBLE_ORE.get());

        this.tag(BlockTags.FENCES)
                .add(ModBlocks.MARBLE_FENCE.get());
        this.tag(BlockTags.FENCE_GATES)
                .add(ModBlocks.MARBLE_FENCE_GATE.get());
        this.tag(BlockTags.WALLS)
                .add(ModBlocks.MARBLE_WALL.get());


    }
}
