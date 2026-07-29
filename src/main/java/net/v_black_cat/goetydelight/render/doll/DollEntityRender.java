package net.v_black_cat.goetydelight.render.doll;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.Model;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.block.BlockRenderDispatcher;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.client.model.data.ModelData;
import net.v_black_cat.goetydelight.bedrock.BedrockModel;
import net.v_black_cat.goetydelight.bedrock.model.BedrockPart;
import net.v_black_cat.goetydelight.entities.DollEntity;
import net.v_black_cat.goetydelight.init.doll.CustomDollLoader;
import net.v_black_cat.goetydelight.init.doll.CustomDollReloadListener;
import org.apache.commons.lang3.StringUtils;
import org.joml.Vector3f;

import javax.annotation.Nullable;

public class DollEntityRender extends EntityRenderer<DollEntity> {
    private static final ResourceLocation EMPTY = ResourceLocation.fromNamespaceAndPath("minecraft", "textures/misc/empty.png");
    private static final int TOUCH_ANIMATION_DURATION = 17;

    public DollEntityRender(EntityRendererProvider.Context context) {
        super(context);
    }

    @Override
    public void render(DollEntity dollEntity, float entityYaw, float partialTick, PoseStack poseStack,
                       MultiBufferSource bufferSource, int packedLight) {
        @Nullable BlockState blockState = dollEntity.getDisplayBlockState();
        String customDollId = dollEntity.getCustomDollId();

        if (StringUtils.isBlank(customDollId) && (blockState == null || blockState.isAir())) {
            super.render(dollEntity, entityYaw, partialTick, poseStack, bufferSource, packedLight);
            return;
        }

        poseStack.pushPose();

        Vector3f translation = dollEntity.getDisplayTranslation();
        poseStack.translate(translation.x, translation.y, translation.z);

        net.minecraft.world.entity.Entity vehicle = dollEntity.getVehicle();
        if (vehicle != null) {
            float vehicleYaw = Mth.lerp(partialTick, vehicle.yRotO, vehicle.getYRot());
            poseStack.mulPose(Axis.YP.rotationDegrees(-vehicleYaw));
        } else {
            entityYaw = Mth.lerp(partialTick, dollEntity.yRotO, dollEntity.getYRot());
            poseStack.mulPose(Axis.YP.rotationDegrees(-entityYaw));
        }

        float pitchRadians = Mth.lerp(partialTick, dollEntity.xRotO, dollEntity.getXRot());
        poseStack.mulPose(Axis.XP.rotationDegrees(pitchRadians));

        Vector3f scale = dollEntity.getDisplayScale();
        poseStack.scale(scale.x, scale.y, scale.z);

        poseStack.translate(-0.5, 0, -0.5);

        if (!StringUtils.isBlank(customDollId)) {
            renderCustom(dollEntity, customDollId, poseStack, bufferSource, packedLight, partialTick);
        } else if (blockState != null && !blockState.isAir()) {
            renderBlock(dollEntity, poseStack, bufferSource, blockState);
        }

        poseStack.popPose();
        super.render(dollEntity, entityYaw, partialTick, poseStack, bufferSource, packedLight);
    }

    private static void renderCustom(DollEntity dollEntity, String modelId, PoseStack poseStack,
                                     MultiBufferSource bufferSource, int packedLight, float partialTick) {
        Model model = CustomDollReloadListener.DFAULT_DOLL_MODEL;
        ResourceLocation texture;

        if (StringUtils.isBlank(modelId)) {
            texture = CustomDollReloadListener.DEFAULT_TEXTURE_ID;
        } else {
            texture = CustomDollLoader.getTexture(modelId);
            if (texture == null) {
                texture = CustomDollReloadListener.DEFAULT_TEXTURE_ID;
            }
        }

        if (model == null || texture == null) {
            return;
        }

        poseStack.translate(0.5, 1.5, 0.5);
        poseStack.mulPose(Axis.ZN.rotationDegrees(180));
        poseStack.mulPose(Axis.YN.rotationDegrees(180));

        boolean hasAnimation = false;
        if (model instanceof BedrockModel bedrockModel) {
            BedrockPart dollPart = bedrockModel.getModelMap().get("doll");
            if (dollPart != null) {
                int touchTick = dollEntity.getTouchAnimationTick();
                if (touchTick > 0) {
                    float animationProgress = 1.0f - ((float)touchTick - partialTick) / (float)TOUCH_ANIMATION_DURATION;
                    applyTouchAnimation(dollPart, animationProgress);
                    hasAnimation = true;
                }
            }
        }

        VertexConsumer buffer = bufferSource.getBuffer(RenderType.entityCutoutNoCull(texture));

        // 1.21 renderToBuffer 使用打包的 ARGB 颜色
        model.renderToBuffer(poseStack, buffer, packedLight, OverlayTexture.NO_OVERLAY, 0xFFFFFFFF);

        if (hasAnimation && model instanceof BedrockModel bedrockModel) {
            BedrockPart dollPart = bedrockModel.getModelMap().get("doll");
            if (dollPart != null) {
                resetTouchAnimation(dollPart);
            }
        }
    }

    private static void applyTouchAnimation(BedrockPart dollPart, float progress) {
        if (progress < 0.0f) progress = 0.0f;
        if (progress > 1.0f) progress = 1.0f;

        float scale = calculateSmoothScale(progress);

        dollPart.xScale = scale;
        dollPart.yScale = 2.0f - scale;
        dollPart.zScale = scale;
    }

    private static float calculateSmoothScale(float t) {
        float bounce = Mth.sin(t * (float)Math.PI * 3.0f) * 0.15f * (1.0f - t);
        return 1.0f + bounce;
    }

    private static void resetTouchAnimation(BedrockPart dollPart) {
        if (dollPart != null) {
            dollPart.xScale = 1.0f;
            dollPart.yScale = 1.0f;
            dollPart.zScale = 1.0f;
        }
    }

    private static void renderBlock(DollEntity dollEntity, PoseStack poseStack,
                                    MultiBufferSource bufferSource, BlockState blockState) {
        BlockRenderDispatcher blockRenderer = Minecraft.getInstance().getBlockRenderer();
        Level level = dollEntity.level();
        VertexConsumer buffer = bufferSource.getBuffer(RenderType.cutout());

        // 1.21 renderBatched 方法签名可能有变化
        blockRenderer.renderBatched(
                blockState,
                dollEntity.blockPosition(),
                level,
                poseStack,
                buffer,
                false,
                level.random,
                ModelData.EMPTY,
                RenderType.cutout()
        );
    }

    @Override
    public ResourceLocation getTextureLocation(DollEntity dollEntity) {
        return EMPTY;
    }
}