package net.v_black_cat.goetydelight.visual;

import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.v_black_cat.goetydelight.GoetyDelight;

import java.util.Collection;

public final class GDVisualEffects {
    public static final ResourceKey<Registry<EntityVisualEffectType>> REGISTRY_KEY =
            ResourceKey.createRegistryKey(ResourceLocation.fromNamespaceAndPath(
                    GoetyDelight.MODID, "entity_visual_effect_types"));
    public static final ResourceKey<EntityVisualEffectType> ORBIT_SPHERE_KEY = key("orbit_sphere");
    public static final ResourceKey<EntityVisualEffectType> HELIX_TRAIL_KEY = key("helix_trail");
    public static final ResourceKey<EntityVisualEffectType> DEPTH_OCCLUDED_HALO_KEY = key("depth_occluded_halo");
    public static final ResourceKey<EntityVisualEffectType> CONTACT_EDGE_GLOW_KEY = key("contact_edge_glow");
    public static final ResourceKey<EntityVisualEffectType> SOFT_TRAIL_KEY = key("soft_trail");
    public static final ResourceKey<EntityVisualEffectType> SCREEN_SPACE_SHOCKWAVE_KEY = key("screen_space_shockwave");
    public static final ResourceKey<EntityVisualEffectType> DEPTH_REFRACTION_HEATWAVE_KEY = key("depth_refraction_heatwave");
    public static final ResourceKey<EntityVisualEffectType> VOLUMETRIC_LIGHT_COLUMN_KEY = key("volumetric_light_column");
    public static final ResourceKey<EntityVisualEffectType> OUTLINE_SCAN_KEY = key("outline_scan");
    public static final ResourceKey<EntityVisualEffectType> BLOCK_CRACK_LIGHT_KEY = key("block_crack_light");
    public static final ResourceKey<EntityVisualEffectType> RED_EYE_FLASH_KEY = key("red_eye_flash");
    public static final ResourceKey<EntityVisualEffectType> TILTED_HALO_KEY = key("tilted_halo");
    public static final ResourceKey<EntityVisualEffectType> DOOM_CORONA_KEY = key("doom_corona");
    public static final ResourceKey<EntityVisualEffectType> ABYSSAL_RIFT_EYE_KEY = key("abyssal_rift_eye");
    public static final ResourceKey<EntityVisualEffectType> HOLY_JUDGEMENT_HALO_KEY = key("holy_judgement_halo");
    public static final ResourceKey<EntityVisualEffectType> ASTRAL_CROWN_KEY = key("astral_crown");
    public static final ResourceKey<EntityVisualEffectType> BLOOD_MOON_BACKWHEEL_KEY = key("blood_moon_backwheel");
    public static final ResourceKey<EntityVisualEffectType> CAUSAL_CHAINS_KEY = key("causal_chains");
    public static final ResourceKey<EntityVisualEffectType> INVERTED_CROSS_MARK_KEY = key("inverted_cross_mark");
    public static final ResourceKey<EntityVisualEffectType> DEPTH_REFRACTION_PRESSURE_KEY = key("depth_refraction_pressure");
    public static final ResourceKey<EntityVisualEffectType> VOLUMETRIC_FLAME_KEY = key("volumetric_flame");
    public static final ResourceKey<EntityVisualEffectType> PHANTOM_RIFT_SHARDS_KEY = key("phantom_rift_shards");
    public static final ResourceKey<EntityVisualEffectType> SUPREME_CHAOS_COSMOS_KEY = key("supreme_chaos_cosmos");
    private static final DeferredRegister<EntityVisualEffectType> VISUAL_EFFECTS =
            DeferredRegister.create(REGISTRY_KEY, GoetyDelight.MODID);
    public static final Registry<EntityVisualEffectType> REGISTRY =
            VISUAL_EFFECTS.makeRegistry(builder -> {
            });
    public static final DeferredHolder<EntityVisualEffectType, EntityVisualEffectType> ORBIT_SPHERE = register(
            ORBIT_SPHERE_KEY,
            EntityVisualEffectType.properties()
                    .infiniteDuration()
                    .renderDistance(80.0D)
                    .persistent()
    );
    public static final DeferredHolder<EntityVisualEffectType, EntityVisualEffectType> HELIX_TRAIL = register(
            HELIX_TRAIL_KEY,
            EntityVisualEffectType.properties()
                    .infiniteDuration()
                    .renderDistance(48.0D)
                    .persistent()
    );
    public static final DeferredHolder<EntityVisualEffectType, EntityVisualEffectType> DEPTH_OCCLUDED_HALO = register(
            DEPTH_OCCLUDED_HALO_KEY,
            EntityVisualEffectType.properties()
                    .infiniteDuration()
                    .renderDistance(72.0D)
                    .persistent()
    );
    public static final DeferredHolder<EntityVisualEffectType, EntityVisualEffectType> CONTACT_EDGE_GLOW = register(
            CONTACT_EDGE_GLOW_KEY,
            EntityVisualEffectType.properties()
                    .infiniteDuration()
                    .renderDistance(48.0D).persistent()
    );
    public static final DeferredHolder<EntityVisualEffectType, EntityVisualEffectType> SOFT_TRAIL = register(
            SOFT_TRAIL_KEY,
            EntityVisualEffectType.properties()
                    .infiniteDuration()
                    .renderDistance(48.0D).persistent()
    );
    public static final DeferredHolder<EntityVisualEffectType, EntityVisualEffectType> SCREEN_SPACE_SHOCKWAVE = register(
            SCREEN_SPACE_SHOCKWAVE_KEY,
            EntityVisualEffectType.properties()
                    .defaultDuration(36)
                    .renderDistance(96.0D).persistent()
    );
    public static final DeferredHolder<EntityVisualEffectType, EntityVisualEffectType> DEPTH_REFRACTION_HEATWAVE = register(
            DEPTH_REFRACTION_HEATWAVE_KEY,
            EntityVisualEffectType.properties()
                    .infiniteDuration()
                    .renderDistance(48.0D).persistent()
    );
    public static final DeferredHolder<EntityVisualEffectType, EntityVisualEffectType> VOLUMETRIC_LIGHT_COLUMN = register(
            VOLUMETRIC_LIGHT_COLUMN_KEY,
            EntityVisualEffectType.properties()
                    .infiniteDuration()
                    .renderDistance(96.0D).persistent()
    );
    public static final DeferredHolder<EntityVisualEffectType, EntityVisualEffectType> OUTLINE_SCAN = register(
            OUTLINE_SCAN_KEY,
            EntityVisualEffectType.properties()
                    .infiniteDuration()
                    .renderDistance(64.0D).persistent()
    );
    public static final DeferredHolder<EntityVisualEffectType, EntityVisualEffectType> BLOCK_CRACK_LIGHT = register(
            BLOCK_CRACK_LIGHT_KEY,
            EntityVisualEffectType.properties()
                    .infiniteDuration()
                    .renderDistance(80.0D)
                    .renderInFirstPerson().persistent()
    );
    public static final DeferredHolder<EntityVisualEffectType, EntityVisualEffectType> RED_EYE_FLASH = register(
            RED_EYE_FLASH_KEY,
            EntityVisualEffectType.properties()
                    .infiniteDuration()
                    .renderDistance(80.0D).persistent()
    );
    public static final DeferredHolder<EntityVisualEffectType, EntityVisualEffectType> TILTED_HALO = register(
            TILTED_HALO_KEY,
            EntityVisualEffectType.properties()
                    .infiniteDuration()
                    .renderDistance(88.0D).persistent()
    );
    public static final DeferredHolder<EntityVisualEffectType, EntityVisualEffectType> DOOM_CORONA = register(
            DOOM_CORONA_KEY,
            EntityVisualEffectType.properties()
                    .infiniteDuration()
                    .renderDistance(96.0D).persistent()
    );
    public static final DeferredHolder<EntityVisualEffectType, EntityVisualEffectType> ABYSSAL_RIFT_EYE = register(
            ABYSSAL_RIFT_EYE_KEY,
            EntityVisualEffectType.properties()
                    .infiniteDuration()
                    .renderDistance(96.0D).persistent()
    );
    public static final DeferredHolder<EntityVisualEffectType, EntityVisualEffectType> HOLY_JUDGEMENT_HALO = register(
            HOLY_JUDGEMENT_HALO_KEY,
            EntityVisualEffectType.properties()
                    .infiniteDuration()
                    .renderDistance(96.0D).persistent()
    );
    public static final DeferredHolder<EntityVisualEffectType, EntityVisualEffectType> ASTRAL_CROWN = register(
            ASTRAL_CROWN_KEY,
            EntityVisualEffectType.properties()
                    .infiniteDuration()
                    .renderDistance(96.0D).persistent()
    );
    public static final DeferredHolder<EntityVisualEffectType, EntityVisualEffectType> BLOOD_MOON_BACKWHEEL = register(
            BLOOD_MOON_BACKWHEEL_KEY,
            EntityVisualEffectType.properties()
                    .infiniteDuration()
                    .renderDistance(96.0D).persistent()
    );
    public static final DeferredHolder<EntityVisualEffectType, EntityVisualEffectType> CAUSAL_CHAINS = register(
            CAUSAL_CHAINS_KEY,
            EntityVisualEffectType.properties()
                    .infiniteDuration()
                    .renderDistance(96.0D).persistent()
    );
    public static final DeferredHolder<EntityVisualEffectType, EntityVisualEffectType> INVERTED_CROSS_MARK = register(
            INVERTED_CROSS_MARK_KEY,
            EntityVisualEffectType.properties()
                    .infiniteDuration()
                    .renderDistance(96.0D).persistent()
    );
    public static final DeferredHolder<EntityVisualEffectType, EntityVisualEffectType> DEPTH_REFRACTION_PRESSURE = register(
            DEPTH_REFRACTION_PRESSURE_KEY,
            EntityVisualEffectType.properties()
                    .infiniteDuration()
                    .renderDistance(72.0D)
                    .renderInFirstPerson().persistent()
    );
    public static final DeferredHolder<EntityVisualEffectType, EntityVisualEffectType> VOLUMETRIC_FLAME = register(
            VOLUMETRIC_FLAME_KEY,
            EntityVisualEffectType.properties()
                    .infiniteDuration()
                    .renderDistance(80.0D).persistent()
    );
    public static final DeferredHolder<EntityVisualEffectType, EntityVisualEffectType> PHANTOM_RIFT_SHARDS = register(
            PHANTOM_RIFT_SHARDS_KEY,
            EntityVisualEffectType.properties()
                    .infiniteDuration()
                    .renderDistance(88.0D).persistent()
    );
    public static final DeferredHolder<EntityVisualEffectType, EntityVisualEffectType> SUPREME_CHAOS_COSMOS = register(
            SUPREME_CHAOS_COSMOS_KEY,
            EntityVisualEffectType.properties()
                    .infiniteDuration()
                    .renderDistance(96.0D).persistent()
    );

