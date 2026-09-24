package net.v_black_cat.goetydelight.compat.goetyrevelation;

import com.Polarice3.Goety.common.effects.GoetyEffects;
import com.Polarice3.Goety.common.items.magic.MagicFocus;
import com.mega.revelationfix.common.item.ModItemTiers;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.Tiers;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import net.v_black_cat.goetydelight.GoetyDelight;
import net.v_black_cat.goetydelight.compat.goetyrevelation.item.*;
import net.v_black_cat.goetydelight.effect.ModEffects;
import net.v_black_cat.goetydelight.spell.MalevolentShrineSpell;
import vectorwing.farmersdelight.common.item.KnifeItem;

import static net.v_black_cat.goetydelight.item.ModItems.NOURISHMENT_EFFECT_SUPPLIER;
import static net.v_black_cat.goetydelight.util.TimeConverter.minToTick;
import static net.v_black_cat.goetydelight.util.TimeConverter.sToTick;
import static vectorwing.farmersdelight.common.registry.ModItems.basicItem;

@Mod.EventBusSubscriber(modid = GoetyRevelationCompat.ID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class RevelationCompatRegistry {

    public static final DeferredRegister<Item> ITEMS =
            DeferredRegister.create(ForgeRegistries.ITEMS, GoetyDelight.MODID);

    /** 本模块的方块注册器（联动方块只在 goety_revelation 存在时注册） */
    public static final DeferredRegister<Block> BLOCKS =
            DeferredRegister.create(ForgeRegistries.BLOCKS, GoetyDelight.MODID);

    /** 启示录方块：神金罐（原注册在主 ModBlocks，按联动拆分到本模块） */
    public static final RegistryObject<Block> APOCALYPTIUM_POT;
    public static final RegistryObject<Item> APOCALYPTIUM_POT_ITEM;

    public static final RegistryObject<Item> APOCALYPTIUM_KNIFE;
    public static final RegistryObject<Item> VENOMOUS_SPIDER_KNIFE;
    public static final RegistryObject<Item> SPECTRE_KNIFE;
    public static final RegistryObject<Item> APOCALYPTIUM_INGOT_BRUSH;
    public static final RegistryObject<Item> STONE_SWORD_SKEWER;
    public static final RegistryObject<Item> PI_PIE;
    public static final RegistryObject<Item> SHARK_GUMMY;
    public static final RegistryObject<Item> APOCALYPTIUM_COD;
    public static final RegistryObject<Item> DOOM_COOKIE;
    public static final RegistryObject<Item> ATONEMENT_VOUCHER_WRAPED_COD;
    public static final RegistryObject<Item> ASCENSION_MOONCAKE;
    public static final RegistryObject<Item> QUIETUS_MARROW;
    public static final RegistryObject<Item> MALEVOLENT_SHRINE_FOCUS;

    static {
        APOCALYPTIUM_POT = BLOCKS.register("apocalyptium_pot", () -> new Block(
                BlockBehaviour.Properties.copy(Blocks.IRON_BLOCK)
                        .noLootTable()
                        .sound(SoundType.AMETHYST)));
        APOCALYPTIUM_POT_ITEM = ITEMS.register("apocalyptium_pot",
                () -> new BlockItem(APOCALYPTIUM_POT.get(), new Item.Properties()));

        APOCALYPTIUM_KNIFE = ITEMS.register("apocalyptium_knife",
                () -> new ApocalyptiumKnifeItem(
                        ModItemTiers.APOCALYPTIUM, -5.0F, -2.0F,
                        basicItem().durability(666).rarity(Rarity.UNCOMMON)));

        VENOMOUS_SPIDER_KNIFE = ITEMS.register("venomous_spider_knife",
                () -> new KnifeItem(Tiers.IRON, 0.5F, -2.0F, basicItem()));

        SPECTRE_KNIFE = ITEMS.register("spectre_knife",
                () -> new KnifeItem(Tiers.IRON, 0.5F, -2.0F, basicItem()));

        APOCALYPTIUM_INGOT_BRUSH = ITEMS.register("apocalyptium_ingot_brush",
                () -> new ApocalyptiumBrushItem(
                        basicItem().durability(166).rarity(Rarity.UNCOMMON)));

        STONE_SWORD_SKEWER = ITEMS.register("stone_sword_skewer",
                () -> new StoneSwordSkewerItem(Tiers.STONE, 4, -2.4F,
                        basicItem().stacksTo(1).rarity(Rarity.EPIC)
                                .food(simpleFoodItemProperties(8, 4).build())));

        PI_PIE = ITEMS.register("pi_pie",
                () -> new PiPieItem(basicItem().stacksTo(1).rarity(Rarity.EPIC).food(
                        simpleFoodItemProperties(15, 10)
                               .effect(() -> new MobEffectInstance(NOURISHMENT_EFFECT_SUPPLIER.get(), minToTick(5), 0), 1.0F)
                               .build())));

        SHARK_GUMMY = ITEMS.register("shark_gummy",
                () -> new SharkGummyItem(basicItem().stacksTo(1).rarity(Rarity.EPIC).food(
                        simpleFoodItemProperties(10, 7)
                                .effect(() -> new MobEffectInstance(NOURISHMENT_EFFECT_SUPPLIER.get(), minToTick(3), 0), 1.0F)
                                .build())));

        APOCALYPTIUM_COD = ITEMS.register("apocalyptium_cod",
                () -> new ApocalyptiumCodItem(basicItem().stacksTo(1).rarity(Rarity.EPIC).food(
                        simpleFoodItemProperties(12, 8)
                                .effect(() -> new MobEffectInstance(NOURISHMENT_EFFECT_SUPPLIER.get(), minToTick(6), 0), 1.0F)
                                .build())));

        DOOM_COOKIE = ITEMS.register("doom_cookie",
                () -> new DoomCookieItem(
                        basicItem().stacksTo(16).rarity(Rarity.UNCOMMON).food(
                        simpleFoodItemProperties(6, 4)
                                .effect(() -> new MobEffectInstance(GoetyEffects.DOOM.get(), minToTick(1), 19), 1.0F)
                                .build())));

        ATONEMENT_VOUCHER_WRAPED_COD = ITEMS.register("atonement_voucher_wraped_cod",
                () -> new AtonementVoucherWrapedCodItem(
                        basicItem().stacksTo(16).rarity(Rarity.UNCOMMON).food(
                        simpleFoodItemProperties(10, 7)
                                .effect(() -> new MobEffectInstance(NOURISHMENT_EFFECT_SUPPLIER.get(), minToTick(10), 0), 1.0F)
                                .build())));

        ASCENSION_MOONCAKE = ITEMS.register("ascension_mooncake",
                () -> new AscensionMooncakeItem(basicItem().stacksTo(1).rarity(Rarity.EPIC).food(
                        simpleFoodItemProperties(66, 333)
                                .effect(() -> new MobEffectInstance(NOURISHMENT_EFFECT_SUPPLIER.get(), sToTick(66), 0), 1.0F)
                                .effect(() -> new MobEffectInstance(MobEffects.REGENERATION, sToTick(66), 2), 1.0F)
                                .effect(() -> new MobEffectInstance(MobEffects.DAMAGE_RESISTANCE, sToTick(66), 3), 1.0F)
                                .effect(() -> new MobEffectInstance(ModEffects.THE_PALE_MESSRNGER.get(), sToTick(66), 0), 1.0F)
                                .build())));

        QUIETUS_MARROW = ITEMS.register("quietus_marrow",
                () -> new QuietusMarrowItem(basicItem().stacksTo(16).rarity(Rarity.EPIC).food(
                        simpleFoodItemProperties(5, 0)
                                 .effect(() -> new MobEffectInstance(com.mega.revelationfix.common.init.ModEffects.QUIETUS.get(), -1, 1), 1.0F)
                                 .effect(() -> new MobEffectInstance(net.v_black_cat.goetydelight.effect.ModEffects.FASTING.get(), -1, 0), 1.0F)
                                 .build())));

        MALEVOLENT_SHRINE_FOCUS = ITEMS.register("malevolent_shrine_focus",
                () -> new PetroleumMistFocus(new MalevolentShrineSpell()));
    }

    private static FoodProperties.Builder simpleFoodItemProperties(int nutrition, float saturationMod) {
        return new FoodProperties.Builder()
                .alwaysEat()
                .nutrition(nutrition)
                .saturationMod(saturationMod / nutrition);
    }

    public static void register(IEventBus eventBus) {
        BLOCKS.register(eventBus);
        ITEMS.register(eventBus);
    }
}