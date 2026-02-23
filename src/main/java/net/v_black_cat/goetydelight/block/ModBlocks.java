package net.v_black_cat.goetydelight.block;



import net.minecraft.core.BlockPos;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockSetType;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.NoteBlockInstrument;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.level.material.PushReaction;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import net.v_black_cat.goetydelight.GoetyDelight;
import net.v_black_cat.goetydelight.item.ModItems;
import vectorwing.farmersdelight.common.block.CookingPotBlock;
import vectorwing.farmersdelight.common.block.RoastChickenBlock;
import vectorwing.farmersdelight.common.block.StoveBlock;

import java.util.Arrays;
import java.util.function.Supplier;
import java.util.function.ToIntFunction;

import static vectorwing.farmersdelight.common.registry.ModBlocks.STOVE;

public class ModBlocks {
    public static final DeferredRegister<Block> BLOCKS =
            DeferredRegister.create(ForgeRegistries.BLOCKS, GoetyDelight.MODID);

//    public static final RegistryObject<Block> MARBLE_BLOCK = registerBlock("marble_block",
//            () -> new Block(BlockBehaviour.Properties.copy(Blocks.IRON_BLOCK).sound(SoundType.AMETHYST)));


    public static final RegistryObject<Block> EXAMPLE_BLOCK = BLOCKS.register("example_block", () -> new Block(BlockBehaviour.Properties.of().mapColor(MapColor.STONE).noLootTable()));

    public static final RegistryObject<Block> MARBLE = registerBlock("marble",() -> new Block(BlockBehaviour.Properties.copy(Blocks.IRON_BLOCK).sound(SoundType.AMETHYST)));
    public static final RegistryObject<Block> SILT_MARBLE_HEAVY = registerBlock("silt_marble_heavy",() -> new Block(BlockBehaviour.Properties.copy(Blocks.IRON_BLOCK).sound(SoundType.AMETHYST)));
    public static final RegistryObject<Block> BLUE_MARBLE= registerBlock("blue_marble",() -> new Block(BlockBehaviour.Properties.copy(Blocks.IRON_BLOCK).sound(SoundType.AMETHYST)));
    public static final RegistryObject<Block> JUNGLE_MARBLE = registerBlock("jungle_marble",() -> new Block(BlockBehaviour.Properties.copy(Blocks.IRON_BLOCK).sound(SoundType.AMETHYST)));
    public static final RegistryObject<Block> NETHER_MARBLE = registerBlock("nether_marble",() -> new Block(BlockBehaviour.Properties.copy(Blocks.IRON_BLOCK).sound(SoundType.AMETHYST)));


    public static final RegistryObject<Block> DRIPMARBLE_BLOCK = registerBlock("dripmarble_block",() -> new Block(BlockBehaviour.Properties.copy(Blocks.DRIPSTONE_BLOCK).sound(SoundType.AMETHYST).noOcclusion()));
    public static final RegistryObject<Block> POINTED_DRIPMARBLE = registerBlock("pointed_dripmarble",() ->  new PointedDripstoneBlock(BlockBehaviour.Properties.of().mapColor(MapColor.TERRACOTTA_BROWN).forceSolidOn().instrument(NoteBlockInstrument.BASEDRUM).noOcclusion().sound(SoundType.POINTED_DRIPSTONE).randomTicks().strength(1.5F, 3.0F).dynamicShape().offsetType(BlockBehaviour.OffsetType.XZ).pushReaction(PushReaction.DESTROY).isRedstoneConductor(ModBlocks::never)));
   
    public static final RegistryObject<Block> MARBLE_STAIRS = registerBlock("marble_stairs",() -> new StairBlock(() -> ModBlocks.MARBLE.get().defaultBlockState(), BlockBehaviour.Properties.copy(Blocks.IRON_BLOCK).sound(SoundType.AMETHYST)));
    public static final RegistryObject<Block> MARBLE_SLAB = registerBlock("marble_slab", () -> new SlabBlock(BlockBehaviour.Properties.copy(Blocks.IRON_BLOCK).sound(SoundType.AMETHYST)));

    public static final RegistryObject<Block> MARBLE_BUTTON = registerBlock("marble_button",() -> new ButtonBlock(BlockBehaviour.Properties.copy(Blocks.STONE_BUTTON).sound(SoundType.AMETHYST),BlockSetType.IRON, 10, true));
    public static final RegistryObject<Block> MARBLE_PRESSURE_PLATE = registerBlock("marble_pressure_plate", () -> new PressurePlateBlock(PressurePlateBlock.Sensitivity.EVERYTHING, BlockBehaviour.Properties.copy(Blocks.IRON_BLOCK).sound(SoundType.AMETHYST), BlockSetType.IRON));

