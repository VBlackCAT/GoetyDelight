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
    private static final ResourceLocation MODEL_APOLLYON_CAKE = new ResourceLocation("goetydelight", "block/apollyon_cake");
    private static final ResourceLocation MODEL_THE_END_CAKE = new ResourceLocation("goetydelight", "block/the_end_cake");
    private static final ModelState MODEL_STATE = BlockModelRotation.X0_Y0;

    private static final float ANIMATION_ORIGIN_X = 8.0F / 16.0F;
    private static final float ANIMATION_ORIGIN_Y = 29.0F / 16.0F;
    private static final float ANIMATION_ORIGIN_Z = 8.0F / 16.0F;

    private final List<BakedQuad> staticQuads = new ArrayList<>();
    private final List<BakedQuad> animatedQuads = new ArrayList<>();

    private boolean isTheEnd = false;

    private Vector3f animatedNormal = new Vector3f(0.0F, 1.0F, 0.0F);

    public ApollyonCakeModel() {
        this(false);
    }

    public ApollyonCakeModel(boolean isTheEnd) {
        this.isTheEnd = isTheEnd;
        bakeModel(getCurrentModelLocation());
    }

    public boolean setTheEnd(boolean isTheEnd) {
        if (this.isTheEnd == isTheEnd) {
            return false;
        }
        this.isTheEnd = isTheEnd;
        bakeModel(getCurrentModelLocation());
        return true;
    }

    public boolean isTheEnd() {
        return isTheEnd;
    }

    private ResourceLocation getCurrentModelLocation() {
        return isTheEnd ? MODEL_THE_END_CAKE : MODEL_APOLLYON_CAKE;
    }

    private void bakeModel(ResourceLocation modelLocation) {
        staticQuads.clear();
        animatedQuads.clear();
        animatedNormal.set(0.0F, 1.0F, 0.0F);

        Minecraft minecraft = Minecraft.getInstance();

        ModelManager modelManager = minecraft.getModelManager();
        ModelBakery modelBakery = modelManager.getModelBakery();

        UnbakedModel unbakedModel = modelBakery.getModel(modelLocation);

        if (!(unbakedModel instanceof BlockModel blockModel)) {
            throw new IllegalStateException("Model is not a BlockModel: " + modelLocation);
        }
        TextureAtlas blockAtlas = modelManager.getAtlas(TextureAtlas.LOCATION_BLOCKS);
        for (BlockElement element : blockModel.getElements()) {
            boolean animated = isAnimatedElement(element);
            for (Map.Entry<Direction, BlockElementFace> entry : element.faces.entrySet()) {
                Direction direction = entry.getKey();
                BlockElementFace face = entry.getValue();
                Material material = blockModel.getMaterial(face.texture);
                if (material == null) {
                    throw new IllegalStateException("Cannot resolve texture '" + face.texture + "' in model " + modelLocation);
                }
                TextureAtlasSprite sprite = blockAtlas.getSprite(material.texture());
                BakedQuad quad = BlockModel.bakeFace(
                        element,
                        face,
                        sprite,
                        direction,
                        MODEL_STATE,
                        modelLocation
                );
                if (animated) {
                    if (direction == Direction.UP) {
                        animatedQuads.add(quad);
                    }
                } else {
                    staticQuads.add(quad);
                }
            }
        }
        calculateAnimatedNormal();
    }
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
    private void calculateAnimatedNormal() {
        if (animatedQuads.isEmpty()) {
            animatedNormal.set(0.0F, 1.0F, 0.0F);
            return;
        }
        BakedQuad quad = animatedQuads.get(0);
        int[] vertices = quad.getVertices();
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
    public void renderStatic(PoseStack poseStack, VertexConsumer buffer, float red, float green, float blue, float alpha, int packedLight, int packedOverlay) {
        renderQuads(poseStack, buffer, staticQuads, red, green, blue, alpha, packedLight, packedOverlay);
    }

    public void renderAnimated(PoseStack poseStack, VertexConsumer buffer, float angle, Vector3f worldNormal, float red, float green, float blue, float alpha, int packedLight, int packedOverlay) {
        if (animatedQuads.isEmpty()) {
            return;
        }
        poseStack.pushPose();
        poseStack.translate(ANIMATION_ORIGIN_X, ANIMATION_ORIGIN_Y, ANIMATION_ORIGIN_Z);
        poseStack.mulPose(new org.joml.Quaternionf().fromAxisAngleDeg(animatedNormal.x(), animatedNormal.y(), animatedNormal.z(), angle));
        poseStack.translate(-ANIMATION_ORIGIN_X, -ANIMATION_ORIGIN_Y, -ANIMATION_ORIGIN_Z);
        renderQuads(poseStack, buffer, animatedQuads, red, green, blue, alpha, packedLight, packedOverlay);
        poseStack.popPose();
    }

    private void renderQuads(PoseStack poseStack, VertexConsumer buffer, List<BakedQuad> quads, float red, float green, float blue, float alpha, int packedLight, int packedOverlay) {
        PoseStack.Pose pose = poseStack.last();
        for (BakedQuad quad : quads) {
            buffer.putBulkData(pose, quad, red, green, blue, alpha, packedLight, packedOverlay, true);
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