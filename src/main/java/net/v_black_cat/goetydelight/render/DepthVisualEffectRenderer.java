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
import org.joml.Vector3f;

import java.util.*;

public final class DepthVisualEffectRenderer {
    private static final int TRAIL_POINTS = 22;
    private static final double TRAIL_MIN_STEP = 0.018D;
    private static final Map<UUID, ArrayDeque<TrailPoint>> SOFT_TRAILS = new HashMap<>();

    private DepthVisualEffectRenderer() {
    }

    public static void renderDepthOccludedHalo(RenderLevelStageEvent event, Entity entity, ActiveEntityVisualEffect effect) {
        withDepthShader(event, 0, 1.25F, shader -> {
            Vec3 center = entityCenter(entity, event.getPartialTick().getGameTimeDeltaPartialTick(true), 0.58D);
            double radius = Math.max(0.9D, entity.getBbWidth() * 1.3D);
            drawBillboard(event, center, radius * 2.35D, radius * 2.35D, color(event, 0.0F, 205), 0.0F);
        });
    }

    public static void renderContactEdgeGlow(RenderLevelStageEvent event, Entity entity, ActiveEntityVisualEffect effect) {
        withDepthShader(event, 1, contactIntensity(entity), shader -> {
            Vec3 base = entity.getPosition(event.getPartialTick().getGameTimeDeltaPartialTick(true)).add(0.0D, 0.035D, 0.0D);
            double radius = Math.max(0.72D, entity.getBbWidth() * 0.82D);
            drawGroundRing(event, base, radius, 0.09D, color(event, 1.7F, entity.onGround() ? 220 : 95));
        });
    }

    public static void renderSoftTrail(RenderLevelStageEvent event, Entity entity, ActiveEntityVisualEffect effect) {
        float renderTime = event.getCamera().getEntity().level().getGameTime() + event.getPartialTick().getGameTimeDeltaPartialTick(true);
        recordSoftTrail(entity, event.getPartialTick().getGameTimeDeltaPartialTick(true), renderTime);

        withDepthShader(event, 2, 0.95F, shader -> drawSoftTrail(event, entity.getUUID(), renderTime));
    }

    public static void renderScreenSpaceShockwave(RenderLevelStageEvent event, Entity entity, ActiveEntityVisualEffect effect) {
        float progress = effectProgress(event, entity, effect);
        if (progress >= 1.0F) {
            return;
        }

        withDepthShader(event, 3, 1.45F * (1.0F - progress), shader -> {
            Vec3 center = entityCenter(entity, event.getPartialTick().getGameTimeDeltaPartialTick(true), 0.52D);
            double radius = (0.9D + progress * 5.4D) * Math.max(1.0D, entity.getBbWidth());
            drawCameraRing(event, center, radius, 0.12D + progress * 0.42D, color(event, 3.4F + progress * 2.0F, (int) (210.0F * (1.0F - progress))));
        });
    }

    public static void renderDepthRefractionHeatwave(RenderLevelStageEvent event, Entity entity, ActiveEntityVisualEffect effect) {
        withDepthShader(event, 4, 0.62F, shader -> {
            Vec3 center = entityCenter(entity, event.getPartialTick().getGameTimeDeltaPartialTick(true), 0.56D);
            double width = Math.max(1.1D, entity.getBbWidth() * 1.45D);
            double height = Math.max(1.7D, entity.getBbHeight() * 1.25D);
            drawBillboard(event, center, width, height, color(event, 4.8F, 118), 0.0F);
            drawBillboard(event, center, width * 0.72D, height * 1.18D, color(event, 6.1F, 82), 0.5F);
        });
    }

    public static void renderVolumetricLightColumn(RenderLevelStageEvent event, Entity entity, ActiveEntityVisualEffect effect) {
        withDepthShader(event, 5, 0.78F, shader -> {
            Vec3 base = entity.getPosition(event.getPartialTick().getGameTimeDeltaPartialTick(true)).add(0.0D, entity.getBbHeight() * 0.04D, 0.0D);
            double radius = Math.max(0.55D, entity.getBbWidth() * 0.72D);
            double height = Math.max(3.2D, entity.getBbHeight() * 2.6D);
            int color = color(event, 5.9F, 88);
            drawVerticalSheet(event, base, radius, height, color, 0.0D);
            drawVerticalSheet(event, base, radius, height, color(event, 7.4F, 72), Math.PI * 0.5D);
            drawVerticalSheet(event, base, radius * 0.72D, height * 1.12D, color(event, 8.2F, 58), Math.PI * 0.25D);
        });
    }

