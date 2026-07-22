package net.v_black_cat.goetydelight.visual.client;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Camera;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.culling.Frustum;
import net.neoforged.neoforge.client.event.RenderLevelStageEvent;
import org.joml.Matrix3f;
import org.joml.Matrix4f;

import javax.annotation.Nullable;

public final class LateShaderPackRenderContext {

    @Nullable
    private static CachedStage afterParticles;


    private LateShaderPackRenderContext() {
    }


    public static void captureAfterParticles(RenderLevelStageEvent event) {

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
        private final Matrix4f modelViewMatrix;

        private final int renderTick;
        private final DeltaTracker partialTick;

        private final Camera camera;
        private final Frustum frustum;


        private CachedStage(
                RenderLevelStageEvent.Stage stage,
                LevelRenderer levelRenderer,
                Matrix4f poseMatrix,
                Matrix3f normalMatrix,
                Matrix4f modelViewMatrix,
                Matrix4f projectionMatrix,
                int renderTick,
                DeltaTracker partialTick,
                Camera camera,
                Frustum frustum
        ) {
            this.stage = stage;
            this.levelRenderer = levelRenderer;
            this.poseMatrix = poseMatrix;
            this.normalMatrix = normalMatrix;
            this.modelViewMatrix = modelViewMatrix;
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
                    new Matrix4f(event.getModelViewMatrix()),
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
                    new Matrix4f(modelViewMatrix),
                    new Matrix4f(projectionMatrix),
                    renderTick,
                    partialTick,
                    camera,
                    frustum
            );
        }


        private void release() {

            /*
        滚木1号
             */

        }
    }
}
