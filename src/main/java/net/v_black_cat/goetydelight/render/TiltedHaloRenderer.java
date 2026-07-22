package net.v_black_cat.goetydelight.render;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.*;
import net.minecraft.client.Camera;
import net.minecraft.client.renderer.ShaderInstance;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.client.event.RenderLevelStageEvent;
import net.v_black_cat.goetydelight.render.test.ModShaderReg;
import net.v_black_cat.goetydelight.visual.ActiveEntityVisualEffect;
import org.joml.Matrix4f;

public final class TiltedHaloRenderer {
    private static final double TRACK_DISTANCE = 88.0D;
    private static final Vec3 WORLD_UP = new Vec3(0.0D, 1.0D, 0.0D);

    private TiltedHaloRenderer() {
    }

    public static void render(RenderLevelStageEvent event, Entity entity, ActiveEntityVisualEffect effect) {
        float partialTick = event.getPartialTick().getGameTimeDeltaPartialTick(true);
        float renderTime = entity.level().getGameTime() + partialTick;
        Camera camera = event.getCamera();
        Vec3 cameraPos = camera.getPosition();

        double distanceSqr = entity.distanceToSqr(cameraPos);
        if (distanceSqr > TRACK_DISTANCE * TRACK_DISTANCE) {
            return;
        }

        Vec3 forward = faceLook(entity, partialTick);
        Vec3 right = headRight(entity, forward, partialTick);
        Vec3 headUp = forward.cross(right).normalize();
        double tiltRadians = tiltDegrees(effect) * Math.PI / 180.0D;
        Vec3 haloX = right;
        Vec3 haloY = headUp.scale(Math.cos(tiltRadians)).add(forward.scale(Math.sin(tiltRadians))).normalize();
        Vec3 center = entity.getEyePosition(partialTick)
                .add(headUp.scale(yOffset(entity, effect)))
                .subtract(forward.scale(behindOffset(entity, effect)));

        float scale = scale(entity, effect);
        float intensity = intensity(effect);
        float distanceFade = distanceFade(Math.sqrt(distanceSqr));
        int alpha = (int) (distanceFade * 230.0F);

        ShaderInstance shader = ModShaderReg.getTiltedHaloShader();
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

        Matrix4f matrix = event.getPoseStack().last().pose();
        shader.safeGetUniform("EffectMode").set(1);
        drawPlane(matrix, cameraPos, haloX, haloY, center, scale * 1.24F, scale * 1.24F, 0xFFE9A6, alpha / 2);

        shader.safeGetUniform("EffectMode").set(0);
        drawPlane(matrix, cameraPos, haloX, haloY, center, scale, scale, 0xFFECCC, alpha);

        shader.safeGetUniform("EffectMode").set(2);
        drawOrbitingMarks(matrix, cameraPos, haloX, haloY, center, renderTime, scale, alpha);

        RenderSystem.enableCull();
        RenderSystem.disableBlend();
        RenderSystem.depthMask(true);
        RenderSystem.setProjectionMatrix(oldProjection, oldSorting);
    }

    private static void drawOrbitingMarks(Matrix4f matrix, Vec3 cameraPos, Vec3 haloX, Vec3 haloY, Vec3 center, float renderTime, float scale, int alpha) {
        BufferBuilder buffer = Tesselator.getInstance().begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_TEX_COLOR);

        for (int i = 0; i < 12; i++) {
            float baseAngle = (float) (i * Math.PI * 2.0D / 12.0D);
            float angle = baseAngle + renderTime * 0.018F * (i % 2 == 0 ? 1.0F : -0.65F);
            float pulse = 0.72F + 0.28F * Mth.sin(renderTime * 0.16F + i * 1.73F);
            double radius = scale * (0.72D + 0.055D * Mth.sin(renderTime * 0.09F + i));
            Vec3 radial = haloX.scale(Mth.cos(angle)).add(haloY.scale(Mth.sin(angle))).normalize();
            Vec3 tangent = haloX.scale(-Mth.sin(angle)).add(haloY.scale(Mth.cos(angle))).normalize();
            Vec3 markCenter = center.add(radial.scale(radius));
            float halfLong = scale * (0.050F + 0.026F * pulse);
            float halfWide = scale * (0.018F + 0.010F * pulse);
            int markAlpha = (int) (alpha * (0.42F + pulse * 0.34F));
            addQuad(buffer, matrix, cameraPos, tangent, radial, markCenter, halfLong, halfWide, 0xB9F7FF, markAlpha);
        }

        for (int i = 0; i < 5; i++) {
            float angle = renderTime * 0.031F + i * 2.5132742F;
            Vec3 radial = haloX.scale(Mth.cos(angle)).add(haloY.scale(Mth.sin(angle))).normalize();
            Vec3 tangent = haloX.scale(-Mth.sin(angle)).add(haloY.scale(Mth.cos(angle))).normalize();
            Vec3 sparkCenter = center.add(radial.scale(scale * 0.98D));
            int sparkAlpha = (int) (alpha * (0.45F + 0.25F * Mth.sin(renderTime * 0.21F + i)));
            addQuad(buffer, matrix, cameraPos, tangent, radial, sparkCenter, scale * 0.035F, scale * 0.035F, 0xFF66D9, sparkAlpha);
        }

