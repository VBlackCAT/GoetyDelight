package net.v_black_cat.goetydelight.item.food;

import com.Polarice3.Goety.common.effects.GoetyEffects;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.food.FoodProperties;
import net.v_black_cat.goetydelight.init.ModEffects;

import static net.v_black_cat.goetydelight.util.TickConverterUtil.minToTick;

public class ModFoods {
    // 饮品
    public static final FoodProperties TAINTED_DRINK;
    public static final FoodProperties PURE_DRINK;
    public static final FoodProperties BOILING_BLOOD_BREW;
    public static final FoodProperties SPIDER_EGG_BUBBLE_TEA;
    public static final FoodProperties SKULL_SHOT;
    public static final FoodProperties GRAPE_SLUSH;
    public static final FoodProperties RAKI;
    public static final FoodProperties RUBY_SYRUP;
    public static final FoodProperties OMINOUS_RAMUNE;
    public static final FoodProperties LIQUID_VOID_TEA_DRINK;

    // 普通食物
    public static final FoodProperties CANDY_FISH;
    public static final FoodProperties WHITE_SHARK_SUGAR_PACK;
    public static final FoodProperties WHITE_SHARK_CANDY;
    public static final FoodProperties SIBLING_SUNDAE;
    public static final FoodProperties ROASTED_CORPSE_MAGGOTS;
    public static final FoodProperties CORPSE_MAGGOT;
    public static final FoodProperties CRYING_SHARK_SUGAR_PACK;
    public static final FoodProperties SUNSHINE_SUGAR_BUN;
    public static final FoodProperties SEVEN_LEAF_PUDDING;
    public static final FoodProperties BEAR_PAW;
    public static final FoodProperties CAKE;
    public static final FoodProperties OMINOUS_ICE_CREAM;
    public static final FoodProperties ECTOPLASMIC_MELON;
    public static final FoodProperties BLUE_ECTOPLASMIC_SUNDAE;
    public static final FoodProperties NIGHT_HEART_PEA_SOUP;
    public static final FoodProperties POACHED_SPIDER_EGG;
    public static final FoodProperties GRILL_FROG_LEG;
    public static final FoodProperties FRENZIED_FUNGUS_POP_ROCKS;
    public static final FoodProperties SOUL_CONVERGENCE_ROOM;
    public static final FoodProperties SOUL_CONVERGENCE_ROOM_2;
    public static final FoodProperties BONE_LORD_ASH_RICE;
    public static final FoodProperties RUBY_HARD_CANDY;
    public static final FoodProperties CRISP_BISCUIT;
    public static final FoodProperties ROTTEN_CORPSE_MAGGOT_FEAST;
    public static final FoodProperties CHERRY_BLOSSOM_CAKE;
    public static final FoodProperties NETHER_WART_OMELETTE;
    public static final FoodProperties WARPED_WART_OMELETTE;
    public static final FoodProperties FULL_SPIDER_FEAST;
    public static final FoodProperties LICHS_CHAOS_STEW;
    public static final FoodProperties MAGIC_QUARTZ_COOKIE;
    public static final FoodProperties SNAP_UNHOLY_TRIPE;
    public static final FoodProperties SUNDAE_OF_THE_PHILOSOPHERS_POTION;
    public static final FoodProperties THE_BOX_OF_THE_DEAD;
    public static final FoodProperties RING_PACKED_VOID_GEL_JELLY;
    public static final FoodProperties STUFFED_TALL_SKULL_RICE;
    public static final FoodProperties BOAT_STUFFED_ROASTED_WARDEN_HEAD;
    public static final FoodProperties BOAT_STUFFED_ROASTED_WARDEN_MEET;
    public static final FoodProperties BOAT_STUFFED_ROASTED_WARDEN_FLANK;
    public static final FoodProperties ANCIENT_ENCHANTED_GOLDEN_APPLE;
    public static final FoodProperties ROAST_LAOWANG;
    public static final FoodProperties POLARICE;
    public static final FoodProperties FORBIDDDEN_SOUP_BUN;
    public static final FoodProperties HIDDEN_PANCAKE;
    public static final FoodProperties CREAMY_BERRY_FISH_PASTE_DUMPLING_WITH_CHOCOLATE_SAUCE;
    public static final FoodProperties OBSIDIAN_THICK_SOUP;
    public static final FoodProperties SHAWARMA;
    public static final FoodProperties MENEMEN_WITH_BREAD;
    public static final FoodProperties BAKLAVA;
    public static final FoodProperties BISCAT;
    public static final FoodProperties ASCENSION_MOONCAKE;
    public static final FoodProperties QUICK_GROWING_SEED_POPCORN;
    public static final FoodProperties ECTOPLASM_JELLY;
    public static final FoodProperties FROG_LEG_SANDWICH;
    public static final FoodProperties SPIDER_EGG_BUBBLE_TEA_2;
    public static final FoodProperties PROMOTION_HARD_CANDY;
    public static final FoodProperties TOXIC_MEAL;
    public static final FoodProperties POACHED_NETHER_WART_EGG;
    public static final FoodProperties REJECTED_DARK_MEAT_SOUP;
    public static final FoodProperties CUP;
    public static final FoodProperties NETHER_STYLE_FRIED_EGG_SANDWICH;
    public static final FoodProperties EXOTIC_BREAKFAST;
    public static final FoodProperties VILLAGERS_FEAST;
    public static final FoodProperties JUNGLE_SALAD;
    public static final FoodProperties SAUCE_GRILLED_CANDY_FISH;
    public static final FoodProperties METAMORPHIC_SCENT_FRUIT;
    public static final FoodProperties METAMORPHIC_SCENT_GRASS;
    public static final FoodProperties TEN_THOUSAND_POISON_FEAST;