    private GDVisualEffects() {
    }

    public static void register(IEventBus eventBus) {
        VISUAL_EFFECTS.register(eventBus);
    }

    public static DeferredHolder<EntityVisualEffectType, EntityVisualEffectType> register(String path) {
        return register(path, EntityVisualEffectType.properties());
    }

    public static DeferredHolder<EntityVisualEffectType, EntityVisualEffectType> register(String path, EntityVisualEffectType.Properties properties) {
        return VISUAL_EFFECTS.register(path, () -> new EntityVisualEffectType(properties));
    }

    public static DeferredHolder<EntityVisualEffectType, EntityVisualEffectType> register(ResourceKey<EntityVisualEffectType> key) {
        return register(key, EntityVisualEffectType.properties());
    }

    public static DeferredHolder<EntityVisualEffectType, EntityVisualEffectType> register(ResourceKey<EntityVisualEffectType> key, EntityVisualEffectType.Properties properties) {
        return register(key.location().getPath(), properties);
    }

    public static EntityVisualEffectType get(ResourceLocation id) {
        return REGISTRY.get(id);
    }

    public static Collection<ResourceLocation> registeredIds() {
        return REGISTRY.keySet().stream()
                .sorted()
                .toList();
    }

    public static boolean isRegistered(ResourceLocation id) {
        return REGISTRY.containsKey(id);
    }

    public static ResourceLocation id(String path) {
        return ResourceLocation.fromNamespaceAndPath(GoetyDelight.MODID, path);
    }

    public static ResourceKey<EntityVisualEffectType> key(String path) {
        return ResourceKey.create(REGISTRY_KEY, id(path));
    }
}
