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

public final class SupremeChaosCosmosRenderer {
    private static final double TRACK_DISTANCE = 96.0D;
    private static final Vec3 WORLD_UP = new Vec3(0.0D, 1.0D, 0.0D);

    private SupremeChaosCosmosRenderer() {
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

        float distanceFade = distanceFade(Math.sqrt(distanceSqr));
        int baseAlpha = (int) (distanceFade * 230.0F);
        if (baseAlpha <= 0) {
            return;
        }

        float scale = scale(entity, effect);
        float intensity = intensity(effect);
        Vec3 forward = faceLook(entity, partialTick);
        Vec3 right = WORLD_UP.cross(forward);
        if (right.lengthSqr() < 1.0E-5D) {
            right = new Vec3(1.0D, 0.0D, 0.0D);
        }
        right = right.normalize();
        Vec3 headUp = forward.cross(right).normalize();

        double yOffset = yOffset(entity, effect);
        double behindOffset = behindOffset(entity, effect);
        Vec3 center = entity.getEyePosition(partialTick)
                .add(headUp.scale(yOffset))
                .subtract(forward.scale(behindOffset));

        double tilt1Rad = tilt1Degrees(effect) * Math.PI / 180.0D;
        double tilt2Rad = tilt2Degrees(effect) * Math.PI / 180.0D;

        Vec3 plane1X = right;
        Vec3 plane1Y = headUp.scale(Math.cos(tilt1Rad)).add(forward.scale(Math.sin(tilt1Rad))).normalize();

        Vec3 plane2X = right.scale(Math.cos(tilt2Rad)).add(headUp.scale(Math.sin(tilt2Rad))).normalize();
        Vec3 plane2Y = headUp.scale(Math.cos(tilt1Rad * 0.6)).add(forward.scale(-Math.sin(tilt1Rad * 0.6))).normalize();

        Vec3 plane3X = right;
        Vec3 plane3Y = headUp;

        ShaderInstance shader = ModShaderReg.getSupremeChaosCosmosShader();
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

        shader.safeGetUniform("EffectMode").set(3);
        drawPlane(matrix, cameraPos, plane2X, plane2Y, center,
                scale * 1.55F, scale * 1.55F, 0xFFAAFF, baseAlpha / 3);

        shader.safeGetUniform("EffectMode").set(2);
        drawPlane(matrix, cameraPos, plane3X, plane3Y, center,
                scale * 1.30F, scale * 1.30F, 0xFFE8B0, (int) (baseAlpha * 0.55F));

        shader.safeGetUniform("EffectMode").set(0);
        drawPlane(matrix, cameraPos, plane1X, plane1Y, center,
                scale * 1.22F, scale * 1.22F, 0xFFD700, baseAlpha / 2);
        drawPlane(matrix, cameraPos, plane1X, plane1Y, center,
                scale, scale, 0xFFFFFF, baseAlpha);

        shader.safeGetUniform("EffectMode").set(1);
        drawOrbitingStars(matrix, cameraPos, plane1X, plane1Y, center, renderTime, scale, baseAlpha);

        shader.safeGetUniform("EffectMode").set(4);
        drawSparkleShower(matrix, cameraPos, right, headUp, center, renderTime, scale, baseAlpha);

        RenderSystem.enableCull();
        RenderSystem.disableBlend();
        RenderSystem.depthMask(true);
        RenderSystem.setProjectionMatrix(oldProjection, oldSorting);
    }

    private static void drawOrbitingStars(Matrix4f matrix, Vec3 cameraPos, Vec3 xAxis, Vec3 yAxis,
                                          Vec3 center, float renderTime, float scale, int alpha) {
        BufferBuilder buffer = Tesselator.getInstance().begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_TEX_COLOR);

        for (int i = 0; i < 10; i++) {
            float baseAngle = (float) (i * Math.PI * 2.0D / 10.0D);
            float speed = 0.024F + (i % 3) * 0.008F;
            float dir = (i % 2 == 0) ? 1.0F : -0.68F;
            float angle = baseAngle + renderTime * speed * dir;

            float pulse = 0.65F + 0.35F * Mth.sin(renderTime * 0.28F + i * 2.17F);
            double radius = scale * (0.68D + 0.08D * Mth.sin(renderTime * 0.13F + i));

            Vec3 radial = xAxis.scale(Mth.cos(angle)).add(yAxis.scale(Mth.sin(angle))).normalize();
            Vec3 tangent = xAxis.scale(-Mth.sin(angle)).add(yAxis.scale(Mth.cos(angle))).normalize();
            Vec3 starCenter = center.add(radial.scale(radius));

            float starSize = scale * (0.038F + 0.018F * pulse);
            int starAlpha = (int) (alpha * (0.50F + pulse * 0.50F));

            addQuad(buffer, matrix, cameraPos, tangent, radial, starCenter,
                    starSize, starSize, 0xFFFFAA, starAlpha);
        }

        for (int i = 0; i < 6; i++) {
            float angle = renderTime * 0.035F + i * 1.047F;
            Vec3 radial = xAxis.scale(Mth.cos(angle)).add(yAxis.scale(Mth.sin(angle))).normalize();
            Vec3 tangent = xAxis.scale(-Mth.sin(angle)).add(yAxis.scale(Mth.cos(angle))).normalize();
            Vec3 sparkCenter = center.add(radial.scale(scale * 0.92D));

            int sparkAlpha = (int) (alpha * (0.40F + 0.30F * Mth.sin(renderTime * 0.33F + i)));
            addQuad(buffer, matrix, cameraPos, tangent, radial, sparkCenter,
                    scale * 0.022F, scale * 0.022F, 0xFF88FF, sparkAlpha);
        }

