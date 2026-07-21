package net.v_black_cat.goetydelight.render.test;

import net.minecraft.client.renderer.ShaderInstance;

/**
 * 着色器注册表（桩代码，待完整迁移）
 */
public class ModShaderReg {
    private static ShaderInstance floridShader;

    public static ShaderInstance getFloridShader() {
        return floridShader;
    }
}