    public static final RegistryObject<Block> MARBLE_FENCE = registerBlock("marble_fence", () -> new FenceBlock(BlockBehaviour.Properties.copy(Blocks.IRON_BLOCK).sound(SoundType.AMETHYST)));
    public static final RegistryObject<Block> MARBLE_FENCE_GATE = registerBlock("marble_fence_gate", () -> new FenceGateBlock(BlockBehaviour.Properties.copy(Blocks.IRON_BLOCK).sound(SoundType.AMETHYST), SoundEvents.CHAIN_PLACE, SoundEvents.ANVIL_BREAK));
    public static final RegistryObject<Block> MARBLE_WALL = registerBlock("marble_wall", () -> new WallBlock(BlockBehaviour.Properties.copy(Blocks.IRON_BLOCK).sound(SoundType.AMETHYST)));

    public static final RegistryObject<Block> MARBLE_DOOR = registerBlock("marble_door", () -> new DoorBlock(BlockBehaviour.Properties.copy(Blocks.IRON_BLOCK).sound(SoundType.AMETHYST).noOcclusion(), BlockSetType.IRON));
    public static final RegistryObject<Block> MARBLE_TRAPDOOR = registerBlock("marble_trapdoor", () -> new TrapDoorBlock(BlockBehaviour.Properties.copy(Blocks.IRON_BLOCK).sound(SoundType.AMETHYST).noOcclusion(), BlockSetType.IRON));


    //暗夜炉灶
    public static final RegistryObject<Block> NIGHT_STOVE = registerBlock("night_stove",() ->
            new NightStoveBlock(BlockBehaviour.Properties
                    .copy(Blocks.IRON_BLOCK)
                    .sound(SoundType.AMETHYST)
                    .strength(50f, 5000f) // 设置硬度和爆炸抗性
                    .requiresCorrectToolForDrops() //需要json中配置的正确工具才能掉落
                    .lightLevel(litBlockEmission(13))));


    //阴影炉灶
    public static final RegistryObject<Block> SHADE_STOVE = registerBlock("shade_stove",() ->
            new ShadeStoveBlock(BlockBehaviour.Properties
                    .copy(Blocks.BRICKS)
                    .lightLevel(litBlockEmission(13))));

    //诅咒金属锅
    public static final RegistryObject<Block> CURSED_INGOT_POT = registerBlock("cursed_ingot_pot",() ->
            new CursedIngotPotBlock(BlockBehaviour.Properties.of()
                    .mapColor(MapColor.METAL)
                    .strength(0.5F, 6.0F)
                    .sound(SoundType.LANTERN)));


    //腐尸蛆虫盛宴方块
    public static final RegistryObject<Block> ROTTEN_CORPSE_MAGGOT_FEAST_BLOCK = registerBlock("rotten_corpse_maggot_feast_block",() ->
            new RottenCorpseMaggotFeastBlock(BlockBehaviour.Properties.copy(Blocks.CAKE),
                    ModItems.ROTTEN_CORPSE_MAGGOT_FEAST, true),1);
    //虚空果冻
    public static final RegistryObject<Block> VOID_GEL_JELLY_BLOCK = registerBlock("void_gel_jelly_block",() ->
            new VoidGelJellyBlock(BlockBehaviour.Properties.copy(Blocks.CAKE),
                    ModItems.RING_PACKED_VOID_GEL_JELLY, true),1);

    //填馅高头骨
    public static final RegistryObject<Block> STUFFED_TALL_SKULL_RICE_BLOCK = registerBlock("stuffed_tall_skull_rice_block",() ->
            new StuffedTallSkullRiceBlock(BlockBehaviour.Properties.copy(Blocks.CAKE),
                    ModItems.STUFFED_TALL_SKULL_RICE, true),1);

    public static final RegistryObject<Block> LICHS_CHAOS_STEW_BLOCK = registerBlock("lichs_chaos_stew_block",() ->
            new LichsChaosStewBlock(BlockBehaviour.Properties.copy(Blocks.CAKE),
                    ModItems.LICHS_CHAOS_STEW, true),1);

    public static final RegistryObject<Block> NIGHT_HEART_PEA_SOUP_BLOCK = registerBlock("night_heart_pea_soup_block",() ->
            new NightHeartPeaSoupBlock(BlockBehaviour.Properties.copy(Blocks.CAKE),
                    ModItems.NIGHT_HEART_PEA_SOUP, true),1);

    public static final RegistryObject<Block> BONE_LORD_ASH_RICE_BLOCK = registerBlock("bone_lord_ash_rice_block",() ->
            new BoneLordAshRiceBlock(BlockBehaviour.Properties.copy(Blocks.CAKE),
                    ModItems.BONE_LORD_ASH_RICE, true),1);

    //坚守者
    public static final RegistryObject<Block>  BOAT_STUFFED_ROASTED_WARDEN_BlOCK= registerBlock("boat_stuffed_roasted_warden_block",
            () -> new BoatStuffedRoastedWardenBlock(
                    BlockBehaviour.Properties.copy(Blocks.CAKE),
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
            ),1
    );
    public static final RegistryObject<Block> ROYAL_CAKE_BLOCK = registerBlock("royal_cake_block",() ->
            new RoyalCakeBlock(BlockBehaviour.Properties.of()
                    .forceSolidOn()
                    .strength(0.5F)
                    .sound(SoundType.WOOL)
                    .noLootTable()
                    .pushReaction(PushReaction.DESTROY)), 1);


