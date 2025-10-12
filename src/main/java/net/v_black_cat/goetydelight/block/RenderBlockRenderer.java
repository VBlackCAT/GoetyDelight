package net.v_black_cat.goetydelight.block;

import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.blaze3d.vertex.VertexFormat;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderStateShard;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.phys.Vec3;
import net.v_black_cat.goetydelight.GoetyDelight;
import net.v_black_cat.goetydelight.render.test.ModShaderInstance;
import net.v_black_cat.goetydelight.render.test.ModShaderReg;
import org.jetbrains.annotations.NotNull;
import org.joml.Matrix4f;
import org.joml.Quaternionf;

public class RenderBlockRenderer implements BlockEntityRenderer<RenderBlockEntity> {

    private static final ResourceLocation DUMMY_TEXTURE =
            new ResourceLocation(GoetyDelight.MODID, "textures/effect/test_texture.png");


    private static final RenderType SHADER_RENDER_TYPE = RenderType.create(
            "shader_block",
            DefaultVertexFormat.POSITION_TEX,
            VertexFormat.Mode.QUADS,
            256,
            false,
            false,
            RenderType.CompositeState.builder()
                    .setShaderState(new RenderStateShard.ShaderStateShard(() -> ModShaderReg.getFloridShader()))
                    .setTextureState(new RenderStateShard.TextureStateShard(DUMMY_TEXTURE, false, false))
                    .createCompositeState(false)
    );

    public RenderBlockRenderer(BlockEntityRendererProvider.Context context) {
    }

    @Override
    public void render(RenderBlockEntity blockEntity, float partialTick, PoseStack poseStack,
                       MultiBufferSource bufferSource, int packedLight, int packedOverlay) {

        // 获取着色器实例
        ModShaderInstance shader = (ModShaderInstance) ModShaderReg.getFloridShader();
        if (shader == null) {
            // 着色器未正确注册，输出错误信息
            GoetyDelight.LOGGER.error("Florid shader is not registered!");
            return;
        }

        // 设置着色器时间
        shader.setTime(blockEntity.getShaderTime());

        poseStack.pushPose();
        poseStack.translate( 0.5,0.5  , 0.5); // 居中渲染

        VertexConsumer buffer = bufferSource.getBuffer(SHADER_RENDER_TYPE);

        // 创建四边形
        float size = 0.5f;
        Vec3[] vertices = {
                new Vec3(-size, -size, 0),
                new Vec3(-size, size, 0),
                new Vec3(size, size, 0),
                new Vec3(size, -size, 0)
        };

        // 渲染每个面
        for (Direction direction : Direction.values()) {
            poseStack.pushPose();
            switch (direction) {
                case UP -> poseStack.translate(0, 0.5, 0);
                case DOWN -> poseStack.translate(0, -0.5, 0);
                case NORTH -> poseStack.translate(0, 0, 0.5);
                case SOUTH -> poseStack.translate(0, 0, -0.5);
                case EAST -> poseStack.translate(-0.5, 0, 0);
                case WEST -> poseStack.translate(0.5, 0, 0);
            }
            renderQuad(poseStack, buffer, vertices, direction, packedLight, packedOverlay);
            poseStack.popPose();
        }

        poseStack.popPose();
    }

    private void renderQuad(PoseStack poseStack, VertexConsumer buffer, Vec3[] vertices,
                            Direction direction, int packedLight, int packedOverlay) {
        poseStack.pushPose();

        // 根据方向旋转
        Quaternionf rotation = getQuaternionf(direction);
        poseStack.mulPose(rotation);

        Matrix4f matrix = poseStack.last().pose();

        // 添加顶点（使用与着色器匹配的格式）
        buffer.vertex(matrix, (float)vertices[0].x, (float)vertices[0].y, (float)vertices[0].z)
                .uv(0, 0)
                .overlayCoords(packedOverlay)
                .uv2(packedLight)
                .endVertex();

        buffer.vertex(matrix, (float)vertices[1].x, (float)vertices[1].y, (float)vertices[1].z)
                .uv(0, 1)
                .overlayCoords(packedOverlay)
                .uv2(packedLight)
                .endVertex();

        buffer.vertex(matrix, (float)vertices[2].x, (float)vertices[2].y, (float)vertices[2].z)
                .uv(1, 1)
                .overlayCoords(packedOverlay)
                .uv2(packedLight)
                .endVertex();

        buffer.vertex(matrix, (float)vertices[3].x, (float)vertices[3].y, (float)vertices[3].z)
                .uv(1, 0)
                .overlayCoords(packedOverlay)
                .uv2(packedLight)
                .endVertex();

        poseStack.popPose();
    }

    private static @NotNull Quaternionf getQuaternionf(Direction direction) {
        Quaternionf rotation = new Quaternionf();
        switch (direction) {
            case UP -> rotation.rotateX((float) Math.toRadians(90));
            case DOWN -> rotation.rotateX((float) Math.toRadians(-90));
            case NORTH -> rotation.rotateY((float) Math.toRadians(180));
            case EAST -> rotation.rotateY((float) Math.toRadians(90));
            case WEST -> rotation.rotateY((float) Math.toRadians(-90));
            case SOUTH -> rotation.rotateY((float) Math.toRadians(0));
            default -> {} // SOUTH不需要旋转
        }
        return rotation;
    }
}