package net.v_black_cat.goetydelight.entities.soul_lich;

import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.EyesLayer;
import net.minecraft.resources.ResourceLocation;

public class SoulLichGlowLayer extends EyesLayer<SoulLichEntity, SoulLichModel<SoulLichEntity>> {
    private static final RenderType GLOW_TYPE = RenderType.entityTranslucentEmissive(
            ResourceLocation.fromNamespaceAndPath("goetydelight", "textures/entity/soul_lich_emissive.png")
    );

    public SoulLichGlowLayer(RenderLayerParent<SoulLichEntity, SoulLichModel<SoulLichEntity>> renderer) {
        super(renderer);
    }

    public RenderType renderType() {
        return GLOW_TYPE;
    }
}
