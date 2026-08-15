package net.v_black_cat.goetydelight.init;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.Item;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.event.config.ModConfigEvent;
import net.neoforged.neoforge.common.ModConfigSpec;
import net.v_black_cat.goetydelight.GoetyDelight;

import java.util.*;
import java.util.function.Consumer;
import java.util.stream.Collectors;

@EventBusSubscriber(modid = GoetyDelight.MODID)
public class ModConfig {
    private static final ModConfigSpec.Builder BUILDER = new ModConfigSpec.Builder();

    // ==================== 黑名单物品 ====================
    private static final ModConfigSpec.ConfigValue<List<? extends String>> BLACKLISTED_ITEMS = BUILDER
            .comment("A list of blacklisted items that will be hidden from creative tabs and prevent drops\n物品黑名单列表，这些物品将从创造模式标签页隐藏并阻止掉落")
            .defineListAllowEmpty("blacklistedItems", List.of(
                    "goetydelight:roasted_corpse_maggots",
                    "goetydelight:corpse_maggot",
                    "goetydelight:rotten_corpse_maggot_feast",
                    "goetydelight:rotten_corpse_maggot_feast_block"
            ), ModConfig::validateItemName);

    // ==================== 皇家蛋糕效果半径 ====================
    private static final ModConfigSpec.DoubleValue CAKE_EFFECT_RADIUS = BUILDER
            .comment("Effect radius for the cake item\n皇家蛋糕的效果半径")
            .defineInRange("cakeEffectRadius", 32.0, 1.0, 256.0);

    // ==================== 北极刨冰相关 ====================
    private static final ModConfigSpec.BooleanValue POLARICE_AFFECTS_BOSSES = BUILDER
            .comment("Whether bosses are affected by Polarice item\nBoss是否北极刨冰影响")
            .define("polariceAffectsBosses", false);

    private static final ModConfigSpec.DoubleValue POLARICE_HEALTH_THRESHOLD = BUILDER
            .comment("Maximum health threshold for entities to be affected by Polarice item (in half-hearts)\n实体受北极刨冰影响的最大生命值阈值（单位：半颗心）")
            .defineInRange("polariceHealthThreshold", 50.0, 1.0, Float.MAX_VALUE);

    private static final ModConfigSpec.IntValue POLARICE_COOLDOWN = BUILDER
            .comment("The cooldown for Polarice item to use\n北极刨冰的使用冷却时间（tick）")
            .defineInRange("polarice_cooldown", 1800, 300, Integer.MAX_VALUE);

    private static final ModConfigSpec.IntValue POLARICE_COUNT = BUILDER
            .comment("The number of Polarice item can affect\n北极刨冰可以影响的实体数量")
            .defineInRange("polarice_count", 10, 1, Integer.MAX_VALUE);

    // ==================== 幻味草黑名单 ====================
    private static final ModConfigSpec.ConfigValue<List<? extends String>> METAMORPHIC_SCENT_GRASS_COPY_BLACKLIST = BUILDER
            .comment("A list of items that cannot be copied by Metamorphic Scent Grass\n幻味草无法复制的物品黑名单")
            .defineListAllowEmpty("MetamorphicScentGrassCopyBlacklist",
                    List.of("goety_revelation:ascension_hard_candy",
                            "enigmaticdelicacy:abyssal_stew", "goetydelight:pure_drink", "goetydelight:tainted_drink",
                            "goetydelight:snap_unholy_tripe", "goetydelight:lichs_chaos_stew", "goetydelight:sundae_of_the_philosophers_potion",
                            "l2complements:totemic_apple", "l2complements:enchanted_totemic_apple", "hmag:insomnia_fruit",
                            "artifacts:everlasting_beef", "artifacts:eternal_steak", "born_in_chaos_v1:eternal_candy", "avaritia_delight:infinity_apple",
                            "avaritia_delight:slice_of_endless_cake", "avaritia_delight:infinity_taco", "avaritia_delight:pasta_with_cosmic_meatballs",
                            "avaritia_delight:infinity_large_hamburger", "minecraft:apple"), ModConfig::noValidateItemName);

