package net.v_black_cat.goetydelight.visual.client;

import com.mojang.blaze3d.pipeline.RenderTarget;
import com.mojang.blaze3d.pipeline.TextureTarget;
import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.BufferUploader;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.blaze3d.vertex.VertexFormat;
import com.mojang.blaze3d.vertex.VertexSorting;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.EffectInstance;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.ResourceManagerReloadListener;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RenderLevelStageEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.v_black_cat.goetydelight.GoetyDelight;
import net.v_black_cat.goetydelight.visual.ActiveEntityVisualEffect;
import net.v_black_cat.goetydelight.visual.EntityVisualEffectSystem;
import net.v_black_cat.goetydelight.visual.GDVisualEffects;
import org.joml.Matrix4f;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nullable;
import java.io.IOException;
import java.util.function.IntSupplier;

@Mod.EventBusSubscriber(modid = GoetyDelight.MODID, value = Dist.CLIENT)
public final class ScreenSpaceDepthEffectPostProcessor {
    private static final Logger LOGGER = LoggerFactory.getLogger(ScreenSpaceDepthEffectPostProcessor.class);
    private static final ResourceLocation SHADER = new ResourceLocation(GoetyDelight.MODID, "entity_depth_reconstruct");
    private static final int MAX_EFFECTS = 8;
    private static final float DEFAULT_RADIUS = 3.5F;

    @Nullable
    private static EffectInstance effect;
    @Nullable
    private static TextureTarget scratchTarget;
    @Nullable
    private static Matrix4f cachedViewProjection;
    private static Matrix4f orthoMatrix = new Matrix4f();
    private static int scratchWidth = -1;
    private static int scratchHeight = -1;
    private static boolean warnedLoadFailure;

    // ── 可复用矩阵（渲染线程单线程，静态安全） ──
    private static final Matrix4f TEMP_OLD_PROJECTION = new Matrix4f();
    private static final Matrix4f TEMP_VIEW_PROJECTION = new Matrix4f();
    private static final Matrix4f TEMP_INV_VIEW = new Matrix4f();

    // ── 采样器缓存 ──
    private static RenderTarget samplerTarget;
    private static final IntSupplier COLOR_SAMPLER = () -> samplerTarget.getColorTextureId();
    private static final IntSupplier DEPTH_SAMPLER = () -> samplerTarget.getDepthTextureId();

    // ── Uniform 名称缓存 ──
    private static final String[] CENTER_NAMES = new String[MAX_EFFECTS];
    private static final String[] DATA_NAMES = new String[MAX_EFFECTS];
    private static final String[] COLOR_NAMES = new String[MAX_EFFECTS];

    static {
        for (int i = 0; i < MAX_EFFECTS; i++) {
            CENTER_NAMES[i] = "EffectCenter" + i;
            DATA_NAMES[i] = "EffectData" + i;
            COLOR_NAMES[i] = "EffectColor" + i;
        }
    }

    private ScreenSpaceDepthEffectPostProcessor() {
    }

