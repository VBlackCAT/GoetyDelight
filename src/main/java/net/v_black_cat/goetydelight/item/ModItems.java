package net.v_black_cat.goetydelight.item;

import com.Polarice3.Goety.common.items.ModTiers;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.*;
import net.minecraftforge.common.ForgeSpawnEggItem;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import net.v_black_cat.goetydelight.GoetyDelight;
import net.v_black_cat.goetydelight.block.ModBlocks;
import net.v_black_cat.goetydelight.effect.ModEffects;
import net.v_black_cat.goetydelight.entities.ModEntities;
import net.v_black_cat.goetydelight.item.food.*;
import net.v_black_cat.goetydelight.item.food.BowlFoodItem;
import vectorwing.farmersdelight.common.item.KnifeItem;

import java.util.Random;
import java.util.function.Supplier;

import static com.Polarice3.Goety.common.effects.GoetyEffects.*;
import static net.v_black_cat.goetydelight.block.ModBlocks.EXAMPLE_BLOCK;
import static net.v_black_cat.goetydelight.util.TimeConverter.minToTick;
import static net.v_black_cat.goetydelight.util.TimeConverter.sToTick;
import static vectorwing.farmersdelight.common.registry.ModItems.basicItem;

public class ModItems {
    public static final DeferredRegister<Item> ITEMS =
            DeferredRegister.create(ForgeRegistries.ITEMS, GoetyDelight.MODID);

    // ==================== 物品声明区域 ====================
    // 块物品
    public static final RegistryObject<Item> EXAMPLE_BLOCK_ITEM;
    public static final RegistryObject<Item> METAMORPHIC_SCENT_GRASS;

    //刷子
    public static final RegistryObject<Item> CURSED_METAL_BRUSH;
    public static final RegistryObject<Item> DARK_BRUSH;
    // 刀具物品
    public static final RegistryObject<Item> APOCALYPTIUM_KNIFE;
    public static final RegistryObject<Item> VENOMOUS_SPIDER_KNIFE;
    public static final RegistryObject<Item> SPECTRE_KNIFE;
    public static final RegistryObject<Item> CURSED_INGOT_KNIFE;
    public static final RegistryObject<Item> DARK_KNIFE;
    public static final RegistryObject<Item> APOCALYPTIUM_INGOT_BRUSH;

    // 武器物品
    public static final RegistryObject<Item> MARBLE_OP_SWORD;
    public static final RegistryObject<Item> PARASITIZED_WARDEN;
    public static final RegistryObject<Item> STARLESS_NIGHT;
    //刷怪蛋

    public static final RegistryObject<Item> GHOST_FARMER_SPAWN_EGG;
    // 食物物品
    public static final RegistryObject<Item> EXAMPLE_ITEM;
    public static final RegistryObject<Item> GOETYDELIGHT_ICON;
    public static final RegistryObject<Item> TAINTED_DRINK;
    public static final RegistryObject<Item> REJECTED_DARK_MEAT_SOUP;
    public static final RegistryObject<Item> SIBLING_SUNDAE;
    public static final RegistryObject<Item> PROMOTION_HARD_CANDY;
    public static final RegistryObject<Item> CUP;
    public static final RegistryObject<Item> TOXIC_MEAL;
    public static final RegistryObject<Item> POACHED_NETHER_WART_EGG;
    public static final RegistryObject<Item> ECTOPLASM_JELLY;
    public static final RegistryObject<Item> ROASTED_CORPSE_MAGGOTS;
    public static final RegistryObject<Item> WHITE_SHARK_CANDY;
    public static final RegistryObject<Item> WHITE_SHARK_SUGAR_PACK;
    public static final RegistryObject<Item> SUNSHINE_SUGAR_BUN;
    public static final RegistryObject<Item> CANDY_FISH;
    public static final RegistryObject<Item> GRAPE_SLUSH;
    public static final RegistryObject<Item> FROG_LEG_SANDWICH;
    public static final RegistryObject<Item> SPIDER_EGG_BUBBLE_TEA;
    public static final RegistryObject<Item> SPIDER_EGG_BUBBLE_TEA_2;
    public static final RegistryObject<Item> SAUCE_GRILLED_CANDY_FISH;
    public static final RegistryObject<Item> CRYING_SHARK_SUGAR_PACK;
    public static final RegistryObject<Item> SEVEN_LEAF_PUDDING;
    public static final RegistryObject<Item> BEAR_PAW;
    public static final RegistryObject<Item> CAKE;
    public static final RegistryObject<Item> OMINOUS_ICE_CREAM;
    public static final RegistryObject<Item> ECTOPLASMIC_MELON;
    public static final RegistryObject<Item> BLUE_ECTOPLASMIC_SUNDAE;
    public static final RegistryObject<Item> SKULL_SHOT;
    public static final RegistryObject<Item> NIGHT_HEART_PEA_SOUP;
    public static final RegistryObject<Item> POACHED_SPIDER_EGG;
    public static final RegistryObject<Item> GRILL_FROG_LEG;
    public static final RegistryObject<Item> FRENZIED_FUNGUS_POP_ROCKS;
    public static final RegistryObject<Item> SOUL_CONVERGENCE_ROOM;
    public static final RegistryObject<Item> SOUL_CONVERGENCE_ROOM_2;
    public static final RegistryObject<Item> BONE_LORD_ASH_RICE;
    public static final RegistryObject<Item> RUBY_HARD_CANDY;
    public static final RegistryObject<Item> CRISP_BISCUIT;
    public static final RegistryObject<Item> ROTTEN_CORPSE_MAGGOT_FEAST;
    public static final RegistryObject<Item> CORPSE_MAGGOT;
    public static final RegistryObject<Item> QUICK_GROWING_SEED_POPCORN;
    public static final RegistryObject<Item> NETHER_STYLE_FRIED_EGG_SANDWICH;
    public static final RegistryObject<Item> EXOTIC_BREAKFAST;
    public static final RegistryObject<Item> JUNGLE_SALAD;
    public static final RegistryObject<Item> BOILING_BLOOD_BREW;
    public static final RegistryObject<Item> ASCENSION_MOONCAKE;
    public static final RegistryObject<Item> VILLAGERS_FEAST;
    public static final RegistryObject<Item> CHERRY_BLOSSOM_CAKE;
    public static final RegistryObject<Item> NETHER_WART_OMELETTE;
    public static final RegistryObject<Item> WARPED_WART_OMELETTE;
    public static final RegistryObject<Item> FULL_SPIDER_FEAST;
    public static final RegistryObject<Item> LIQUID_VOID_TEA_DRINK;
    public static final RegistryObject<Item> PURE_DRINK;
    public static final RegistryObject<Item> LICHS_CHAOS_STEW;
    public static final RegistryObject<Item> MAGIC_QUARTZ_COOKIE;
    public static final RegistryObject<Item> SNAP_UNHOLY_TRIPE;
    public static final RegistryObject<Item> SUNDAE_OF_THE_PHILOSOPHERS_POTION;
    public static final RegistryObject<Item> THE_BOX_OF_THE_DEAD;
    public static final RegistryObject<Item> RING_PACKED_VOID_GEL_JELLY;
    public static final RegistryObject<Item> STUFFED_TALL_SKULL_RICE;
    public static final RegistryObject<Item> OMINOUS_RAMUNE;
    public static final RegistryObject<Item> BOAT_STUFFED_ROASTED_WARDEN_HEAD;
    public static final RegistryObject<Item> BOAT_STUFFED_ROASTED_WARDEN_MEET;
    public static final RegistryObject<Item> BOAT_STUFFED_ROASTED_WARDEN_FLANK;
    public static final RegistryObject<Item> ANCIENT_ENCHANTED_GOLDEN_APPLE;
    public static final RegistryObject<Item> NOT_ANYTHING;
    public static final RegistryObject<Item> ROAST_LAOWANG;
    public static final RegistryObject<Item> POLARICE;
    public static final RegistryObject<Item> METAMORPHIC_SCENT_FRUIT;
    public static final RegistryObject<Item> FORBIDDDEN_SOUP_BUN;
    public static final RegistryObject<Item> HIDDEN_PANCAKE;

