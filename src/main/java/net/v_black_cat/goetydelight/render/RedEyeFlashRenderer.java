package net.v_black_cat.goetydelight.render;

import com.mojang.blaze3d.shaders.Uniform;
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
import net.v_black_cat.goetydelight.entities.DollEntity;
import net.v_black_cat.goetydelight.entities.ModEntities;
import net.v_black_cat.goetydelight.render.test.ModShaderReg;
import net.v_black_cat.goetydelight.visual.ActiveEntityVisualEffect;
import org.joml.Matrix4f;
import org.joml.Vector3f;

public final class RedEyeFlashRenderer {
    private static final double TRACK_DISTANCE = 80.0D;

    // ── 缓存对象（线程安全） ──
    private static final ThreadLocal<Matrix4f> OLD_PROJECTION = ThreadLocal.withInitial(Matrix4f::new);

    // Shader uniform 缓存（全局）
    private static ShaderInstance cachedShader;
    private static Uniform timeUniform;
    private static Uniform intensityUniform;

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

        // ── 计算眼睛中心坐标（分量形式） ──
        double[] anchor = placement.anchorComponents(entity, effect, partialTick);
        double neckDist = 0.15D;
        double neckX = anchor[0];
        double neckY = anchor[1] - neckDist;
        double neckZ = anchor[2];

        double forwardOff = placement.forwardOffset(entity, effect);
        double sideOff = placement.sideOffset(entity, effect);

        double eyeX = neckX
                + localUp.x * neckDist
                + faceForward.x * forwardOff
                + right.x * sideOff;
        double eyeY = neckY
                + localUp.y * neckDist
                + faceForward.y * forwardOff
                + right.y * sideOff;
        double eyeZ = neckZ
                + localUp.z * neckDist
                + faceForward.z * forwardOff
                + right.z * sideOff;

        // 确保 localUp 有效
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
        ensureShaderCache(shader);
        timeUniform.set(renderTime / 20.0F);
        intensityUniform.set(intensity);

        Matrix4f oldProj = OLD_PROJECTION.get();
        oldProj.set(RenderSystem.getProjectionMatrix());
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
        drawBillboard(matrix, cameraPos, right, localUp, eyeX, eyeY, eyeZ, auraHalfWidth(scale), auraHalfHeight(scale), 0xFF2600, alpha / 2);

        shader.safeGetUniform("EffectMode").set(4);
        drawLightning(matrix, cameraPos, streakAxis, streakUp, eyeX, eyeY, eyeZ, renderTime, scale, alpha);

        shader.safeGetUniform("EffectMode").set(1);
        drawBillboard(matrix, cameraPos, streakAxis, streakUp, eyeX, eyeY, eyeZ, beamHalfWidth(scale), beamHalfHeight(scale), 0xFF1200, alpha);

        shader.safeGetUniform("EffectMode").set(0);
        drawBillboard(matrix, cameraPos, right, localUp, eyeX, eyeY, eyeZ, coreHalfWidth(scale), coreHalfHeight(scale), 0xFF1C00, Math.min(255, alpha + 20));

        shader.safeGetUniform("EffectMode").set(3);
        drawGlints(matrix, cameraPos, streakAxis, streakUp, eyeX, eyeY, eyeZ, renderTime, scale, alpha);