    // ==================== 幻味草持续时长倍率 ====================
    private static final ModConfigSpec.DoubleValue METAMORPHIC_SCENT_GRASS_DURATION_MULTIPLIER = BUILDER
            .comment("Duration multiplier for Metamorphic Scent Grass effect (0.0 to 1.0)\n幻味草效果持续时间倍率（0.0-1.0）")
            .defineInRange("metamorphicScentGrassDurationMultiplier", 0.2, 0.0, 1.0);

    // ==================== 幻味草buff强度倍率 ====================
    private static final ModConfigSpec.DoubleValue METAMORPHIC_SCENT_GRASS_AMPLIFIER_MULTIPLIER = BUILDER
            .comment("Amplifier multiplier for Metamorphic Scent Grass effect (0.0 to 1.0)\n幻味草效果等级倍率（0.0-1.0）")
            .defineInRange("metamorphicScentGrassAmplifierMultiplier", 0.3, 0.0, 1.0);

    // ==================== 幻味草复制数量 ====================
    private static final ModConfigSpec.IntValue METAMORPHIC_SCENT_GRASS_COPY_COUNT = BUILDER
            .comment("The maximum number of effects that can be copied by Metamorphic Scent Grass (0-64)\n幻味草可复制的最大效果数量（0-64）")
            .defineInRange("metamorphicScentGrassCopyCount", 1, 0, 64);

    // ==================== 幻味果黑名单 ====================
    private static final ModConfigSpec.ConfigValue<List<? extends String>> METAMORPHIC_SCENT_FRUIT_COPY_BLACKLIST = BUILDER
            .comment("A list of items that cannot be copied by Metamorphic Scent Fruit\n幻味果无法复制的物品黑名单")
            .defineListAllowEmpty("MetamorphicScentFruitCopyBlacklist", List.of("goety_revelation:ascension_hard_candy",
                    "enigmaticdelicacy:abyssal_stew", "goetydelight:pure_drink", "goetydelight:tainted_drink",
                    "goetydelight:snap_unholy_tripe", "goetydelight:lichs_chaos_stew", "goetydelight:sundae_of_the_philosophers_potion",
                    "l2complements:totemic_apple", "l2complements:enchanted_totemic_apple", "hmag:insomnia_fruit",
                    "artifacts:everlasting_beef", "artifacts:eternal_steak", "born_in_chaos_v1:eternal_candy", "avaritia_delight:infinity_apple",
                    "avaritia_delight:slice_of_endless_cake", "avaritia_delight:infinity_taco", "avaritia_delight:pasta_with_cosmic_meatballs",
                    "avaritia_delight:infinity_large_hamburger", "minecraft:apple"), ModConfig::noValidateItemName);

    // ==================== 幻味果复制数量 ====================
    private static final ModConfigSpec.IntValue METAMORPHIC_SCENT_FRUIT_COPY_COUNT = BUILDER
            .comment("The maximum number of effects that can be copied by Metamorphic Scent Fruit (1-64)\n幻味果可复制的最大效果数量（1-64）")
            .defineInRange("metamorphicScentFruitCopyCount", 1, 1, 12);

    // ==================== Shift速度倍数 ====================
    private static final ModConfigSpec.DoubleValue SHIFT_SPEED_MULTIPLIER = BUILDER
            .comment("Movement speed multiplier when Shift key is pressed\n按下Shift键时的移动速度倍率")
            .defineInRange("shiftSpeedMultiplier", 2.0, 0.0, Double.MAX_VALUE);

    // ==================== 虚妄箴言伤害配置 ====================
    private static final ModConfigSpec.DoubleValue FALSE_PROVERBS_NORMAL_DAMAGE_MULTIPLIER = BUILDER
            .comment("Damage multiplier when holding False Proverbs without Shift and not using item\n虚妄箴言普通攻击伤害倍率")
            .defineInRange("falseProverbsNormalDamageMultiplier", 1.5, 0.0, Double.MAX_VALUE);

