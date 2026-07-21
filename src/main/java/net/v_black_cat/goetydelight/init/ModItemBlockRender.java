package net.v_black_cat.goetydelight.init;

import net.minecraft.client.renderer.ItemBlockRenderTypes;
import net.minecraft.client.renderer.RenderType;
import net.neoforged.neoforge.client.ChunkRenderTypeSet;

public class ModItemBlockRender {
    public static void setRenderLayer(){
        ItemBlockRenderTypes.setRenderLayer(
                ModBlocks.ATTACHED_ECTOPLASMIC_MELON_STEM.get(),
                ChunkRenderTypeSet.of(RenderType.cutout())
        );
        ItemBlockRenderTypes.setRenderLayer(
                ModBlocks.DRIPMARBLE_BLOCK.get(),
                ChunkRenderTypeSet.of(RenderType.cutout())
        );
        ItemBlockRenderTypes.setRenderLayer(
                ModBlocks.ROYAL_CAKE_BLOCK.get(),
                ChunkRenderTypeSet.of(RenderType.translucent())
        );
        ItemBlockRenderTypes.setRenderLayer(
                ModBlocks.ECTOPLASMIC_MELON_BLOCK.get(),
                ChunkRenderTypeSet.of(RenderType.translucent())
        );
        ItemBlockRenderTypes.setRenderLayer(
                ModBlocks.BOAT_STUFFED_ROASTED_WARDEN_BlOCK.get(),
                ChunkRenderTypeSet.of(RenderType.cutout())
        );
        ItemBlockRenderTypes.setRenderLayer(
                ModBlocks.ROAST_LAOWANG_BLOCK.get(),
                ChunkRenderTypeSet.of(RenderType.cutout())
        );
        ItemBlockRenderTypes.setRenderLayer(
                ModBlocks.METAMORPHIC_SCENT_GRASS.get(),
                ChunkRenderTypeSet.of(RenderType.cutout())
        );
    }
}
