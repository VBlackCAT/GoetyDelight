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
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NumericTag;
import net.minecraft.nbt.Tag;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.client.event.RenderLevelStageEvent;
import net.v_black_cat.goetydelight.render.test.ModShaderReg;
import net.v_black_cat.goetydelight.visual.ActiveEntityVisualEffect;
import org.joml.Matrix4f;

public final class VolumetricFlameRenderer {
    private static final double TRACK_DISTANCE = 80.0D;

    private VolumetricFlameRenderer() {
    }

    public static void render(RenderLevelStageEvent event, Entity entity, ActiveEntityVisualEffect effect) {
        Camera camera = event.getCamera();
        Vec3 cameraPos = camera.getPosition();
        double distanceSqr = entity.distanceToSqr(cameraPos);
        if (distanceSqr > TRACK_DISTANCE * TRACK_DISTANCE) {
            return;
        }

        float partialTick = event.getPartialTick();
        Vec3 entityPos = entity.getPosition(partialTick);
        float scale = scale(effect);
        double halfX = width(entity, effect) * scale;
        double halfY = height(entity, effect) * scale * 0.5D;
        double halfZ = depth(entity, effect) * scale;
        Vec3 center = entityPos.add(0.0D, halfY - 0.04D * scale, 0.0D);
        Vec3 cameraLocal = new Vec3(
                (cameraPos.x - center.x) / halfX,
                (cameraPos.y - center.y) / halfY,
                (cameraPos.z - center.z) / halfZ
        );

        ShaderInstance shader = ModShaderReg.getVolumetricFlameShader();
        shader.safeGetUniform("iTime").set((entity.level().getGameTime() + partialTick) / 20.0F);
        shader.safeGetUniform("intensity").set(intensity(effect));
        shader.safeGetUniform("CameraLocal").set((float) cameraLocal.x, (float) cameraLocal.y, (float) cameraLocal.z);
        uploadColors(shader, effect.data());

        Matrix4f oldProjection = new Matrix4f(RenderSystem.getProjectionMatrix());
        VertexSorting oldSorting = RenderSystem.getVertexSorting();
        RenderSystem.setProjectionMatrix(event.getProjectionMatrix(), VertexSorting.DISTANCE_TO_ORIGIN);

        RenderSystem.setShader(() -> shader);
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        RenderSystem.enableDepthTest();
        RenderSystem.depthMask(false);
        RenderSystem.enableBlend();
        RenderSystem.blendFuncSeparate(770, 1, 1, 1);
        RenderSystem.enableCull();

        drawVolumeBox(event.getPoseStack().last().pose(), cameraPos, center, halfX, halfY, halfZ, alpha(distanceSqr));

        RenderSystem.disableBlend();
        RenderSystem.depthMask(true);
        RenderSystem.setProjectionMatrix(oldProjection, oldSorting);
    }

    private static void drawVolumeBox(Matrix4f matrix, Vec3 cameraPos, Vec3 center, double halfX, double halfY, double halfZ, int alpha) {
        BufferBuilder buffer = Tesselator.getInstance().getBuilder();
        buffer.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_COLOR_TEX);

        putFace(buffer, matrix, cameraPos, center, halfX, halfY, halfZ, alpha,
                -1.0F, -1.0F, 1.0F,
                1.0F, -1.0F, 1.0F,
                1.0F, 1.0F, 1.0F,
                -1.0F, 1.0F, 1.0F);
        putFace(buffer, matrix, cameraPos, center, halfX, halfY, halfZ, alpha,
                1.0F, -1.0F, -1.0F,
                -1.0F, -1.0F, -1.0F,
                -1.0F, 1.0F, -1.0F,
                1.0F, 1.0F, -1.0F);
        putFace(buffer, matrix, cameraPos, center, halfX, halfY, halfZ, alpha,
                1.0F, -1.0F, 1.0F,
                1.0F, -1.0F, -1.0F,
                1.0F, 1.0F, -1.0F,
                1.0F, 1.0F, 1.0F);
        putFace(buffer, matrix, cameraPos, center, halfX, halfY, halfZ, alpha,
                -1.0F, -1.0F, -1.0F,
                -1.0F, -1.0F, 1.0F,
                -1.0F, 1.0F, 1.0F,
                -1.0F, 1.0F, -1.0F);
        putFace(buffer, matrix, cameraPos, center, halfX, halfY, halfZ, alpha,
                -1.0F, 1.0F, 1.0F,
                1.0F, 1.0F, 1.0F,
                1.0F, 1.0F, -1.0F,
                -1.0F, 1.0F, -1.0F);
        putFace(buffer, matrix, cameraPos, center, halfX, halfY, halfZ, alpha,
                -1.0F, -1.0F, -1.0F,
                1.0F, -1.0F, -1.0F,
                1.0F, -1.0F, 1.0F,
                -1.0F, -1.0F, 1.0F);

