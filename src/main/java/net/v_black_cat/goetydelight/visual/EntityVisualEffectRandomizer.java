package net.v_black_cat.goetydelight.visual;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.FloatTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.RandomSource;

import java.util.ArrayList;
import java.util.List;

/**
 * 调试用：为特效生成随机 data。
 *
 * <p>每个特效只随机它<b>真正会读取</b>的键（取值都落在渲染器/shader 的 clamp 范围内），
 * 所以随机出来的组合一定是"有效"的，不会出现传了没人读的键、或者数值被 clamp 掉的情况。
 * 改渲染器里读取的键时记得同步这里。
 */
public final class EntityVisualEffectRandomizer {

    private EntityVisualEffectRandomizer() {
    }

    /** 从注册表里随机挑 {@code count} 个不重复的特效。 */
    public static List<ResourceLocation> pickRandom(RandomSource random, int count) {
        List<ResourceLocation> pool = new ArrayList<>(GDVisualEffects.registeredIds());
        int size = Math.min(count, pool.size());

        // Fisher–Yates：只洗前 size 个
        for (int i = 0; i < size; i++) {
            int j = i + random.nextInt(pool.size() - i);
            ResourceLocation picked = pool.get(j);
            pool.set(j, pool.get(i));
            pool.set(i, picked);
        }

        return new ArrayList<>(pool.subList(0, size));
    }

