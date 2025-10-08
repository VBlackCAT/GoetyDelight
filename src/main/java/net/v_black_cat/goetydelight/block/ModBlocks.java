package net.v_black_cat.goetydelight.block;



import net.minecraft.core.BlockPos;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
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
            new ShadeStoveBlock(BlockBehaviour.Properties.copy(Blocks.BRICKS).lightLevel(litBlockEmission(13))));

    //诅咒金属锅
    public static final RegistryObject<Block> CURSED_INGOT_POT = registerBlock("cursed_ingot_pot",() ->
            new CursedIngotPotBlock(BlockBehaviour.Properties.of()
                    .mapColor(MapColor.METAL)
                    .strength(0.5F, 6.0F)
                    .sound(SoundType.LANTERN)));


    //腐尸蛆虫盛宴方块
    public static final RegistryObject<Block> ROTTEN_CORPSE_MAGGOT_FEAST_BLOCK = registerBlock("rotten_corpse_maggot_feast_block",() ->
            new RottenCorpseMaggotFeastBlock(BlockBehaviour.Properties.copy(Blocks.CAKE),
                    ModItems.ROTTEN_CORPSE_MAGGOT_FEAST, true));


    public static final RegistryObject<Block> RENDER_BLOCK = registerBlock("render_block",() ->
            new RenderBlock(BlockBehaviour.Properties.of()
                    .strength(2.0f)
                    .noOcclusion()
                    .lightLevel(state -> 15)));

    public static final RegistryObject<Block> APOCALYPTIUM_POT = registerBlock("apocalyptium_pot",() -> new Block(BlockBehaviour.Properties.copy(Blocks.IRON_BLOCK).sound(SoundType.AMETHYST)));


    //照抄的农夫乐事设置方块亮度的方法
    private static ToIntFunction<BlockState> litBlockEmission(int lightValue) {
        return (state) -> {
            return (Boolean)state.getValue(BlockStateProperties.LIT) ? lightValue : 0;
        };
    }

    private static <T extends Block> RegistryObject<T> registerBlock(String name, Supplier<T> block) {
        RegistryObject<T> toReturn = BLOCKS.register(name, block);registerBlockItem(name, toReturn);
        return toReturn;
    }

    private static boolean never(BlockState p_50806_, BlockGetter p_50807_, BlockPos p_50808_) {
        return false;
    }

    private static <T extends Block> RegistryObject<Item> registerBlockItem(String name, RegistryObject<T> block) {
        return ModItems.ITEMS.register(name, () -> new BlockItem(block.get(), new Item.Properties()));
    }
}