    //种子
    public static final RegistryObject<Item> ECTOPLASMIC_MELON_SEEDS;
    public static final RegistryObject<Item> METAMORPHIC_SCENT_GRASS_SEEDS;
    // ==================== 效果供应商常量 ====================
    private static final Supplier<MobEffect> COMFORT_EFFECT_SUPPLIER = farmersDelightBuff("comfort");
    private static final Supplier<MobEffect> NOURISHMENT_EFFECT_SUPPLIER = farmersDelightBuff("nourishment");
    private static final Supplier<MobEffect> WILD_RAGE_EFFECT_SUPPLIER = goetyBuff("wild_rage");
    private static final Supplier<MobEffect> RAMPAGE_EFFECT_SUPPLIER = goetyBuff("rampage");
    private static final Supplier<MobEffect> FORTUNATE_EFFECT_SUPPLIER = goetyBuff("fortunate");
    private static final Supplier<MobEffect> CHILL_HIDE_EFFECT_SUPPLIER = goetyBuff("chill_hide");
    private static final Supplier<MobEffect> CORPSE_EATER_EFFECT_SUPPLIER = goetyBuff("corpse_eater");
    private static final Supplier<MobEffect> SHADOW_WALK_EFFECT_SUPPLIER = goetyBuff("shadow_walk");
    private static final Supplier<MobEffect> CLIMBING_EFFECT_SUPPLIER = goetyBuff("climbing");
    private static final Supplier<MobEffect> FROG_LEG_EFFECT_SUPPLIER = goetyBuff("frog_leg");
    private static final Supplier<MobEffect> CHARGED_EFFECT_SUPPLIER = goetyBuff("charged");
    private static final Supplier<MobEffect> SOUL_ARMOR_EFFECT_SUPPLIER = goetyBuff("soul_armor");
    private static final Supplier<MobEffect> BUFF_EFFECT_SUPPLIER = goetyBuff("buff");
    private static final Supplier<MobEffect> SAVE_EFFECTS_SUPPLIER = goetyBuff("save_effects");
    private static final Supplier<MobEffect> PHOTOSYNTHESIS_SUPPLIER = goetyBuff("photosynthesis");
    private static final Supplier<MobEffect> FROSTY_AURA_SUPPLIER = goetyBuff("frosty_aura");
    private static final Supplier<MobEffect> FIERY_AURA_SUPPLIER = goetyBuff("fiery_aura");
    private static final Supplier<MobEffect> ILLAGUE = goetyBuff("illague");

