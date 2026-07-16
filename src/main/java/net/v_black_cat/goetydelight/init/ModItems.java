package net.v_black_cat.goetydelight.init;

import com.Polarice3.Goety.common.effects.GoetyEffects;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.Rarity;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.bus.api.IEventBus;
import net.v_black_cat.goetydelight.GoetyDelight;
import net.v_black_cat.goetydelight.item.CustomDrinkItem;
import net.v_black_cat.goetydelight.item.food.*;
import vectorwing.farmersdelight.common.registry.ModEffects;

import static net.v_black_cat.goetydelight.util.TickConverterUtil.minToTick;
import static vectorwing.farmersdelight.common.registry.ModItems.basicItem;

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
    public static final DeferredItem<Item> GOETYDELIGHT_ICON;
    public static final DeferredItem<Item> TAINTED_DRINK;
    public static final DeferredItem<Item> PURE_DRINK;
    public static final DeferredItem<Item> REJECTED_DARK_MEAT_SOUP;
//    public static final DeferredItem<Item> SIBLING_SUNDAE;
    public static final DeferredItem<Item> PROMOTION_HARD_CANDY;
    //    public static final DeferredItem<Item> CUP;
    public static final DeferredItem<Item> TOXIC_MEAL;
//    public static final DeferredItem<Item> POACHED_NETHER_WART_EGG;
    public static final DeferredItem<Item> ECTOPLASM_JELLY;
    //    public static final DeferredItem<Item> ROASTED_CORPSE_MAGGOTS;
//    public static final DeferredItem<Item> WHITE_SHARK_CANDY;
    public static final DeferredItem<Item> WHITE_SHARK_SUGAR_PACK;
    public static final DeferredItem<Item> SUNSHINE_SUGAR_BUN;
    public static final DeferredItem<Item> CANDY_FISH;
    public static final DeferredItem<Item> GRAPE_SLUSH;
    public static final DeferredItem<Item> FROG_LEG_SANDWICH;
    public static final DeferredItem<Item> SPIDER_EGG_BUBBLE_TEA;
    public static final DeferredItem<Item> SPIDER_EGG_BUBBLE_TEA_2;
    public static final DeferredItem<Item> SAUCE_GRILLED_CANDY_FISH;
    public static final DeferredItem<Item> CRYING_SHARK_SUGAR_PACK;
    //    public static final DeferredItem<Item> SEVEN_LEAF_PUDDING;
    public static final DeferredItem<Item> BEAR_PAW;
    public static final DeferredItem<Item> CAKE;
//    public static final DeferredItem<Item> OMINOUS_ICE_CREAM;
    public static final DeferredItem<Item> ECTOPLASMIC_MELON;
    public static final DeferredItem<Item> BLUE_ECTOPLASMIC_SUNDAE;
    public static final DeferredItem<Item> SKULL_SHOT;
    public static final DeferredItem<Item> NIGHT_HEART_PEA_SOUP;
    public static final DeferredItem<Item> POACHED_SPIDER_EGG;
    public static final DeferredItem<Item> GRILL_FROG_LEG;
    public static final DeferredItem<Item> FRENZIED_FUNGUS_POP_ROCKS;
    public static final DeferredItem<Item> SOUL_CONVERGENCE_ROOM;
    public static final DeferredItem<Item> SOUL_CONVERGENCE_ROOM_2;
    public static final DeferredItem<Item> BONE_LORD_ASH_RICE;
