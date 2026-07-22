package net.v_black_cat.goetydelight.entities.ghostfarmer;

import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.EyesLayer;
import net.minecraft.resources.ResourceLocation;

public class GhostFarmerGlowLayer extends EyesLayer<GhostFarmerEntity, GhostFarmerModel<GhostFarmerEntity>> {
    private static final RenderType GLOW_TYPE = RenderType.entityTranslucentEmissive(
            ResourceLocation.fromNamespaceAndPath("goetydelight", "textures/entity/ghost_farmer_emissive.png")
    );

    public GhostFarmerGlowLayer(RenderLayerParent<GhostFarmerEntity, GhostFarmerModel<GhostFarmerEntity>> renderer) {
        super(renderer);
    }

    public RenderType renderType() {
        return GLOW_TYPE;
    }
}
