package net.v_black_cat.goetydelight.init;

import net.minecraft.world.item.Item;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.bus.api.IEventBus;
import net.v_black_cat.goetydelight.GoetyDelight;

public class ModItems {
    // 创建专属于物品的 DeferredRegister，使用模组主类的 MODID
    public static final DeferredRegister.Items ITEMS =
            DeferredRegister.createItems(GoetyDelight.MODID);

    // 示例物品：一个普通的物品（无特殊属性）
    public static final DeferredItem<Item> EXAMPLE_ITEM = ITEMS.registerSimpleItem(
            "example_item",
            new Item.Properties()
    );

    // 示例食物物品
    public static final DeferredItem<Item> EXAMPLE_FOOD = ITEMS.registerSimpleItem(
            "example_food",
            new Item.Properties().food(new net.minecraft.world.food.FoodProperties.Builder()
                    .nutrition(4).saturationModifier(0.5f).build())
    );


//    // ==================== 物品声明区域 ====================
//    // 块物品
//    public static final DeferredItem<Item> METAMORPHIC_SCENT_GRASS;
//
//    //刷子
//    public static final DeferredItem<Item> CURSED_METAL_BRUSH;
//    public static final DeferredItem<Item> DARK_BRUSH;
//    // 刀具物品
//    public static final DeferredItem<Item> APOCALYPTIUM_KNIFE;
//    public static final DeferredItem<Item> VENOMOUS_SPIDER_KNIFE;
//    public static final DeferredItem<Item> SPECTRE_KNIFE;
//    public static final DeferredItem<Item> CURSED_INGOT_KNIFE;
//    public static final DeferredItem<Item> DARK_KNIFE;
//    public static final DeferredItem<Item> APOCALYPTIUM_INGOT_BRUSH;
//
//    // 武器物品
//    public static final DeferredItem<Item> MARBLE_OP_SWORD;
//    public static final DeferredItem<Item> FALSE_PROVERBS;
//    public static final DeferredItem<Item> PARASITIZED_WARDEN;
//    public static final DeferredItem<Item> VIZIERS_COOKBOOK;
//    //刷怪蛋
//
//    public static final DeferredItem<Item> GHOST_FARMER_SPAWN_EGG;
//    // 食物物品
//    public static final DeferredItem<Item> GOETYDELIGHT_ICON;
//    public static final DeferredItem<Item> TAINTED_DRINK;
//    public static final DeferredItem<Item> REJECTED_DARK_MEAT_SOUP;
//    public static final DeferredItem<Item> SIBLING_SUNDAE;
//    public static final DeferredItem<Item> PROMOTION_HARD_CANDY;
//    public static final DeferredItem<Item> CUP;
//    public static final DeferredItem<Item> TOXIC_MEAL;
//    public static final DeferredItem<Item> POACHED_NETHER_WART_EGG;
//    public static final DeferredItem<Item> ECTOPLASM_JELLY;
//    public static final DeferredItem<Item> ROASTED_CORPSE_MAGGOTS;
//    public static final DeferredItem<Item> WHITE_SHARK_CANDY;
//    public static final DeferredItem<Item> WHITE_SHARK_SUGAR_PACK;
//    public static final DeferredItem<Item> SUNSHINE_SUGAR_BUN;
//    public static final DeferredItem<Item> CANDY_FISH;
//    public static final DeferredItem<Item> GRAPE_SLUSH;
//    public static final DeferredItem<Item> FROG_LEG_SANDWICH;
//    public static final DeferredItem<Item> SPIDER_EGG_BUBBLE_TEA;
//    public static final DeferredItem<Item> SPIDER_EGG_BUBBLE_TEA_2;
//    public static final DeferredItem<Item> SAUCE_GRILLED_CANDY_FISH;
//    public static final DeferredItem<Item> CRYING_SHARK_SUGAR_PACK;
//    public static final DeferredItem<Item> SEVEN_LEAF_PUDDING;
//    public static final DeferredItem<Item> BEAR_PAW;
//    public static final DeferredItem<Item> CAKE;
//    public static final DeferredItem<Item> OMINOUS_ICE_CREAM;
//    public static final DeferredItem<Item> ECTOPLASMIC_MELON;
//    public static final DeferredItem<Item> BLUE_ECTOPLASMIC_SUNDAE;
//    public static final DeferredItem<Item> SKULL_SHOT;
//    public static final DeferredItem<Item> NIGHT_HEART_PEA_SOUP;
//    public static final DeferredItem<Item> POACHED_SPIDER_EGG;
//    public static final DeferredItem<Item> GRILL_FROG_LEG;
//    public static final DeferredItem<Item> FRENZIED_FUNGUS_POP_ROCKS;
//    public static final DeferredItem<Item> SOUL_CONVERGENCE_ROOM;
//    public static final DeferredItem<Item> SOUL_CONVERGENCE_ROOM_2;
//    public static final DeferredItem<Item> BONE_LORD_ASH_RICE;
//    public static final DeferredItem<Item> RUBY_HARD_CANDY;
//    public static final DeferredItem<Item> CRISP_BISCUIT;
//    public static final DeferredItem<Item> ROTTEN_CORPSE_MAGGOT_FEAST;
//    public static final DeferredItem<Item> CORPSE_MAGGOT;
//    public static final DeferredItem<Item> QUICK_GROWING_SEED_POPCORN;
//    public static final DeferredItem<Item> NETHER_STYLE_FRIED_EGG_SANDWICH;
//    public static final DeferredItem<Item> EXOTIC_BREAKFAST;
//    public static final DeferredItem<Item> JUNGLE_SALAD;
//    public static final DeferredItem<Item> BOILING_BLOOD_BREW;
//    public static final DeferredItem<Item> ASCENSION_MOONCAKE;
//    public static final DeferredItem<Item> VILLAGERS_FEAST;
//    public static final DeferredItem<Item> CHERRY_BLOSSOM_CAKE;
//    public static final DeferredItem<Item> NETHER_WART_OMELETTE;
//    public static final DeferredItem<Item> WARPED_WART_OMELETTE;
//    public static final DeferredItem<Item> FULL_SPIDER_FEAST;
//    public static final DeferredItem<Item> LIQUID_VOID_TEA_DRINK;
//    public static final DeferredItem<Item> PURE_DRINK;
//    public static final DeferredItem<Item> LICHS_CHAOS_STEW;
//    public static final DeferredItem<Item> MAGIC_QUARTZ_COOKIE;
//    public static final DeferredItem<Item> SNAP_UNHOLY_TRIPE;
//    public static final DeferredItem<Item> SUNDAE_OF_THE_PHILOSOPHERS_POTION;
//    public static final DeferredItem<Item> THE_BOX_OF_THE_DEAD;
//    public static final DeferredItem<Item> RING_PACKED_VOID_GEL_JELLY;
//    public static final DeferredItem<Item> STUFFED_TALL_SKULL_RICE;
//    public static final DeferredItem<Item> OMINOUS_RAMUNE;
//    public static final DeferredItem<Item> BOAT_STUFFED_ROASTED_WARDEN_HEAD;
//    public static final DeferredItem<Item> BOAT_STUFFED_ROASTED_WARDEN_MEET;
//    public static final DeferredItem<Item> BOAT_STUFFED_ROASTED_WARDEN_FLANK;
//    public static final DeferredItem<Item> ANCIENT_ENCHANTED_GOLDEN_APPLE;
//    public static final DeferredItem<Item> NOT_ANYTHING;
//    public static final DeferredItem<Item> ROAST_LAOWANG;
//    public static final DeferredItem<Item> POLARICE;
//    public static final DeferredItem<Item> METAMORPHIC_SCENT_FRUIT;
//    public static final DeferredItem<Item> FORBIDDDEN_SOUP_BUN;
//    public static final DeferredItem<Item> HIDDEN_PANCAKE;
//    public static final DeferredItem<Item> CREAMY_BERRY_FISH_PASTE_DUMPLING_WITH_CHOCOLATE_SAUCE;
//    public static final DeferredItem<Item> OBSIDIAN_THICK_SOUP;
//    public static final DeferredItem<Item> SHAWARMA;
//    public static final DeferredItem<Item> RAKI;
//    public static final DeferredItem<Item> MENEMEN_WITH_BREAD;
//    public static final DeferredItem<Item> BAKLAVA;
//    public static final DeferredItem<Item> CUSTOM_DOLL;
//    public static final DeferredItem<Item> BISCAT;
//    public static final DeferredItem<Item> RUBY_SYRUP;
//    public static final DeferredItem<Item> ROAST_LAOWANG_EAR;
//    public static final DeferredItem<Item> ROAST_LAOWANG_FEET;
//    public static final DeferredItem<Item> ROAST_LAOWANG_HEAD;
//    public static final DeferredItem<Item> ROAST_LAOWANG_LEG;
//    public static final DeferredItem<Item> ONION_PORK_CHOP_RICE;
//    public static final DeferredItem<Item> ECTOPLASMIC_MELON_SALAD;
//
//    //种子
//    public static final DeferredItem<Item> ECTOPLASMIC_MELON_SEEDS;
//    public static final DeferredItem<Item> METAMORPHIC_SCENT_GRASS_SEEDS;
//
//    //    public static final DeferredItem<Item> MENU;
//    public static final DeferredItem<Item> DOLL_ITEM;


//    // ==================== 静态初始化块：物品定义区域 ====================
//    static {
//        NOT_ANYTHING = ITEMS.register("not_anything",
//                () -> new Item(basicItem().stacksTo(1)));
//
//        CUSTOM_DOLL = ITEMS.register("custom_doll", () -> new CustomDollItem(ModBlocks.CUSTOM_DOLL.get()));
//
//        METAMORPHIC_SCENT_GRASS = ITEMS.register("metamorphic_scent_grass",
//                () -> new MetamorphicScentGrassItem(basicItem().stacksTo(64).food(simpleFoodItemProperties(2, 3).build())));
//
//        // 神金刀
//        APOCALYPTIUM_KNIFE = registerWithTab("apocalyptium_knife",
//                () -> new KnifeItem(Tiers.NETHERITE, basicItem().durability(1666)));
//
//        VENOMOUS_SPIDER_KNIFE = registerWithTab("venomous_spider_knife",
//                () -> new KnifeItem(Tiers.IRON, basicItem()));
//        SPECTRE_KNIFE = registerWithTab("spectre_knife",
//                () -> new KnifeItem(Tiers.IRON, basicItem()));
//
//        // 诅咒金属刀
//        CURSED_INGOT_KNIFE = registerWithTab("cursed_ingot_knife",
//                () -> new KnifeItem(ModTiers.SPECIAL, basicItem().durability(256)));
//
//        // 黑暗金属刀
//        DARK_KNIFE = registerWithTab("dark_knife",
//                () -> new DarkKnifeItem(ModTiers.DARK, 1F, -2.0F, basicItem().durability(512)));
//
//        FALSE_PROVERBS = ITEMS.register("false_proverbs",
//                () -> new FalseProverbsItem(ModTiers.VOID, 9, -2, basicItem().rarity(Rarity.EPIC)));
//
//        // 诅咒金属刷子
//        CURSED_METAL_BRUSH = ITEMS.register("cursed_metal_brush",
//                () -> new DarkBrushItem(basicItem().durability(64), 2));
//
//        // 黑暗金属刷子
//        DARK_BRUSH = ITEMS.register("dark_brush",
//                () -> new DarkBrushItem(basicItem().durability(64), 3));
//
//        // 神金刷子
//        APOCALYPTIUM_INGOT_BRUSH = ITEMS.register("apocalyptium_ingot_brush",
//                () -> new DarkBrushItem(basicItem().durability(166), 4));
//
//        // 大理石op剑
//        MARBLE_OP_SWORD = ITEMS.register("marble_op_sword",
//                () -> new MarbleOpSwordItem(Tiers.WOOD, 1, 2, basicItem().rarity(Rarity.EPIC)));
//
//        PARASITIZED_WARDEN = ITEMS.register("parasitized_warden",
//                () -> new Item(basicItem().stacksTo(1).rarity(Rarity.UNCOMMON)));
//
//        VIZIERS_COOKBOOK = ITEMS.register("viziers_cookbook",
//                () -> new ViziersCookbookItem());
//
//        GOETYDELIGHT_ICON = ITEMS.register("goetydelight_icon",
//                () -> simpleFoodItem(666, 666, true));
//
//        TAINTED_DRINK = ITEMS.register("tainted_drink",
//                () -> new CustomDrinkItem(basicItem().stacksTo(1).rarity(Rarity.RARE).rarity(Rarity.RARE).food(
//                        simpleFoodItemProperties(4, 4)
//                                .effect(() -> new MobEffectInstance(MobEffects.REGENERATION, 150, 1), 1.0F)
//                                .effect(() -> new MobEffectInstance(MobEffects.DAMAGE_RESISTANCE, 300, 1), 1.0F)
//                                .effect(() -> new MobEffectInstance(BuiltInRegistries.MOB_EFFECT.wrapAsHolder(ModEffects.THE_PALE_MESSRNGER.get()), minToTick(3), 0), 1.0F)
//                                .effect(() -> new MobEffectInstance(BuiltInRegistries.MOB_EFFECT.wrapAsHolder(ModEffects.ZOMBIFIED_PIGLIN_BRUTE_SERVANT_SUPPORT.get()), minToTick(6), 0), 1.0F)
//                                .build())));
//
//        PURE_DRINK = ITEMS.register("pure_drink",
//                () -> {
//                    FoodProperties food = simpleFoodItemProperties(4, 4)
//                            .effect(() -> new MobEffectInstance(MobEffects.REGENERATION, 600, 3), 1.0F)
//                            .effect(() -> new MobEffectInstance(MobEffects.DAMAGE_RESISTANCE, 1200, 3), 1.0F)
//                            .effect(() -> new MobEffectInstance(BuiltInRegistries.MOB_EFFECT.wrapAsHolder(ModEffects.THE_PALE_MESSRNGER.get()), minToTick(15), 0), 1.0F)
//                            .effect(() -> new MobEffectInstance(BuiltInRegistries.MOB_EFFECT.wrapAsHolder(ModEffects.ZOMBIFIED_PIGLIN_BRUTE_SERVANT_SUPPORT.get()), minToTick(30), 0), 1.0F)
//                            .build();
//                    return new CustomDrinkItem(basicItem().stacksTo(1).rarity(Rarity.RARE).food(food)) {
//                        @Override
//                        public boolean isFoil(ItemStack pStack) {
//                            return true;
//                        }
//                    };
//                });
//
//        CUP = ITEMS.register("eternal_refusal_of_black_meat_soup",
//                () -> new EternalRefusalOfBlackMeatSoupItem(basicItem().stacksTo(1).rarity(Rarity.RARE).food(
//                        simpleFoodItemProperties(10, 4)
//                                .effect(() -> new MobEffectInstance(MobEffects.CONFUSION, 600, 0), 1.0F)
//                                .effect(() -> {
//                                    int randomAmplifier = new Random().nextInt(5);
//                                    return new MobEffectInstance(MobEffects.POISON, 600, randomAmplifier, false, true);
//                                }, 1.0F)
//                                .effect(() -> new MobEffectInstance(MobEffects.WEAKNESS, 600, 1), 1.0F)
//                                .build())));
//
//        REJECTED_DARK_MEAT_SOUP = ITEMS.register("rejected_dark_meat_soup",
//                () -> new RejectedDarkMeatSoupItem(basicItem().stacksTo(16).food(
//                        simpleFoodItemProperties(10, 4)
//                                .effect(() -> new MobEffectInstance(MobEffects.CONFUSION, 600, 0), 1.0F)
//                                .effect(() -> {
//                                    int randomAmplifier = new Random().nextInt(5);
//                                    return new MobEffectInstance(MobEffects.POISON, 600, randomAmplifier, false, true);
//                                }, 1.0F)
//                                .effect(() -> new MobEffectInstance(MobEffects.WEAKNESS, 600, 1), 1.0F)
//                                .build())));
//
//        PROMOTION_HARD_CANDY = ITEMS.register("promotion_hard_candy",
//                () -> simpleFoodItem(1, 1, true));
//
//        TOXIC_MEAL = ITEMS.register("toxic_meal",
//                () -> new ToxicMealItem(basicItem().stacksTo(16).food(
//                        simpleFoodItemProperties(8, 4)
//                                .effect(() -> new MobEffectInstance(MobEffects.CONFUSION, 2000, 0), 1.0F)
//                                .effect(() -> new MobEffectInstance(MobEffects.POISON, 2000, 9), 1.0F)
//                                .effect(() -> new MobEffectInstance(MobEffects.WEAKNESS, 2000, 4), 1.0F)
//                                .build())));
//        POACHED_NETHER_WART_EGG = ITEMS.register("poached_nether_wart_egg",
//                () -> new PoachedNetherWartEggItem(basicItem().stacksTo(16).food(
//                        simpleFoodItemProperties(7, 2).fast().build())));
//        ECTOPLASM_JELLY = ITEMS.register("ectoplasm_jelly",
//                () -> simpleFastFoodItem(4, 4, false));
//        FROG_LEG_SANDWICH = ITEMS.register("frog_leg_sandwich",
//                () -> simpleFoodItem(10, 8, false));
//
//        SPIDER_EGG_BUBBLE_TEA_2 = ITEMS.register("spider_egg_bubble_tea_2",
//                () -> simpleFoodItem(1, 1, true));
//
//        // 特殊效果食物物品初始化
//
//        ASCENSION_MOONCAKE = ITEMS.register("ascension_mooncake",
//                () -> new Item(basicItem().stacksTo(1).rarity(Rarity.EPIC).food(
//                        simpleFoodItemProperties(66, 333)
//                                .effect(() -> new MobEffectInstance(MobEffects.REGENERATION, minToTick(66), 5), 1.0F)
//                                .effect(() -> new MobEffectInstance(MobEffects.DAMAGE_RESISTANCE, minToTick(66), 5), 1.0F)
//                                .build())));
//
//        SPIDER_EGG_BUBBLE_TEA = ITEMS.register("spider_egg_bubble_tea",
//                () -> new CustomDrinkItem(basicItem().stacksTo(16).food(
//                        simpleFoodItemProperties(6, 4)
//                                .effect(() -> new MobEffectInstance(CLIMBING_EFFECT_SUPPLIER.get(), minToTick(7), 0), 1.0F)
//                                .build())));
//
//        BOILING_BLOOD_BREW = ITEMS.register("boiling_blood_brew",
//                () -> new CustomDrinkItem(basicItem().stacksTo(16).food(
//                        simpleFoodItemProperties(6, 4)
//                                .effect(() -> new MobEffectInstance(FIERY_AURA_SUPPLIER.get(), minToTick(5), 0), 1.0F)
//                                .effect(() -> new MobEffectInstance(COMFORT_EFFECT_SUPPLIER.get(), minToTick(5), 0), 1.0F)
//                                .build())));
//
//        NETHER_STYLE_FRIED_EGG_SANDWICH = ITEMS.register("nether_style_fried_egg_sandwich",
//                () -> new Item(basicItem().stacksTo(64).food(
//                        simpleFoodItemProperties(11, 6)
//                                .effect(() -> new MobEffectInstance(MobEffects.FIRE_RESISTANCE, minToTick(8), 0), 1.0F)
//                                .build())));
//
//        EXOTIC_BREAKFAST = ITEMS.register("exotic_breakfast",
//                () -> new Item(basicItem().stacksTo(16).food(
//                        simpleFoodItemProperties(8, 5)
//                                .effect(() -> new MobEffectInstance(WILD_RAGE_EFFECT_SUPPLIER.get(), minToTick(1), 0), 1.0F)
//                                .effect(() -> new MobEffectInstance(NOURISHMENT_EFFECT_SUPPLIER.get(), minToTick(3), 0), 1.0F)
//                                .build())));
//
//        VILLAGERS_FEAST = ITEMS.register("villagers_feast",
//                () -> new Item(basicItem().stacksTo(16).food(
//                        simpleFoodItemProperties(16, 10)
//                                .effect(() -> new MobEffectInstance(MobEffects.HERO_OF_THE_VILLAGE, minToTick(3), 0), 1.0F)
//                                .effect(() -> new MobEffectInstance(NOURISHMENT_EFFECT_SUPPLIER.get(), minToTick(10), 0), 1.0F)
//                                .build())));
//
//        JUNGLE_SALAD = ITEMS.register("jungle_salad",
//                () -> new BowlFoodItem(basicItem().stacksTo(16).food(
//                        simpleFoodItemProperties(8, 4)
//                                .effect(() -> new MobEffectInstance(PHOTOSYNTHESIS_SUPPLIER.get(), minToTick(5), 0), 1.0F)
//                                .effect(() -> new MobEffectInstance(NOURISHMENT_EFFECT_SUPPLIER.get(), minToTick(1), 0), 1.0F)
//                                .build())));
//
//        QUICK_GROWING_SEED_POPCORN = ITEMS.register("quick_growing_seed_popcorn",
//                () -> new Item(basicItem().stacksTo(16).food(
//                        simpleFoodItemProperties(8, 5)
//                                .effect(() -> new MobEffectInstance(MobEffects.SATURATION, 100, 0), 1.0F)
//                                .effect(() -> new MobEffectInstance(COMFORT_EFFECT_SUPPLIER.get(), minToTick(10), 0), 1.0F)
//                                .effect(() -> new MobEffectInstance(PHOTOSYNTHESIS_SUPPLIER.get(), minToTick(5), 0), 1.0F)
//                                .effect(() -> new MobEffectInstance(NOURISHMENT_EFFECT_SUPPLIER.get(), minToTick(10), 0), 1.0F)
//                                .build())));
//
//        SAUCE_GRILLED_CANDY_FISH = ITEMS.register("sauce_grilled_candy_fish",
//                () -> new SauceGrilledCandyFishItem(basicItem().stacksTo(8).food(
//                        simpleFoodItemProperties(9, 6)
//                                .effect(() -> new MobEffectInstance(NOURISHMENT_EFFECT_SUPPLIER.get(), minToTick(8), 0), 1.0F)
//                                .effect(() -> new MobEffectInstance(BuiltInRegistries.MOB_EFFECT.wrapAsHolder(FIERY_AURA.get()), minToTick(5), 0), 1.0F)
//                                .build())));
//
//        CANDY_FISH = ITEMS.register("candy_fish",
//                () -> new CandyFishItem(basicItem().stacksTo(8).food(
//                        simpleFoodItemProperties(6, 4)
//                                .effect(() -> new MobEffectInstance(NOURISHMENT_EFFECT_SUPPLIER.get(), minToTick(7), 0), 1.0F)
//                                .effect(() -> new MobEffectInstance(MobEffects.WATER_BREATHING, minToTick(5), 0), 1.0F)
//                                .build())));
//
//        WHITE_SHARK_SUGAR_PACK = ITEMS.register("sugar_pack",
//                () -> new Item(basicItem().stacksTo(16).food(
//                        simpleFoodItemProperties(6, 4)
//                                .effect(() -> new MobEffectInstance(NOURISHMENT_EFFECT_SUPPLIER.get(), minToTick(4), 0), 1.0F)
//                                .build())));
//
//        WHITE_SHARK_CANDY = ITEMS.register("sugar_scepter",
//                () -> new SugarScepterItem(basicItem().stacksTo(8).rarity(Rarity.UNCOMMON).food(
//                        simpleFoodItemProperties(8, 5)
//                                .effect(() -> new MobEffectInstance(MobEffects.REGENERATION, minToTick(1), 1), 1.0F)
//                                .effect(() -> new MobEffectInstance(NOURISHMENT_EFFECT_SUPPLIER.get(), minToTick(10), 0), 1.0F)
//                                .build())));
//
//        SIBLING_SUNDAE = ITEMS.register("possible_holy_representative",
//                () -> new SiblingSundaeItem(basicItem().stacksTo(8).rarity(Rarity.UNCOMMON).food(
//                        simpleFoodItemProperties(6, 5)
//                                .effect(() -> new MobEffectInstance(BuiltInRegistries.MOB_EFFECT.wrapAsHolder(INSIGHT.get()), minToTick(2.5F), 3), 1.0F)
//                                .build())));
//
//        ROASTED_CORPSE_MAGGOTS = ITEMS.register("roasted_corpse_maggots",
//                () -> new RoastedCorpseMaggotsitem(basicItem().craftRemainder(Items.BOWL).stacksTo(16).food(
//                        simpleFoodItemProperties(5, 2)
//                                .build())));
//
//        CORPSE_MAGGOT = ITEMS.register("corpse_maggot",
//                () -> new CorpseMaggotItem(basicItem().stacksTo(64).food(
//                        simpleFoodItemProperties(3, 1)
//                                .effect(() -> new MobEffectInstance(MobEffects.CONFUSION, sToTick(10), 0), 1.0F)
//                                .build())));
//
//        CRYING_SHARK_SUGAR_PACK = ITEMS.register("cry_sugar_pack",
//                () -> new Item(basicItem().stacksTo(64).food(
//                        simpleFoodItemProperties(7, 4)
//                                .effect(() -> new MobEffectInstance(BuiltInRegistries.MOB_EFFECT.wrapAsHolder(ModEffects.HYDRATION.get()), minToTick(15), 1), 1.0F)
//                                .effect(() -> new MobEffectInstance(NOURISHMENT_EFFECT_SUPPLIER.get(), minToTick(5), 0), 1.0F)
//                                .build())));
//
//        SUNSHINE_SUGAR_BUN = ITEMS.register("sunshine_sugar_bun",
//                () -> new Item(basicItem().stacksTo(64).food(
//                        simpleFoodItemProperties(7, 4)
//                                .effect(() -> new MobEffectInstance(PHOTOSYNTHESIS_SUPPLIER.get(), minToTick(15), 1), 1.0F)
//                                .effect(() -> new MobEffectInstance(NOURISHMENT_EFFECT_SUPPLIER.get(), minToTick(5), 0), 1.0F)
//                                .build())));
//
//        GRAPE_SLUSH = ITEMS.register("grape_slush",
//                () -> new NoGlassBottleDrinkItem(basicItem().stacksTo(64).food(
//                        simpleFoodItemProperties(9, 6)
//                                .effect(() -> new MobEffectInstance(CHILL_HIDE_EFFECT_SUPPLIER.get(), 4200, 1), 1.0F)
//                                .effect(() -> new MobEffectInstance(FROSTY_AURA_SUPPLIER.get(), 600, 1), 1.0F)
//                                .effect(() -> new MobEffectInstance(NOURISHMENT_EFFECT_SUPPLIER.get(), minToTick(10), 0), 1.0F)
//                                .build())));
//
//        SEVEN_LEAF_PUDDING = ITEMS.register("sweet_berry_pudding",
//                () -> new SevenLeafPuddingItem(basicItem().stacksTo(64).craftRemainder(Items.BOWL).food(
//                        simpleFoodItemProperties(7, 5)
//                                .effect(() -> new MobEffectInstance(MobEffects.REGENERATION, minToTick(5), 1), 1.0F)
//                                .effect(() -> new MobEffectInstance(NOURISHMENT_EFFECT_SUPPLIER.get(), minToTick(5), 0), 1.0F)
//                                .build())));
//
//        BEAR_PAW = ITEMS.register("bear_paw",
//                () -> new Item(basicItem().stacksTo(64).food(
//                        simpleFoodItemProperties(6, 5)
//                                .effect(() -> new MobEffectInstance(NOURISHMENT_EFFECT_SUPPLIER.get(), 6000, 0), 1.0F)
//                                .effect(() -> new MobEffectInstance(RAMPAGE_EFFECT_SUPPLIER.get(), 2400, 0), 1.0F)
//                                .build())));
//        CAKE = ITEMS.register("royal_cake",
//                () -> new CakeItem(basicItem().stacksTo(64).rarity(Rarity.RARE).food(
//                        simpleFoodItemProperties(6, 3)
//                                .effect(() -> new MobEffectInstance(NOURISHMENT_EFFECT_SUPPLIER.get(), 4500, 0), 1.0F)
//                                .effect(() -> new MobEffectInstance(FORTUNATE_EFFECT_SUPPLIER.get(), 1500, 2), 1.0F)
//                                .effect(() -> new MobEffectInstance(BuiltInRegistries.MOB_EFFECT.wrapAsHolder(BOTTLING.get()), 1500, 2), 1.0F)
//                                .effect(() -> new MobEffectInstance(MobEffects.DAMAGE_RESISTANCE, 1500, 1), 1.0F)
//                                .build())));
//        OMINOUS_ICE_CREAM = ITEMS.register("ominous_ice_cream",
//                () -> new OminousIceCreamItem(basicItem().stacksTo(64).rarity(Rarity.UNCOMMON).food(
//                        simpleFoodItemProperties(8, 5)
//                                .effect(() -> new MobEffectInstance(MobEffects.BAD_OMEN, 6000, 4), 1.0F)
//                                .effect(() -> new MobEffectInstance(CHILL_HIDE_EFFECT_SUPPLIER.get(), 1200, 0), 1.0F)
//                                .build())));
//        ECTOPLASMIC_MELON = ITEMS.register("ectoplasmic_melon",
//                () -> new Item(basicItem().stacksTo(64).food(
//                        simpleFoodItemProperties(3, 1)
//                                .effect(() -> new MobEffectInstance(CHILL_HIDE_EFFECT_SUPPLIER.get(), 100, 0), 1.0F)
//                                .build())));
//        BLUE_ECTOPLASMIC_SUNDAE = ITEMS.register("blue_ectoplasmic_sundae",
//                () -> new Item(basicItem().stacksTo(64).food(
//                        simpleFoodItemProperties(10, 6)
//                                .effect(() -> new MobEffectInstance(FORTUNATE_EFFECT_SUPPLIER.get(), 12000, 0), 1.0F)
//                                .effect(() -> new MobEffectInstance(MobEffects.REGENERATION, 2400, 0), 1.0F)
//                                .effect(() -> new MobEffectInstance(CHILL_HIDE_EFFECT_SUPPLIER.get(), 2400, 0), 1.0F)
//                                .build())));
//
//        SKULL_SHOT = ITEMS.register("skull_shot",
//                () -> new NoGlassBottleDrinkItem(basicItem().stacksTo(16).food(
//                        simpleFoodItemProperties(6, 4)
//                                .effect(() -> new MobEffectInstance(CORPSE_EATER_EFFECT_SUPPLIER.get(), 1200, 0), 1.0F)
//                                .build())));
//
//        NIGHT_HEART_PEA_SOUP = ITEMS.register("night_heart_pea_soup",
//                () -> new NightHeartPeaSoupItem(basicItem().craftRemainder(Items.GLASS_BOTTLE).stacksTo(64).rarity(Rarity.UNCOMMON).food(
//                        simpleFoodItemProperties(7, 3)
//                                .effect(() -> new MobEffectInstance(MobEffects.DAMAGE_BOOST, minToTick(5), 2), 1.0F)
//                                .effect(() -> new MobEffectInstance(BuiltInRegistries.MOB_EFFECT.wrapAsHolder(SHADOW_WALK.get()), sToTick(60), 2), 1.0F)
//                                .effect(() -> new MobEffectInstance(MobEffects.DAMAGE_RESISTANCE, 6000, 2), 1.0F)
//                                .effect(() -> new MobEffectInstance(NOURISHMENT_EFFECT_SUPPLIER.get(), 12000, 0), 1.0F)
//                                .effect(() -> new MobEffectInstance(COMFORT_EFFECT_SUPPLIER.get(), 12000, 0), 1.0F)
//                                .effect(() -> new MobEffectInstance(BuiltInRegistries.MOB_EFFECT.wrapAsHolder(ModEffects.SERVANT_REINFORCEMENT.get()), minToTick(5), 0), 1.0F)
//                                .build())));
//        POACHED_SPIDER_EGG = ITEMS.register("poached_spider_egg",
//                () -> new Item(basicItem().stacksTo(64).food(
//                        simpleFoodItemProperties(5, 2)
//                                .effect(() -> new MobEffectInstance(CLIMBING_EFFECT_SUPPLIER.get(), 200, 0), 1.0F)
//                                .fast()
//                                .build())));
//        GRILL_FROG_LEG = ITEMS.register("grill_frog_leg",
//                () -> new Item(basicItem().stacksTo(64).food(
//                        simpleFoodItemProperties(10, 6)
//                                .effect(() -> new MobEffectInstance(FROG_LEG_EFFECT_SUPPLIER.get(), 1200, 0), 1.0F)
//                                .effect(() -> new MobEffectInstance(MobEffects.REGENERATION, 400, 1), 1.0F)
//                                .build())));
//        FRENZIED_FUNGUS_POP_ROCKS = ITEMS.register("frenzied_fungus_pop_rocks",
//                () -> new FrenziedFungusPopRocksItem(basicItem().stacksTo(64).food(
//                        simpleFoodItemProperties(6, 4)
//                                .build())));
//        SOUL_CONVERGENCE_ROOM = ITEMS.register("gathering_soul_embryos",
//                () -> new Item(basicItem().stacksTo(64).food(
//                        simpleFoodItemProperties(8, 12)
//                                .effect(() -> new MobEffectInstance(MobEffects.HUNGER, 200, 0), 1.0F)
//                                .effect(() -> new MobEffectInstance(SOUL_ARMOR_EFFECT_SUPPLIER.get(), 1200, 1), 1.0F)
//                                .build())));
//        SOUL_CONVERGENCE_ROOM_2 = ITEMS.register("soul_convergence_room",
//                () -> new Item(basicItem().stacksTo(64).rarity(Rarity.UNCOMMON).food(
//                        simpleFoodItemProperties(20, 30)
//                                .effect(() -> new MobEffectInstance(SOUL_ARMOR_EFFECT_SUPPLIER.get(), 6000, 4), 1.0F)
//                                .effect(() -> new MobEffectInstance(MobEffects.HEALTH_BOOST, 6000, 2), 1.0F)
//                                .effect(() -> new MobEffectInstance(BuiltInRegistries.MOB_EFFECT.wrapAsHolder(ModEffects.HUNTING_DENIAL.get()), minToTick(10), 0), 1.0F)
//                                .build())));
//        BONE_LORD_ASH_RICE = ITEMS.register("bone_lord_ash_rice",
//                () -> new BoneLordAshRiceItem(basicItem().craftRemainder(Items.BOWL).stacksTo(64).food(
//                        simpleFoodItemProperties(6, 4)
//                                .effect(() -> new MobEffectInstance(CHILL_HIDE_EFFECT_SUPPLIER.get(), 6000, 1), 1.0F)
//                                .effect(() -> new MobEffectInstance(BUFF_EFFECT_SUPPLIER.get(), 6000, 2), 1.0F)
//                                .effect(() -> new MobEffectInstance(NOURISHMENT_EFFECT_SUPPLIER.get(), minToTick(5), 0), 1.0F)
//                                .effect(() -> new MobEffectInstance(BuiltInRegistries.MOB_EFFECT.wrapAsHolder(CORPSE_EATER.get()), minToTick(1), 2), 1.0F)
//                                .build())));
//        RUBY_HARD_CANDY = ITEMS.register("ruby_hard_candy",
//                () -> new RubyHardCandyItem(basicItem().stacksTo(64).rarity(Rarity.UNCOMMON).food(
//                        simpleFoodItemProperties(10, 8)
//                                .effect(() -> new MobEffectInstance(BuiltInRegistries.MOB_EFFECT.wrapAsHolder(ModEffects.SPELL_MASTERY.get()), minToTick(10), 2), 1.0F)
//                                .effect(() -> new MobEffectInstance(BuiltInRegistries.MOB_EFFECT.wrapAsHolder(ModEffects.SPELL_DURATION.get()), minToTick(10), 2), 1.0F)
//                                .effect(() -> new MobEffectInstance(MobEffects.REGENERATION, minToTick(8), 1), 1.0F)
//                                .effect(() -> new MobEffectInstance(NOURISHMENT_EFFECT_SUPPLIER.get(), minToTick(15), 0), 1.0F)
//                                .build())));
//        CRISP_BISCUIT = ITEMS.register("crisp_biscuit",
//                () -> new CrispBiscuitItem(basicItem().stacksTo(64).food(
//                        simpleFoodItemProperties(9, 6)
//                                .effect(() -> new MobEffectInstance(NOURISHMENT_EFFECT_SUPPLIER.get(), minToTick(5), 2), 1.0F)
//                                .effect(() -> new MobEffectInstance(BuiltInRegistries.MOB_EFFECT.wrapAsHolder(ModEffects.SPELL_MASTERY.get()), minToTick(2), 0), 1.0F)
//                                .build())));
//        ROTTEN_CORPSE_MAGGOT_FEAST = ITEMS.register("rotten_corpse_maggot_feast",
//                () -> new RottenCorpseMaggotFeastItem(basicItem().stacksTo(16).food(
//                        simpleFoodItemProperties(8, 5)
//                                .effect(() -> new MobEffectInstance(NOURISHMENT_EFFECT_SUPPLIER.get(), minToTick(2), 0), 1.0F)
//                                .build())));
//        CHERRY_BLOSSOM_CAKE = ITEMS.register("cherry_blossom_cake",
//                () -> new CherryBlossomCakeItem(basicItem().stacksTo(64).rarity(Rarity.UNCOMMON).food(
//                        simpleFoodItemProperties(12, 8)
//                                .effect(() -> new MobEffectInstance(NOURISHMENT_EFFECT_SUPPLIER.get(), minToTick(10), 0), 1.0F)
//                                .effect(() -> new MobEffectInstance(MobEffects.LUCK, minToTick(2), 2), 1.0F)
//                                .build())));
//        NETHER_WART_OMELETTE = ITEMS.register("nether_wart_omelette",
//                () -> new NetherWartOmeletteItem(basicItem().stacksTo(64).food(
//                        simpleFoodItemProperties(6, 2)
//                                .build())));
//        WARPED_WART_OMELETTE = ITEMS.register("warped_wart_omelette",
//                () -> new WarpedWartOmeletteItem(basicItem().stacksTo(64).food(
//                        simpleFoodItemProperties(6, 2)
//                                .build())));
//        FULL_SPIDER_FEAST = ITEMS.register("full_spider_feast",
//                () -> new Item(basicItem().stacksTo(64).food(
//                        simpleFoodItemProperties(8, 5)
//                                .effect(() -> new MobEffectInstance(NOURISHMENT_EFFECT_SUPPLIER.get(), minToTick(5), 0), 1.0F)
//                                .effect(() -> new MobEffectInstance(BuiltInRegistries.MOB_EFFECT.wrapAsHolder(CLIMBING.get()), minToTick(5), 0), 1.0F)
//                                .build())));
//        LIQUID_VOID_TEA_DRINK = ITEMS.register("liquid_void_tea_drink",
//                () -> new LiquidVoidTeaDrinkItem(basicItem().stacksTo(16)
//                        .food(new FoodProperties.Builder()
//                                .nutrition(0)
//                                .alwaysEdible()
//                                .build())));
//
//        LICHS_CHAOS_STEW = ITEMS.register("lichs_chaos_stew",
//                () -> new LichsChaosStewItem(basicItem().craftRemainder(Items.BOWL).stacksTo(64).rarity(Rarity.EPIC).food(
//                        simpleFoodItemProperties(16, 12)
//                                .effect(() -> new MobEffectInstance(BuiltInRegistries.MOB_EFFECT.wrapAsHolder(SAVE_EFFECTS.get()), -1, 2), 1.0F)
//                                .effect(() -> new MobEffectInstance(BuiltInRegistries.MOB_EFFECT.wrapAsHolder(ModEffects.WIGHT_DENIAL.get()), minToTick(30), 0, false, false), 1.0F)
//                                .effect(() -> new MobEffectInstance(MobEffects.FIRE_RESISTANCE, -1, 0), 1.0F)
//                                .effect(() -> new MobEffectInstance(MobEffects.REGENERATION, minToTick(30), 2), 1.0F)
//                                .build())));
//        MAGIC_QUARTZ_COOKIE = ITEMS.register("magic_quartz_cookie",
//                () -> new MagicQuartzCookieItem(basicItem().stacksTo(64).food(
//                        simpleFoodItemProperties(8, 4)
//                                .build())));
//        SNAP_UNHOLY_TRIPE = ITEMS.register("snap_unholy_tripe",
//                () -> new SnapUnholyTripeItem(basicItem().stacksTo(16).rarity(Rarity.RARE).food(
//                        simpleFoodItemProperties(18, 20)
//                                .effect(() -> new MobEffectInstance(NOURISHMENT_EFFECT_SUPPLIER.get(), minToTick(30), 0), 1.0F)
//                                .effect(() -> new MobEffectInstance(MobEffects.SATURATION, sToTick(10), 0), 1.0F)
//                                .effect(() -> new MobEffectInstance(NOURISHMENT_EFFECT_SUPPLIER.get(), minToTick(30), 0), 1.0F)
//                                .effect(() -> new MobEffectInstance(BuiltInRegistries.MOB_EFFECT.wrapAsHolder(ModEffects.THE_PALE_MESSRNGER.get()), minToTick(5), 0), 1.0F)
//                                .effect(() -> new MobEffectInstance(MobEffects.FIRE_RESISTANCE, minToTick(30), 0), 1.0F)
//                                .build())));
//        SUNDAE_OF_THE_PHILOSOPHERS_POTION = ITEMS.register("sundae_of_the_philosophers_potion",
//                () -> new SundaeOfThePhilosophersPotionItem(basicItem().stacksTo(16).rarity(Rarity.EPIC).food(
//                        simpleFoodItemProperties(10, 6)
//                                .effect(() -> new MobEffectInstance(BuiltInRegistries.MOB_EFFECT.wrapAsHolder(SAVE_EFFECTS.get()), -1, 1, false, false), 1.0F)
//                                .effect(() -> new MobEffectInstance(BuiltInRegistries.MOB_EFFECT.wrapAsHolder(GOLD_TOUCHED.get()), minToTick(30), 0, false, false), 1.0F)
//                                .effect(() -> new MobEffectInstance(BuiltInRegistries.MOB_EFFECT.wrapAsHolder(SOUL_ARMOR.get()), -1, 1, false, false), 1.0F)
//                                .build())));
//        THE_BOX_OF_THE_DEAD = ITEMS.register("the_box_of_the_dead",
//                () -> new TheBoxOfTheDeadItem(basicItem().stacksTo(64).rarity(Rarity.UNCOMMON).food(
//                        simpleFoodItemProperties(6, 3)
//                                .effect(() -> new MobEffectInstance(BuiltInRegistries.MOB_EFFECT.wrapAsHolder(CURSED.get()), sToTick(20), 1), 1.0F)
//                                .build())));
//        RING_PACKED_VOID_GEL_JELLY = ITEMS.register("ring_packed_void_gel_jelly",
//                () -> new FoiledBowlFoodItem(basicItem().craftRemainder(Items.BOWL).stacksTo(64)
//                        .food(
//                                simpleFoodItemProperties(8, 4)
//                                        .effect(() -> new MobEffectInstance(BuiltInRegistries.MOB_EFFECT.wrapAsHolder(ModEffects.VOID_AFFIX.get()), sToTick(60), 0), 1.0F)
//                                        .effect(() -> new MobEffectInstance(NOURISHMENT_EFFECT_SUPPLIER.get(), sToTick(300), 0), 1.0F)
//                                        .effect(() -> new MobEffectInstance(MobEffects.HUNGER, sToTick(60), 4), 1.0F)
//                                        .build())));
//        STUFFED_TALL_SKULL_RICE = ITEMS.register("stuffed_tall_skull_rice",
//                () -> new StuffedTallSkullRiceItem(basicItem().craftRemainder(Items.BOWL).stacksTo(64)
//                        .food(
//                                simpleFoodItemProperties(8, 5)
//                                        .effect(() -> new MobEffectInstance(NOURISHMENT_EFFECT_SUPPLIER.get(), sToTick(60), 0), 1.0F)
//                                        .effect(() -> new MobEffectInstance(BuiltInRegistries.MOB_EFFECT.wrapAsHolder(PHOTOSYNTHESIS.get()), minToTick(5), 1), 1.0F)
//                                        .build())));
//
//        OMINOUS_RAMUNE = ITEMS.register("ominous_ramune",
//                () -> new GlassBottleFoodItem(basicItem().stacksTo(16).rarity(Rarity.UNCOMMON)
//                        .food(
//                                simpleFoodItemProperties(2, 1)
//                                        .effect(() -> new MobEffectInstance(BuiltInRegistries.MOB_EFFECT.wrapAsHolder(ModEffects.TINGLING.get()), sToTick(60), 0), 1.0F)
//                                        .build())));
//
//        BOAT_STUFFED_ROASTED_WARDEN_HEAD = ITEMS.register("boat_stuffed_roasted_warden_head",
//                () -> new BoatStuffedRoastedWardenItem(basicItem().stacksTo(1).rarity(Rarity.UNCOMMON).craftRemainder(Items.DARK_OAK_BOAT)
//                        .food(simpleFoodItemProperties(25, 20)
//                                .effect(() -> new MobEffectInstance(BuiltInRegistries.MOB_EFFECT.wrapAsHolder(SOUL_ARMOR.get()), minToTick(5), 3), 1.0F)
//                                .effect(() -> new MobEffectInstance(BuiltInRegistries.MOB_EFFECT.wrapAsHolder(ModEffects.HUNTING_DENIAL.get()), minToTick(10), 0), 1.0F)
//                                .effect(() -> new MobEffectInstance(MobEffects.REGENERATION, minToTick(5), 1), 1.0F)
//                                .effect(() -> new MobEffectInstance(MobEffects.DAMAGE_BOOST, minToTick(5), 1), 1.0F)
//                                .effect(() -> new MobEffectInstance(BuiltInRegistries.MOB_EFFECT.wrapAsHolder(ModEffects.WARDEN.get()), minToTick(10), 0), 1.0F)
//                                .build())));
//
//        BOAT_STUFFED_ROASTED_WARDEN_MEET = ITEMS.register("boat_stuffed_roasted_warden_meet",
//                () -> new BoatStuffedRoastedWardenItem(basicItem().stacksTo(1).rarity(Rarity.UNCOMMON).craftRemainder(Items.DARK_OAK_BOAT)
//                        .food(simpleFoodItemProperties(30, 25)
//                                .effect(() -> new MobEffectInstance(BuiltInRegistries.MOB_EFFECT.wrapAsHolder(SOUL_ARMOR.get()), minToTick(5), 3), 1.0F)
//                                .effect(() -> new MobEffectInstance(BuiltInRegistries.MOB_EFFECT.wrapAsHolder(ModEffects.HUNTING_DENIAL.get()), minToTick(10), 0), 1.0F)
//                                .effect(() -> new MobEffectInstance(MobEffects.REGENERATION, minToTick(5), 1), 1.0F)
//                                .effect(() -> new MobEffectInstance(MobEffects.DAMAGE_BOOST, minToTick(5), 1), 1.0F)
//                                .effect(() -> new MobEffectInstance(BuiltInRegistries.MOB_EFFECT.wrapAsHolder(ModEffects.WARDEN.get()), minToTick(10), 0), 1.0F)
//                                .build())));
//
//        BOAT_STUFFED_ROASTED_WARDEN_FLANK = ITEMS.register("boat_stuffed_roasted_warden_flank",
//                () -> new BoatStuffedRoastedWardenItem(basicItem().stacksTo(1).rarity(Rarity.UNCOMMON).craftRemainder(Items.DARK_OAK_BOAT)
//                        .food(simpleFoodItemProperties(40, 30)
//                                .effect(() -> new MobEffectInstance(BuiltInRegistries.MOB_EFFECT.wrapAsHolder(SOUL_ARMOR.get()), minToTick(5), 3), 1.0F)
//                                .effect(() -> new MobEffectInstance(BuiltInRegistries.MOB_EFFECT.wrapAsHolder(ModEffects.HUNTING_DENIAL.get()), minToTick(15), 0), 1.0F)
//                                .effect(() -> new MobEffectInstance(MobEffects.REGENERATION, minToTick(7.5f), 1), 1.0F)
//                                .effect(() -> new MobEffectInstance(MobEffects.DAMAGE_BOOST, minToTick(7.5f), 1), 1.0F)
//                                .effect(() -> new MobEffectInstance(BuiltInRegistries.MOB_EFFECT.wrapAsHolder(ModEffects.WARDEN.get()), minToTick(15), 0), 1.0F)
//                                .build())));
//
//        ANCIENT_ENCHANTED_GOLDEN_APPLE = ITEMS.register("ancient_enchanted_golden_apple",
//                () -> new AncientEnchantedGoldenAppleItem(basicItem().stacksTo(64).rarity(Rarity.EPIC)
//                        .food(simpleFoodItemProperties(6, 6)
//                                .effect(() -> new MobEffectInstance(MobEffects.DAMAGE_RESISTANCE, minToTick(5), 0), 1.0F)
//                                .effect(() -> new MobEffectInstance(MobEffects.ABSORPTION, minToTick(2), 3), 1.0F)
//                                .effect(() -> new MobEffectInstance(MobEffects.REGENERATION, 600, 5), 1.0F)
//                                .effect(() -> new MobEffectInstance(MobEffects.FIRE_RESISTANCE, minToTick(5), 0), 1.0F)
//                                .build())));
//
//        ROAST_LAOWANG = ITEMS.register("roast_laowang",
//                () -> new RoastLaowangItem(basicItem().stacksTo(64).rarity(Rarity.EPIC)
//                        .food(simpleFoodItemProperties(20, 15)
//                                .effect(() -> new MobEffectInstance(NOURISHMENT_EFFECT_SUPPLIER.get(), minToTick(20), 0), 1.0F)
//                                .build())));
//
//        POLARICE = ITEMS.register("polarice",
//                () -> new PolariceItem(basicItem().stacksTo(64).rarity(Rarity.EPIC)
//                        .food(simpleFoodItemProperties(10, 4)
//                                .effect(() -> new MobEffectInstance(ILLAGUE.get(), minToTick(5), 0), 1.0F)
//                                .effect(() -> new MobEffectInstance(MobEffects.DIG_SPEED, minToTick(5), 2), 1.0F)
//                                .build())));
//        METAMORPHIC_SCENT_FRUIT = ITEMS.register("metamorphic_scent_fruit",
//                () -> simpleFoodItem(10, 8, false));
//
//        FORBIDDDEN_SOUP_BUN = ITEMS.register("forbidden_soup_bun",
//                () -> new ForbiddenSoupBunItem(basicItem().stacksTo(64).rarity(Rarity.UNCOMMON)
//                        .food(simpleFoodItemProperties(13, 5)
//                                .effect(() -> new MobEffectInstance(BuiltInRegistries.MOB_EFFECT.wrapAsHolder(NYCTOPHOBIA.get()), 600, 0), 0.3F)
//                                .effect(() -> new MobEffectInstance(BuiltInRegistries.MOB_EFFECT.wrapAsHolder(SENSE_LOSS.get()), 600, 0), 0.7F)
//                                .build())));
//
//        HIDDEN_PANCAKE = ITEMS.register("hidden_pancake",
//                () -> new HiddenPancakeItem(basicItem().stacksTo(64).rarity(Rarity.RARE)
//                        .food(simpleFoodItemProperties(15, 7)
//                                .effect(() -> new MobEffectInstance(BuiltInRegistries.MOB_EFFECT.wrapAsHolder(IRON_HIDE.get()), minToTick(1), 4), 1.0F)
//                                .effect(() -> new MobEffectInstance(MobEffects.DAMAGE_BOOST, minToTick(2), 1), 1.0F)
//                                .build())));
//
//        CREAMY_BERRY_FISH_PASTE_DUMPLING_WITH_CHOCOLATE_SAUCE = ITEMS.register("creamy_berry_fish_paste_dumpling_with_chocolate_sauce",
//                () -> new Item(
//                        basicItem().stacksTo(64)
//                                .food(simpleFoodItemProperties(20, 10)
//                                        .effect(() -> new MobEffectInstance(MobEffects.WITHER, minToTick(1), 4), 1.0F)
//                                        .effect(() -> new MobEffectInstance(MobEffects.CONFUSION, minToTick(2), 9), 1.0F)
//                                        .effect(() -> new MobEffectInstance(BuiltInRegistries.MOB_EFFECT.wrapAsHolder(SENSE_LOSS.get()), minToTick(1), 0), 1.0F)
//                                        .build()
//                                )
//                ) {
//                    @Override
//                    public int getUseDuration(ItemStack stack, LivingEntity entity) {
//                        return (int) (32 * 4);
//                    }
//                });
//
//        OBSIDIAN_THICK_SOUP = ITEMS.register("obsidian_thick_soup",
//                () -> new BowlFoodItem(basicItem().stacksTo(64).rarity(Rarity.EPIC)
//                        .food(simpleFoodItemProperties(6, 3)
//                                .effect(() -> new MobEffectInstance(NOURISHMENT_EFFECT_SUPPLIER.get(), minToTick(5), 0), 1.0F)
//                                .effect(() -> new MobEffectInstance(FIERY_AURA_SUPPLIER.get(), 1320, 0), 1.0F)
//                                .effect(() -> new MobEffectInstance(BuiltInRegistries.MOB_EFFECT.wrapAsHolder(FLAME_HANDS.get()), 1320, 0), 1.0F)
//                                .effect(() -> new MobEffectInstance(BuiltInRegistries.MOB_EFFECT.wrapAsHolder(ModEffects.THE_PALE_MESSRNGER.get()), 120, 0), 1.0F)
//                                .build())));
//
//        SHAWARMA = ITEMS.register("shawarma",
//                () -> new Item(basicItem().stacksTo(64).rarity(Rarity.COMMON)
//                        .food(simpleFoodItemProperties(16, 12.5f)
//                                .effect(() -> new MobEffectInstance(NOURISHMENT_EFFECT_SUPPLIER.get(), minToTick(10), 0), 1.0F)
//                                .effect(() -> new MobEffectInstance(MobEffects.DAMAGE_BOOST, minToTick(2), 0), 1.0F)
//                                .effect(() -> new MobEffectInstance(COMFORT_EFFECT_SUPPLIER.get(), minToTick(2.5f), 0), 1.0F)
//                                .build())));
//
//        RAKI = ITEMS.register("raki",
//                () -> new GlassBottleFoodItem(basicItem().stacksTo(64)
//                        .food(simpleFoodItemProperties(2, 1)
//                                .effect(() -> new MobEffectInstance(MobEffects.MOVEMENT_SPEED, 600, 1), 1.0F)
//                                .effect(() -> new MobEffectInstance(BuiltInRegistries.MOB_EFFECT.wrapAsHolder(ModEffects.TINGLING.get()), 600, 0), 1.0F)
//                                .build())));
//
//        MENEMEN_WITH_BREAD = ITEMS.register("menemen_with_bread",
//                () -> new Item(basicItem().stacksTo(64).rarity(Rarity.COMMON).craftRemainder(Items.BREAD)
//                        .food(simpleFoodItemProperties(10, 6)
//                                .effect(() -> new MobEffectInstance(NOURISHMENT_EFFECT_SUPPLIER.get(), minToTick(3), 0), 1.0F)
//                                .effect(() -> new MobEffectInstance(COMFORT_EFFECT_SUPPLIER.get(), minToTick(3), 0), 1.0F)
//                                .effect(() -> new MobEffectInstance(PHOTOSYNTHESIS_SUPPLIER.get(), 600, 0), 1.0F)
//                                .effect(() -> new MobEffectInstance(BuiltInRegistries.MOB_EFFECT.wrapAsHolder(ModEffects.WIGHT_DENIAL.get()), minToTick(30), 0), 1.0F)
//                                .build())));
//
//        BAKLAVA = ITEMS.register("baklava",
//                () -> new BaklavaItem(basicItem().stacksTo(64).rarity(Rarity.EPIC)
//                        .food(simpleFoodItemProperties(6, 3)
//                                .effect(() -> new MobEffectInstance(MobEffects.REGENERATION, sToTick(10), 0), 1.0F)
//                                .effect(() -> new MobEffectInstance(NOURISHMENT_EFFECT_SUPPLIER.get(), sToTick(30), 0), 1.0F)
//                                .effect(() -> new MobEffectInstance(COMFORT_EFFECT_SUPPLIER.get(), sToTick(15), 0), 1.0F)
//                                .build())));
//
//        BISCAT = ITEMS.register("biscat",
//                () -> new BiscatItem(basicItem().stacksTo(64).rarity(Rarity.COMMON)
//                        .food(simpleFoodItemProperties(6, 2)
//                                .effect(() -> new MobEffectInstance(NOURISHMENT_EFFECT_SUPPLIER.get(), minToTick(5), 0), 1.0F)
//                                .effect(() -> new MobEffectInstance(MobEffects.LUCK, minToTick(1), 0), 1.0F)
//                                .build())));
//
//        RUBY_SYRUP = ITEMS.register("ruby_syrup",
//                () -> new GlassBottleFoodItem(basicItem().stacksTo(16).rarity(Rarity.COMMON)
//                        .food(simpleFoodItemProperties(5, 4)
//                                .effect(() -> new MobEffectInstance(BuiltInRegistries.MOB_EFFECT.wrapAsHolder(ModEffects.SPELL_MASTERY.get()), minToTick(1), 0), 1.0F)
//                                .effect(() -> new MobEffectInstance(BuiltInRegistries.MOB_EFFECT.wrapAsHolder(ModEffects.SPELL_DURATION.get()), minToTick(1), 0), 1.0F)
//                                .effect(() -> new MobEffectInstance(NOURISHMENT_EFFECT_SUPPLIER.get(), minToTick(1), 0), 1.0F)
//                                .build())));
//
//        // ==================== 种子物品 ====================
//        ECTOPLASMIC_MELON_SEEDS = ITEMS.register("ectoplasmic_melon_seeds",
//                () -> new ItemNameBlockItem(ModBlocks.ECTOPLASMIC_MELON_STEM.get(),
//                        new Item.Properties()
//                ));
//        METAMORPHIC_SCENT_GRASS_SEEDS = ITEMS.register("metamorphic_scent_grass_seeds",
//                () -> new ItemNameBlockItem(ModBlocks.METAMORPHIC_SCENT_GRASS.get(),
//                        new Item.Properties()
//                ));
//        // ==================== 杂项物品 ====================
//        GHOST_FARMER_SPAWN_EGG = ITEMS.register("ghost_farmer_spawn_egg",
//                () -> new SpawnEggItem(ModEntities.GHOST_FARMER.get(), 0xFFFFFF, 0xFFFFFF, new Item.Properties()));
//
//        DOLL_ITEM = registerWithTab("doll_item", DollEntityItem::new);
//    }



    // 注册方法：供主类调用，将 DeferredRegister 绑定到 mod 事件总线
    public static void register(IEventBus modEventBus) {
        ITEMS.register(modEventBus);
    }
}