    public static ResourceManagerReloadListener reloadListener() {
        return ScreenSpaceDepthEffectPostProcessor::reload;
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void onRenderLevelStage(RenderLevelStageEvent event) {
        if (event.getStage() == RenderLevelStageEvent.Stage.AFTER_PARTICLES) {
            cachedViewProjection = new Matrix4f(event.getProjectionMatrix()).mul(event.getPoseStack().last().pose());
            return;
        }

        if (event.getStage() != RenderLevelStageEvent.Stage.AFTER_LEVEL) {
            return;
        }

        process(event);
        cachedViewProjection = null;
    }

    private static void process(RenderLevelStageEvent event) {
        Minecraft minecraft = Minecraft.getInstance();
        ClientLevel level = minecraft.level;
        if (level == null || minecraft.player == null || effect == null) {
            return;
        }

        EffectPacket packet = collectEffects(level, event);
        if (packet.count == 0) {
            return;
        }

        RenderTarget mainTarget = minecraft.getMainRenderTarget();
        ensureScratchTarget(mainTarget.width, mainTarget.height);
        if (scratchTarget == null) {
            return;
        }

        runPostPass(minecraft, mainTarget, scratchTarget, event, packet);
    }

    private static void reload(ResourceManager resourceManager) {
        close();

        try {
            effect = new EffectInstance(resourceManager, SHADER.toString());
            warnedLoadFailure = false;
        } catch (IOException exception) {
            effect = null;
            if (!warnedLoadFailure) {
                LOGGER.warn("Failed to load screen-space depth effect shader {}", SHADER, exception);
                warnedLoadFailure = true;
            }
        }
    }

    private static void close() {
        if (effect != null) {
            effect.close();
            effect = null;
        }

        if (scratchTarget != null) {
            scratchTarget.destroyBuffers();
            scratchTarget = null;
        }

        scratchWidth = -1;
        scratchHeight = -1;
    }

    private static void ensureScratchTarget(int width, int height) {
        if (scratchTarget != null && scratchWidth == width && scratchHeight == height) {
            return;
        }

        if (scratchTarget != null) {
            scratchTarget.destroyBuffers();
        }

        scratchTarget = new TextureTarget(width, height, false, Minecraft.ON_OSX);
        scratchTarget.setClearColor(0.0F, 0.0F, 0.0F, 0.0F);
        scratchWidth = width;
        scratchHeight = height;
        orthoMatrix = new Matrix4f().setOrtho(0.0F, (float) width, 0.0F, (float) height, 0.1F, 1000.0F);
    }

    // ── 修改：移除 ifPresent lambda ──
    private static EffectPacket collectEffects(ClientLevel level, RenderLevelStageEvent event) {
        EffectPacket packet = new EffectPacket();
        Vec3 cameraPosition = event.getCamera().getPosition();

        for (Entity entity : level.entitiesForRendering()) {
            if (entity.isRemoved()) {
                continue;
            }

            var capability = entity.getCapability(EntityVisualEffectSystem.ENTITY_VISUAL_EFFECTS);
            if (!capability.isPresent()) {
                continue;
            }

            var effects = capability.orElse(null);
            if (effects == null) {
                continue;
            }

            for (ActiveEntityVisualEffect activeEffect : effects.effects()) {
                if (packet.count >= MAX_EFFECTS) {
                    break;
                }

                int mode = mode(activeEffect);
                if (mode < 0) {
                    continue;
                }

                double renderDistance = renderDistance(activeEffect);
                if (renderDistance > 0.0D && entity.distanceToSqr(cameraPosition) > renderDistance * renderDistance) {
                    continue;
                }

                packet.add(entity, activeEffect, event, mode);
            }
        }

        return packet;
    }

    private static int mode(ActiveEntityVisualEffect effect) {
        if (effect.id().equals(GDVisualEffects.SCREEN_SPACE_SHOCKWAVE.getId())) {
            return 0;
        }

        if (effect.id().equals(GDVisualEffects.DEPTH_REFRACTION_HEATWAVE.getId())) {
            return 1;
        }

        if (effect.id().equals(GDVisualEffects.OUTLINE_SCAN.getId())) {
            return 2;
        }

        if (effect.id().equals(GDVisualEffects.DEPTH_OCCLUDED_HALO.getId())) {
            return 3;
        }

        if (effect.id().equals(GDVisualEffects.CONTACT_EDGE_GLOW.getId())) {
            return 4;
        }

        if (effect.id().equals(GDVisualEffects.VOLUMETRIC_LIGHT_COLUMN.getId())) {
            return 5;
        }

        if (effect.id().equals(GDVisualEffects.DEPTH_REFRACTION_PRESSURE.getId())) {
            return 6;
        }

        return -1;
    }

    private static double renderDistance(ActiveEntityVisualEffect effect) {
        if (effect.id().equals(GDVisualEffects.SCREEN_SPACE_SHOCKWAVE.getId())) {
            return GDVisualEffects.SCREEN_SPACE_SHOCKWAVE.get().renderDistance();
        }

        if (effect.id().equals(GDVisualEffects.DEPTH_REFRACTION_HEATWAVE.getId())) {
            return GDVisualEffects.DEPTH_REFRACTION_HEATWAVE.get().renderDistance();
        }

        if (effect.id().equals(GDVisualEffects.OUTLINE_SCAN.getId())) {
            return GDVisualEffects.OUTLINE_SCAN.get().renderDistance();
        }

        if (effect.id().equals(GDVisualEffects.DEPTH_OCCLUDED_HALO.getId())) {
            return GDVisualEffects.DEPTH_OCCLUDED_HALO.get().renderDistance();
        }

        if (effect.id().equals(GDVisualEffects.CONTACT_EDGE_GLOW.getId())) {
            return GDVisualEffects.CONTACT_EDGE_GLOW.get().renderDistance();
        }

        if (effect.id().equals(GDVisualEffects.VOLUMETRIC_LIGHT_COLUMN.getId())) {
            return GDVisualEffects.VOLUMETRIC_LIGHT_COLUMN.get().renderDistance();
        }

        if (effect.id().equals(GDVisualEffects.DEPTH_REFRACTION_PRESSURE.getId())) {
            return GDVisualEffects.DEPTH_REFRACTION_PRESSURE.get().renderDistance();
        }

        return 0.0D;
    }

    private static void runPostPass(Minecraft minecraft, RenderTarget mainTarget, TextureTarget outTarget, RenderLevelStageEvent event, EffectPacket packet) {
        EffectInstance shader = effect;
        if (shader == null) {
            return;
        }

        // 复用矩阵
        TEMP_OLD_PROJECTION.set(RenderSystem.getProjectionMatrix());
        VertexSorting oldSorting = RenderSystem.getVertexSorting();

        if (cachedViewProjection != null) {
            TEMP_VIEW_PROJECTION.set(cachedViewProjection);
        } else {
            TEMP_VIEW_PROJECTION.set(event.getProjectionMatrix());
        }

        mainTarget.unbindWrite();
        RenderSystem.viewport(0, 0, outTarget.width, outTarget.height);
        RenderSystem.disableDepthTest();
        RenderSystem.depthMask(false);
        RenderSystem.disableBlend();
        RenderSystem.resetTextureMatrix();

        // 设置采样器（使用静态 supplier，避免每帧 lambda）
        samplerTarget = mainTarget;
        shader.setSampler("DiffuseSampler", COLOR_SAMPLER);
        shader.setSampler("DepthSampler", DEPTH_SAMPLER);

        shader.safeGetUniform("ProjMat").set(orthoMatrix);
        shader.safeGetUniform("ViewProjMat").set(TEMP_VIEW_PROJECTION);

        // 逆矩阵：复用 TEMP_INV_VIEW
        TEMP_INV_VIEW.set(TEMP_VIEW_PROJECTION).invert();
        shader.safeGetUniform("InvViewProjMat").set(TEMP_INV_VIEW);

        shader.safeGetUniform("InSize").set((float) mainTarget.width, (float) mainTarget.height);
        shader.safeGetUniform("OutSize").set((float) outTarget.width, (float) outTarget.height);
        shader.safeGetUniform("Time").set((event.getRenderTick() + event.getPartialTick()) / 20.0F);
        shader.safeGetUniform("EffectCount").set(packet.count);
        uploadPacket(shader, packet);

        shader.apply();
        outTarget.clear(Minecraft.ON_OSX);
        outTarget.bindWrite(false);
        RenderSystem.depthFunc(519);

        BufferBuilder bufferBuilder = Tesselator.getInstance().getBuilder();
        bufferBuilder.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION);
        bufferBuilder.vertex(0.0D, 0.0D, 500.0D).endVertex();
        bufferBuilder.vertex((double) outTarget.width, 0.0D, 500.0D).endVertex();
        bufferBuilder.vertex((double) outTarget.width, (double) outTarget.height, 500.0D).endVertex();
        bufferBuilder.vertex(0.0D, (double) outTarget.height, 500.0D).endVertex();
        BufferUploader.draw(bufferBuilder.end());

        RenderSystem.depthFunc(515);
        shader.clear();
        outTarget.unbindWrite();
        mainTarget.unbindRead();

        GlStateManager._glBindFramebuffer(36008, outTarget.frameBufferId);
        GlStateManager._glBindFramebuffer(36009, mainTarget.frameBufferId);
        GlStateManager._glBlitFrameBuffer(0, 0, outTarget.width, outTarget.height, 0, 0, mainTarget.width, mainTarget.height, 16384, 9728);
        GlStateManager._glBindFramebuffer(36160, 0);

        RenderSystem.depthMask(true);
        RenderSystem.setProjectionMatrix(TEMP_OLD_PROJECTION, oldSorting);
        mainTarget.bindWrite(false);
    }