    static {
        // ========== 饮品 ==========
        TAINTED_DRINK = new FoodProperties.Builder()
                .nutrition(4).saturationModifier(0.25F).alwaysEdible()
                .effect(() -> new MobEffectInstance(MobEffects.REGENERATION, 150, 1), 1.0F)
                .effect(() -> new MobEffectInstance(MobEffects.DAMAGE_RESISTANCE, 300, 1), 1.0F)
                .effect(() -> new MobEffectInstance(ModEffects.THE_PALE_MESSENGER, 3600, 0), 1.0F)
                .effect(() -> new MobEffectInstance(ModEffects.ZOMBIFIED_PIGLIN_BRUTE_SERVANT_SUPPORT, 7200, 0), 1.0F)
                .build();

        // PURE_DRINK 单独定义，营养 4，期望总饱和度 8 → 修正值 = 8/(4×2) = 1.0F
        PURE_DRINK = new FoodProperties.Builder()
                .nutrition(4).saturationModifier(1.0F).alwaysEdible()
                .effect(() -> new MobEffectInstance(MobEffects.REGENERATION, 600, 3), 1.0F)
                .effect(() -> new MobEffectInstance(MobEffects.DAMAGE_RESISTANCE, 1200, 3), 1.0F)
                .effect(() -> new MobEffectInstance(ModEffects.THE_PALE_MESSENGER, minToTick(15), 0), 1.0F)
                .effect(() -> new MobEffectInstance(ModEffects.ZOMBIFIED_PIGLIN_BRUTE_SERVANT_SUPPORT, minToTick(30), 0), 1.0F)
                .build();

        BOILING_BLOOD_BREW = new FoodProperties.Builder()
                .nutrition(6).saturationModifier(0.6667F).alwaysEdible()
                .effect(() -> new MobEffectInstance(GoetyEffects.FIERY_AURA, minToTick(5), 0), 1.0F)
                .effect(() -> new MobEffectInstance(vectorwing.farmersdelight.common.registry.ModEffects.COMFORT, minToTick(5), 0), 1.0F)
                .build();

        SPIDER_EGG_BUBBLE_TEA = new FoodProperties.Builder()
                .nutrition(6).saturationModifier(0.6667F).alwaysEdible()
                .effect(() -> new MobEffectInstance(GoetyEffects.CLIMBING, minToTick(7), 0), 1.0F)
                .build();

        SKULL_SHOT = new FoodProperties.Builder()
                .nutrition(6).saturationModifier(0.6667F).alwaysEdible()
                .effect(() -> new MobEffectInstance(GoetyEffects.CORPSE_EATER, 1200, 0), 1.0F)
                .build();

        GRAPE_SLUSH = new FoodProperties.Builder()
                .nutrition(9).saturationModifier(0.6667F).alwaysEdible()
                .effect(() -> new MobEffectInstance(GoetyEffects.CHILL_HIDE, 4200, 1), 1.0F)
                .effect(() -> new MobEffectInstance(GoetyEffects.FROSTY_AURA, 600, 1), 1.0F)
                .effect(() -> new MobEffectInstance(vectorwing.farmersdelight.common.registry.ModEffects.NOURISHMENT, minToTick(10), 0), 1.0F)
                .build();

        RAKI = new FoodProperties.Builder()
                .nutrition(2).saturationModifier(0.5F).alwaysEdible()
                .effect(() -> new MobEffectInstance(MobEffects.MOVEMENT_SPEED, 600, 1), 1.0F)
                .effect(() -> new MobEffectInstance(ModEffects.TINGLING, 600, 0), 1.0F)
                .build();

        RUBY_SYRUP = new FoodProperties.Builder()
                .nutrition(5).saturationModifier(0.8F).alwaysEdible()
                .effect(() -> new MobEffectInstance(ModEffects.SPELL_MASTERY, minToTick(1), 0), 1.0F)
                .effect(() -> new MobEffectInstance(ModEffects.SPELL_DURATION, minToTick(1), 0), 1.0F)
                .effect(() -> new MobEffectInstance(vectorwing.farmersdelight.common.registry.ModEffects.NOURISHMENT, minToTick(1), 0), 1.0F)
                .build();

        OMINOUS_RAMUNE = new FoodProperties.Builder()
                .nutrition(2).saturationModifier(0.5F).alwaysEdible()
                .effect(() -> new MobEffectInstance(ModEffects.TINGLING, 1200, 0), 1.0F)
                .build();

        LIQUID_VOID_TEA_DRINK = new FoodProperties.Builder()
                .nutrition(0).alwaysEdible()
                .build();

        // ========== 普通食物 ==========
        CANDY_FISH = new FoodProperties.Builder()
                .nutrition(6).saturationModifier(0.6667F).alwaysEdible()
                .effect(() -> new MobEffectInstance(vectorwing.farmersdelight.common.registry.ModEffects.NOURISHMENT, minToTick(7), 0), 1.0F)
                .effect(() -> new MobEffectInstance(MobEffects.WATER_BREATHING, minToTick(5), 0), 1.0F)
                .build();

        WHITE_SHARK_SUGAR_PACK = new FoodProperties.Builder()
                .nutrition(6).saturationModifier(0.6667F).alwaysEdible()
                .effect(() -> new MobEffectInstance(vectorwing.farmersdelight.common.registry.ModEffects.NOURISHMENT, minToTick(4), 0), 1.0F)
                .build();

        WHITE_SHARK_CANDY = new FoodProperties.Builder()
                .nutrition(8).saturationModifier(0.625F).alwaysEdible()
                .effect(() -> new MobEffectInstance(MobEffects.REGENERATION, minToTick(1), 1), 1.0F)
                .effect(() -> new MobEffectInstance(vectorwing.farmersdelight.common.registry.ModEffects.NOURISHMENT, minToTick(10), 0), 1.0F)
                .build();

        SIBLING_SUNDAE = new FoodProperties.Builder()
                .nutrition(6).saturationModifier(0.8333F).alwaysEdible()
                .effect(() -> new MobEffectInstance(GoetyEffects.INSIGHT, minToTick(2.5F), 3), 1.0F)
                .build();

        ROASTED_CORPSE_MAGGOTS = new FoodProperties.Builder()
                .nutrition(5).saturationModifier(0.4F).alwaysEdible()
                .build();

        CORPSE_MAGGOT = new FoodProperties.Builder()
                .nutrition(3).saturationModifier(0.3333F).alwaysEdible()
                .effect(() -> new MobEffectInstance(MobEffects.CONFUSION, 200, 0), 1.0F)
                .build();

        CRYING_SHARK_SUGAR_PACK = new FoodProperties.Builder()
                .nutrition(7).saturationModifier(0.5714F).alwaysEdible()
                .effect(() -> new MobEffectInstance(ModEffects.HYDRATION, minToTick(15), 1), 1.0F)
                .effect(() -> new MobEffectInstance(vectorwing.farmersdelight.common.registry.ModEffects.NOURISHMENT, minToTick(5), 0), 1.0F)
                .build();

        SUNSHINE_SUGAR_BUN = new FoodProperties.Builder()
                .nutrition(7).saturationModifier(0.5714F).alwaysEdible()
                .effect(() -> new MobEffectInstance(GoetyEffects.PHOTOSYNTHESIS, minToTick(15), 1), 1.0F)
                .effect(() -> new MobEffectInstance(vectorwing.farmersdelight.common.registry.ModEffects.NOURISHMENT, minToTick(5), 0), 1.0F)
                .build();

        SEVEN_LEAF_PUDDING = new FoodProperties.Builder()
                .nutrition(7).saturationModifier(0.7143F).alwaysEdible()
                .effect(() -> new MobEffectInstance(MobEffects.REGENERATION, minToTick(5), 1), 1.0F)
                .effect(() -> new MobEffectInstance(vectorwing.farmersdelight.common.registry.ModEffects.NOURISHMENT, minToTick(5), 0), 1.0F)
                .build();

        BEAR_PAW = new FoodProperties.Builder()
                .nutrition(6).saturationModifier(0.8333F).alwaysEdible()
                .effect(() -> new MobEffectInstance(vectorwing.farmersdelight.common.registry.ModEffects.NOURISHMENT, 6000, 0), 1.0F)
                .effect(() -> new MobEffectInstance(GoetyEffects.RAMPAGE, 2400, 0), 1.0F)
                .build();

        CAKE = new FoodProperties.Builder()
                .nutrition(6).saturationModifier(0.5F).alwaysEdible()
                .effect(() -> new MobEffectInstance(vectorwing.farmersdelight.common.registry.ModEffects.NOURISHMENT, 4500, 0), 1.0F)
                .effect(() -> new MobEffectInstance(GoetyEffects.FORTUNATE, 1500, 2), 1.0F)
                .effect(() -> new MobEffectInstance(GoetyEffects.BOTTLING, 1500, 2), 1.0F)
                .effect(() -> new MobEffectInstance(MobEffects.DAMAGE_RESISTANCE, 1500, 1), 1.0F)
                .build();

        OMINOUS_ICE_CREAM = new FoodProperties.Builder()
                .nutrition(8).saturationModifier(0.625F).alwaysEdible()
                .effect(() -> new MobEffectInstance(MobEffects.BAD_OMEN, 6000, 4), 1.0F)
                .effect(() -> new MobEffectInstance(GoetyEffects.CHILL_HIDE, 1200, 0), 1.0F)
                .build();

        ECTOPLASMIC_MELON = new FoodProperties.Builder()
                .nutrition(3).saturationModifier(0.3333F).alwaysEdible()
                .effect(() -> new MobEffectInstance(GoetyEffects.CHILL_HIDE, 100, 0), 1.0F)
                .build();

        BLUE_ECTOPLASMIC_SUNDAE = new FoodProperties.Builder()
                .nutrition(10).saturationModifier(0.6F).alwaysEdible()
                .effect(() -> new MobEffectInstance(GoetyEffects.FORTUNATE, 12000, 0), 1.0F)
                .effect(() -> new MobEffectInstance(MobEffects.REGENERATION, 2400, 0), 1.0F)
                .effect(() -> new MobEffectInstance(GoetyEffects.CHILL_HIDE, 2400, 0), 1.0F)
                .build();

        NIGHT_HEART_PEA_SOUP = new FoodProperties.Builder()
                .nutrition(7).saturationModifier(0.4286F).alwaysEdible()
                .effect(() -> new MobEffectInstance(MobEffects.DAMAGE_BOOST, minToTick(5), 2), 1.0F)
                .effect(() -> new MobEffectInstance(GoetyEffects.SHADOW_WALK, 1200, 2), 1.0F)
                .effect(() -> new MobEffectInstance(MobEffects.DAMAGE_RESISTANCE, 6000, 2), 1.0F)
                .effect(() -> new MobEffectInstance(vectorwing.farmersdelight.common.registry.ModEffects.NOURISHMENT, 12000, 0), 1.0F)
                .effect(() -> new MobEffectInstance(vectorwing.farmersdelight.common.registry.ModEffects.COMFORT, 12000, 0), 1.0F)
                .effect(() -> new MobEffectInstance(ModEffects.SERVANT_REINFORCEMENT, minToTick(5), 0), 1.0F)
                .build();

        POACHED_SPIDER_EGG = new FoodProperties.Builder()
                .nutrition(5).saturationModifier(0.4F).alwaysEdible().fast()
                .effect(() -> new MobEffectInstance(GoetyEffects.CLIMBING, 200, 0), 1.0F)
                .build();

        GRILL_FROG_LEG = new FoodProperties.Builder()
                .nutrition(10).saturationModifier(0.6F).alwaysEdible()
                .effect(() -> new MobEffectInstance(GoetyEffects.FROG_LEG, 1200, 0), 1.0F)
                .effect(() -> new MobEffectInstance(MobEffects.REGENERATION, 400, 1), 1.0F)
                .build();

        FRENZIED_FUNGUS_POP_ROCKS = new FoodProperties.Builder()
                .nutrition(6).saturationModifier(0.6667F).alwaysEdible()
                .build();

        SOUL_CONVERGENCE_ROOM = new FoodProperties.Builder()
                .nutrition(8).saturationModifier(1.5F).alwaysEdible()
                .effect(() -> new MobEffectInstance(MobEffects.HUNGER, 200, 0), 1.0F)
                .effect(() -> new MobEffectInstance(GoetyEffects.SOUL_ARMOR, 1200, 1), 1.0F)
                .build();

        SOUL_CONVERGENCE_ROOM_2 = new FoodProperties.Builder()
                .nutrition(20).saturationModifier(1.5F).alwaysEdible()
                .effect(() -> new MobEffectInstance(GoetyEffects.SOUL_ARMOR, 6000, 4), 1.0F)
                .effect(() -> new MobEffectInstance(MobEffects.HEALTH_BOOST, 6000, 2), 1.0F)
                .effect(() -> new MobEffectInstance(ModEffects.HUNTING_DENIAL, minToTick(10), 0), 1.0F)
                .build();

        BONE_LORD_ASH_RICE = new FoodProperties.Builder()
                .nutrition(6).saturationModifier(0.6667F).alwaysEdible()
                .effect(() -> new MobEffectInstance(GoetyEffects.CHILL_HIDE, 6000, 1), 1.0F)
                .effect(() -> new MobEffectInstance(GoetyEffects.BUFF, 6000, 2), 1.0F)
                .effect(() -> new MobEffectInstance(vectorwing.farmersdelight.common.registry.ModEffects.NOURISHMENT, minToTick(5), 0), 1.0F)
                .effect(() -> new MobEffectInstance(GoetyEffects.CORPSE_EATER, minToTick(1), 2), 1.0F)
                .build();

        RUBY_HARD_CANDY = new FoodProperties.Builder()
                .nutrition(10).saturationModifier(0.8F).alwaysEdible()
                .effect(() -> new MobEffectInstance(ModEffects.SPELL_MASTERY, minToTick(10), 2), 1.0F)
                .effect(() -> new MobEffectInstance(ModEffects.SPELL_DURATION, minToTick(10), 2), 1.0F)
                .effect(() -> new MobEffectInstance(MobEffects.REGENERATION, minToTick(8), 1), 1.0F)
                .effect(() -> new MobEffectInstance(vectorwing.farmersdelight.common.registry.ModEffects.NOURISHMENT, minToTick(15), 0), 1.0F)
                .build();

        CRISP_BISCUIT = new FoodProperties.Builder()
                .nutrition(9).saturationModifier(0.6667F).alwaysEdible()
                .effect(() -> new MobEffectInstance(vectorwing.farmersdelight.common.registry.ModEffects.NOURISHMENT, minToTick(5), 2), 1.0F)
                .effect(() -> new MobEffectInstance(ModEffects.SPELL_MASTERY, minToTick(2), 0), 1.0F)
                .build();

        ROTTEN_CORPSE_MAGGOT_FEAST = new FoodProperties.Builder()
                .nutrition(8).saturationModifier(0.625F).alwaysEdible()
                .effect(() -> new MobEffectInstance(vectorwing.farmersdelight.common.registry.ModEffects.NOURISHMENT, minToTick(2), 0), 1.0F)
                .build();

        CHERRY_BLOSSOM_CAKE = new FoodProperties.Builder()
                .nutrition(12).saturationModifier(0.6667F).alwaysEdible()
                .effect(() -> new MobEffectInstance(vectorwing.farmersdelight.common.registry.ModEffects.NOURISHMENT, minToTick(10), 0), 1.0F)
                .effect(() -> new MobEffectInstance(MobEffects.LUCK, minToTick(2), 2), 1.0F)
                .build();

        NETHER_WART_OMELETTE = new FoodProperties.Builder()
                .nutrition(6).saturationModifier(0.3333F).alwaysEdible()
                .build();

        WARPED_WART_OMELETTE = new FoodProperties.Builder()
                .nutrition(6).saturationModifier(0.3333F).alwaysEdible()
                .build();

        FULL_SPIDER_FEAST = new FoodProperties.Builder()
                .nutrition(8).saturationModifier(0.625F).alwaysEdible()
                .effect(() -> new MobEffectInstance(vectorwing.farmersdelight.common.registry.ModEffects.NOURISHMENT, minToTick(5), 0), 1.0F)
                .effect(() -> new MobEffectInstance(GoetyEffects.CLIMBING, minToTick(5), 0), 1.0F)
                .build();

        LICHS_CHAOS_STEW = new FoodProperties.Builder()
                .nutrition(16).saturationModifier(0.75F).alwaysEdible()
                .effect(() -> new MobEffectInstance(GoetyEffects.SAVE_EFFECTS, -1, 2), 1.0F)
                .effect(() -> new MobEffectInstance(ModEffects.WIGHT_DENIAL, minToTick(30), 0, false, false), 1.0F)
                .effect(() -> new MobEffectInstance(MobEffects.FIRE_RESISTANCE, -1, 0), 1.0F)
                .effect(() -> new MobEffectInstance(MobEffects.REGENERATION, minToTick(30), 2), 1.0F)
                .build();

        MAGIC_QUARTZ_COOKIE = new FoodProperties.Builder()
                .nutrition(8).saturationModifier(0.5F).alwaysEdible()
                .build();

        SNAP_UNHOLY_TRIPE = new FoodProperties.Builder()
                .nutrition(18).saturationModifier(1.1111F).alwaysEdible()
                .effect(() -> new MobEffectInstance(vectorwing.farmersdelight.common.registry.ModEffects.NOURISHMENT, minToTick(30), 0), 1.0F)
                .effect(() -> new MobEffectInstance(MobEffects.SATURATION, 200, 0), 1.0F)
                .effect(() -> new MobEffectInstance(ModEffects.THE_PALE_MESSENGER, minToTick(5), 0), 1.0F)
                .effect(() -> new MobEffectInstance(MobEffects.FIRE_RESISTANCE, minToTick(30), 0), 1.0F)
                .build();

        SUNDAE_OF_THE_PHILOSOPHERS_POTION = new FoodProperties.Builder()
                .nutrition(10).saturationModifier(0.6F).alwaysEdible()
                .effect(() -> new MobEffectInstance(GoetyEffects.SAVE_EFFECTS, -1, 1, false, false), 1.0F)
                .effect(() -> new MobEffectInstance(GoetyEffects.GOLD_TOUCHED, minToTick(30), 0, false, false), 1.0F)
                .effect(() -> new MobEffectInstance(GoetyEffects.SOUL_ARMOR, -1, 1, false, false), 1.0F)
                .build();

        THE_BOX_OF_THE_DEAD = new FoodProperties.Builder()
                .nutrition(6).saturationModifier(0.5F).alwaysEdible()
                .effect(() -> new MobEffectInstance(GoetyEffects.CURSED, 400, 1), 1.0F)
                .build();

        RING_PACKED_VOID_GEL_JELLY = new FoodProperties.Builder()
                .nutrition(8).saturationModifier(0.5F).alwaysEdible()
                .effect(() -> new MobEffectInstance(ModEffects.VOID_AFFIX, 1200, 0), 1.0F)
                .effect(() -> new MobEffectInstance(vectorwing.farmersdelight.common.registry.ModEffects.NOURISHMENT, 6000, 0), 1.0F)
                .effect(() -> new MobEffectInstance(MobEffects.HUNGER, 1200, 4), 1.0F)
                .build();

        STUFFED_TALL_SKULL_RICE = new FoodProperties.Builder()
                .nutrition(8).saturationModifier(0.625F).alwaysEdible()
                .effect(() -> new MobEffectInstance(vectorwing.farmersdelight.common.registry.ModEffects.NOURISHMENT, 1200, 0), 1.0F)
                .effect(() -> new MobEffectInstance(GoetyEffects.PHOTOSYNTHESIS, minToTick(5), 1), 1.0F)
                .build();

        BOAT_STUFFED_ROASTED_WARDEN_HEAD = new FoodProperties.Builder()
                .nutrition(25).saturationModifier(0.8F).alwaysEdible()
                .effect(() -> new MobEffectInstance(GoetyEffects.SOUL_ARMOR, minToTick(5), 3), 1.0F)
                .effect(() -> new MobEffectInstance(ModEffects.HUNTING_DENIAL, minToTick(10), 0), 1.0F)
                .effect(() -> new MobEffectInstance(MobEffects.REGENERATION, minToTick(5), 1), 1.0F)
                .effect(() -> new MobEffectInstance(MobEffects.DAMAGE_BOOST, minToTick(5), 1), 1.0F)
                .build();

        BOAT_STUFFED_ROASTED_WARDEN_MEET = new FoodProperties.Builder()
                .nutrition(30).saturationModifier(0.8333F).alwaysEdible()
                .effect(() -> new MobEffectInstance(GoetyEffects.SOUL_ARMOR, minToTick(5), 3), 1.0F)
                .effect(() -> new MobEffectInstance(ModEffects.HUNTING_DENIAL, minToTick(10), 0), 1.0F)
                .effect(() -> new MobEffectInstance(MobEffects.REGENERATION, minToTick(5), 1), 1.0F)
                .effect(() -> new MobEffectInstance(MobEffects.DAMAGE_BOOST, minToTick(5), 1), 1.0F)
                .build();

        BOAT_STUFFED_ROASTED_WARDEN_FLANK = new FoodProperties.Builder()
                .nutrition(40).saturationModifier(0.75F).alwaysEdible()
                .effect(() -> new MobEffectInstance(GoetyEffects.SOUL_ARMOR, minToTick(5), 3), 1.0F)
                .effect(() -> new MobEffectInstance(ModEffects.HUNTING_DENIAL, minToTick(15), 0), 1.0F)
                .effect(() -> new MobEffectInstance(MobEffects.REGENERATION, minToTick(7.5f), 1), 1.0F)
                .effect(() -> new MobEffectInstance(MobEffects.DAMAGE_BOOST, minToTick(7.5f), 1), 1.0F)
                .build();

        ANCIENT_ENCHANTED_GOLDEN_APPLE = new FoodProperties.Builder()
                .nutrition(6).saturationModifier(1.0F).alwaysEdible()
                .effect(() -> new MobEffectInstance(MobEffects.DAMAGE_RESISTANCE, minToTick(5), 0), 1.0F)
                .effect(() -> new MobEffectInstance(MobEffects.ABSORPTION, minToTick(2), 3), 1.0F)
                .effect(() -> new MobEffectInstance(MobEffects.REGENERATION, 600, 5), 1.0F)
                .effect(() -> new MobEffectInstance(MobEffects.FIRE_RESISTANCE, minToTick(5), 0), 1.0F)
                .build();

        ROAST_LAOWANG = new FoodProperties.Builder()
                .nutrition(20).saturationModifier(0.75F).alwaysEdible()
                .effect(() -> new MobEffectInstance(vectorwing.farmersdelight.common.registry.ModEffects.NOURISHMENT, minToTick(20), 0), 1.0F)
                .build();

        POLARICE = new FoodProperties.Builder()
                .nutrition(10).saturationModifier(0.4F).alwaysEdible()
                .effect(() -> new MobEffectInstance(GoetyEffects.ILLAGUE, minToTick(5), 0), 1.0F)
                .effect(() -> new MobEffectInstance(MobEffects.DIG_SPEED, minToTick(5), 2), 1.0F)
                .build();

        FORBIDDDEN_SOUP_BUN = new FoodProperties.Builder()
                .nutrition(13).saturationModifier(0.3846F).alwaysEdible()
                .effect(() -> new MobEffectInstance(GoetyEffects.NYCTOPHOBIA, 600, 0), 0.3F)
                .effect(() -> new MobEffectInstance(GoetyEffects.SENSE_LOSS, 600, 0), 0.7F)
                .build();

        HIDDEN_PANCAKE = new FoodProperties.Builder()
                .nutrition(15).saturationModifier(0.4667F).alwaysEdible()
                .effect(() -> new MobEffectInstance(GoetyEffects.IRON_HIDE, minToTick(1), 4), 1.0F)
                .effect(() -> new MobEffectInstance(MobEffects.DAMAGE_BOOST, minToTick(2), 1), 1.0F)
                .build();

        CREAMY_BERRY_FISH_PASTE_DUMPLING_WITH_CHOCOLATE_SAUCE = new FoodProperties.Builder()
                .nutrition(20).saturationModifier(0.5F).alwaysEdible()
                .effect(() -> new MobEffectInstance(MobEffects.WITHER, minToTick(1), 4), 1.0F)
                .effect(() -> new MobEffectInstance(MobEffects.CONFUSION, minToTick(2), 9), 1.0F)
                .effect(() -> new MobEffectInstance(GoetyEffects.SENSE_LOSS, minToTick(1), 0), 1.0F)
                .build();

        OBSIDIAN_THICK_SOUP = new FoodProperties.Builder()
                .nutrition(6).saturationModifier(0.5F).alwaysEdible()
                .effect(() -> new MobEffectInstance(vectorwing.farmersdelight.common.registry.ModEffects.NOURISHMENT, minToTick(5), 0), 1.0F)
                .effect(() -> new MobEffectInstance(GoetyEffects.FIERY_AURA, 1320, 0), 1.0F)
                .effect(() -> new MobEffectInstance(GoetyEffects.FLAME_HANDS, 1320, 0), 1.0F)
                .effect(() -> new MobEffectInstance(ModEffects.THE_PALE_MESSENGER, 120, 0), 1.0F)
                .build();

        SHAWARMA = new FoodProperties.Builder()
                .nutrition(16).saturationModifier(0.78125F).alwaysEdible()
                .effect(() -> new MobEffectInstance(vectorwing.farmersdelight.common.registry.ModEffects.NOURISHMENT, minToTick(10), 0), 1.0F)
                .effect(() -> new MobEffectInstance(MobEffects.DAMAGE_BOOST, minToTick(2), 0), 1.0F)
                .effect(() -> new MobEffectInstance(vectorwing.farmersdelight.common.registry.ModEffects.COMFORT, minToTick(2.5F), 0), 1.0F)
                .build();

        MENEMEN_WITH_BREAD = new FoodProperties.Builder()
                .nutrition(10).saturationModifier(0.6F).alwaysEdible()
                .effect(() -> new MobEffectInstance(vectorwing.farmersdelight.common.registry.ModEffects.NOURISHMENT, minToTick(3), 0), 1.0F)
                .effect(() -> new MobEffectInstance(vectorwing.farmersdelight.common.registry.ModEffects.COMFORT, minToTick(3), 0), 1.0F)
                .effect(() -> new MobEffectInstance(GoetyEffects.PHOTOSYNTHESIS, 600, 0), 1.0F)
                .effect(() -> new MobEffectInstance(ModEffects.WIGHT_DENIAL, minToTick(30), 0), 1.0F)
                .build();

        BAKLAVA = new FoodProperties.Builder()
                .nutrition(6).saturationModifier(0.5F).alwaysEdible()
                .effect(() -> new MobEffectInstance(MobEffects.REGENERATION, 200, 0), 1.0F)
                .effect(() -> new MobEffectInstance(vectorwing.farmersdelight.common.registry.ModEffects.NOURISHMENT, 600, 0), 1.0F)
                .effect(() -> new MobEffectInstance(vectorwing.farmersdelight.common.registry.ModEffects.COMFORT, 300, 0), 1.0F)
                .build();

        BISCAT = new FoodProperties.Builder()
                .nutrition(6).saturationModifier(0.3333F).alwaysEdible()
                .effect(() -> new MobEffectInstance(vectorwing.farmersdelight.common.registry.ModEffects.NOURISHMENT, minToTick(5), 0), 1.0F)
                .effect(() -> new MobEffectInstance(MobEffects.LUCK, minToTick(1), 0), 1.0F)
                .build();

        ASCENSION_MOONCAKE = new FoodProperties.Builder()
                .nutrition(66).saturationModifier(333.0F / 66F).alwaysEdible()
                .effect(() -> new MobEffectInstance(MobEffects.REGENERATION, minToTick(66), 5), 1.0F)
                .effect(() -> new MobEffectInstance(MobEffects.DAMAGE_RESISTANCE, minToTick(66), 5), 1.0F)
                .build();

        QUICK_GROWING_SEED_POPCORN = new FoodProperties.Builder()
                .nutrition(8).saturationModifier(5.0F / 8F).alwaysEdible()
                .effect(() -> new MobEffectInstance(MobEffects.SATURATION, 100, 0), 1.0F)
                .effect(() -> new MobEffectInstance(vectorwing.farmersdelight.common.registry.ModEffects.COMFORT, minToTick(10), 0), 1.0F)
                .effect(() -> new MobEffectInstance(GoetyEffects.PHOTOSYNTHESIS, minToTick(5), 0), 1.0F)
                .effect(() -> new MobEffectInstance(vectorwing.farmersdelight.common.registry.ModEffects.NOURISHMENT, minToTick(10), 0), 1.0F)
                .build();

        ECTOPLASM_JELLY = new FoodProperties.Builder()
                .nutrition(4).saturationModifier(4.0F / 4F).alwaysEdible().fast()
                .build();

        FROG_LEG_SANDWICH = new FoodProperties.Builder()
                .nutrition(10).saturationModifier(8.0F / 10F).alwaysEdible()
                .build();

        SPIDER_EGG_BUBBLE_TEA_2 = new FoodProperties.Builder()
                .nutrition(1).saturationModifier(1.0F).alwaysEdible()
                .build();

        PROMOTION_HARD_CANDY = new FoodProperties.Builder()
                .nutrition(1).saturationModifier(1.0F).alwaysEdible()
                .build();

        TOXIC_MEAL = new FoodProperties.Builder()
                .nutrition(8).saturationModifier(0.5F).alwaysEdible()
                .effect(() -> new MobEffectInstance(MobEffects.CONFUSION, 2000, 0), 1.0F)
                .effect(() -> new MobEffectInstance(MobEffects.POISON, 2000, 9), 1.0F)
                .effect(() -> new MobEffectInstance(MobEffects.WEAKNESS, 2000, 4), 1.0F)
                .build();

        POACHED_NETHER_WART_EGG = new FoodProperties.Builder()
                .nutrition(7).saturationModifier(0.2857F).alwaysEdible().fast()
                .build();

        REJECTED_DARK_MEAT_SOUP = new FoodProperties.Builder()
                .nutrition(10).saturationModifier(0.4F).alwaysEdible()
                .effect(() -> new MobEffectInstance(MobEffects.CONFUSION, 600, 0), 1.0F)
                .effect(() -> new MobEffectInstance(MobEffects.POISON, 600, new java.util.Random().nextInt(5)), 1.0F)
                .effect(() -> new MobEffectInstance(MobEffects.WEAKNESS, 600, 1), 1.0F)
                .build();

        CUP = REJECTED_DARK_MEAT_SOUP; // 暂时与拒绝黑肉汤相同

        NETHER_STYLE_FRIED_EGG_SANDWICH = new FoodProperties.Builder()
                .nutrition(11).saturationModifier(0.5455F).alwaysEdible()
                .effect(() -> new MobEffectInstance(MobEffects.FIRE_RESISTANCE, minToTick(8), 0), 1.0F)
                .build();

        EXOTIC_BREAKFAST = new FoodProperties.Builder()
                .nutrition(8).saturationModifier(0.625F).alwaysEdible()
                .effect(() -> new MobEffectInstance(GoetyEffects.WILD_RAGE, minToTick(1), 0), 1.0F)
                .effect(() -> new MobEffectInstance(vectorwing.farmersdelight.common.registry.ModEffects.NOURISHMENT, minToTick(3), 0), 1.0F)
                .build();

        VILLAGERS_FEAST = new FoodProperties.Builder()
                .nutrition(16).saturationModifier(0.625F).alwaysEdible()
                .effect(() -> new MobEffectInstance(MobEffects.HERO_OF_THE_VILLAGE, minToTick(3), 0), 1.0F)
                .effect(() -> new MobEffectInstance(vectorwing.farmersdelight.common.registry.ModEffects.NOURISHMENT, minToTick(10), 0), 1.0F)
                .build();

        JUNGLE_SALAD = new FoodProperties.Builder()
                .nutrition(8).saturationModifier(0.5F).alwaysEdible()
                .effect(() -> new MobEffectInstance(GoetyEffects.PHOTOSYNTHESIS, minToTick(5), 0), 1.0F)
                .effect(() -> new MobEffectInstance(vectorwing.farmersdelight.common.registry.ModEffects.NOURISHMENT, minToTick(1), 0), 1.0F)
                .build();

        SAUCE_GRILLED_CANDY_FISH = new FoodProperties.Builder()
                .nutrition(9).saturationModifier(0.6667F).alwaysEdible()
                .effect(() -> new MobEffectInstance(vectorwing.farmersdelight.common.registry.ModEffects.NOURISHMENT, minToTick(8), 0), 1.0F)
                .effect(() -> new MobEffectInstance(GoetyEffects.FIERY_AURA, minToTick(5), 0), 1.0F)
                .build();

        METAMORPHIC_SCENT_FRUIT = new FoodProperties.Builder()
                .nutrition(10).saturationModifier(0.8F).alwaysEdible()
                .build();

        METAMORPHIC_SCENT_GRASS = new FoodProperties.Builder()
                .nutrition(2).saturationModifier(1.5F).alwaysEdible()
                .build();
        TEN_THOUSAND_POISON_FEAST = new FoodProperties.Builder()
                .nutrition(8)
                .saturationModifier(0.6f)
                .alwaysEdible()
                .build();
    }
}