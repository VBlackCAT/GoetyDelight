package net.v_black_cat.goetydelight.compat.farmersdelight;

import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.item.ItemStack;

import java.lang.reflect.Method;

/**
 * Farmer's Delight 提示文本的反射访问。
 * <p>
 * 1.2.x 的 {@code TextUtils.getTranslation(String, Object...)} 与基类
 * {@code FeastBlock} 使用的 key {@code "block.feast.use_container"} 一致，
 * 子类按同样方式调用即可与 1.2.x 基类行为保持同步。
 * <p>
 * 若 FD 缺失或方法探测失败，回退到本 mod 自己的 translation key，保证不崩。
 */
public final class FarmersDelightTextUtilsLegacy {

    private static final String TEXT_UTILS_CLASS = "vectorwing.farmersdelight.common.utility.TextUtils";
    private static final String METHOD_NAME = "getTranslation";
    private static final String USE_CONTAINER_KEY = "block.feast.use_container";

    private static Method getTranslationMethod;
    private static boolean resolved = false;

    private FarmersDelightTextUtilsLegacy() {
    }

    /** "feast.use_container" 提示：需要对应的容器（如碗）。 */
    public static MutableComponent feastUseContainer(ItemStack container) {
        Component name = container.getHoverName();
        resolve();

        if (getTranslationMethod != null) {
            try {
                Object result = getTranslationMethod.invoke(null, USE_CONTAINER_KEY, new Object[]{name});
                if (result instanceof MutableComponent) {
                    return (MutableComponent) result;
                }
            } catch (ReflectiveOperationException e) {
                // 落到下面的回退
            }
        }

        // 回退：本 mod 自己的 key，语义一致，不依赖 FD
        return Component.translatable("block.goetydelight.feast.use_container", name);
    }

    private static void resolve() {
        if (resolved) {
            return;
        }
        resolved = true;

        try {
            Class<?> clazz = Class.forName(TEXT_UTILS_CLASS);
            // getTranslation(String, Object...)
            getTranslationMethod = clazz.getMethod(METHOD_NAME, String.class, Object[].class);
        } catch (ReflectiveOperationException e) {
            getTranslationMethod = null;
        }
    }
}