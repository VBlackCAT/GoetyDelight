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
        afterParticles = CachedStage.capture(event);
    }

    @Nullable
    public static RenderLevelStageEvent afterParticlesEvent() {
        return afterParticles != null ? afterParticles.toEvent() : null;
    }

    public static void clear() {
        afterParticles = null;
    }

    private record CachedStage(RenderLevelStageEvent.Stage stage, LevelRenderer levelRenderer, Matrix4f poseMatrix,
                               Matrix3f normalMatrix, Matrix4f projectionMatrix, int renderTick, float partialTick,
                               Camera camera, Frustum frustum) {
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
            return new RenderLevelStageEvent(stage, levelRenderer, poseStack, new Matrix4f(projectionMatrix), renderTick, partialTick, camera, frustum);
        }
    }
}
