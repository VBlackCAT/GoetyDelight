package net.v_black_cat.goetydelight.entities.display_entity;

import com.mojang.blaze3d.platform.NativeImage;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.v_black_cat.goetydelight.GoetyDelight;
import net.v_black_cat.goetydelight.bedrock.BedrockModel;
import net.v_black_cat.goetydelight.bedrock.model.BedrockPart;
import org.apache.maven.artifact.versioning.InvalidVersionSpecificationException;

import javax.annotation.Nullable;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Map;
import java.util.WeakHashMap;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@OnlyIn(Dist.CLIENT)
public class DisplayEntityRender extends EntityRenderer<DisplayEntity> {

    // ============================================================
    // 常量
    // ============================================================

    private static final ResourceLocation MODEL_LOCATION =
            new ResourceLocation("goetydelight", "models/entity/display_entity.json");

    /** 默认贴图：原版史蒂夫 */
    private static final ResourceLocation DEFAULT_STEVE =
            new ResourceLocation("minecraft", "textures/entity/player/wide/steve.png");

    /** 本地贴图：命名牌为 wu1wu2 时使用 */
    private static final ResourceLocation WU1WU2_TEXTURE =
            new ResourceLocation("goetydelight", "textures/entity/wu1wu2.png");

    private static final String WU1WU2_NAME = "wu1wu2";

    /** 模型整体缩放 */
    private static final float MODEL_SCALE = 0.9F;

    /** 失败重试间隔（毫秒） */
    private static final long RETRY_INTERVAL_MS = 60_000L;

    // ============================================================
    // 模型状态
    // ============================================================

    @Nullable
    private static BedrockModel cachedModel = null;
    private static boolean modelLoadFailed = false;

    /** 每根骨骼的初始旋转/位置，加载模型时快照一次 */
    private static final Map<String, float[]> INITIAL_POSE = new ConcurrentHashMap<>();

    private static float headInitXRot = 0f;
    private static float headInitYRot = 0f;

    // ============================================================
    // 皮肤状态（名字作为 key，成功才写缓存）
    // ============================================================

    private static final Map<String, ResourceLocation> SKIN_CACHE = new ConcurrentHashMap<>();
    private static final Map<String, Boolean> PENDING = new ConcurrentHashMap<>();
    private static final Map<String, Long> FAILED_AT = new ConcurrentHashMap<>();
    private static final Map<ResourceLocation, DynamicTexture> REGISTERED_TEXTURES = new ConcurrentHashMap<>();

    /** 每个实体上次处理过的命名牌 revision，用 WeakHashMap 保证实体卸载后条目自动回收 */
    private static final Map<DisplayEntity, Integer> LAST_REVISION =
            java.util.Collections.synchronizedMap(new WeakHashMap<>());

    public DisplayEntityRender(EntityRendererProvider.Context context) {
        super(context);
    }

    // ============================================================
    // 渲染
    // ============================================================

    @Override
    public void render(DisplayEntity entity, float entityYaw, float partialTick,
                       PoseStack poseStack, MultiBufferSource bufferSource, int packedLight) {

        ensureSkinResolved(entity);

        BedrockModel model = getOrLoadModel();
        if (model == null) {
            super.render(entity, entityYaw, partialTick, poseStack, bufferSource, packedLight);
            return;
        }

        ResourceLocation texture = resolveTexture(entity);

        poseStack.pushPose();
        float yaw = Mth.lerp(partialTick, entity.yRotO, entity.getYRot());
        poseStack.mulPose(Axis.YP.rotationDegrees(-yaw));
        float pitch = Mth.lerp(partialTick, entity.xRotO, entity.getXRot());
        poseStack.mulPose(Axis.XP.rotationDegrees(pitch));

        poseStack.scale(MODEL_SCALE, MODEL_SCALE, MODEL_SCALE);
        poseStack.translate(0.0, 1.5, 0.0);
        poseStack.mulPose(Axis.YN.rotationDegrees(180));
        poseStack.mulPose(Axis.ZP.rotationDegrees(180));
        resetModelToInitial(model);
        applyAnimation(entity, model, partialTick);
        
        BedrockPart head = model.getModelMap().get("Head");
        if (head != null && entity.getAnimation().isEmpty()) {
            head.yRot = headInitYRot + (float) Math.toRadians(entity.getYRot() - yaw);
            head.xRot = headInitXRot + (float) Math.toRadians(entity.getXRot());
        }

        VertexConsumer buffer = bufferSource.getBuffer(RenderType.entityCutoutNoCull(texture));
        model.renderToBuffer(poseStack, buffer, packedLight, OverlayTexture.NO_OVERLAY,
                1.0F, 1.0F, 1.0F, 1.0F);

        poseStack.popPose();

        super.render(entity, entityYaw, partialTick, poseStack, bufferSource, packedLight);
    }

