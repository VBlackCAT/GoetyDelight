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

public final class PhantomRiftShardsRenderer {
    private static final double TRACK_DISTANCE = 88.0D;
    private static final Vec3 WORLD_UP = new Vec3(0.0D, 1.0D, 0.0D);

    private PhantomRiftShardsRenderer() {
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

        float scale = scale(entity, effect);
        float intensity = intensity(effect);
        float distanceFade = distanceFade(Math.sqrt(distanceSqr));
        int alpha = (int) (distanceFade * 225.0F);
        if (alpha <= 0) {
            return;
        }

        Vec3 forward = faceLook(entity, partialTick);
        Vec3 right = WORLD_UP.cross(forward);
        if (right.lengthSqr() < 1.0E-5D) {
            right = new Vec3(1.0D, 0.0D, 0.0D);
        }
        right = right.normalize();
        Vec3 up = forward.cross(right).normalize();

        double yOffset = yOffset(entity, effect);
        double behindOffset = behindOffset(entity, effect);
        Vec3 center = entity.getEyePosition(partialTick)
                .add(up.scale(yOffset))
                .subtract(forward.scale(behindOffset));

        int shardCount = shardCount(effect);
        double orbitRadius = orbitRadius(effect, scale);
        double tiltRadians = tiltDegrees(effect) * Math.PI / 180.0D;
        Vec3 orbitX = right;
        Vec3 orbitY = up.scale(Math.cos(tiltRadians)).add(forward.scale(Math.sin(tiltRadians))).normalize();

        ShardPosition[] shards = computeShards(shardCount, orbitRadius, orbitX, orbitY, center, renderTime, scale);

        ShaderInstance shader = ModShaderReg.getPhantomRiftShardsShader();
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

        // Pass 1: outer aura glow
        shader.safeGetUniform("EffectMode").set(3);
        drawAuraPlane(matrix, cameraPos, orbitX, orbitY, center, scale * 1.40F, scale * 1.40F, 0x8B2FFF, alpha / 3);

        // Pass 2: void core
        shader.safeGetUniform("EffectMode").set(1);
        drawQuad(matrix, cameraPos, right, up, center, scale * 0.68F, scale * 0.68F, 0x0A0012, alpha);

        // Pass 3: arc lightning between shards
        shader.safeGetUniform("EffectMode").set(2);
        drawArcs(matrix, cameraPos, shards, renderTime, scale, alpha);

        // Pass 4: crystal shards
        shader.safeGetUniform("EffectMode").set(0);
        drawShards(matrix, cameraPos, shards, renderTime, scale, alpha);

        RenderSystem.enableCull();
        RenderSystem.disableBlend();
        RenderSystem.depthMask(true);
        RenderSystem.setProjectionMatrix(oldProjection, oldSorting);
    }

    private static ShardPosition[] computeShards(int count, double orbitRadius, Vec3 orbitX, Vec3 orbitY,
                                                  Vec3 center, float renderTime, float scale) {
        ShardPosition[] shards = new ShardPosition[count];
        for (int i = 0; i < count; i++) {
            float baseAngle = (float) (i * Math.PI * 2.0D / count);
            float speed = 0.012F + (i % 3) * 0.004F;
            float direction = (i % 2 == 0) ? 1.0F : -0.72F;
            float angle = baseAngle + renderTime * speed * direction;

            float radialOscillation = 0.88F + 0.12F * Mth.sin(renderTime * 0.14F + i * 2.17F);
            float verticalOscillation = Mth.sin(renderTime * 0.19F + i * 1.53F) * scale * 0.18F;

            Vec3 radial = orbitX.scale(Mth.cos(angle)).add(orbitY.scale(Mth.sin(angle))).normalize();
            Vec3 position = center
                    .add(radial.scale(orbitRadius * radialOscillation))
                    .add(WORLD_UP.scale(verticalOscillation));

            float shardScale = scale * (0.18F + 0.06F * Mth.sin(renderTime * 0.22F + i * 1.91F));
            shards[i] = new ShardPosition(position, radial, angle, shardScale);
        }
        return shards;
    }

