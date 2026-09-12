package net.v_black_cat.goetydelight.compat;

import net.neoforged.bus.api.IEventBus;
import net.v_black_cat.goetydelight.compat.enigmaticdelicacy.EnigmaticDelicacyCompat;
import net.v_black_cat.goetydelight.compat.goetycataclysm.GoetyCataclysmCompat;
import net.v_black_cat.goetydelight.compat.goetyrevelation.GoetyRevelationCompat;

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

    /**
     * 两个注册表都冻结后（FMLCommonSetupEvent）调用，用于需要注册表内容的联动初始化。
     * 各模块的 commonSetup 钩子在这里统一挂上。
     */
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
