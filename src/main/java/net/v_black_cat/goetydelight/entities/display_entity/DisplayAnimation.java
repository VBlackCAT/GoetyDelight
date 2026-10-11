package net.v_black_cat.goetydelight.entities.display_entity;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.v_black_cat.goetydelight.GoetyDelight;

import javax.annotation.Nullable;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 基岩版 animation JSON 的解析与采样。
 *
 * 文件位置：assets/goetydelight/models/entity_animation/<name>.json
 *
 * 支持的关键帧形态：
 *   1) 顶层常量数组：       "rotation": [x, y, z]
 *   2) 时间 -> 数组：        "rotation": { "0.0": [x,y,z], "0.5": [x,y,z] }
 *   3) 时间 -> {pre,post}：  "rotation": { "0.375": {"pre":[...], "post":[...], "lerp_mode":"catmullrom"} }
 *
 * 目前 lerp_mode 统一按线性插值处理；如需 catmullrom，可后续在 sampleBone 里扩展。
 */
@OnlyIn(Dist.CLIENT)
public final class DisplayAnimation {

    /** 动画名 -> 动画数据（解析后缓存） */
    private static final Map<String, DisplayAnimation> CACHE = new ConcurrentHashMap<>();

    /** 动画名 -> 上次加载失败时间戳（用于失败重试） */
    private static final Map<String, Long> FAILED_AT = new ConcurrentHashMap<>();

    /** 失败后多久允许重试（毫秒） */
    private static final long RETRY_INTERVAL_MS = 30_000L;

    // ------------------------------------------------------------------
    // 数据结构
    // ------------------------------------------------------------------

    public record Keyframe(float time,
                           float rx, float ry, float rz,
                           float px, float py, float pz,
                           boolean hasPos, boolean hasRot) {}

    public final float length;
    /** 骨骼名 -> 该骨骼的关键帧列表（按时间升序） */
    public final Map<String, List<Keyframe>> boneKeys;

    private DisplayAnimation(float length, Map<String, List<Keyframe>> boneKeys) {
        this.length = length;
        this.boneKeys = boneKeys;
    }

    // ------------------------------------------------------------------
    // 加载
    // ------------------------------------------------------------------

    @Nullable
    public static DisplayAnimation get(String name) {
        if (name == null || name.isEmpty()) return null;

        DisplayAnimation cached = CACHE.get(name);
        if (cached != null) return cached;

        Long failedAt = FAILED_AT.get(name);
        if (failedAt != null && System.currentTimeMillis() - failedAt < RETRY_INTERVAL_MS) {
            return null;
        }

        DisplayAnimation loaded = load(name);
        if (loaded != null) {
            CACHE.put(name, loaded);
            FAILED_AT.remove(name);
        } else {
            FAILED_AT.put(name, System.currentTimeMillis());
        }
        return loaded;
    }

    /** 资源重载时清空缓存，下次 get 重新从磁盘读 */
    public static void clearCache() {
        CACHE.clear();
        FAILED_AT.clear();
    }

    @Nullable
    private static DisplayAnimation load(String name) {
        ResourceLocation loc = new ResourceLocation(
                "goetydelight", "models/entity_animation/" + name + ".json");

        try (InputStream in = Minecraft.getInstance().getResourceManager()
                .getResource(loc)
                .orElseThrow(() -> new IllegalStateException("Missing " + loc))
                .open()) {

            JsonObject root = JsonParser.parseReader(
                    new InputStreamReader(in, StandardCharsets.UTF_8)).getAsJsonObject();

            JsonObject animations = root.getAsJsonObject("animations");
            if (animations == null || animations.size() == 0) {
                GoetyDelight.LOGGER.warn("动画文件 {} 里没有 animations 字段", name);
                return null;
            }

            // 一个文件里可能定义多个动画，这里只取第一个
            JsonObject anim = animations.entrySet().iterator().next()
                    .getValue().getAsJsonObject();

            float length = anim.has("animation_length")
                    ? anim.get("animation_length").getAsFloat() : 0f;

            Map<String, List<Keyframe>> boneKeys = new HashMap<>();

            JsonObject bones = anim.getAsJsonObject("bones");
            if (bones != null) {
                for (var entry : bones.entrySet()) {
                    String boneName = entry.getKey();
                    JsonObject bone = entry.getValue().getAsJsonObject();

                    TreeMap<Float, float[]> rotMap = new TreeMap<>();
                    TreeMap<Float, float[]> posMap = new TreeMap<>();

                    collectChannel(bone.get("rotation"), rotMap);
                    collectChannel(bone.get("position"), posMap);

                    TreeSet<Float> times = new TreeSet<>();
                    times.addAll(rotMap.keySet());
                    times.addAll(posMap.keySet());

                    List<Keyframe> keys = new ArrayList<>(times.size());
                    for (float t : times) {
                        float[] rot = rotMap.get(t);
                        float[] pos = posMap.get(t);
                        keys.add(new Keyframe(
                                t,
                                rot != null ? rot[0] : 0f,
                                rot != null ? rot[1] : 0f,
                                rot != null ? rot[2] : 0f,
                                pos != null ? pos[0] : 0f,
                                pos != null ? pos[1] : 0f,
                                pos != null ? pos[2] : 0f,
                                pos != null,
                                rot != null
                        ));
                    }
                    boneKeys.put(boneName, keys);
                }
            }
            return new DisplayAnimation(length, boneKeys);

        } catch (Exception e) {
            return null;
        }
    }

