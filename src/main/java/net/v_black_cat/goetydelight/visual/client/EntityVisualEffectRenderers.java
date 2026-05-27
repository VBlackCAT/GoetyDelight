package net.v_black_cat.goetydelight.visual.client;

import net.minecraft.resources.ResourceLocation;
import net.v_black_cat.goetydelight.render.DepthVisualEffectRenderer;
import net.v_black_cat.goetydelight.render.OrbitingSphereRenderer;
import net.v_black_cat.goetydelight.render.PlayerHelixTrailRenderer;
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
    }

    public static void register(ResourceLocation id, EntityVisualEffectRenderer renderer) {
        RENDERERS.put(id, renderer);
    }

    public static EntityVisualEffectRenderer get(ResourceLocation id) {
        return RENDERERS.get(id);
    }
}
