package net.v_black_cat.goetydelight.compat.goetyrevelation.block;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.BlockElement;
import net.minecraft.client.renderer.block.model.BlockElementFace;
import net.minecraft.client.renderer.block.model.BlockModel;
import net.minecraft.client.resources.model.*;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import org.joml.Vector3f;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class ApollyonCakeModel {

    /**
     * 对应：
     * assets/goetydelight/models/block/apollyon_cake.json
     */
    private static final ResourceLocation MODEL_LOCATION =
            new ResourceLocation("goetydelight", "block/apollyon_cake");

    /**
     * 使用模型原本的 X0_Y0 状态。
     */
    private static final ModelState MODEL_STATE =
            BlockModelRotation.X0_Y0;

    /**
     * 动画元素的旋转中心：
     *
     * JSON：
     * from = [0, 29, 0]
     * to   = [16, 29, 16]
     *
     * origin = [8, 29, 8]
     */
    private static final float ANIMATION_ORIGIN_X = 8.0F / 16.0F;
    private static final float ANIMATION_ORIGIN_Y = 29.0F / 16.0F;
    private static final float ANIMATION_ORIGIN_Z = 8.0F / 16.0F;

    private final List<BakedQuad> staticQuads = new ArrayList<>();
    private final List<BakedQuad> animatedQuads = new ArrayList<>();

    /**
     * 这个向量不是世界 Y。
     *
     * 它是 FaceBakery 根据 JSON：
     *
     * rotation:
     *   angle: -22.5
     *   axis: x
     *   origin: [8,29,8]
     *
     * 烘焙出来的那个“大面”的真实法线。
     */
    private Vector3f animatedNormal = new Vector3f(0.0F, 1.0F, 0.0F);

    public ApollyonCakeModel() {
        bakeModel();
    }

    private void bakeModel() {
        Minecraft minecraft = Minecraft.getInstance();

        ModelManager modelManager = minecraft.getModelManager();
        ModelBakery modelBakery = modelManager.getModelBakery();

        UnbakedModel unbakedModel =
                modelBakery.getModel(MODEL_LOCATION);

        if (!(unbakedModel instanceof BlockModel blockModel)) {
            throw new IllegalStateException(
                    "Model is not a BlockModel: " + MODEL_LOCATION
            );
        }

        TextureAtlas blockAtlas =
                modelManager.getAtlas(TextureAtlas.LOCATION_BLOCKS);

        for (BlockElement element : blockModel.getElements()) {

            boolean animated = isAnimatedElement(element);

            for (Map.Entry<Direction, BlockElementFace> entry
                    : element.faces.entrySet()) {

                Direction direction = entry.getKey();
                BlockElementFace face = entry.getValue();

                /*
                 * 这里使用 BlockModel 自己解析 #2、#1 等 texture reference。
                 *
                 * 不要直接把 face.texture 当 ResourceLocation。
                 */
                Material material =
                        blockModel.getMaterial(face.texture);

                if (material == null) {
                    throw new IllegalStateException(
                            "Cannot resolve texture '" +
                                    face.texture +
                                    "' in model " +
                                    MODEL_LOCATION
                    );
                }

                TextureAtlasSprite sprite =
                        blockAtlas.getSprite(material.texture());

                /*
                 * Forge 1.20.1 正确签名：
                 *
                 * bakeFace(
                 *     BlockElement,
                 *     BlockElementFace,
                 *     TextureAtlasSprite,
                 *     Direction,
                 *     ModelState,
                 *     ResourceLocation
                 * )
                 *
                 * 注意：
                 * element 本身传进去以后，
                 * FaceBakery 会处理 element.rotation。
                 */
                BakedQuad quad = BlockModel.bakeFace(
                        element,
                        face,
                        sprite,
                        direction,
                        MODEL_STATE,
                        MODEL_LOCATION
                );

                if (animated) {

                    /*
                     * 这个元素：
                     *
                     * from = [0,29,0]
                     * to   = [16,29,16]
                     *
                     * 因为 Y 厚度为 0：
                     *
                     * UP
                     * DOWN
                     *
                     * 两个面完全重合。
                     *
                     * 如果两个都渲染，就会发生 Z-fighting，
                     * 表现为忽明忽暗、闪烁。
                     *
                     * 所以只保留 UP。
                     */
                    if (direction == Direction.UP) {
                        animatedQuads.add(quad);
                    }

                } else {
                    staticQuads.add(quad);
                }
            }
        }

        /*
         * 根据 FaceBakery 最终生成的动画面顶点，
         * 计算这个面的真实法线。
         */
        calculateAnimatedNormal();
    }

    /**
     * 判断是不是唯一的旋转 Cube。
     *
     * 必须严格匹配：
     *
     * from = [0,29,0]
     * to   = [16,29,16]
     */
    private static boolean isAnimatedElement(BlockElement element) {

        return approximatelyEquals(element.from.x(), 0.0F)
                && approximatelyEquals(element.from.y(), 29.0F)
                && approximatelyEquals(element.from.z(), 0.0F)

                && approximatelyEquals(element.to.x(), 16.0F)
                && approximatelyEquals(element.to.y(), 29.0F)
                && approximatelyEquals(element.to.z(), 16.0F);
    }

    private static boolean approximatelyEquals(float a, float b) {
        return Math.abs(a - b) < 0.001F;
    }

    /**
     * 从已经经过 FaceBakery 处理的 UP Quad
     * 计算真实几何法线。
     *
     * 这样不用自己重复计算 JSON 的 -22.5° X 旋转。
     */
    private void calculateAnimatedNormal() {

        if (animatedQuads.isEmpty()) {
            animatedNormal.set(0.0F, 1.0F, 0.0F);
            return;
        }

        BakedQuad quad = animatedQuads.get(0);

        int[] vertices = quad.getVertices();

        /*
         * Minecraft 1.20.1 默认 BLOCK vertex format：
         *
         * 每个顶点 8 个 int：
         *
         * 0 = X
         * 1 = Y
         * 2 = Z
         * 3 = Color
         * 4 = U
         * 5 = V
         * 6 = Light
         * 7 = Normal
         */

        int vertexSize = 8;

        float x1 = Float.intBitsToFloat(vertices[0]);
        float y1 = Float.intBitsToFloat(vertices[1]);
        float z1 = Float.intBitsToFloat(vertices[2]);

        float x2 = Float.intBitsToFloat(vertices[vertexSize]);
        float y2 = Float.intBitsToFloat(vertices[vertexSize + 1]);
        float z2 = Float.intBitsToFloat(vertices[vertexSize + 2]);

        float x3 = Float.intBitsToFloat(vertices[vertexSize * 2]);
        float y3 = Float.intBitsToFloat(vertices[vertexSize * 2 + 1]);
        float z3 = Float.intBitsToFloat(vertices[vertexSize * 2 + 2]);

        Vector3f edge1 = new Vector3f(
                x2 - x1,
                y2 - y1,
                z2 - z1
        );

        Vector3f edge2 = new Vector3f(
                x3 - x1,
                y3 - y1,
                z3 - z1
        );

        edge1.cross(edge2);

        if (edge1.lengthSquared() < 1.0E-8F) {
            /*
             * 理论上不会发生。
             */
            animatedNormal.set(
                    0.0F,
                    1.0F,
                    0.0F
            );
            return;
        }

        edge1.normalize();

        animatedNormal.set(edge1);
    }

    /**
     * 渲染静态部分。
     */
    public void renderStatic(
            PoseStack poseStack,
            VertexConsumer buffer,
            float red,
            float green,
            float blue,
            float alpha,
            int packedLight,
            int packedOverlay
    ) {
        renderQuads(
                poseStack,
                buffer,
                staticQuads,
                red,
                green,
                blue,
                alpha,
                packedLight,
                packedOverlay
        );
    }

    /**
     * 渲染旋转环。
     *
     * 注意：
     *
     * yAngle 这个名字现在只是“动画角度”。
     * 它不再意味着绕世界 Y 轴旋转。
     *
     * 实际旋转轴：
     *
     * animatedNormal
     */
    public void renderAnimated(
            PoseStack poseStack,
            VertexConsumer buffer,
            float angle,
            Vector3f worldNormal, float red,
            float green,
            float blue,
            float alpha,
            int packedLight,
            int packedOverlay
    ) {

        if (animatedQuads.isEmpty()) {
            return;
        }

        poseStack.pushPose();

        /*
         * 以 JSON 的 origin：
         *
         * [8,29,8]
         *
         * 作为旋转中心。
         */
        poseStack.translate(
                ANIMATION_ORIGIN_X,
                ANIMATION_ORIGIN_Y,
                ANIMATION_ORIGIN_Z
        );

        /*
         * 关键：
         *
         * 不再：
         *
         * Axis.YP.rotationDegrees(angle)
         *
         * 而是：
         *
         * 绕动画平面自己的法线旋转。
         *
         * 这样法线本身不会随着动画旋转。
         */
        poseStack.mulPose(
                new org.joml.Quaternionf()
                        .fromAxisAngleDeg(
                                animatedNormal.x(),
                                animatedNormal.y(),
                                animatedNormal.z(),
                                angle
                        )
        );

        /*
         * 回到模型局部坐标。
         */
        poseStack.translate(
                -ANIMATION_ORIGIN_X,
                -ANIMATION_ORIGIN_Y,
                -ANIMATION_ORIGIN_Z
        );

        renderQuads(
                poseStack,
                buffer,
                animatedQuads,
                red,
                green,
                blue,
                alpha,
                packedLight,
                packedOverlay
        );

        poseStack.popPose();
    }

    private void renderQuads(
            PoseStack poseStack,
            VertexConsumer buffer,
            List<BakedQuad> quads,
            float red,
            float green,
            float blue,
            float alpha,
            int packedLight,
            int packedOverlay
    ) {

        PoseStack.Pose pose = poseStack.last();

        for (BakedQuad quad : quads) {

            /*
             * Forge 的 putBulkData 会正确处理：
             *
             * - BakedQuad 顶点
             * - UV
             * - baked normal
             * - light
             * - overlay
             *
             * 这比我们手写 vertex() 要可靠得多。
             */
            buffer.putBulkData(
                    pose,
                    quad,
                    red,
                    green,
                    blue,
                    alpha,
                    packedLight,
                    packedOverlay,
                    true
            );
        }
    }

    public int getStaticQuadCount() {
        return staticQuads.size();
    }

    public int getAnimatedQuadCount() {
        return animatedQuads.size();
    }

    public Vector3f getAnimatedNormal() {
        return new Vector3f(animatedNormal);
    }
}