    private static final ModConfigSpec.DoubleValue FALSE_PROVERBS_SHIFT_DAMAGE_MULTIPLIER = BUILDER
            .comment("Damage multiplier when holding False Proverbs while Shift is pressed (non-backstab)\n虚妄箴言按Shift时的伤害倍率（非背刺）")
            .defineInRange("falseProverbsShiftDamageMultiplier", 1.5, 0.0, Double.MAX_VALUE);

    private static final ModConfigSpec.DoubleValue FALSE_PROVERBS_BACKSTAB_DAMAGE_MULTIPLIER = BUILDER
            .comment("Damage multiplier when performing backstab with False Proverbs while Shift is pressed\n虚妄箴言按Shift且背刺时的伤害倍率")
            .defineInRange("falseProverbsBackstabDamageMultiplier", 2.5, 0.0, Double.MAX_VALUE);

    // ==================== 灵魂附魔相关 ====================
    private static final ModConfigSpec.DoubleValue SOUL_AFFIX_DAMAGE_PER_LEVEL = BUILDER
            .comment("Damage increase per level of Soul Affix enchantment\n灵魂附加附魔每级增加的伤害值")
            .defineInRange("soulAffixDamagePerLevel", 0.4, 0.0, Double.MAX_VALUE);

    private static final ModConfigSpec.IntValue SOUL_AFFIX_SOUL_COST_PER_LEVEL = BUILDER
            .comment("Soul energy cost per level of Soul Affix enchantment\n灵魂附加附魔每级消耗的灵魂能量")
            .defineInRange("soulAffixSoulCostPerLevel", 5, 1, Integer.MAX_VALUE);

    private static final ModConfigSpec.BooleanValue DISABLE_SOUL_MENDING = BUILDER
            .comment("Disable Soul Mending enchantment entirely\n完全禁用灵魂修补附魔")
            .define("disableSoulMending", false);

    private static final ModConfigSpec.BooleanValue DISABLE_SOUL_HEALING = BUILDER
            .comment("Disable Soul Healing enchantment entirely\n完全禁用溢魂弥躯附魔")
            .define("disableSoulHealing", false);

    private static final ModConfigSpec.BooleanValue DISABLE_SOUL_AFFIX = BUILDER
            .comment("Disable Soul Affix enchantment entirely\n完全禁用灵魂附加附魔")
            .define("disableSoulAffix", false);

    // ==================== 骷髅红眼特效 ====================
    private static final ModConfigSpec.BooleanValue SKELETON_RED_EYE_EFFECT_ENABLED = BUILDER
            .comment("Whether to enable the skeleton red-eye effect (red eye flash when a skeleton targets a low-health player)\n是否启用骷髅红眼特效（骷髅锁定低血量玩家时触发的红眼闪光特效）")
            .define("skeletonRedEyeEffectEnabled", true);

    private static final ModConfigSpec.ConfigValue<List<? extends String>> SOUL_MENDING_BLACKLIST = BUILDER
            .comment("A list of items that cannot be enchanted with Soul Mending\n无法附魔灵魂修补的物品列表")
            .defineListAllowEmpty("soulMendingBlacklist", List.of(), ModConfig::noValidateItemName);

    private static final ModConfigSpec.ConfigValue<List<? extends String>> SOUL_HEALING_BLACKLIST = BUILDER
            .comment("A list of items that cannot be enchanted with Soul Healing\n无法附魔溢魂弥躯的物品列表")
            .defineListAllowEmpty("soulHealingBlacklist", List.of(), ModConfig::noValidateItemName);

    private static final ModConfigSpec.ConfigValue<List<? extends String>> SOUL_AFFIX_BLACKLIST = BUILDER
            .comment("A list of items that cannot be enchanted with Soul Affix\n无法附魔灵魂附加的物品列表")
            .defineListAllowEmpty("soulAffixBlacklist", List.of(), ModConfig::noValidateItemName);

    // ==================== 巫妖乱炖相关 ====================
    private static final ModConfigSpec.DoubleValue LICH_CHAOS_STEW_BOOST_PERCENTAGE = BUILDER
            .comment("Boost percentage per stack of Lich's Chaos Stew for minions (0.15 = 15%)\n巫妖乱炖每层为仆从提供的加成百分比（0.15 = 15%）")
            .defineInRange("lichChaosStewBoostPercentage", 0.15, 0.0, 1.0);