//    public static final DeferredItem<Item> RUBY_HARD_CANDY;
    public static final DeferredItem<Item> CRISP_BISCUIT;
    public static final DeferredItem<Item> ROTTEN_CORPSE_MAGGOT_FEAST;
    public static final DeferredItem<Item> CORPSE_MAGGOT;
    public static final DeferredItem<Item> QUICK_GROWING_SEED_POPCORN;
    public static final DeferredItem<Item> NETHER_STYLE_FRIED_EGG_SANDWICH;
    public static final DeferredItem<Item> EXOTIC_BREAKFAST;
    //    public static final DeferredItem<Item> JUNGLE_SALAD;
    public static final DeferredItem<Item> BOILING_BLOOD_BREW;
    public static final DeferredItem<Item> ASCENSION_MOONCAKE;
    public static final DeferredItem<Item> VILLAGERS_FEAST;
    public static final DeferredItem<Item> CHERRY_BLOSSOM_CAKE;
    public static final DeferredItem<Item> NETHER_WART_OMELETTE;
    public static final DeferredItem<Item> WARPED_WART_OMELETTE;
    public static final DeferredItem<Item> FULL_SPIDER_FEAST;
    public static final DeferredItem<Item> LIQUID_VOID_TEA_DRINK;

    public static final DeferredItem<Item> LICHS_CHAOS_STEW;
//    public static final DeferredItem<Item> MAGIC_QUARTZ_COOKIE;
    public static final DeferredItem<Item> SNAP_UNHOLY_TRIPE;
//    public static final DeferredItem<Item> SUNDAE_OF_THE_PHILOSOPHERS_POTION;
    public static final DeferredItem<Item> THE_BOX_OF_THE_DEAD;
//    public static final DeferredItem<Item> RING_PACKED_VOID_GEL_JELLY;
    public static final DeferredItem<Item> STUFFED_TALL_SKULL_RICE;
    public static final DeferredItem<Item> OMINOUS_RAMUNE;
    public static final DeferredItem<Item> BOAT_STUFFED_ROASTED_WARDEN_HEAD;
    public static final DeferredItem<Item> BOAT_STUFFED_ROASTED_WARDEN_MEET;
    public static final DeferredItem<Item> BOAT_STUFFED_ROASTED_WARDEN_FLANK;
//    public static final DeferredItem<Item> ANCIENT_ENCHANTED_GOLDEN_APPLE;
//    public static final DeferredItem<Item> NOT_ANYTHING;
//    public static final DeferredItem<Item> ROAST_LAOWANG;
//    public static final DeferredItem<Item> POLARICE;
    public static final DeferredItem<Item> METAMORPHIC_SCENT_FRUIT;
    public static final DeferredItem<Item> FORBIDDDEN_SOUP_BUN;
//    public static final DeferredItem<Item> HIDDEN_PANCAKE;
//    public static final DeferredItem<Item> CREAMY_BERRY_FISH_PASTE_DUMPLING_WITH_CHOCOLATE_SAUCE;
//    public static final DeferredItem<Item> OBSIDIAN_THICK_SOUP;
    public static final DeferredItem<Item> SHAWARMA;
    public static final DeferredItem<Item> RAKI;
    public static final DeferredItem<Item> MENEMEN_WITH_BREAD;
    public static final DeferredItem<Item> BAKLAVA;
