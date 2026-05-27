package net.v_black_cat.goetydelight.render;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.BufferUploader;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.blaze3d.vertex.VertexFormat;
import com.mojang.blaze3d.vertex.VertexSorting;
import net.minecraft.client.Camera;
import net.minecraft.client.renderer.ShaderInstance;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.client.event.RenderLevelStageEvent;
import net.v_black_cat.goetydelight.render.test.ModShaderReg;
import net.v_black_cat.goetydelight.visual.ActiveEntityVisualEffect;
import org.joml.Matrix4f;
import org.joml.Vector3f;

public final class RedEyeFlashRenderer {
    private static final double TRACK_DISTANCE = 80.0D;

    private RedEyeFlashRenderer() {
    }

    public static void render(RenderLevelStageEvent event, Entity entity, ActiveEntityVisualEffect effect) {
        float partialTick = event.getPartialTick();
        float renderTime = entity.level().getGameTime() + partialTick;
        Camera camera = event.getCamera();
        Vec3 cameraPos = camera.getPosition();

        double distanceSqr = entity.distanceToSqr(cameraPos);
        if (distanceSqr > TRACK_DISTANCE * TRACK_DISTANCE) {
            return;
        }

        Vec3 faceForward = faceLook(entity, partialTick);
        Vec3 horizontalForward = horizontalLook(entity, partialTick);
        Vec3 right = new Vec3(horizontalForward.z, 0.0D, -horizontalForward.x);
        Vec3 eyeCenter = entity.getEyePosition(partialTick)
                .add(0.0D, eyeYOffset(entity, effect), 0.0D)
                .add(faceForward.scale(forwardOffset(entity, effect)))
                .add(right.scale(sideOffset(entity, effect)));

        Vector3f cameraLeftVector = camera.getLeftVector();
        Vector3f cameraUpVector = camera.getUpVector();
        Vec3 cameraLeft = new Vec3(cameraLeftVector.x(), cameraLeftVector.y(), cameraLeftVector.z());
        Vec3 cameraUp = new Vec3(cameraUpVector.x(), cameraUpVector.y(), cameraUpVector.z());
        Vec3 streakAxis = cameraLeft.scale(-0.98D).add(cameraUp.scale(0.24D)).normalize();
        Vec3 streakUp = cameraUp.scale(0.98D).add(cameraLeft.scale(0.24D)).normalize();
        Matrix4f matrix = event.getPoseStack().last().pose();
        float scale = scale(entity, effect);
        float intensity = intensity(effect);
        float distanceFade = distanceFade(Math.sqrt(distanceSqr));
        int alpha = (int) (distanceFade * 230.0F);

        ShaderInstance shader = ModShaderReg.getRedEyeFlashShader();
        shader.safeGetUniform("iTime").set(renderTime / 20.0F);
        shader.safeGetUniform("intensity").set(intensity);

        Matrix4f oldProjection = new Matrix4f(RenderSystem.getProjectionMatrix());
        VertexSorting oldSorting = RenderSystem.getVertexSorting();
        RenderSystem.setProjectionMatrix(event.getProjectionMatrix(), VertexSorting.DISTANCE_TO_ORIGIN);

        RenderSystem.setShader(() -> shader);
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        RenderSystem.enableDepthTest();
        RenderSystem.depthMask(false);
        RenderSystem.enableBlend();
        RenderSystem.blendFuncSeparate(770, 1, 1, 1);
        RenderSystem.disableCull();

        shader.safeGetUniform("EffectMode").set(2);
        drawBillboard(matrix, cameraPos, cameraLeft, cameraUp, eyeCenter, scale * 1.72F, scale * 1.22F, 0xFF2600, alpha / 2);

        shader.safeGetUniform("EffectMode").set(4);
        drawLightning(matrix, cameraPos, streakAxis, streakUp, eyeCenter, renderTime, scale, alpha);

        shader.safeGetUniform("EffectMode").set(1);
        drawBillboard(matrix, cameraPos, streakAxis, streakUp, eyeCenter, scale * 4.90F, scale * 0.32F, 0xFF1200, alpha);

        shader.safeGetUniform("EffectMode").set(0);
        drawBillboard(matrix, cameraPos, cameraLeft, cameraUp, eyeCenter, scale * 0.62F, scale * 0.44F, 0xFF1C00, Math.min(255, alpha + 20));

        shader.safeGetUniform("EffectMode").set(3);
        drawGlints(matrix, cameraPos, streakAxis, streakUp, eyeCenter, renderTime, scale, alpha);

        RenderSystem.enableCull();
        RenderSystem.disableBlend();
        RenderSystem.depthMask(true);
        RenderSystem.setProjectionMatrix(oldProjection, oldSorting);
    }