    private static final ModConfigSpec.IntValue LICH_STEW_MAX_COUNT = BUILDER
            .comment("Maximum stack count for Lich's Chaos Stew effect\n巫妖乱炖效果的最大叠加层数")
            .defineInRange("lichStewMaxCount", 6, 1, 20);

    // ==================== 暗夜之心豌豆汤相关 ====================
    private static final ModConfigSpec.DoubleValue NIGHT_HEART_PEA_SOUP_BOOST_PERCENTAGE = BUILDER
            .comment("Boost percentage per stack of Night Heart Pea Soup for minions (0.05 = 5%)\n暗夜之心豌豆汤每层为仆从提供的加成百分比（0.05 = 5%）")
            .defineInRange("nightHeartPeaSoupBoostPercentage", 0.05, 0.0, 1.0);

    private static final ModConfigSpec.IntValue NIGHT_PEA_SOUP_MAX_COUNT = BUILDER
            .comment("Maximum stack count for Night Heart Pea Soup effect\n暗夜之心豌豆汤效果的最大叠加层数")
            .defineInRange("nightPeaSoupMaxCount", 12, 1, 30);

    // ==================== Goety Revelation 兼容性 ====================
    private static final ModConfigSpec.BooleanValue ENABLE_GOETY_REVELATION_COMPATIBILITY = BUILDER
            .comment("Whether to enable compatibility with goety_revelation mod\n是否启用与goety_revelation模组的兼容性")
            .define("enableGoetyRevelationCompatibility", true);

    // ==================== 玩家模型缩放 ====================
    private static final ModConfigSpec.ConfigValue<List<? extends String>> PLAYER_MODEL_SCALES = BUILDER
            .comment("Player model scale settings (format: playerName=scale)\n玩家模型缩放设置（格式：玩家名称=缩放比例）\n注：请勿在高版本ysm中使用该功能（2.6.2版本可用，2.6.5版本不可用）")
            .defineListAllowEmpty("playerModelScales", List.of(
                    "Steve=1.0", "Alex=1.0", "wu1wu2=1.0"
            ), ModConfig::validatePlayerScaleEntry);

    // ==================== 万毒盛宴相关配置 ====================
    private static final ModConfigSpec.BooleanValue TEN_THOUSAND_POISON_FEAST_USE_WHITELIST = BUILDER
            .comment("If true, use whitelist mode (only apply debuffs in the list); if false, use blacklist mode (exclude debuffs in the list)\n" +
                    "true=白名单模式（仅施加列表中的debuff），false=黑名单模式（排除列表中的debuff）")
            .define("tenThousandPoisonFeastUseWhitelist", true);

    private static final ModConfigSpec.ConfigValue<List<? extends String>> TEN_THOUSAND_POISON_FEAST_EFFECT_LIST = BUILDER
            .comment("""
                    A list of debuff effects for whitelist/blacklist (mode controlled by tenThousandPoisonFeastUseWhitelist)
                    万毒盛宴的效果白名单/黑名单（模式由tenThousandPoisonFeastUseWhitelist控制）
                    Supports:
                      - Effect ID: 'minecraft:slowness' (exact match)
                      - MOD ID partial match: 'mod:goety' (matches any effect from modid containing 'goety', supports sub-mods)
                    支持格式：
                      - 效果ID：'minecraft:slowness'（精确匹配）
                      - MOD ID部分匹配：'mod:goety'（匹配modid包含'goety'的所有效果，支持附属模组）""")
            .defineListAllowEmpty("tenThousandPoisonFeastEffectList", List.of(
                    "minecraft:slowness", "minecraft:mining_fatigue", "minecraft:poison", "minecraft:wither",
                    "minecraft:blindness", "minecraft:nausea", "minecraft:darkness", "minecraft:weakness",
                    "minecraft:hunger", "minecraft:unluck", "minecraft:bad_omen", "minecraft:levitation",
                    "mod:goety", "mod:twilightforest", "mod:quark", "mod:jerotes", "mod:delight", "mod:cataclysm",
                    "mod:enigmatic", "mod:aether", "mod:born_in_chaos_v1", "mod:spell", "mod:iron"
            ), entry -> {
                if (!(entry instanceof String str)) return false;
                if (str.startsWith("mod:")) {
                    String modidPartial = str.substring(4);
                    return !modidPartial.isEmpty();
                }
                return validateEffectName(str);
            });