    // ==================== 静态初始化块：物品定义区域 ====================
    static {
        // 块物品初始化
        EXAMPLE_BLOCK_ITEM = ITEMS.register("example_block",
                () -> new BlockItem(EXAMPLE_BLOCK.get(), basicItem().stacksTo(1)));

        NOT_ANYTHING = ITEMS.register("not_anything",
                () -> new Item(basicItem().stacksTo(1)));

        METAMORPHIC_SCENT_GRASS = ITEMS.register("metamorphic_scent_grass",
            () -> new MetamorphicScentGrassItem(basicItem().stacksTo(64).food(simpleFoodItemProperties(2,3).build())));

        //神金刀
        APOCALYPTIUM_KNIFE = registerWithTab("apocalyptium_knife",
                () -> new KnifeItem(Tiers.NETHERITE, 4F, -2.0F, basicItem().durability(1666)));

        VENOMOUS_SPIDER_KNIFE = registerWithTab("venomous_spider_knife",
                () -> new KnifeItem(Tiers.IRON, 0.5F, -2.0F, basicItem()));
        SPECTRE_KNIFE = registerWithTab("spectre_knife",
                () -> new KnifeItem(Tiers.IRON, 0.5F, -2.0F, basicItem()));


        //诅咒金属刀
        CURSED_INGOT_KNIFE = registerWithTab("cursed_ingot_knife",
                () -> new KnifeItem(ModTiers.SPECIAL, 0F, -2.0F, basicItem().durability(256)));

        //黑暗金属刀
        DARK_KNIFE = registerWithTab("dark_knife",
                () -> new DarkKnifeItem(ModTiers.DARK, 1F, -2.0F, basicItem().durability(512)));

        //诅咒金属刷子
        CURSED_METAL_BRUSH = ITEMS.register("cursed_metal_brush",
                () -> new DarkBrushItem(basicItem().durability(64),2));

        //黑暗金属刷子
        DARK_BRUSH = ITEMS.register("dark_brush",
                () -> new DarkBrushItem(basicItem().durability(64),3));

        //神金刷子
        APOCALYPTIUM_INGOT_BRUSH = ITEMS.register("apocalyptium_ingot_brush",
                () -> new DarkBrushItem(basicItem().durability(166),4));

        //大理石op剑
        MARBLE_OP_SWORD = ITEMS.register("marble_op_sword",
                () -> new MarbleOpSwordItem(Tiers.WOOD, 1, 2, basicItem().rarity(Rarity.EPIC)));

        STARLESS_NIGHT = ITEMS.register("starless_night",
                () -> new StarlessNightitem(Tiers.valueOf("NETHERITE"), 7, -3, basicItem().rarity(Rarity.EPIC)));

        PARASITIZED_WARDEN = ITEMS.register("parasitized_warden",
                () -> new Item(basicItem().stacksTo(1).rarity(Rarity.UNCOMMON)));

        EXAMPLE_ITEM = ITEMS.register("example_item",
                () -> new Item(basicItem().stacksTo(1)
                        .food(simpleFoodItemProperties(1, 2f).build())));
        GOETYDELIGHT_ICON = ITEMS.register("goetydelight_icon",
                () -> simpleFoodItem(666, 666, true));

        TAINTED_DRINK = ITEMS.register("tainted_drink",
                () -> new CustomDrinkItem(basicItem().stacksTo(1).rarity(Rarity.RARE).rarity(Rarity.RARE).food(
                        simpleFoodItemProperties(4, 4)
                                .effect(() -> new MobEffectInstance(MobEffects.REGENERATION, 150, 1), 1.0F)
                                .effect(() -> new MobEffectInstance(MobEffects.DAMAGE_RESISTANCE, 300, 1), 1.0F)
                                .effect(() -> new MobEffectInstance(ModEffects.THE_PALE_MESSRNGER.get(), minToTick(3), 0), 1.0F)
                                .effect(() -> new MobEffectInstance(ModEffects.ZOMBIFIED_PIGLIN_BRUTE_SERVANT_SUPPORT.get(), minToTick(6), 0), 1.0F)
                                .build())));

        PURE_DRINK = ITEMS.register("pure_drink",
                () -> new CustomDrinkItem(basicItem().stacksTo(1).rarity(Rarity.RARE).food(
                        simpleFoodItemProperties(4, 4)
                                .effect(() -> new MobEffectInstance(MobEffects.REGENERATION, 600, 3), 1.0F)
                                .effect(() -> new MobEffectInstance(MobEffects.DAMAGE_RESISTANCE, 1200, 3), 1.0F)
                                .effect(() -> new MobEffectInstance(ModEffects.THE_PALE_MESSRNGER.get(), minToTick(15), 0), 1.0F)
                                .effect(() -> new MobEffectInstance(ModEffects.ZOMBIFIED_PIGLIN_BRUTE_SERVANT_SUPPORT.get(), minToTick(30), 0), 1.0F)
                                .build())) {
                    @Override
                    public boolean isFoil(ItemStack pStack) {
                        return true;
                    }
                });

        CUP = ITEMS.register("eternal_refusal_of_black_meat_soup",
                () -> new EternalRefusalOfBlackMeatSoupItem(basicItem().stacksTo(1).rarity(Rarity.RARE).food(
                        simpleFoodItemProperties(10, 4)
                                .effect(() -> new MobEffectInstance(MobEffects.CONFUSION, 600, 0), 1.0F)
                                .effect(() -> {
                                    int randomAmplifier = new Random().nextInt(5);
                                    return new MobEffectInstance(MobEffects.POISON, 600, randomAmplifier, false, true);
                                }, 1.0F)
                                .effect(() -> new MobEffectInstance(MobEffects.WEAKNESS, 600, 1), 1.0F)
                                .build())));

        REJECTED_DARK_MEAT_SOUP = ITEMS.register("rejected_dark_meat_soup",
                () -> new RejectedDarkMeatSoupItem(basicItem().stacksTo(16).food(
                        simpleFoodItemProperties(10, 4)
                                .effect(() -> new MobEffectInstance(MobEffects.CONFUSION, 600, 0), 1.0F)
                                .effect(() -> {
                                    int randomAmplifier = new Random().nextInt(5);
                                    return new MobEffectInstance(MobEffects.POISON, 600, randomAmplifier, false, true);
                                }, 1.0F)
                                .effect(() -> new MobEffectInstance(MobEffects.WEAKNESS, 600, 1), 1.0F)
                                .build())));

        PROMOTION_HARD_CANDY = ITEMS.register("promotion_hard_candy",
                () -> simpleFoodItem(1, 1, true)); 

        TOXIC_MEAL = ITEMS.register("toxic_meal",
                () -> new ToxicMealItem(basicItem().stacksTo(16).food(
                        simpleFoodItemProperties(8, 4)
                                .effect(() -> new MobEffectInstance(MobEffects.CONFUSION, 2000, 0), 1.0F)
                                .effect(() -> new MobEffectInstance(MobEffects.POISON, 2000, 9), 1.0F)
                                .effect(() -> new MobEffectInstance(MobEffects.WEAKNESS, 2000, 4), 1.0F)
                                .build())));
        POACHED_NETHER_WART_EGG = ITEMS.register("poached_nether_wart_egg",
                () -> new PoachedNetherWartEggItem(basicItem().stacksTo(16).food(
                        simpleFoodItemProperties(7, 2).fast().build())));
        ECTOPLASM_JELLY = ITEMS.register("ectoplasm_jelly",
                () -> simpleFastFoodItem(4, 4, false));
        FROG_LEG_SANDWICH = ITEMS.register("frog_leg_sandwich",
                () -> simpleFoodItem(10, 8, false));

        SPIDER_EGG_BUBBLE_TEA_2 = ITEMS.register("spider_egg_bubble_tea_2",
                () -> simpleFoodItem(1, 1, true));


        // 特殊效果食物物品初始化



        ASCENSION_MOONCAKE = ITEMS.register("ascension_mooncake",
                () -> new Item(basicItem().stacksTo(1).rarity(Rarity.EPIC).food(
                        simpleFoodItemProperties(66, 666)
                                .effect(() -> new MobEffectInstance(MobEffects.REGENERATION, minToTick(66), 5), 1.0F)
                                .effect(() -> new MobEffectInstance(MobEffects.DAMAGE_RESISTANCE, minToTick(66), 5), 1.0F)
                                .build())));

        SPIDER_EGG_BUBBLE_TEA = ITEMS.register("spider_egg_bubble_tea",
                () -> new CustomDrinkItem(basicItem().stacksTo(16).food(
                        simpleFoodItemProperties(6, 4)
                                .effect(() -> new MobEffectInstance(CLIMBING_EFFECT_SUPPLIER.get(), minToTick(7), 0), 1.0F)
                                .build())));

        BOILING_BLOOD_BREW = ITEMS.register("boiling_blood_brew",
                () -> new CustomDrinkItem(basicItem().stacksTo(16).food(
                        simpleFoodItemProperties(6, 4)
                                .effect(() -> new MobEffectInstance(FIERY_AURA_SUPPLIER.get(), minToTick(5), 0), 1.0F)
                                .effect(() -> new MobEffectInstance(COMFORT_EFFECT_SUPPLIER.get(), minToTick(5), 0), 1.0F)
                                .build())));

        NETHER_STYLE_FRIED_EGG_SANDWICH = ITEMS.register("nether_style_fried_egg_sandwich",
                () -> new Item(basicItem().stacksTo(64).food(
                        simpleFoodItemProperties(11, 6)
                                .effect(() -> new MobEffectInstance(MobEffects.FIRE_RESISTANCE, minToTick(8), 0), 1.0F)
                                .build())));

        EXOTIC_BREAKFAST = ITEMS.register("exotic_breakfast",
                () -> new Item(basicItem().stacksTo(16).food(
                        simpleFoodItemProperties(8, 5)
                                .effect(() -> new MobEffectInstance(WILD_RAGE_EFFECT_SUPPLIER.get(), minToTick(1), 0), 1.0F)
                                .effect(() -> new MobEffectInstance(NOURISHMENT_EFFECT_SUPPLIER.get(), minToTick(3), 0), 1.0F)
                                .build())));

        VILLAGERS_FEAST = ITEMS.register("villagers_feast",
                () -> new Item(basicItem().stacksTo(16).food(
                        simpleFoodItemProperties(16, 10)
                                .effect(() -> new MobEffectInstance(MobEffects.HERO_OF_THE_VILLAGE, minToTick(3), 0), 1.0F)
                                .effect(() -> new MobEffectInstance(NOURISHMENT_EFFECT_SUPPLIER.get(), minToTick(10), 0), 1.0F)
                                .build())));

        JUNGLE_SALAD = ITEMS.register("jungle_salad",
                () -> new BowlFoodItem(basicItem().stacksTo(16).food(
                        simpleFoodItemProperties(8, 4)
                                .effect(() -> new MobEffectInstance(PHOTOSYNTHESIS_SUPPLIER.get(), minToTick(5), 0), 1.0F)
                                .effect(() -> new MobEffectInstance(NOURISHMENT_EFFECT_SUPPLIER.get(), minToTick(1), 0), 1.0F)
                                .build())));

        QUICK_GROWING_SEED_POPCORN = ITEMS.register("quick_growing_seed_popcorn",
                () -> new Item(basicItem().stacksTo(1).food(
                        simpleFoodItemProperties(8, 5)
                                .effect(() -> new MobEffectInstance(MobEffects.SATURATION, 100, 0), 1.0F)
                                .effect(() -> new MobEffectInstance(COMFORT_EFFECT_SUPPLIER.get(), minToTick(10), 0), 1.0F)
                                .effect(() -> new MobEffectInstance(PHOTOSYNTHESIS_SUPPLIER.get(), minToTick(5), 0), 1.0F)
                                .effect(() -> new MobEffectInstance(NOURISHMENT_EFFECT_SUPPLIER.get(), minToTick(10), 0), 1.0F)
                                .build())));


        SAUCE_GRILLED_CANDY_FISH = ITEMS.register("sauce_grilled_candy_fish",
                () ->  new SauceGrilledCandyFishItem(basicItem().stacksTo(8).food(
                        simpleFoodItemProperties(9, 6)
                                .effect(() -> new MobEffectInstance(NOURISHMENT_EFFECT_SUPPLIER.get(), minToTick(8), 0), 1.0F)
                                .effect(() -> new MobEffectInstance(FIERY_AURA.get(), minToTick(5), 0), 1.0F)
                                .build())));

        CANDY_FISH = ITEMS.register("candy_fish",
                () -> new CandyFishItem(basicItem().stacksTo(8).food(
                        simpleFoodItemProperties(6, 4)
                                .effect(() -> new MobEffectInstance(NOURISHMENT_EFFECT_SUPPLIER.get(), minToTick(7), 0), 1.0F)
                                .effect(() -> new MobEffectInstance(MobEffects.WATER_BREATHING, minToTick(5), 0), 1.0F)
                                .build())));

        WHITE_SHARK_SUGAR_PACK = ITEMS.register("sugar_pack",
                () -> new Item(basicItem().stacksTo(16).food(
                        simpleFoodItemProperties(6, 4)
                                .effect(() -> new MobEffectInstance(NOURISHMENT_EFFECT_SUPPLIER.get(), minToTick(4), 0), 1.0F)
                                .build())));

        WHITE_SHARK_CANDY = ITEMS.register("sugar_scepter",
                () ->  new SugarScepterItem(basicItem().stacksTo(8).rarity(Rarity.UNCOMMON).food(
                        simpleFoodItemProperties(8, 5)
                                .effect(() -> new MobEffectInstance(MobEffects.REGENERATION, minToTick(1), 1), 1.0F)
                                .effect(() -> new MobEffectInstance(NOURISHMENT_EFFECT_SUPPLIER.get(), minToTick(10), 0), 1.0F)
                                .build())));

        SIBLING_SUNDAE = ITEMS.register("possible_holy_representative",
                () -> new SiblingSundaeItem(basicItem().stacksTo(8).rarity(Rarity.UNCOMMON).food(
                        simpleFoodItemProperties(6, 5)
                                .effect(() -> new MobEffectInstance(INSIGHT.get(), minToTick(2.5F), 3), 1.0F)
                                .build())));


        ROASTED_CORPSE_MAGGOTS = ITEMS.register("roasted_corpse_maggots",
                () -> new RoastedCorpseMaggotsitem(basicItem().stacksTo(16).food(
                        simpleFoodItemProperties(5, 2)
                                .build())));


        CORPSE_MAGGOT = ITEMS.register("corpse_maggot",
                () -> new CorpseMaggotItem(basicItem().stacksTo(64).food(
                        simpleFoodItemProperties(3, 1)
                                .effect(() -> new MobEffectInstance(MobEffects.CONFUSION, sToTick(10), 0), 1.0F)
                                .build())));


        CRYING_SHARK_SUGAR_PACK = ITEMS.register("cry_sugar_pack",
                () -> new Item(basicItem().stacksTo(16).food(
                        simpleFoodItemProperties(7, 4)
                                .effect(() -> new MobEffectInstance(ModEffects.HYDRATION.get(), minToTick(15), 1), 1.0F)
                                .effect(() -> new MobEffectInstance(NOURISHMENT_EFFECT_SUPPLIER.get(), minToTick(5), 0), 1.0F)
                                .build())));

        SUNSHINE_SUGAR_BUN = ITEMS.register("sunshine_sugar_bun",
                () -> new Item(basicItem().stacksTo(16).food(
                        simpleFoodItemProperties(7, 4)
                                .effect(() -> new MobEffectInstance(PHOTOSYNTHESIS_SUPPLIER.get(), minToTick(15), 1), 1.0F)
                                .effect(() -> new MobEffectInstance(NOURISHMENT_EFFECT_SUPPLIER.get(), minToTick(5), 0), 1.0F)
                                .build())));

        GRAPE_SLUSH = ITEMS.register("grape_slush",
                () -> new NoGlassBottleDrinkItem(basicItem().stacksTo(16).food(
                        simpleFoodItemProperties(9, 6)
                                .effect(() -> new MobEffectInstance(CHILL_HIDE_EFFECT_SUPPLIER.get(),4200, 1), 1.0F)
                                .effect(() -> new MobEffectInstance(FROSTY_AURA_SUPPLIER.get(), 600, 1), 1.0F)
                                .effect(() -> new MobEffectInstance(NOURISHMENT_EFFECT_SUPPLIER.get(), minToTick(10), 0), 1.0F)
                                .build())));

        SEVEN_LEAF_PUDDING = ITEMS.register("sweet_berry_pudding",
                () -> new SevenLeafPuddingItem(basicItem().stacksTo(16).craftRemainder(Items.BOWL).food(
                        simpleFoodItemProperties(7, 5)
                                .effect(() -> new MobEffectInstance(MobEffects.REGENERATION, minToTick(5), 1), 1.0F)
                                .effect(() -> new MobEffectInstance(NOURISHMENT_EFFECT_SUPPLIER.get(), minToTick(5), 0), 1.0F)
                                .build())));


        BEAR_PAW = ITEMS.register("bear_paw",
                () -> new Item(basicItem().stacksTo(64).food(
                        simpleFoodItemProperties(6, 5)
                                .effect(() -> new MobEffectInstance(NOURISHMENT_EFFECT_SUPPLIER.get(), 6000, 0), 1.0F)
                                .effect(() -> new MobEffectInstance(RAMPAGE_EFFECT_SUPPLIER.get(), 2400, 0), 1.0F)
                                .build())));
        CAKE = ITEMS.register("royal_cake",
                () -> new CakeItem(basicItem().stacksTo(16).rarity(Rarity.RARE).food(
                        simpleFoodItemProperties(6, 3)
                                .effect(() -> new MobEffectInstance(NOURISHMENT_EFFECT_SUPPLIER.get(), 4500, 0), 1.0F)
                                .effect(() -> new MobEffectInstance(FORTUNATE_EFFECT_SUPPLIER.get(), 1500, 2), 1.0F)
                                .effect(() -> new MobEffectInstance(BOTTLING.get(), 1500, 2), 1.0F)
                                .effect(() -> new MobEffectInstance(MobEffects.DAMAGE_RESISTANCE, 1500, 1), 1.0F)
                                .build())));
        OMINOUS_ICE_CREAM = ITEMS.register("ominous_ice_cream",
                () -> new OminousIceCreamItem(basicItem()
                        .stacksTo(16)
                        .rarity(Rarity.UNCOMMON)
                        .food(
                                simpleFoodItemProperties(8, 5)
                                        .effect(() -> new MobEffectInstance(MobEffects.BAD_OMEN, 6000, 4), 1.0F)
                                        .effect(() -> new MobEffectInstance(CHILL_HIDE_EFFECT_SUPPLIER.get(), 1200, 0), 1.0F)
                                        .build())));
        ECTOPLASMIC_MELON = ITEMS.register("ectoplasmic_melon",
                () -> new Item(basicItem().stacksTo(64).food(
                        simpleFoodItemProperties(3, 1)
                                .effect(() -> new MobEffectInstance(CHILL_HIDE_EFFECT_SUPPLIER.get(), 100, 0), 1.0F)
                                .build())));
        BLUE_ECTOPLASMIC_SUNDAE = ITEMS.register("blue_ectoplasmic_sundae",
                () -> new Item(basicItem().stacksTo(16).food(
                        simpleFoodItemProperties(10, 6)
                                .effect(() -> new MobEffectInstance(FORTUNATE_EFFECT_SUPPLIER.get(), 12000, 0), 1.0F)
                                .effect(() -> new MobEffectInstance(MobEffects.REGENERATION, 2400, 0), 1.0F)
                                .effect(() -> new MobEffectInstance(CHILL_HIDE_EFFECT_SUPPLIER.get(), 2400, 0), 1.0F)
                                .build())));


        SKULL_SHOT = ITEMS.register("skull_shot",
                () -> new NoGlassBottleDrinkItem(basicItem().stacksTo(16).food(
                        simpleFoodItemProperties(6, 4)
                                .effect(() -> new MobEffectInstance(CORPSE_EATER_EFFECT_SUPPLIER.get(), 1200, 0), 1.0F)
                                .build())));



        NIGHT_HEART_PEA_SOUP = ITEMS.register("night_heart_pea_soup",
                () -> new NightHeartPeaSoupItem(basicItem().craftRemainder(Items.GLASS_BOTTLE).stacksTo(1).rarity(Rarity.UNCOMMON).food(
                        simpleFoodItemProperties(7, 3)
                                .effect(() -> new MobEffectInstance(MobEffects.DAMAGE_BOOST, minToTick(5), 2), 1.0F)
                                .effect(() -> new MobEffectInstance(SHADOW_WALK.get(), sToTick(60), 2), 1.0F)
                                .effect(() -> new MobEffectInstance(MobEffects.DAMAGE_RESISTANCE, 6000, 2), 1.0F)
                                .effect(() -> new MobEffectInstance(NOURISHMENT_EFFECT_SUPPLIER.get(), 12000, 0), 1.0F)
                                .effect(() -> new MobEffectInstance(COMFORT_EFFECT_SUPPLIER.get(), 12000, 0), 1.0F)
                                .effect(() -> new MobEffectInstance(ModEffects.SERVANT_REINFORCEMENT.get(), minToTick(5), 0), 1.0F)
                                .build())));
        POACHED_SPIDER_EGG = ITEMS.register("poached_spider_egg",
                () -> new Item(basicItem().stacksTo(64).food(
                        simpleFoodItemProperties(5, 2)
                                .effect(() -> new MobEffectInstance(CLIMBING_EFFECT_SUPPLIER.get(), 200, 0), 1.0F)
                                .fast()
                                .build())));
        GRILL_FROG_LEG = ITEMS.register("grill_frog_leg",
                () -> new Item(basicItem().stacksTo(64).food(
                        simpleFoodItemProperties(10, 6)
                                .effect(() -> new MobEffectInstance(FROG_LEG_EFFECT_SUPPLIER.get(), 1200, 0), 1.0F)
                                .effect(() -> new MobEffectInstance(MobEffects.REGENERATION, 400, 1), 1.0F)
                                .build())));
        FRENZIED_FUNGUS_POP_ROCKS = ITEMS.register("frenzied_fungus_pop_rocks",
                () -> new FrenziedFungusPopRocksItem(basicItem().stacksTo(16).food(
                        simpleFoodItemProperties(6, 4)
                                .build())));
        SOUL_CONVERGENCE_ROOM = ITEMS.register("gathering_soul_embryos",
                () -> new Item(basicItem().stacksTo(64).food(
                        simpleFoodItemProperties(8, 12)
                                .effect(() -> new MobEffectInstance(MobEffects.HUNGER, 200, 0), 1.0F)
                                .effect(() -> new MobEffectInstance(SOUL_ARMOR_EFFECT_SUPPLIER.get(), 1200, 1), 1.0F)
                                .build())));
        SOUL_CONVERGENCE_ROOM_2 = ITEMS.register("soul_convergence_room",
                () -> new Item(basicItem().stacksTo(64).rarity(Rarity.UNCOMMON).food(
                        simpleFoodItemProperties(20, 30)
                                .effect(() -> new MobEffectInstance(SOUL_ARMOR_EFFECT_SUPPLIER.get(), 6000, 4), 1.0F)
                                .effect(() -> new MobEffectInstance(MobEffects.HEALTH_BOOST, 6000, 2), 1.0F)
                                .effect(() -> new MobEffectInstance(ModEffects.HUNTING_DENIAL.get(), minToTick(10), 0), 1.0F)
                                .build())));
        BONE_LORD_ASH_RICE = ITEMS.register("bone_lord_ash_rice",
                () -> new BoneLordAshRiceItem(basicItem().craftRemainder(Items.BOWL).stacksTo(1).food(
                        simpleFoodItemProperties(6, 4)
                                .effect(() -> new MobEffectInstance(CHILL_HIDE_EFFECT_SUPPLIER.get(), 6000, 1), 1.0F)
                                .effect(() -> new MobEffectInstance(BUFF_EFFECT_SUPPLIER.get(), 6000, 2), 1.0F)
                                .effect(() -> new MobEffectInstance(NOURISHMENT_EFFECT_SUPPLIER.get(), minToTick(5), 0), 1.0F)
                                .effect(() -> new MobEffectInstance(CORPSE_EATER.get(), minToTick(1), 2), 1.0F)
                                .build())));
        RUBY_HARD_CANDY = ITEMS.register("ruby_hard_candy",
                () -> new RubyHardCandyItem(basicItem().stacksTo(16).rarity(Rarity.UNCOMMON).food(
                        simpleFoodItemProperties(10, 8)
                                .effect(() -> new MobEffectInstance(ModEffects.SPELL_MASTERY.get(), minToTick(30), 2), 1.0F)
                                .effect(() -> new MobEffectInstance(ModEffects.SPELL_DURATION.get(), minToTick(10), 2), 1.0F)
                                .effect(() -> new MobEffectInstance(MobEffects.REGENERATION, minToTick(8), 1), 1.0F)
                                .effect(() -> new MobEffectInstance(NOURISHMENT_EFFECT_SUPPLIER.get(), minToTick(15), 0), 1.0F)
                                .build())));
        CRISP_BISCUIT = ITEMS.register("crisp_biscuit",
                () -> new CrispBiscuitItem(basicItem().stacksTo(16).food(
                        simpleFoodItemProperties(9, 6)
                                .effect(() -> new MobEffectInstance(NOURISHMENT_EFFECT_SUPPLIER.get(), minToTick(5), 2), 1.0F)
                                .effect(() -> new MobEffectInstance(ModEffects.SPELL_MASTERY.get(), minToTick(2), 0), 1.0F)
                                .build())));
        ROTTEN_CORPSE_MAGGOT_FEAST = ITEMS.register("rotten_corpse_maggot_feast",
                () -> new RottenCorpseMaggotFeastItem(basicItem().stacksTo(16).food(
                        simpleFoodItemProperties(8, 5)
                                .effect(() -> new MobEffectInstance(NOURISHMENT_EFFECT_SUPPLIER.get(), minToTick(2), 0), 1.0F)
                                .build())));
        CHERRY_BLOSSOM_CAKE = ITEMS.register("cherry_blossom_cake",
                () -> new CherryBlossomCakeItem(basicItem().stacksTo(16).rarity(Rarity.UNCOMMON).food(
                        simpleFoodItemProperties(12, 8)
                                .effect(() -> new MobEffectInstance(NOURISHMENT_EFFECT_SUPPLIER.get(), minToTick(10), 0), 1.0F)
                                .effect(() -> new MobEffectInstance(MobEffects.LUCK, minToTick(2), 2), 1.0F)
                                .build())));
        NETHER_WART_OMELETTE = ITEMS.register("nether_wart_omelette",
                () -> new NetherWartOmeletteItem(basicItem().stacksTo(16).food(
                        simpleFoodItemProperties(6, 2)
                                .build())));
        WARPED_WART_OMELETTE = ITEMS.register("warped_wart_omelette",
                () -> new WarpedWartOmeletteItem(basicItem().stacksTo(16).food(
                        simpleFoodItemProperties(6, 2)
                                .build())));
        FULL_SPIDER_FEAST = ITEMS.register("full_spider_feast",
                () -> new Item(basicItem().stacksTo(16).food(
                        simpleFoodItemProperties(8, 5)
                                .effect(() -> new MobEffectInstance(NOURISHMENT_EFFECT_SUPPLIER.get(), minToTick(5), 0), 1.0F)
                                .effect(() -> new MobEffectInstance(CLIMBING.get(), minToTick(5), 0), 1.0F)
                                .build())));
        LIQUID_VOID_TEA_DRINK = ITEMS.register("liquid_void_tea_drink",
                () -> new LiquidVoidTeaDrinkItem(basicItem().stacksTo(1)
                        .food(new FoodProperties.Builder()
                                .nutrition(0)
                                .alwaysEat()
                               .build())));

        LICHS_CHAOS_STEW = ITEMS.register("lichs_chaos_stew",
                () -> new LichsChaosStewItem(basicItem().craftRemainder(Items.BOWL).stacksTo(1).rarity(Rarity.EPIC).food(
                        simpleFoodItemProperties(16, 12)
                                .effect(() -> new MobEffectInstance(SAVE_EFFECTS.get(), -1, 2), 1.0F)
                                .effect(() -> new MobEffectInstance(ModEffects.WIGHT_DENIAL.get(), minToTick(30), 0,false,false), 1.0F)
                                .effect(() -> new MobEffectInstance(MobEffects.FIRE_RESISTANCE, -1, 0), 1.0F)
                                .effect(() -> new MobEffectInstance(MobEffects.REGENERATION, minToTick(30), 2), 1.0F)
                                .build())));
        MAGIC_QUARTZ_COOKIE = ITEMS.register("magic_quartz_cookie",
                () -> new MagicQuartzCookieItem(basicItem().stacksTo(16).food(
                        simpleFoodItemProperties(8, 4)
                               .build())));
        SNAP_UNHOLY_TRIPE = ITEMS.register("snap_unholy_tripe",
                () -> new SnapUnholyTripeItem(basicItem().stacksTo(1).rarity(Rarity.RARE).food(
                        simpleFoodItemProperties(18, 20)
                                .effect(() -> new MobEffectInstance(NOURISHMENT_EFFECT_SUPPLIER.get(), minToTick(30), 0), 1.0F)
                                .effect(() -> new MobEffectInstance(MobEffects.SATURATION, sToTick(10), 0), 1.0F)
                                .effect(() -> new MobEffectInstance(NOURISHMENT_EFFECT_SUPPLIER.get(), minToTick(30), 0), 1.0F)
                                .effect(() -> new MobEffectInstance(ModEffects.THE_PALE_MESSRNGER.get(), minToTick(5), 0), 1.0F)
                                .effect(() -> new MobEffectInstance(MobEffects.FIRE_RESISTANCE,  minToTick(30), 0), 1.0F)
                                .build())));
        SUNDAE_OF_THE_PHILOSOPHERS_POTION = ITEMS.register("sundae_of_the_philosophers_potion",
                () -> new SundaeOfThePhilosophersPotionItem(basicItem().stacksTo(16).rarity(Rarity.EPIC).food(
                        simpleFoodItemProperties(10, 6)
                                .effect(() -> new MobEffectInstance(SAVE_EFFECTS.get(), -1, 1,false,false), 1.0F)
                                .effect(() -> new MobEffectInstance(GOLD_TOUCHED.get(), minToTick(30), 0,false,false), 1.0F)
                                .effect(() -> new MobEffectInstance(SOUL_ARMOR.get(), -1, 1,false,false), 1.0F)
                                .build())));
        THE_BOX_OF_THE_DEAD = ITEMS.register("the_box_of_the_dead",
                () -> new TheBoxOfTheDeadItem(basicItem().stacksTo(64).rarity(Rarity.UNCOMMON).food(
                        simpleFoodItemProperties(6, 3)
                                .effect(() -> new MobEffectInstance(CURSED.get(), sToTick(20), 1), 1.0F)
                                .build())));
        RING_PACKED_VOID_GEL_JELLY = ITEMS.register("ring_packed_void_gel_jelly",
                () -> new FoiledBowlFoodItem(basicItem().craftRemainder(Items.BOWL).stacksTo(16)
                        .food(
                        simpleFoodItemProperties(8, 4)
                                .effect(() -> new MobEffectInstance(ModEffects.VOID_AFFIX.get(), sToTick(60), 0), 1.0F)
                                .effect(() -> new MobEffectInstance(NOURISHMENT_EFFECT_SUPPLIER.get(), sToTick(300), 0), 1.0F)
                                .effect(() -> new MobEffectInstance(MobEffects.HUNGER, sToTick(60), 4), 1.0F)
                                .build())));
        STUFFED_TALL_SKULL_RICE = ITEMS.register("stuffed_tall_skull_rice",
                () -> new StuffedTallSkullRiceItem(basicItem().craftRemainder(Items.BOWL).stacksTo(16)
                        .food(
                        simpleFoodItemProperties(8, 5)
                                .effect(() -> new MobEffectInstance(NOURISHMENT_EFFECT_SUPPLIER.get(), sToTick(60), 0), 1.0F)
                                .effect(() -> new MobEffectInstance(PHOTOSYNTHESIS.get(), minToTick(5), 1), 1.0F)
                                .build())));

        OMINOUS_RAMUNE = ITEMS.register("ominous_ramune",
                    () -> new GlassBottleFoodItem(basicItem().stacksTo(16).rarity(Rarity.UNCOMMON)
                        .food(
                                simpleFoodItemProperties(2, 1)
                                        .effect(() -> new MobEffectInstance(ModEffects.TINGLING.get(), sToTick(60), 0), 1.0F)
                                        .build())));

        BOAT_STUFFED_ROASTED_WARDEN_HEAD = ITEMS.register("boat_stuffed_roasted_warden_head",
                    () -> new BoatStuffedRoastedWardenItem(basicItem().stacksTo(1).rarity(Rarity.UNCOMMON).craftRemainder(Items.DARK_OAK_BOAT)
                        .food(simpleFoodItemProperties(25, 20)
                                        .effect(() ->new MobEffectInstance(SOUL_ARMOR.get(), minToTick(5), 3), 1.0F)
                                        .effect(() ->new MobEffectInstance(ModEffects.HUNTING_DENIAL.get(), minToTick(10), 0), 1.0F)
                                        .effect(() ->new MobEffectInstance(MobEffects.REGENERATION, minToTick(5), 1), 1.0F)
                                        .effect(() ->new MobEffectInstance(MobEffects.DAMAGE_BOOST, minToTick(5), 1), 1.0F)
                                        .effect(() ->new MobEffectInstance(ModEffects.WARDEN.get(), minToTick(10), 0), 1.0F)
                                        .build())));

        BOAT_STUFFED_ROASTED_WARDEN_MEET = ITEMS.register("boat_stuffed_roasted_warden_meet",
                    () -> new BoatStuffedRoastedWardenItem(basicItem().stacksTo(1).rarity(Rarity.UNCOMMON).craftRemainder(Items.DARK_OAK_BOAT)
                        .food(simpleFoodItemProperties(30, 25)
                                        .effect(() ->new MobEffectInstance(SOUL_ARMOR.get(), minToTick(5), 3), 1.0F)
                                        .effect(() ->new MobEffectInstance(ModEffects.HUNTING_DENIAL.get(), minToTick(10), 0), 1.0F)
                                        .effect(() ->new MobEffectInstance(MobEffects.REGENERATION, minToTick(5), 1), 1.0F)
                                        .effect(() ->new MobEffectInstance(MobEffects.DAMAGE_BOOST, minToTick(5), 1), 1.0F)
                                        .effect(() ->new MobEffectInstance(ModEffects.WARDEN.get(), minToTick(10), 0), 1.0F)
                                        .build())));

        BOAT_STUFFED_ROASTED_WARDEN_FLANK = ITEMS.register("boat_stuffed_roasted_warden_flank",
                    () -> new BoatStuffedRoastedWardenItem(basicItem().stacksTo(1).rarity(Rarity.UNCOMMON).craftRemainder(Items.DARK_OAK_BOAT)
                        .food(simpleFoodItemProperties(40, 30)
                                        .effect(() ->new MobEffectInstance(SOUL_ARMOR.get(), minToTick(5), 3), 1.0F)
                                        .effect(() ->new MobEffectInstance(ModEffects.HUNTING_DENIAL.get(), minToTick(15), 0), 1.0F)
                                        .effect(() ->new MobEffectInstance(MobEffects.REGENERATION, minToTick(7.5f), 1), 1.0F)
                                        .effect(() ->new MobEffectInstance(MobEffects.DAMAGE_BOOST, minToTick(7.5f), 1), 1.0F)
                                        .effect(() ->new MobEffectInstance(ModEffects.WARDEN.get(), minToTick(15), 0), 1.0F)
                                        .build())));

        ANCIENT_ENCHANTED_GOLDEN_APPLE = ITEMS.register("ancient_enchanted_golden_apple",
                () -> new AncientEnchantedGoldenAppleItem(basicItem().stacksTo(16).rarity(Rarity.EPIC)
                        .food(simpleFoodItemProperties(6, 6)
                                .effect(() ->new MobEffectInstance(MobEffects.DAMAGE_RESISTANCE, minToTick(5), 0), 1.0F)
                                .effect(() ->new MobEffectInstance(MobEffects.ABSORPTION, minToTick(2), 3), 1.0F)
                                .effect(() ->new MobEffectInstance(MobEffects.REGENERATION, 600, 5), 1.0F)
                                .effect(() ->new MobEffectInstance(MobEffects.FIRE_RESISTANCE, minToTick(5), 0), 1.0F)
                                .build())));

        ROAST_LAOWANG = ITEMS.register("roast_laowang",
                () -> new RoastLaowangItem(basicItem().stacksTo(64).rarity(Rarity.EPIC)
                        .food(simpleFoodItemProperties(20, 15)
                                .effect(() ->new MobEffectInstance(NOURISHMENT_EFFECT_SUPPLIER.get(), minToTick(20), 0), 1.0F)
                                .build())));

        POLARICE = ITEMS.register("polarice",
                () -> new PolariceItem(basicItem().stacksTo(1).rarity(Rarity.EPIC)
                        .food(simpleFoodItemProperties(10, 4)
                                .effect(() ->new MobEffectInstance(ILLAGUE.get(), minToTick(5), 0), 1.0F)
                                .effect(() ->new MobEffectInstance(MobEffects.DIG_SPEED, minToTick(5), 2), 1.0F)
                                .build())));
        METAMORPHIC_SCENT_FRUIT=ITEMS.register("metamorphic_scent_fruit",
                () -> simpleFoodItem(10, 8,false));;

        FORBIDDDEN_SOUP_BUN = ITEMS.register("forbidden_soup_bun",
                () -> new ForbiddenSoupBunItem(basicItem().stacksTo(64).rarity(Rarity.UNCOMMON)
                        .food(simpleFoodItemProperties(13, 5)
                                .effect(() ->new MobEffectInstance(NYCTOPHOBIA.get(), 600, 0), 0.3F)
                                .effect(() ->new MobEffectInstance(SENSE_LOSS.get(),600, 0), 0.7F)
                                .build())));

        HIDDEN_PANCAKE = ITEMS.register("hidden_pancake",
                () -> new HiddenPancakeItem(basicItem().stacksTo(64).rarity(Rarity.RARE)
                        .food(simpleFoodItemProperties(12, 7)
                                .effect(() ->new MobEffectInstance(IRON_HIDE.get(), minToTick(1), 4), 1.0F)
                                .effect(() ->new MobEffectInstance(MobEffects.DAMAGE_BOOST, minToTick(2), 1), 1.0F)
                                .build())));

        // ==================== 种子物品 ====================

        ECTOPLASMIC_MELON_SEEDS = ITEMS.register("ectoplasmic_melon_seeds",
                () -> new ItemNameBlockItem(ModBlocks.ECTOPLASMIC_MELON_STEM.get(),
                        new Item.Properties()
                ));
        METAMORPHIC_SCENT_GRASS_SEEDS = ITEMS.register("metamorphic_scent_grass_seeds",
                () -> new ItemNameBlockItem(ModBlocks.METAMORPHIC_SCENT_GRASS.get(),
                        new Item.Properties()
                ));
        // ==================== 杂项物品 ====================
        GHOST_FARMER_SPAWN_EGG = ITEMS.register("ghost_farmer_spawn_egg",
                () -> new ForgeSpawnEggItem(ModEntities.GHOST_FARMER, 0xFFFFFF,0xFFFFFF, new Item.Properties()));

        
    }





