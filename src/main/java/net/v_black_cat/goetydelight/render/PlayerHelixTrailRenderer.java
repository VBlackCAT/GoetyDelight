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

public final class PlayerHelixTrailRenderer {
    private static final int HELIX_STEPS = 44;
    private static final int TRAIL_POINTS = 18;
    private static final double TRACK_DISTANCE = 48.0D;
    private static final double TRAIL_SCALE = 2.0D;
    private static final Map<UUID, ArrayDeque<TrailPoint>> TRAILS = new HashMap<>();

    private PlayerHelixTrailRenderer() {
    }

    public static void render(RenderLevelStageEvent event, Entity entity, ActiveEntityVisualEffect effect) {
        float partialTick = event.getPartialTick().getGameTimeDeltaPartialTick(true);
        float renderTime = entity.level().getGameTime() + partialTick;
        Camera camera = event.getCamera();
        Vec3 cameraPos = camera.getPosition();

        if (entity.distanceToSqr(cameraPos) > TRACK_DISTANCE * TRACK_DISTANCE) {
            TRAILS.remove(entity.getUUID());
            return;
        }

        Matrix4f oldProjection = new Matrix4f(RenderSystem.getProjectionMatrix());
        VertexSorting oldSorting = RenderSystem.getVertexSorting();
        RenderSystem.setProjectionMatrix(event.getProjectionMatrix(), VertexSorting.DISTANCE_TO_ORIGIN);

        ShaderInstance shader = ModShaderReg.getPlayerHelixTrailShader();
        shader.safeGetUniform("iTime").set(renderTime / 20.0F);
        shader.safeGetUniform("intensity").set(1.0F);

        RenderSystem.setShader(() -> shader);
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        RenderSystem.enableDepthTest();
        RenderSystem.depthMask(false);
        RenderSystem.enableBlend();
        RenderSystem.blendFuncSeparate(770, 1, 1, 1);
        RenderSystem.disableCull();

        PoseStack poseStack = event.getPoseStack();
        Vec3 entityPos = entity.getPosition(partialTick);
        recordTrailPoint(entity, entityPos, renderTime);
        renderEntityEffect(poseStack, shader, camera, cameraPos, entity, renderTime);

        RenderSystem.enableCull();
        RenderSystem.disableBlend();
        RenderSystem.depthMask(true);
        RenderSystem.setProjectionMatrix(oldProjection, oldSorting);
    }

    private static void recordTrailPoint(Entity entity, Vec3 entityPos, float renderTime) {
        ArrayDeque<TrailPoint> points = TRAILS.computeIfAbsent(entity.getUUID(), uuid -> new ArrayDeque<>());
        Vec3 center = entityPos.add(0.0D, entity.getBbHeight() * 0.52D, 0.0D);

        if (points.isEmpty() || points.peekFirst().position.distanceToSqr(center) > 0.012D) {
            points.addFirst(new TrailPoint(center, renderTime));
        }

        while (points.size() > TRAIL_POINTS) {
            points.removeLast();
        }
    }

    private static void renderEntityEffect(PoseStack poseStack, ShaderInstance shader, Camera camera, Vec3 cameraPos, Entity entity, float renderTime) {
        shader.safeGetUniform("EffectMode").set(0);
        drawHelixTrail(poseStack.last().pose(), cameraPos, entity, renderTime, 0.0F);
        drawHelixTrail(poseStack.last().pose(), cameraPos, entity, renderTime, (float) Math.PI);

        shader.safeGetUniform("EffectMode").set(1);
        drawSparkTrail(poseStack.last().pose(), camera, cameraPos, entity.getUUID(), renderTime);
    }

    private static void drawHelixTrail(Matrix4f matrix, Vec3 cameraPos, Entity entity, float renderTime, float phaseOffset) {
        ArrayDeque<TrailPoint> points = TRAILS.get(entity.getUUID());
        if (points == null || points.size() < 2) {
            return;
        }

        TrailPoint[] trail = points.toArray(new TrailPoint[0]);
        BufferBuilder buffer = Tesselator.getInstance().begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_TEX_COLOR);