    /** 按特效实际读取的键生成一组随机数值。 */
    public static CompoundTag randomData(RandomSource random, ResourceLocation effectId) {
        CompoundTag data = new CompoundTag();

        switch (effectId.getPath()) {
            // ── 屏幕空间：通用深度特效（mode 0-6），只吃 Radius ──
            case "screen_space_shockwave", "depth_refraction_heatwave", "outline_scan",
                 "depth_occluded_halo", "contact_edge_glow", "volumetric_light_column",
                 "depth_refraction_pressure" -> data.putFloat("Radius", range(random, 3.0F, 14.0F));

            // ── 不义游戏 ──
            case "malevolent_shrine_domain", "malevolent_shrine_slash" -> {
                data.putFloat("Radius", range(random, 6.0F, 18.0F));
                data.putFloat("Height", range(random, 5.0F, 16.0F));
                data.putFloat("Intensity", range(random, 0.6F, 1.6F));
            }
            case "malevolent_shrine_target_glow" -> data.putFloat("Radius", range(random, 0.8F, 2.5F));
            case "malevolent_shrine_fire", "malevolent_shrine_fire_legacy",
                 "malevolent_shrine_black_domain", "malevolent_shrine_black_mist" ->
                    data.putFloat("Radius", range(random, 5.0F, 14.0F));
            case "malevolent_shrine_void", "malevolent_shrine_starfield" ->
                    data.putFloat("Radius", range(random, 4.0F, 12.0F));
            // 宇宙领域：只吃 Radius / Intensity（盘面倾角与自转由 shader 随时间推进）
            case "cosmic_domain" -> {
                data.putFloat("Radius", range(random, 7.0F, 18.0F));
                data.putFloat("Intensity", range(random, 0.7F, 1.8F));
            }
            // 寂灭之月：Radius / Intensity + Tint（随机月面色调，银灰 ~ 血月都有可能）
            case "lunar_domain" -> {
                data.putFloat("Radius", range(random, 9.0F, 20.0F));
                data.putFloat("Intensity", range(random, 0.7F, 1.6F));
                data.put("Tint", randomColor(random, 0.35F, 1.0F));
            }

            // ── 猫雾：Scale 控体积，Yaw 控朝向，FogColor 控颜色 ──
            case "black_cat_head_fog_field" -> {
                data.putFloat("Scale", range(random, 0.8F, 3.5F));
                data.putFloat("Yaw", random.nextFloat() * 360.0F);
                data.putFloat("YOffset", range(random, -0.4F, 1.2F));
                data.put("FogColor", randomColor(random, 0.15F, 0.65F));
            }

            // ── 逐实体渲染 ──
            case "block_crack_light" -> {
                data.putFloat("Radius", range(random, 4.0F, 12.0F));
                data.putFloat("Intensity", range(random, 0.6F, 2.0F));
            }
            case "red_eye_flash" -> {
                data.putFloat("Scale", range(random, 0.8F, 2.5F));
                data.putFloat("Intensity", range(random, 0.6F, 2.2F));
                data.putFloat("ForwardOffset", range(random, -0.4F, 0.6F));
                data.putFloat("SideOffset", range(random, -0.3F, 0.3F));
            }
            case "tilted_halo" -> {
                data.putFloat("Scale", range(random, 0.7F, 2.2F));
                data.putFloat("Intensity", range(random, 0.6F, 2.0F));
                data.putFloat("BehindOffset", range(random, -0.5F, 1.0F));
                data.putFloat("YOffset", range(random, 0.2F, 1.2F));
                data.putFloat("TiltDegrees", range(random, -60.0F, 60.0F));
            }
            case "doom_corona", "abyssal_rift_eye", "holy_judgement_halo", "astral_crown",
                 "blood_moon_backwheel", "causal_chains", "inverted_cross_mark" -> {
                data.putFloat("Scale", range(random, 0.7F, 2.0F));
                data.putFloat("Intensity", range(random, 0.6F, 2.4F));
                data.putFloat("BehindOffset", range(random, -0.5F, 1.2F));
                data.putFloat("YOffset", range(random, 0.2F, 1.4F));
            }
            case "volumetric_flame" -> {
                data.put("Color", randomColor(random, 0.25F, 1.0F));
                data.put("CoreColor", randomColor(random, 0.65F, 1.0F));
                data.put("TipColor", randomColor(random, 0.35F, 1.0F));
                data.put("SmokeColor", randomColor(random, 0.01F, 0.08F));
                data.putFloat("Width", range(random, 0.4F, 2.5F));
                data.putFloat("Depth", range(random, 0.4F, 2.5F));
                data.putFloat("Height", range(random, 0.8F, 5.0F));
                data.putFloat("Scale", range(random, 0.7F, 2.0F));
                data.putFloat("Intensity", range(random, 0.6F, 2.0F));
            }
            case "phantom_rift_shards" -> {
                data.putFloat("Scale", range(random, 0.7F, 2.2F));
                data.putFloat("Intensity", range(random, 0.6F, 2.4F));
                data.putInt("ShardCount", 3 + random.nextInt(12));
                data.putFloat("OrbitRadius", range(random, 0.5F, 3.0F));
                data.putFloat("BehindOffset", range(random, -0.5F, 1.2F));
                data.putFloat("YOffset", range(random, 0.2F, 1.4F));
                data.putFloat("TiltDegrees", range(random, -60.0F, 60.0F));
            }
            case "supreme_chaos_cosmos" -> {
                data.putFloat("Scale", range(random, 0.7F, 2.2F));
                data.putFloat("Intensity", range(random, 0.6F, 2.4F));
                data.putFloat("BehindOffset", range(random, -0.5F, 1.2F));
                data.putFloat("YOffset", range(random, 0.2F, 1.4F));
                data.putFloat("Tilt1Degrees", range(random, -60.0F, 60.0F));
                data.putFloat("Tilt2Degrees", range(random, -60.0F, 60.0F));
            }

            // orbit_sphere / helix_trail / soft_trail 不吃 data
            default -> {
            }
        }

        return data;
    }

    private static float range(RandomSource random, float min, float max) {
        return min + random.nextFloat() * (max - min);
    }

    private static ListTag randomColor(RandomSource random, float min, float max) {
        ListTag list = new ListTag();
        list.add(FloatTag.valueOf(range(random, min, max)));
        list.add(FloatTag.valueOf(range(random, min, max)));
        list.add(FloatTag.valueOf(range(random, min, max)));
        return list;
    }
}