        BufferUploader.drawWithShader(buffer.buildOrThrow());
    }

    private static void drawSparkleShower(Matrix4f matrix, Vec3 cameraPos, Vec3 xAxis, Vec3 yAxis,
                                          Vec3 center, float renderTime, float scale, int alpha) {
        BufferBuilder buffer = Tesselator.getInstance().begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_TEX_COLOR);

        for (int i = 0; i < 14; i++) {
            float seedX = Mth.sin(i * 17.31F) * 0.5F + 0.5F;
            float seedY = Mth.sin(i * 31.77F) * 0.5F + 0.5F;

            float offsetX = (seedX - 0.5F) * 2.0F * scale * 0.85F;
            float speed = 0.25F + seedY * 0.45F;
            float cycle = (renderTime * speed * 0.04F + seedY) % 1.0F;
            float offsetY = (1.0F - cycle * 2.0F) * scale * 0.90F;

            float twinkle = 0.45F + 0.55F * Mth.sin(renderTime * 0.52F + i * 3.71F);
            float size = scale * (0.020F + 0.014F * twinkle);

            Vec3 particleCenter = center.add(xAxis.scale(offsetX)).add(yAxis.scale(offsetY));
            int particleAlpha = (int) (alpha * twinkle * (1.0F - cycle * 0.60F) * 0.70F);

            addQuad(buffer, matrix, cameraPos, xAxis, yAxis, particleCenter,
                    size, size, 0xFFDDFF, particleAlpha);
        }

        BufferUploader.drawWithShader(buffer.buildOrThrow());
    }

    private static void drawPlane(Matrix4f matrix, Vec3 cameraPos, Vec3 xAxis, Vec3 yAxis,
                                  Vec3 center, float halfWidth, float halfHeight, int color, int alpha) {
        BufferBuilder buffer = Tesselator.getInstance().begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_TEX_COLOR);
        addQuad(buffer, matrix, cameraPos, xAxis, yAxis, center, halfWidth, halfHeight, color, alpha);
        BufferUploader.drawWithShader(buffer.buildOrThrow());
    }

    private static void addQuad(BufferBuilder buffer, Matrix4f matrix, Vec3 cameraPos, Vec3 xAxis, Vec3 yAxis,
                                Vec3 center, float halfWidth, float halfHeight, int color, int alpha) {
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
            float yaw = entity.getViewYRot(partialTick) * ((float) Math.PI / 180.0F);
            look = new Vec3(-Mth.sin(yaw), 0.0D, Mth.cos(yaw));
        }
        return look.normalize();
    }

    private static float scale(Entity entity, ActiveEntityVisualEffect effect) {
        float base = effect.data().contains("Scale") ? effect.data().getFloat("Scale") : 1.0F;
        float entityScale = (float) Mth.clamp(0.66D + entity.getBbWidth() * 0.42D + entity.getBbHeight() * 0.07D, 0.78D, 2.60D);
        return Mth.clamp(base, 0.20F, 7.0F) * entityScale;
    }

    private static float intensity(ActiveEntityVisualEffect effect) {
        return effect.data().contains("Intensity") ? Mth.clamp(effect.data().getFloat("Intensity"), 0.05F, 8.0F) : 1.35F;
    }

    private static double behindOffset(Entity entity, ActiveEntityVisualEffect effect) {
        if (effect.data().contains("BehindOffset")) {
            return Mth.clamp(effect.data().getDouble("BehindOffset"), -2.0D, 3.0D);
        }
        return Mth.clamp(0.22D + entity.getBbWidth() * 0.62D, 0.42D, 1.70D);
    }

    private static double yOffset(Entity entity, ActiveEntityVisualEffect effect) {
        if (effect.data().contains("YOffset")) {
            return Mth.clamp(effect.data().getDouble("YOffset"), -entity.getBbHeight(), entity.getBbHeight());
        }
        return 0.18D + entity.getBbHeight() * 0.12D;
    }

    private static float tilt1Degrees(ActiveEntityVisualEffect effect) {
        if (effect.data().contains("Tilt1Degrees")) {
            return Mth.clamp(effect.data().getFloat("Tilt1Degrees"), -85.0F, 85.0F);
        }
        return 38.0F;
    }

    private static float tilt2Degrees(ActiveEntityVisualEffect effect) {
        if (effect.data().contains("Tilt2Degrees")) {
            return Mth.clamp(effect.data().getFloat("Tilt2Degrees"), -85.0F, 85.0F);
        }
        return -25.0F;
    }

    private static float distanceFade(double distance) {
        float fade = 1.0F - smoothstep((float) (TRACK_DISTANCE * 0.74D), (float) TRACK_DISTANCE, (float) distance);
        return Mth.clamp(fade, 0.0F, 1.0F);
    }

    private static float smoothstep(float edge0, float edge1, float value) {
        float x = Mth.clamp((value - edge0) / (edge1 - edge0), 0.0F, 1.0F);
        return x * x * (3.0F - 2.0F * x);
    }
}