    private static Vec3 faceLook(Entity entity, float partialTick) {
        Vec3 look = entity.getViewVector(partialTick);
        if (look.lengthSqr() < 1.0E-5D) {
            return horizontalLook(entity, partialTick);
        }
        return look.normalize();
    }

    private static Vec3 horizontalLook(Entity entity, float partialTick) {
        Vec3 look = entity.getViewVector(partialTick);
        Vec3 horizontal = new Vec3(look.x, 0.0D, look.z);
        if (horizontal.lengthSqr() < 1.0E-5D) {
            float yaw = entity.getViewYRot(partialTick) * ((float) Math.PI / 180.0F);
            horizontal = new Vec3(-Mth.sin(yaw), 0.0D, Mth.cos(yaw));
        }
        return horizontal.normalize();
    }

    private static void drawGlints(Matrix4f matrix, Vec3 cameraPos, Vec3 cameraLeft, Vec3 cameraUp, Vec3 center, float renderTime, float scale, int alpha) {
        for (int i = 0; i < 5; i++) {
            float side = i - 2.0F;
            float wave = Mth.sin(renderTime * 0.37F + i * 1.91F) * 0.18F;
            Vec3 glintCenter = center.add(cameraLeft.scale((side * 0.48F + wave) * scale));
            int glintAlpha = (int) (alpha * (0.42F - Math.abs(side) * 0.055F));
            drawBillboard(matrix, cameraPos, cameraLeft, cameraUp, glintCenter, scale * 0.20F, scale * 0.12F, 0xFF3A08, glintAlpha);
        }
    }

    private static void drawLightning(Matrix4f matrix, Vec3 cameraPos, Vec3 axis, Vec3 up, Vec3 center, float renderTime, float scale, int alpha) {
        BufferBuilder buffer = Tesselator.getInstance().getBuilder();
        buffer.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_COLOR_TEX);

        int segments = 10;
        Vec3 previous = center.add(axis.scale(-0.06D * scale));
        float previousWidth = scale * 0.090F;
        for (int i = 1; i <= segments; i++) {
            float t = i / (float) segments;
            float previousT = (i - 1) / (float) segments;
            double bend = (Mth.sin(renderTime * 0.23F + i * 1.77F) * 0.20D
                    + Mth.sin(renderTime * 0.53F + i * 2.91F) * 0.10D) * scale * (0.30D + t);
            Vec3 next = center
                    .add(up.scale((0.10D + t * 2.28D) * scale))
                    .add(axis.scale(bend));
            float width = scale * Mth.lerp(t, 0.090F, 0.030F);
            int segmentAlpha = (int) (alpha * (1.0F - t * 0.54F));
            addLightningSegment(buffer, matrix, cameraPos, axis, up, previous, next, previousWidth, width, previousT, t, segmentAlpha);
            previous = next;
            previousWidth = width;
        }

        Vec3 branchStart = center.add(up.scale(0.86D * scale)).add(axis.scale(0.12D * scale));
        Vec3 branchMid = branchStart.add(up.scale(0.32D * scale)).add(axis.scale(-0.30D * scale));
        Vec3 branchEnd = branchMid.add(up.scale(0.34D * scale)).add(axis.scale(0.16D * scale));
        addLightningSegment(buffer, matrix, cameraPos, axis, up, branchStart, branchMid, scale * 0.040F, scale * 0.025F, 0.0F, 0.5F, alpha / 2);
        addLightningSegment(buffer, matrix, cameraPos, axis, up, branchMid, branchEnd, scale * 0.025F, scale * 0.012F, 0.5F, 1.0F, alpha / 3);

