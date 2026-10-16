package net.v_black_cat.goetydelight.compat.goetyrevelation;

import net.minecraftforge.fml.loading.LoadingModList;
import net.v_black_cat.goetydelight.compat.EndingLibrary.EndingLibraryCompat;
import org.objectweb.asm.tree.ClassNode;
import org.spongepowered.asm.mixin.extensibility.IMixinConfigPlugin;
import org.spongepowered.asm.mixin.extensibility.IMixinInfo;

import java.util.List;
import java.util.Set;

public class RevelationMixinPlugin implements IMixinConfigPlugin {

    /** EndingLibrary 的 modid */
    private static final String ENDING_LIBRARY_MODID = EndingLibraryCompat.ID;

    @Override
    public void onLoad(String mixinPackage) {
    }

    @Override
    public String getRefMapperConfig() {
        return null;
    }

    @Override
    public boolean shouldApplyMixin(String targetClassName, String mixinClassName) {
        // goety_revelation 相关的 Mixin
        if (mixinClassName.contains("SummonApollyonMixin")) {
            return LoadingModList.get().getModFileById("goety_revelation") != null;
        }

        // EndingLibrary 相关的 Mixin：只有 EL 加载且版本 >= 2.2.2 时才应用
        if (mixinClassName.contains("DisplayEntityTimeStopMixin")) {
            // 注意：Mixin 应用发生在 Mod 加载早期，
            // ModList.get() 可能还没就绪，所以这里只能做「是否加载」判断；
            // 真正的版本号判断放在运行时（updateSkill / precache）里，
            // 由 EndingLibraryCompat.isAtLeastMinVersion() 完成。
            return LoadingModList.get().getModFileById(ENDING_LIBRARY_MODID) != null;
        }

        return true;
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