package net.v_black_cat.goetydelight.compat.farmersdelight;

import net.minecraftforge.fml.ModList;
import org.apache.maven.artifact.versioning.ArtifactVersion;
import org.apache.maven.artifact.versioning.DefaultArtifactVersion;

/**
 * Farmer's Delight 联动信息入口。
 * <p>
 * 注意：<b>不要</b>用这里的版本判断来决定是否引用 {@code ModTags.Blocks} /
 * {@code ModTags.Items}。dev 环境 classpath 上可能同时存在新旧两个 FD jar，
 * {@code ModList} 版本字符串与 classpath 类探测都可能偏新，导致运行时
 * {@code NoClassDefFoundError}。
 * <p>
 * 标签判断请统一走 {@link FarmersDelightTrayTags} / {@link FarmersDelightItemTags}，
 * 它们内部用反射"先新后旧"探测，不依赖版本号。
 */
public final class FarmersDelightCompat {

    public static final String ID = "farmersdelight";
    public static final String MIN_VERSION = "1.3.0";

    private static final ArtifactVersion MIN_ARTIFACT_VERSION =
            new DefaultArtifactVersion(MIN_VERSION);

    private FarmersDelightCompat() {
    }

    public static ArtifactVersion getVersion() {
        return ModList.get().getModContainerById(ID)
                .map(container -> container.getModInfo().getVersion())
                .orElse(null);
    }

    /** 仅用于日志/展示，<b>不要</b>用于决定是否加载新类。 */
    public static boolean isAtLeastMinVersion() {
        ArtifactVersion v = getVersion();
        return v != null && v.compareTo(MIN_ARTIFACT_VERSION) >= 0;
    }

    public static boolean isAtLeast(String minVersion) {
        ArtifactVersion v = getVersion();
        if (v == null) {
            return false;
        }
        return v.compareTo(new DefaultArtifactVersion(minVersion)) >= 0;
    }
}