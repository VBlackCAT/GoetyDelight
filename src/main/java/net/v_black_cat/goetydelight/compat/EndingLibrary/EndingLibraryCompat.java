package net.v_black_cat.goetydelight.compat.EndingLibrary;

import net.minecraftforge.fml.ModList;
import org.apache.maven.artifact.versioning.ArtifactVersion;
import org.apache.maven.artifact.versioning.DefaultArtifactVersion;

/**
 * EndingLibrary 联动模块入口。
 * <p>
 * 本类<b>不引用任何 EndingLibrary 的类</b>，仅通过 Forge 的 ModList 读取元数据，
 * 因此 EndingLibrary 缺失时本类也能安全加载（相关方法返回 false / null）。
 * <p>
 * 版本判断入口统一放在这里，供 ApollyonCakeSliceItem、DisplayEntityRender、
 * DisplayEntityTimeStopMixin 等调用。
 */
public final class EndingLibraryCompat {

    public static final String ID = "ending_library";

    /** 关键版本：2.2.2，低于此版本时联动功能不启用 */
    public static final String MIN_VERSION = "2.2.2";

    private static final ArtifactVersion MIN_ARTIFACT_VERSION =
            new DefaultArtifactVersion(MIN_VERSION);

    private EndingLibraryCompat() {
    }

    /** EndingLibrary 是否已加载（任意版本） */
    public static boolean isLoaded() {
        return ModList.get().getModContainerById(ID).isPresent();
    }

    /** 获取 EndingLibrary 的版本号，未加载返回 null */
    public static ArtifactVersion getVersion() {
        return ModList.get().getModContainerById(ID)
                .map(container -> container.getModInfo().getVersion())
                .orElse(null);
    }

    /**
     * EndingLibrary 是否已加载且版本 >= 2.2.2。
     * 这是 ApollyonCakeSliceItem 召唤 DisplayEntity、
     * DisplayEntityRender 预缓存皮肤、以及 TimeStop Mixin 运行的总开关。
     */
    public static boolean isAtLeastMinVersion() {
        ArtifactVersion v = getVersion();
        if (v == null) {
            return false;
        }
        return v.compareTo(MIN_ARTIFACT_VERSION) >= 0;
    }

    /** 便捷方法：EndingLibrary 是否已加载且版本 >= 指定版本 */
    public static boolean isAtLeast(String minVersion) {
        ArtifactVersion v = getVersion();
        if (v == null) {
            return false;
        }
        return v.compareTo(new DefaultArtifactVersion(minVersion)) >= 0;
    }
}