package net.v_black_cat.goetydelight.util;

import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;

import static net.v_black_cat.goetydelight.GoetyDelight.MODID;

public class TextUtils {

    public static MutableComponent getTranslation(String key, Object... args) {
        return Component.translatable(MODID+"." + key, args);
    }
}
