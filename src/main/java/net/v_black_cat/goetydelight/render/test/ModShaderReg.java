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

        ModShaderInstance tiltedHalo = new ModShaderInstance(
                resourceProvider,
                new ResourceLocation(GoetyDelight.MODID, "tilted_halo").toString(),
                DefaultVertexFormat.POSITION_COLOR_TEX
        );
        event.registerShader(tiltedHalo, shaderInstance -> tiltedHaloShader = shaderInstance);

        ModShaderInstance backSigilEffect = new ModShaderInstance(
                resourceProvider,
                new ResourceLocation(GoetyDelight.MODID, "back_sigil_effect").toString(),
                DefaultVertexFormat.POSITION_COLOR_TEX
        );
        event.registerShader(backSigilEffect, shaderInstance -> backSigilEffectShader = shaderInstance);

        ModShaderInstance volumetricFlame = new ModShaderInstance(
                resourceProvider,
                new ResourceLocation(GoetyDelight.MODID, "volumetric_flame").toString(),
                DefaultVertexFormat.POSITION_COLOR_TEX
        );
        event.registerShader(volumetricFlame, shaderInstance -> volumetricFlameShader = shaderInstance);

        ModShaderInstance phantomRiftShards = new ModShaderInstance(
                resourceProvider,
                new ResourceLocation(GoetyDelight.MODID, "phantom_rift_shards").toString(),
                DefaultVertexFormat.POSITION_COLOR_TEX
        );
        event.registerShader(phantomRiftShards, shaderInstance -> phantomRiftShardsShader = shaderInstance);

        ModShaderInstance supremeChaosCosmos = new ModShaderInstance(
                resourceProvider,
                new ResourceLocation(GoetyDelight.MODID, "supreme_chaos_cosmos").toString(),
                DefaultVertexFormat.POSITION_COLOR_TEX
        );
        event.registerShader(supremeChaosCosmos, shaderInstance -> supremeChaosCosmosShader = shaderInstance);

    }
}