    private static final ModConfigSpec.ConfigValue<List<? extends String>> TEN_THOUSAND_POISON_FEAST_LEVEL_CONFIG = BUILDER
            .comment("""
                    Level range configuration for specific debuffs (format: effect_id=min-max)
                    万毒宴特定效果等级范围配置（格式：效果id=最小值-最大值）
                    Priority: Level/Duration Config > Whitelist/Blacklist > Default Config
                    优先级：等级/时长配置 > 白名单/黑名单 > 默认配置""")
            .defineListAllowEmpty("tenThousandPoisonFeastLevelConfig", List.of(
                    "minecraft:slowness=0-4",
                    "minecraft:weakness=0-3",
                    "minecraft:wither=0-2",
                    "minecraft:blindness=0-1",
                    "minecraft:nausea=0-1",
                    "minecraft:hunger=0-3",
                    "minecraft:mining_fatigue=0-3"
            ), entry -> {
                if (!(entry instanceof String str)) return false;
                String[] parts = str.split("=");
                if (parts.length != 2) return false;
                String[] range = parts[1].split("-");
                if (range.length != 2) return false;
                try {
                    int min = Integer.parseInt(range[0]);
                    int max = Integer.parseInt(range[1]);
                    return min >= 0 && max >= min && validateEffectName(parts[0]);
                } catch (NumberFormatException e) {
                    return false;
                }
            });

    private static final ModConfigSpec.ConfigValue<List<? extends String>> TEN_THOUSAND_POISON_FEAST_DURATION_CONFIG = BUILDER
            .comment("""
                    Duration range configuration for specific debuffs (format: effect_id=min-max, unit: minutes)
                    万毒宴特定效果时长范围配置（格式：效果id=最小值-最大值，单位：分钟）
                    Priority: Level/Duration Config > Whitelist/Blacklist > Default Config
                    优先级：等级/时长配置 > 白名单/黑名单 > 默认配置""")
            .defineListAllowEmpty("tenThousandPoisonFeastDurationConfig", List.of(
                    "minecraft:slowness=0.5-3",
                    "minecraft:weakness=0.5-3",
                    "minecraft:wither=0.25-1.5",
                    "minecraft:blindness=0.25-0.5",
                    "minecraft:nausea=0.25-0.5",
                    "minecraft:hunger=0.5-2",
                    "minecraft:mining_fatigue=0.5-2"
            ), entry -> {
                if (!(entry instanceof String str)) return false;
                String[] parts = str.split("=");
                if (parts.length != 2) return false;
                String[] range = parts[1].split("-");
                if (range.length != 2) return false;
                try {
                    double min = Double.parseDouble(range[0]);
                    double max = Double.parseDouble(range[1]);
                    return min > 0 && max >= min && validateEffectName(parts[0]);
                } catch (NumberFormatException e) {
                    return false;
                }
            });

    private static final ModConfigSpec.IntValue TEN_THOUSAND_POISON_FEAST_DEFAULT_MIN_LEVEL = BUILDER
            .comment("Default minimum level for debuffs not configured in LevelConfig\n未在LevelConfig中配置的效果的默认最小等级")
            .defineInRange("tenThousandPoisonFeastDefaultMinLevel", 0, 0, 255);

    private static final ModConfigSpec.IntValue TEN_THOUSAND_POISON_FEAST_DEFAULT_MAX_LEVEL = BUILDER
            .comment("Default maximum level for debuffs not configured in LevelConfig\n未在LevelConfig中配置的效果的默认最大等级")
            .defineInRange("tenThousandPoisonFeastDefaultMaxLevel", 2, 0, 255);