        RenderSystem.enableCull();
        RenderSystem.disableBlend();
        RenderSystem.depthMask(true);
        RenderSystem.setProjectionMatrix(oldProj, oldSorting);
    }

    private static void ensureShaderCache(ShaderInstance shader) {
        if (cachedShader != shader) {
            cachedShader = shader;
            timeUniform = shader.getUniform("iTime");
            intensityUniform = shader.getUniform("intensity");
        }
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

    // ── drawGlints 使用分量坐标 ──
    private static void drawGlints(Matrix4f matrix, Vec3 cameraPos, Vec3 cameraLeft, Vec3 cameraUp,
                                   double cx, double cy, double cz, float renderTime, float scale, int alpha) {
        for (int i = 0; i < 5; i++) {
            float side = i - 2.0F;
            float wave = Mth.sin(renderTime * 0.37F + i * 1.91F) * 0.18F;
            double offset = (side * 0.48F + wave) * scale;
            double gx = cx + cameraLeft.x * offset;
            double gy = cy + cameraLeft.y * offset;
            double gz = cz + cameraLeft.z * offset;
            int glintAlpha = (int) (alpha * (0.42F - Math.abs(side) * 0.055F));
            drawBillboard(matrix, cameraPos, cameraLeft, cameraUp, gx, gy, gz, scale * 0.20F, scale * 0.12F, 0xFF3A08, glintAlpha);
        }
    }

    // ── drawLightning 使用分量坐标 ──
    private static void drawLightning(Matrix4f matrix, Vec3 cameraPos, Vec3 axis, Vec3 up,
                                      double cx, double cy, double cz, float renderTime, float scale, int alpha) {
        BufferBuilder buffer = Tesselator.getInstance().getBuilder();
        buffer.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_COLOR_TEX);

        int segments = 10;
        // previous 坐标分量
        double prevX = cx + axis.x * (-0.06D * scale);
        double prevY = cy + axis.y * (-0.06D * scale);
        double prevZ = cz + axis.z * (-0.06D * scale);
        float prevWidth = scale * 0.090F;

        for (int i = 1; i <= segments; i++) {
            float t = i / (float) segments;
            float prevT = (i - 1) / (float) segments;
            double bend = (Mth.sin(renderTime * 0.23F + i * 1.77F) * 0.20D
                    + Mth.sin(renderTime * 0.53F + i * 2.91F) * 0.10D) * scale * (0.30D + t);
            double nextX = cx + up.x * ((0.10D + t * 2.28D) * scale) + axis.x * bend;
            double nextY = cy + up.y * ((0.10D + t * 2.28D) * scale) + axis.y * bend;
            double nextZ = cz + up.z * ((0.10D + t * 2.28D) * scale) + axis.z * bend;
            float width = scale * Mth.lerp(t, 0.090F, 0.030F);
            int segmentAlpha = (int) (alpha * (1.0F - t * 0.54F));
            addLightningSegment(buffer, matrix, cameraPos, axis, up,
                    prevX, prevY, prevZ, nextX, nextY, nextZ,
                    prevWidth, width, prevT, t, segmentAlpha);
            prevX = nextX;
            prevY = nextY;
            prevZ = nextZ;
            prevWidth = width;
        }

        // 分支
        double branchStartX = cx + up.x * (0.86D * scale) + axis.x * (0.12D * scale);
        double branchStartY = cy + up.y * (0.86D * scale) + axis.y * (0.12D * scale);
        double branchStartZ = cz + up.z * (0.86D * scale) + axis.z * (0.12D * scale);

        double branchMidX = branchStartX + up.x * (0.32D * scale) + axis.x * (-0.30D * scale);
        double branchMidY = branchStartY + up.y * (0.32D * scale) + axis.y * (-0.30D * scale);
        double branchMidZ = branchStartZ + up.z * (0.32D * scale) + axis.z * (-0.30D * scale);

        double branchEndX = branchMidX + up.x * (0.34D * scale) + axis.x * (0.16D * scale);
        double branchEndY = branchMidY + up.y * (0.34D * scale) + axis.y * (0.16D * scale);
        double branchEndZ = branchMidZ + up.z * (0.34D * scale) + axis.z * (0.16D * scale);

        addLightningSegment(buffer, matrix, cameraPos, axis, up,
                branchStartX, branchStartY, branchStartZ,
                branchMidX, branchMidY, branchMidZ,
                scale * 0.040F, scale * 0.025F, 0.0F, 0.5F, alpha / 2);
        addLightningSegment(buffer, matrix, cameraPos, axis, up,
                branchMidX, branchMidY, branchMidZ,
                branchEndX, branchEndY, branchEndZ,
                scale * 0.025F, scale * 0.012F, 0.5F, 1.0F, alpha / 3);

        BufferUploader.drawWithShader(buffer.end());
    }

    private static void addLightningSegment(BufferBuilder buffer, Matrix4f matrix, Vec3 cameraPos, Vec3 axis, Vec3 up,
                                            double sx, double sy, double sz,
                                            double ex, double ey, double ez,
                                            float startWidth, float endWidth, float startU, float endU, int alpha) {
        double tx = ex - sx;
        double ty = ey - sy;
        double tz = ez - sz;
        // viewNormal = axis × up
        double vnx = axis.y * up.z - axis.z * up.y;
        double vny = axis.z * up.x - axis.x * up.z;
        double vnz = axis.x * up.y - axis.y * up.x;
        // side = viewNormal × tangent
        double sideX = vny * tz - vnz * ty;
        double sideY = vnz * tx - vnx * tz;
        double sideZ = vnx * ty - vny * tx;
        double len = Math.sqrt(sideX * sideX + sideY * sideY + sideZ * sideZ);
        if (len < 1.0E-5D) {
            sideX = axis.x;
            sideY = axis.y;
            sideZ = axis.z;
        } else {
            double invLen = 1.0 / len;
            sideX *= invLen;
            sideY *= invLen;
            sideZ *= invLen;
        }

        double startSideX = sideX * startWidth;
        double startSideY = sideY * startWidth;
        double startSideZ = sideZ * startWidth;
        double endSideX = sideX * endWidth;
        double endSideY = sideY * endWidth;
        double endSideZ = sideZ * endWidth;

        int startAlpha = Mth.clamp(alpha, 0, 255);
        int endAlpha = Mth.clamp((int) (alpha * 0.82F), 0, 255);
        int color = 0xFF1200;

        putVertex(buffer, matrix,
                sx - startSideX - cameraPos.x,
                sy - startSideY - cameraPos.y,
                sz - startSideZ - cameraPos.z,
                color, startAlpha, startU, 0.0F);
        putVertex(buffer, matrix,
                sx + startSideX - cameraPos.x,
                sy + startSideY - cameraPos.y,
                sz + startSideZ - cameraPos.z,
                color, startAlpha, startU, 1.0F);
        putVertex(buffer, matrix,
                ex + endSideX - cameraPos.x,
                ey + endSideY - cameraPos.y,
                ez + endSideZ - cameraPos.z,
                color, endAlpha, endU, 1.0F);
        putVertex(buffer, matrix,
                ex - endSideX - cameraPos.x,
                ey - endSideY - cameraPos.y,
                ez - endSideZ - cameraPos.z,
                color, endAlpha, endU, 0.0F);
    }

    // ── drawBillboard 使用分量坐标 ──
    private static void drawBillboard(Matrix4f matrix, Vec3 cameraPos, Vec3 xAxis, Vec3 yAxis,
                                      double cx, double cy, double cz,
                                      float halfWidth, float halfHeight, int color, int alpha) {
        double dxX = xAxis.x * halfWidth;
        double dxY = xAxis.y * halfWidth;
        double dxZ = xAxis.z * halfWidth;
        double dyX = yAxis.x * halfHeight;
        double dyY = yAxis.y * halfHeight;
        double dyZ = yAxis.z * halfHeight;

        BufferBuilder buffer = Tesselator.getInstance().getBuilder();
        buffer.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_COLOR_TEX);

        putVertex(buffer, matrix,
                cx - dxX - dyX - cameraPos.x,
                cy - dxY - dyY - cameraPos.y,
                cz - dxZ - dyZ - cameraPos.z,
                color, alpha, 0.0F, 1.0F);
        putVertex(buffer, matrix,
                cx + dxX - dyX - cameraPos.x,
                cy + dxY - dyY - cameraPos.y,
                cz + dxZ - dyZ - cameraPos.z,
                color, alpha, 1.0F, 1.0F);
        putVertex(buffer, matrix,
                cx + dxX + dyX - cameraPos.x,
                cy + dxY + dyY - cameraPos.y,
                cz + dxZ + dyZ - cameraPos.z,
                color, alpha, 1.0F, 0.0F);
        putVertex(buffer, matrix,
                cx - dxX + dyX - cameraPos.x,
                cy - dxY + dyY - cameraPos.y,
                cz - dxZ + dyZ - cameraPos.z,
                color, alpha, 0.0F, 0.0F);

        BufferUploader.drawWithShader(buffer.end());
    }

    // ── putVertex 接受原始坐标 ──
    private static void putVertex(BufferBuilder buffer, Matrix4f matrix,
                                  double x, double y, double z,
                                  int color, int alpha, float u, float v) {
        buffer.vertex(matrix, (float) x, (float) y, (float) z)
                .color((color >> 16) & 255, (color >> 8) & 255, color & 255, Mth.clamp(alpha, 0, 255))
                .uv(u, v)
                .endVertex();
    }

    // ── PlacementProfile 相关（使用类型比较替代字符串） ──
    private static PlacementProfile placement(Entity entity) {
        if (entity.getType() == ModEntities.DOLL_ENTITY.get()) {
            return dollProfile();
        }
        return defaultProfile();
    }

    private static float intensity(ActiveEntityVisualEffect effect) {
        return effect.data().contains("Intensity") ? Mth.clamp(effect.data().getFloat("Intensity"), 0.05F, 8.0F) : 1.30F;
    }

    private static PlacementProfile defaultProfile() {
        return profile(
                "", 0.07D, 1D, 0.27D, 0.275D,
                -0.12D, -0.005D,
                0.30D, 0.36D, 0.34D, 1.40D, false
        );
    }

    private static PlacementProfile dollProfile() {
        return profile(
                "Doll", -0.45D, 1D, -0.1D, 0.82D,
                -0.1D, -0.25D,
                0.26D, 0.5D, 0.30D, 1.18D, true
        );
    }

    private static PlacementProfile profile(String dataPrefix,
                                            double forwardBase, double forwardWidthScale,
                                            double minForward, double maxForward,
                                            double sideOffset, double eyeYOffset,
                                            double scaleBase, double scaleWidthScale,
                                            double minScale, double maxScale,
                                            boolean includeDisplayTranslation) {
        return new PlacementProfile(dataPrefix, forwardBase, forwardWidthScale, minForward, maxForward,
                sideOffset, eyeYOffset, scaleBase, scaleWidthScale, minScale, maxScale, includeDisplayTranslation);
    }

    // ── 尺寸辅助 ──
    private static float auraHalfWidth(float scale) { return scale * 1.72F; }
    private static float auraHalfHeight(float scale) { return scale * 1.22F; }
    private static float beamHalfWidth(float scale) { return scale * 4.90F; }
    private static float beamHalfHeight(float scale) { return scale * 0.32F; }
    private static float coreHalfWidth(float scale) { return scale * 0.62F; }
    private static float coreHalfHeight(float scale) { return scale * 0.44F; }
    private static double streakAxisRightMix() { return -0.98D; }
    private static double streakAxisUpMix() { return 0.24D; }
    private static double streakUpUpMix() { return 0.98D; }
    private static double streakUpRightMix() { return 0.24D; }
    private static float distanceFadeStartRatio() { return 0.72F; }
    private static float frontFadeStart() { return -0.10F; }
    private static float frontFadeEnd() { return 0.42F; }
    private static float maxAlpha() { return 230.0F; }

    private static float distanceFade(double distance) {
        float fade = 1.0F - smoothstep((float) (TRACK_DISTANCE * distanceFadeStartRatio()), (float) TRACK_DISTANCE, (float) distance);
        return Mth.clamp(fade, 0.0F, 1.0F);
    }

    private static float smoothstep(float edge0, float edge1, float value) {
        float x = Mth.clamp((value - edge0) / (edge1 - edge0), 0.0F, 1.0F);
        return x * x * (3.0F - 2.0F * x);
    }

    // ── PlacementProfile 内部记录 ──
    private record PlacementProfile(String dataPrefix,
                                    double forwardBase, double forwardWidthScale,
                                    double minForward, double maxForward,
                                    double sideOffset, double eyeYOffset,
                                    double scaleBase, double scaleWidthScale,
                                    double minScale, double maxScale,
                                    boolean includeDisplayTranslation) {

        // 返回 [x, y, z] 分量数组，避免创建 Vec3
        double[] anchorComponents(Entity entity, ActiveEntityVisualEffect effect, float partialTick) {
            double yOff = doubleValue(effect, "EyeYOffset", eyeYOffset * entity.getBbHeight(), -entity.getBbHeight(), entity.getBbHeight());
            Vec3 eyePos = entity.getEyePosition(partialTick);
            double ax = eyePos.x;
            double ay = eyePos.y + yOff;
            double az = eyePos.z;
            if (includeDisplayTranslation && entity instanceof DollEntity doll) {
                Vector3f trans = doll.getDisplayTranslation();
                ax += trans.x();
                ay += trans.y();
                az += trans.z();
            }
            return new double[]{ax, ay, az};
        }

        double forwardOffset(Entity entity, ActiveEntityVisualEffect effect) {
            double fallback = Mth.clamp(forwardBase + entity.getBbWidth() * forwardWidthScale, minForward, maxForward);
            return doubleValue(effect, "ForwardOffset", fallback, -2.0D, 2.0D);
        }

        double sideOffset(Entity entity, ActiveEntityVisualEffect effect) {
            double limit = Math.max(0.15D, entity.getBbWidth());
            return doubleValue(effect, "SideOffset", sideOffset, -limit, limit);
        }

        float scale(Entity entity, ActiveEntityVisualEffect effect) {
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