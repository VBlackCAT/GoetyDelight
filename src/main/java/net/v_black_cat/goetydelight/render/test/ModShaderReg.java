package net.v_black_cat.goetydelight.render.test;

import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import net.minecraft.client.renderer.ShaderInstance;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceProvider;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RegisterShadersEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.v_black_cat.goetydelight.GoetyDelight;

import javax.annotation.Nullable;
import java.io.IOException;
import java.util.Objects;

@Mod.EventBusSubscriber(value = Dist.CLIENT, modid = GoetyDelight.MODID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class ModShaderReg {

    @Nullable
    private static ShaderInstance colorfulShader;

    @Nullable
    private static ShaderInstance floridShader;

    public static ShaderInstance getColorfulShader() {
        return Objects.requireNonNull(colorfulShader, "Colorful shader not registered");
    }

    public static ShaderInstance getFloridShader() {
        return Objects.requireNonNull(floridShader, "Florid shader not registered");
    }

    @SubscribeEvent
    public static void registerShaders(RegisterShadersEvent event) throws IOException {
        ResourceProvider resourceProvider = event.getResourceProvider();


        ModShaderInstance colorful = new ModShaderInstance(
                resourceProvider,
                new ResourceLocation(GoetyDelight.MODID, "colorful_shader").toString(),
                DefaultVertexFormat.POSITION_COLOR_TEX
        );
        event.registerShader(colorful, shaderInstance -> colorfulShader = shaderInstance);


        ModShaderInstance florid = new ModShaderInstance(
                resourceProvider,
                new ResourceLocation(GoetyDelight.MODID, "florid_shader").toString(),
                DefaultVertexFormat.POSITION_TEX
        );
        event.registerShader(florid, shaderInstance -> floridShader = shaderInstance);
    }
}