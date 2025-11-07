package net.v_black_cat.goetydelight.config;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.Item;
import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.config.ModConfigEvent;
import net.minecraftforge.registries.ForgeRegistries;
import net.v_black_cat.goetydelight.GoetyDelight;

import java.util.List;
import java.util.Set;
import java.util.function.Consumer;
import java.util.stream.Collectors;


@Mod.EventBusSubscriber(modid = GoetyDelight.MODID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class Config
{
    private static final ForgeConfigSpec.Builder BUILDER = new ForgeConfigSpec.Builder();

    
    private static final ForgeConfigSpec.ConfigValue<List<? extends String>> BLACKLISTED_ITEMS = BUILDER
            .comment("A list of blacklisted items that will be hidden from creative tabs and prevent drops")
            .defineListAllowEmpty("blacklistedItems", List.of(
                    "goetydelight:roasted_corpse_maggots",
                    "goetydelight:corpse_maggot",
                    "goetydelight:rotten_corpse_maggot_feast",
                    "goetydelight:rotten_corpse_maggot_feast_block"
            ), Config::validateItemName);

    private static final ForgeConfigSpec.IntValue MAX_ATTACK_COUNT = BUILDER
            .comment("Maximum attack count for Starless Night item")
            .defineInRange("starlessNightMaxAttackCount", 10, 0,2147483646);


    private static final ForgeConfigSpec.DoubleValue STARLESS_NIGHT_SEARCH_RANGE = BUILDER
            .comment("Search range for chain damage effect of Starless Night item")
            .defineInRange("starlessNightSearchRange", 16.0, 1.0, 128.0);

    private static final ForgeConfigSpec.IntValue STARLESS_NIGHT_MAX_CHAIN_TARGETS = BUILDER
            .comment("Maximum number of targets for chain damage effect of Starless Night item")
            .defineInRange("starlessNightMaxChainTargets", 10, 1, 2147483646);
    private static final ForgeConfigSpec.ConfigValue<List<? extends String>> STARLESS_NIGHT_WHITELIST = BUILDER
            .comment("List of entity types that are immune to Starless Night damage when tamed by the player")
            .defineListAllowEmpty("starlessNightWhitelist", List.of(
                    "minecraft:villager"
            ), Config::validateEntityName);
    private static final ForgeConfigSpec.DoubleValue CAKE_EFFECT_RADIUS = BUILDER
            .comment("Effect radius for the cake item")
            .defineInRange("cakeEffectRadius", 32.0, 1.0, 256.0);

    public static double getCakeEffectRadius() {
        return CAKE_EFFECT_RADIUS.get();
    }

    private static boolean validateEntityName(final Object obj) {
        return obj instanceof final String entityName && ForgeRegistries.ENTITY_TYPES.containsKey(new ResourceLocation(entityName));
    }

    public static Set<EntityType<?>> getStarlessNightWhitelist() {
        return STARLESS_NIGHT_WHITELIST.get().stream()
                .map(entityName -> ForgeRegistries.ENTITY_TYPES.getValue(new ResourceLocation(entityName)))
                .collect(Collectors.toSet());
    }

    public static final ForgeConfigSpec SPEC = BUILDER.build();

    public static Set<Item> blacklistedItems;
    
    private static Consumer<Void> blackListUpdateListener;

    private static boolean validateItemName(final Object obj)
    {
        return obj instanceof final String itemName && ForgeRegistries.ITEMS.containsKey(new ResourceLocation(itemName));
    }

    @SubscribeEvent
    static void onLoad(final ModConfigEvent event)
    {
        
        blacklistedItems = BLACKLISTED_ITEMS.get().stream()
                .map(itemName -> ForgeRegistries.ITEMS.getValue(new ResourceLocation(itemName)))
                .collect(Collectors.toSet());
        
        
        if (blackListUpdateListener != null) {
            blackListUpdateListener.accept(null);
        }
    }
    
    public static void registerBlackListUpdateListener(Consumer<Void> listener) {
        blackListUpdateListener = listener;
    }
    public static int getMaxAttackCount() {
        return MAX_ATTACK_COUNT.get();
    }
    public static double getStarlessNightSearchRange() {
        return STARLESS_NIGHT_SEARCH_RANGE.get();
    }
    public static int getStarlessNightMaxChainTargets() {
        return STARLESS_NIGHT_MAX_CHAIN_TARGETS.get();
    }
}