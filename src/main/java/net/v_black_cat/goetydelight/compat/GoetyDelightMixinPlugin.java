package net.v_black_cat.goetydelight.compat;

import net.minecraftforge.fml.loading.LoadingModList;
import net.minecraftforge.forgespi.language.IModInfo;
import org.apache.maven.artifact.versioning.DefaultArtifactVersion;
import org.objectweb.asm.tree.ClassNode;
import org.spongepowered.asm.mixin.extensibility.IMixinConfigPlugin;
import org.spongepowered.asm.mixin.extensibility.IMixinInfo;

import java.util.List;
import java.util.Set;

public class GoetyDelightMixinPlugin implements IMixinConfigPlugin {

    /**
     * 不要在这里引用 {@code EndingLibraryCompat.ID}：
     * mixin 配置插件在极早期类加载阶段执行，引用那个类会连带初始化它的 Maven 版本比较依赖。
     * 一旦其中任何一个类在早期不可用，插件类初始化失败，Mixin 会把整个配置当成不可用，
     * 表现就是“所有 mixin 一起失效”。这里用纯字符串常量。
     */
    private static final String ENDING_LIBRARY_MODID = "ending_library";

    @Override
    public void onLoad(String mixinPackage) {
    }

    @Override
    public String getRefMapperConfig() {
        return null;
    }

    @Override
    public boolean shouldApplyMixin(String targetClassName, String mixinClassName) {
        if (mixinClassName.contains("SummonApollyonMixin") || mixinClassName.contains("TheNeedleItemMixin")) {
            return isModFilePresent("goety_revelation");
        }

        if (mixinClassName.contains("DisplayEntityTimeStopMixin")) {
            // 该 mixin 直接 implements EndingLibrary 的 TimeStopEntity。
            // 旧版本没有这个接口时，mixin 类加载就会失败，因此按版本门控，而不是只看 mod 是否存在。
            return isModAtLeast(ENDING_LIBRARY_MODID, "2.2.2");
        }

        if (mixinClassName.contains("CarryOnListHandlerMixin")) {
            return isModFilePresent("carryon");
        }

        if (mixinClassName.contains("SophisticatedFeeding")) {
            return isModFilePresent("sophisticatedcore");
        }

        if (mixinClassName.contains("TravelersBackpackFeeding")) {
            return isModFilePresent("travelersbackpack");
        }

        if (mixinClassName.contains("FtbUltimineVanillaCropMixin")) {
            return isModFilePresent("ftbultimine") && isModFilePresent("goety_revelation");
        }

        if (mixinClassName.contains("RightClickGetCropsMixin")) {
            return isModFilePresent("right_click_get_crops") && isModFilePresent("goety_revelation");
        }

        return true;
    }

    /**
     * LoadingModList 在极少数启动阶段可能尚未初始化；直接链式调用会抛 NPE，
     * 而 IMixinConfigPlugin 里抛异常可能被 Mixin 当成“整个配置不可用”，
     * 表现为所有 mixin 突然一起失效。这里统一做空值/异常保护。
     */
    private static boolean isModFilePresent(String modId) {
        try {
            LoadingModList modList = LoadingModList.get();
            return modList != null && modList.getModFileById(modId) != null;
        } catch (Throwable ignored) {
            return false;
        }
    }

    private static boolean isModAtLeast(String modId, String minVersion) {
        try {
            LoadingModList modList = LoadingModList.get();
            if (modList == null) {
                return false;
            }
            var modFile = modList.getModFileById(modId);
            if (modFile == null) {
                return false;
            }
            DefaultArtifactVersion minimum = new DefaultArtifactVersion(minVersion);
            for (IModInfo info : modFile.getMods()) {
                if (modId.equals(info.getModId())
                        && info.getVersion().compareTo(minimum) >= 0) {
                    return true;
                }
            }
        } catch (Throwable ignored) {
            // 版本信息不可用时保守地不应用该 mixin，避免整个配置被连带禁用。
        }
        return false;
    }

    @Override
    public void acceptTargets(Set<String> myTargets, Set<String> otherTargets) {
    }

    @Override
    public List<String> getMixins() {
        return null;
    }

    @Override
    public void preApply(String targetClassName, ClassNode targetClass,
                         String mixinClassName, IMixinInfo mixinInfo) {
    }

    @Override
    public void postApply(String targetClassName, ClassNode targetClass,
                          String mixinClassName, IMixinInfo mixinInfo) {
    }
}