    // ==================== 辅助方法 ====================
    public static RegistryObject<Item> registerWithTab(String name, Supplier<Item> supplier) {
        return ITEMS.register(name, supplier);
    }

    private static Supplier<MobEffect> farmersDelightBuff(String effectId) {
        return () -> ForgeRegistries.MOB_EFFECTS.getValue(
                new ResourceLocation("farmersdelight", effectId));
    }

    private static Supplier<MobEffect> goetyBuff(String effectId) {
        return () -> ForgeRegistries.MOB_EFFECTS.getValue(
                new ResourceLocation("goety", effectId));
    }

    private static FoodProperties.Builder simpleFoodItemProperties(int nutrition, float saturationMod) {
        return new FoodProperties
                .Builder()
                .alwaysEat()
                .nutrition(nutrition)
                .saturationMod(saturationMod / nutrition);
    }

    // 修改辅助方法，添加unstackable参数控制是否不可堆叠
    private static Item simpleFoodItem(int nutrition, float saturationMod, boolean unstackable) {
        Item.Properties properties = basicItem();
        if (unstackable) {
            properties = properties.stacksTo(1);
        }
        return new Item(properties.food(
                simpleFoodItemProperties(nutrition, saturationMod).build()));
    }

    private static Item simpleFastFoodItem(int nutrition, float saturationMod, boolean unstackable) {
        Item.Properties properties = basicItem();
        if (unstackable) {
            properties = properties.stacksTo(1);
        }
        return new Item(properties.food(
                simpleFoodItemProperties(nutrition, saturationMod).fast().build()));
    }

