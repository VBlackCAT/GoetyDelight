package net.v_black_cat.goetydelight.compat.carryon;

import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.server.ServerAboutToStartEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.v_black_cat.goetydelight.GoetyDelight;
import net.v_black_cat.goetydelight.compat.CompatManager;

import java.lang.reflect.Field;
import java.util.Set;

/**
 * Carry On 联动模块入口。
 */
public final class CarryOnCompat {

    public static final String ID = "carryon";

    /** 模组是否存在 */
    public static final boolean IS_LOADED = CompatManager.isCarryOnLoaded();

    private CarryOnCompat() {
    }

    /** 模组构造期：注册本模块内容（如有） */
    public static void register(IEventBus modBus) {
        GoetyDelight.LOGGER.info("[compat] carryon 联动已挂载");
    }

    public static void commonSetup() {
    }


}