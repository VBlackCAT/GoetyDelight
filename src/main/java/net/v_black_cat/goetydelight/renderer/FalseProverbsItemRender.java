package net.v_black_cat.goetydelight.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.model.geom.EntityModelSet;
import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderDispatcher;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.v_black_cat.goetydelight.GoetyDelight;
import net.v_black_cat.goetydelight.item.FalseProverbsItemModel;

public class FalseProverbsItemRender extends BlockEntityWithoutLevelRenderer {
    private static final ResourceLocation TEXTURE = ResourceLocation.parse(GoetyDelight.MODID + ":textures/item/false_proverbs_model.png");
    private static final ResourceLocation GLOW_TEXTURE = ResourceLocation.parse(GoetyDelight.MODID + ":textures/item/false_proverbs_model_layer.png");
    private final FalseProverbsItemModel<?> model;
    private boolean logged = false;

    public FalseProverbsItemRender(BlockEntityRenderDispatcher dispatcher, EntityModelSet modelSet) {
        super(dispatcher, modelSet);
        this.model = new FalseProverbsItemModel<>(modelSet.bakeLayer(FalseProverbsItemModel.LAYER_LOCATION));
    }

    @Override
    public void renderByItem(ItemStack stack, ItemDisplayContext context, PoseStack poseStack, MultiBufferSource buffer, int packedLight, int packedOverlay) {

        VertexConsumer mainConsumer = buffer.getBuffer(RenderType.entityCutout(TEXTURE));
        this.model.renderToBuffer(poseStack, mainConsumer, packedLight, OverlayTexture.NO_OVERLAY, 0xFFFFFFFF);

        VertexConsumer glowConsumer = buffer.getBuffer(RenderType.entityTranslucent(GLOW_TEXTURE));
        this.model.renderToBuffer(poseStack, glowConsumer, packedLight, OverlayTexture.NO_OVERLAY, 0xFFFFFFFF);
    }
}