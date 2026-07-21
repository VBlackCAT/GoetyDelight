package net.v_black_cat.goetydelight.init;

import net.minecraft.resources.ResourceLocation;

/**
 * 客户端自定义人偶模型加载器（桩代码，待完整迁移）
 */
public class CustomDollLoader {
    public static void init() {
    }

    public static String getLanguage(String locale, String modelId) {
        return modelId;
    }

    public static ResourceLocation getTexture(String modelId) {
        return ResourceLocation.withDefaultNamespace("textures/entity/doll/" + modelId + ".png");
    }
}
