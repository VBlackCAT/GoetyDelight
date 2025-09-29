package net.v_black_cat.goetydelight.item;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.*;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import net.v_black_cat.goetydelight.GoetyDelight;
import net.v_black_cat.goetydelight.effect.ModEffects;
import vectorwing.farmersdelight.common.item.KnifeItem;

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

    // 刀具物品
    public static final RegistryObject<Item> APOCALYPTIUM_KNIFE0;
    public static final RegistryObject<Item> VENOMOUS_SPIDER_KNIFE;
    public static final RegistryObject<Item> SPECTRE_KNIFE;
    public static final RegistryObject<Item> APOCALYPTIUM_KNIFE2;
    public static final RegistryObject<Item> APOCALYPTIUM_KNIFE1;
    public static final RegistryObject<Item> BLACK_IRON_KNIFE;
    public static final RegistryObject<Item> DARK_KNIFE;

    // 武器物品
    public static final RegistryObject<Item> Marble_OP_SWORD;

    // 基础食物物品
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
    public static final RegistryObject<Item> CANDY_FISH;
    public static final RegistryObject<Item> GRAPE_SLUSH;
    public static final RegistryObject<Item> FROG_LEG_SANDWICH;
    public static final RegistryObject<Item> SPIDER_EGG_BUBBLE_TEA;
    public static final RegistryObject<Item> SPIDER_EGG_BUBBLE_TEA_2;
    public static final RegistryObject<Item> SAUCE_GRILLED_CANDY_FISH;

    // 特殊效果食物物品
    public static final RegistryObject<Item> CRYING_SHARK_SUGAR_PACK;
    public static final RegistryObject<Item> SEVEN_LEAF_PUDDING;
    public static final RegistryObject<Item> BEAR_PAW;
    public static final RegistryObject<Item> CAKE;
    public static final RegistryObject<Item> OMINOUS_ICE_CREAM;
    public static final RegistryObject<Item> ECTOPLASMIC_MELON;
    public static final RegistryObject<Item> BLUE_ECTOPLASMIC_SUNDAE;
    public static final RegistryObject<Item> BRUSH;
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

    // ==================== 静态初始化块：物品定义区域 ====================
    static {
        // 块物品初始化
        EXAMPLE_BLOCK_ITEM = ITEMS.register("example_block",
                () -> new BlockItem(EXAMPLE_BLOCK.get(), basicItem().stacksTo(1))); 


        APOCALYPTIUM_KNIFE0 = registerWithTab("apocalyptium_knife0",
                () -> new KnifeItem(Tiers.IRON, 0.5F, -2.0F, basicItem()));
        VENOMOUS_SPIDER_KNIFE = registerWithTab("venomous_spider_knife",
                () -> new KnifeItem(Tiers.IRON, 0.5F, -2.0F, basicItem()));
        SPECTRE_KNIFE = registerWithTab("spectre_knife",
                () -> new KnifeItem(Tiers.IRON, 0.5F, -2.0F, basicItem()));
        APOCALYPTIUM_KNIFE2 = registerWithTab("apocalyptium_knife2",
                () -> new KnifeItem(Tiers.IRON, 0.5F, -2.0F, basicItem()));
        APOCALYPTIUM_KNIFE1 = registerWithTab("apocalyptium_knife1",
                () -> new KnifeItem(Tiers.IRON, 0.5F, -2.0F, basicItem()));
        BLACK_IRON_KNIFE = registerWithTab("black_iron_knife",
                () -> new KnifeItem(Tiers.IRON, 0.5F, -2.0F, basicItem()));
        DARK_KNIFE = registerWithTab("dark_knife",
                () -> new KnifeItem(Tiers.IRON, 0.5F, -2.0F, basicItem()));


        Marble_OP_SWORD = ITEMS.register("marble_op_sword",
                () -> new SwordItem(Tiers.WOOD, Integer.MAX_VALUE, 2, basicItem()));


        EXAMPLE_ITEM = ITEMS.register("example_item",
                () -> new Item(basicItem().stacksTo(1)
                        .food(simpleFoodItemProperties(1, 2f).build())));
        GOETYDELIGHT_ICON = ITEMS.register("goetydelight_icon",
                () -> simpleFoodItem(666, 666, true)); 

        TAINTED_DRINK = ITEMS.register("tainted_drink",
                () -> new CustomDrinkItem(basicItem().stacksTo(1).food(
                        simpleFoodItemProperties(4, 4)
                                .effect(() -> new MobEffectInstance(MobEffects.REGENERATION, 600, 3), 1.0F)
                                .effect(() -> new MobEffectInstance(MobEffects.DAMAGE_RESISTANCE, 1200, 3), 1.0F)
                                .effect(() -> new MobEffectInstance(ModEffects.TAINTED_DRINK.get(), minToTick(15), 0), 1.0F)
                                .effect(() -> new MobEffectInstance(ModEffects.TAINTED_PIG.get(), minToTick(30), 0), 1.0F)
                                .build())));

        REJECTED_DARK_MEAT_SOUP = ITEMS.register("rejected_dark_meat_soup",
                () -> new RejectedDarkMeatSoupItem(basicItem().stacksTo(1).food(
                        simpleFoodItemProperties(10, 4)
                                .effect(() -> new MobEffectInstance(MobEffects.CONFUSION, 600, 0), 1.0F)
                                .effect(() -> {
                                    int randomAmplifier = new java.util.Random().nextInt(5);
                                    return new MobEffectInstance(MobEffects.POISON, 600, randomAmplifier, false, true);
                                }, 1.0F)
                                .effect(() -> new MobEffectInstance(MobEffects.WEAKNESS, 600, 1), 1.0F)
                                .build())));

        PROMOTION_HARD_CANDY = ITEMS.register("promotion_hard_candy",
                () -> simpleFoodItem(1, 1, true)); 
        CUP = ITEMS.register("cup",
                () -> simpleFoodItem(1, 1, true)); 
        TOXIC_MEAL = ITEMS.register("toxic_meal",
                () -> new ToxicMealItem(basicItem().stacksTo(1).food(
                        simpleFoodItemProperties(8, 4)
                                .effect(() -> new MobEffectInstance(MobEffects.CONFUSION, 2000, 0), 1.0F)
                                .effect(() -> new MobEffectInstance(MobEffects.POISON, 2000, 9), 1.0F)
                                .effect(() -> new MobEffectInstance(MobEffects.WEAKNESS, 2000, 4), 1.0F)
                                .build())));
        POACHED_NETHER_WART_EGG = ITEMS.register("poached_nether_wart_egg",
                () -> new PoachedNetherWartEggItem(basicItem().food( 
                        simpleFoodItemProperties(7, 4).fast().build())));
        ECTOPLASM_JELLY = ITEMS.register("ectoplasm_jelly",
                () -> simpleFastFoodItem(4, 6, true)); 

        WHITE_SHARK_CANDY = ITEMS.register("white_shark_candy",
                () -> simpleFoodItem(1, 1, true)); 
        WHITE_SHARK_SUGAR_PACK = ITEMS.register("white_shark_sugar_pack",
                () -> simpleFoodItem(1, 1, true)); 
        CANDY_FISH = ITEMS.register("candy_fish",
                () -> simpleFoodItem(1, 1, true)); 
        GRAPE_SLUSH = ITEMS.register("grape_slush",
                () -> simpleFoodItem(1, 1, true)); 
        FROG_LEG_SANDWICH = ITEMS.register("frog_leg_sandwich",
                () -> simpleFoodItem(8, 8, true)); 
        SPIDER_EGG_BUBBLE_TEA = ITEMS.register("spider_egg_bubble_tea",
                () -> simpleFoodItem(1, 1, true)); 
        SPIDER_EGG_BUBBLE_TEA_2 = ITEMS.register("spider_egg_bubble_tea_2",
                () -> simpleFoodItem(1, 1, true)); 
        SAUCE_GRILLED_CANDY_FISH = ITEMS.register("sauce_grilled_candy_fish",
                () -> simpleFoodItem(1, 1, true)); 

        // 特殊效果食物物品初始化



        SIBLING_SUNDAE = ITEMS.register("sibling_sundae",
                () -> new SiblingSundaeItem(basicItem().food(
                        simpleFoodItemProperties(6, 5)
                                .effect(() -> new MobEffectInstance(INSIGHT.get(), minToTick(2.5F), 3), 1.0F)
                                .build())));


        ROASTED_CORPSE_MAGGOTS = ITEMS.register("roasted_corpse_maggots",
                () -> new RoastedCorpseMaggotsitem(basicItem().food(
                        simpleFoodItemProperties(5, 2)
                                .build())));


        CORPSE_MAGGOT = ITEMS.register("corpse_maggot",
                () -> new CorpseMaggotItem(basicItem().food(
                        simpleFoodItemProperties(3, 1)
                                .effect(() -> new MobEffectInstance(MobEffects.CONFUSION, sToTick(10), 0), 1.0F)
                                .build())));

        CRYING_SHARK_SUGAR_PACK = ITEMS.register("crying_shark_sugar_pack",
                () -> new Item(basicItem().food(
                        simpleFoodItemProperties(7, 5)
                                .effect(() -> new MobEffectInstance(MobEffects.REGENERATION, minToTick(1), 1), 1.0F)
                                .effect(() -> new MobEffectInstance(NOURISHMENT_EFFECT_SUPPLIER.get(), minToTick(5), 0), 1.0F)
                                .build())));


        SEVEN_LEAF_PUDDING = ITEMS.register("seven_leaf_pudding",
                () -> new SevenLeafPuddingItem(basicItem().food(
                        simpleFoodItemProperties(7, 9)
                                .effect(() -> new MobEffectInstance(MobEffects.REGENERATION, minToTick(5), 1), 1.0F)
                                .effect(() -> new MobEffectInstance(NOURISHMENT_EFFECT_SUPPLIER.get(), minToTick(5), 0), 1.0F)
                                .build())));


        BEAR_PAW = ITEMS.register("bear_paw",
                () -> new Item(basicItem().food( 
                        simpleFoodItemProperties(10, 16)
                                .effect(() -> new MobEffectInstance(NOURISHMENT_EFFECT_SUPPLIER.get(), 6000, 0), 1.0F)
                                .effect(() -> new MobEffectInstance(RAMPAGE_EFFECT_SUPPLIER.get(), 2400, 0), 1.0F)
                                .build())));
        CAKE = ITEMS.register("cake",
                () -> new CakeItem(basicItem().stacksTo(1).food(
                        simpleFoodItemProperties(16, 20)
                                .effect(() -> new MobEffectInstance(NOURISHMENT_EFFECT_SUPPLIER.get(), 36000, 0), 1.0F)
                                .effect(() -> new MobEffectInstance(FORTUNATE_EFFECT_SUPPLIER.get(), 12000, 0), 1.0F)
                                .build())));
        OMINOUS_ICE_CREAM = ITEMS.register("ominous_ice_cream",
                () -> new OminousIceCreamItem(basicItem()
                        .stacksTo(1) 
                        .food(
                                simpleFoodItemProperties(8, 10)
                                        .effect(() -> new MobEffectInstance(MobEffects.BAD_OMEN, 6000, 4), 1.0F)
                                        .effect(() -> new MobEffectInstance(CHILL_HIDE_EFFECT_SUPPLIER.get(), 1200, 0), 1.0F)
                                        .build())));
        ECTOPLASMIC_MELON = ITEMS.register("ectoplasmic_melon",
                () -> new Item(basicItem().food( 
                        simpleFoodItemProperties(3, 1)
                                .effect(() -> new MobEffectInstance(CHILL_HIDE_EFFECT_SUPPLIER.get(), 100, 0), 1.0F)
                                .build())));
        BLUE_ECTOPLASMIC_SUNDAE = ITEMS.register("blue_ectoplasmic_sundae",
                () -> new Item(basicItem().stacksTo(1).food( 
                        simpleFoodItemProperties(10, 12)
                                .effect(() -> new MobEffectInstance(FORTUNATE_EFFECT_SUPPLIER.get(), 12000, 0), 1.0F)
                                .effect(() -> new MobEffectInstance(MobEffects.REGENERATION, 2400, 0), 1.0F)
                                .effect(() -> new MobEffectInstance(CHILL_HIDE_EFFECT_SUPPLIER.get(), 2400, 0), 1.0F)
                                .build())));
        BRUSH = ITEMS.register("brush",
                () -> new CursedIngotBrushItem(basicItem().durability(64)));
        SKULL_SHOT = ITEMS.register("skull_shot",
                () -> simpleFoodItem(6, 7, CORPSE_EATER_EFFECT_SUPPLIER, 1200, 0, true)); 
        NIGHT_HEART_PEA_SOUP = ITEMS.register("night_heart_pea_soup",
                () -> new NightHeartPeaSoupItem(basicItem().stacksTo(1).food( 
                        simpleFoodItemProperties(16, 24)
                                .effect(() -> new MobEffectInstance(MobEffects.DAMAGE_BOOST, minToTick(5), 4), 1.0F)
                                .effect(() -> new MobEffectInstance(SHADOW_WALK.get(), sToTick(60), 3), 1.0F)
                                .effect(() -> new MobEffectInstance(MobEffects.DAMAGE_RESISTANCE, 6000, 2), 1.0F)
                                .effect(() -> new MobEffectInstance(NOURISHMENT_EFFECT_SUPPLIER.get(), 12000, 0), 1.0F)
                                .effect(() -> new MobEffectInstance(COMFORT_EFFECT_SUPPLIER.get(), 12000, 0), 1.0F)
                                .build())));
        POACHED_SPIDER_EGG = ITEMS.register("poached_spider_egg",
                () -> new Item(basicItem().food( 
                        simpleFoodItemProperties(5, 2)
                                .effect(() -> new MobEffectInstance(CLIMBING_EFFECT_SUPPLIER.get(), 200, 0), 1.0F)
                                .fast()
                                .build())));
        GRILL_FROG_LEG = ITEMS.register("grill_frog_leg",
                () -> new Item(basicItem().stacksTo(1).food( 
                        simpleFoodItemProperties(10, 16)
                                .effect(() -> new MobEffectInstance(FROG_LEG_EFFECT_SUPPLIER.get(), 1200, 0), 1.0F)
                                .effect(() -> new MobEffectInstance(MobEffects.REGENERATION, 400, 1), 1.0F)
                                .build())));
        FRENZIED_FUNGUS_POP_ROCKS = ITEMS.register("frenzied_fungus_pop_rocks",
                () -> new FrenziedFungusPopRocksItem(basicItem().stacksTo(1).food(
                        simpleFoodItemProperties(6, 8)
                                .build())));
        SOUL_CONVERGENCE_ROOM = ITEMS.register("soul_convergence_room",
                () -> new Item(basicItem().food( 
                        simpleFoodItemProperties(8, 12)
                                .effect(() -> new MobEffectInstance(MobEffects.HUNGER, 200, 0), 1.0F)
                                .effect(() -> new MobEffectInstance(SOUL_ARMOR_EFFECT_SUPPLIER.get(), 1200, 1), 1.0F)
                                .build())));
        SOUL_CONVERGENCE_ROOM_2 = ITEMS.register("soul_convergence_room_2",
                () -> new Item(basicItem().food( 
                        simpleFoodItemProperties(20, 30)
                                .effect(() -> new MobEffectInstance(SOUL_ARMOR_EFFECT_SUPPLIER.get(), 6000, 4), 1.0F)
                                .effect(() -> new MobEffectInstance(MobEffects.HEALTH_BOOST, 6000, 2), 1.0F)
                                .effect(() -> new MobEffectInstance(ModEffects.SOUL_CONVERGENCE_ROOM.get(), minToTick(10), 0), 1.0F)
                                .build())));
        BONE_LORD_ASH_RICE = ITEMS.register("bone_lord_ash_rice",
                () -> new BoneLordAshRiceItem(basicItem().stacksTo(1).food( 
                        simpleFoodItemProperties(12, 16)
                                .effect(() -> new MobEffectInstance(CHILL_HIDE_EFFECT_SUPPLIER.get(), 6000, 1), 1.0F)
                                .effect(() -> new MobEffectInstance(BUFF_EFFECT_SUPPLIER.get(), 6000, 2), 1.0F)
                                .effect(() -> new MobEffectInstance(NOURISHMENT_EFFECT_SUPPLIER.get(), minToTick(5), 0), 1.0F)
                                .effect(() -> new MobEffectInstance(CORPSE_EATER.get(), minToTick(1), 2), 1.0F)
                                .build())));
        RUBY_HARD_CANDY = ITEMS.register("ruby_hard_candy",
                () -> new RubyHardCandyItem(basicItem().stacksTo(1).food( 
                        simpleFoodItemProperties(10, 16)
                                .effect(() -> new MobEffectInstance(ModEffects.SPELL_MASTERY.get(), minToTick(30), 2), 1.0F)
                                .effect(() -> new MobEffectInstance(ModEffects.SPELL_DURATION.get(), minToTick(10), 2), 1.0F)
                                .effect(() -> new MobEffectInstance(MobEffects.REGENERATION, minToTick(8), 1), 1.0F)
                                .effect(() -> new MobEffectInstance(NOURISHMENT_EFFECT_SUPPLIER.get(), minToTick(15), 0), 1.0F)
                                .build())));
        CRISP_BISCUIT = ITEMS.register("crisp_biscuit",
                () -> new CrispBiscuitItem(basicItem().food( 
                        simpleFoodItemProperties(9, 12)
                                .effect(() -> new MobEffectInstance(CORPSE_EATER_EFFECT_SUPPLIER.get(), sToTick(30), 0), 1.0F)
                                .effect(() -> new MobEffectInstance(NOURISHMENT_EFFECT_SUPPLIER.get(), minToTick(5), 2), 1.0F)
                                .effect(() -> new MobEffectInstance(ModEffects.SPELL_MASTERY.get(), minToTick(2), 0), 1.0F)
                                .effect(() -> new MobEffectInstance(SAVE_EFFECTS_SUPPLIER.get(), minToTick(30), 0), 1.0F)
                                .build())));
        ROTTEN_CORPSE_MAGGOT_FEAST = ITEMS.register("rotten_corpse_maggot_feast",
                () -> new RottenCorpseMaggotFeastItem(basicItem().stacksTo(1).food( 
                        simpleFoodItemProperties(16, 20)
                                .effect(() -> new MobEffectInstance(NOURISHMENT_EFFECT_SUPPLIER.get(), minToTick(10), 0), 1.0F)
                                .build())));
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

    public static void register(IEventBus eventBus) {
        ITEMS.register(eventBus);
    }
}