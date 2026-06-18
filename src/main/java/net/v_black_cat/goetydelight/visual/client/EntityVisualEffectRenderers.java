package net.v_black_cat.goetydelight.visual.client;

import net.minecraft.resources.ResourceLocation;
import net.v_black_cat.goetydelight.render.BackSigilEffectRenderer;
import net.v_black_cat.goetydelight.render.BlockCrackLightRenderer;
import net.v_black_cat.goetydelight.render.DepthVisualEffectRenderer;
import net.v_black_cat.goetydelight.render.OrbitingSphereRenderer;
import net.v_black_cat.goetydelight.render.PhantomRiftShardsRenderer;
import net.v_black_cat.goetydelight.render.PlayerHelixTrailRenderer;
import net.v_black_cat.goetydelight.render.RedEyeFlashRenderer;
import net.v_black_cat.goetydelight.render.SupremeChaosCosmosRenderer;
import net.v_black_cat.goetydelight.render.TiltedHaloRenderer;
import net.v_black_cat.goetydelight.render.VolumetricFlameRenderer;
import net.v_black_cat.goetydelight.visual.GDVisualEffects;

import java.util.HashMap;
import java.util.Map;

public final class EntityVisualEffectRenderers {
    private static final Map<ResourceLocation, EntityVisualEffectRenderer> RENDERERS = new HashMap<>();

    private EntityVisualEffectRenderers() {
    }

    public static void registerDefaults() {
        register(GDVisualEffects.ORBIT_SPHERE.getId(), OrbitingSphereRenderer::render);
        register(GDVisualEffects.HELIX_TRAIL.getId(), PlayerHelixTrailRenderer::render);
        register(GDVisualEffects.SOFT_TRAIL.getId(), DepthVisualEffectRenderer::renderSoftTrail);
        register(GDVisualEffects.BLOCK_CRACK_LIGHT.getId(), BlockCrackLightRenderer::render);
        register(GDVisualEffects.RED_EYE_FLASH.getId(), RedEyeFlashRenderer::render);
        register(GDVisualEffects.TILTED_HALO.getId(), TiltedHaloRenderer::render);
        register(GDVisualEffects.DOOM_CORONA.getId(), BackSigilEffectRenderer::renderDoomCorona);
        register(GDVisualEffects.ABYSSAL_RIFT_EYE.getId(), BackSigilEffectRenderer::renderAbyssalRiftEye);
        register(GDVisualEffects.HOLY_JUDGEMENT_HALO.getId(), BackSigilEffectRenderer::renderHolyJudgementHalo);
        register(GDVisualEffects.ASTRAL_CROWN.getId(), BackSigilEffectRenderer::renderAstralCrown);
        register(GDVisualEffects.BLOOD_MOON_BACKWHEEL.getId(), BackSigilEffectRenderer::renderBloodMoonBackwheel);
        register(GDVisualEffects.CAUSAL_CHAINS.getId(), BackSigilEffectRenderer::renderCausalChains);
        register(GDVisualEffects.INVERTED_CROSS_MARK.getId(), BackSigilEffectRenderer::renderInvertedCrossMark);
        register(GDVisualEffects.VOLUMETRIC_FLAME.getId(), VolumetricFlameRenderer::render);
        register(GDVisualEffects.PHANTOM_RIFT_SHARDS.getId(), PhantomRiftShardsRenderer::render);
        register(GDVisualEffects.SUPREME_CHAOS_COSMOS.getId(), SupremeChaosCosmosRenderer::render);
    }

    public static void register(ResourceLocation id, EntityVisualEffectRenderer renderer) {
        RENDERERS.put(id, renderer);
    }

    public static EntityVisualEffectRenderer get(ResourceLocation id) {
        return RENDERERS.get(id);
    }
}
