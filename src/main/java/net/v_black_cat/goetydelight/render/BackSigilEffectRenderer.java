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

public final class BackSigilEffectRenderer {
    private static final double TRACK_DISTANCE = 96.0D;
    private static final Vec3 WORLD_UP = new Vec3(0.0D, 1.0D, 0.0D);

    private BackSigilEffectRenderer() {
    }

    public static void renderDoomCorona(RenderLevelStageEvent event, Entity entity, ActiveEntityVisualEffect effect) {
        renderBackPlane(event, entity, effect, 0, 0xFFB13A, 1.50F, 0.68D, 1.12D);
    }

    public static void renderAbyssalRiftEye(RenderLevelStageEvent event, Entity entity, ActiveEntityVisualEffect effect) {
        renderBackPlane(event, entity, effect, 1, 0xA80BFF, 1.38F, 0.58D, 1.18D);
    }

    public static void renderHolyJudgementHalo(RenderLevelStageEvent event, Entity entity, ActiveEntityVisualEffect effect) {
        renderBackPlane(event, entity, effect, 2, 0xFFE9B2, 1.18F, 0.52D, 1.08D);
    }

    public static void renderAstralCrown(RenderLevelStageEvent event, Entity entity, ActiveEntityVisualEffect effect) {
        renderBackPlane(event, entity, effect, 3, 0x86DAFF, 1.24F, 0.48D, 1.14D);
    }

    public static void renderBloodMoonBackwheel(RenderLevelStageEvent event, Entity entity, ActiveEntityVisualEffect effect) {
        renderBackPlane(event, entity, effect, 4, 0xD01822, 1.56F, 0.72D, 1.12D);
    }

    public static void renderCausalChains(RenderLevelStageEvent event, Entity entity, ActiveEntityVisualEffect effect) {
        renderChains(event, entity, effect);
    }

    public static void renderInvertedCrossMark(RenderLevelStageEvent event, Entity entity, ActiveEntityVisualEffect effect) {
        renderBackPlane(event, entity, effect, 6, 0xB023FF, 1.34F, 0.60D, 1.18D);
    }

    private static void renderBackPlane(RenderLevelStageEvent event, Entity entity, ActiveEntityVisualEffect effect, int mode, int color, float scaleMul, double behindMul, double yMul) {
        RenderBasis basis = basis(entity, event.getPartialTick(), effect, scaleMul, behindMul, yMul);
        float intensity = intensity(effect);
        int alpha = alpha(entity, event.getCamera());
        if (alpha <= 0) {
            return;
        }

        ShaderInstance shader = ModShaderReg.getBackSigilEffectShader();
        shader.safeGetUniform("iTime").set((entity.level().getGameTime() + event.getPartialTick()) / 20.0F);
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

        shader.safeGetUniform("EffectMode").set(7);
        drawPlane(event.getPoseStack().last().pose(), event.getCamera().getPosition(), basis.x, basis.y, basis.center, basis.scale * 1.35F, basis.scale * 1.35F, color, alpha / 2);

        shader.safeGetUniform("EffectMode").set(mode);
        drawPlane(event.getPoseStack().last().pose(), event.getCamera().getPosition(), basis.x, basis.y, basis.center, basis.scale, basis.scale, color, alpha);

        if (mode == 0 || mode == 3) {
            shader.safeGetUniform("EffectMode").set(8);
            drawOrbitingShards(event, basis, color, alpha, mode == 0 ? 10 : 14);
        }

        RenderSystem.enableCull();
        RenderSystem.disableBlend();
        RenderSystem.depthMask(true);
        RenderSystem.setProjectionMatrix(oldProjection, oldSorting);
    }

    private static void renderChains(RenderLevelStageEvent event, Entity entity, ActiveEntityVisualEffect effect) {
        RenderBasis basis = basis(entity, event.getPartialTick(), effect, 1.22F, 0.55D, 0.92D);
        int alpha = alpha(entity, event.getCamera());
        if (alpha <= 0) {
            return;
        }

        ShaderInstance shader = ModShaderReg.getBackSigilEffectShader();
        shader.safeGetUniform("iTime").set((entity.level().getGameTime() + event.getPartialTick()) / 20.0F);
        shader.safeGetUniform("intensity").set(intensity(effect));

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

        shader.safeGetUniform("EffectMode").set(5);
        drawChainBands(event, basis, alpha);

        shader.safeGetUniform("EffectMode").set(8);
        drawOrbitingShards(event, basis, 0xBA6DFF, alpha, 8);

        RenderSystem.enableCull();
        RenderSystem.disableBlend();
        RenderSystem.depthMask(true);
        RenderSystem.setProjectionMatrix(oldProjection, oldSorting);
    }

