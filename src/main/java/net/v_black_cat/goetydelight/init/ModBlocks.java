package net.v_black_cat.goetydelight.init;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.material.MapColor;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.v_black_cat.goetydelight.GoetyDelight;
import net.v_black_cat.goetydelight.block.*;

import net.minecraft.world.item.Item;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.state.properties.BlockSetType;
import net.minecraft.world.level.block.state.properties.NoteBlockInstrument;
import net.minecraft.world.level.material.PushReaction;
import net.minecraft.sounds.SoundEvents;

import java.util.Arrays;
import java.util.function.Supplier;
import java.util.function.ToIntFunction;

public class ModBlocks {
    public static final DeferredRegister.Blocks BLOCKS = DeferredRegister.createBlocks(GoetyDelight.MODID);

    public static final DeferredBlock<Block> EXAMPLE_BLOCK = BLOCKS.registerSimpleBlock(
            "example_block",
            BlockBehaviour.Properties.of().mapColor(MapColor.STONE)
    );

    public static final DeferredBlock<Block> MARBLE;
    public static final DeferredBlock<Block> SILT_MARBLE_HEAVY;
    public static final DeferredBlock<Block> BLUE_MARBLE;
    public static final DeferredBlock<Block> JUNGLE_MARBLE;
    public static final DeferredBlock<Block> NETHER_MARBLE;
    public static final DeferredBlock<Block> DRIPMARBLE_BLOCK;
    public static final DeferredBlock<Block> POINTED_DRIPMARBLE;
    public static final DeferredBlock<Block> MARBLE_STAIRS;
    public static final DeferredBlock<Block> MARBLE_SLAB;
    public static final DeferredBlock<Block> MARBLE_BUTTON;
    public static final DeferredBlock<Block> MARBLE_PRESSURE_PLATE;
    public static final DeferredBlock<Block> MARBLE_FENCE;
    public static final DeferredBlock<Block> MARBLE_FENCE_GATE;
    public static final DeferredBlock<Block> MARBLE_WALL;
    public static final DeferredBlock<Block> MARBLE_DOOR;
    public static final DeferredBlock<Block> MARBLE_TRAPDOOR;
    public static final DeferredBlock<Block> CUSTOM_DOLL;
    public static final DeferredBlock<Block> NIGHT_STOVE; // 声明

    public static final DeferredBlock<Block> SHADE_STOVE;

    public static final DeferredBlock<Block> CURSED_INGOT_POT;

    public static final DeferredBlock<Block> ROTTEN_CORPSE_MAGGOT_FEAST_BLOCK;
    public static final DeferredBlock<Block> VOID_GEL_JELLY_BLOCK;
    public static final DeferredBlock<Block> STUFFED_TALL_SKULL_RICE_BLOCK;
    public static final DeferredBlock<Block> LICHS_CHAOS_STEW_BLOCK;
    public static final DeferredBlock<Block> NIGHT_HEART_PEA_SOUP_BLOCK;
    public static final DeferredBlock<Block> BONE_LORD_ASH_RICE_BLOCK;
    public static final DeferredBlock<Block> MENEMEN_BLOCK;
    public static final DeferredBlock<Block> BOAT_STUFFED_ROASTED_WARDEN_BlOCK;
    public static final DeferredBlock<Block> SNAP_UNHOLY_TRIPE_BLOCK;
    public static final DeferredBlock<Block> ROAST_LAOWANG_BLOCK;
    public static final DeferredBlock<Block> ROYAL_CAKE_BLOCK;
    public static final DeferredBlock<Block> RENDER_BLOCK;
    public static final DeferredBlock<Block> APOCALYPTIUM_POT;
    public static final DeferredBlock<Block> ECTOPLASMIC_MELON_BLOCK;
    public static final DeferredBlock<Block> ECTOPLASMIC_MELON_STEM;
    public static final DeferredBlock<Block> METAMORPHIC_SCENT_GRASS;
    public static final DeferredBlock<Block> ATTACHED_ECTOPLASMIC_MELON_STEM;

