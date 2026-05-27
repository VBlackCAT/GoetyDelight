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

    @Nullable
    private static ShaderInstance orbitSphereShader;

    @Nullable
    private static ShaderInstance playerHelixTrailShader;

    @Nullable
    private static ShaderInstance entityDepthEffectShader;

    @Nullable
    private static ShaderInstance blockCrackLightShader;

    @Nullable
    private static ShaderInstance redEyeFlashShader;

    public static ShaderInstance getColorfulShader() {
        return Objects.requireNonNull(colorfulShader, "Colorful shader not registered");
    }

    public static ShaderInstance getFloridShader() {
        return Objects.requireNonNull(floridShader, "Florid shader not registered");
    }

    public static ShaderInstance getOrbitSphereShader() {
        return Objects.requireNonNull(orbitSphereShader, "Orbit sphere shader not registered");
    }

    public static ShaderInstance getPlayerHelixTrailShader() {
        return Objects.requireNonNull(playerHelixTrailShader, "Player helix trail shader not registered");
    }

    public static ShaderInstance getEntityDepthEffectShader() {
        return Objects.requireNonNull(entityDepthEffectShader, "Entity depth effect shader not registered");
    }

    public static ShaderInstance getBlockCrackLightShader() {
        return Objects.requireNonNull(blockCrackLightShader, "Block crack light shader not registered");
    }

    public static ShaderInstance getRedEyeFlashShader() {
        return Objects.requireNonNull(redEyeFlashShader, "Red eye flash shader not registered");
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

        ModShaderInstance orbitSphere = new ModShaderInstance(
                resourceProvider,
                new ResourceLocation(GoetyDelight.MODID, "orbit_sphere").toString(),
                DefaultVertexFormat.POSITION_COLOR_NORMAL
        );
        event.registerShader(orbitSphere, shaderInstance -> orbitSphereShader = shaderInstance);

        ModShaderInstance playerHelixTrail = new ModShaderInstance(
                resourceProvider,
                new ResourceLocation(GoetyDelight.MODID, "player_helix_trail").toString(),
                DefaultVertexFormat.POSITION_COLOR_TEX
        );
        event.registerShader(playerHelixTrail, shaderInstance -> playerHelixTrailShader = shaderInstance);

        ModShaderInstance entityDepthEffect = new ModShaderInstance(
                resourceProvider,
                new ResourceLocation(GoetyDelight.MODID, "entity_depth_effect").toString(),
                DefaultVertexFormat.POSITION_COLOR_TEX
        );
        event.registerShader(entityDepthEffect, shaderInstance -> entityDepthEffectShader = shaderInstance);

        ModShaderInstance blockCrackLight = new ModShaderInstance(
                resourceProvider,
                new ResourceLocation(GoetyDelight.MODID, "block_crack_light").toString(),
                DefaultVertexFormat.POSITION_COLOR_TEX
        );
        event.registerShader(blockCrackLight, shaderInstance -> blockCrackLightShader = shaderInstance);

        ModShaderInstance redEyeFlash = new ModShaderInstance(
                resourceProvider,
                new ResourceLocation(GoetyDelight.MODID, "red_eye_flash").toString(),
                DefaultVertexFormat.POSITION_COLOR_TEX
        );
        event.registerShader(redEyeFlash, shaderInstance -> redEyeFlashShader = shaderInstance);
    }
}