    private static Item simpleFoodItem(FoodProperties.Builder builder, boolean unstackable) {
        Item.Properties properties = basicItem();
        if (unstackable) {
            properties = properties.stacksTo(1);
        }
        return new Item(properties.food(builder.build()));
    }

    private static Item simpleFoodItem(int nutrition, float saturationMod,
                                       Supplier<MobEffect> effectSupplier,
                                       int duration, int amplifier, boolean unstackable) {
        Item.Properties properties = basicItem();
        if (unstackable) {
            properties = properties.stacksTo(1);
        }
        FoodProperties.Builder builder = simpleFoodItemProperties(nutrition, saturationMod)
                .effect(() -> new MobEffectInstance(effectSupplier.get(), duration, amplifier), 1.0F);
        return new Item(properties.food(builder.build()));
    }

    private static Item simpleFoodItem(int nutrition, float saturationMod,
                                       MobEffect mobEffect, int duration, int amplifier, boolean unstackable) {
        Item.Properties properties = basicItem();
        if (unstackable) {
            properties = properties.stacksTo(1);
        }
        FoodProperties.Builder builder = simpleFoodItemProperties(nutrition, saturationMod)
                .effect(() -> new MobEffectInstance(mobEffect, duration, amplifier), 1.0F);
        return new Item(properties.food(builder.build()));
    }