    // ============================================================
    // 模型姿态重置
    // ============================================================

    private static void snapshotInitialPose(BedrockModel model) {
        INITIAL_POSE.clear();
        for (Map.Entry<String, BedrockPart> e : model.getModelMap().entrySet()) {
            BedrockPart part = e.getValue();
            INITIAL_POSE.put(e.getKey(), new float[]{
                    part.xRot, part.yRot, part.zRot,
                    part.x, part.y, part.z
            });
        }
    }

    private static void resetModelToInitial(BedrockModel model) {
        for (Map.Entry<String, BedrockPart> e : model.getModelMap().entrySet()) {
            float[] init = INITIAL_POSE.get(e.getKey());
            if (init == null) continue;
            BedrockPart part = e.getValue();
            part.xRot = init[0];
            part.yRot = init[1];
            part.zRot = init[2];
            part.x = init[3];
            part.y = init[4];
            part.z = init[5];
        }
    }

    // ============================================================
    // 动画应用
    // ============================================================

    private static void applyAnimation(DisplayEntity entity, BedrockModel model, float partialTick) {
        String animName = entity.getAnimation();
        if (animName == null || animName.isEmpty()) return;

        DisplayAnimation anim = DisplayAnimation.get(animName);
        if (anim == null) return;

        float timeSeconds = (entity.tickCount + partialTick) / 20.0F;

        for (Map.Entry<String, java.util.List<DisplayAnimation.Keyframe>> entry : anim.boneKeys.entrySet()) {
            BedrockPart part = model.getModelMap().get(entry.getKey());
            if (part == null) continue;

            float[] s = anim.sampleBone(entry.getKey(), timeSeconds, true);

            float[] init = INITIAL_POSE.get(entry.getKey());
            float baseXRot = init != null ? init[0] : 0f;
            float baseYRot = init != null ? init[1] : 0f;
            float baseZRot = init != null ? init[2] : 0f;
            float baseX    = init != null ? init[3] : 0f;
            float baseY    = init != null ? init[4] : 0f;
            float baseZ    = init != null ? init[5] : 0f;

            part.xRot = baseXRot + (float) Math.toRadians(s[0]);
            part.yRot = baseYRot + (float) Math.toRadians(s[1]);
            part.zRot = baseZRot + (float) Math.toRadians(s[2]);

            part.x = baseX + s[3] / 16f;
            part.y = baseY - s[4] / 16f;
            part.z = baseZ + s[5] / 16f;
        }
    }

    // ============================================================
    // 模型加载
    // ============================================================

    @Nullable
    private static BedrockModel getOrLoadModel() {
        if (cachedModel != null) return cachedModel;
        if (modelLoadFailed) return null;

        try (InputStream in = Minecraft.getInstance().getResourceManager()
                .getResource(MODEL_LOCATION)
                .orElseThrow(() -> new IllegalStateException("Missing " + MODEL_LOCATION))
                .open()) {

            BedrockModel model = new BedrockModel(in);
            cachedModel = model;

            snapshotInitialPose(model);

            BedrockPart head = model.getModelMap().get("Head");
            if (head != null) {
                headInitXRot = head.xRot;
                headInitYRot = head.yRot;
            }
            return model;

        } catch (InvalidVersionSpecificationException e) {
            GoetyDelight.LOGGER.error("display_entity.json 版本不受支持", e);
            modelLoadFailed = true;
            return null;
        } catch (Exception e) {
            GoetyDelight.LOGGER.error("加载 display_entity 基岩模型失败", e);
            modelLoadFailed = true;
            return null;
        }
    }

