package net.v_black_cat.goetydelight.compat.goetyrevelation.block;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.state.BlockState;
import org.joml.Vector3f;

public class ApollyonCakeRenderer implements BlockEntityRenderer<ApollyonCakeBlockEntity> {
    private final ApollyonCakeModel model;
    public ApollyonCakeRenderer(BlockEntityRendererProvider.Context context) {
        this.model = new ApollyonCakeModel();
    }
    @Override
    public void render(ApollyonCakeBlockEntity blockEntity, float partialTick, PoseStack poseStack, MultiBufferSource buffer, int packedLight, int packedOverlay) {
        BlockState state = blockEntity.getBlockState();
        boolean isTheEnd = state.getValue(ApollyonCakeBlock.IS_THE_END);
        model.setTheEnd(isTheEnd);
        long gameTime = 0L;
        if (blockEntity.getLevel() != null) {
            gameTime = blockEntity.getLevel().getGameTime();
        }
        float time = gameTime + partialTick;
        float angle = (time % 720.0F) * 0.5F;
        if (isTheEnd) {angle = 0.0F;}
        Direction facing = state.getValue(ApollyonCakeBlock.FACING);
        VertexConsumer consumer = buffer.getBuffer(RenderType.entityCutoutNoCull(TextureAtlas.LOCATION_BLOCKS));
        poseStack.pushPose();
        float blockRotation = getBlockRotation(facing);
        poseStack.translate(0.5F, 0.0F, 0.5F);
        poseStack.mulPose(Axis.YP.rotationDegrees(blockRotation));
        poseStack.translate(-0.5F, 0.0F, -0.5F);
        model.renderStatic(poseStack, consumer, 1.0F, 1.0F, 1.0F, 1.0F, packedLight, packedOverlay);
        Vector3f worldNormal = rotateNormalForFacing(model.getAnimatedNormal(), facing);
        model.renderAnimated(poseStack, consumer, angle, worldNormal, 1.0F, 1.0F, 1.0F, 1.0F, packedLight, packedOverlay);
        poseStack.popPose();
    }
    private static float getBlockRotation(Direction facing) {
        return switch (facing) {
            case NORTH -> 0.0F;
            case EAST  -> 270.0F;
            case SOUTH -> 180.0F;
            case WEST  -> 90.0F;
            default -> 0.0F;
        };
    }
    private static Vector3f rotateNormalForFacing(Vector3f modelNormal, Direction facing) {
        Vector3f normal = new Vector3f(modelNormal);
        float rotation = getBlockRotation(facing);
        normal.rotateY((float) Math.toRadians(rotation));
        return normal.normalize();
    }
}