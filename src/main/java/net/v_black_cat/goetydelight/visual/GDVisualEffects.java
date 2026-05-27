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