    // ============================================================
    // 命名牌 -> 皮肤解析
    // ============================================================

    private static void ensureSkinResolved(DisplayEntity entity) {
        Component customName = entity.getCustomName();
        String raw = customName == null ? "" : customName.getString();
        String name = cleanName(raw);

        int revision = entity.getSkinRevision();
        Integer lastRev = LAST_REVISION.get(entity);

        // 命名牌每交互一次 revision 就 +1，即使名字相同也重新触发解析
        boolean interacted = (lastRev == null || lastRev != revision);
        if (interacted) {
            LAST_REVISION.put(entity, revision);
            if (name != null) {
                SKIN_CACHE.remove(name);
                PENDING.remove(name);
                FAILED_AT.remove(name);
            }
        }

        if (name == null || name.isEmpty()) return;

        // wu1wu2 直接走本地贴图
        if (name.equalsIgnoreCase(WU1WU2_NAME)) {
            SKIN_CACHE.put(name, WU1WU2_TEXTURE);
            return;
        }

        // 已成功解析，直接用
        if (SKIN_CACHE.containsKey(name)) return;

        // 解析中
        if (PENDING.containsKey(name)) return;

        // 最近失败过：短时间内不重试
        Long failedAt = FAILED_AT.get(name);
        if (failedAt != null && System.currentTimeMillis() - failedAt < RETRY_INTERVAL_MS) {
            return;
        }

        // 可以解析
        PENDING.put(name, Boolean.TRUE);
        resolveSkinAsync(name);
    }

    private static void resolveSkinAsync(String playerName) {
        CompletableFuture
                .supplyAsync(() -> {
                    try {
                        String uuid = fetchUuid(playerName);
                        if (uuid == null) return null;

                        String skinHash = fetchSkinHash(uuid);
                        if (skinHash == null) return null;

                        if (!isValidSkinSize(skinHash)) return null;

                        return skinHash;
                    } catch (Exception e) {
                        return null;
                    }
                })
                .thenAccept(hash -> {
                    if (hash == null) {
                        // 失败：不写 SKIN_CACHE，只记时间戳，允许后续重试
                        FAILED_AT.put(playerName, System.currentTimeMillis());
                        PENDING.remove(playerName);
                        return;
                    }
                    Minecraft.getInstance().execute(() -> {
                        ResourceLocation loc = registerDynamicSkin(playerName, hash);
                        if (loc != null) {
                            SKIN_CACHE.put(playerName, loc);
                        } else {
                            FAILED_AT.put(playerName, System.currentTimeMillis());
                        }
                        PENDING.remove(playerName);
                    });
                })
                .exceptionally(ex -> {
                    GoetyDelight.LOGGER.error("解析皮肤失败: {}", playerName, ex);
                    FAILED_AT.put(playerName, System.currentTimeMillis());
                    PENDING.remove(playerName);
                    return null;
                });
    }

    @Nullable
    private static ResourceLocation registerDynamicSkin(String playerName, String skinHash) {
        try {
            URL url = new URL("http://textures.minecraft.net/texture/" + skinHash);
            byte[] bytes;
            try (InputStream in = url.openStream()) {
                bytes = in.readAllBytes();
            }

            NativeImage image = NativeImage.read(bytes);
            if (image.getWidth() != 64 || image.getHeight() != 64) {
                image.close();
                return null;
            }

            DynamicTexture dynamicTexture = new DynamicTexture(image);
            ResourceLocation loc = new ResourceLocation(
                    "goetydelight", "skins/" + sanitize(playerName));

            Minecraft.getInstance().getTextureManager().register(loc, dynamicTexture);
            REGISTERED_TEXTURES.put(loc, dynamicTexture);
            return loc;
        } catch (Exception e) {
            GoetyDelight.LOGGER.error("注册动态皮肤失败: {}", playerName, e);
            return null;
        }
    }