    public static void renderOutlineScan(RenderLevelStageEvent event, Entity entity, ActiveEntityVisualEffect effect) {
        withDepthShader(event, 6, 1.0F, shader -> {
            float renderTime = entity.tickCount + event.getPartialTick().getGameTimeDeltaPartialTick(true);
            double scan = 0.12D + (0.5D + 0.5D * Math.sin(renderTime * 0.12D)) * entity.getBbHeight() * 0.9D;
            Vec3 center = entity.getPosition(event.getPartialTick().getGameTimeDeltaPartialTick(true)).add(0.0D, scan, 0.0D);
            double radius = Math.max(0.72D, entity.getBbWidth() * 0.9D);
            drawCameraRing(event, center, radius, 0.045D, color(event, 8.7F, 230));
            drawBillboard(event, entityCenter(entity, event.getPartialTick().getGameTimeDeltaPartialTick(true), 0.54D), radius * 2.05D, entity.getBbHeight() * 1.2D, color(event, 9.6F, 62), 0.0F);
        });
    }

    private static void withDepthShader(RenderLevelStageEvent event, int mode, float intensity, RenderWork work) {
        Matrix4f oldProjection = new Matrix4f(RenderSystem.getProjectionMatrix());
        VertexSorting oldSorting = RenderSystem.getVertexSorting();
        RenderSystem.setProjectionMatrix(event.getProjectionMatrix(), VertexSorting.DISTANCE_TO_ORIGIN);

        ShaderInstance shader = ModShaderReg.getEntityDepthEffectShader();
        shader.safeGetUniform("iTime").set((event.getRenderTick() + event.getPartialTick().getGameTimeDeltaPartialTick(true)) / 20.0F);
        shader.safeGetUniform("intensity").set(intensity);
        shader.safeGetUniform("EffectMode").set(mode);

        RenderSystem.setShader(() -> shader);
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        RenderSystem.enableDepthTest();
        RenderSystem.depthMask(false);
        RenderSystem.enableBlend();
        RenderSystem.blendFuncSeparate(770, 1, 1, 1);
        RenderSystem.disableCull();

        work.render(shader);

        RenderSystem.enableCull();
        RenderSystem.disableBlend();
        RenderSystem.depthMask(true);
        RenderSystem.setProjectionMatrix(oldProjection, oldSorting);
    }

    private static void drawBillboard(RenderLevelStageEvent event, Vec3 center, double width, double height, int color, float roll) {
        Camera camera = event.getCamera();
        Vec3 cameraPos = camera.getPosition();
        Vector3f leftVector = camera.getLeftVector();
        Vector3f upVector = camera.getUpVector();
        Vec3 left = new Vec3(leftVector.x(), leftVector.y(), leftVector.z());
        Vec3 up = new Vec3(upVector.x(), upVector.y(), upVector.z());

        if (roll != 0.0F) {
            double cos = Math.cos(roll);
            double sin = Math.sin(roll);
            Vec3 rolledLeft = left.scale(cos).add(up.scale(sin));
            up = up.scale(cos).subtract(left.scale(sin));
            left = rolledLeft;
        }

        Vec3 dx = left.scale(width * 0.5D);
        Vec3 dy = up.scale(height * 0.5D);
        putQuad(event.getPoseStack().last().pose(), center.subtract(cameraPos), dx, dy, color);
    }

    private static void drawGroundRing(RenderLevelStageEvent event, Vec3 center, double radius, double width, int color) {
        Matrix4f matrix = event.getPoseStack().last().pose();
        Vec3 cameraPos = event.getCamera().getPosition();
        BufferBuilder buffer = Tesselator.getInstance().begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_TEX_COLOR);

        int segments = 72;
        for (int i = 0; i < segments; i++) {
            double a0 = Math.PI * 2.0D * i / segments;
            double a1 = Math.PI * 2.0D * (i + 1) / segments;
            Vec3 inner0 = center.add(Math.cos(a0) * (radius - width), 0.0D, Math.sin(a0) * (radius - width)).subtract(cameraPos);
            Vec3 outer0 = center.add(Math.cos(a0) * (radius + width), 0.0D, Math.sin(a0) * (radius + width)).subtract(cameraPos);
            Vec3 outer1 = center.add(Math.cos(a1) * (radius + width), 0.0D, Math.sin(a1) * (radius + width)).subtract(cameraPos);
            Vec3 inner1 = center.add(Math.cos(a1) * (radius - width), 0.0D, Math.sin(a1) * (radius - width)).subtract(cameraPos);
            putVertex(buffer, matrix, inner0, color, i / (float) segments, 0.0F);
            putVertex(buffer, matrix, outer0, color, i / (float) segments, 1.0F);
            putVertex(buffer, matrix, outer1, color, (i + 1.0F) / segments, 1.0F);
            putVertex(buffer, matrix, inner1, color, (i + 1.0F) / segments, 0.0F);
        }

