package net.v_black_cat.goetydelight.init;

import java.util.ArrayList;
import java.util.List;

/**
 * 服务端自定义人偶模型加载器（桩代码，待完整迁移）
 */
public class ServerCustomDollLoader {
    private static final List<String> MODELS = new ArrayList<>();

    public static List<String> getModels() {
        return MODELS;
    }
}