        BufferUploader.drawWithShader(buffer.end());
    }

    private static void putFace(BufferBuilder buffer, Matrix4f matrix, Vec3 cameraPos, Vec3 center, double halfX, double halfY, double halfZ, int alpha,
                                float ax, float ay, float az, float bx, float by, float bz, float cx, float cy, float cz, float dx, float dy, float dz) {
        putVertex(buffer, matrix, cameraPos, center, halfX, halfY, halfZ, ax, ay, az, 0.0F, 1.0F, alpha);
        putVertex(buffer, matrix, cameraPos, center, halfX, halfY, halfZ, bx, by, bz, 1.0F, 1.0F, alpha);
        putVertex(buffer, matrix, cameraPos, center, halfX, halfY, halfZ, cx, cy, cz, 1.0F, 0.0F, alpha);
        putVertex(buffer, matrix, cameraPos, center, halfX, halfY, halfZ, dx, dy, dz, 0.0F, 0.0F, alpha);
    }

    private static void putVertex(BufferBuilder buffer, Matrix4f matrix, Vec3 cameraPos, Vec3 center, double halfX, double halfY, double halfZ,
                                  float localX, float localY, float localZ, float u, float v, int alpha) {
        Vec3 pos = center.add(localX * halfX, localY * halfY, localZ * halfZ).subtract(cameraPos);
        buffer.vertex(matrix, (float) pos.x, (float) pos.y, (float) pos.z)
                .color(encodeLocal(localX), encodeLocal(localY), encodeLocal(localZ), Mth.clamp(alpha, 0, 255))
                .uv(u, v)
                .endVertex();
    }

    private static int encodeLocal(float value) {
        return Mth.clamp((int) ((value * 0.5F + 0.5F) * 255.0F), 0, 255);
    }

    private static double width(Entity entity, ActiveEntityVisualEffect effect) {
        if (effect.data().contains("Width")) {
            return Mth.clamp(effect.data().getDouble("Width"), 0.25D, 5.0D);
        }
        return Math.max(0.58D, entity.getBbWidth() * 0.98D);
    }

    private static double depth(Entity entity, ActiveEntityVisualEffect effect) {
        if (effect.data().contains("Depth")) {
            return Mth.clamp(effect.data().getDouble("Depth"), 0.25D, 5.0D);
        }
        return Math.max(0.58D, entity.getBbWidth() * 0.98D);
    }

    private static double height(Entity entity, ActiveEntityVisualEffect effect) {
        if (effect.data().contains("Height")) {
            return Mth.clamp(effect.data().getDouble("Height"), 0.5D, 8.0D);
        }
        return Math.max(1.85D, entity.getBbHeight() * 1.42D);
    }

    private static float scale(ActiveEntityVisualEffect effect) {
        return effect.data().contains("Scale") ? Mth.clamp(effect.data().getFloat("Scale"), 0.10F, 6.0F) : 1.0F;
    }

    private static float intensity(ActiveEntityVisualEffect effect) {
        return effect.data().contains("Intensity") ? Mth.clamp(effect.data().getFloat("Intensity"), 0.05F, 8.0F) : 1.16F;
    }

    private static void uploadColors(ShaderInstance shader, CompoundTag data) {
        Color base = color(data, "Color", new Color(1.0F, 0.26F, 0.035F));
        Color core = color(data, "CoreColor", deriveCore(base));
        Color tip = color(data, "TipColor", deriveTip(base));
        Color smoke = color(data, "SmokeColor", deriveSmoke(base));

        shader.safeGetUniform("FlameColor").set(base.red, base.green, base.blue);
        shader.safeGetUniform("CoreColor").set(core.red, core.green, core.blue);
        shader.safeGetUniform("TipColor").set(tip.red, tip.green, tip.blue);
        shader.safeGetUniform("SmokeColor").set(smoke.red, smoke.green, smoke.blue);
    }

    private static Color color(CompoundTag data, String key, Color fallback) {
        if (!data.contains(key)) {
            return fallback;
        }

        Tag tag = data.get(key);
        if (tag instanceof NumericTag) {
            return fromRgbInt(data.getInt(key));
        }

        if (tag instanceof ListTag list && list.size() >= 3) {
            return new Color(
                    channel(list.get(0)),
                    channel(list.get(1)),
                    channel(list.get(2))
            );
        }

        return fallback;
    }

    private static float channel(Tag tag) {
        if (tag instanceof NumericTag numericTag) {
            float value = numericTag.getAsFloat();
            return Mth.clamp(value > 1.0F ? value / 255.0F : value, 0.0F, 1.0F);
        }

        return 1.0F;
    }

    private static Color fromRgbInt(int rgb) {
        return new Color(
                ((rgb >> 16) & 255) / 255.0F,
                ((rgb >> 8) & 255) / 255.0F,
                (rgb & 255) / 255.0F
        );
    }

    private static Color deriveCore(Color base) {
        return new Color(
                Mth.clamp(base.red * 0.55F + 0.45F, 0.0F, 1.0F),
                Mth.clamp(base.green * 0.55F + 0.45F, 0.0F, 1.0F),
                Mth.clamp(base.blue * 0.55F + 0.30F, 0.0F, 1.0F)
        );
    }

    private static Color deriveTip(Color base) {
        return new Color(
                Mth.clamp(base.blue * 0.78F + 0.04F, 0.0F, 1.0F),
                Mth.clamp(base.green * 0.52F + base.blue * 0.22F, 0.0F, 1.0F),
                Mth.clamp(base.red * 0.28F + base.blue * 0.82F, 0.0F, 1.0F)
        );
    }

    private static Color deriveSmoke(Color base) {
        return new Color(
                Mth.clamp(base.red * 0.08F, 0.0F, 1.0F),
                Mth.clamp(base.green * 0.12F, 0.0F, 1.0F),
                Mth.clamp(base.blue * 0.18F + 0.10F, 0.0F, 1.0F)
        );
    }

    private static int alpha(double distanceSqr) {
        double distance = Math.sqrt(distanceSqr);
        float fade = 1.0F - smoothstep((float) (TRACK_DISTANCE * 0.78D), (float) TRACK_DISTANCE, (float) distance);
        return (int) (Mth.clamp(fade, 0.0F, 1.0F) * 225.0F);
    }

    private static float smoothstep(float edge0, float edge1, float value) {
        float x = Mth.clamp((value - edge0) / (edge1 - edge0), 0.0F, 1.0F);
        return x * x * (3.0F - 2.0F * x);
    }

    private record Color(float red, float green, float blue) {
    }
}
