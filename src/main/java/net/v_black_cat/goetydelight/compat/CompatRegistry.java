package net.v_black_cat.goetydelight.compat;

import net.minecraftforge.eventbus.api.IEventBus;
import net.v_black_cat.goetydelight.compat.enigmaticdelicacy.EnigmaticDelicacyCompat;
import net.v_black_cat.goetydelight.compat.goetycataclysm.GoetyCataclysmCompat;
import net.v_black_cat.goetydelight.compat.goetyrevelation.GoetyRevelationCompat;

/**
 * 联动模块统一挂载入口（对齐 1.21.1 优化版）。
 * 各模块自己带模组存在性判断，GoetyDelight 只调这两个方法。
 */
public final class CompatRegistry {

    private CompatRegistry() {
    }

    /** 挂载所有联动模块（模组构造期） */
    public static void register(IEventBus modBus) {
        CompatManager.logStatus();

        if (CompatManager.isEnigmaticDelicacyLoaded()) {
            EnigmaticDelicacyCompat.register(modBus);
        }
        if (CompatManager.isGoetyCataclysmLoaded()) {
            GoetyCataclysmCompat.register(modBus);
        }
        if (CompatManager.isGoetyRevelationLoaded()) {
            GoetyRevelationCompat.register(modBus);
        }
    }

    /** 注册表冻结后（FMLCommonSetupEvent 的 enqueueWork 里）调用，用于需要注册表内容的联动初始化 */
    public static void commonSetup() {
        if (CompatManager.isEnigmaticDelicacyLoaded()) {
            EnigmaticDelicacyCompat.commonSetup();
        }
        if (CompatManager.isGoetyCataclysmLoaded()) {
            GoetyCataclysmCompat.commonSetup();
        }
        if (CompatManager.isGoetyRevelationLoaded()) {
            GoetyRevelationCompat.commonSetup();
        }
    }
}