    private static final ModConfigSpec.DoubleValue TEN_THOUSAND_POISON_FEAST_DEFAULT_MIN_DURATION = BUILDER
            .comment("Default minimum duration (minutes) for debuffs not configured in DurationConfig\n未在DurationConfig中配置的效果的默认最短持续时间（分钟）")
            .defineInRange("tenThousandPoisonFeastDefaultMinDuration", 0.25, 0.0, Double.MAX_VALUE);

    private static final ModConfigSpec.DoubleValue TEN_THOUSAND_POISON_FEAST_DEFAULT_MAX_DURATION = BUILDER
            .comment("Default maximum duration (minutes) for debuffs not configured in DurationConfig\n未在DurationConfig中配置的效果的默认最长持续时间（分钟）")
            .defineInRange("tenThousandPoisonFeastDefaultMaxDuration", 3.0, 0.0, Double.MAX_VALUE);

    private static final ModConfigSpec.IntValue TEN_THOUSAND_POISON_FEAST_EFFECT_COUNT = BUILDER
            .comment("Number of random debuffs to apply when eating Ten Thousand Poison Feast\n食用万毒盛宴时随机施加的debuff数量")
            .defineInRange("tenThousandPoisonFeastEffectCount", 3, 1, 20);

    private static final ModConfigSpec.IntValue TEN_THOUSAND_POISON_FEAST_MIN_ITEM_COUNT = BUILDER
            .comment("Minimum number of items required in crafting grid to create Ten Thousand Poison Feast\n" +
                    "合成万毒盛宴所需的最少物品数量")
            .defineInRange("tenThousandPoisonFeastMinItemCount", 4, 1, 9);

    private static final ModConfigSpec.IntValue TEN_THOUSAND_POISON_FEAST_MIN_DEBUFF_COUNT = BUILDER
            .comment("Minimum number of unique debuff types from all ingredients required to create Ten Thousand Poison Feast\n" +
                    "合成万毒盛宴所需的最少debuff种类数（所有材料提供的不同debuff种类之和）")
            .defineInRange("tenThousandPoisonFeastMinDebuffCount", 8, 1, 100);

    // ==================== 构建 Spec ====================
    public static final ModConfigSpec SPEC = BUILDER.build();

    // ==================== 缓存字段 ====================
    public static Set<Item> blacklistedItems;
    private static Consumer<Void> blackListUpdateListener;

    // ==================== Getter 方法 ====================

    public static Set<Item> getSoulMendingBlacklist() {
        return SOUL_MENDING_BLACKLIST.get().stream()
                .map(itemName -> BuiltInRegistries.ITEM.get(ResourceLocation.parse(itemName)))
                .collect(Collectors.toSet());
    }

    public static Set<Item> getSoulHealingBlacklist() {
        return SOUL_HEALING_BLACKLIST.get().stream()
                .map(itemName -> BuiltInRegistries.ITEM.get(ResourceLocation.parse(itemName)))
                .collect(Collectors.toSet());
    }

    public static Set<Item> getSoulAffixBlacklist() {
        return SOUL_AFFIX_BLACKLIST.get().stream()
                .map(itemName -> BuiltInRegistries.ITEM.get(ResourceLocation.parse(itemName)))
                .collect(Collectors.toSet());
    }

    public static double getLichChaosStewBoostPercentage() {
        return LICH_CHAOS_STEW_BOOST_PERCENTAGE.get();
    }

    public static int getLichStewMaxCount() {
        return LICH_STEW_MAX_COUNT.get();
    }

    public static double getNightHeartPeaSoupBoostPercentage() {
        return NIGHT_HEART_PEA_SOUP_BOOST_PERCENTAGE.get();
    }

    public static int getNightPeaSoupMaxCount() {
        return NIGHT_PEA_SOUP_MAX_COUNT.get();
    }

    public static boolean isSoulMendingDisabled() {
        return DISABLE_SOUL_MENDING.get();
    }

    public static boolean isSoulHealingDisabled() {
        return DISABLE_SOUL_HEALING.get();
    }

