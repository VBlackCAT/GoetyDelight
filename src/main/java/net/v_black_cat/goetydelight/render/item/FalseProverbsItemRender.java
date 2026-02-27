package net.v_black_cat.goetydelight.render.item;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.model.geom.EntityModelSet;
import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderDispatcher;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.event.EntityRenderersEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.v_black_cat.goetydelight.item.FalseProverbsItemModel;
import org.jetbrains.annotations.NotNull;

public class FalseProverbsItemRender extends BlockEntityWithoutLevelRenderer {
    public FalseProverbsItemRender(BlockEntityRenderDispatcher blockEntityRenderDispatcher, EntityModelSet entityModelSet) {
        super(blockEntityRenderDispatcher, entityModelSet);
        this.model = new FalseProverbsItemModel<>(entityModelSet.bakeLayer(FalseProverbsItemModel.LAYER_LOCATION));
    }

    private static final ResourceLocation TEXTURE = new ResourceLocation("goetydelight:textures/item/false_proverbs_model.png");
    private static final ResourceLocation GLOW_TEXTURE = new ResourceLocation("goetydelight:textures/item/false_proverbs_model_layer.png");
    private final FalseProverbsItemModel<?> model;

    public void renderByItem(@NotNull ItemStack itemStack, @NotNull ItemDisplayContext displayContext, @NotNull PoseStack poseStack, @NotNull MultiBufferSource multiBufferSource, int packedLight, int overlay) {
        VertexConsumer vertexConsumer = ItemRenderer.getFoilBufferDirect(multiBufferSource, RenderType.entityCutout(TEXTURE), true, itemStack.hasFoil());
        this.model.renderToBuffer(poseStack, vertexConsumer, packedLight, OverlayTexture.NO_OVERLAY, 1.0F, 1.0F, 1.0F, 1.0F);
        VertexConsumer glowVertexConsumer = ItemRenderer.getFoilBufferDirect(multiBufferSource, RenderType.entityCutoutNoCull(GLOW_TEXTURE), true, itemStack.hasFoil());
        this.model.renderToBuffer(poseStack, glowVertexConsumer, packedLight, OverlayTexture.NO_OVERLAY, 1.0F, 1.0F, 1.0F, 1.0F);
    }

    @OnlyIn(Dist.CLIENT)
    @SubscribeEvent
    public void onRegisterLayers(EntityRenderersEvent.RegisterLayerDefinitions event) {
        event.registerLayerDefinition(FalseProverbsItemModel.LAYER_LOCATION, FalseProverbsItemModel::createBodyLayer);
    }
}