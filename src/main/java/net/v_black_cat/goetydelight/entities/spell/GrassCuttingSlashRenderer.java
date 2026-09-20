package net.v_black_cat.goetydelight.entities.spell;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import org.joml.Matrix4f;

public class GrassCuttingSlashRenderer extends EntityRenderer<GrassCuttingSlashEntity> {
    private static final ResourceLocation TEXTURE =
            new ResourceLocation("minecraft", "textures/misc/white.png");
    private static final int SEGMENTS = 32;

    public GrassCuttingSlashRenderer(EntityRendererProvider.Context context) {
        super(context);
        this.shadowRadius = 0.0F;
    }

    @Override
    public void render(GrassCuttingSlashEntity entity, float entityYaw, float partialTick, PoseStack poseStack,
                       MultiBufferSource bufferSource, int packedLight) {
        float age = entity.animationTime(partialTick);
        float activeTicks = GrassCuttingSlashEntity.TOTAL_LIFETIME_TICKS - GrassCuttingSlashEntity.FADE_TICKS;
        float fade = age <= activeTicks
                ? 1.0F
                : 1.0F - Mth.clamp((age - activeTicks)
                        / GrassCuttingSlashEntity.FADE_TICKS, 0.0F, 1.0F);
        if (fade <= 0.0F) {
            return;
        }

        poseStack.pushPose();
        poseStack.mulPose(Axis.YP.rotationDegrees(-entity.getYRot()));
        poseStack.mulPose(Axis.XP.rotationDegrees(entity.getXRot()));
        poseStack.mulPose(Axis.ZP.rotationDegrees(-12.0F + Mth.clamp(age / 8.0F, 0.0F, 1.0F) * 24.0F));

        Matrix4f matrix = poseStack.last().pose();
        VertexConsumer consumer = bufferSource.getBuffer(RenderType.debugQuads());
        float radius = 0.8F + entity.getRadius() * 0.67F;
        float expand = 0.82F + Mth.clamp(age / 8.0F, 0.0F, 1.0F) * 0.18F;

        renderCrescent(consumer, matrix, radius * expand, 0.25F,
                85, 205, 255, alpha(42.0F * fade));
        renderCrescent(consumer, matrix, radius * expand * 0.86F, 0.17F,
                145, 235, 255, alpha(72.0F * fade));
        renderCrescent(consumer, matrix, radius * expand * 0.70F, 0.10F,
                220, 250, 255, alpha(105.0F * fade));
        renderCrescent(consumer, matrix, radius * expand * 0.94F, 0.08F,
                90, 205, 255, alpha(30.0F * fade), -0.18F);
        renderCrescent(consumer, matrix, radius * expand * 0.86F, 0.06F,
                120, 220, 255, alpha(18.0F * fade), -0.36F);

        poseStack.popPose();
        super.render(entity, entityYaw, partialTick, poseStack, bufferSource, packedLight);
    }

    private static void renderCrescent(VertexConsumer consumer, Matrix4f matrix, float radius,
                                       float thickness, int red, int green, int blue, int alpha) {
        renderCrescent(consumer, matrix, radius, thickness, red, green, blue, alpha, 0.0F);
    }

    private static void renderCrescent(VertexConsumer consumer, Matrix4f matrix, float radius,
                                       float thickness, int red, int green, int blue, int alpha, float z) {
        float inner = Math.max(0.0F, radius - thickness);
        for (int i = 0; i < SEGMENTS; i++) {
            float angle0 = (float) (-Math.PI * 0.72D + Math.PI * 1.44D * i / SEGMENTS);
            float angle1 = (float) (-Math.PI * 0.72D + Math.PI * 1.44D * (i + 1) / SEGMENTS);
            float cos0 = Mth.cos(angle0);
            float sin0 = Mth.sin(angle0);
            float cos1 = Mth.cos(angle1);
            float sin1 = Mth.sin(angle1);

            vertex(consumer, matrix, cos0 * inner, sin0 * inner * 0.38F, z, red, green, blue, alpha);
            vertex(consumer, matrix, cos1 * inner, sin1 * inner * 0.38F, z, red, green, blue, alpha);
            vertex(consumer, matrix, cos1 * radius, sin1 * radius * 0.38F, z, red, green, blue, alpha);
            vertex(consumer, matrix, cos0 * radius, sin0 * radius * 0.38F, z, red, green, blue, alpha);
        }
    }

    private static void vertex(VertexConsumer consumer, Matrix4f matrix, float x, float y, float z,
                               int red, int green, int blue, int alpha) {
        consumer.vertex(matrix, x, y, z).color(red, green, blue, alpha).endVertex();
    }

    private static int alpha(float alpha) {
        return Mth.clamp((int) alpha, 0, 255);
    }

    @Override
    public ResourceLocation getTextureLocation(GrassCuttingSlashEntity entity) {
        return TEXTURE;
    }
}
