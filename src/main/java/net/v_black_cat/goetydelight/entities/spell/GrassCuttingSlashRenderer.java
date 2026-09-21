package net.v_black_cat.goetydelight.entities.spell;

import com.Polarice3.Goety.common.items.ModItems;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import org.joml.Matrix3f;

public class GrassCuttingSlashRenderer extends EntityRenderer<GrassCuttingSlashEntity> {
    private static final Item[] SCYTHES = {
            ModItems.OMINOUS_SCYTHE.get(),
            ModItems.DARK_SCYTHE.get(),
            ModItems.DEATH_SCYTHE.get()
    };

    public GrassCuttingSlashRenderer(EntityRendererProvider.Context context) {
        super(context);
        this.shadowRadius = 0.0F;
    }

    @Override
    public void render(GrassCuttingSlashEntity entity, float entityYaw, float partialTick, PoseStack poseStack,
                       MultiBufferSource bufferSource, int packedLight) {
        float age = entity.animationTime(partialTick);
        int lifeSpan = Math.max(1, entity.getMaxLifeSpan());
        float fade = age <= lifeSpan - GrassCuttingSlashEntity.FADE_TICKS
                ? 1.0F
                : 1.0F - Mth.clamp((age - (lifeSpan - GrassCuttingSlashEntity.FADE_TICKS))
                        / GrassCuttingSlashEntity.FADE_TICKS, 0.0F, 1.0F);
        if (fade <= 0.0F) {
            return;
        }

        int variant = Math.floorMod(entity.getUUID().hashCode(), SCYTHES.length);
        ItemStack stack = new ItemStack(SCYTHES[variant]);
        float scale = 0.65F + entity.getRadius() * 0.35F;
        float spin = (entity.tickCount + partialTick) * 36.0F;

        poseStack.pushPose();
        poseStack.scale(scale, scale, scale);
        poseStack.mulPose(this.entityRenderDispatcher.cameraOrientation());
        poseStack.mulPose(Axis.YP.rotationDegrees(180.0F));
        poseStack.mulPose(Axis.ZP.rotationDegrees(spin));

        Minecraft minecraft = Minecraft.getInstance();
        minecraft.getItemRenderer().renderStatic(stack, ItemDisplayContext.GROUND, LightTexture.FULL_BRIGHT,
                OverlayTexture.NO_OVERLAY, poseStack,
                tintedBuffer(bufferSource, RenderType.entityTranslucentEmissive(TextureAtlas.LOCATION_BLOCKS),
                        0.12F, 1.65F, fade * 0.32F),
                entity.level(), entity.getId());
        poseStack.popPose();

        super.render(entity, entityYaw, partialTick, poseStack, bufferSource, packedLight);
    }

    private static MultiBufferSource tintedBuffer(MultiBufferSource delegate, RenderType renderType,
                                                  float blue, float colorIntensity, float opacity) {
        return ignored -> new BlueTintVertexConsumer(delegate.getBuffer(renderType), blue, colorIntensity, opacity);
    }

    private static int alpha(float alpha) {
        return Mth.clamp((int) (alpha * 255.0F), 0, 255);
    }

    private static int channel(float value) {
        return Mth.clamp((int) (value * 255.0F), 0, 255);
    }

    @Override
    public ResourceLocation getTextureLocation(GrassCuttingSlashEntity entity) {
        return TextureAtlas.LOCATION_BLOCKS;
    }

    private static final class BlueTintVertexConsumer implements VertexConsumer {
        private final VertexConsumer delegate;
        private final float blueMix;
        private final float colorIntensity;
        private final int opacity;

        private BlueTintVertexConsumer(VertexConsumer delegate, float blueMix,
                                       float colorIntensity, float opacity) {
            this.delegate = delegate;
            this.blueMix = blueMix;
            this.colorIntensity = colorIntensity;
            this.opacity = alpha(opacity);
        }

        @Override
        public VertexConsumer vertex(double x, double y, double z) {
            this.delegate.vertex(x, y, z);
            return this;
        }

        @Override
        public VertexConsumer color(int red, int green, int blue, int alpha) {
            this.delegate.color(
                    channel(red / 255.0F * (1.0F - this.blueMix) * this.colorIntensity),
                    channel(green / 255.0F * (1.0F - this.blueMix * 0.65F) * this.colorIntensity),
                    channel(blue / 255.0F * (1.0F + this.blueMix) * this.colorIntensity),
                    this.opacity);
            return this;
        }

        @Override
        public VertexConsumer uv(float u, float v) {
            this.delegate.uv(u, v);
            return this;
        }

        @Override
        public VertexConsumer overlayCoords(int u, int v) {
            this.delegate.overlayCoords(u, v);
            return this;
        }

        @Override
        public VertexConsumer uv2(int u, int v) {
            this.delegate.uv2(u, v);
            return this;
        }

        @Override
        public VertexConsumer normal(float x, float y, float z) {
            this.delegate.normal(x, y, z);
            return this;
        }

        @Override
        public VertexConsumer normal(Matrix3f matrix, float x, float y, float z) {
            this.delegate.normal(matrix, x, y, z);
            return this;
        }

        @Override
        public void endVertex() {
            this.delegate.endVertex();
        }

        @Override
        public void defaultColor(int red, int green, int blue, int alpha) {
            this.delegate.defaultColor(0, 0, 0, this.opacity);
        }

        @Override
        public void unsetDefaultColor() {
            this.delegate.unsetDefaultColor();
        }

        @Override
        public void putBulkData(PoseStack.Pose pose, BakedQuad quad, float[] colorMuls,
                                float red, float green, float blue, float alpha,
                                int[] lightmap, int overlay, boolean readExistingColor) {
            float tintedRed = Mth.clamp(red * (1.0F - this.blueMix) * this.colorIntensity, 0.0F, 1.0F);
            float tintedGreen = Mth.clamp(green * (1.0F - this.blueMix * 0.65F) * this.colorIntensity, 0.0F, 1.0F);
            float tintedBlue = Mth.clamp(blue * (1.0F + this.blueMix) * this.colorIntensity, 0.0F, 1.0F);
            this.delegate.putBulkData(pose, quad, colorMuls,
                    tintedRed, tintedGreen, tintedBlue, this.opacity / 255.0F,
                    lightmap, overlay, readExistingColor);
        }
    }
}