package net.v_black_cat.goetydelight.config;

import com.electronwill.nightconfig.core.file.CommentedFileConfig;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.config.ModConfigEvent;
import net.minecraftforge.registries.ForgeRegistries;
import net.v_black_cat.goetydelight.GoetyDelight;
import net.v_black_cat.goetydelight.util.ConvertServantUtil;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;
import java.util.stream.Collectors;


@Mod.EventBusSubscriber(modid = GoetyDelight.MODID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class Config
{
    private static final ForgeConfigSpec.Builder BUILDER = new ForgeConfigSpec.Builder();

    // ==================== 旧值缓存（线程安全） ====================
    private static final Map<String, Object> LEGACY_VALUES = new ConcurrentHashMap<>();
    private static final AtomicBoolean LEGACY_CAPTURED = new AtomicBoolean(false);

    // ==================== 旧 key -> 新 ConfigValue 映射 ====================
    private static final Map<String, ForgeConfigSpec.ConfigValue<?>> LEGACY_KEY_MAP = new HashMap<>();

    // ==================== 旧 key -> 新路径 ====================
    private static final Map<String, String> LEGACY_PATH_MAP = new HashMap<>();

    // 配置界面用到的文案已全部迁移到语言文件：
    //   标签   goetydelight.configuration.<配置完整路径>
    //   说明   goetydelight.configuration.<配置完整路径>.tooltip（可选）
    //   分组   goetydelight.configuration.group.<分组完整路径>

    // ==================== 字段声明（全部先声明，后赋值） ====================

    // 杂项 - 物品
    private static final ForgeConfigSpec.ConfigValue<List<? extends String>> BLACKLISTED_ITEMS;
    // 食物 - 蛋糕
    private static final ForgeConfigSpec.DoubleValue CAKE_EFFECT_RADIUS;
    // 食物 - 北极刨冰
    private static final ForgeConfigSpec.BooleanValue POLARICE_AFFECTS_BOSSES;
    private static final ForgeConfigSpec.DoubleValue POLARICE_HEALTH_THRESHOLD;
    private static final ForgeConfigSpec.IntValue POLARICE_COOLDOWN;
    private static final ForgeConfigSpec.IntValue POLARICE_COUNT;
    private static final ForgeConfigSpec.ConfigValue<List<? extends String>> EXTRA_BANNED_ENTITIES;
    // 食物 - 幻味草
    private static final ForgeConfigSpec.ConfigValue<List<? extends String>> METAMORPHIC_SCENT_GRASS_COPY_BLACKLIST;
    private static final ForgeConfigSpec.DoubleValue METAMORPHIC_SCENT_GRASS_DURATION_MULTIPLIER;
    private static final ForgeConfigSpec.DoubleValue METAMORPHIC_SCENT_GRASS_AMPLIFIER_MULTIPLIER;
    private static final ForgeConfigSpec.IntValue METAMORPHIC_SCENT_GRASS_COPY_COUNT;
    // 食物 - 幻味果
    private static final ForgeConfigSpec.ConfigValue<List<? extends String>> METAMORPHIC_SCENT_FRUIT_COPY_BLACKLIST;
    private static final ForgeConfigSpec.IntValue METAMORPHIC_SCENT_FRUIT_COPY_COUNT;
    // 食物 - 巫妖乱炖
    private static final ForgeConfigSpec.DoubleValue LICH_CHAOS_STEW_BOOST_PERCENTAGE;
    private static final ForgeConfigSpec.IntValue LICH_STEW_MAX_COUNT;
    // 食物 - 暗夜之心豌豆汤
    private static final ForgeConfigSpec.DoubleValue NIGHT_HEART_PEA_SOUP_BOOST_PERCENTAGE;
    private static final ForgeConfigSpec.IntValue NIGHT_PEA_SOUP_MAX_COUNT;
    // 食物 - 万毒盛宴
    private static final ForgeConfigSpec.BooleanValue TEN_THOUSAND_POISON_FEAST_USE_WHITELIST;
    private static final ForgeConfigSpec.ConfigValue<List<? extends String>> TEN_THOUSAND_POISON_FEAST_EFFECT_LIST;
    private static final ForgeConfigSpec.ConfigValue<List<? extends String>> TEN_THOUSAND_POISON_FEAST_LEVEL_CONFIG;
    private static final ForgeConfigSpec.ConfigValue<List<? extends String>> TEN_THOUSAND_POISON_FEAST_DURATION_CONFIG;
    private static final ForgeConfigSpec.IntValue TEN_THOUSAND_POISON_FEAST_DEFAULT_MIN_LEVEL;
    private static final ForgeConfigSpec.IntValue TEN_THOUSAND_POISON_FEAST_DEFAULT_MAX_LEVEL;
    private static final ForgeConfigSpec.DoubleValue TEN_THOUSAND_POISON_FEAST_DEFAULT_MIN_DURATION;
    private static final ForgeConfigSpec.DoubleValue TEN_THOUSAND_POISON_FEAST_DEFAULT_MAX_DURATION;
    private static final ForgeConfigSpec.IntValue TEN_THOUSAND_POISON_FEAST_EFFECT_COUNT;
    private static final ForgeConfigSpec.IntValue TEN_THOUSAND_POISON_FEAST_MIN_ITEM_COUNT;
    private static final ForgeConfigSpec.IntValue TEN_THOUSAND_POISON_FEAST_MIN_DEBUFF_COUNT;
    // 工具 - 战斗
    private static final ForgeConfigSpec.DoubleValue SHIFT_SPEED_MULTIPLIER;
    private static final ForgeConfigSpec.DoubleValue LIVING_HURT_DAMAGE_MULTIPLIER;
    private static final ForgeConfigSpec.DoubleValue LIVING_DAMAGE_GENERAL_MULTIPLIER;
    private static final ForgeConfigSpec.DoubleValue LIVING_DAMAGE_BACKSTAB_MULTIPLIER;
    // 工具 - 附魔 - 灵魂附加
    private static final ForgeConfigSpec.DoubleValue SOUL_AFFIX_DAMAGE_PER_LEVEL;
    private static final ForgeConfigSpec.IntValue SOUL_AFFIX_SOUL_COST_PER_LEVEL;
    private static final ForgeConfigSpec.BooleanValue DISABLE_SOUL_AFFIX;
    private static final ForgeConfigSpec.ConfigValue<List<? extends String>> SOUL_AFFIX_BLACKLIST;
    // 工具 - 附魔 - 灵魂修补
    private static final ForgeConfigSpec.BooleanValue DISABLE_SOUL_MENDING;
    private static final ForgeConfigSpec.ConfigValue<List<? extends String>> SOUL_MENDING_BLACKLIST;
    // 工具 - 附魔 - 溢魂弥躯
    private static final ForgeConfigSpec.BooleanValue DISABLE_SOUL_HEALING;
    private static final ForgeConfigSpec.ConfigValue<List<? extends String>> SOUL_HEALING_BLACKLIST;
    // 杂项 - 玩家模型
    private static final ForgeConfigSpec.ConfigValue<List<? extends String>> PLAYER_MODEL_SCALES;
    // 杂项 - 骷髅红眼
    private static final ForgeConfigSpec.BooleanValue SKELETON_RED_EYE_EFFECT_ENABLED;
    // 杂项 - 兼容
    private static final ForgeConfigSpec.BooleanValue ENABLE_GOETY_REVELATION_COMPATIBILITY;


    // ==================== 初始化块：分区 + 映射填充 ====================
    static {

        // ============================================================
        //                      大区域：食物
        // ============================================================
        BUILDER.push("food");
        BUILDER.comment("All food-related configurations / 所有食物相关的配置");

        // ---------- 北极刨冰 ----------
        BUILDER.push("polarice");
        BUILDER.comment("Polarice item settings / 北极刨冰设置");

        POLARICE_AFFECTS_BOSSES = BUILDER
                .comment("Whether bosses are affected by Polarice item\nBoss是否北极刨冰影响")
                .define("polariceAffectsBosses", false);
        POLARICE_HEALTH_THRESHOLD = BUILDER
                .comment("Maximum health threshold for entities to be affected by Polarice item (in half-hearts)\n实体受北极刨冰影响的最大生命值阈值（单位：半颗心）")
                .defineInRange("polariceHealthThreshold", 50.0, 1.0, Float.MAX_VALUE);
        POLARICE_COOLDOWN = BUILDER
                .comment("The cooldown for Polarice item to use\n北极刨冰的使用冷却时间（tick）")
                .defineInRange("polariceCooldown", 1800, 300, Integer.MAX_VALUE);
        POLARICE_COUNT = BUILDER
                .comment("The number of Polarice item can affect\n北极刨冰可以影响的实体数量")
                .defineInRange("polariceCount", 10, 1, Integer.MAX_VALUE);
        EXTRA_BANNED_ENTITIES = BUILDER
                .comment("Additional entity IDs that cannot be converted by Polarice item.\n北极刨冰无法转化的额外实体黑名单")
                .defineListAllowEmpty("extraBannedEntities", List.of(), Config::validateEntityName);

        BUILDER.pop();

        // ---------- 皇家蛋糕 ----------
        BUILDER.push("cake");
        CAKE_EFFECT_RADIUS = BUILDER
                .comment("Effect radius for the cake item\n皇家蛋糕的效果半径")
                .defineInRange("cakeEffectRadius", 32.0, 1.0, 256.0);
        BUILDER.pop();

        // ---------- 幻味草/果 ----------
        BUILDER.push("metamorphicScent");
        BUILDER.push("grass");
        METAMORPHIC_SCENT_GRASS_COPY_BLACKLIST = BUILDER
                .comment("A list of items that cannot be copied by Metamorphic Scent Grass\n幻味草无法复制的物品黑名单")
                .defineListAllowEmpty("copyBlacklist", List.of(
                        "goety_revelation:ascension_hard_candy",
                        "enigmaticdelicacy:abyssal_stew","goetydelight:pure_drink","goetydelight:tainted_drink",
                        "goetydelight:snap_unholy_tripe","goetydelight:lichs_chaos_stew","goetydelight:sundae_of_the_philosophers_potion",
                        "l2complements:totemic_apple","l2complements:enchanted_totemic_apple","hmag:insomnia_fruit",
                        "artifacts:everlasting_beef","artifacts:eternal_steak","born_in_chaos_v1:eternal_candy",
                        "avaritia_delight:infinity_apple","avaritia_delight:slice_of_endless_cake",
                        "avaritia_delight:infinity_taco","avaritia_delight:pasta_with_cosmic_meatballs",
                        "avaritia_delight:infinity_large_hamburger","minecraft:apple"
                ), Config::NoValidateItemName);
        METAMORPHIC_SCENT_GRASS_DURATION_MULTIPLIER = BUILDER
                .comment("Duration multiplier for Metamorphic Scent Grass effect (0.0 to 1.0)\n幻味草效果持续时间倍率")
                .defineInRange("durationMultiplier", 0.2, 0.0, 1.0);
        METAMORPHIC_SCENT_GRASS_AMPLIFIER_MULTIPLIER = BUILDER
                .comment("Amplifier multiplier for Metamorphic Scent Grass effect (0.0 to 1.0)\n幻味草效果等级倍率")
                .defineInRange("amplifierMultiplier", 0.3, 0.0, 1.0);
        METAMORPHIC_SCENT_GRASS_COPY_COUNT = BUILDER
                .comment("The maximum number of effects that can be copied by Metamorphic Scent Grass (0-64)\n幻味草可复制的最大效果数量")
                .defineInRange("copyCount", 1, 0, 64);
        BUILDER.pop();
        BUILDER.push("fruit");
        METAMORPHIC_SCENT_FRUIT_COPY_BLACKLIST = BUILDER
                .comment("A list of items that cannot be copied by Metamorphic Scent Fruit\n幻味果无法复制的物品黑名单")
                .defineListAllowEmpty("copyBlacklist", List.of(
                        "goety_revelation:ascension_hard_candy",
                        "enigmaticdelicacy:abyssal_stew","goetydelight:pure_drink","goetydelight:tainted_drink",
                        "goetydelight:snap_unholy_tripe","goetydelight:lichs_chaos_stew","goetydelight:sundae_of_the_philosophers_potion",
                        "l2complements:totemic_apple","l2complements:enchanted_totemic_apple","hmag:insomnia_fruit",
                        "artifacts:everlasting_beef","artifacts:eternal_steak","born_in_chaos_v1:eternal_candy",
                        "avaritia_delight:infinity_apple","avaritia_delight:slice_of_endless_cake",
                        "avaritia_delight:infinity_taco","avaritia_delight:pasta_with_cosmic_meatballs",
                        "avaritia_delight:infinity_large_hamburger","minecraft:apple"
                ), Config::NoValidateItemName);
        METAMORPHIC_SCENT_FRUIT_COPY_COUNT = BUILDER
                .comment("The maximum number of effects that can be copied by Metamorphic Scent Fruit (1-64)\n幻味果可复制的最大效果数量")
                .defineInRange("copyCount", 1, 1, 12);
        BUILDER.pop();
        BUILDER.pop();

        // ---------- 巫妖乱炖 ----------
        BUILDER.push("lichChaosStew");
        LICH_CHAOS_STEW_BOOST_PERCENTAGE = BUILDER
                .comment("Boost percentage per stack of Lich's Chaos Stew for minions (0.1 = 10%)\n巫妖乱炖每层为仆从提供的加成百分比")
                .defineInRange("boostPercentage", 0.1, 0.0, 1.0);
        LICH_STEW_MAX_COUNT = BUILDER
                .comment("Maximum stack count for Lich's Chaos Stew effect\n巫妖乱炖效果的最大叠加层数")
                .defineInRange("maxCount", 6, 1, 20);
        BUILDER.pop();

        // ---------- 暗夜之心豌豆汤 ----------
        BUILDER.push("nightHeartPeaSoup");
        NIGHT_HEART_PEA_SOUP_BOOST_PERCENTAGE = BUILDER
                .comment("Boost percentage per stack of Night Heart Pea Soup for minions (0.02 = 2%)\n暗夜之心豌豆汤每层为仆从提供的加成百分比")
                .defineInRange("boostPercentage", 0.02, 0.0, 1.0);
        NIGHT_PEA_SOUP_MAX_COUNT = BUILDER
                .comment("Maximum stack count for Night Heart Pea Soup effect\n暗夜之心豌豆汤效果的最大叠加层数")
                .defineInRange("maxCount", 12, 1, 30);
        BUILDER.pop();

        // ---------- 万毒盛宴 ----------
        BUILDER.push("tenThousandPoisonFeast");
        TEN_THOUSAND_POISON_FEAST_USE_WHITELIST = BUILDER
                .comment("If true, use whitelist mode; if false, use blacklist mode\ntrue=白名单模式，false=黑名单模式")
                .define("useWhitelist", true);
        TEN_THOUSAND_POISON_FEAST_EFFECT_LIST = BUILDER
                .comment("A list of debuff effects for whitelist/blacklist\n万毒盛宴的效果白名单/黑名单")
                .defineListAllowEmpty("effectList", List.of(
                        "minecraft:slowness", "minecraft:mining_fatigue", "minecraft:poison", "minecraft:wither",
                        "minecraft:blindness", "minecraft:nausea", "minecraft:darkness", "minecraft:weakness",
                        "minecraft:hunger", "minecraft:unluck", "minecraft:bad_omen", "minecraft:levitation",
                        "mod:goety","mod:twilightforest","mod:quark","mod:jerotes","mod:delight","mod:cataclysm",
                        "mod:enigmatic","mod:aether","mod:born_in_chaos_v1","mod:spell","mod:iron"
                ), entry -> {
                    if (!(entry instanceof String str)) return false;
                    if (str.startsWith("mod:")) return !str.substring(4).isEmpty();
                    return validateEffectName(str);
                });
        TEN_THOUSAND_POISON_FEAST_LEVEL_CONFIG = BUILDER
                .comment("Level range configuration for specific debuffs (format: effect_id=min-max)\n万毒宴特定效果等级范围配置")
                .defineListAllowEmpty("levelConfig", List.of(
                        "minecraft:slowness=0-4", "minecraft:weakness=0-3", "minecraft:wither=0-2",
                        "minecraft:blindness=0-1", "minecraft:nausea=0-1", "minecraft:hunger=0-3",
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
                    } catch (NumberFormatException e) { return false; }
                });
        TEN_THOUSAND_POISON_FEAST_DURATION_CONFIG = BUILDER
                .comment("Duration range configuration for specific debuffs (format: effect_id=min-max, unit: minutes)\n万毒宴特定效果时长范围配置")
                .defineListAllowEmpty("durationConfig", List.of(
                        "minecraft:slowness=0.5-3", "minecraft:weakness=0.5-3", "minecraft:wither=0.25-1.5",
                        "minecraft:blindness=0.25-0.5", "minecraft:nausea=0.25-0.5", "minecraft:hunger=0.5-2",
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
                    } catch (NumberFormatException e) { return false; }
                });
        TEN_THOUSAND_POISON_FEAST_DEFAULT_MIN_LEVEL = BUILDER
                .comment("Default minimum level for unconfigured debuffs\n未配置效果的默认最小等级")
                .defineInRange("defaultMinLevel", 0, 0, 255);
        TEN_THOUSAND_POISON_FEAST_DEFAULT_MAX_LEVEL = BUILDER
                .comment("Default maximum level for unconfigured debuffs\n未配置效果的默认最大等级")
                .defineInRange("defaultMaxLevel", 2, 0, 255);
        TEN_THOUSAND_POISON_FEAST_DEFAULT_MIN_DURATION = BUILDER
                .comment("Default minimum duration (minutes) for unconfigured debuffs\n未配置效果的默认最短持续时间")
                .defineInRange("defaultMinDuration", 0.1, 0.0, Double.MAX_VALUE);
        TEN_THOUSAND_POISON_FEAST_DEFAULT_MAX_DURATION = BUILDER
                .comment("Default maximum duration (minutes) for unconfigured debuffs\n未配置效果的默认最长持续时间")
                .defineInRange("defaultMaxDuration", 5.0, 0.0, Double.MAX_VALUE);
        TEN_THOUSAND_POISON_FEAST_EFFECT_COUNT = BUILDER
                .comment("Number of random debuffs to apply when eating Ten Thousand Poison Feast\n食用万毒盛宴时随机施加的debuff数量")
                .defineInRange("effectCount", 6, 1, 100);
        TEN_THOUSAND_POISON_FEAST_MIN_ITEM_COUNT = BUILDER
                .comment("Minimum number of items required in crafting grid\n合成万毒盛宴所需的最少物品数量")
                .defineInRange("minItemCount", 4, 1, 9);
        TEN_THOUSAND_POISON_FEAST_MIN_DEBUFF_COUNT = BUILDER
                .comment("Minimum number of unique debuff types required\n合成万毒盛宴所需的最少debuff种类数")
                .defineInRange("minDebuffCount", 8, 1, 100);
        BUILDER.pop();

        BUILDER.pop(); // 结束 food

        // ============================================================
        //                      大区域：工具
        // ============================================================
        BUILDER.push("tools");

        BUILDER.push("combat");
        SHIFT_SPEED_MULTIPLIER = BUILDER
                .comment("Movement speed multiplier when Shift key is pressed\n按下Shift键时的移动速度倍率")
                .defineInRange("shiftSpeedMultiplier", 2.0, 0.0, Double.MAX_VALUE);
        LIVING_HURT_DAMAGE_MULTIPLIER = BUILDER
                .comment("Normal damage multiplier (when not sneaking)\n正常的伤害增幅倍率")
                .defineInRange("livingHurtDamageMultiplier", 1.5, 0.0, Float.MAX_VALUE);
        LIVING_DAMAGE_GENERAL_MULTIPLIER = BUILDER
                .comment("Damage multiplier when sneaking but not backstabbing\n潜行非背刺的伤害增幅倍率")
                .defineInRange("livingDamageGeneralMultiplier", 1.5, 0.0, Float.MAX_VALUE);
        LIVING_DAMAGE_BACKSTAB_MULTIPLIER = BUILDER
                .comment("Damage multiplier when sneaking and backstabbing\n潜行背刺的伤害增幅倍率")
                .defineInRange("livingDamageBackstabMultiplier", 2.5, 0.0, Float.MAX_VALUE);
        BUILDER.pop();

        BUILDER.push("enchantments");
        BUILDER.push("soulAffix");
        DISABLE_SOUL_AFFIX = BUILDER
                .comment("Disable Soul Affix enchantment entirely\n完全禁用灵魂附加附魔")
                .define("disable", false);
        SOUL_AFFIX_DAMAGE_PER_LEVEL = BUILDER
                .comment("Damage increase per level of Soul Affix enchantment\n灵魂附加附魔每级增加的伤害值")
                .defineInRange("damagePerLevel", 0.4, 0.0, Double.MAX_VALUE);
        SOUL_AFFIX_SOUL_COST_PER_LEVEL = BUILDER
                .comment("Soul energy cost per level of Soul Affix enchantment\n灵魂附加附魔每级消耗的灵魂能量")
                .defineInRange("soulCostPerLevel", 5, 1, Integer.MAX_VALUE);
        SOUL_AFFIX_BLACKLIST = BUILDER
                .comment("A list of items that cannot be enchanted with Soul Affix\n无法附魔灵魂附加的物品列表")
                .defineListAllowEmpty("blacklist", List.of(), Config::NoValidateItemName);
        BUILDER.pop();
        BUILDER.push("soulMending");
        DISABLE_SOUL_MENDING = BUILDER
                .comment("Disable Soul Mending enchantment entirely\n完全禁用灵魂修补附魔")
                .define("disable", false);
        SOUL_MENDING_BLACKLIST = BUILDER
                .comment("A list of items that cannot be enchanted with Soul Mending\n无法附魔灵魂修补的物品列表")
                .defineListAllowEmpty("blacklist", List.of(), Config::NoValidateItemName);
        BUILDER.pop();
        BUILDER.push("soulHealing");
        DISABLE_SOUL_HEALING = BUILDER
                .comment("Disable Soul Healing enchantment entirely\n完全禁用溢魂弥躯附魔")
                .define("disable", false);
        SOUL_HEALING_BLACKLIST = BUILDER
                .comment("A list of items that cannot be enchanted with Soul Healing\n无法附魔溢魂弥躯的物品列表")
                .defineListAllowEmpty("blacklist", List.of(), Config::NoValidateItemName);
        BUILDER.pop();
        BUILDER.pop();

        BUILDER.pop(); // 结束 tools

        // ============================================================
        //                      大区域：杂项
        // ============================================================
        BUILDER.push("misc");

        BUILDER.push("items");
        BLACKLISTED_ITEMS = BUILDER
                .comment("A list of blacklisted items that will be hidden from creative tabs and prevent drops\n物品黑名单列表")
                .defineListAllowEmpty("blacklistedItems", List.of(
                        "goetydelight:roasted_corpse_maggots",
                        "goetydelight:corpse_maggot",
                        "goetydelight:rotten_corpse_maggot_feast",
                        "goetydelight:rotten_corpse_maggot_feast_block"
                ), Config::validateItemName);
        BUILDER.pop();

        BUILDER.push("playerModel");
        PLAYER_MODEL_SCALES = BUILDER
                .comment("Player model scale settings (format: playerName=scale)\n玩家模型缩放设置")
                .defineListAllowEmpty("playerModelScales", List.of(
                        "Steve=1.0", "Alex=1.0", "wu1wu2=1.0"
                ), Config::validatePlayerScaleEntry);
        BUILDER.pop();

        BUILDER.push("skeletonEye");
        SKELETON_RED_EYE_EFFECT_ENABLED = BUILDER
                .comment("Whether to enable the skeleton red-eye effect\n是否启用骷髅红眼特效")
                .define("enabled", false);
        BUILDER.pop();

        BUILDER.push("compat");
        ENABLE_GOETY_REVELATION_COMPATIBILITY = BUILDER
                .comment("Whether to enable compatibility with goety_revelation mod\n是否启用与goety_revelation模组的兼容性")
                .define("enableGoetyRevelationCompatibility", true);
        BUILDER.pop();

        BUILDER.pop(); // 结束 misc

        // ============================================================
        //  填充旧 key -> 新 ConfigValue / 新路径 映射
        // ============================================================
        registerLegacy("blacklistedItems", BLACKLISTED_ITEMS);
        registerLegacy("cakeEffectRadius", CAKE_EFFECT_RADIUS);
        registerLegacy("polariceAffectsBosses", POLARICE_AFFECTS_BOSSES);
        registerLegacy("polariceHealthThreshold", POLARICE_HEALTH_THRESHOLD);
        registerLegacy("polarice_cooldown", POLARICE_COOLDOWN);
        registerLegacy("polarice_count", POLARICE_COUNT);
        registerLegacy("extraBannedEntities", EXTRA_BANNED_ENTITIES);
        registerLegacy("MetamorphicScentGrassCopyBlacklist", METAMORPHIC_SCENT_GRASS_COPY_BLACKLIST);
        registerLegacy("metamorphicScentGrassDurationMultiplier", METAMORPHIC_SCENT_GRASS_DURATION_MULTIPLIER);
        registerLegacy("metamorphicScentGrassAmplifierMultiplier", METAMORPHIC_SCENT_GRASS_AMPLIFIER_MULTIPLIER);
        registerLegacy("metamorphicScentGrassCopyCount", METAMORPHIC_SCENT_GRASS_COPY_COUNT);
        registerLegacy("MetamorphicScentFruitCopyBlacklist", METAMORPHIC_SCENT_FRUIT_COPY_BLACKLIST);
        registerLegacy("metamorphicScentFruitCopyCount", METAMORPHIC_SCENT_FRUIT_COPY_COUNT);
        registerLegacy("shiftSpeedMultiplier", SHIFT_SPEED_MULTIPLIER);
        registerLegacy("livingHurtDamageMultiplier", LIVING_HURT_DAMAGE_MULTIPLIER);
        registerLegacy("livingDamageGeneralMultiplier", LIVING_DAMAGE_GENERAL_MULTIPLIER);
        registerLegacy("livingDamageBackstabMultiplier", LIVING_DAMAGE_BACKSTAB_MULTIPLIER);
        registerLegacy("soulAffixDamagePerLevel", SOUL_AFFIX_DAMAGE_PER_LEVEL);
        registerLegacy("soulAffixSoulCostPerLevel", SOUL_AFFIX_SOUL_COST_PER_LEVEL);
        registerLegacy("disableSoulMending", DISABLE_SOUL_MENDING);
        registerLegacy("disableSoulHealing", DISABLE_SOUL_HEALING);
        registerLegacy("disableSoulAffix", DISABLE_SOUL_AFFIX);
        registerLegacy("skeletonRedEyeEffectEnabled", SKELETON_RED_EYE_EFFECT_ENABLED);
        registerLegacy("soulRepairBlacklist", SOUL_MENDING_BLACKLIST);
        registerLegacy("soulHealBlacklist", SOUL_HEALING_BLACKLIST);
        registerLegacy("soulAffixBlacklist", SOUL_AFFIX_BLACKLIST);
        registerLegacy("lichChaosStewBoostPercentage", LICH_CHAOS_STEW_BOOST_PERCENTAGE);
        registerLegacy("lichStewMaxCount", LICH_STEW_MAX_COUNT);
        registerLegacy("nightHeartPeaSoupBoostPercentage", NIGHT_HEART_PEA_SOUP_BOOST_PERCENTAGE);
        registerLegacy("nightPeaSoupMaxCount", NIGHT_PEA_SOUP_MAX_COUNT);
        registerLegacy("tenThousandPoisonFeastUseWhitelist", TEN_THOUSAND_POISON_FEAST_USE_WHITELIST);
        registerLegacy("tenThousandPoisonFeastEffectList", TEN_THOUSAND_POISON_FEAST_EFFECT_LIST);
        registerLegacy("tenThousandPoisonFeastLevelConfig", TEN_THOUSAND_POISON_FEAST_LEVEL_CONFIG);
        registerLegacy("tenThousandPoisonFeastDurationConfig", TEN_THOUSAND_POISON_FEAST_DURATION_CONFIG);
        registerLegacy("tenThousandPoisonFeastDefaultMinLevel", TEN_THOUSAND_POISON_FEAST_DEFAULT_MIN_LEVEL);
        registerLegacy("tenThousandPoisonFeastDefaultMaxLevel", TEN_THOUSAND_POISON_FEAST_DEFAULT_MAX_LEVEL);
        registerLegacy("tenThousandPoisonFeastDefaultMinDuration", TEN_THOUSAND_POISON_FEAST_DEFAULT_MIN_DURATION);
        registerLegacy("tenThousandPoisonFeastDefaultMaxDuration", TEN_THOUSAND_POISON_FEAST_DEFAULT_MAX_DURATION);
        registerLegacy("tenThousandPoisonFeastEffectCount", TEN_THOUSAND_POISON_FEAST_EFFECT_COUNT);
        registerLegacy("tenThousandPoisonFeastMinItemCount", TEN_THOUSAND_POISON_FEAST_MIN_ITEM_COUNT);
        registerLegacy("tenThousandPoisonFeastMinDebuffCount", TEN_THOUSAND_POISON_FEAST_MIN_DEBUFF_COUNT);
        registerLegacy("playerModelScales", PLAYER_MODEL_SCALES);
        registerLegacy("enableGoetyRevelationCompatibility", ENABLE_GOETY_REVELATION_COMPATIBILITY);

        // 配置项注释 / 分组名已迁移到语言文件（见类头的键说明），此处不再硬编码
    }

    /**
     * 注册旧 key 与新 ConfigValue 的映射，同时记录新路径。
     */
    private static void registerLegacy(String oldKey, ForgeConfigSpec.ConfigValue<?> value) {
        LEGACY_KEY_MAP.put(oldKey, value);
        LEGACY_PATH_MAP.put(oldKey, String.join(".", value.getPath()));
    }


    // ==================== 迁移逻辑 ====================

    public static void captureLegacyConfig(Path configDir) {
        if (!LEGACY_CAPTURED.compareAndSet(false, true)) return;

        Path configPath = configDir.resolve("goetydelight-common.toml");
        if (!Files.exists(configPath)) {
            GoetyDelight.LOGGER.info("[Config] No legacy config file found, skip migration.");
            return;
        }

        try {
            Path backup = configDir.resolve("goetydelight-common.toml.bak");
            if (!Files.exists(backup)) {
                Files.copy(configPath, backup);
                GoetyDelight.LOGGER.info("[Config] Backed up legacy config to: {}", backup.getFileName());
            }

            CommentedFileConfig oldConfig = CommentedFileConfig.builder(configPath)
                    .preserveInsertionOrder()
                    .build();
            oldConfig.load();

            int migrated = 0;
            for (Map.Entry<String, String> entry : LEGACY_PATH_MAP.entrySet()) {
                String oldKey = entry.getKey();
                String newPath = entry.getValue();

                if (!oldConfig.contains(oldKey)) continue;

                Object oldValue = oldConfig.get(oldKey);
                if (oldValue == null) continue;

                if (oldConfig.contains(newPath)) {
                    oldConfig.remove(oldKey);
                    migrated++;
                    continue;
                }

                oldConfig.set(newPath, oldValue);
                oldConfig.remove(oldKey);
                migrated++;
            }

            if (migrated > 0) {
                oldConfig.save();
                GoetyDelight.LOGGER.info("[Config] Migrated {} legacy entries into new sections.", migrated);
            } else {
                GoetyDelight.LOGGER.info("[Config] No legacy entries needed migration.");
            }

            oldConfig.close();
        } catch (Exception e) {
            GoetyDelight.LOGGER.warn("[Config] Failed to migrate legacy config", e);
        }
    }

    // ==================== 事件处理 ====================

    @SubscribeEvent
    static void onLoad(final ModConfigEvent.Loading event) {
        if (!event.getConfig().getSpec().equals(SPEC)) return;
        refreshCaches();
    }

    @SubscribeEvent
    static void onReload(final ModConfigEvent.Reloading event) {
        if (!event.getConfig().getSpec().equals(SPEC)) return;
        refreshCaches();
    }

    private static void refreshCaches() {
        try {
            List<? extends String> raw = BLACKLISTED_ITEMS.get();
            if (raw == null) {
                blacklistedItems = Set.of();
            } else {
                blacklistedItems = raw.stream()
                        .map(itemName -> {
                            try {
                                return ForgeRegistries.ITEMS.getValue(new ResourceLocation(itemName));
                            } catch (Exception e) {
                                return null;
                            }
                        })
                        .filter(java.util.Objects::nonNull)
                        .collect(Collectors.toSet());
            }
        } catch (Exception e) {
            GoetyDelight.LOGGER.warn("[Config] Failed to refresh blacklistedItems cache", e);
            if (blacklistedItems == null) blacklistedItems = Set.of();
        }

        if (blackListUpdateListener != null) {
            try {
                blackListUpdateListener.accept(null);
            } catch (Exception e) {
                GoetyDelight.LOGGER.warn("[Config] blackListUpdateListener threw", e);
            }
        }

        try {
            ConvertServantUtil.onConfigLoad();
        } catch (Exception e) {
            GoetyDelight.LOGGER.warn("[Config] ConvertServantUtil.onConfigLoad threw", e);
        }
    }


    // ==================== 原有 getter（全部保留） ====================

    public static Set<Item> getSoulMendingBlacklist() {
        return SOUL_MENDING_BLACKLIST.get().stream()
                .map(itemName -> ForgeRegistries.ITEMS.getValue(new ResourceLocation(itemName)))
                .collect(Collectors.toSet());
    }

    public static Set<Item> getSoulHealingBlacklist() {
        return SOUL_HEALING_BLACKLIST.get().stream()
                .map(itemName -> ForgeRegistries.ITEMS.getValue(new ResourceLocation(itemName)))
                .collect(Collectors.toSet());
    }

    public static Set<Item> getSoulAffixBlacklist() {
        return SOUL_AFFIX_BLACKLIST.get().stream()
                .map(itemName -> ForgeRegistries.ITEMS.getValue(new ResourceLocation(itemName)))
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
                .map(itemName -> ForgeRegistries.ITEMS.getValue(new ResourceLocation(itemName)))
                .collect(Collectors.toSet());
    }

    public static Set<Item> getMetamorphicScentFruitCopyBlacklist() {
        return METAMORPHIC_SCENT_FRUIT_COPY_BLACKLIST.get().stream()
                .map(itemName -> ForgeRegistries.ITEMS.getValue(new ResourceLocation(itemName)))
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

    public static List<? extends String> getExtraBannedEntities() {
        return EXTRA_BANNED_ENTITIES.get();
    }

    public static double getCakeEffectRadius() {
        return CAKE_EFFECT_RADIUS.get();
    }

    public static double getShiftSpeedMultiplier() {
        return SHIFT_SPEED_MULTIPLIER.get();
    }

    public static double getLivingHurtDamageMultiplier() {
        return LIVING_HURT_DAMAGE_MULTIPLIER.get();
    }

    public static double getLivingDamageGeneralMultiplier() {
        return LIVING_DAMAGE_GENERAL_MULTIPLIER.get();
    }

    public static double getLivingDamageBackstabMultiplier() {
        return LIVING_DAMAGE_BACKSTAB_MULTIPLIER.get();
    }

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


    // ==================== 私有工具方法 ====================

    private static Map<ResourceLocation, int[]> parseEffectRangeConfig(List<? extends String> configList) {
        Map<ResourceLocation, int[]> result = new HashMap<>();
        for (String entry : configList) {
            String[] parts = entry.split("=");
            if (parts.length == 2) {
                String[] range = parts[1].split("-");
                if (range.length == 2) {
                    try {
                        ResourceLocation effectId = new ResourceLocation(parts[0]);
                        int min = Integer.parseInt(range[0]);
                        int max = Integer.parseInt(range[1]);
                        result.put(effectId, new int[]{min, max});
                    } catch (NumberFormatException ignored) {}
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
                        ResourceLocation effectId = new ResourceLocation(parts[0]);
                        double min = Double.parseDouble(range[0]);
                        double max = Double.parseDouble(range[1]);
                        result.put(effectId, new double[]{min, max});
                    } catch (NumberFormatException ignored) {}
                }
            }
        }
        return result;
    }

    private static boolean validateEffectName(final Object obj) {
        return obj instanceof final String effectName &&
                ForgeRegistries.MOB_EFFECTS.containsKey(new ResourceLocation(effectName));
    }

    private static boolean validateEntityName(final Object obj) {
        return obj instanceof final String entityName &&
                ForgeRegistries.ENTITY_TYPES.containsKey(new ResourceLocation(entityName));
    }

    private static boolean validateItemName(final Object obj) {
        return obj instanceof final String itemName &&
                ForgeRegistries.ITEMS.containsKey(new ResourceLocation(itemName));
    }

    private static boolean NoValidateItemName(final Object obj) {
        return true;
    }

    private static boolean validatePlayerScaleEntry(final Object obj) {
        if (!(obj instanceof String entry)) return false;
        String[] parts = entry.split("=");
        if (parts.length != 2) return false;
        try {
            return Float.parseFloat(parts[1].trim()) > 0;
        } catch (NumberFormatException e) {
            return false;
        }
    }


    // ==================== 静态常量 ====================

    public static final ForgeConfigSpec SPEC = BUILDER.build();

    public static Set<Item> blacklistedItems = Set.of();
    private static Consumer<Void> blackListUpdateListener;

    public static void registerBlackListUpdateListener(Consumer<Void> listener) {
        blackListUpdateListener = listener;
    }
}