    public static boolean isSoulAffixDisabled() {
        return DISABLE_SOUL_AFFIX.get();
    }

    public static boolean isSkeletonRedEyeEffectEnabled() {
        return SKELETON_RED_EYE_EFFECT_ENABLED.get();
    }

    public static int getSoulAffixSoulCostPerLevel() {
        return SOUL_AFFIX_SOUL_COST_PER_LEVEL.get();
    }

    public static double getSoulAffixDamagePerLevel() {
        return SOUL_AFFIX_DAMAGE_PER_LEVEL.get();
    }

    public static Map<String, Float> getPlayerModelScales() {
        return PLAYER_MODEL_SCALES.get().stream()
                .map(entry -> entry.split("="))
                .filter(parts -> parts.length == 2)
                .collect(Collectors.toMap(
                        parts -> parts[0].trim(),
                        parts -> Float.parseFloat(parts[1].trim())
                ));
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
                .map(itemName -> BuiltInRegistries.ITEM.get(ResourceLocation.parse(itemName)))
                .collect(Collectors.toSet());
    }

    public static Set<Item> getMetamorphicScentFruitCopyBlacklist() {
        return METAMORPHIC_SCENT_FRUIT_COPY_BLACKLIST.get().stream()
                .map(itemName -> BuiltInRegistries.ITEM.get(ResourceLocation.parse(itemName)))
                .collect(Collectors.toSet());
    }

