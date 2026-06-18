package net.v_black_cat.goetydelight.visual;

import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.IForgeRegistry;
import net.minecraftforge.registries.RegistryBuilder;
import net.minecraftforge.registries.RegistryObject;
import net.v_black_cat.goetydelight.GoetyDelight;
import net.v_black_cat.goetydelight.register.ModRegistries;

import java.util.Collection;
import java.util.function.Supplier;

public final class GDVisualEffects {
    private static final DeferredRegister<EntityVisualEffectType> VISUAL_EFFECTS =
            DeferredRegister.create(ModRegistries.ENTITY_VISUAL_EFFECT_TYPE_KEY, GoetyDelight.MODID);

    public static final Supplier<IForgeRegistry<EntityVisualEffectType>> REGISTRY =
            VISUAL_EFFECTS.makeRegistry(() -> new RegistryBuilder<EntityVisualEffectType>()
                    .disableSaving()
                    .disableSync()
                    .disableOverrides());

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

    public static final RegistryObject<EntityVisualEffectType> ORBIT_SPHERE = register(
            ORBIT_SPHERE_KEY,
            EntityVisualEffectType.properties()
                    .infiniteDuration()
                    .renderDistance(80.0D)
    );
    public static final RegistryObject<EntityVisualEffectType> HELIX_TRAIL = register(
            HELIX_TRAIL_KEY,
            EntityVisualEffectType.properties()
                    .infiniteDuration()
                    .renderDistance(48.0D)
    );
    public static final RegistryObject<EntityVisualEffectType> DEPTH_OCCLUDED_HALO = register(
            DEPTH_OCCLUDED_HALO_KEY,
            EntityVisualEffectType.properties()
                    .infiniteDuration()
                    .renderDistance(72.0D)
    );
    public static final RegistryObject<EntityVisualEffectType> CONTACT_EDGE_GLOW = register(
            CONTACT_EDGE_GLOW_KEY,
            EntityVisualEffectType.properties()
                    .infiniteDuration()
                    .renderDistance(48.0D)
    );
    public static final RegistryObject<EntityVisualEffectType> SOFT_TRAIL = register(
            SOFT_TRAIL_KEY,
            EntityVisualEffectType.properties()
                    .infiniteDuration()
                    .renderDistance(48.0D)
    );
    public static final RegistryObject<EntityVisualEffectType> SCREEN_SPACE_SHOCKWAVE = register(
            SCREEN_SPACE_SHOCKWAVE_KEY,
            EntityVisualEffectType.properties()
                    .defaultDuration(36)
                    .renderDistance(96.0D)
    );
    public static final RegistryObject<EntityVisualEffectType> DEPTH_REFRACTION_HEATWAVE = register(
            DEPTH_REFRACTION_HEATWAVE_KEY,
            EntityVisualEffectType.properties()
                    .infiniteDuration()
                    .renderDistance(48.0D)
    );
    public static final RegistryObject<EntityVisualEffectType> VOLUMETRIC_LIGHT_COLUMN = register(
            VOLUMETRIC_LIGHT_COLUMN_KEY,
            EntityVisualEffectType.properties()
                    .infiniteDuration()
                    .renderDistance(96.0D)
    );
    public static final RegistryObject<EntityVisualEffectType> OUTLINE_SCAN = register(
            OUTLINE_SCAN_KEY,
            EntityVisualEffectType.properties()
                    .infiniteDuration()
                    .renderDistance(64.0D)
    );
    public static final RegistryObject<EntityVisualEffectType> BLOCK_CRACK_LIGHT = register(
            BLOCK_CRACK_LIGHT_KEY,
            EntityVisualEffectType.properties()
                    .infiniteDuration()
                    .renderDistance(80.0D)
                    .renderInFirstPerson()
    );
    public static final RegistryObject<EntityVisualEffectType> RED_EYE_FLASH = register(
            RED_EYE_FLASH_KEY,
            EntityVisualEffectType.properties()
                    .infiniteDuration()
                    .renderDistance(80.0D)
    );
    public static final RegistryObject<EntityVisualEffectType> TILTED_HALO = register(
            TILTED_HALO_KEY,
            EntityVisualEffectType.properties()
                    .infiniteDuration()
                    .renderDistance(88.0D)
    );
    public static final RegistryObject<EntityVisualEffectType> DOOM_CORONA = register(
            DOOM_CORONA_KEY,
            EntityVisualEffectType.properties()
                    .infiniteDuration()
                    .renderDistance(96.0D)
    );
    public static final RegistryObject<EntityVisualEffectType> ABYSSAL_RIFT_EYE = register(
            ABYSSAL_RIFT_EYE_KEY,
            EntityVisualEffectType.properties()
                    .infiniteDuration()
                    .renderDistance(96.0D)
    );
    public static final RegistryObject<EntityVisualEffectType> HOLY_JUDGEMENT_HALO = register(
            HOLY_JUDGEMENT_HALO_KEY,
            EntityVisualEffectType.properties()
                    .infiniteDuration()
                    .renderDistance(96.0D)
    );
    public static final RegistryObject<EntityVisualEffectType> ASTRAL_CROWN = register(
            ASTRAL_CROWN_KEY,
            EntityVisualEffectType.properties()
                    .infiniteDuration()
                    .renderDistance(96.0D)
    );
    public static final RegistryObject<EntityVisualEffectType> BLOOD_MOON_BACKWHEEL = register(
            BLOOD_MOON_BACKWHEEL_KEY,
            EntityVisualEffectType.properties()
                    .infiniteDuration()
                    .renderDistance(96.0D)
    );
    public static final RegistryObject<EntityVisualEffectType> CAUSAL_CHAINS = register(
            CAUSAL_CHAINS_KEY,
            EntityVisualEffectType.properties()
                    .infiniteDuration()
                    .renderDistance(96.0D)
    );
    public static final RegistryObject<EntityVisualEffectType> INVERTED_CROSS_MARK = register(
            INVERTED_CROSS_MARK_KEY,
            EntityVisualEffectType.properties()
                    .infiniteDuration()
                    .renderDistance(96.0D)
    );
    public static final RegistryObject<EntityVisualEffectType> DEPTH_REFRACTION_PRESSURE = register(
            DEPTH_REFRACTION_PRESSURE_KEY,
            EntityVisualEffectType.properties()
                    .infiniteDuration()
                    .renderDistance(72.0D)
                    .renderInFirstPerson()
    );
    public static final RegistryObject<EntityVisualEffectType> VOLUMETRIC_FLAME = register(
            VOLUMETRIC_FLAME_KEY,
            EntityVisualEffectType.properties()
                    .infiniteDuration()
                    .renderDistance(80.0D)
    );
    public static final RegistryObject<EntityVisualEffectType> PHANTOM_RIFT_SHARDS = register(
            PHANTOM_RIFT_SHARDS_KEY,
            EntityVisualEffectType.properties()
                    .infiniteDuration()
                    .renderDistance(88.0D)
    );
    public static final RegistryObject<EntityVisualEffectType> SUPREME_CHAOS_COSMOS = register(
            SUPREME_CHAOS_COSMOS_KEY,
            EntityVisualEffectType.properties()
                    .infiniteDuration()
                    .renderDistance(96.0D)
    );

