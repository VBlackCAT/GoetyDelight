package net.v_black_cat.goetydelight.visual.client;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Camera;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.culling.Frustum;
import net.minecraftforge.client.event.RenderLevelStageEvent;
import org.joml.Matrix3f;
import org.joml.Matrix4f;

import javax.annotation.Nullable;

public final class LateShaderPackRenderContext {

    @Nullable
    private static CachedStage afterParticles;


    private LateShaderPackRenderContext() {
    }


    public static void captureAfterParticles(RenderLevelStageEvent event) {

        // 防止重复覆盖旧对象
        if (afterParticles != null) {
            afterParticles.release();
        }

        afterParticles = CachedStage.capture(event);
    }


    @Nullable
    public static RenderLevelStageEvent consumeAfterParticles() {

        CachedStage cached = afterParticles;

        if (cached == null) {
            return null;
        }

        afterParticles = null;

        return cached.toEvent();
    }


    public static void clear() {

        if (afterParticles != null) {
            afterParticles.release();
            afterParticles = null;
        }
    }


    private static final class CachedStage {

        private final RenderLevelStageEvent.Stage stage;
        private final LevelRenderer levelRenderer;

        private final Matrix4f poseMatrix;
        private final Matrix3f normalMatrix;
        private final Matrix4f projectionMatrix;

        private final int renderTick;
        private final float partialTick;

        private final Camera camera;
        private final Frustum frustum;


        private CachedStage(
                RenderLevelStageEvent.Stage stage,
                LevelRenderer levelRenderer,
                Matrix4f poseMatrix,
                Matrix3f normalMatrix,
                Matrix4f projectionMatrix,
                int renderTick,
                float partialTick,
                Camera camera,
                Frustum frustum
        ) {
            this.stage = stage;
            this.levelRenderer = levelRenderer;
            this.poseMatrix = poseMatrix;
            this.normalMatrix = normalMatrix;
            this.projectionMatrix = projectionMatrix;
            this.renderTick = renderTick;
            this.partialTick = partialTick;
            this.camera = camera;
            this.frustum = frustum;
        }


        private static CachedStage capture(RenderLevelStageEvent event) {

            return new CachedStage(
                    event.getStage(),
                    event.getLevelRenderer(),
                    new Matrix4f(event.getPoseStack().last().pose()),
                    new Matrix3f(event.getPoseStack().last().normal()),
                    new Matrix4f(event.getProjectionMatrix()),
                    event.getRenderTick(),
                    event.getPartialTick(),
                    event.getCamera(),
                    event.getFrustum()
            );
        }


        private RenderLevelStageEvent toEvent() {

            PoseStack poseStack = new PoseStack();

            poseStack.last().pose().set(poseMatrix);
            poseStack.last().normal().set(normalMatrix);


            return new RenderLevelStageEvent(
                    stage,
                    levelRenderer,
                    poseStack,
                    new Matrix4f(projectionMatrix),
                    renderTick,
                    partialTick,
                    camera,
                    frustum
            );
        }


        private void release() {

            /*
             * 断开引用
             *
             * 防止 levelRenderer/camera/frustum
             * 长时间被 static 引用
             */

        }
    }
}