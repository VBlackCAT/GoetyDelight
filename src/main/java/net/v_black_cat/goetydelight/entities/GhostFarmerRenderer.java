package net.v_black_cat.goetydelight.entities;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.ResourceLocation;
import net.v_black_cat.goetydelight.GoetyDelight;


public class GhostFarmerRenderer extends MobRenderer<GhostFarmerEntity, GhostFarmerModel<GhostFarmerEntity>> {


    public GhostFarmerRenderer(EntityRendererProvider.Context pContext) {
        super(pContext, new GhostFarmerModel<>(pContext.bakeLayer(ModModelLayers.GHOST_FARMER_LAYER)), 1f);
    }


    @Override
    public ResourceLocation getTextureLocation(GhostFarmerEntity pEntity) {
        return new ResourceLocation(GoetyDelight.MODID, "textures/entity/ghostfarmer.png");
    }





    @Override
    public void render(GhostFarmerEntity pEntity, float pEntityYaw, float pPartialTicks, PoseStack pMatrixStack, MultiBufferSource pBuffer, int pPackedLight) {

        if (pEntity.isBaby()) {
            pMatrixStack.scale(0.5f, 0.5f, 0.5f);

        }

        super.render(pEntity, pEntityYaw, pPartialTicks, pMatrixStack, pBuffer, pPackedLight);
    }

}
