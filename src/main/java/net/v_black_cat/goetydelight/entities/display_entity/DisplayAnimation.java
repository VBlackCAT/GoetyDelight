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
 */
@OnlyIn(Dist.CLIENT)
public final class DisplayAnimation {

    /** 动画名 -> 动画数据（解析后缓存） */
    private static final Map<String, DisplayAnimation> CACHE = new ConcurrentHashMap<>();

    /** 明确标记"加载失败"，避免重复 IO */
    private static final DisplayAnimation FAILED = new DisplayAnimation(0f, Map.of());

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
        if (cached == FAILED) return null;
        if (cached != null) return cached;

        DisplayAnimation loaded = load(name);
        CACHE.put(name, loaded != null ? loaded : FAILED);
        return loaded;
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

            // 取第一个动画条目（一个文件一个动画）
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

                    collectRotation(bone.get("rotation"), rotMap);
                    collectPosition(bone.get("position"), posMap);

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

            GoetyDelight.LOGGER.info("已加载动画 '{}'，length={}s，骨骼数={}",
                    name, length, boneKeys.size());
            return new DisplayAnimation(length, boneKeys);

        } catch (Exception e) {
            GoetyDelight.LOGGER.error("加载动画失败: {}", name, e);
            return null;
        }
    }

    /** 收集 rotation：可能是 [x,y,z] 常量，也可能是 {"t":[x,y,z]} */
    private static void collectRotation(JsonElement el, TreeMap<Float, float[]> out) {
        if (el == null) return;
        if (el.isJsonArray()) {
            JsonArray a = el.getAsJsonArray();
            out.put(0f, new float[]{a.get(0).getAsFloat(), a.get(1).getAsFloat(), a.get(2).getAsFloat()});
            return;
        }
        if (el.isJsonObject()) {
            for (var e : el.getAsJsonObject().entrySet()) {
                float t = Float.parseFloat(e.getKey());
                JsonArray a = e.getValue().getAsJsonArray();
                out.put(t, new float[]{a.get(0).getAsFloat(), a.get(1).getAsFloat(), a.get(2).getAsFloat()});
            }
        }
    }

    /** 收集 position：格式同 rotation */
    private static void collectPosition(JsonElement el, TreeMap<Float, float[]> out) {
        if (el == null) return;
        if (el.isJsonArray()) {
            JsonArray a = el.getAsJsonArray();
            out.put(0f, new float[]{a.get(0).getAsFloat(), a.get(1).getAsFloat(), a.get(2).getAsFloat()});
            return;
        }
        if (el.isJsonObject()) {
            for (var e : el.getAsJsonObject().entrySet()) {
                float t = Float.parseFloat(e.getKey());
                JsonArray a = e.getValue().getAsJsonArray();
                out.put(t, new float[]{a.get(0).getAsFloat(), a.get(1).getAsFloat(), a.get(2).getAsFloat()});
            }
        }
    }

    // ------------------------------------------------------------------
    // 采样
    // ------------------------------------------------------------------

    /**
     * 在时间 t（秒）处采样一根骨骼。
     *
     * @param loop 是否循环
     * @return float[6]：rx,ry,rz（角度）, px,py,pz（Bedrock 像素，y 向下）
     */
    public float[] sampleBone(String boneName, float t, boolean loop) {
        List<Keyframe> keys = boneKeys.get(boneName);
        if (keys == null || keys.isEmpty()) {
            return new float[]{0, 0, 0, 0, 0, 0};
        }

        if (loop && length > 0f) {
            t = t % length;
        } else {
            t = Mth.clamp(t, 0f, length > 0f ? length : keys.get(keys.size() - 1).time);
        }

        Keyframe prev = keys.get(0);
        Keyframe next = keys.get(keys.size() - 1);

        for (Keyframe k : keys) {
            if (k.time <= t) prev = k;
            if (k.time >= t) {
                next = k;
                break;
            }
        }

        if (prev == next || next.time == prev.time) {
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

    /** 只有两端都有值才插值，否则取存在的那个 */
    private static float lerpIf(boolean aHas, boolean bHas, float f, float a, float b) {
        if (aHas && bHas) return Mth.lerp(f, a, b);
        if (aHas) return a;
        if (bHas) return b;
        return 0f;
    }
}