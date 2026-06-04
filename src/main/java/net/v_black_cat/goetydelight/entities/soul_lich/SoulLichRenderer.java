package net.v_black_cat.goetydelight.entities.soul_lich;

import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.ResourceLocation;
import net.v_black_cat.goetydelight.entities.ModModelLayers;


public class SoulLichRenderer extends MobRenderer<SoulLichEntity, SoulLichModel<SoulLichEntity>> {
    public SoulLichRenderer(EntityRendererProvider.Context pContext) {
        super(pContext, new SoulLichModel<>(pContext.bakeLayer(ModModelLayers.SOUL_LICH)), 0f);
        this.addLayer(new SoulLichGlowLayer(this));
    }

    @Override
    public ResourceLocation getTextureLocation(SoulLichEntity pEntity) {
        return new ResourceLocation("goetydelight", "textures/entity/soul_lich.png");
    }
}