    /**
     * 统一收集 rotation / position 通道。
     *
     * 支持三种形态：
     *   [x, y, z]                              -> t = 0 的常量
     *   { "0.0": [x,y,z], ... }                -> 时间表
     *   { "0.375": {pre, post, lerp_mode}, ...}-> 时间表（取 post，没有则取 pre）
     */
    private static void collectChannel(JsonElement el, TreeMap<Float, float[]> out) {
        if (el == null || el.isJsonNull()) return;

        // 形态 1：顶层常量数组
        if (el.isJsonArray()) {
            JsonArray a = el.getAsJsonArray();
            if (a.size() < 3) return;
            out.put(0f, new float[]{
                    a.get(0).getAsFloat(),
                    a.get(1).getAsFloat(),
                    a.get(2).getAsFloat()
            });
            return;
        }

        if (!el.isJsonObject()) return;

        JsonObject obj = el.getAsJsonObject();

        // 形态 2 / 3：key 必须是时间
        for (var e : obj.entrySet()) {
            float t;
            try {
                t = Float.parseFloat(e.getKey());
            } catch (NumberFormatException ex) {
                // key 不是数字，跳过（防止 pre/post 被当成时间）
                continue;
            }

            JsonElement v = e.getValue();
            float[] xyz = readXYZ(v);
            if (xyz != null) {
                out.put(t, xyz);
            }
        }
    }

    /** 从 JsonElement 里读出 [x,y,z]；支持数组和 {pre,post} 两种 */
    @Nullable
    private static float[] readXYZ(JsonElement v) {
        if (v == null || v.isJsonNull()) return null;

        if (v.isJsonArray()) {
            JsonArray a = v.getAsJsonArray();
            if (a.size() < 3) return null;
            return new float[]{
                    a.get(0).getAsFloat(),
                    a.get(1).getAsFloat(),
                    a.get(2).getAsFloat()
            };
        }

        if (v.isJsonObject()) {
            JsonObject o = v.getAsJsonObject();
            JsonArray a = null;
            // 优先 post，其次 pre
            if (o.has("post") && o.get("post").isJsonArray()) {
                a = o.getAsJsonArray("post");
            } else if (o.has("pre") && o.get("pre").isJsonArray()) {
                a = o.getAsJsonArray("pre");
            }
            if (a == null || a.size() < 3) return null;
            return new float[]{
                    a.get(0).getAsFloat(),
                    a.get(1).getAsFloat(),
                    a.get(2).getAsFloat()
            };
        }

        return null;
    }

    // ------------------------------------------------------------------
    // 采样
    // ------------------------------------------------------------------

    /**
     * 在时间 t（秒）处采样一根骨骼。
     *
     * @param loop 是否循环
     * @return float[6]：rx,ry,rz（角度），px,py,pz（Bedrock 像素）
     */
    public float[] sampleBone(String boneName, float t, boolean loop) {
        List<Keyframe> keys = boneKeys.get(boneName);
        if (keys == null || keys.isEmpty()) {
            return new float[]{0, 0, 0, 0, 0, 0};
        }

        // 有效长度：优先用 animation_length，没有就用最后一帧时间
        float effLength = length > 0f ? length : keys.get(keys.size() - 1).time;

        if (loop && effLength > 0f) {
            t = t % effLength;
            if (t < 0) t += effLength;
        } else {
            t = Mth.clamp(t, 0f, effLength);
        }

        // 单关键帧：直接返回
        if (keys.size() == 1) {
            Keyframe k = keys.get(0);
            return new float[]{
                    k.hasRot ? k.rx : 0f,
                    k.hasRot ? k.ry : 0f,
                    k.hasRot ? k.rz : 0f,
                    k.hasPos ? k.px : 0f,
                    k.hasPos ? k.py : 0f,
                    k.hasPos ? k.pz : 0f
            };
        }

        Keyframe prev = keys.get(0);
        Keyframe next = keys.get(keys.size() - 1);

        for (Keyframe k : keys) {
            if (k.time <= t) {
                prev = k;
            }
            if (k.time >= t) {
                next = k;
                break;
            }
        }

        if (prev == next || next.time <= prev.time) {
            return new float[]{
                    prev.hasRot ? prev.rx : 0f,
                    prev.hasRot ? prev.ry : 0f,
                    prev.hasRot ? prev.rz : 0f,
                    prev.hasPos ? prev.px : 0f,
                    prev.hasPos ? prev.py : 0f,
                    prev.hasPos ? prev.pz : 0f
            };
        }

        float f = (t - prev.time) / (next.time - prev.time);

        return new float[]{
                lerpIf(prev.hasRot, next.hasRot, f, prev.rx, next.rx),
                lerpIf(prev.hasRot, next.hasRot, f, prev.ry, next.ry),
                lerpIf(prev.hasRot, next.hasRot, f, prev.rz, next.rz),
                lerpIf(prev.hasPos, next.hasPos, f, prev.px, next.px),
                lerpIf(prev.hasPos, next.hasPos, f, prev.py, next.py),
                lerpIf(prev.hasPos, next.hasPos, f, prev.pz, next.pz)
        };
    }

    /** 只有两端都有值才插值，否则取存在的那个；都没有返回 0 */
    private static float lerpIf(boolean aHas, boolean bHas, float f, float a, float b) {
        if (aHas && bHas) return Mth.lerp(f, a, b);
        if (aHas) return a;
        if (bHas) return b;
        return 0f;
    }
}