        BufferUploader.drawWithShader(buffer.buildOrThrow());
    }

    private static void drawCameraRing(RenderLevelStageEvent event, Vec3 center, double radius, double width, int color) {
        Camera camera = event.getCamera();
        Vec3 cameraPos = camera.getPosition();
        Vector3f leftVector = camera.getLeftVector();
        Vector3f upVector = camera.getUpVector();
        Vec3 left = new Vec3(leftVector.x(), leftVector.y(), leftVector.z());
        Vec3 up = new Vec3(upVector.x(), upVector.y(), upVector.z());
        Matrix4f matrix = event.getPoseStack().last().pose();
        BufferBuilder buffer = Tesselator.getInstance().begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_TEX_COLOR);

        int segments = 96;
        for (int i = 0; i < segments; i++) {
            double a0 = Math.PI * 2.0D * i / segments;
            double a1 = Math.PI * 2.0D * (i + 1) / segments;
            Vec3 inner0 = ringPoint(center, left, up, radius - width, a0).subtract(cameraPos);
            Vec3 outer0 = ringPoint(center, left, up, radius + width, a0).subtract(cameraPos);
            Vec3 outer1 = ringPoint(center, left, up, radius + width, a1).subtract(cameraPos);
            Vec3 inner1 = ringPoint(center, left, up, radius - width, a1).subtract(cameraPos);
            putVertex(buffer, matrix, inner0, color, i / (float) segments, 0.0F);
            putVertex(buffer, matrix, outer0, color, i / (float) segments, 1.0F);
            putVertex(buffer, matrix, outer1, color, (i + 1.0F) / segments, 1.0F);
            putVertex(buffer, matrix, inner1, color, (i + 1.0F) / segments, 0.0F);
        }

        BufferUploader.drawWithShader(buffer.buildOrThrow());
    }

    private static Vec3 ringPoint(Vec3 center, Vec3 left, Vec3 up, double radius, double angle) {
        return center.add(left.scale(Math.cos(angle) * radius)).add(up.scale(Math.sin(angle) * radius));
    }

    private static void drawVerticalSheet(RenderLevelStageEvent event, Vec3 base, double radius, double height, int color, double angle) {
        Vec3 cameraPos = event.getCamera().getPosition();
        Vec3 side = new Vec3(Math.cos(angle), 0.0D, Math.sin(angle)).scale(radius);
        Vec3 up = new Vec3(0.0D, height, 0.0D);
        Vec3 center = base.add(0.0D, height * 0.5D, 0.0D).subtract(cameraPos);
        putQuad(event.getPoseStack().last().pose(), center, side, up.scale(0.5D), color);
    }

    private static void putQuad(Matrix4f matrix, Vec3 center, Vec3 dx, Vec3 dy, int color) {
        BufferBuilder buffer = Tesselator.getInstance().begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_TEX_COLOR);
        putVertex(buffer, matrix, center.subtract(dx).subtract(dy), color, 0.0F, 1.0F);
        putVertex(buffer, matrix, center.add(dx).subtract(dy), color, 1.0F, 1.0F);
        putVertex(buffer, matrix, center.add(dx).add(dy), color, 1.0F, 0.0F);
        putVertex(buffer, matrix, center.subtract(dx).add(dy), color, 0.0F, 0.0F);
        BufferUploader.drawWithShader(buffer.buildOrThrow());
    }

    private static void recordSoftTrail(Entity entity, float partialTick, float renderTime) {
        ArrayDeque<TrailPoint> points = SOFT_TRAILS.computeIfAbsent(entity.getUUID(), uuid -> new ArrayDeque<>());
        Vec3 center = entityCenter(entity, partialTick, 0.48D);
        if (points.isEmpty() || points.peekFirst().position.distanceToSqr(center) > TRAIL_MIN_STEP) {
            points.addFirst(new TrailPoint(center, renderTime));
        }

        while (points.size() > TRAIL_POINTS) {
            points.removeLast();
        }
    }

    private static void drawSoftTrail(RenderLevelStageEvent event, UUID entityId, float renderTime) {
        ArrayDeque<TrailPoint> points = SOFT_TRAILS.get(entityId);
        if (points == null || points.size() < 2) {
            return;
        }

        Camera camera = event.getCamera();
        Vec3 cameraPos = camera.getPosition();
        Vector3f leftVector = camera.getLeftVector();
        Vector3f upVector = camera.getUpVector();
        Vec3 left = new Vec3(leftVector.x(), leftVector.y(), leftVector.z());
        Vec3 up = new Vec3(upVector.x(), upVector.y(), upVector.z());
        Matrix4f matrix = event.getPoseStack().last().pose();
        BufferBuilder buffer = Tesselator.getInstance().begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_TEX_COLOR);

        int index = 0;
        Iterator<TrailPoint> iterator = points.iterator();
        while (iterator.hasNext()) {
            TrailPoint point = iterator.next();
            float age = index / (float) Math.max(1, TRAIL_POINTS - 1);
            float fade = 1.0F - age;
            double swirl = renderTime * 0.18D + index * 0.74D;
            Vec3 center = point.position
                    .add(Math.cos(swirl) * 0.18D * fade, Math.sin(swirl * 1.4D) * 0.12D * fade, Math.sin(swirl) * 0.18D * fade)
                    .subtract(cameraPos);
            float size = 0.18F * fade + 0.035F;
            int color = color(renderTime, age * 4.0F, (int) (170.0F * fade * fade));
            Vec3 dx = left.scale(size);
            Vec3 dy = up.scale(size);
            putVertex(buffer, matrix, center.subtract(dx).subtract(dy), color, age, 1.0F);
            putVertex(buffer, matrix, center.add(dx).subtract(dy), color, age, 1.0F);
            putVertex(buffer, matrix, center.add(dx).add(dy), color, age, 0.0F);
            putVertex(buffer, matrix, center.subtract(dx).add(dy), color, age, 0.0F);
            index++;
        }

        BufferUploader.drawWithShader(buffer.buildOrThrow());
    }

    private static Vec3 entityCenter(Entity entity, float partialTick, double heightScale) {
        return entity.getPosition(partialTick).add(0.0D, entity.getBbHeight() * heightScale, 0.0D);
    }

    private static float effectProgress(RenderLevelStageEvent event, Entity entity, ActiveEntityVisualEffect effect) {
        if (effect.initialDuration() > 0) {
            return Mth.clamp(1.0F - effect.remainingTicks() / (float) effect.initialDuration(), 0.0F, 1.0F);
        }

        long start = effect.data().contains("StartGameTime") ? effect.data().getLong("StartGameTime") : entity.level().getGameTime();
        return Mth.clamp((entity.level().getGameTime() + event.getPartialTick().getGameTimeDeltaPartialTick(true) - start) / 36.0F, 0.0F, 1.0F);
    }

    private static float contactIntensity(Entity entity) {
        if (entity.onGround()) {
            return 1.15F;
        }
        return 0.38F;
    }

    private static int color(RenderLevelStageEvent event, float phase, int alpha) {
        return color((event.getRenderTick() + event.getPartialTick().getGameTimeDeltaPartialTick(true)) / 20.0F, phase, alpha);
    }

    private static int color(float time, float phase, int alpha) {
        int red = channel(time * 2.2F + phase);
        int green = channel(time * 2.2F + phase + 2.0943952F);
        int blue = channel(time * 2.2F + phase + 4.1887903F);
        return (Mth.clamp(alpha, 0, 255) << 24) | (red << 16) | (green << 8) | blue;
    }

    private static int channel(float phase) {
        return 92 + (int) ((0.5F + 0.5F * Mth.sin(phase)) * 163.0F);
    }

    private static void putVertex(BufferBuilder buffer, Matrix4f matrix, Vec3 pos, int color, float u, float v) {
        buffer.addVertex(matrix, (float) pos.x, (float) pos.y, (float) pos.z)
                .setColor((color >> 16) & 255, (color >> 8) & 255, color & 255, (color >> 24) & 255)
                .setUv(u, v)
        ;
    }

    @FunctionalInterface
    private interface RenderWork {
        void render(ShaderInstance shader);
    }

    private record TrailPoint(Vec3 position, float time) {
    }
}

