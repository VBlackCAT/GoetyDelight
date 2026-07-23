package net.v_black_cat.goetydelight.render.test;

import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import net.minecraft.client.renderer.ShaderInstance;
import net.minecraft.server.packs.resources.ResourceProvider;
import net.neoforged.neoforge.client.event.RegisterShadersEvent;
import net.v_black_cat.goetydelight.GoetyDelight;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.util.Objects;

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

    @Nullable
    private static ShaderInstance tiltedHaloShader;

    @Nullable
    private static ShaderInstance backSigilEffectShader;

    @Nullable
    private static ShaderInstance volumetricFlameShader;

    @Nullable
    private static ShaderInstance phantomRiftShardsShader;

    @Nullable
    private static ShaderInstance supremeChaosCosmosShader;

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

    public static ShaderInstance getTiltedHaloShader() {
        return Objects.requireNonNull(tiltedHaloShader, "Tilted halo shader not registered");
    }

    public static ShaderInstance getBackSigilEffectShader() {
        return Objects.requireNonNull(backSigilEffectShader, "Back sigil effect shader not registered");
    }

    public static ShaderInstance getVolumetricFlameShader() {
        return Objects.requireNonNull(volumetricFlameShader, "Volumetric flame shader not registered");
    }

    public static ShaderInstance getPhantomRiftShardsShader() {
        return Objects.requireNonNull(phantomRiftShardsShader, "Phantom rift shards shader not registered");
    }

    public static ShaderInstance getSupremeChaosCosmosShader() {
        return Objects.requireNonNull(supremeChaosCosmosShader, "Supreme chaos cosmos shader not registered");
    }

    public static void registerShaders(RegisterShadersEvent event) throws IOException {
        ResourceProvider resourceProvider = event.getResourceProvider();


        ModShaderInstance colorful = new ModShaderInstance(
                resourceProvider,
                GoetyDelight.MODID + ":colorful_shader",
                DefaultVertexFormat.POSITION_TEX_COLOR
        );
        event.registerShader(colorful, shaderInstance -> colorfulShader = shaderInstance);


        ModShaderInstance florid = new ModShaderInstance(
                resourceProvider,
                GoetyDelight.MODID + ":florid_shader",
                DefaultVertexFormat.POSITION_TEX
        );
        event.registerShader(florid, shaderInstance -> floridShader = shaderInstance);

        ModShaderInstance orbitSphere = new ModShaderInstance(
                resourceProvider,
                GoetyDelight.MODID + ":orbit_sphere",
                DefaultVertexFormat.POSITION_COLOR_NORMAL
        );
        event.registerShader(orbitSphere, shaderInstance -> orbitSphereShader = shaderInstance);

        ModShaderInstance playerHelixTrail = new ModShaderInstance(
                resourceProvider,
                GoetyDelight.MODID + ":player_helix_trail",
                DefaultVertexFormat.POSITION_TEX_COLOR
        );
        event.registerShader(playerHelixTrail, shaderInstance -> playerHelixTrailShader = shaderInstance);

        ModShaderInstance entityDepthEffect = new ModShaderInstance(
                resourceProvider,
                GoetyDelight.MODID + ":entity_depth_effect",
                DefaultVertexFormat.POSITION_TEX_COLOR
        );
        event.registerShader(entityDepthEffect, shaderInstance -> entityDepthEffectShader = shaderInstance);

        ModShaderInstance blockCrackLight = new ModShaderInstance(
                resourceProvider,
                GoetyDelight.MODID + ":block_crack_light",
                DefaultVertexFormat.POSITION_TEX_COLOR
        );
        event.registerShader(blockCrackLight, shaderInstance -> blockCrackLightShader = shaderInstance);

        ModShaderInstance redEyeFlash = new ModShaderInstance(
                resourceProvider,
                GoetyDelight.MODID + ":red_eye_flash",
                DefaultVertexFormat.POSITION_TEX_COLOR
        );
        event.registerShader(redEyeFlash, shaderInstance -> redEyeFlashShader = shaderInstance);

        ModShaderInstance tiltedHalo = new ModShaderInstance(
                resourceProvider,
                GoetyDelight.MODID + ":tilted_halo",
                DefaultVertexFormat.POSITION_TEX_COLOR
        );
        event.registerShader(tiltedHalo, shaderInstance -> tiltedHaloShader = shaderInstance);

        ModShaderInstance backSigilEffect = new ModShaderInstance(
                resourceProvider,
                GoetyDelight.MODID + ":back_sigil_effect",
                DefaultVertexFormat.POSITION_TEX_COLOR
        );
        event.registerShader(backSigilEffect, shaderInstance -> backSigilEffectShader = shaderInstance);

        ModShaderInstance volumetricFlame = new ModShaderInstance(
                resourceProvider,
                GoetyDelight.MODID + ":volumetric_flame",
                DefaultVertexFormat.POSITION_TEX_COLOR
        );
        event.registerShader(volumetricFlame, shaderInstance -> volumetricFlameShader = shaderInstance);

        ModShaderInstance phantomRiftShards = new ModShaderInstance(
                resourceProvider,
                GoetyDelight.MODID + ":phantom_rift_shards",
                DefaultVertexFormat.POSITION_TEX_COLOR
        );
        event.registerShader(phantomRiftShards, shaderInstance -> phantomRiftShardsShader = shaderInstance);

        ModShaderInstance supremeChaosCosmos = new ModShaderInstance(
                resourceProvider,
                GoetyDelight.MODID + ":supreme_chaos_cosmos",
                DefaultVertexFormat.POSITION_TEX_COLOR
        );
        event.registerShader(supremeChaosCosmos, shaderInstance -> supremeChaosCosmosShader = shaderInstance);

    }
}