    // ============================================================
    // 名字清洗
    // ============================================================

    @Nullable
    private static String cleanName(String raw) {
        if (raw == null) return null;
        String s = raw.replaceAll("§.", "");
        s = s.replaceAll("[\\u200B-\\u200D\\uFEFF]", "");
        s = s.trim();
        return s.isEmpty() ? null : s;
    }

    private static String sanitize(String name) {
        return name.toLowerCase().replaceAll("[^a-z0-9_]", "_");
    }

    // ============================================================
    // Mojang API
    // ============================================================

    @Nullable
    private static String fetchUuid(String username) {
        try {
            URL url = new URL("https://api.mojang.com/users/profiles/minecraft/" + username);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setConnectTimeout(5000);
            conn.setReadTimeout(5000);
            conn.setRequestMethod("GET");

            if (conn.getResponseCode() != 200) return null;

            String json;
            try (InputStream in = conn.getInputStream()) {
                json = new String(in.readAllBytes(), StandardCharsets.UTF_8);
            }

            Matcher m = Pattern.compile("\"id\"\\s*:\\s*\"([0-9a-fA-F]{32})\"").matcher(json);
            return m.find() ? m.group(1) : null;
        } catch (Exception e) {
            return null;
        }
    }

    @Nullable
    private static String fetchSkinHash(String uuid) {
        try {
            URL url = new URL("https://sessionserver.mojang.com/session/minecraft/profile/" + uuid);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setConnectTimeout(5000);
            conn.setReadTimeout(5000);
            conn.setRequestMethod("GET");

            if (conn.getResponseCode() != 200) return null;

            String json;
            try (InputStream in = conn.getInputStream()) {
                json = new String(in.readAllBytes(), StandardCharsets.UTF_8);
            }

            Matcher m = Pattern.compile("\"value\"\\s*:\\s*\"([^\"]+)\"").matcher(json);
            if (!m.find()) return null;

            String decoded = new String(Base64.getDecoder().decode(m.group(1)), StandardCharsets.UTF_8);

            Matcher skinMatcher = Pattern.compile(
                    "\"SKIN\"\\s*:\\s*\\{[^}]*\"url\"\\s*:\\s*\"([^\"]+)\"").matcher(decoded);
            if (!skinMatcher.find()) return null;

            String skinUrl = skinMatcher.group(1);
            return skinUrl.substring(skinUrl.lastIndexOf('/') + 1);
        } catch (Exception e) {
            return null;
        }
    }

    private static boolean isValidSkinSize(String skinHash) {
        try {
            URL url = new URL("http://textures.minecraft.net/texture/" + skinHash);
            try (InputStream in = url.openStream()) {
                NativeImage image = NativeImage.read(in);
                boolean valid = image.getWidth() == 64 && image.getHeight() == 64;
                image.close();
                return valid;
            }
        } catch (Exception e) {
            return false;
        }
    }

    // ============================================================
    // 贴图选择
    // ============================================================

    private static ResourceLocation resolveTexture(DisplayEntity entity) {
        Component customName = entity.getCustomName();
        if (customName != null) {
            String name = cleanName(customName.getString());
            if (name != null) {
                ResourceLocation cached = SKIN_CACHE.get(name);
                if (cached != null) {
                    return cached;
                }
            }
        }
        return DEFAULT_STEVE;
    }

    @Override
    public ResourceLocation getTextureLocation(DisplayEntity entity) {
        return resolveTexture(entity);
    }

    // ============================================================
    // 工具：清空缓存（可用于测试）
    // ============================================================

    public static void clearSkinCache() {
        SKIN_CACHE.clear();
        PENDING.clear();
        FAILED_AT.clear();
    }
}