    // ── 使用预定义 uniform 名称数组 ──
    private static void uploadPacket(EffectInstance shader, EffectPacket packet) {
        for (int i = 0; i < MAX_EFFECTS; i++) {
            shader.safeGetUniform(CENTER_NAMES[i]).set(
                    packet.centers[i * 3],
                    packet.centers[i * 3 + 1],
                    packet.centers[i * 3 + 2]
            );
            shader.safeGetUniform(DATA_NAMES[i]).set(
                    packet.data[i * 4],
                    packet.data[i * 4 + 1],
                    packet.data[i * 4 + 2],
                    packet.data[i * 4 + 3]
            );
            shader.safeGetUniform(COLOR_NAMES[i]).set(
                    packet.colors[i * 3],
                    packet.colors[i * 3 + 1],
                    packet.colors[i * 3 + 2]
            );
        }
    }

    private static final class EffectPacket {
        private final float[] centers = new float[MAX_EFFECTS * 3];
        private final float[] data = new float[MAX_EFFECTS * 4];
        private final float[] colors = new float[MAX_EFFECTS * 3];
        private int count;

        private void add(Entity entity, ActiveEntityVisualEffect effect, RenderLevelStageEvent event, int mode) {
            Vec3 center = center(entity, event.getPartialTick(), mode);
            Vec3 cameraRelative = center.subtract(event.getCamera().getPosition());
            float progress = progress(entity, effect, event);
            float radius = radius(entity, effect, mode, progress);
            float intensity = intensity(effect, mode, progress);
            float secondary = secondary(entity, mode, progress);
            int offset3 = count * 3;
            int offset4 = count * 4;

            centers[offset3] = (float) cameraRelative.x;
            centers[offset3 + 1] = (float) cameraRelative.y;
            centers[offset3 + 2] = (float) cameraRelative.z;
            data[offset4] = mode;
            data[offset4 + 1] = radius;
            data[offset4 + 2] = secondary;
            data[offset4 + 3] = intensity;

            float phase = (event.getRenderTick() + event.getPartialTick()) * 0.08F + count * 1.37F;
            colors[offset3] = 0.55F + 0.45F * Mth.sin(phase);
            colors[offset3 + 1] = 0.55F + 0.45F * Mth.sin(phase + 2.0943952F);
            colors[offset3 + 2] = 0.55F + 0.45F * Mth.sin(phase + 4.1887903F);
            count++;
        }