    public static final RegistryObject<Block> RENDER_BLOCK = registerBlock("render_block",() ->
            new RenderBlock(BlockBehaviour.Properties.of()
                    .strength(2.0f)
                    .noOcclusion()
                    .noLootTable()
                    .lightLevel(state -> 15)));

    public static final RegistryObject<Block> APOCALYPTIUM_POT = registerBlock("apocalyptium_pot",() ->
            new Block(BlockBehaviour.Properties.copy(Blocks.IRON_BLOCK)
                    .noLootTable()
                    .sound(SoundType.AMETHYST)));

    //灵质瓜
    public static final RegistryObject<Block> ECTOPLASMIC_MELON_BLOCK = registerBlock("ectoplasmic_melon_block",
            () -> new EctoplasmicMelonBlock(BlockBehaviour.Properties.copy(Blocks.MELON)));

    //灵质瓜藤
    public static final RegistryObject<Block> ECTOPLASMIC_MELON_STEM = registerBlockWithoutBlockItem("ectoplasmic_melon_stem",
            () -> new StemBlock((StemGrownBlock)ECTOPLASMIC_MELON_BLOCK.get(),
                    () -> ModItems.ECTOPLASMIC_MELON_SEEDS.get(),
                    BlockBehaviour.Properties.copy(Blocks.MELON_STEM).noOcclusion()
                    .noCollission() // 无碰撞箱
                    .instabreak() // 瞬间破坏
                    .sound(SoundType.CROP) // 作物音效
                    .randomTicks() // 需要随机刻
            ));
    //幻味草
    public static final RegistryObject<Block> METAMORPHIC_SCENT_GRASS = registerBlockWithoutBlockItem("metamorphic_scent_grass",
            () -> new MetamorphicScentGrassBlock(
                  BlockBehaviour.Properties.of()
                          .mapColor(MapColor.PLANT)
                          .noCollission().randomTicks().instabreak()
                          .sound(SoundType.CROP).pushReaction(PushReaction.DESTROY)));

    //灵质瓜茎
    public static final RegistryObject<Block> ATTACHED_ECTOPLASMIC_MELON_STEM = registerBlockWithoutBlockItem("attached_ectoplasmic_melon_stem",
            () -> new AttachedStemBlock((StemGrownBlock)ECTOPLASMIC_MELON_BLOCK.get(),
                    () -> ModItems.ECTOPLASMIC_MELON_SEEDS.get(),
                    BlockBehaviour.Properties.copy(Blocks.ATTACHED_MELON_STEM)
                            .noCollission()
                            .instabreak()
                            .sound(SoundType.CROP)

            ));

    public static final RegistryObject<Block> RESTAURANT = registerBlock("restaurant",
            () -> new RestaurantBlock(BlockBehaviour.Properties.copy(Blocks.IRON_BLOCK)
                    .noLootTable()
                    .sound(SoundType.AMETHYST)));



    private static ToIntFunction<BlockState> litBlockEmission(int lightValue) {
        return (state) -> {
            return (Boolean)state.getValue(BlockStateProperties.LIT) ? lightValue : 0;
        };
    }

    private static <T extends Block> RegistryObject<T> registerBlock(String name, Supplier<T> block, int stackSize) {
        RegistryObject<T> toReturn = BLOCKS.register(name, block);
        registerBlockItem(name, toReturn, stackSize);
        return toReturn;
    }

    private static <T extends Block> RegistryObject<T> registerBlock(String name, Supplier<T> block) {
        RegistryObject<T> toReturn = BLOCKS.register(name, block);
        registerBlockItem(name, toReturn);
        return toReturn;
    }

    private static boolean never(BlockState p_50806_, BlockGetter p_50807_, BlockPos p_50808_) {
        return false;
    }

    private static <T extends Block> RegistryObject<Item> registerBlockItem(String name, RegistryObject<T> block, int stackSize) {
        return ModItems.ITEMS.register(name, () -> new BlockItem(block.get(), new Item.Properties().stacksTo(stackSize)));
    }

    private static <T extends Block> RegistryObject<Item> registerBlockItem(String name, RegistryObject<T> block) {
        return ModItems.ITEMS.register(name, () -> new BlockItem(block.get(), new Item.Properties()));
    }
    // 专用于注册没有物品形式的方块
    private static <T extends Block> RegistryObject<T> registerBlockWithoutBlockItem(String name, Supplier<T> block) {
        return BLOCKS.register(name, block);
    }

    public static <T extends Block> RegistryObject<Item> getBlockItem(RegistryObject<T> block) {
        return RegistryObject.create(
            net.minecraftforge.registries.ForgeRegistries.BLOCKS.getKey(block.get()), 
            net.minecraftforge.registries.ForgeRegistries.ITEMS
        );
    }
}
