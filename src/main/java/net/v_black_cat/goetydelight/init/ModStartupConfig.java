package net.v_black_cat.goetydelight.init;

import net.neoforged.neoforge.common.ModConfigSpec;

/**
 * 启动时配置：在游戏启动早期读取，用于决定注册/加载行为，改动需要重启游戏。
 * <p>
 * 对应配置文件：goetydelight-startup.toml
 */
public class ModStartupConfig {
    private static final ModConfigSpec.Builder BUILDER = new ModConfigSpec.Builder();

    // ==================== Goety Revelation 兼容性 ====================
    private static final ModConfigSpec.BooleanValue ENABLE_GOETY_REVELATION_COMPATIBILITY = BUILDER
            .comment("Whether to enable compatibility with goety_revelation mod\n是否启用与goety_revelation模组的兼容性")
            .define("enableGoetyRevelationCompatibility", false);

    // ==================== 构建 Spec ====================
    public static final ModConfigSpec SPEC = BUILDER.build();

    // ==================== Getter / 辅助方法 ====================

    public static boolean isGoetyRevelationCompatibilityEnabled() {
        return ENABLE_GOETY_REVELATION_COMPATIBILITY.get();
    }
}
