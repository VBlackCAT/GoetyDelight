package net.v_black_cat.goetydelight.render;

import com.mojang.blaze3d.vertex.*;
import com.mojang.math.Axis;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.Model;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.block.BlockRenderDispatcher;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.texture.MissingTextureAtlasSprite;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.client.model.data.ModelData;
import net.v_black_cat.goetydelight.entities.DollEntity;
import net.v_black_cat.goetydelight.entities.ModAnimationDefinitions;
import net.v_black_cat.goetydelight.init.CustomDollLoader;
import net.v_black_cat.goetydelight.init.CustomDollReloadListener;
import org.apache.commons.lang3.StringUtils;
import org.joml.Vector3f;

import javax.annotation.Nullable;

public class DollEntityRender extends EntityRenderer<DollEntity> {
    private static final ResourceLocation EMPTY = new ResourceLocation("minecraft", "textures/misc/empty.png");

    public DollEntityRender(EntityRendererProvider.Context context) {
        super(context);
    }

    @Override
    public void render(DollEntity dollEntity, float entityYaw, float partialTick, PoseStack poseStack, MultiBufferSource bufferSource, int packedLight) {
        @Nullable BlockState blockState = dollEntity.getDisplayBlockState();
        String customDollId = dollEntity.getCustomDollId();
        if (StringUtils.isBlank(customDollId) && (blockState == null || blockState.isAir())) {
            super.render(dollEntity, entityYaw, partialTick, poseStack, bufferSource, packedLight);
            return;
        }
        poseStack.pushPose();

        // 应用位移变换
        Vector3f translation = dollEntity.getDisplayTranslation();
        poseStack.translate(translation.x, translation.y, translation.z);

        // 应用 Y 轴旋转（基于实体的 yaw）
        Entity vehicle = dollEntity.getVehicle();
        if (vehicle != null) {
            float vehicleYaw = Mth.lerp(partialTick, vehicle.yRotO, vehicle.getYRot());
            poseStack.mulPose(Axis.YP.rotationDegrees(-vehicleYaw));
        } else {
            entityYaw = Mth.lerp(partialTick, dollEntity.yRotO, dollEntity.getYRot());
            poseStack.mulPose(Axis.YP.rotationDegrees(-entityYaw));
        }
        float pitchRadians = Mth.lerp(partialTick, dollEntity.xRotO, dollEntity.getXRot());
        poseStack.mulPose(Axis.XP.rotationDegrees(pitchRadians));

        // 应用缩放变换
        Vector3f scale = dollEntity.getDisplayScale();
        poseStack.scale(scale.x, scale.y, scale.z);

        // 将方块中心对齐到实体位置
        poseStack.translate(-0.5, 0, -0.5);

        // 渲染逻辑
        if (!StringUtils.isBlank(customDollId)) {
            renderCustom(dollEntity, customDollId, poseStack, bufferSource, packedLight, partialTick);
        } else if (blockState != null && !blockState.isAir()) {
            renderBlock(dollEntity, poseStack, bufferSource, blockState);
        }

        poseStack.popPose();
        super.render(dollEntity, entityYaw, partialTick, poseStack, bufferSource, packedLight);
    }

    private static void renderCustom(DollEntity dollEntity, String modelId, PoseStack poseStack, MultiBufferSource bufferSource, int packedLight, float partialTick) {
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

        poseStack.translate(0.5, 1.5, 0.5);
        poseStack.mulPose(Axis.ZN.rotationDegrees(180));
        poseStack.mulPose(Axis.YN.rotationDegrees(180));

        int touchTick = dollEntity.getTouchAnimationTick();
        boolean hasAnimation = false;
        if (touchTick > 0) {
            float animationProgress = 1.0f - ((float)touchTick - partialTick) / 17f;
            applyTouchAnimation(model, animationProgress);
            hasAnimation = true;
        }

        VertexConsumer buffer = bufferSource.getBuffer(RenderType.entityCutoutNoCull(texture));
        model.renderToBuffer(poseStack, buffer, packedLight, OverlayTexture.NO_OVERLAY, 1.0F, 1.0F, 1.0F, 1.0F);

        if (hasAnimation) {
            resetTouchAnimation(model);
        }
    }

    private static void applyTouchAnimation(Model model, float progress) {
        if (progress < 0 || progress > 1) {
            return;
        }
        if (!(model instanceof net.v_black_cat.goetydelight.bedrock.BedrockModel bedrockModel)) {
            return;
        }
        var dollBlock = bedrockModel.getModelMap().get("doll");
        if (dollBlock == null) {
            return;
        }
        float scale;
        if (progress < 0.1f) {
            scale = 1.0f + (0.15f * (progress / 0.1f));
        } else if (progress < 0.25f) {
            float t = (progress - 0.1f) / 0.15f;
            scale = 1.15f - (0.14f * t);
        } else if (progress < 0.4f) {
            float t = (progress - 0.25f) / 0.15f;
            scale = 1.01f + (0.02f * t);
        } else if (progress < 0.55f) {
            float t = (progress - 0.4f) / 0.15f;
            scale = 1.03f - (0.03f * t);
        } else if (progress < 0.7f) {
            float t = (progress - 0.55f) / 0.15f;
            scale = 1.0f + (0.01f * t);
        } else {
            float t = (progress - 0.7f) / 0.3f;
            scale = 1.01f - (0.01f * t);
        }

        dollBlock.xScale = scale;
        dollBlock.yScale = 2.0f - scale;
        dollBlock.zScale = scale;
    }

    private static void resetTouchAnimation(Model model) {
        if (!(model instanceof net.v_black_cat.goetydelight.bedrock.BedrockModel bedrockModel)) {
            return;
        }
        var dollBlock = bedrockModel.getModelMap().get("doll");
        if (dollBlock == null) {
            return;
        }
        dollBlock.xScale = 1.0f;
        dollBlock.yScale = 1.0f;
        dollBlock.zScale = 1.0f;
    }

    private static void renderBlock(DollEntity dollEntity, PoseStack poseStack, MultiBufferSource bufferSource, BlockState blockState) {
        BlockRenderDispatcher blockRenderer = Minecraft.getInstance().getBlockRenderer();
        Level level = dollEntity.level();
        VertexConsumer buffer = bufferSource.getBuffer(RenderType.cutout());
        blockRenderer.renderBatched(blockState, dollEntity.blockPosition(), level, poseStack, buffer, false, level.random, ModelData.EMPTY, RenderType.cutout());
    }

    @Override
    public ResourceLocation getTextureLocation(DollEntity dollEntity) {
        return EMPTY;
    }
}


