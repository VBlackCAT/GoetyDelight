package net.v_black_cat.goetydelight.render;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.BufferUploader;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.blaze3d.vertex.VertexFormat;
import com.mojang.blaze3d.vertex.VertexSorting;
import com.mojang.math.Axis;
import net.minecraft.client.Camera;
import net.minecraft.client.renderer.ShaderInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.client.event.RenderLevelStageEvent;
import net.v_black_cat.goetydelight.render.test.ModShaderReg;
import net.v_black_cat.goetydelight.visual.ActiveEntityVisualEffect;
import org.joml.Matrix3f;
import org.joml.Matrix4f;

public final class OrbitingSphereRenderer {
    private static final int STACKS = 24;
    private static final int SLICES = 48;
    private static final float SPHERE_RADIUS = 0.42F;
    private static final float GLOW_RADIUS = 0.76F;
    private static final double ORBIT_RADIUS = 2.15D;

    private OrbitingSphereRenderer() {
    }

    public static void render(RenderLevelStageEvent event, Entity entity, ActiveEntityVisualEffect effect) {
        float partialTick = event.getPartialTick();
        float renderTime = entity.tickCount + partialTick;
        Vec3 entityCenter = entity.getPosition(partialTick).add(0.0D, entity.getBbHeight() * 0.72D, 0.0D);
        Vec3 sphereCenter = entityCenter.add(orbitOffset(renderTime, entity));

        Camera camera = event.getCamera();
        Vec3 cameraPos = camera.getPosition();

        PoseStack poseStack = event.getPoseStack();
        poseStack.pushPose();
        poseStack.translate(sphereCenter.x - cameraPos.x, sphereCenter.y - cameraPos.y, sphereCenter.z - cameraPos.z);
        poseStack.mulPose(Axis.YP.rotationDegrees(renderTime * 6.0F));
        poseStack.mulPose(Axis.XP.rotationDegrees(renderTime * 3.7F));
        poseStack.mulPose(Axis.ZP.rotationDegrees(renderTime * 2.3F));

        ShaderInstance shader = ModShaderReg.getOrbitSphereShader();
        shader.safeGetUniform("iTime").set(renderTime / 20.0F);

        Matrix4f oldProjection = new Matrix4f(RenderSystem.getProjectionMatrix());
        VertexSorting oldSorting = RenderSystem.getVertexSorting();
        RenderSystem.setProjectionMatrix(event.getProjectionMatrix(), VertexSorting.DISTANCE_TO_ORIGIN);

        RenderSystem.setShader(() -> shader);
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        RenderSystem.enableDepthTest();
        RenderSystem.depthMask(true);
        RenderSystem.disableBlend();
        RenderSystem.enableCull();

        poseStack.pushPose();
        poseStack.scale(SPHERE_RADIUS, SPHERE_RADIUS, SPHERE_RADIUS);
        shader.safeGetUniform("GlowMode").set(0);
        shader.safeGetUniform("intensity").set(1.0F);
        drawSphere(poseStack.last(), renderTime, 255);
        poseStack.popPose();

        RenderSystem.depthMask(false);
        RenderSystem.enableBlend();
        RenderSystem.blendFuncSeparate(770, 1, 1, 1);
        RenderSystem.disableCull();

        poseStack.pushPose();
        poseStack.scale(GLOW_RADIUS, GLOW_RADIUS, GLOW_RADIUS);
        shader.safeGetUniform("GlowMode").set(1);
        shader.safeGetUniform("intensity").set(1.35F);
        drawSphere(poseStack.last(), renderTime, 120);
        poseStack.popPose();

        RenderSystem.enableCull();
        RenderSystem.disableBlend();
        RenderSystem.depthMask(true);
        RenderSystem.setProjectionMatrix(oldProjection, oldSorting);

        poseStack.popPose();
    }

    private static Vec3 orbitOffset(float renderTime, Entity entity) {
        double orbitAngle = renderTime * 0.065D;
        double verticalBob = 0.22D + Math.sin(renderTime * 0.095D) * 0.28D;
        double orbitRadius = Math.max(ORBIT_RADIUS, entity.getBbWidth() * 1.65D);
        return new Vec3(
                Math.cos(orbitAngle) * orbitRadius,
                verticalBob,
                Math.sin(orbitAngle) * orbitRadius
        );
    }

    private static void drawSphere(PoseStack.Pose pose, float renderTime, int alpha) {
        Matrix4f matrix = pose.pose();
        Matrix3f normal = pose.normal();
        BufferBuilder buffer = Tesselator.getInstance().getBuilder();
        buffer.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_COLOR_NORMAL);

        for (int stack = 0; stack < STACKS; stack++) {
            float v0 = (float) stack / STACKS;
            float v1 = (float) (stack + 1) / STACKS;
            float phi0 = (float) (-Math.PI / 2.0D + Math.PI * v0);
            float phi1 = (float) (-Math.PI / 2.0D + Math.PI * v1);

            for (int slice = 0; slice < SLICES; slice++) {
                float u0 = (float) slice / SLICES;
                float u1 = (float) (slice + 1) / SLICES;
                float theta0 = (float) (Math.PI * 2.0D * u0);
                float theta1 = (float) (Math.PI * 2.0D * u1);

                addSphereVertex(buffer, matrix, normal, theta0, phi0, renderTime, alpha);
                addSphereVertex(buffer, matrix, normal, theta1, phi0, renderTime, alpha);
                addSphereVertex(buffer, matrix, normal, theta1, phi1, renderTime, alpha);
                addSphereVertex(buffer, matrix, normal, theta0, phi1, renderTime, alpha);
            }
        }

        BufferUploader.drawWithShader(buffer.end());
    }

    private static void addSphereVertex(BufferBuilder buffer, Matrix4f matrix, Matrix3f normal, float theta, float phi, float renderTime, int alpha) {
        float cosPhi = (float) Math.cos(phi);
        float x = cosPhi * (float) Math.cos(theta);
        float y = (float) Math.sin(phi);
        float z = cosPhi * (float) Math.sin(theta);
        float colorPhase = theta + phi * 1.7F + renderTime * 0.08F;

        int red = colorChannel(colorPhase);
        int green = colorChannel(colorPhase + 2.0943952F);
        int blue = colorChannel(colorPhase + 4.1887903F);

        buffer.vertex(matrix, x, y, z)
                .color(red, green, blue, alpha)
                .normal(normal, x, y, z)
                .endVertex();
    }

    private static int colorChannel(float phase) {
        return 96 + (int) (Math.max(0.0F, Math.min(1.0F, 0.5F + 0.5F * Math.sin(phase))) * 159.0F);
    }
}
