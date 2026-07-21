package net.v_black_cat.goetydelight.render.test;

import com.mojang.blaze3d.vertex.VertexFormat;
import net.minecraft.client.renderer.ShaderInstance;
import net.minecraft.server.packs.resources.ResourceProvider;

import java.io.IOException;

/**
 * 自定义着色器实例（桩代码，待完整迁移）
 */
public class ModShaderInstance extends ShaderInstance {
    private float time = 0;

    public ModShaderInstance(ResourceProvider resourceProvider, String name, VertexFormat vertexFormat) throws IOException {
        super(resourceProvider, name, vertexFormat);
    }

    public void setTime(float time) {
        this.time = time;
    }

    public float getTime() {
        return time;
    }
}