        private static Vec3 center(Entity entity, float partialTick, int mode) {
            double heightScale = switch (mode) {
                case 4, 5 -> 0.04D;
                default -> 0.52D;
            };
            return entity.getPosition(partialTick).add(0.0D, entity.getBbHeight() * heightScale, 0.0D);
        }

        private static float progress(Entity entity, ActiveEntityVisualEffect effect, RenderLevelStageEvent event) {
            if (effect.initialDuration() > 0) {
                return Mth.clamp(1.0F - (effect.remainingTicks() - event.getPartialTick()) / (float) effect.initialDuration(), 0.0F, 1.0F);
            }

            long start = effect.data().contains("StartGameTime") ? effect.data().getLong("StartGameTime") : entity.level().getGameTime();
            return Mth.clamp((entity.level().getGameTime() + event.getPartialTick() - start) / 80.0F, 0.0F, 1.0F);
        }

        private static float radius(Entity entity, ActiveEntityVisualEffect effect, int mode, float progress) {
            if (effect.data().contains("Radius")) {
                return effect.data().getFloat("Radius");
            }

            float scale = Math.max(1.0F, entity.getBbWidth());
            return switch (mode) {
                case 0 -> (1.1F + progress * 6.4F) * scale;
                case 1 -> Math.max(2.2F, Math.max(entity.getBbHeight() * 1.15F, entity.getBbWidth() * 2.0F));
                case 2 -> Math.max(1.6F, entity.getBbHeight() * 0.95F);
                case 3 -> Math.max(1.1F, entity.getBbWidth() * 1.6F);
                case 4 -> Math.max(0.85F, entity.getBbWidth() * 1.2F);
                case 5 -> Math.max(0.72F, entity.getBbWidth() * 0.82F);
                case 6 -> Math.max(2.1F, Math.max(entity.getBbHeight() * 1.05F, entity.getBbWidth() * 2.35F));
                default -> DEFAULT_RADIUS;
            };
        }

        private static float secondary(Entity entity, int mode, float progress) {
            return switch (mode) {
                case 4 -> Math.max(0.8F, entity.getBbHeight());
                case 5 -> Math.max(3.6F, entity.getBbHeight() * 2.8F);
                case 3 -> entity.getBbHeight();
                case 6 -> Math.max(0.9F, entity.getBbHeight());
                default -> progress;
            };
        }

        private static float intensity(ActiveEntityVisualEffect effect, int mode, float progress) {
            if (effect.data().contains("Intensity")) {
                return effect.data().getFloat("Intensity");
            }

            return switch (mode) {
                case 0 -> 1.15F * (1.0F - progress);
                case 1 -> 0.85F;
                case 2 -> 0.95F;
                case 3 -> 0.82F;
                case 4 -> 1.0F;
                case 5 -> 0.78F;
                case 6 -> 0.92F;
                default -> 1.0F;
            };
        }
    }
}