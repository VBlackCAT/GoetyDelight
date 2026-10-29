package net.v_black_cat.goetydelight.compat.farmersdelight;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

/**
 * Farmer's Delight 配置项的反射访问。
 * <p>
 * 1.3.0+ 在 {@code Configuration} 中新增了 {@code ENABLE_COOKING_POT_RECIPE_BOOK} 字段。
 * 1.2.x 中没有该字段，且默认支持配方书，因此回退返回 true。
 */
public final class FarmersDelightConfigLegacy {

    private static final String CONFIGURATION_CLASS = "vectorwing.farmersdelight.common.Configuration";
    private static final String COOKING_POT_RECIPE_BOOK_FIELD = "ENABLE_COOKING_POT_RECIPE_BOOK";

    private static boolean resolved = false;
    private static Method configGetMethod;
    private static Object configValueObj;

    private FarmersDelightConfigLegacy() {
    }

    /**
     * 检查是否允许使用烹饪锅配方书。
     * <p>
     * 1.3.0+ 返回真实的配置值；
     * 1.2.x 默认返回 true（1.2.x 中配方书是硬编码允许的，或者字段叫旧名字）。
     */
    public static boolean isCookingPotRecipeBookEnabled() {
        resolve();

        if (configValueObj != null && configGetMethod != null) {
            try {
                // ForgeConfigSpec.BooleanValue.get()
                Object result = configGetMethod.invoke(configValueObj);
                if (result instanceof Boolean) {
                    return (Boolean) result;
                }
            } catch (ReflectiveOperationException e) {
                // 兜底走下面的 true
            }
        }

        // 1.2.x 环境，或反射失败时默认开启配方书
        return true;
    }

    private static void resolve() {
        if (resolved) {
            return;
        }
        resolved = true;

        try {
            Class<?> clazz = Class.forName(CONFIGURATION_CLASS);
            // 1.3.0+ 的字段
            Field field = clazz.getField(COOKING_POT_RECIPE_BOOK_FIELD);
            configValueObj = field.get(null);
            if (configValueObj != null) {
                // 获取 .get() 方法（ForgeConfigSpec.ConfigValue#get 或 BooleanValue#get）
                configGetMethod = configValueObj.getClass().getMethod("get");
            }
        } catch (ReflectiveOperationException | ClassCastException e) {
            // 1.2.x 环境，没有该字段，安全降级
            configValueObj = null;
            configGetMethod = null;
        }
    }
}