    static {
        MARBLE = registerBlock("marble",
                () -> new Block(BlockBehaviour.Properties.ofFullCopy(Blocks.IRON_BLOCK).sound(SoundType.AMETHYST)));
        SILT_MARBLE_HEAVY = registerBlock("silt_marble_heavy",
                () -> new Block(BlockBehaviour.Properties.ofFullCopy(Blocks.IRON_BLOCK).sound(SoundType.AMETHYST)));
        BLUE_MARBLE = registerBlock("blue_marble",
                () -> new Block(BlockBehaviour.Properties.ofFullCopy(Blocks.IRON_BLOCK).sound(SoundType.AMETHYST)));
        JUNGLE_MARBLE = registerBlock("jungle_marble",
                () -> new Block(BlockBehaviour.Properties.ofFullCopy(Blocks.IRON_BLOCK).sound(SoundType.AMETHYST)));
        NETHER_MARBLE = registerBlock("nether_marble",
                () -> new Block(BlockBehaviour.Properties.ofFullCopy(Blocks.IRON_BLOCK).sound(SoundType.AMETHYST)));

        DRIPMARBLE_BLOCK = registerBlock("dripmarble_block",
                () -> new Block(BlockBehaviour.Properties.ofFullCopy(Blocks.DRIPSTONE_BLOCK).sound(SoundType.AMETHYST).noOcclusion()));
        POINTED_DRIPMARBLE = registerBlock("pointed_dripmarble",
                () -> new PointedDripstoneBlock(BlockBehaviour.Properties.of()
                        .mapColor(MapColor.TERRACOTTA_BROWN)
                        .forceSolidOn()
                        .instrument(NoteBlockInstrument.BASEDRUM)
                        .noOcclusion()
                        .sound(SoundType.POINTED_DRIPSTONE)
                        .randomTicks()
                        .strength(1.5F, 3.0F)
                        .dynamicShape()
                        .offsetType(BlockBehaviour.OffsetType.XZ)
                        .pushReaction(PushReaction.DESTROY)
                        .isRedstoneConductor(ModBlocks::never)));

        MARBLE_STAIRS = registerBlock("marble_stairs",
                () -> new StairBlock(ModBlocks.MARBLE.get().defaultBlockState(),
                        BlockBehaviour.Properties.ofFullCopy(Blocks.IRON_BLOCK).sound(SoundType.AMETHYST)));
        MARBLE_SLAB = registerBlock("marble_slab",
                () -> new SlabBlock(BlockBehaviour.Properties.ofFullCopy(Blocks.IRON_BLOCK).sound(SoundType.AMETHYST)));

        MARBLE_BUTTON = registerBlock("marble_button",
                () -> new ButtonBlock(BlockSetType.IRON, 10,
                        BlockBehaviour.Properties.ofFullCopy(Blocks.STONE_BUTTON).sound(SoundType.AMETHYST)));
        MARBLE_PRESSURE_PLATE = registerBlock("marble_pressure_plate",
                () -> new PressurePlateBlock(BlockSetType.IRON,
                        BlockBehaviour.Properties.ofFullCopy(Blocks.IRON_BLOCK).sound(SoundType.AMETHYST)));

        MARBLE_FENCE = registerBlock("marble_fence",
                () -> new FenceBlock(BlockBehaviour.Properties.ofFullCopy(Blocks.IRON_BLOCK).sound(SoundType.AMETHYST)));
        MARBLE_FENCE_GATE = registerBlock("marble_fence_gate",
                () -> new FenceGateBlock(BlockBehaviour.Properties.ofFullCopy(Blocks.IRON_BLOCK).sound(SoundType.AMETHYST),
                        SoundEvents.CHAIN_PLACE, SoundEvents.ANVIL_BREAK));
        MARBLE_WALL = registerBlock("marble_wall",
                () -> new WallBlock(BlockBehaviour.Properties.ofFullCopy(Blocks.IRON_BLOCK).sound(SoundType.AMETHYST)));

        MARBLE_DOOR = registerBlock("marble_door",
                () -> new DoorBlock(BlockSetType.IRON,
                        BlockBehaviour.Properties.ofFullCopy(Blocks.IRON_BLOCK).sound(SoundType.AMETHYST).noOcclusion()));
        MARBLE_TRAPDOOR = registerBlock("marble_trapdoor",
                () -> new TrapDoorBlock(BlockSetType.IRON,
                        BlockBehaviour.Properties.ofFullCopy(Blocks.IRON_BLOCK).sound(SoundType.AMETHYST).noOcclusion()));

        CUSTOM_DOLL = BLOCKS.register("custom_doll", CustomDollBlock::new);

        NIGHT_STOVE = registerBlock("night_stove",
                () -> new NightStoveBlock(BlockBehaviour.Properties
                        .ofFullCopy(Blocks.IRON_BLOCK)
                        .sound(SoundType.AMETHYST)
                        .strength(50f, 5000f)
                        .requiresCorrectToolForDrops()
                        .lightLevel(litBlockEmission(13))));

        SHADE_STOVE = registerBlock("shade_stove",
                () -> new ShadeStoveBlock(BlockBehaviour.Properties
                        .ofFullCopy(Blocks.BRICKS)
                        .lightLevel(litBlockEmission(13))));

        CURSED_INGOT_POT = registerBlock("cursed_ingot_pot",
                () -> new CursedIngotPotBlock(BlockBehaviour.Properties.of()
                        .mapColor(MapColor.METAL)
                        .strength(0.5F, 6.0F)
                        .sound(SoundType.LANTERN)));

        ROTTEN_CORPSE_MAGGOT_FEAST_BLOCK = registerBlock("rotten_corpse_maggot_feast_block",
                () -> new RottenCorpseMaggotFeastBlock(BlockBehaviour.Properties.ofFullCopy(Blocks.CAKE),
                        ModItems.ROTTEN_CORPSE_MAGGOT_FEAST, true), 1);

        VOID_GEL_JELLY_BLOCK = registerBlock("void_gel_jelly_block",
                () -> new VoidGelJellyBlock(BlockBehaviour.Properties.ofFullCopy(Blocks.CAKE),
                        ModItems.RING_PACKED_VOID_GEL_JELLY, true), 1);

        STUFFED_TALL_SKULL_RICE_BLOCK = registerBlock("stuffed_tall_skull_rice_block",
                () -> new StuffedTallSkullRiceBlock(BlockBehaviour.Properties.ofFullCopy(Blocks.CAKE),
                        ModItems.STUFFED_TALL_SKULL_RICE, true), 1);

        LICHS_CHAOS_STEW_BLOCK = registerBlock("lichs_chaos_stew_block",
                () -> new LichsChaosStewBlock(BlockBehaviour.Properties.ofFullCopy(Blocks.CAKE),
                        ModItems.LICHS_CHAOS_STEW, true), 1);

        NIGHT_HEART_PEA_SOUP_BLOCK = registerBlock("night_heart_pea_soup_block",
                () -> new NightHeartPeaSoupBlock(BlockBehaviour.Properties.ofFullCopy(Blocks.CAKE),
                        ModItems.NIGHT_HEART_PEA_SOUP, true), 1);

        BONE_LORD_ASH_RICE_BLOCK = registerBlock("bone_lord_ash_rice_block",
                () -> new BoneLordAshRiceBlock(BlockBehaviour.Properties.ofFullCopy(Blocks.CAKE),
                        ModItems.BONE_LORD_ASH_RICE, true), 1);

        MENEMEN_BLOCK = registerBlock("menemen_block",
                () -> new MenemenBlock(BlockBehaviour.Properties.ofFullCopy(Blocks.CAKE),
                        ModItems.MENEMEN_WITH_BREAD, true), 1);

        SNAP_UNHOLY_TRIPE_BLOCK = registerBlock("snap_unholy_tripe_block",
                () -> new SnapUnholyTripeBlock(BlockBehaviour.Properties.ofFullCopy(Blocks.CAKE),
                        ModItems.SNAP_UNHOLY_TRIPE, true), 1);

        BOAT_STUFFED_ROASTED_WARDEN_BlOCK = registerBlock("boat_stuffed_roasted_warden_block",
                () -> new BoatStuffedRoastedWardenBlock(
                        BlockBehaviour.Properties.ofFullCopy(Blocks.CAKE),
                        Arrays.asList(
                                ModItems.BOAT_STUFFED_ROASTED_WARDEN_HEAD,
                                ModItems.BOAT_STUFFED_ROASTED_WARDEN_MEET,
                                ModItems.BOAT_STUFFED_ROASTED_WARDEN_MEET,
                                ModItems.BOAT_STUFFED_ROASTED_WARDEN_MEET,
                                ModItems.BOAT_STUFFED_ROASTED_WARDEN_MEET,
                                ModItems.BOAT_STUFFED_ROASTED_WARDEN_FLANK,
                                ModItems.BOAT_STUFFED_ROASTED_WARDEN_FLANK
                        ),
                        true
                ), 1
        );

        ROAST_LAOWANG_BLOCK = registerBlock("roast_laowang_block",
                () -> new RoastLaowangBlock(
                        BlockBehaviour.Properties.ofFullCopy(Blocks.CAKE),
                        Arrays.asList(
                                ModItems.ROAST_LAOWANG_EAR,
                                ModItems.ROAST_LAOWANG_EAR,
                                ModItems.ROAST_LAOWANG_HEAD,
                                ModItems.ROAST_LAOWANG_FEET,
                                ModItems.ROAST_LAOWANG_FEET,
                                ModItems.ROAST_LAOWANG_LEG,
                                ModItems.ROAST_LAOWANG_LEG,
                                ModItems.ONION_PORK_CHOP_RICE,
                                ModItems.ONION_PORK_CHOP_RICE,
                                ModItems.ONION_PORK_CHOP_RICE,
                                ModItems.ONION_PORK_CHOP_RICE,
                                ModItems.ONION_PORK_CHOP_RICE,
                                ModItems.ECTOPLASMIC_MELON_SALAD
                        ),
                        true
                ), 1
        );

        ROYAL_CAKE_BLOCK = registerBlock("royal_cake_block",
                () -> new RoyalCakeBlock(BlockBehaviour.Properties.of()
                        .forceSolidOn()
                        .strength(0.5F)
                        .sound(SoundType.WOOL)
                        .noLootTable()
                        .pushReaction(PushReaction.DESTROY)), 1);

        RENDER_BLOCK = registerBlock("render_block",
                () -> new RenderBlock(BlockBehaviour.Properties.of()
                        .strength(2.0f)
                        .noOcclusion()
                        .noLootTable()
                        .lightLevel(state -> 15)));

        APOCALYPTIUM_POT = registerBlock("apocalyptium_pot",
                () -> new Block(BlockBehaviour.Properties.ofFullCopy(Blocks.IRON_BLOCK)
                        .noLootTable()
                        .sound(SoundType.AMETHYST)));

        ECTOPLASMIC_MELON_BLOCK = registerBlock("ectoplasmic_melon_block",
                () -> new EctoplasmicMelonBlock(BlockBehaviour.Properties.ofFullCopy(Blocks.MELON)));

        ECTOPLASMIC_MELON_STEM = registerBlockWithoutBlockItem("ectoplasmic_melon_stem",
                () -> new StemBlock(
                        ModBlocks.ECTOPLASMIC_MELON_BLOCK.getKey(),
                        ModBlocks.ECTOPLASMIC_MELON_STEM.getKey(),
                        ModItems.ECTOPLASMIC_MELON_SEEDS.getKey(),
                        BlockBehaviour.Properties.ofFullCopy(Blocks.MELON_STEM)
                                .noOcclusion()
                                .noCollission()
                                .instabreak()
                                .sound(SoundType.CROP)
                                .randomTicks()));

        METAMORPHIC_SCENT_GRASS = registerBlockWithoutBlockItem("metamorphic_scent_grass",
                () -> new MetamorphicScentGrassBlock(
                        BlockBehaviour.Properties.of()
                                .mapColor(MapColor.PLANT)
                                .noCollission()
                                .randomTicks()
                                .instabreak()
                                .sound(SoundType.CROP)
                                .pushReaction(PushReaction.DESTROY)));

        ATTACHED_ECTOPLASMIC_MELON_STEM = registerBlockWithoutBlockItem("attached_ectoplasmic_melon_stem",
                () -> new AttachedStemBlock(
                        ModBlocks.ECTOPLASMIC_MELON_STEM.getKey(),
                        ModBlocks.ECTOPLASMIC_MELON_BLOCK.getKey(),
                        ModItems.ECTOPLASMIC_MELON_SEEDS.getKey(),
                        BlockBehaviour.Properties.ofFullCopy(Blocks.ATTACHED_MELON_STEM)
                                .noCollission()
                                .instabreak()
                                .sound(SoundType.CROP)));
    }

