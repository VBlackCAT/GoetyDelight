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
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.client.event.RenderLevelStageEvent;
import net.minecraftforge.registries.ForgeRegistries;
import net.v_black_cat.goetydelight.entities.DollEntity;
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

        PlacementProfile placement = placement(entity);
        Vec3 faceForward = faceLook(entity, partialTick);

        Vec3 WORLD_UP = new Vec3(0.0D, 1.0D, 0.0D);
        Vec3 right = WORLD_UP.cross(faceForward);
        if (right.lengthSqr() < 1.0E-5D) {
            Vec3 horizontalForward = horizontalLook(entity, partialTick);
            right = new Vec3(horizontalForward.z, 0.0D, -horizontalForward.x);
        }
        right = right.normalize();

        Vec3 localUp = faceForward.cross(right).normalize();


        Vec3 baseAnchor = placement.anchor(entity, effect, partialTick);

        double neckDist = 0.15D;

        Vec3 neckPos = baseAnchor.subtract(0.0D, neckDist, 0.0D);

        Vec3 eyeCenter = neckPos
                .add(localUp.scale(neckDist))
                .add(faceForward.scale(placement.forwardOffset(entity, effect)))
                .add(right.scale(placement.sideOffset(entity, effect)));

        if (localUp.lengthSqr() < 1.0E-5D) {
            localUp = new Vec3(0.0D, 1.0D, 0.0D);
        } else {
            localUp = localUp.normalize();
        }
        Vec3 streakAxis = right.scale(streakAxisRightMix()).add(localUp.scale(streakAxisUpMix())).normalize();
        Vec3 streakUp = localUp.scale(streakUpUpMix()).add(right.scale(streakUpRightMix())).normalize();
        Matrix4f matrix = event.getPoseStack().last().pose();
        float scale = placement.scale(entity, effect);
        float intensity = intensity(effect);
        float distanceFade = distanceFade(Math.sqrt(distanceSqr));
        float frontFade = 1.0F;
        int alpha = (int) (distanceFade * frontFade * maxAlpha());
        if (alpha <= 0) {
            return;
        }

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
        drawBillboard(matrix, cameraPos, right, localUp, eyeCenter, auraHalfWidth(scale), auraHalfHeight(scale), 0xFF2600, alpha / 2);

        shader.safeGetUniform("EffectMode").set(4);
        drawLightning(matrix, cameraPos, streakAxis, streakUp, eyeCenter, renderTime, scale, alpha);

        shader.safeGetUniform("EffectMode").set(1);
        drawBillboard(matrix, cameraPos, streakAxis, streakUp, eyeCenter, beamHalfWidth(scale), beamHalfHeight(scale), 0xFF1200, alpha);

        shader.safeGetUniform("EffectMode").set(0);
        drawBillboard(matrix, cameraPos, right, localUp, eyeCenter, coreHalfWidth(scale), coreHalfHeight(scale), 0xFF1C00, Math.min(255, alpha + 20));
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

    private static PlacementProfile placement(Entity entity) {
        PlacementProfile temporaryProfile = temporaryProfile(entity);
        return temporaryProfile != null ? temporaryProfile : defaultProfile();
    }

    private static PlacementProfile temporaryProfile(Entity entity) {
        String typeId = entityTypeId(entity);

        return switch (typeId) {
            case "goetydelight:doll_entity" -> dollProfile();
            default -> null;
        };
    }

    private static String entityTypeId(Entity entity) {
        ResourceLocation id = ForgeRegistries.ENTITY_TYPES.getKey(entity.getType());
        return id != null ? id.toString() : "";
    }

    private static float intensity(ActiveEntityVisualEffect effect) {
        return effect.data().contains("Intensity") ? Mth.clamp(effect.data().getFloat("Intensity"), 0.05F, 8.0F) : 1.30F;
    }

    private static PlacementProfile defaultProfile() {
        return profile(
                "",
                0.07D, 1D, 0.27D, 0.275D,
                -0.12D,
                -0.005D,
                0.30D, 0.36D, 0.34D, 1.40D,
                false
        );
    }

    private static PlacementProfile dollProfile() {
        return profile(
                "Doll",
                -0.45D, 1D, -0.1D, 0.82D,
                -0.1D,
                -0.25D,
                0.26D, 0.5D, 0.30D, 1.18D,
                true
        );
    }

    private static PlacementProfile profile(String dataPrefix,
                                            double forwardBase,
                                            double forwardWidthScale,
                                            double minForward,
                                            double maxForward,
                                            double sideOffset,
                                            double eyeYOffset,
                                            double scaleBase,
                                            double scaleWidthScale,
                                            double minScale,
                                            double maxScale,
                                            boolean includeDisplayTranslation) {
        return new PlacementProfile(
                dataPrefix,
                forwardBase,
                forwardWidthScale,
                minForward,
                maxForward,
                sideOffset,
                eyeYOffset,
                scaleBase,
                scaleWidthScale,
                minScale,
                maxScale,
                includeDisplayTranslation
        );
    }

    private static float auraHalfWidth(float scale) {
        return scale * 1.72F;
    }

    private static float auraHalfHeight(float scale) {
        return scale * 1.22F;
    }

    private static float beamHalfWidth(float scale) {
        return scale * 4.90F;
    }

    private static float beamHalfHeight(float scale) {
        return scale * 0.32F;
    }

    private static float coreHalfWidth(float scale) {
        return scale * 0.62F;
    }

    private static float coreHalfHeight(float scale) {
        return scale * 0.44F;
    }

    private static double streakAxisRightMix() {
        return -0.98D;
    }

    private static double streakAxisUpMix() {
        return 0.24D;
    }

    private static double streakUpUpMix() {
        return 0.98D;
    }

    private static double streakUpRightMix() {
        return 0.24D;
    }

    private static float distanceFadeStartRatio() {
        return 0.72F;
    }

    private static float frontFadeStart() {
        return -0.10F;
    }

    private static float frontFadeEnd() {
        return 0.42F;
    }

    private static float maxAlpha() {
        return 230.0F;
    }
    // END TEMP red-eye tuning block.

    private static float distanceFade(double distance) {
        float fade = 1.0F - smoothstep((float) (TRACK_DISTANCE * distanceFadeStartRatio()), (float) TRACK_DISTANCE, (float) distance);
        return Mth.clamp(fade, 0.0F, 1.0F);
    }

    private static float frontFade(Vec3 faceForward, Vec3 center, Vec3 cameraPos) {
        Vec3 toCamera = cameraPos.subtract(center);
        if (toCamera.lengthSqr() < 1.0E-5D) {
            return 1.0F;
        }

        double facing = faceForward.dot(toCamera.normalize());
        return smoothstep(frontFadeStart(), frontFadeEnd(), (float) facing);
    }

    private static float smoothstep(float edge0, float edge1, float value) {
        float x = Mth.clamp((value - edge0) / (edge1 - edge0), 0.0F, 1.0F);
        return x * x * (3.0F - 2.0F * x);
    }

    private record PlacementProfile(String dataPrefix,
                                    double forwardBase,
                                    double forwardWidthScale,
                                    double minForward,
                                    double maxForward,
                                    double sideOffset,
                                    double eyeYOffset,
                                    double scaleBase,
                                    double scaleWidthScale,
                                    double minScale,
                                    double maxScale,
                                    boolean includeDisplayTranslation) {
        private Vec3 anchor(Entity entity, ActiveEntityVisualEffect effect, float partialTick) {
            double yOffset = doubleValue(effect, "EyeYOffset", eyeYOffset * entity.getBbHeight(), -entity.getBbHeight(), entity.getBbHeight());
            Vec3 anchor = entity.getEyePosition(partialTick).add(0.0D, yOffset, 0.0D);
            if (includeDisplayTranslation && entity instanceof DollEntity doll) {
                Vector3f translation = doll.getDisplayTranslation();
                anchor = anchor.add(translation.x(), translation.y(), translation.z());
            }
            return anchor;
        }

        private double forwardOffset(Entity entity, ActiveEntityVisualEffect effect) {
            double fallback = Mth.clamp(forwardBase + entity.getBbWidth() * forwardWidthScale, minForward, maxForward);
            return doubleValue(effect, "ForwardOffset", fallback, -2.0D, 2.0D);
        }

        private double sideOffset(Entity entity, ActiveEntityVisualEffect effect) {
            double limit = Math.max(0.15D, entity.getBbWidth());
            return doubleValue(effect, "SideOffset", sideOffset, -limit, limit);
        }

        private float scale(Entity entity, ActiveEntityVisualEffect effect) {
            float base = (float) doubleValue(effect, "Scale", 1.0D, 0.20D, 6.0D);
            double entityScale = Mth.clamp(scaleBase + entity.getBbWidth() * scaleWidthScale, minScale, maxScale);
            return (float) (base * entityScale);
        }

        private double doubleValue(ActiveEntityVisualEffect effect, String key, double fallback, double min, double max) {
            String prefixedKey = dataPrefix + key;
            if (!dataPrefix.isEmpty() && effect.data().contains(prefixedKey)) {
                return Mth.clamp(effect.data().getDouble(prefixedKey), min, max);
            }
            if (effect.data().contains(key)) {
                return Mth.clamp(effect.data().getDouble(key), min, max);
            }
            return fallback;
        }
    }
}
