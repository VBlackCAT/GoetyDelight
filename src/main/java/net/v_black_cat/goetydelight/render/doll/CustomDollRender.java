package net.v_black_cat.goetydelight.render.doll;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import net.minecraft.client.model.Model;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.v_black_cat.goetydelight.block.CustomDollBlockEntity;
import net.v_black_cat.goetydelight.init.doll.CustomDollLoader;
import net.v_black_cat.goetydelight.init.doll.CustomDollReloadListener;
import org.apache.commons.lang3.StringUtils;

public class CustomDollRender implements BlockEntityRenderer<CustomDollBlockEntity> {
    public CustomDollRender(BlockEntityRendererProvider.Context render) {
    }

    @Override
    public void render(CustomDollBlockEntity doll, float partialTick, PoseStack poseStack,
                       MultiBufferSource bufferSource, int packedLight, int packedOverlay) {
        String modelId = doll.getModelId();
        Model model = CustomDollReloadListener.DFAULT_DOLL_MODEL;
        ResourceLocation texture;
        BlockState blockState = doll.getBlockState();
        if (StringUtils.isBlank(modelId)) {
            texture = getTextureByBlockState(blockState);
        } else {
            texture = getTextureByName(modelId);
        }
        Direction facing = blockState.getValue(HorizontalDirectionalBlock.FACING);
        poseStack.pushPose();
        poseStack.translate(0.5, 1.5, 0.5);
        poseStack.mulPose(Axis.ZN.rotationDegrees(180));
        poseStack.mulPose(Axis.YN.rotationDegrees(180 - facing.toYRot()));
        VertexConsumer buffer = bufferSource.getBuffer(RenderType.entityCutoutNoCull(texture));
        model.renderToBuffer(poseStack, buffer, packedLight, packedOverlay, 0xFFFFFFFF);

        poseStack.popPose();
    }

    private ResourceLocation getTextureByBlockState(BlockState blockState) {
        ResourceLocation blockId = BuiltInRegistries.BLOCK.getKey(blockState.getBlock());
        if (blockId == null || "custom_doll".equals(blockId.getPath())) {
            return CustomDollReloadListener.DEFAULT_TEXTURE_ID;
        }
        return getTextureByName(blockId.getPath());
    }

    private ResourceLocation getTextureByName(String modelId) {
        ResourceLocation texture = CustomDollLoader.getTexture(extractTextureNameFromModelId(modelId));
        return texture == null ? CustomDollReloadListener.DEFAULT_TEXTURE_ID : texture;
    }

    private String extractTextureNameFromModelId(String modelId) {
        if (modelId == null || !modelId.contains(".")) {
            return modelId;
        }
        String[] parts = modelId.split("\\.");
        return parts[parts.length - 1];
    }
}