        BufferUploader.drawWithShader(buffer.buildOrThrow());
    }

    private static void drawPlane(Matrix4f matrix, Vec3 cameraPos, Vec3 xAxis, Vec3 yAxis, Vec3 center, float halfWidth, float halfHeight, int color, int alpha) {
        BufferBuilder buffer = Tesselator.getInstance().begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_TEX_COLOR);
        addQuad(buffer, matrix, cameraPos, xAxis, yAxis, center, halfWidth, halfHeight, color, alpha);
        BufferUploader.drawWithShader(buffer.buildOrThrow());
    }

    private static void addQuad(BufferBuilder buffer, Matrix4f matrix, Vec3 cameraPos, Vec3 xAxis, Vec3 yAxis, Vec3 center, float halfWidth, float halfHeight, int color, int alpha) {
        Vec3 relative = center.subtract(cameraPos);
        Vec3 dx = xAxis.scale(halfWidth);
        Vec3 dy = yAxis.scale(halfHeight);

        putVertex(buffer, matrix, relative.subtract(dx).subtract(dy), color, alpha, 0.0F, 1.0F);
        putVertex(buffer, matrix, relative.add(dx).subtract(dy), color, alpha, 1.0F, 1.0F);
        putVertex(buffer, matrix, relative.add(dx).add(dy), color, alpha, 1.0F, 0.0F);
        putVertex(buffer, matrix, relative.subtract(dx).add(dy), color, alpha, 0.0F, 0.0F);
    }

    private static void putVertex(BufferBuilder buffer, Matrix4f matrix, Vec3 pos, int color, int alpha, float u, float v) {
        buffer.addVertex(matrix, (float) pos.x, (float) pos.y, (float) pos.z)
                .setColor((color >> 16) & 255, (color >> 8) & 255, color & 255, Mth.clamp(alpha, 0, 255))
                .setUv(u, v)
        ;
    }

    private static Vec3 faceLook(Entity entity, float partialTick) {
        Vec3 look = entity.getViewVector(partialTick);
        if (look.lengthSqr() < 1.0E-5D) {
            return horizontalLook(entity, partialTick);
        }
        return look.normalize();
    }

    private static Vec3 headRight(Entity entity, Vec3 forward, float partialTick) {
        Vec3 right = WORLD_UP.cross(forward);
        if (right.lengthSqr() < 1.0E-5D) {
            Vec3 horizontalForward = horizontalLook(entity, partialTick);
            right = new Vec3(horizontalForward.z, 0.0D, -horizontalForward.x);
        }
        return right.normalize();
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

    private static float scale(Entity entity, ActiveEntityVisualEffect effect) {
        float base = effect.data().contains("Scale") ? effect.data().getFloat("Scale") : 1.0F;
        float entityScale = (float) Mth.clamp(0.62D + entity.getBbWidth() * 0.40D + entity.getBbHeight() * 0.06D, 0.70D, 2.25D);
        return Mth.clamp(base, 0.20F, 6.0F) * entityScale;
    }

    private static float intensity(ActiveEntityVisualEffect effect) {
        return effect.data().contains("Intensity") ? Mth.clamp(effect.data().getFloat("Intensity"), 0.05F, 8.0F) : 1.08F;
    }

    private static double behindOffset(Entity entity, ActiveEntityVisualEffect effect) {
        if (effect.data().contains("BehindOffset")) {
            return Mth.clamp(effect.data().getDouble("BehindOffset"), -2.0D, 3.0D);
        }
        return Mth.clamp(0.20D + entity.getBbWidth() * 0.66D, 0.42D, 1.65D);
    }

    private static double yOffset(Entity entity, ActiveEntityVisualEffect effect) {
        if (effect.data().contains("YOffset")) {
            return Mth.clamp(effect.data().getDouble("YOffset"), -entity.getBbHeight(), entity.getBbHeight());
        }
        return 0.16D + entity.getBbHeight() * 0.08D;
    }

    private static float tiltDegrees(ActiveEntityVisualEffect effect) {
        if (effect.data().contains("TiltDegrees")) {
            return Mth.clamp(effect.data().getFloat("TiltDegrees"), -80.0F, 80.0F);
        }
        return 45.0F;
    }

    private static float distanceFade(double distance) {
        float fade = 1.0F - smoothstep((float) (TRACK_DISTANCE * 0.76D), (float) TRACK_DISTANCE, (float) distance);
        return Mth.clamp(fade, 0.0F, 1.0F);
    }

    private static float smoothstep(float edge0, float edge1, float value) {
        float x = Mth.clamp((value - edge0) / (edge1 - edge0), 0.0F, 1.0F);
        return x * x * (3.0F - 2.0F * x);
    }
}