    private GDVisualEffects() {
    }

    public static void register(IEventBus eventBus) {
        VISUAL_EFFECTS.register(eventBus);
    }

    public static RegistryObject<EntityVisualEffectType> register(String path) {
        return register(path, EntityVisualEffectType.properties());
    }

    public static RegistryObject<EntityVisualEffectType> register(String path, EntityVisualEffectType.Properties properties) {
        return VISUAL_EFFECTS.register(path, () -> new EntityVisualEffectType(properties));
    }

    public static RegistryObject<EntityVisualEffectType> register(ResourceKey<EntityVisualEffectType> key) {
        return register(key, EntityVisualEffectType.properties());
    }

    public static RegistryObject<EntityVisualEffectType> register(ResourceKey<EntityVisualEffectType> key, EntityVisualEffectType.Properties properties) {
        return register(key.location().getPath(), properties);
    }

    public static EntityVisualEffectType get(ResourceLocation id) {
        IForgeRegistry<EntityVisualEffectType> registry = REGISTRY.get();
        return registry == null ? null : registry.getValue(id);
    }

    public static Collection<ResourceLocation> registeredIds() {
        IForgeRegistry<EntityVisualEffectType> registry = REGISTRY.get();
        if (registry != null && !registry.isEmpty()) {
            return registry.getKeys().stream()
                    .sorted()
                    .toList();
        }

        return VISUAL_EFFECTS.getEntries().stream()
                .map(RegistryObject::getId)
                .sorted()
                .toList();
    }

    public static boolean isRegistered(ResourceLocation id) {
        IForgeRegistry<EntityVisualEffectType> registry = REGISTRY.get();
        if (registry != null) {
            return registry.containsKey(id);
        }

        return VISUAL_EFFECTS.getEntries().stream().anyMatch(effect -> effect.getId().equals(id));
    }

    public static ResourceLocation id(String path) {
        return new ResourceLocation(GoetyDelight.MODID, path);
    }

    public static ResourceKey<EntityVisualEffectType> key(String path) {
        return ResourceKey.create(ModRegistries.ENTITY_VISUAL_EFFECT_TYPE_KEY, id(path));
    }
}
