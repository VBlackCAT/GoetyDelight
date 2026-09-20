package net.v_black_cat.goetydelight.entities.spell;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import org.joml.Matrix4f;

public class RichSoilSpellRenderer<T extends RichSoilSpellEntity> extends EntityRenderer<T> {
    private static final ResourceLocation TEXTURE =
            new ResourceLocation("minecraft", "textures/misc/white.png");
    private static final int SIDES = 32;

    public RichSoilSpellRenderer(EntityRendererProvider.Context context) {
        super(context);
        this.shadowRadius = 0.0F;
    }

    @Override
    public void render(T entity, float entityYaw, float partialTick, PoseStack poseStack,
                       MultiBufferSource bufferSource, int packedLight) {
        float age = entity.animationTime(partialTick);
        float progress = Mth.clamp(age / RichSoilSpellEntity.WINDUP_TICKS, 0.0F, 1.0F);
        boolean impacted = age >= RichSoilSpellEntity.WINDUP_TICKS;
        float impactAge = Math.max(0.0F, age - RichSoilSpellEntity.WINDUP_TICKS);
        float impactFade = impacted
                ? 1.0F - Mth.clamp(impactAge / RichSoilSpellEntity.IMPACT_TICKS, 0.0F, 1.0F)
                : 1.0F;

        if (impacted && impactFade <= 0.0F) {
            return;
        }

        Matrix4f matrix = poseStack.last().pose();
        VertexConsumer consumer = bufferSource.getBuffer(RenderType.debugQuads());
        float radius = entity.getRadius() + 0.45F;
        float groundY = 0.035F;

        renderGroundRing(consumer, matrix, radius, groundY,
                55, 155, 255, alpha(impacted ? 30.0F * impactFade : 24.0F + progress * 24.0F));

        if (impacted) {
            renderGroundRing(consumer, matrix, radius * (0.35F + impactAge * 0.09F), groundY + 0.006F,
                    105, 210, 255, alpha(105.0F * impactFade));
            renderGroundRing(consumer, matrix, radius, groundY + 0.012F,
                    125, 225, 255, alpha(80.0F * impactFade));
            return;
        }

        float eased = progress * progress * (3.0F - 2.0F * progress);
        float beamBottom = (1.0F - eased) * 11.0F;
        float beamHeight = 10.0F;
        float beamRadius = 0.42F + entity.getRadius() * 0.12F;
        float pulse = 0.86F + Mth.sin(age * 0.62F) * 0.14F;

        renderCylinder(consumer, matrix, beamRadius, beamBottom, beamHeight,
                45, 135, 255, alpha((34.0F + 18.0F * progress) * pulse));
        renderCylinder(consumer, matrix, beamRadius * 0.58F, beamBottom + 0.15F, beamHeight - 0.3F,
                115, 215, 255, alpha((58.0F + 24.0F * progress) * pulse));
        renderCylinder(consumer, matrix, beamRadius * 0.20F, beamBottom + 0.24F, beamHeight - 0.48F,
                205, 245, 255, alpha(92.0F * pulse));

        for (int ring = 0; ring < 5; ring++) {
            float ringProgress = (age * 0.075F + ring * 0.2F) % 1.0F;
            float ringY = beamBottom + beamHeight * (1.0F - ringProgress);
            float ringRadius = beamRadius * (1.15F + 0.2F * ringProgress);
            renderGroundRing(consumer, matrix, ringRadius, ringY,
                    145, 225, 255, alpha(38.0F * (1.0F - ringProgress)));
        }

        super.render(entity, entityYaw, partialTick, poseStack, bufferSource, packedLight);
    }

    private static void renderCylinder(VertexConsumer consumer, Matrix4f matrix, float radius,
                                       float bottom, float height, int red, int green, int blue, int alpha) {
        float top = bottom + height;
        for (int i = 0; i < SIDES; i++) {
            float angle0 = (float) (Math.PI * 2.0D * i / SIDES);
            float angle1 = (float) (Math.PI * 2.0D * (i + 1) / SIDES);
            float x0 = Mth.cos(angle0) * radius;
            float z0 = Mth.sin(angle0) * radius;
            float x1 = Mth.cos(angle1) * radius;
            float z1 = Mth.sin(angle1) * radius;

            vertex(consumer, matrix, x0, bottom, z0, red, green, blue, alpha);
            vertex(consumer, matrix, x1, bottom, z1, red, green, blue, alpha);
            vertex(consumer, matrix, x1, top, z1, red, green, blue, alpha);
            vertex(consumer, matrix, x0, top, z0, red, green, blue, alpha);
        }
    }

    private static void renderGroundRing(VertexConsumer consumer, Matrix4f matrix, float radius, float y,
                                         int red, int green, int blue, int alpha) {
        float width = Math.max(0.055F, radius * 0.075F);
        float inner = Math.max(0.0F, radius - width);
        float outer = radius + width;
        for (int i = 0; i < SIDES; i++) {
            float angle0 = (float) (Math.PI * 2.0D * i / SIDES);
            float angle1 = (float) (Math.PI * 2.0D * (i + 1) / SIDES);
            float cos0 = Mth.cos(angle0);
            float sin0 = Mth.sin(angle0);
            float cos1 = Mth.cos(angle1);
            float sin1 = Mth.sin(angle1);

            vertex(consumer, matrix, cos0 * inner, y, sin0 * inner, red, green, blue, alpha);
            vertex(consumer, matrix, cos1 * inner, y, sin1 * inner, red, green, blue, alpha);
            vertex(consumer, matrix, cos1 * outer, y, sin1 * outer, red, green, blue, alpha);
            vertex(consumer, matrix, cos0 * outer, y, sin0 * outer, red, green, blue, alpha);
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
    public ResourceLocation getTextureLocation(T entity) {
        return TEXTURE;
    }
}