    private static void drawChainBands(RenderLevelStageEvent event, RenderBasis basis, int alpha) {
        Matrix4f matrix = event.getPoseStack().last().pose();
        Vec3 cameraPos = event.getCamera().getPosition();
        float time = event.getRenderTick() + event.getPartialTick();
        BufferBuilder buffer = Tesselator.getInstance().getBuilder();
        buffer.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_COLOR_TEX);

        for (int band = 0; band < 3; band++) {
            double bandHeight = (band - 1) * basis.scale * 0.34D;
            float phase = time * (0.025F + band * 0.006F) + band * 2.0943952F;
            for (int i = 0; i < 44; i++) {
                float t0 = i / 44.0F;
                float t1 = (i + 1) / 44.0F;
                addChainSegment(buffer, matrix, cameraPos, basis, phase, bandHeight, t0, t1, alpha);
            }
        }

        BufferUploader.drawWithShader(buffer.end());
    }

    private static void addChainSegment(BufferBuilder buffer, Matrix4f matrix, Vec3 cameraPos, RenderBasis basis, float phase, double bandHeight, float t0, float t1, int alpha) {
        ChainSample a = sampleChain(basis, phase, bandHeight, t0);
        ChainSample b = sampleChain(basis, phase, bandHeight, t1);
        Vec3 sideA = a.radial.scale(basis.scale * 0.030D);
        Vec3 sideB = b.radial.scale(basis.scale * 0.030D);
        int color = 0xBA6DFF;
        int alphaA = (int) (alpha * (0.42F + 0.30F * Mth.sin((t0 + phase) * 6.2831855F)));
        int alphaB = (int) (alpha * (0.42F + 0.30F * Mth.sin((t1 + phase) * 6.2831855F)));

        putVertex(buffer, matrix, a.position.add(sideA).subtract(cameraPos), color, alphaA, t0, 0.0F);
        putVertex(buffer, matrix, a.position.subtract(sideA).subtract(cameraPos), color, alphaA, t0, 1.0F);
        putVertex(buffer, matrix, b.position.subtract(sideB).subtract(cameraPos), color, alphaB, t1, 1.0F);
        putVertex(buffer, matrix, b.position.add(sideB).subtract(cameraPos), color, alphaB, t1, 0.0F);
    }

    private static ChainSample sampleChain(RenderBasis basis, float phase, double bandHeight, float t) {
        float angle = t * Mth.TWO_PI + phase;
        Vec3 radial = basis.x.scale(Mth.cos(angle)).add(basis.forward.scale(Mth.sin(angle))).normalize();
        Vec3 lift = basis.y.scale(bandHeight + Math.sin(angle * 2.0D + phase) * basis.scale * 0.08D);
        Vec3 position = basis.center.add(radial.scale(basis.scale * 0.82D)).add(lift);
        return new ChainSample(position, radial);
    }

    private static void drawOrbitingShards(RenderLevelStageEvent event, RenderBasis basis, int color, int alpha, int count) {
        Matrix4f matrix = event.getPoseStack().last().pose();
        Vec3 cameraPos = event.getCamera().getPosition();
        float renderTime = event.getRenderTick() + event.getPartialTick();
        BufferBuilder buffer = Tesselator.getInstance().getBuilder();
        buffer.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_COLOR_TEX);

        for (int i = 0; i < count; i++) {
            float angle = (float) (i * Math.PI * 2.0D / count) + renderTime * 0.018F * (i % 2 == 0 ? 1.0F : -0.72F);
            float pulse = 0.62F + 0.38F * Mth.sin(renderTime * 0.11F + i * 1.47F);
            Vec3 radial = basis.x.scale(Mth.cos(angle)).add(basis.y.scale(Mth.sin(angle))).normalize();
            Vec3 tangent = basis.x.scale(-Mth.sin(angle)).add(basis.y.scale(Mth.cos(angle))).normalize();
            Vec3 center = basis.center.add(radial.scale(basis.scale * (0.82D + pulse * 0.10D)));
            addQuad(buffer, matrix, cameraPos, tangent, radial, center, basis.scale * (0.035F + pulse * 0.018F), basis.scale * 0.018F, color, (int) (alpha * (0.35F + pulse * 0.35F)));
        }

        BufferUploader.drawWithShader(buffer.end());
    }

    private static void drawPlane(Matrix4f matrix, Vec3 cameraPos, Vec3 xAxis, Vec3 yAxis, Vec3 center, float halfWidth, float halfHeight, int color, int alpha) {
        BufferBuilder buffer = Tesselator.getInstance().getBuilder();
        buffer.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_COLOR_TEX);
        addQuad(buffer, matrix, cameraPos, xAxis, yAxis, center, halfWidth, halfHeight, color, alpha);
        BufferUploader.drawWithShader(buffer.end());
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
        buffer.vertex(matrix, (float) pos.x, (float) pos.y, (float) pos.z)
                .color((color >> 16) & 255, (color >> 8) & 255, color & 255, Mth.clamp(alpha, 0, 255))
                .uv(u, v)
                .endVertex();
    }

    private static RenderBasis basis(Entity entity, float partialTick, ActiveEntityVisualEffect effect, float scaleMul, double behindMul, double yMul) {
        Vec3 forward = faceLook(entity, partialTick);
        Vec3 right = WORLD_UP.cross(forward);
        if (right.lengthSqr() < 1.0E-5D) {
            right = new Vec3(1.0D, 0.0D, 0.0D);
        }
        right = right.normalize();
        Vec3 up = forward.cross(right).normalize();
        float scale = scale(entity, effect) * scaleMul;
        Vec3 center = entity.getEyePosition(partialTick)
                .add(up.scale(yOffset(entity, effect) * yMul))
                .subtract(forward.scale(behindOffset(entity, effect) * behindMul));
        return new RenderBasis(center, right, up, forward, scale);
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
        float entityScale = (float) Mth.clamp(0.68D + entity.getBbWidth() * 0.36D + entity.getBbHeight() * 0.05D, 0.78D, 2.45D);
        return Mth.clamp(base, 0.15F, 7.0F) * entityScale;
    }

    private static float intensity(ActiveEntityVisualEffect effect) {
        return effect.data().contains("Intensity") ? Mth.clamp(effect.data().getFloat("Intensity"), 0.05F, 8.0F) : 1.0F;
    }

    private static double behindOffset(Entity entity, ActiveEntityVisualEffect effect) {
        if (effect.data().contains("BehindOffset")) {
            return Mth.clamp(effect.data().getDouble("BehindOffset"), -2.0D, 4.0D);
        }
        return Mth.clamp(0.26D + entity.getBbWidth() * 0.72D, 0.48D, 1.85D);
    }

    private static double yOffset(Entity entity, ActiveEntityVisualEffect effect) {
        if (effect.data().contains("YOffset")) {
            return Mth.clamp(effect.data().getDouble("YOffset"), -entity.getBbHeight(), entity.getBbHeight());
        }
        return 0.10D + entity.getBbHeight() * 0.12D;
    }

    private static int alpha(Entity entity, Camera camera) {
        double distance = Math.sqrt(entity.distanceToSqr(camera.getPosition()));
        float fade = 1.0F - smoothstep((float) (TRACK_DISTANCE * 0.76D), (float) TRACK_DISTANCE, (float) distance);
        return (int) (Mth.clamp(fade, 0.0F, 1.0F) * 225.0F);
    }

    private static float smoothstep(float edge0, float edge1, float value) {
        float x = Mth.clamp((value - edge0) / (edge1 - edge0), 0.0F, 1.0F);
        return x * x * (3.0F - 2.0F * x);
    }

    private record RenderBasis(Vec3 center, Vec3 x, Vec3 y, Vec3 forward, float scale) {
    }

    private record ChainSample(Vec3 position, Vec3 radial) {
    }
}