        BufferUploader.drawWithShader(buffer.end());
    }

    private static void addLightningSegment(BufferBuilder buffer, Matrix4f matrix, Vec3 cameraPos, Vec3 axis, Vec3 up, Vec3 start, Vec3 end, float startWidth, float endWidth, float startU, float endU, int alpha) {
        Vec3 tangent = end.subtract(start);
        Vec3 viewNormal = axis.cross(up);
        Vec3 side = viewNormal.cross(tangent);
        if (side.lengthSqr() < 1.0E-5D) {
            side = axis;
        } else {
            side = side.normalize();
        }

        Vec3 startSide = side.scale(startWidth);
        Vec3 endSide = side.scale(endWidth);
        int startAlpha = Mth.clamp(alpha, 0, 255);
        int endAlpha = Mth.clamp((int) (alpha * 0.82F), 0, 255);
        int color = 0xFF1200;

        putVertex(buffer, matrix, start.subtract(startSide).subtract(cameraPos), color, startAlpha, startU, 0.0F);
        putVertex(buffer, matrix, start.add(startSide).subtract(cameraPos), color, startAlpha, startU, 1.0F);
        putVertex(buffer, matrix, end.add(endSide).subtract(cameraPos), color, endAlpha, endU, 1.0F);
        putVertex(buffer, matrix, end.subtract(endSide).subtract(cameraPos), color, endAlpha, endU, 0.0F);
    }

    private static void drawBillboard(Matrix4f matrix, Vec3 cameraPos, Vec3 xAxis, Vec3 yAxis, Vec3 center, float halfWidth, float halfHeight, int color, int alpha) {
        Vec3 relative = center.subtract(cameraPos);
        Vec3 dx = xAxis.scale(halfWidth);
        Vec3 dy = yAxis.scale(halfHeight);
        BufferBuilder buffer = Tesselator.getInstance().getBuilder();
        buffer.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_COLOR_TEX);

        putVertex(buffer, matrix, relative.subtract(dx).subtract(dy), color, alpha, 0.0F, 1.0F);
        putVertex(buffer, matrix, relative.add(dx).subtract(dy), color, alpha, 1.0F, 1.0F);
        putVertex(buffer, matrix, relative.add(dx).add(dy), color, alpha, 1.0F, 0.0F);
        putVertex(buffer, matrix, relative.subtract(dx).add(dy), color, alpha, 0.0F, 0.0F);

        BufferUploader.drawWithShader(buffer.end());
    }

    private static void putVertex(BufferBuilder buffer, Matrix4f matrix, Vec3 pos, int color, int alpha, float u, float v) {
        buffer.vertex(matrix, (float) pos.x, (float) pos.y, (float) pos.z)
                .color((color >> 16) & 255, (color >> 8) & 255, color & 255, Mth.clamp(alpha, 0, 255))
                .uv(u, v)
                .endVertex();
    }

    private static float scale(Entity entity, ActiveEntityVisualEffect effect) {
        float base = effect.data().contains("Scale") ? effect.data().getFloat("Scale") : 1.0F;
        float entityScale = (float) Mth.clamp(0.30D + entity.getBbWidth() * 0.36D, 0.34D, 1.40D);
        return Mth.clamp(base, 0.20F, 6.0F) * entityScale;
    }

    private static float intensity(ActiveEntityVisualEffect effect) {
        return effect.data().contains("Intensity") ? Mth.clamp(effect.data().getFloat("Intensity"), 0.05F, 8.0F) : 1.30F;
    }

    private static double forwardOffset(Entity entity, ActiveEntityVisualEffect effect) {
        if (effect.data().contains("ForwardOffset")) {
            return Mth.clamp(effect.data().getDouble("ForwardOffset"), -2.0D, 2.0D);
        }
        return Mth.clamp(0.12D + entity.getBbWidth() * 0.58D, 0.34D, 1.25D);
    }

    private static double sideOffset(Entity entity, ActiveEntityVisualEffect effect) {
        if (effect.data().contains("SideOffset")) {
            return Mth.clamp(effect.data().getDouble("SideOffset"), -entity.getBbWidth(), entity.getBbWidth());
        }
        return -0.1D;
    }

    private static double eyeYOffset(Entity entity, ActiveEntityVisualEffect effect) {
        if (effect.data().contains("EyeYOffset")) {
            return Mth.clamp(effect.data().getDouble("EyeYOffset"), -entity.getBbHeight(), entity.getBbHeight());
        }
        return -entity.getBbHeight() * 0.005D;
    }

    private static float distanceFade(double distance) {
        float fade = 1.0F - smoothstep((float) (TRACK_DISTANCE * 0.72D), (float) TRACK_DISTANCE, (float) distance);
        return Mth.clamp(fade, 0.0F, 1.0F);
    }

    private static float smoothstep(float edge0, float edge1, float value) {
        float x = Mth.clamp((value - edge0) / (edge1 - edge0), 0.0F, 1.0F);
        return x * x * (3.0F - 2.0F * x);
    }
}
