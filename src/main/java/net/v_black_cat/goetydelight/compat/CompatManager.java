package net.v_black_cat.goetydelight.compat;

import net.neoforged.fml.ModList;
import net.v_black_cat.goetydelight.GoetyDelight;
import net.v_black_cat.goetydelight.init.ModStartupConfig;

public final class CompatManager {

    private CompatManager() {
    }

    // ==================== 通用查询 ====================

    public static boolean isLoaded(String modId) {
        return ModList.get().isLoaded(modId);
    }

    // ==================== 已有联动（保留） ====================

    public static boolean isCuriosLoaded() {
        return isLoaded("curios");
    }

    public static boolean isJadeLoaded() {
        return isLoaded("jade");
    }

    /** Oculus（Forge 端 Iris）——光影相关的客户端联动 */
    public static boolean isShaderModLoaded() {
        return isLoaded("oculus") || isLoaded("iris");
    }

    // ==================== 当前目标联动 ====================
    public static boolean isEnigmaticDelicacyLoaded() {
        return isLoaded("enigmaticdelicacy");
    }

    public static boolean isGoetyCataclysmLoaded() {
        return isLoaded("goety_cataclysm");
    }

    public static boolean isGoetyRevelationLoaded() {
        return isLoaded("goety_revelation");
    }

    // ==================== "存在 + 启动配置开启" 组合判断（仅运行期调用） ====================

    /** goety_revelation 联动是否真正生效（模组存在，且启动配置没有关掉它） */
    public static boolean isGoetyRevelationCompatEnabled() {
        return isGoetyRevelationLoaded() && ModStartupConfig.isGoetyRevelationCompatibilityEnabled();
    }

    // ==================== 启动日志 ====================

    /** 打印当前挂载的联动，便于排查"为什么某联动没生效" */
    public static void logStatus() {
        StringBuilder compatload = new StringBuilder();
        append(compatload, "curios", isCuriosLoaded());
        append(compatload, "jade", isJadeLoaded());
        append(compatload, "oculus/iris", isShaderModLoaded());
        append(compatload, "enigmaticdelicacy", isEnigmaticDelicacyLoaded());
        append(compatload, "goety_cataclysm", isGoetyCataclysmLoaded());
        append(compatload, "goety_revelation", isGoetyRevelationLoaded());
        GoetyDelight.LOGGER.info("[compat] 已检测到联动：{}", compatload.length() == 0 ? "（无）" : compatload.toString());
    }

    private static void append(StringBuilder compatload, String name, boolean present) {
        if (present) {
            if (compatload.length() > 0) {
                compatload.append(", ");
            }
            compatload.append(name);
        }
    }
}