    // ===== 辅助方法（取消注释） =====
    private static ToIntFunction<BlockState> litBlockEmission(int lightValue) {
        return (state) -> state.getValue(BlockStateProperties.LIT) ? lightValue : 0;
    }

    private static boolean never(BlockState state, BlockGetter level, BlockPos pos) {
        return false;
    }

    private static <T extends Block> DeferredBlock<T> registerBlock(String name, Supplier<
                    T> block, int stackSize) {
        DeferredBlock<T> toReturn = BLOCKS.register(name, block);
        registerBlockItem(name, toReturn, stackSize);
        return toReturn;
    }

    private static <T extends Block> DeferredBlock<T> registerBlock(String name, Supplier<
                    T> block) {
        DeferredBlock<T> toReturn = BLOCKS.register(name, block);
        registerBlockItem(name, toReturn);
        return toReturn;
    }

    private static <T extends Block> DeferredHolder<
                    Item, BlockItem> registerBlockItem(String name, DeferredBlock<
                    T> block, int stackSize) {
        return ModItems.ITEMS.register(name, () -> new BlockItem(block.get(), new Item.Properties().stacksTo(stackSize)));
    }

    private static <T extends Block> DeferredHolder<
                    Item, BlockItem> registerBlockItem(String name, DeferredBlock<T> block) {
        return ModItems.ITEMS.register(name, () -> new BlockItem(block.get(), new Item.Properties()));
    }

    private static <T extends Block> DeferredBlock<T> registerBlockWithoutBlockItem(String name, Supplier<T> block) {
        return BLOCKS.register(name, block);
    }

    public static void register(IEventBus modEventBus) {
        BLOCKS.register(modEventBus);
    }
}
