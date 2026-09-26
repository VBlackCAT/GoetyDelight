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
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.EffectInstance;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NumericTag;
import net.minecraft.nbt.Tag;
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
import net.v_black_cat.goetydelight.visual.EntityVisualEffects;
import net.v_black_cat.goetydelight.visual.GDVisualEffects;
import net.v_black_cat.goetydelight.visual.IVisualEffectHolder;
import org.joml.Matrix4f;
import org.joml.Vector4f;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nullable;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * 屏幕空间深度特效（{@code entity_depth_reconstruct}）。
 *
 * <p>渲染方式：每个特效<b>各占一次 draw</b>，用只覆盖它屏幕包围盒的四边形，绑一份单特效 uniform
 * （{@code EffectCenter0}/{@code EffectData0}/{@code EffectColor0}）。
 *
 * <ul>
 *     <li>没有槽位上限：想画多少个就画多少个，只受 GPU 填充率限制；</li>
 *     <li>比旧的「一次全屏 pass 里循环 N 个槽位」更省：每个特效只在它的包围盒里跑像素，
 *     现在还会顺手叠回 main target，不再需要整屏 blit；</li>
 *     <li>累积顺序仍然保持：「先领域雾（mode 7），再按相机距离」。</li>
 * </ul>
 *
 * <p>累积是正确的：每个特效绘制前，把它包围盒内「已经叠好的画面」blit 到 {@link #colorScratch}，
 * shader 采样这张副本、写回 main target 的同一区域 —— 既不会读写同一张纹理，也不会破坏盒子外的像素。
 */
@Mod.EventBusSubscriber(modid = GoetyDelight.MODID, value = Dist.CLIENT)
public final class ScreenSpaceDepthEffectPostProcessor {
    private static final Logger LOGGER = LoggerFactory.getLogger(ScreenSpaceDepthEffectPostProcessor.class);
    private static final ResourceLocation SHADER = new ResourceLocation(GoetyDelight.MODID, "entity_depth_reconstruct");
    private static final float DEFAULT_RADIUS = 3.5F;

    /** 纯性能提醒阈值：同屏超过这个数量会每 5 秒提示一次（不会丢弃任何特效）。 */
    private static final int PERF_WARN_THRESHOLD = 48;

    // 每次 draw 只绑一个特效，所以 uniform 名字是固定的
    private static final String EFFECT_CENTER = "EffectCenter0";
    private static final String EFFECT_DATA = "EffectData0";
    private static final String EFFECT_COLOR = "EffectColor0";

    @Nullable
    private static EffectInstance effect;
    /** 逐特效拷贝「当前累积画面」用：shader 不能同时采样它正在写入的 main target。 */
    @Nullable
    private static TextureTarget colorScratch;
    /** 场景深度副本：main 的深度附件属于当前 FBO，不能再当 sampler 用。 */
    @Nullable
    private static TextureTarget depthCopy;
    @Nullable
    private static Matrix4f cachedViewProjection;
    private static Matrix4f orthoMatrix = new Matrix4f();
    private static int targetWidth = -1;
    private static int targetHeight = -1;
    private static boolean warnedLoadFailure;
    private static long lastPerfWarnMillis;

    // ── 可复用矩阵（渲染线程单线程，静态安全） ──
    private static final Matrix4f TEMP_OLD_PROJECTION = new Matrix4f();
    private static final Matrix4f TEMP_VIEW_PROJECTION = new Matrix4f();
    private static final Matrix4f TEMP_INV_VIEW = new Matrix4f();
    private static final float[] COLOR_SCRATCH = new float[3];

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

        List<EffectDraw> draws = collectEffects(level, event);
        if (draws.isEmpty()) {
            return;
        }

        RenderTarget mainTarget = minecraft.getMainRenderTarget();
        if (!ensureTargets(mainTarget.width, mainTarget.height)) {
            return;
        }

        TEMP_OLD_PROJECTION.set(RenderSystem.getProjectionMatrix());
        VertexSorting oldSorting = RenderSystem.getVertexSorting();

        if (cachedViewProjection != null) {
            TEMP_VIEW_PROJECTION.set(cachedViewProjection);
        } else {
            TEMP_VIEW_PROJECTION.set(event.getProjectionMatrix());
        }

        // 深度只要一份场景副本，整帧共用（我们的 draw 不写深度，main 的深度始终是原始场景深度）
        copySceneDepth(mainTarget);

        RenderSystem.disableDepthTest();
        RenderSystem.depthMask(false);
        RenderSystem.disableBlend();
        RenderSystem.resetTextureMatrix();
        RenderSystem.depthFunc(519);
        RenderSystem.viewport(0, 0, mainTarget.width, mainTarget.height);
        mainTarget.bindWrite(false);

        for (EffectDraw draw : draws) {
            renderEffect(mainTarget, draw, event);
        }

        RenderSystem.depthFunc(515);
        RenderSystem.depthMask(true);
        RenderSystem.enableDepthTest();
        RenderSystem.setProjectionMatrix(TEMP_OLD_PROJECTION, oldSorting);
        mainTarget.bindWrite(false);
    }

    // ────────────────────────── 收集 ──────────────────────────

    private static List<EffectDraw> collectEffects(ClientLevel level, RenderLevelStageEvent event) {
        List<EffectDraw> draws = new ArrayList<>();
        Vec3 cameraPosition = event.getCamera().getPosition();

        for (Entity entity : level.entitiesForRendering()) {
            if (entity.isRemoved()) {
                continue;
            }

            if (!entity.getCapability(EntityVisualEffectSystem.ENTITY_VISUAL_EFFECTS).isPresent()) {
                continue;
            }

            EntityVisualEffects effects = entity instanceof IVisualEffectHolder holder
                    ? holder.goetydelight$getVisualEffects()
                    : null;
            if (effects == null || effects.isEmpty()) {
                continue;
            }

            for (ActiveEntityVisualEffect activeEffect : effects.effects()) {
                int mode = mode(activeEffect);
                if (mode < 0) {
                    continue;
                }

                Vec3 center = effectCenter(entity, event.getPartialTick(), mode, activeEffect).subtract(cameraPosition);
                double distanceSqr = center.lengthSqr();

                double renderDistance = renderDistance(activeEffect);
                if (renderDistance > 0.0D && distanceSqr > renderDistance * renderDistance) {
                    continue;
                }

                float progress = effectProgress(entity, activeEffect, event);
                // 入场展开：刚 add 时按比例缩小，GrowTicks 内长到 data 设定的 Radius/Height
                float growth = activeEffect.growthScale(entity.level().getGameTime(), event.getPartialTick());
                float radius = effectRadius(entity, activeEffect, mode, progress) * growth;
                float secondary = effectSecondary(entity, activeEffect, mode, progress);
                if (isHeightSecondary(mode)) {
                    secondary *= growth; // 高度类 second 一起长，否则展开过程会被压扁
                }
                float intensity = effectIntensity(activeEffect, mode, progress);
                effectColor(activeEffect, mode, event, COLOR_SCRATCH);
                double bound = boundRadius(entity, mode, radius, secondary);

                draws.add(new EffectDraw(
                        mode,
                        center,
                        radius,
                        secondary,
                        intensity,
                        COLOR_SCRATCH[0],
                        COLOR_SCRATCH[1],
                        COLOR_SCRATCH[2],
                        distanceSqr,
                        bound
                ));
            }
        }

        // mode 7 的领域雾先画，其它特效叠在雾上（保持原来的层次），同层内按相机距离
        draws.sort(Comparator
                .comparingInt((EffectDraw draw) -> draw.mode() == 7 ? 0 : 1)
                .thenComparingDouble(EffectDraw::distanceSqr));

        warnIfTooMany(draws.size());
        return draws;
    }

    private record EffectDraw(
            int mode,
            Vec3 center,
            float radius,
            float secondary,
            float intensity,
            float red,
            float green,
            float blue,
            double distanceSqr,
            double bound
    ) {
    }

    private static void warnIfTooMany(int count) {
        if (count <= PERF_WARN_THRESHOLD) {
            return;
        }

        long now = System.currentTimeMillis();
        if (now - lastPerfWarnMillis < 5000L) {
            return;
        }

        lastPerfWarnMillis = now;
        LOGGER.warn(
                "同屏屏幕空间特效 {} 个（> {}）：每个特效一次 draw，数量太多会拖慢帧率，"
                        + "建议用 /goetydelightvisual clear 清理后逐个测试。",
                count, PERF_WARN_THRESHOLD
        );
    }

    // ────────────────────────── 绘制 ──────────────────────────

    private static void renderEffect(RenderTarget mainTarget, EffectDraw draw, RenderLevelStageEvent event) {
        EffectInstance shader = effect;
        if (shader == null || colorScratch == null || depthCopy == null) {
            return;
        }

        ScreenRect rect = projectToScreen(draw, mainTarget.width, mainTarget.height);
        if (rect == null) {
            return; // 整个特效在相机后面或屏幕外
        }

        // 把这个矩形内「已经叠好的画面」拷进 scratch，shader 才能安全地采样它
        copyColorRegion(mainTarget, colorScratch, rect);

        // Forge 的 EffectInstance 只接受 IntSupplier 形式的 sampler（它自己的 apply() 里按需取 id 并绑定）
        TextureTarget colorSource = colorScratch;
        TextureTarget depthSource = depthCopy;
        shader.setSampler("DiffuseSampler", colorSource::getColorTextureId);
        shader.setSampler("DepthSampler", depthSource::getDepthTextureId);

        shader.safeGetUniform("ProjMat").set(orthoMatrix);
        shader.safeGetUniform("ViewProjMat").set(TEMP_VIEW_PROJECTION);

        TEMP_INV_VIEW.set(TEMP_VIEW_PROJECTION).invert();
        shader.safeGetUniform("InvViewProjMat").set(TEMP_INV_VIEW);

        shader.safeGetUniform("InSize").set((float) mainTarget.width, (float) mainTarget.height);
        shader.safeGetUniform("OutSize").set((float) mainTarget.width, (float) mainTarget.height);
        shader.safeGetUniform("Time").set((event.getRenderTick() + event.getPartialTick()) / 20.0F);

        // 本次 draw 只画这一个特效
        shader.safeGetUniform(EFFECT_CENTER).set((float) draw.center().x, (float) draw.center().y, (float) draw.center().z);
        shader.safeGetUniform(EFFECT_DATA).set((float) draw.mode(), draw.radius(), draw.secondary(), draw.intensity());
        shader.safeGetUniform(EFFECT_COLOR).set(draw.red(), draw.green(), draw.blue());

        shader.apply();
        mainTarget.bindWrite(false);
        drawQuad(rect);
        shader.clear();
    }

    private static void drawQuad(ScreenRect rect) {
        BufferBuilder bufferBuilder = Tesselator.getInstance().getBuilder();
        bufferBuilder.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION);
        bufferBuilder.vertex(rect.x0(), rect.y0(), 500.0D).endVertex();
        bufferBuilder.vertex(rect.x1(), rect.y0(), 500.0D).endVertex();
        bufferBuilder.vertex(rect.x1(), rect.y1(), 500.0D).endVertex();
        bufferBuilder.vertex(rect.x0(), rect.y1(), 500.0D).endVertex();
        BufferUploader.draw(bufferBuilder.end());
    }

    /**
     * 把特效的包围盒投影成屏幕像素矩形（保守放大，宁可多画也别裁掉）。
     *
     * <p>实现要点：投的是包围盒的 <b>8 个角</b>，再取 NDC 的 min/max。
     * 不能像以前那样用 {@code ViewProjMat.m00/m11} 乘一乘当像素半径 —— {@code VP = P·V}，
     * 这两个元素里混着相机旋转（m00 含 {@code cos(yaw)·cos(pitch)}，m11 含 {@code cos(pitch)}），
     * 于是转视角时横向半径会跟着缩小、把特效从两侧裁进来，转过头又变回来。
     * 投 8 个角还顺带解决了「特效贴脸 / 在画面边缘」时线性近似偏小的问题。
     */
    @Nullable
    private static ScreenRect projectToScreen(EffectDraw draw, int width, int height) {
        Matrix4f viewProjection = TEMP_VIEW_PROJECTION;
        double bound = Math.max(0.5D, draw.bound());
        Vec3 center = draw.center();

        Vector4f clip = new Vector4f((float) center.x, (float) center.y, (float) center.z, 1.0F);
        viewProjection.transform(clip);

        float centerW = clip.w();
        if (!Float.isFinite(centerW) || !Float.isFinite(clip.x()) || !Float.isFinite(clip.y())) {
            return ScreenRect.full(width, height);
        }

        if (centerW + bound <= 0.0F) {
            return null; // 整个包围盒都在相机平面之后
        }
        if (centerW - bound <= 0.0F) {
            return ScreenRect.full(width, height); // 跨过相机平面，保守整屏
        }

        float minX = Float.POSITIVE_INFINITY;
        float minY = Float.POSITIVE_INFINITY;
        float maxX = Float.NEGATIVE_INFINITY;
        float maxY = Float.NEGATIVE_INFINITY;

        Vector4f corner = new Vector4f();
        for (int i = 0; i < 8; i++) {
            float offsetX = (i & 1) == 0 ? (float) -bound : (float) bound;
            float offsetY = (i & 2) == 0 ? (float) -bound : (float) bound;
            float offsetZ = (i & 4) == 0 ? (float) -bound : (float) bound;

            corner.set(
                    (float) center.x + offsetX,
                    (float) center.y + offsetY,
                    (float) center.z + offsetZ,
                    1.0F
            );
            viewProjection.transform(corner);

            float w = corner.w();
            if (!Float.isFinite(w) || w <= 0.0001F) {
                return ScreenRect.full(width, height);
            }

            float ndcX = corner.x() / w;
            float ndcY = corner.y() / w;
            if (!Float.isFinite(ndcX) || !Float.isFinite(ndcY)) {
                return ScreenRect.full(width, height);
            }

            minX = Math.min(minX, ndcX);
            minY = Math.min(minY, ndcY);
            maxX = Math.max(maxX, ndcX);
            maxY = Math.max(maxY, ndcY);
        }

        // 深度边缘检测会读邻域，另外再留一点余量
        float pad = 8.0F;
        int x0 = Mth.floor(Math.max(0.0F, (minX * 0.5F + 0.5F) * width - pad));
        int y0 = Mth.floor(Math.max(0.0F, (minY * 0.5F + 0.5F) * height - pad));
        int x1 = Mth.ceil(Math.min((float) width, (maxX * 0.5F + 0.5F) * width + pad));
        int y1 = Mth.ceil(Math.min((float) height, (maxY * 0.5F + 0.5F) * height + pad));

        if (x1 - x0 < 1 || y1 - y0 < 1) {
            return null;
        }

        return new ScreenRect(x0, y0, x1, y1);
    }

    /**
     * 包围球半径（格）：取特效自身的半径/高度，再保守放大 —— 少画一点只是浪费填充率，
     * 画少了就是特效被切掉，所以这里宁多勿少。
     *
     * <p>{@link #MODE_RADIAL_SCALE} 里的系数是从 {@code entity_depth_reconstruct.fsh} 各模式函数里
     * 实际用到的 {@code radius * N} 上限推出来的（例如 mode 3 的 halo 到 2.65r、mode 10 的火焰球形边界是 3r），
     * 改 shader 里的空间尺度时记得同步这张表。
     */
    private static double boundRadius(Entity entity, int mode, float radius, float secondary) {
        double extent = Math.max(radius * radialScale(mode), entity.getBbHeight() * 1.25D);

        // 这些模式下 secondary 表示高度（见 effectSecondary）
        if (isHeightSecondary(mode)) {
            extent = Math.max(extent, secondary);
        }

        return extent * 1.15D + 0.75D;
    }

    /** 这些 mode 的 {@code data.z}(secondary) 是高度而不是进度/朝向。 */
    private static boolean isHeightSecondary(int mode) {
        return mode == 3 || mode == 4 || mode == 5 || mode == 6 || mode == 7 || mode == 8 || mode == 9;
    }

    /** 每个 mode 在 shader 里相对 {@code data.y}(=radius) 的最大空间放大倍数。 */
    private static double radialScale(int mode) {
        return switch (mode) {
            case 2 -> 1.9D;   // outline_scan: radius * 1.65
            case 3 -> 3.0D;   // depth_occluded_halo: radius * 2.65
            case 4 -> 1.7D;   // contact_edge_glow: radius * 1.38
            case 5 -> 2.1D;   // volumetric_light_column: radius * 1.8
            case 6 -> 2.0D;   // depth_refraction_pressure: radius * 1.75
            case 8 -> 1.2D;   // shrine_slash: radius * 0.8（高度由 secondary 覆盖）
            case 9 -> 1.4D;   // shrine_target_glow: radius * 1.05
            case 10 -> 3.4D;  // shrine_fire: 球形边界 radius * 3.0
            case 13 -> 2.1D;  // shrine_fire_legacy: radius * 1.8
            case 14 -> 1.5D;  // shrine_black_domain: radius * 1.2
            case 15 -> 1.4D;  // shrine_black_mist: radius * 1.1
            case 16 -> 1.9D;  // black_cat_head_fog: 雾球 radius * 1.58
            case 17 -> 1.7D;  // cosmic_domain: 领域球 radius + 球外日冕（约 0.6r）
            case 18 -> 1.7D;  // lunar_domain: 月球完全在球内，外扩同样是球外日冕
            default -> 1.3D;  // 0/1/7/11/12 等：视觉边界基本就是 radius 本身
        };
    }

    // ────────────────────────── 临时 RT ──────────────────────────

    private static boolean ensureTargets(int width, int height) {
        if (colorScratch != null && depthCopy != null && targetWidth == width && targetHeight == height) {
            return true;
        }

        closeTargets();

        try {
            colorScratch = new TextureTarget(width, height, false, Minecraft.ON_OSX);
            colorScratch.setClearColor(0.0F, 0.0F, 0.0F, 0.0F);
            depthCopy = new TextureTarget(width, height, true, Minecraft.ON_OSX);
            depthCopy.setClearColor(0.0F, 0.0F, 0.0F, 0.0F);
        } catch (RuntimeException exception) {
            LOGGER.error("创建屏幕空间特效临时 RT 失败（{}x{}）", width, height, exception);
            closeTargets();
            return false;
        }

        targetWidth = width;
        targetHeight = height;
        orthoMatrix = new Matrix4f().setOrtho(0.0F, (float) width, 0.0F, (float) height, 0.1F, 1000.0F);
        return true;
    }

    private static void copySceneDepth(RenderTarget mainTarget) {
        TextureTarget depth = depthCopy;
        if (depth == null) {
            return;
        }

        GlStateManager._glBindFramebuffer(36008, mainTarget.frameBufferId);
        GlStateManager._glBindFramebuffer(36009, depth.frameBufferId);
        GlStateManager._glBlitFrameBuffer(
                0, 0, mainTarget.width, mainTarget.height,
                0, 0, depth.width, depth.height,
                256, 9728 // GL_DEPTH_BUFFER_BIT, GL_NEAREST
        );
        GlStateManager._glBindFramebuffer(36160, 0);
    }

    private static void copyColorRegion(RenderTarget source, RenderTarget destination, ScreenRect rect) {
        GlStateManager._glBindFramebuffer(36008, source.frameBufferId);
        GlStateManager._glBindFramebuffer(36009, destination.frameBufferId);
        GlStateManager._glBlitFrameBuffer(
                rect.x0(), rect.y0(), rect.x1(), rect.y1(),
                rect.x0(), rect.y0(), rect.x1(), rect.y1(),
                16384, 9728 // GL_COLOR_BUFFER_BIT, GL_NEAREST
        );
        GlStateManager._glBindFramebuffer(36160, 0);
    }

    private record ScreenRect(int x0, int y0, int x1, int y1) {
        private static ScreenRect full(int width, int height) {
            return new ScreenRect(0, 0, width, height);
        }
    }

    // ────────────────────────── 生命周期 ──────────────────────────

    private static void reload(ResourceManager resourceManager) {
        closeShader();
        closeTargets();

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

    private static void closeShader() {
        if (effect != null) {
            effect.close();
            effect = null;
        }
    }

    private static void closeTargets() {
        if (colorScratch != null) {
            colorScratch.destroyBuffers();
            colorScratch = null;
        }
        if (depthCopy != null) {
            depthCopy.destroyBuffers();
            depthCopy = null;
        }
        targetWidth = -1;
        targetHeight = -1;
    }

    // ────────────────────────── 单特效参数 ──────────────────────────

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

        if (effect.id().equals(GDVisualEffects.MALEVOLENT_SHRINE_DOMAIN.getId())) {
            return 7;
        }

        if (effect.id().equals(GDVisualEffects.MALEVOLENT_SHRINE_SLASH.getId())) {
            return 8;
        }

        if (effect.id().equals(GDVisualEffects.MALEVOLENT_SHRINE_TARGET_GLOW.getId())) {
            return 9;
        }

        if (effect.id().equals(GDVisualEffects.MALEVOLENT_SHRINE_FIRE.getId())) {
            return 10;
        }

        if (effect.id().equals(GDVisualEffects.MALEVOLENT_SHRINE_FIRE_LEGACY.getId())) {
            return 13;
        }
        if (effect.id().equals(GDVisualEffects.MALEVOLENT_SHRINE_BLACK_DOMAIN.getId())) {
            return 14;
        }
        if (effect.id().equals(GDVisualEffects.MALEVOLENT_SHRINE_BLACK_MIST.getId())) {
            return 15;
        }

        if (effect.id().equals(GDVisualEffects.MALEVOLENT_SHRINE_VOID.getId())) {
            return 11;
        }

        if (effect.id().equals(GDVisualEffects.MALEVOLENT_SHRINE_STARFIELD.getId())) {
            return 12;
        }

        if (effect.id().equals(GDVisualEffects.BLACK_CAT_HEAD_FOG_FIELD.getId())) {
            return 16;
        }

        if (effect.id().equals(GDVisualEffects.COSMIC_DOMAIN.getId())) {
            return 17;
        }

        if (effect.id().equals(GDVisualEffects.LUNAR_DOMAIN.getId())) {
            return 18;
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

        if (effect.id().equals(GDVisualEffects.MALEVOLENT_SHRINE_DOMAIN.getId())) {
            return GDVisualEffects.MALEVOLENT_SHRINE_DOMAIN.get().renderDistance();
        }

        if (effect.id().equals(GDVisualEffects.MALEVOLENT_SHRINE_SLASH.getId())) {
            return GDVisualEffects.MALEVOLENT_SHRINE_SLASH.get().renderDistance();
        }

        if (effect.id().equals(GDVisualEffects.MALEVOLENT_SHRINE_TARGET_GLOW.getId())) {
            return GDVisualEffects.MALEVOLENT_SHRINE_TARGET_GLOW.get().renderDistance();
        }

        if (effect.id().equals(GDVisualEffects.MALEVOLENT_SHRINE_FIRE.getId())) {
            return GDVisualEffects.MALEVOLENT_SHRINE_FIRE.get().renderDistance();
        }

        if (effect.id().equals(GDVisualEffects.MALEVOLENT_SHRINE_FIRE_LEGACY.getId())) {
            return GDVisualEffects.MALEVOLENT_SHRINE_FIRE_LEGACY.get().renderDistance();
        }
        if (effect.id().equals(GDVisualEffects.MALEVOLENT_SHRINE_BLACK_DOMAIN.getId())) {
            return GDVisualEffects.MALEVOLENT_SHRINE_BLACK_DOMAIN.get().renderDistance();
        }
        if (effect.id().equals(GDVisualEffects.MALEVOLENT_SHRINE_BLACK_MIST.getId())) {
            return GDVisualEffects.MALEVOLENT_SHRINE_BLACK_MIST.get().renderDistance();
        }

        if (effect.id().equals(GDVisualEffects.MALEVOLENT_SHRINE_VOID.getId())) {
            return GDVisualEffects.MALEVOLENT_SHRINE_VOID.get().renderDistance();
        }

        if (effect.id().equals(GDVisualEffects.MALEVOLENT_SHRINE_STARFIELD.getId())) {
            return GDVisualEffects.MALEVOLENT_SHRINE_STARFIELD.get().renderDistance();
        }

        if (effect.id().equals(GDVisualEffects.BLACK_CAT_HEAD_FOG_FIELD.getId())) {
            return GDVisualEffects.BLACK_CAT_HEAD_FOG_FIELD.get().renderDistance();
        }

        if (effect.id().equals(GDVisualEffects.COSMIC_DOMAIN.getId())) {
            return GDVisualEffects.COSMIC_DOMAIN.get().renderDistance();
        }

        if (effect.id().equals(GDVisualEffects.LUNAR_DOMAIN.getId())) {
            return GDVisualEffects.LUNAR_DOMAIN.get().renderDistance();
        }

        return 0.0D;
    }

    private static Vec3 effectCenter(Entity entity, float partialTick, int mode, ActiveEntityVisualEffect effect) {
        double yOffset = effect.data().contains("YOffset")
                ? Mth.clamp(effect.data().getDouble("YOffset"), -4.0D, 4.0D)
                : -0.04D;

        if ((mode == 10 || mode == 13 || mode == 14 || mode == 15 || mode == 16 || mode == 17 || mode == 18)
                && effect.data().contains("AnchorX")
                && effect.data().contains("AnchorY")
                && effect.data().contains("AnchorZ")) {
            return new Vec3(
                    effect.data().getDouble("AnchorX"),
                    effect.data().getDouble("AnchorY"),
                    effect.data().getDouble("AnchorZ")
            ).add(0.0D, mode == 16 || mode == 17 || mode == 18 ? yOffset : 0.0D, 0.0D);
        }

        if (mode == 16) {
            return entity.getEyePosition(partialTick).add(0.0D, yOffset, 0.0D);
        }

        double heightScale = switch (mode) {
            case 4, 5, 7, 8 -> 0.04D;
            default -> 0.52D;
        };
        return entity.getPosition(partialTick).add(0.0D, entity.getBbHeight() * heightScale, 0.0D);
    }

    private static float effectProgress(Entity entity, ActiveEntityVisualEffect effect, RenderLevelStageEvent event) {
        float partialTick = event.getPartialTick();
        if (effect.initialDuration() > 0) {
            // 进度按 StartGameTime + 当前游戏时间计算（客户端自走），不再依赖只在同步时刷新的 remainingTicks。
            return effect.progress(entity.level().getGameTime(), partialTick);
        }

        long start = effect.startGameTime() >= 0 ? effect.startGameTime() : entity.level().getGameTime();
        return Mth.clamp((entity.level().getGameTime() + partialTick - start) / 80.0F, 0.0F, 1.0F);
    }

    private static float effectRadius(Entity entity, ActiveEntityVisualEffect effect, int mode, float progress) {
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
            case 7, 8 -> DEFAULT_RADIUS;
            case 9 -> Math.max(0.9F, entity.getBbWidth() * 1.6F);
            case 10, 11, 12, 13, 14, 15 -> DEFAULT_RADIUS;
            case 16 -> {
                float sizeScale = effect.data().contains("Scale")
                        ? Mth.clamp(effect.data().getFloat("Scale"), 0.2F, 4.0F)
                        : 1.0F;
                yield Math.max(1.15F, entity.getBbWidth() * 1.25F) * sizeScale;
            }
            // 宇宙领域：默认给一个"领域感"的尺寸，不写 Radius 也不会小得像个球罩。
            case 17 -> Math.max(6.5F, entity.getBbWidth() * 5.0F);
            // 寂灭之月：月亮挂在球心上方，球小了月亮就贴脸，默认给得更大些。
            case 18 -> Math.max(9.0F, entity.getBbWidth() * 6.0F);
            default -> DEFAULT_RADIUS;
        };
    }

    private static float effectSecondary(Entity entity, ActiveEntityVisualEffect effect, int mode, float progress) {
        if (mode == 7 || mode == 8) {
            return effect.data().contains("Height")
                    ? effect.data().getFloat("Height")
                    : 6.0F;
        }

        if (mode == 9) {
            return Math.max(1.1F, entity.getBbHeight());
        }

        if (mode == 16) {
            float yawDegrees = effect.data().contains("Yaw")
                    ? effect.data().getFloat("Yaw")
                    : entity.getViewYRot(1.0F);
            return yawDegrees * ((float) Math.PI / 180.0F);
        }

        if (mode == 10 || mode == 11 || mode == 12 || mode == 13 || mode == 14 || mode == 15 || mode == 17 || mode == 18) {
            return progress;
        }

        return switch (mode) {
            case 4 -> Math.max(0.8F, entity.getBbHeight());
            case 5 -> Math.max(3.6F, entity.getBbHeight() * 2.8F);
            case 3 -> entity.getBbHeight();
            case 6 -> Math.max(0.9F, entity.getBbHeight());
            default -> progress;
        };
    }

    private static float effectIntensity(ActiveEntityVisualEffect effect, int mode, float progress) {
        float base = effect.data().contains("Intensity")
                ? effect.data().getFloat("Intensity")
                : 1.0F;

        // 领域雾和斩击在展开时逐渐浮现，避免瞬间铺满。
        if (mode == 7) {
            return base * Mth.clamp(progress * 1.6F, 0.0F, 1.0F);
        }
        if (mode == 8) {
            return base * Mth.clamp(progress * 1.3F, 0.0F, 1.0F);
        }
        // 宇宙领域：有限时长时才做「展开浮现」，永久领域（progress 恒为 0）直接满强度。
        if (mode == 17 || mode == 18) {
            float spawn = effect.initialDuration() == EntityVisualEffects.INFINITE
                    ? 1.0F
                    : Mth.clamp(progress * 4.0F, 0.0F, 1.0F);
            return base * spawn;
        }

        return switch (mode) {
            case 0 -> 1.15F * (1.0F - progress);
            case 1 -> 0.85F;
            case 2 -> 0.95F;
            case 3 -> 0.82F;
            case 4 -> 1.0F;
            case 5 -> 0.78F;
            case 6 -> 0.92F;
            case 9 -> 0.9F;
            case 10, 11, 12, 13, 14, 15, 16 -> 1.0F;
            default -> base;
        };
    }

    private static void effectColor(ActiveEntityVisualEffect effect, int mode, RenderLevelStageEvent event, float[] output) {
        switch (mode) {
            case 7 -> {
                output[0] = 0.46F;
                output[1] = 0.03F;
                output[2] = 0.055F;
            }
            case 8 -> {
                output[0] = 0.12F;
                output[1] = 0.01F;
                output[2] = 0.02F;
            }
            case 9 -> {
                output[0] = 0.62F;
                output[1] = 0.04F;
                output[2] = 0.07F;
            }
            case 10 -> {
                output[0] = 1.0F;
                output[1] = 0.55F;
                output[2] = 0.08F;
            }
            case 11 -> {
                output[0] = 0.25F;
                output[1] = 0.08F;
                output[2] = 0.55F;
            }
            case 12 -> {
                output[0] = 0.85F;
                output[1] = 0.85F;
                output[2] = 1.0F;
            }
            case 13 -> {
                output[0] = 1.0F;
                output[1] = 0.45F;
                output[2] = 0.08F;
            }
            case 14 -> {
                output[0] = 0.24F;
                output[1] = 0.025F;
                output[2] = 0.34F;
            }
            case 15 -> {
                output[0] = 0.12F;
                output[1] = 0.006F;
                output[2] = 0.02F;
            }
            case 16 -> readColor(effect.data(), "FogColor", output, 0.03F, 0.01F, 0.06F);
            case 17 -> {
                // 深空紫靛：星云/旋臂走冷紫，吸积环的暖白在 shader 里独立调色。
                output[0] = 0.32F;
                output[1] = 0.13F;
                output[2] = 0.72F;
            }
            case 18 -> {
                // 月面/月晕色调：默认银灰。data 里给 Tint（或 FogColor）就能换色，
                // 血月示例：{Tint:[1.0f,0.12f,0.15f]}
                if (effect.data().contains("Tint")) {
                    readColor(effect.data(), "Tint", output, 0.60F, 0.63F, 0.70F);
                } else {
                    readColor(effect.data(), "FogColor", output, 0.60F, 0.63F, 0.70F);
                }
            }
            default -> {
                // 其它模式用会流动的彩虹色；相位按特效 id 固定，避免特效顺序变化导致颜色跳变
                float phase = (event.getRenderTick() + event.getPartialTick()) * 0.08F + stablePhase(effect);
                output[0] = 0.55F + 0.45F * Mth.sin(phase);
                output[1] = 0.55F + 0.45F * Mth.sin(phase + 2.0943952F);
                output[2] = 0.55F + 0.45F * Mth.sin(phase + 4.1887903F);
            }
        }
    }

    private static float stablePhase(ActiveEntityVisualEffect effect) {
        return Math.abs(effect.id().hashCode() % 628) / 100.0F;
    }

    private static void readColor(
            CompoundTag data,
            String key,
            float[] output,
            float defaultRed,
            float defaultGreen,
            float defaultBlue
    ) {
        if (!data.contains(key)) {
            output[0] = defaultRed;
            output[1] = defaultGreen;
            output[2] = defaultBlue;
            return;
        }

        Tag tag = data.get(key);
        if (tag instanceof NumericTag) {
            int rgb = data.getInt(key);
            output[0] = ((rgb >> 16) & 255) / 255.0F;
            output[1] = ((rgb >> 8) & 255) / 255.0F;
            output[2] = (rgb & 255) / 255.0F;
            return;
        }

        if (tag instanceof ListTag list && list.size() >= 3) {
            output[0] = channel(list.get(0));
            output[1] = channel(list.get(1));
            output[2] = channel(list.get(2));
            return;
        }

        output[0] = defaultRed;
        output[1] = defaultGreen;
        output[2] = defaultBlue;
    }

    private static float channel(Tag tag) {
        if (tag instanceof NumericTag numericTag) {
            float value = numericTag.getAsFloat();
            return Mth.clamp(value > 1.0F ? value / 255.0F : value, 0.0F, 1.0F);
        }
        return 1.0F;
    }
}