        int segments = Math.min(HELIX_STEPS, (trail.length - 1) * 4);
        for (int i = 0; i < segments; i++) {
            float t0 = (float) i / segments;
            float t1 = (float) (i + 1) / segments;
            addTrailRibbonSegment(buffer, matrix, cameraPos, trail, renderTime, phaseOffset, t0, t1);
        }

        BufferUploader.drawWithShader(buffer.buildOrThrow());
    }

    private static void addTrailRibbonSegment(BufferBuilder buffer, Matrix4f matrix, Vec3 cameraPos, TrailPoint[] trail, float renderTime, float phaseOffset, float t0, float t1) {
        HelixSample a = sampleTrailHelix(trail, renderTime, phaseOffset, t0);
        HelixSample b = sampleTrailHelix(trail, renderTime, phaseOffset, t1);
        Vec3 sideA = ribbonSide(a.position, cameraPos, a.radial, 0.055D * TRAIL_SCALE);
        Vec3 sideB = ribbonSide(b.position, cameraPos, b.radial, 0.055D * TRAIL_SCALE);

        int alphaA = helixAlpha(t0);
        int alphaB = helixAlpha(t1);
        int colorA = colorPhase(renderTime * 0.065F + t0 * 2.2F + phaseOffset);
        int colorB = colorPhase(renderTime * 0.065F + t1 * 2.2F + phaseOffset);

        putVertex(buffer, matrix, a.position.add(sideA).subtract(cameraPos), colorA, alphaA, t0, 0.0F);
        putVertex(buffer, matrix, a.position.subtract(sideA).subtract(cameraPos), colorA, alphaA, t0, 1.0F);
        putVertex(buffer, matrix, b.position.subtract(sideB).subtract(cameraPos), colorB, alphaB, t1, 1.0F);
        putVertex(buffer, matrix, b.position.add(sideB).subtract(cameraPos), colorB, alphaB, t1, 0.0F);
    }

    private static HelixSample sampleTrailHelix(TrailPoint[] trail, float renderTime, float phaseOffset, float t) {
        TrailFrame frame = sampleTrailFrame(trail, t);
        double ageFade = 1.0D - t;
        double radiusScale = trailHelixRadiusScale(t);
        double radius = (0.22D + 0.10D * ageFade) * TRAIL_SCALE * radiusScale;
        double angle = t * Math.PI * 7.0D + renderTime * 0.18D + phaseOffset;
        double wave = Math.sin(t * Math.PI * 5.0D + renderTime * 0.13D) * 0.035D * TRAIL_SCALE * radiusScale;
        Vec3 radial = frame.right.scale(Math.cos(angle)).add(frame.up.scale(Math.sin(angle)));
        Vec3 position = frame.center.add(radial.scale(radius + wave));
        return new HelixSample(position, radial);
    }

    private static TrailFrame sampleTrailFrame(TrailPoint[] trail, float t) {
        float scaled = Mth.clamp(t, 0.0F, 1.0F) * (trail.length - 1);
        int index = Math.min((int) scaled, trail.length - 2);
        float local = scaled - index;

        Vec3 newer = trail[index].position;
        Vec3 older = trail[index + 1].position;
        Vec3 center = newer.lerp(older, local);
        Vec3 tangent = older.subtract(newer);
        if (tangent.lengthSqr() < 1.0E-5D) {
            tangent = new Vec3(0.0D, 0.0D, 1.0D);
        }
        tangent = tangent.normalize();

        Vec3 upHint = Math.abs(tangent.y) > 0.92D ? new Vec3(1.0D, 0.0D, 0.0D) : new Vec3(0.0D, 1.0D, 0.0D);
        Vec3 right = tangent.cross(upHint).normalize();
        Vec3 up = right.cross(tangent).normalize();
        return new TrailFrame(center, right, up);
    }

    private static Vec3 ribbonSide(Vec3 position, Vec3 cameraPos, Vec3 radial, double width) {
        Vec3 toCamera = cameraPos.subtract(position).normalize();
        Vec3 side = radial.cross(toCamera);
        if (side.lengthSqr() < 1.0E-5D) {
            side = radial.cross(new Vec3(0.0D, 1.0D, 0.0D));
        }
        return side.normalize().scale(width);
    }

    private static int helixAlpha(float t) {
        float endFade = 1.0F - smoothstep(0.78F, 1.0F, t);
        float startFade = smoothstep(0.0F, 0.10F, t);
        return (int) (Mth.clamp(startFade * endFade, 0.0F, 1.0F) * 210.0F);
    }

    private static double trailHelixRadiusScale(float t) {
        float middleBulge = Mth.sin(Mth.PI * Mth.clamp(t, 0.0F, 1.0F));
        return 0.10D + middleBulge * 0.90D;
    }

    private static void drawSparkTrail(Matrix4f matrix, Camera camera, Vec3 cameraPos, UUID playerId, float renderTime) {
        ArrayDeque<TrailPoint> points = TRAILS.get(playerId);
        if (points == null || points.size() < 2) {
            return;
        }

        Vector3f left = camera.getLeftVector();
        Vector3f up = camera.getUpVector();
        Vec3 cameraLeft = new Vec3(left.x(), left.y(), left.z());
        Vec3 cameraUp = new Vec3(up.x(), up.y(), up.z());
        BufferBuilder buffer = Tesselator.getInstance().begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_TEX_COLOR);

        int index = 0;
        Iterator<TrailPoint> iterator = points.iterator();
        while (iterator.hasNext()) {
            TrailPoint point = iterator.next();
            float age = Math.min(1.0F, index / (float) (TRAIL_POINTS - 1));
            float fade = 1.0F - age;
            double swirl = renderTime * 0.20D + index * 1.618D;
            double orbit = 0.16D * fade * TRAIL_SCALE;
            Vec3 sparkle = point.position
                    .add(Math.cos(swirl) * orbit, Math.sin(swirl * 1.3D) * orbit * 0.55D, Math.sin(swirl) * orbit)
                    .subtract(cameraPos);

            float size = (float) ((0.105F * fade + 0.018F) * TRAIL_SCALE);
            int color = colorPhase(renderTime * 0.075F - index * 0.18F);
            int alpha = (int) (fade * fade * 190.0F);

            addBillboard(buffer, matrix, cameraLeft, cameraUp, sparkle, size, color, alpha, age);
            index++;
        }

        BufferUploader.drawWithShader(buffer.buildOrThrow());
    }

    private static void addBillboard(BufferBuilder buffer, Matrix4f matrix, Vec3 left, Vec3 up, Vec3 center, float size, int color, int alpha, float age) {
        Vec3 dx = left.scale(size);
        Vec3 dy = up.scale(size);

        putVertex(buffer, matrix, center.subtract(dx).subtract(dy), color, alpha, age, 1.0F);
        putVertex(buffer, matrix, center.add(dx).subtract(dy), color, alpha, age, 1.0F);
        putVertex(buffer, matrix, center.add(dx).add(dy), color, alpha, age, 0.0F);
        putVertex(buffer, matrix, center.subtract(dx).add(dy), color, alpha, age, 0.0F);
    }

    private static void putVertex(BufferBuilder buffer, Matrix4f matrix, Vec3 pos, int color, int alpha, float u, float v) {
        buffer.addVertex(matrix, (float) pos.x, (float) pos.y, (float) pos.z)
                .setColor((color >> 16) & 255, (color >> 8) & 255, color & 255, alpha)
                .setUv(u, v)
        ;
    }

    private static int colorPhase(float phase) {
        int red = colorChannel(phase);
        int green = colorChannel(phase + 2.0943952F);
        int blue = colorChannel(phase + 4.1887903F);
        return red << 16 | green << 8 | blue;
    }

    private static int colorChannel(float phase) {
        return 92 + (int) ((0.5F + 0.5F * Mth.sin(phase)) * 163.0F);
    }

    private static float smoothstep(float edge0, float edge1, float value) {
        float x = Mth.clamp((value - edge0) / (edge1 - edge0), 0.0F, 1.0F);
        return x * x * (3.0F - 2.0F * x);
    }

    private record HelixSample(Vec3 position, Vec3 radial) {
    }

    private record TrailFrame(Vec3 center, Vec3 right, Vec3 up) {
    }

    private record TrailPoint(Vec3 position, float time) {
    }
}

