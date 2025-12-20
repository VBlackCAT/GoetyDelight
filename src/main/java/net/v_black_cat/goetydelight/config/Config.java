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
            .defineInRange("starlessNightMaxAttackCount", 5, 1,2147483647);


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
    private static final ForgeConfigSpec.ConfigValue<List<? extends String>> SPEED_BOOST_BLACKLIST = BUILDER
            .comment("List of entity types that are immune to movement speed boost from Lich's Chaos Stew")
            .defineListAllowEmpty("lichStewSpeedBoostBlacklist", List.of(
               "goety:redstone_monstrosity","goety:redstone_golem"
            ), Config::validateEntityName);
    private static final ForgeConfigSpec.BooleanValue POLARICE_AFFECTS_BOSSES = BUILDER
            .comment("Whether bosses are affected by Polarice item")
            .define("polariceAffectsBosses", false);

    private static final ForgeConfigSpec.DoubleValue POLARICE_HEALTH_THRESHOLD = BUILDER
            .comment("Maximum health threshold for entities to be affected by Polarice item (in half-hearts)")
            .defineInRange("polariceHealthThreshold", 50.0, 1.0, Float.MAX_VALUE);

    private static final ForgeConfigSpec.IntValue POLARICE_COOLDOWN = BUILDER
            .comment("The cooldown for Polarice item to use")
            .defineInRange("polarice_cooldown", 1800, 300, Integer.MAX_VALUE);

    private static final ForgeConfigSpec.IntValue POLARICE_COUNT = BUILDER
            .comment("The number of Polarice item can affect")
            .defineInRange("polarice_count", 10, 1, Integer.MAX_VALUE);

    //幻味草黑名单
    private static final ForgeConfigSpec.ConfigValue<List<? extends String>> METAMORPHIC_SCENT_GRASS_COPY_BLACKLIST = BUILDER
            .comment("A list of items that cannot be copied by Metamorphic Scent Grass")
            .defineListAllowEmpty("MetamorphicScentGrassCopyBlacklist",
                    List.of("goety_revelation:ascension_hard_candy",
                    "enigmaticdelicacy:abyssal_stew","enigmaticlegacy:forbidden_fruit","goetydelight:pure_drink","goetydelight:tainted_drink",
                    "goetydelight:snap_unholy_tripe","goetydelight:ancient_enchanted_golden_apple","goetydelight:lichs_chaos_stew",
                    "l2complements:life_essence","l2complements:totemic_apple","l2complements:enchanted_totemic_apple","hmag:insomnia_fruit",
                    "artifacts:everlasting_beef","artifacts:eternal_steak","born_in_chaos_v1:eternal_candy","avaritia_delight:infinity_apple",
                    "avaritia_delight:slice_of_endless_cake","avaritia_delight:infinity_taco","avaritia_delight:pasta_with_cosmic_meatballs",
                    "avaritia_delight:infinity_large_hamburger","minecraft:apple"), Config::NoValidateItemName);

    //幻味草持续时长倍率
    private static final ForgeConfigSpec.DoubleValue METAMORPHIC_SCENT_GRASS_DURATION_MULTIPLIER = BUILDER
            .comment("Duration multiplier for Metamorphic Scent Grass effect (0.0 to 1.0)")
            .defineInRange("metamorphicScentGrassDurationMultiplier", 0.2, 0.0, 1.0);
    //幻味草buff强度倍率
    private static final ForgeConfigSpec.DoubleValue METAMORPHIC_SCENT_GRASS_AMPLIFIER_MULTIPLIER = BUILDER
            .comment("Amplifier multiplier for Metamorphic Scent Grass effect (0.0 to 1.0)")
            .defineInRange("metamorphicScentGrassAmplifierMultiplier", 0.3, 0.0, 1.0);
    
    //幻味草复制数量
    private static final ForgeConfigSpec.IntValue METAMORPHIC_SCENT_GRASS_COPY_COUNT = BUILDER
                .comment("The maximum number of effects that can be copied by Metamorphic Scent Grass (0-64)")
                .defineInRange("metamorphicScentGrassCopyCount", 1, 0, 64);

    //幻味果黑名单
    private static final ForgeConfigSpec.ConfigValue<List<? extends String>> METAMORPHIC_SCENT_FRUIT_COPY_BLACKLIST = BUILDER
            .comment("A list of items that cannot be copied by Metamorphic Scent Fruit")
            .defineListAllowEmpty("MetamorphicScentFruitCopyBlacklist", List.of("goety_revelation:ascension_hard_candy",
                    "enigmaticdelicacy:abyssal_stew","enigmaticlegacy:forbidden_fruit","goetydelight:pure_drink","goetydelight:tainted_drink",
                    "goetydelight:snap_unholy_tripe","goetydelight:ancient_enchanted_golden_apple","goetydelight:lichs_chaos_stew",
                    "l2complements:life_essence","l2complements:totemic_apple","l2complements:enchanted_totemic_apple","hmag:insomnia_fruit",
                    "artifacts:everlasting_beef","artifacts:eternal_steak","born_in_chaos_v1:eternal_candy","avaritia_delight:infinity_apple",
                    "avaritia_delight:slice_of_endless_cake","avaritia_delight:infinity_taco","avaritia_delight:pasta_with_cosmic_meatballs",
                    "avaritia_delight:infinity_large_hamburger","minecraft:apple"), Config::NoValidateItemName);
    //幻味果复制数量
    private static final ForgeConfigSpec.IntValue METAMORPHIC_SCENT_FRUIT_COPY_COUNT = BUILDER
            .comment("The maximum number of effects that can be copied by Metamorphic Scent Fruit (1-64)")
            .defineInRange("metamorphicScentFruitCopyCount", 1, 1, 12);

    private static final ForgeConfigSpec.DoubleValue SOUL_AFFIX_DAMAGE_PER_LEVEL = BUILDER
            .comment("Damage increase per level of Soul Affix enchantment")
            .defineInRange("soulAffixDamagePerLevel", 1.0, 1.0, Double.MAX_VALUE);

    private static final ForgeConfigSpec.IntValue SOUL_AFFIX_SOUL_COST_PER_LEVEL = BUILDER
            .comment("Soul energy cost per level of Soul Affix enchantment")
            .defineInRange("soulAffixSoulCostPerLevel", 10, 1, Integer.MAX_VALUE);

    public static int getSoulAffixSoulCostPerLevel() {
        return SOUL_AFFIX_SOUL_COST_PER_LEVEL.get();
    }

    public static double getSoulAffixDamagePerLevel() {
        return SOUL_AFFIX_DAMAGE_PER_LEVEL.get();
    }

    public static double getMetamorphicScentGrassDurationMultiplier() {
        return METAMORPHIC_SCENT_GRASS_DURATION_MULTIPLIER.get();
    }

    public static double getMetamorphicScentGrassAmplifierMultiplier() {
        return METAMORPHIC_SCENT_GRASS_AMPLIFIER_MULTIPLIER.get();
    }

    public static int getMetamorphicScentFruitCopyCount() {
        return METAMORPHIC_SCENT_FRUIT_COPY_COUNT.get();
    }


    public static Set<Item> getMetamorphicScentGrassCopyBlacklist() {
        return METAMORPHIC_SCENT_GRASS_COPY_BLACKLIST.get().stream()
                .map(itemName -> ForgeRegistries.ITEMS.getValue(new ResourceLocation(itemName)))
                .collect(Collectors.toSet());
    }


    public static Set<Item> getMetamorphicScentFruitCopyBlacklist() {
        return METAMORPHIC_SCENT_FRUIT_COPY_BLACKLIST.get().stream()
                .map(itemName -> ForgeRegistries.ITEMS.getValue(new ResourceLocation(itemName)))
                .collect(Collectors.toSet());
    }

    public static int getMetamorphicScentGrassCopyCount() {
        return METAMORPHIC_SCENT_GRASS_COPY_COUNT.get();
    }
    public static int getPolariceCount() {
        return POLARICE_COUNT.get();
    }
    public static int getPolariceCooldown() {
        return POLARICE_COOLDOWN.get();
    }
    public static boolean getPolariceAffectsBosses() {
        return POLARICE_AFFECTS_BOSSES.get();
    }

    public static double getPolariceHealthThreshold() {
        return POLARICE_HEALTH_THRESHOLD.get();
    }
    public static Set<EntityType<?>> getSpeedBoostBlacklist() {
        return SPEED_BOOST_BLACKLIST.get().stream()
                .map(entityName -> ForgeRegistries.ENTITY_TYPES.getValue(new ResourceLocation(entityName)))
                .collect(Collectors.toSet());
    }

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
    private static boolean NoValidateItemName(final Object obj)
    {
        return true;
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