//    public static final DeferredItem<Item> CUSTOM_DOLL;
    public static final DeferredItem<Item> BISCAT;
    public static final DeferredItem<Item> RUBY_SYRUP;
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
    static {
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
        GOETYDELIGHT_ICON = ITEMS.register("goetydelight_icon",
                () -> simpleFoodItem(666, 666,true));
//
        TAINTED_DRINK = ITEMS.register("tainted_drink",
                () -> new CustomDrinkItem(basicItem()
                        .stacksTo(1)
                        .rarity(Rarity.RARE)
                        .food(
                                ModFoods.TAINTED_DRINK
                        )
                )
        );
//
        PURE_DRINK = ITEMS.register("pure_drink",
                () -> {
                    FoodProperties food = ModFoods.TAINTED_DRINK;
                    return new CustomDrinkItem(basicItem().stacksTo(1).rarity(Rarity.RARE).food(food)) {
                        @Override
                        public boolean isFoil(ItemStack pStack) {
                            return true;
                        }
                    };
                });
//
//        CUP = ITEMS.register("eternal_refusal_of_black_meat_soup",
//                () -> new EternalRefusalOfBlackMeatSoupItem(basicItem().stacksTo(1).rarity(Rarity.RARE).food(
//                        ModFoods.CUP
//                )));
//
        REJECTED_DARK_MEAT_SOUP = ITEMS.register("rejected_dark_meat_soup",
                () -> new RejectedDarkMeatSoupItem(basicItem().stacksTo(16).food(
                        ModFoods.REJECTED_DARK_MEAT_SOUP
                )));
//
        PROMOTION_HARD_CANDY = ITEMS.register("promotion_hard_candy",
                () -> new Item(basicItem().stacksTo(1).food(ModFoods.PROMOTION_HARD_CANDY)));
//
        TOXIC_MEAL = ITEMS.register("toxic_meal",
                () -> new ToxicMealItem(basicItem().stacksTo(16).food(
                        ModFoods.TOXIC_MEAL
                )));
//        POACHED_NETHER_WART_EGG = ITEMS.register("poached_nether_wart_egg",
//                () -> new PoachedNetherWartEggItem(basicItem().stacksTo(16).food(
//                        ModFoods.POACHED_NETHER_WART_EGG
//                )));
        ECTOPLASM_JELLY = ITEMS.register("ectoplasm_jelly",
                () -> new Item(basicItem()
                        .stacksTo(1)
                        .rarity(Rarity.RARE)
                        .food(ModFoods.ECTOPLASM_JELLY)
                )
        );
        FROG_LEG_SANDWICH = ITEMS.register("frog_leg_sandwich",
                () -> new Item(basicItem().stacksTo(64).food(ModFoods.FROG_LEG_SANDWICH)));
//
        SPIDER_EGG_BUBBLE_TEA_2 = ITEMS.register("spider_egg_bubble_tea_2",
                () -> new Item(basicItem().stacksTo(1).food(ModFoods.SPIDER_EGG_BUBBLE_TEA_2)));
//
//        // 特殊效果食物物品初始化
//
        ASCENSION_MOONCAKE = ITEMS.register("ascension_mooncake",
                () -> new Item(basicItem().stacksTo(1).rarity(Rarity.EPIC).food(
                        ModFoods.ASCENSION_MOONCAKE
                )));
//
        SPIDER_EGG_BUBBLE_TEA = ITEMS.register("spider_egg_bubble_tea",
                () -> new CustomDrinkItem(basicItem().stacksTo(16).food(
                        ModFoods.SPIDER_EGG_BUBBLE_TEA
                )));

        BOILING_BLOOD_BREW = ITEMS.register("boiling_blood_brew",
                () -> new CustomDrinkItem(basicItem().stacksTo(16).food(
                        ModFoods.BOILING_BLOOD_BREW
                )));
//
        NETHER_STYLE_FRIED_EGG_SANDWICH = ITEMS.register("nether_style_fried_egg_sandwich",
                () -> new Item(basicItem().stacksTo(64).food(
                        ModFoods.NETHER_STYLE_FRIED_EGG_SANDWICH
                )));
//
        EXOTIC_BREAKFAST = ITEMS.register("exotic_breakfast",
                () -> new Item(basicItem().stacksTo(16).food(
                        ModFoods.EXOTIC_BREAKFAST
                )));
//
        VILLAGERS_FEAST = ITEMS.register("villagers_feast",
                () -> new Item(basicItem().stacksTo(16).food(
                        ModFoods.VILLAGERS_FEAST
                )));
//
//        JUNGLE_SALAD = ITEMS.register("jungle_salad",
//                () -> new BowlFoodItem(basicItem().stacksTo(16).food(
//                        ModFoods.JUNGLE_SALAD
//                )));
//
        QUICK_GROWING_SEED_POPCORN = ITEMS.register("quick_growing_seed_popcorn",
                () -> new Item(basicItem().stacksTo(16).food(ModFoods.QUICK_GROWING_SEED_POPCORN)));
//
        SAUCE_GRILLED_CANDY_FISH = ITEMS.register("sauce_grilled_candy_fish",
                () -> new SauceGrilledCandyFishItem(basicItem().stacksTo(8).food(
                        ModFoods.SAUCE_GRILLED_CANDY_FISH
                )));
//
        CANDY_FISH = ITEMS.register("candy_fish",
                () -> new Item(basicItem().stacksTo(8).food(
                        ModFoods.CANDY_FISH
                )));
//
        WHITE_SHARK_SUGAR_PACK = ITEMS.register("sugar_pack",
                () -> new Item(basicItem().stacksTo(16).food(
                        ModFoods.WHITE_SHARK_SUGAR_PACK
                )));
//
//        WHITE_SHARK_CANDY = ITEMS.register("sugar_scepter",
//                () -> new SugarScepterItem(basicItem().stacksTo(8).rarity(Rarity.UNCOMMON).food(
//                        ModFoods.WHITE_SHARK_CANDY
//                )));
//
//        SIBLING_SUNDAE = ITEMS.register("possible_holy_representative",
//                () -> new SiblingSundaeItem(basicItem().stacksTo(8).rarity(Rarity.UNCOMMON).food(
//                        ModFoods.SIBLING_SUNDAE
//                )));
//
//        ROASTED_CORPSE_MAGGOTS = ITEMS.register("roasted_corpse_maggots",
//                () -> new RoastedCorpseMaggotsitem(basicItem().craftRemainder(Items.BOWL).stacksTo(16).food(
//                        ModFoods.ROASTED_CORPSE_MAGGOTS
//                )));
//
        CORPSE_MAGGOT = ITEMS.register("corpse_maggot",
                () -> new CorpseMaggotItem(basicItem().stacksTo(64).food(
                        ModFoods.CORPSE_MAGGOT
                )));
//
        CRYING_SHARK_SUGAR_PACK = ITEMS.register("cry_sugar_pack",
                () -> new Item(basicItem().stacksTo(64).food(
                        ModFoods.CRYING_SHARK_SUGAR_PACK
                )));
//
        SUNSHINE_SUGAR_BUN = ITEMS.register("sunshine_sugar_bun",
                () -> new Item(basicItem().stacksTo(64).food(
                        ModFoods.SUNSHINE_SUGAR_BUN
                )));
//
        GRAPE_SLUSH = ITEMS.register("grape_slush",
                () -> new NoGlassBottleDrinkItem(basicItem().stacksTo(64).food(
                        ModFoods.GRAPE_SLUSH
                )));
//
//        SEVEN_LEAF_PUDDING = ITEMS.register("sweet_berry_pudding",
//                () -> new SevenLeafPuddingItem(basicItem().stacksTo(64).craftRemainder(Items.BOWL).food(
//                        ModFoods.SEVEN_LEAF_PUDDING
//                )));
//
        BEAR_PAW = ITEMS.register("bear_paw",
                () -> new Item(basicItem().stacksTo(64).food(
                        ModFoods.BEAR_PAW
                )));
        CAKE = ITEMS.register("royal_cake",
                () -> new CakeItem(basicItem().stacksTo(64).rarity(Rarity.RARE).food(
                        ModFoods.CAKE
                )));
//        OMINOUS_ICE_CREAM = ITEMS.register("ominous_ice_cream",
//                () -> new OminousIceCreamItem(basicItem().stacksTo(64).rarity(Rarity.UNCOMMON).food(
//                        ModFoods.OMINOUS_ICE_CREAM
//                )));
        ECTOPLASMIC_MELON = ITEMS.register("ectoplasmic_melon",
                () -> new Item(basicItem().stacksTo(64).food(
                        ModFoods.ECTOPLASMIC_MELON
                )));
        BLUE_ECTOPLASMIC_SUNDAE = ITEMS.register("blue_ectoplasmic_sundae",
                () -> new Item(basicItem().stacksTo(64).food(
                        ModFoods.BLUE_ECTOPLASMIC_SUNDAE
                )));
//
        SKULL_SHOT = ITEMS.register("skull_shot",
                () -> new NoGlassBottleDrinkItem(basicItem().stacksTo(16).food(
                        ModFoods.SKULL_SHOT
                )));
//
        NIGHT_HEART_PEA_SOUP = ITEMS.register("night_heart_pea_soup",
                () -> new NightHeartPeaSoupItem(basicItem().craftRemainder(Items.GLASS_BOTTLE).stacksTo(64).rarity(Rarity.UNCOMMON).food(
                        ModFoods.NIGHT_HEART_PEA_SOUP
                )));
        POACHED_SPIDER_EGG = ITEMS.register("poached_spider_egg",
                () -> new Item(basicItem().stacksTo(64).food(
                        ModFoods.POACHED_SPIDER_EGG
                )));
        GRILL_FROG_LEG = ITEMS.register("grill_frog_leg",
                () -> new Item(basicItem().stacksTo(64).food(
                        ModFoods.GRILL_FROG_LEG
                )));
        FRENZIED_FUNGUS_POP_ROCKS = ITEMS.register("frenzied_fungus_pop_rocks",
                () -> new FrenziedFungusPopRocksItem(basicItem().stacksTo(64).food(
                        ModFoods.FRENZIED_FUNGUS_POP_ROCKS
                )));
        SOUL_CONVERGENCE_ROOM = ITEMS.register("gathering_soul_embryos",
                () -> new Item(basicItem().stacksTo(64).food(
                        ModFoods.SOUL_CONVERGENCE_ROOM
                )));
        SOUL_CONVERGENCE_ROOM_2 = ITEMS.register("soul_convergence_room",
                () -> new Item(basicItem().stacksTo(64).rarity(Rarity.UNCOMMON).food(
                        ModFoods.SOUL_CONVERGENCE_ROOM_2
                )));
        BONE_LORD_ASH_RICE = ITEMS.register("bone_lord_ash_rice",
                () -> new BoneLordAshRiceItem(basicItem().craftRemainder(Items.BOWL).stacksTo(64).food(
                        ModFoods.BONE_LORD_ASH_RICE
                )));
//        RUBY_HARD_CANDY = ITEMS.register("ruby_hard_candy",
//                () -> new RubyHardCandyItem(basicItem().stacksTo(64).rarity(Rarity.UNCOMMON).food(
//                        ModFoods.RUBY_HARD_CANDY
//                )));
        CRISP_BISCUIT = ITEMS.register("crisp_biscuit",
                () -> new CrispBiscuitItem(basicItem().stacksTo(64).food(
                        ModFoods.CRISP_BISCUIT
                )));
        ROTTEN_CORPSE_MAGGOT_FEAST = ITEMS.register("rotten_corpse_maggot_feast",
                () -> new RottenCorpseMaggotFeastItem(basicItem().stacksTo(16).food(
                        ModFoods.ROTTEN_CORPSE_MAGGOT_FEAST
                )));
        CHERRY_BLOSSOM_CAKE = ITEMS.register("cherry_blossom_cake",
                () -> new CherryBlossomCakeItem(basicItem().stacksTo(64).rarity(Rarity.UNCOMMON).food(
                        ModFoods.CHERRY_BLOSSOM_CAKE
                )));
        NETHER_WART_OMELETTE = ITEMS.register("nether_wart_omelette",
                () -> new NetherWartOmeletteItem(basicItem().stacksTo(64).food(
                        ModFoods.NETHER_WART_OMELETTE
                )));
        WARPED_WART_OMELETTE = ITEMS.register("warped_wart_omelette",
                () -> new WarpedWartOmeletteItem(basicItem().stacksTo(64).food(
                        ModFoods.WARPED_WART_OMELETTE
                )));
        FULL_SPIDER_FEAST = ITEMS.register("full_spider_feast",
                () -> new Item(basicItem().stacksTo(64).food(
                        ModFoods.FULL_SPIDER_FEAST
                )));
        LIQUID_VOID_TEA_DRINK = ITEMS.register("liquid_void_tea_drink",
                () -> new LiquidVoidTeaDrinkItem(basicItem().stacksTo(16)
                        .food(ModFoods.LIQUID_VOID_TEA_DRINK)));
//
        LICHS_CHAOS_STEW = ITEMS.register("lichs_chaos_stew",
                () -> new LichsChaosStewItem(basicItem().craftRemainder(Items.BOWL).stacksTo(64).rarity(Rarity.EPIC).food(
                        ModFoods.LICHS_CHAOS_STEW
                )));
//        MAGIC_QUARTZ_COOKIE = ITEMS.register("magic_quartz_cookie",
//                () -> new MagicQuartzCookieItem(basicItem().stacksTo(64).food(
//                        ModFoods.MAGIC_QUARTZ_COOKIE
//                )));
        SNAP_UNHOLY_TRIPE = ITEMS.register("snap_unholy_tripe",
                () -> new SnapUnholyTripeItem(basicItem().stacksTo(16).rarity(Rarity.RARE).food(
                        ModFoods.SNAP_UNHOLY_TRIPE
                )));
//        SUNDAE_OF_THE_PHILOSOPHERS_POTION = ITEMS.register("sundae_of_the_philosophers_potion",
//                () -> new SundaeOfThePhilosophersPotionItem(basicItem().stacksTo(16).rarity(Rarity.EPIC).food(
//                        ModFoods.SUNDAE_OF_THE_PHILOSOPHERS_POTION
//                )));
        THE_BOX_OF_THE_DEAD = ITEMS.register("the_box_of_the_dead",
                () -> new TheBoxOfTheDeadItem(basicItem().stacksTo(64).rarity(Rarity.UNCOMMON).food(
                        ModFoods.THE_BOX_OF_THE_DEAD
                )));
//        RING_PACKED_VOID_GEL_JELLY = ITEMS.register("ring_packed_void_gel_jelly",
//                () -> new FoiledBowlFoodItem(basicItem().craftRemainder(Items.BOWL).stacksTo(64)
//                        .food(ModFoods.RING_PACKED_VOID_GEL_JELLY)));
        STUFFED_TALL_SKULL_RICE = ITEMS.register("stuffed_tall_skull_rice",
                () -> new StuffedTallSkullRiceItem(basicItem().craftRemainder(Items.BOWL).stacksTo(64)
                        .food(ModFoods.STUFFED_TALL_SKULL_RICE)));
//
        OMINOUS_RAMUNE = ITEMS.register("ominous_ramune",
                () -> new GlassBottleFoodItem(basicItem().stacksTo(16).rarity(Rarity.UNCOMMON)
                        .food(ModFoods.OMINOUS_RAMUNE)));
//
        BOAT_STUFFED_ROASTED_WARDEN_HEAD = ITEMS.register("boat_stuffed_roasted_warden_head",
                () -> new BoatStuffedRoastedWardenItem(basicItem().stacksTo(1).rarity(Rarity.UNCOMMON).craftRemainder(Items.DARK_OAK_BOAT)
                        .food(ModFoods.BOAT_STUFFED_ROASTED_WARDEN_HEAD)));

        BOAT_STUFFED_ROASTED_WARDEN_MEET = ITEMS.register("boat_stuffed_roasted_warden_meet",
                () -> new BoatStuffedRoastedWardenItem(basicItem().stacksTo(1).rarity(Rarity.UNCOMMON).craftRemainder(Items.DARK_OAK_BOAT)
                        .food(ModFoods.BOAT_STUFFED_ROASTED_WARDEN_MEET)));

        BOAT_STUFFED_ROASTED_WARDEN_FLANK = ITEMS.register("boat_stuffed_roasted_warden_flank",
                () -> new BoatStuffedRoastedWardenItem(basicItem().stacksTo(1).rarity(Rarity.UNCOMMON).craftRemainder(Items.DARK_OAK_BOAT)
                        .food(ModFoods.BOAT_STUFFED_ROASTED_WARDEN_FLANK)));
//
//        ANCIENT_ENCHANTED_GOLDEN_APPLE = ITEMS.register("ancient_enchanted_golden_apple",
//                () -> new AncientEnchantedGoldenAppleItem(basicItem().stacksTo(64).rarity(Rarity.EPIC)
//                        .food(ModFoods.ANCIENT_ENCHANTED_GOLDEN_APPLE)));
//
//        ROAST_LAOWANG = ITEMS.register("roast_laowang",
//                () -> new RoastLaowangItem(basicItem().stacksTo(64).rarity(Rarity.EPIC)
//                        .food(ModFoods.ROAST_LAOWANG)));
//
//        POLARICE = ITEMS.register("polarice",
//                () -> new PolariceItem(basicItem().stacksTo(64).rarity(Rarity.EPIC)
//                        .food(ModFoods.POLARICE)));
        METAMORPHIC_SCENT_FRUIT = ITEMS.register("metamorphic_scent_fruit",
                () -> new Item(basicItem().stacksTo(64).food(ModFoods.METAMORPHIC_SCENT_FRUIT)));
//
        FORBIDDDEN_SOUP_BUN = ITEMS.register("forbidden_soup_bun",
                () -> new ForbiddenSoupBunItem(basicItem().stacksTo(64).rarity(Rarity.UNCOMMON)
                        .food(ModFoods.FORBIDDDEN_SOUP_BUN)));
//
//        HIDDEN_PANCAKE = ITEMS.register("hidden_pancake",
//                () -> new HiddenPancakeItem(basicItem().stacksTo(64).rarity(Rarity.RARE)
//                        .food(ModFoods.HIDDEN_PANCAKE)));
//
//        CREAMY_BERRY_FISH_PASTE_DUMPLING_WITH_CHOCOLATE_SAUCE = ITEMS.register("creamy_berry_fish_paste_dumpling_with_chocolate_sauce",
//                () -> new Item(
//                        basicItem().stacksTo(64)
//                                .food(ModFoods.CREAMY_BERRY_FISH_PASTE_DUMPLING_WITH_CHOCOLATE_SAUCE)
//                ) {
//                    @Override
//                    public int getUseDuration(ItemStack stack, LivingEntity entity) {
//                        return (int) (32 * 4);
//                    }
//                });
//
//        OBSIDIAN_THICK_SOUP = ITEMS.register("obsidian_thick_soup",
//                () -> new BowlFoodItem(basicItem().stacksTo(64).rarity(Rarity.EPIC)
//                        .food(ModFoods.OBSIDIAN_THICK_SOUP)));
//
        SHAWARMA = ITEMS.register("shawarma",
                () -> new Item(basicItem().stacksTo(64).rarity(Rarity.COMMON)
                        .food(ModFoods.SHAWARMA)));
//
        RAKI = ITEMS.register("raki",
                () -> new GlassBottleFoodItem(basicItem().stacksTo(64)
                        .food(ModFoods.RAKI)));
//
        MENEMEN_WITH_BREAD = ITEMS.register("menemen_with_bread",
                () -> new Item(basicItem().stacksTo(64).rarity(Rarity.COMMON).craftRemainder(Items.BREAD)
                        .food(ModFoods.MENEMEN_WITH_BREAD)));
//
        BAKLAVA = ITEMS.register("baklava",
                () -> new BaklavaItem(basicItem().stacksTo(64).rarity(Rarity.EPIC)
                        .food(ModFoods.BAKLAVA)));
//
        BISCAT = ITEMS.register("biscat",
                () -> new BiscatItem(basicItem().stacksTo(64).rarity(Rarity.COMMON)
                        .food(ModFoods.BISCAT)));
//
        RUBY_SYRUP = ITEMS.register("ruby_syrup",
                () -> new GlassBottleFoodItem(basicItem().stacksTo(16).rarity(Rarity.COMMON)
                        .food(ModFoods.RUBY_SYRUP)));
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
    }


    // ==================== 辅助方法 ====================


    private static Item simpleFoodItem(int nutrition, float saturationMod, boolean unstackable) {
        Item.Properties properties = basicItem();
        if (unstackable) {
            properties = properties.stacksTo(1);
        }
        return new Item(properties.food(
                simpleFoodItemProperties(nutrition, saturationMod).build()));
    }
    public static FoodProperties.Builder simpleFoodItemProperties(int nutrition, float saturationMod) {
        return new FoodProperties
                .Builder()
                .alwaysEdible()
                .nutrition(nutrition)
                .saturationModifier(saturationMod / nutrition);
    }



    // 注册方法：供主类调用，将 DeferredRegister 绑定到 mod 事件总线
    public static void register(IEventBus modEventBus) {
        ITEMS.register(modEventBus);
    }
}