    public static boolean isGoetyRevelationCompatibilityEnabled() {
        return ENABLE_GOETY_REVELATION_COMPATIBILITY.get();
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

    public static double getCakeEffectRadius() {
        return CAKE_EFFECT_RADIUS.get();
    }

    public static double getShiftSpeedMultiplier() {
        return SHIFT_SPEED_MULTIPLIER.get();
    }

    public static float getFalseProverbsNormalDamageMultiplier() {
        return FALSE_PROVERBS_NORMAL_DAMAGE_MULTIPLIER.get().floatValue();
    }

    public static float getFalseProverbsShiftDamageMultiplier() {
        return FALSE_PROVERBS_SHIFT_DAMAGE_MULTIPLIER.get().floatValue();
    }

    public static float getFalseProverbsBackstabDamageMultiplier() {
        return FALSE_PROVERBS_BACKSTAB_DAMAGE_MULTIPLIER.get().floatValue();
    }

    // ==================== TenThousandPoison 配置 Getter 方法 ====================

    public static boolean isTenThousandPoisonFeastUseWhitelist() {
        return TEN_THOUSAND_POISON_FEAST_USE_WHITELIST.get();
    }

    public static Set<String> getTenThousandPoisonFeastEffectList() {
        return Set.copyOf(TEN_THOUSAND_POISON_FEAST_EFFECT_LIST.get());
    }

    public static Map<ResourceLocation, int[]> getTenThousandPoisonFeastLevelConfig() {
        return parseEffectRangeConfig(TEN_THOUSAND_POISON_FEAST_LEVEL_CONFIG.get());
    }

    public static Map<ResourceLocation, double[]> getTenThousandPoisonFeastDurationConfig() {
        return parseEffectDurationConfig(TEN_THOUSAND_POISON_FEAST_DURATION_CONFIG.get());
    }

    public static int getTenThousandPoisonFeastDefaultMinLevel() {
        return TEN_THOUSAND_POISON_FEAST_DEFAULT_MIN_LEVEL.get();
    }

    public static int getTenThousandPoisonFeastDefaultMaxLevel() {
        return TEN_THOUSAND_POISON_FEAST_DEFAULT_MAX_LEVEL.get();
    }

    public static double getTenThousandPoisonFeastDefaultMinDuration() {
        return TEN_THOUSAND_POISON_FEAST_DEFAULT_MIN_DURATION.get();
    }

    public static double getTenThousandPoisonFeastDefaultMaxDuration() {
        return TEN_THOUSAND_POISON_FEAST_DEFAULT_MAX_DURATION.get();
    }

    public static int getTenThousandPoisonFeastEffectCount() {
        return TEN_THOUSAND_POISON_FEAST_EFFECT_COUNT.get();
    }

    public static int getTenThousandPoisonFeastMinItemCount() {
        return TEN_THOUSAND_POISON_FEAST_MIN_ITEM_COUNT.get();
    }

    public static int getTenThousandPoisonFeastMinDebuffCount() {
        return TEN_THOUSAND_POISON_FEAST_MIN_DEBUFF_COUNT.get();
    }

    public static int minutesToTicks(double minutes) {
        return (int) Math.round(minutes * 60 * 20);
    }

    public static boolean isEffectInFilterList(ResourceLocation effectId) {
        Set<String> effectList = getTenThousandPoisonFeastEffectList();
        String effectIdStr = effectId.toString();
        String effectModid = effectId.getNamespace();

        for (String entry : effectList) {
            if (entry.startsWith("mod:")) {
                String modidPartial = entry.substring(4).toLowerCase();
                if (effectModid.toLowerCase().contains(modidPartial)) {
                    return true;
                }
            } else {
                if (effectIdStr.equals(entry)) {
                    return true;
                }
            }
        }
        return false;
    }

    // ==================== 解析辅助方法 ====================

    private static Map<ResourceLocation, int[]> parseEffectRangeConfig(List<? extends String> configList) {
        Map<ResourceLocation, int[]> result = new HashMap<>();
        for (String entry : configList) {
            String[] parts = entry.split("=");
            if (parts.length == 2) {
                String[] range = parts[1].split("-");
                if (range.length == 2) {
                    try {
                        ResourceLocation effectId = ResourceLocation.parse(parts[0]);
                        int min = Integer.parseInt(range[0]);
                        int max = Integer.parseInt(range[1]);
                        result.put(effectId, new int[]{min, max});
                    } catch (NumberFormatException ignored) {
                    }
                }
            }
        }
        return result;
    }

    private static Map<ResourceLocation, double[]> parseEffectDurationConfig(List<? extends String> configList) {
        Map<ResourceLocation, double[]> result = new HashMap<>();
        for (String entry : configList) {
            String[] parts = entry.split("=");
            if (parts.length == 2) {
                String[] range = parts[1].split("-");
                if (range.length == 2) {
                    try {
                        ResourceLocation effectId = ResourceLocation.parse(parts[0]);
                        double min = Double.parseDouble(range[0]);
                        double max = Double.parseDouble(range[1]);
                        result.put(effectId, new double[]{min, max});
                    } catch (NumberFormatException ignored) {
                    }
                }
            }
        }
        return result;
    }

    // ==================== 验证方法 ====================

    private static boolean validateItemName(final Object obj) {
        return obj instanceof final String itemName && BuiltInRegistries.ITEM.containsKey(ResourceLocation.parse(itemName));
    }

    private static boolean validateEffectName(final Object obj) {
        return obj instanceof final String effectName && BuiltInRegistries.MOB_EFFECT.containsKey(ResourceLocation.parse(effectName));
    }

    private static boolean validateEntityName(final Object obj) {
        return obj instanceof final String entityName && BuiltInRegistries.ENTITY_TYPE.containsKey(ResourceLocation.parse(entityName));
    }

    private static boolean noValidateItemName(final Object obj) {
        return true;
    }

    private static boolean validatePlayerScaleEntry(final Object obj) {
        if (!(obj instanceof String entry)) return false;
        String[] parts = entry.split("=");
        if (parts.length != 2) return false;
        try {
            float scale = Float.parseFloat(parts[1].trim());
            return scale > 0;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    // ==================== 事件监听 ====================

    @SubscribeEvent
    static void onLoad(final ModConfigEvent event) {
        blacklistedItems = BLACKLISTED_ITEMS.get().stream()
                .map(itemName -> BuiltInRegistries.ITEM.get(ResourceLocation.parse(itemName)))
                .collect(Collectors.toSet());

        if (blackListUpdateListener != null) {
            blackListUpdateListener.accept(null);
        }
    }

    // ==================== 回调注册 ====================

    public static void registerBlackListUpdateListener(Consumer<Void> listener) {
        blackListUpdateListener = listener;
    }
}
