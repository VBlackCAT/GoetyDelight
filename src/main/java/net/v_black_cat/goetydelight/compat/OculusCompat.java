package net.v_black_cat.goetydelight.compat;

import javax.annotation.Nullable;
import java.lang.reflect.Method;

/**
 * Oculus / Iris 光影联动。模组存在性判断统一走 {@link CompatManager}。
 */
public final class OculusCompat {
    @Nullable
    private static Method getInstanceMethod;
    @Nullable
    private static Method isShaderPackInUseMethod;
    private static boolean triedResolveApi;

    private OculusCompat() {
    }

    public static boolean isShaderPackInUse() {
        if (!CompatManager.isShaderModLoaded()) {
            return false;
        }

        try {
            if (!resolveApi()) {
                return false;
            }

            Object irisApi = getInstanceMethod.invoke(null);
            return Boolean.TRUE.equals(isShaderPackInUseMethod.invoke(irisApi));
        } catch (ReflectiveOperationException | LinkageError exception) {
            return false;
        }
    }

    private static boolean resolveApi() throws ReflectiveOperationException {
        if (!triedResolveApi) {
            triedResolveApi = true;
            Class<?> irisApiClass = Class.forName("net.irisshaders.iris.api.v0.IrisApi");
            getInstanceMethod = irisApiClass.getMethod("getInstance");
            isShaderPackInUseMethod = irisApiClass.getMethod("isShaderPackInUse");
        }

        return getInstanceMethod != null && isShaderPackInUseMethod != null;
    }
}
