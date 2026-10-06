package net.v_black_cat.goetydelight.compat.goetyrevelation.block;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.resources.ResourceLocation;
import org.joml.Matrix4f;

public class ApollyonCakeRenderer implements BlockEntityRenderer<ApollyonCakeBlockEntity> {

    private static final ResourceLocation TEXTURE =
            new ResourceLocation("goetydelight", "textures/block/apollyon_cake_1.png");

    private record CubeDef(
            float x1, float y1, float z1,
            float x2, float y2, float z2,
            float ox, float oy, float oz,
            float tiltX
    ) {}

    // 只保留第一个 cube
    private static final CubeDef CUBE =
            new CubeDef(0f, 29f, 0f, 16f, 29f, 16f, 8f, 29f, 8f, -22.5f);

    public ApollyonCakeRenderer(BlockEntityRendererProvider.Context ctx) {}

    @Override
    public void render(ApollyonCakeBlockEntity be, float partialTick, PoseStack poseStack,
                       MultiBufferSource buffer, int packedLight, int packedOverlay) {

        float time = (be.getLevel() != null ? be.getLevel().getGameTime() : 0) + partialTick;
        float yAngle = (time % 720f) * 0.5f;

        boolean isTheEnd = be.getBlockState().getValue(ApollyonCakeBlock.IS_THE_END);
        if (isTheEnd) {
            yAngle = 0f;
        }

        VertexConsumer consumer = buffer.getBuffer(RenderType.entityTranslucent(TEXTURE));

        poseStack.pushPose();

        float ox = CUBE.ox() / 16f;
        float oy = CUBE.oy() / 16f;
        float oz = CUBE.oz() / 16f;

        // 1) 初始 X 倾斜
        poseStack.translate(ox, oy, oz);
        poseStack.mulPose(Axis.XP.rotationDegrees(CUBE.tiltX()));
        poseStack.translate(-ox, -oy, -oz);

        // 2) 绕 Y 轴自转
        poseStack.translate(ox, oy, oz);
        poseStack.mulPose(Axis.YP.rotationDegrees(yAngle));
        poseStack.translate(-ox, -oy, -oz);

        Matrix4f m = poseStack.last().pose();
        renderCube(consumer, m, CUBE, packedLight, packedOverlay);

        poseStack.popPose();
    }

    private void renderCube(VertexConsumer c, Matrix4f m, CubeDef cube,
                            int light, int overlay) {

        float x1 = cube.x1() / 16f, y1 = cube.y1() / 16f, z1 = cube.z1() / 16f;
        float x2 = cube.x2() / 16f, y2 = cube.y2() / 16f, z2 = cube.z2() / 16f;

        float u0 = 0f, v0 = 0f, u1 = 1f, v1 = 1f;

        // down
        quad(c, m, light, overlay,
                x1, y1, z2, u0, v1,
                x2, y1, z2, u1, v1,
                x2, y1, z1, u1, v0,
                x1, y1, z1, u0, v0);

        // up
        quad(c, m, light, overlay,
                x1, y2, z1, u0, v0,
                x2, y2, z1, u1, v0,
                x2, y2, z2, u1, v1,
                x1, y2, z2, u0, v1);

        // north
        quad(c, m, light, overlay,
                x2, y1, z1, u0, v1,
                x1, y1, z1, u1, v1,
                x1, y2, z1, u1, v0,
                x2, y2, z1, u0, v0);

        // south
        quad(c, m, light, overlay,
                x1, y1, z2, u0, v1,
                x2, y1, z2, u1, v1,
                x2, y2, z2, u1, v0,
                x1, y2, z2, u0, v0);

        // west
        quad(c, m, light, overlay,
                x1, y1, z1, u0, v1,
                x1, y1, z2, u1, v1,
                x1, y2, z2, u1, v0,
                x1, y2, z1, u0, v0);

        // east
        quad(c, m, light, overlay,
                x2, y1, z2, u0, v1,
                x2, y1, z1, u1, v1,
                x2, y2, z1, u1, v0,
                x2, y2, z2, u0, v0);
    }

    private void quad(VertexConsumer c, Matrix4f m, int light, int overlay,
                      float x1, float y1, float z1, float u1, float v1,
                      float x2, float y2, float z2, float u2, float v2,
                      float x3, float y3, float z3, float u3, float v3,
                      float x4, float y4, float z4, float u4, float v4) {

        vertex(c, m, light, overlay, x1, y1, z1, u1, v1);
        vertex(c, m, light, overlay, x2, y2, z2, u2, v2);
        vertex(c, m, light, overlay, x3, y3, z3, u3, v3);

        vertex(c, m, light, overlay, x1, y1, z1, u1, v1);
        vertex(c, m, light, overlay, x3, y3, z3, u3, v3);
        vertex(c, m, light, overlay, x4, y4, z4, u4, v4);
    }

    private void vertex(VertexConsumer c, Matrix4f m, int light, int overlay,
                        float x, float y, float z, float u, float v) {
        c.vertex(m, x, y, z)
                .color(255, 255, 255, 255)
                .uv(u, v)
                .overlayCoords(overlay)
                .uv2(light)
                .normal(0f, 1f, 0f)
                .endVertex();
    }
}