    private static void drawShards(Matrix4f matrix, Vec3 cameraPos, ShardPosition[] shards,
                                    float renderTime, float scale, int alpha) {
        BufferBuilder buffer = Tesselator.getInstance().getBuilder();
        buffer.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_COLOR_TEX);

        for (int i = 0; i < shards.length; i++) {
            ShardPosition shard = shards[i];
            float pulse = 0.72F + 0.28F * Mth.sin(renderTime * 0.31F + i * 2.07F);
            int shardAlpha = (int) (alpha * (0.62F + pulse * 0.38F));

            Vec3 tangent = shard.radial.cross(WORLD_UP);
            if (tangent.lengthSqr() < 1.0E-5D) {
                tangent = new Vec3(1.0D, 0.0D, 0.0D);
            }
            tangent = tangent.normalize();
            Vec3 shardUp = shard.radial.cross(tangent).normalize();

            addQuad(buffer, matrix, cameraPos, tangent, shardUp, shard.position,
                    shard.scale, shard.scale * 1.45F, 0x8B2FFF, shardAlpha);
        }

        BufferUploader.drawWithShader(buffer.end());
    }

    private static void drawArcs(Matrix4f matrix, Vec3 cameraPos, ShardPosition[] shards,
                                  float renderTime, float scale, int alpha) {
        if (shards.length < 2) {
            return;
        }

        BufferBuilder buffer = Tesselator.getInstance().getBuilder();
        buffer.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_COLOR_TEX);

        for (int i = 0; i < shards.length; i++) {
            int nextIdx = (i + 1) % shards.length;
            ShardPosition a = shards[i];
            ShardPosition b = shards[nextIdx];

            float arcPulse = 0.42F + 0.58F * Mth.sin(renderTime * 0.27F + i * 1.83F);
            if (arcPulse < 0.38F) {
                continue;
            }

            int segments = 8;
            Vec3 prevPos = a.position;
            for (int s = 1; s <= segments; s++) {
                float t = s / (float) segments;
                float prevT = (s - 1) / (float) segments;

                float bendAmount = Mth.sin(renderTime * 0.53F + s * 2.31F + i * 1.17F) * scale * 0.08F;
                Vec3 lateralOffset = a.radial.scale(bendAmount);
                Vec3 currentPos = lerp(a.position, b.position, t).add(lateralOffset);

                float widthScale = Mth.sin(t * Mth.PI) * 0.85F + 0.15F;
                float segWidth = scale * 0.022F * widthScale;
                int segAlpha = (int) (alpha * arcPulse * (0.55F + 0.45F * Mth.sin(t * Mth.PI)));

                Vec3 segDir = currentPos.subtract(prevPos);
                Vec3 segSide;
                if (segDir.lengthSqr() < 1.0E-8D) {
                    segSide = WORLD_UP.scale(segWidth);
                } else {
                    segSide = segDir.normalize().cross(WORLD_UP);
                    if (segSide.lengthSqr() < 1.0E-5D) {
                        segSide = new Vec3(segDir.z, 0.0D, -segDir.x);
                    }
                    segSide = segSide.normalize().scale(segWidth);
                }

                putVertex(buffer, matrix, prevPos.add(segSide).subtract(cameraPos), 0x00E5FF, segAlpha, prevT, 0.0F);
                putVertex(buffer, matrix, prevPos.subtract(segSide).subtract(cameraPos), 0x00E5FF, segAlpha, prevT, 1.0F);
                putVertex(buffer, matrix, currentPos.subtract(segSide).subtract(cameraPos), 0x00E5FF, segAlpha, t, 1.0F);
                putVertex(buffer, matrix, currentPos.add(segSide).subtract(cameraPos), 0x00E5FF, segAlpha, t, 0.0F);

                prevPos = currentPos;
            }
        }

        BufferUploader.drawWithShader(buffer.end());
    }

    private static void drawAuraPlane(Matrix4f matrix, Vec3 cameraPos, Vec3 xAxis, Vec3 yAxis,
                                       Vec3 center, float halfWidth, float halfHeight, int color, int alpha) {
        BufferBuilder buffer = Tesselator.getInstance().getBuilder();
        buffer.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_COLOR_TEX);
        addQuad(buffer, matrix, cameraPos, xAxis, yAxis, center, halfWidth, halfHeight, color, alpha);
        BufferUploader.drawWithShader(buffer.end());
    }

    private static void drawQuad(Matrix4f matrix, Vec3 cameraPos, Vec3 xAxis, Vec3 yAxis,
                                  Vec3 center, float halfWidth, float halfHeight, int color, int alpha) {
        BufferBuilder buffer = Tesselator.getInstance().getBuilder();
        buffer.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_COLOR_TEX);
        addQuad(buffer, matrix, cameraPos, xAxis, yAxis, center, halfWidth, halfHeight, color, alpha);
        BufferUploader.drawWithShader(buffer.end());
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
        buffer.vertex(matrix, (float) pos.x, (float) pos.y, (float) pos.z)
                .color((color >> 16) & 255, (color >> 8) & 255, color & 255, Mth.clamp(alpha, 0, 255))
                .uv(u, v)
                .endVertex();
    }

    private static Vec3 lerp(Vec3 a, Vec3 b, float t) {
        return a.add(b.subtract(a).scale(t));
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
        float entityScale = (float) Mth.clamp(0.60D + entity.getBbWidth() * 0.38D + entity.getBbHeight() * 0.06D, 0.72D, 2.35D);
        return Mth.clamp(base, 0.20F, 6.0F) * entityScale;
    }

    private static float intensity(ActiveEntityVisualEffect effect) {
        return effect.data().contains("Intensity") ? Mth.clamp(effect.data().getFloat("Intensity"), 0.05F, 8.0F) : 1.20F;
    }

    private static int shardCount(ActiveEntityVisualEffect effect) {
        return effect.data().contains("ShardCount") ? Mth.clamp(effect.data().getInt("ShardCount"), 3, 14) : 7;
    }

    private static double orbitRadius(ActiveEntityVisualEffect effect, float scale) {
        if (effect.data().contains("OrbitRadius")) {
            return Mth.clamp(effect.data().getDouble("OrbitRadius"), 0.30D, 4.0D);
        }
        return scale * 0.92D;
    }

    private static double behindOffset(Entity entity, ActiveEntityVisualEffect effect) {
        if (effect.data().contains("BehindOffset")) {
            return Mth.clamp(effect.data().getDouble("BehindOffset"), -2.0D, 3.0D);
        }
        return Mth.clamp(0.18D + entity.getBbWidth() * 0.60D, 0.40D, 1.60D);
    }

    private static double yOffset(Entity entity, ActiveEntityVisualEffect effect) {
        if (effect.data().contains("YOffset")) {
            return Mth.clamp(effect.data().getDouble("YOffset"), -entity.getBbHeight(), entity.getBbHeight());
        }
        return 0.14D + entity.getBbHeight() * 0.10D;
    }

    private static float tiltDegrees(ActiveEntityVisualEffect effect) {
        if (effect.data().contains("TiltDegrees")) {
            return Mth.clamp(effect.data().getFloat("TiltDegrees"), -80.0F, 80.0F);
        }
        return 32.0F;
    }

    private static float distanceFade(double distance) {
        float fade = 1.0F - smoothstep((float) (TRACK_DISTANCE * 0.74D), (float) TRACK_DISTANCE, (float) distance);
        return Mth.clamp(fade, 0.0F, 1.0F);
    }

    private static float smoothstep(float edge0, float edge1, float value) {
        float x = Mth.clamp((value - edge0) / (edge1 - edge0), 0.0F, 1.0F);
        return x * x * (3.0F - 2.0F * x);
    }

    private record ShardPosition(Vec3 position, Vec3 radial, float angle, float scale) {
    }
}
