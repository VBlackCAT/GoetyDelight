package net.v_black_cat.goetydelight.init;

import java.util.*;
import java.util.stream.Collectors;
import net.neoforged.neoforge.common.ModConfigSpec;

/**
 * 客户端配置：只影响本机表现（渲染/界面），不参与服务端同步。
 * <p>
 * 对应配置文件：goetydelight-client.toml
 */
public class ModClientConfig {
    private static final ModConfigSpec.Builder BUILDER = new ModConfigSpec.Builder();

    // ==================== 玩家模型缩放 ====================
    private static final ModConfigSpec.ConfigValue<List<? extends String>> PLAYER_MODEL_SCALES = BUILDER
            .comment("Player model scale settings (format: playerName=scale)\n玩家模型缩放设置（格式：玩家名称=缩放比例）\n注：请勿在高版本ysm中使用该功能（2.6.2版本可用，2.6.5版本不可用）")
            .defineListAllowEmpty("playerModelScales", List.of(
                    "Steve=1.0", "Alex=1.0", "wu1wu2=1.0"
            ), ModClientConfig::validatePlayerScaleEntry);

    // ==================== 骷髅红眼特效 ====================
    private static final ModConfigSpec.BooleanValue SKELETON_RED_EYE_EFFECT_ENABLED = BUILDER
            .comment("Whether to enable the skeleton red-eye effect (red eye flash when a skeleton targets a low-health player)\n是否启用骷髅红眼特效（骷髅锁定低血量玩家时触发的红眼闪光特效）")
            .define("skeletonRedEyeEffectEnabled", false);

    // ==================== 构建 Spec ====================
    public static final ModConfigSpec SPEC = BUILDER.build();

    // ==================== Getter / 辅助方法 ====================

    public static Map<String, Float> getPlayerModelScales() {
        return PLAYER_MODEL_SCALES.get().stream()
                .map(entry -> entry.split("="))
                .filter(parts -> parts.length == 2)
                .collect(Collectors.toMap(
                        parts -> parts[0].trim(),
                        parts -> Float.parseFloat(parts[1].trim())
                ));
    }

    public static boolean isSkeletonRedEyeEffectEnabled() {
        return SKELETON_RED_EYE_EFFECT_ENABLED.get();
    }

    private static boolean validatePlayerScaleEntry(final Object obj) {
        if (!(obj instanceof String entry)) return false;
        String[] parts = entry.split("=");
        if (parts.length != 2) return false;
        try {
            float scale = Float.parseFloat(parts[1].trim());
            return scale > 0;
        } catch (NumberFormatException e) {
            return false;
        }
    }
}