    /*
    @Mod.EventBusSubscriber(modid = GoetyDelight.MODID, bus = Mod.EventBusSubscriber.Bus.MOD)
    public class ClientSetup {
        @SubscribeEvent
        public static void onClientSetup(FMLClientSetupEvent event) {
            event.enqueueWork(() -> {
                ItemProperties.register(
                        RegistryItem.EQUIPMENT_ITEM.get(),
                        ResourceLocation.fromNamespaceAndPath(GoetyDelight.MODID,"quality"),
                        (stack, world, entity, seed) -> {
                            if(stack.hasTag()){
                                CompoundTag nbt = stack.getTag();
                                if (nbt ==  null) return 1.0F;
                                if (nbt.contains("quality")){
                                    int quality = nbt.getInt("quality");
                                    switch ( quality){
                                        case 0: return 0.3F;
                                        case 1: return 0.1F;
                                        case 2: return 0.2F;
                                        case 3: return 0.3F;
                                        case 4: return 0.4F;
                                        case 5: return 0.5F;
                                        case 6: return 0.6F;
                                        case 7: return 0.7F;
                                        case 8: return 0.8F;
                                        case 9: return 0.9F;

                                        default: return 1.0F;
                                    }
                                }
                            }
                            return 1.0F;
                        }
                );
            });
        }
    }
    */
    public static void register(IEventBus eventBus) {
        ITEMS.register(eventBus);
    }
}