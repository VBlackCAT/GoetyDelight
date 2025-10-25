package net.v_black_cat.goetydelight.entities;

import com.Polarice3.Goety.Goety;
import com.Polarice3.Goety.client.render.ModRenderType;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.EyesLayer;
import net.minecraft.resources.ResourceLocation;
import net.v_black_cat.goetydelight.entities.GhostFarmerEntity;
import net.v_black_cat.goetydelight.entities.GhostFarmerModel;

public class GhostFarmerGlowLayer extends EyesLayer<GhostFarmerEntity, GhostFarmerModel<GhostFarmerEntity>> {
    private static final RenderType GLOW_TYPE = RenderType.entityTranslucentEmissive(
            new ResourceLocation("goetydelight", "textures/entity/ghost_farmer_emissive.png")
    );
    public GhostFarmerGlowLayer(RenderLayerParent<GhostFarmerEntity, GhostFarmerModel<GhostFarmerEntity>> renderer) {
        super(renderer);
    }

    public RenderType renderType() {
        return GLOW_TYPE;
    }


}