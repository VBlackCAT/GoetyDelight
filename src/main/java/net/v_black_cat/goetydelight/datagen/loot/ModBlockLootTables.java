package net.v_black_cat.goetydelight.datagen.loot;

import net.minecraft.advancements.critereon.EnchantmentPredicate;
import net.minecraft.advancements.critereon.MinMaxBounds;
import net.minecraft.advancements.critereon.StatePropertiesPredicate;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.storage.loot.IntRange;
import net.minecraft.world.level.storage.loot.LootPool;
import net.minecraft.world.level.storage.loot.entries.AlternativesEntry;
import net.minecraft.world.level.storage.loot.functions.ApplyExplosionDecay;
import net.minecraft.world.level.storage.loot.functions.LimitCount;
import net.minecraft.world.level.storage.loot.predicates.LootItemBlockStatePropertyCondition;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.minecraft.world.level.storage.loot.predicates.LootItemRandomChanceCondition;
import net.minecraft.world.level.storage.loot.predicates.MatchTool;
import net.minecraft.world.level.storage.loot.providers.number.ConstantValue;
import net.v_black_cat.goetydelight.GoetyDelight;
import net.v_black_cat.goetydelight.block.MetamorphicScentGrassBlock;
import net.v_black_cat.goetydelight.block.ModBlocks;
import net.v_black_cat.goetydelight.block.RoastLaowangBlock;
import net.minecraft.data.loot.BlockLootSubProvider;
import net.minecraft.world.flag.FeatureFlags;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.properties.BedPart;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.entries.LootItem;
import net.minecraft.world.level.storage.loot.functions.ApplyBonusCount;
import net.minecraft.world.level.storage.loot.functions.SetItemCountFunction;
import net.minecraft.world.level.storage.loot.predicates.InvertedLootItemCondition;
import net.minecraft.world.level.storage.loot.providers.number.UniformGenerator;
import net.minecraftforge.registries.RegistryObject;
import net.v_black_cat.goetydelight.item.ModItems;
import vectorwing.farmersdelight.common.block.FeastBlock;
import vectorwing.farmersdelight.common.item.CookingPotItem;

import java.util.Set;

public class ModBlockLootTables extends BlockLootSubProvider {
    public ModBlockLootTables() {
        super(Set.of(), FeatureFlags.REGISTRY.allFlags());
    }

    @Override
    protected void generate() {
//        this.dropSelf(ModBlocks.MARBLE_BLOCK.get());
//        this.dropSelf(ModBlocks.RAW_MARBLE_BLOCK.get());
//        this.dropSelf(ModBlocks.SOUND_BLOCK.get());

//        this.add(ModBlocks.MARBLE_ORE.get(),
//                block -> createCopperLikeOreDrops(ModBlocks.MARBLE_ORE.get(), ModItems.RAW_MARBLE.get()));
//        this.add(ModBlocks.DEEPSLATE_MARBLE_ORE.get(),
//                block -> createCopperLikeOreDrops(ModBlocks.DEEPSLATE_MARBLE_ORE.get(), ModItems.RAW_MARBLE.get()));
//        this.add(ModBlocks.NETHER_MARBLE_ORE.get(),
//                block -> createCopperLikeOreDrops(ModBlocks.NETHER_MARBLE_ORE.get(), ModItems.RAW_MARBLE.get()));
//        this.add(ModBlocks.END_STONE_MARBLE_ORE.get(),
//                block -> createCopperLikeOreDrops(ModBlocks.END_STONE_MARBLE_ORE.get(), ModItems.RAW_MARBLE.get()));

        this.dropSelf(ModBlocks.MARBLE.get());
        this.dropSelf(ModBlocks.SILT_MARBLE_HEAVY.get());
        this.dropSelf(ModBlocks.BLUE_MARBLE.get());
        this.dropSelf(ModBlocks.JUNGLE_MARBLE.get());
        this.dropSelf(ModBlocks.NETHER_MARBLE.get());
        this.dropSelf(ModBlocks.DRIPMARBLE_BLOCK.get());
        this.dropSelf(ModBlocks.POINTED_DRIPMARBLE.get());
        this.dropSelf(ModBlocks.MARBLE_STAIRS.get());
        this.dropSelf(ModBlocks.MARBLE_BUTTON.get());
        this.dropSelf(ModBlocks.MARBLE_PRESSURE_PLATE.get());
        this.dropSelf(ModBlocks.MARBLE_TRAPDOOR.get());
        this.dropSelf(ModBlocks.MARBLE_FENCE.get());
        this.dropSelf(ModBlocks.MARBLE_FENCE_GATE.get());
        this.dropSelf(ModBlocks.MARBLE_WALL.get());
        this.dropSelf(ModBlocks.NIGHT_STOVE.get());
        this.dropSelf(ModBlocks.CURSED_INGOT_POT.get());
        this.dropSelf(ModBlocks.NIGHT_STOVE.get());
        this.dropSelf(ModBlocks.SHADE_STOVE.get());
        this.add(ModBlocks.ECTOPLASMIC_MELON_BLOCK.get(),
                createFruitBlockDrops(ModBlocks.ECTOPLASMIC_MELON_BLOCK.get(),
                        ModItems.ECTOPLASMIC_MELON.get(), 3.0F, 7.0F, 9));

        this.dropOther(ModBlocks.ATTACHED_ECTOPLASMIC_MELON_STEM.get(), ModItems.ECTOPLASMIC_MELON_SEEDS.get());
        this.dropOther(ModBlocks.ECTOPLASMIC_MELON_STEM.get(), ModItems.ECTOPLASMIC_MELON_SEEDS.get());

        // 方块食物掉落：与农夫乐事一致——满盘时掉方块本体，取过餐后只掉容器/残留物。
        this.add(ModBlocks.ROTTEN_CORPSE_MAGGOT_FEAST_BLOCK.get(),
                createFeastBlockDrops(ModBlocks.ROTTEN_CORPSE_MAGGOT_FEAST_BLOCK.get(),
                        FeastBlock.SERVINGS, 3, Items.BOWL));
        this.add(ModBlocks.VOID_GEL_JELLY_BLOCK.get(),
                createFeastBlockDrops(ModBlocks.VOID_GEL_JELLY_BLOCK.get(),
                        FeastBlock.SERVINGS, 3, Items.BOWL));
        this.add(ModBlocks.STUFFED_TALL_SKULL_RICE_BLOCK.get(),
                createFeastBlockDrops(ModBlocks.STUFFED_TALL_SKULL_RICE_BLOCK.get(),
                        FeastBlock.SERVINGS, 4, com.Polarice3.Goety.common.blocks.ModBlocks.TALL_SKULL_ITEM.get()));
        this.add(ModBlocks.LICHS_CHAOS_STEW_BLOCK.get(),
                createFeastBlockDrops(ModBlocks.LICHS_CHAOS_STEW_BLOCK.get(),
                        net.v_black_cat.goetydelight.block.LichsChaosStewBlock.SERVINGS, 5,
                        ModBlocks.CURSED_INGOT_POT.get()));
        this.add(ModBlocks.NIGHT_HEART_PEA_SOUP_BLOCK.get(),
                createFeastBlockDrops(ModBlocks.NIGHT_HEART_PEA_SOUP_BLOCK.get(),
                        net.v_black_cat.goetydelight.block.NightHeartPeaSoupBlock.SERVINGS, 3,
                        ModBlocks.CURSED_INGOT_POT.get()));
        this.add(ModBlocks.BONE_LORD_ASH_RICE_BLOCK.get(),
                createFeastBlockDrops(ModBlocks.BONE_LORD_ASH_RICE_BLOCK.get(),
                        net.v_black_cat.goetydelight.block.BoneLordAshRiceBlock.SERVINGS, 3,
                        ModBlocks.CURSED_INGOT_POT.get()));
        this.add(ModBlocks.SNAP_UNHOLY_TRIPE_BLOCK.get(),
                createFeastBlockDrops(ModBlocks.SNAP_UNHOLY_TRIPE_BLOCK.get(),
                        net.v_black_cat.goetydelight.block.SnapUnholyTripeBlock.SERVINGS, 3,
                        vectorwing.farmersdelight.common.registry.ModItems.COOKING_POT.get()));
        this.add(ModBlocks.MENEMEN_BLOCK.get(),
                createFeastBlockDrops(ModBlocks.MENEMEN_BLOCK.get(),
                        net.v_black_cat.goetydelight.block.MenemenBlock.SERVINGS, 5,
                        vectorwing.farmersdelight.common.registry.ModItems.COOKING_POT.get()));
        this.add(ModBlocks.ROAST_LAOWANG_BLOCK.get(),
                createRoastLaowangDrops(ModBlocks.ROAST_LAOWANG_BLOCK.get(),
                        RoastLaowangBlock.SERVINGS, 13, Items.BOWL));
        this.add(ModBlocks.MARBLE_SLAB.get(),
                block -> createSlabItemTable(ModBlocks.MARBLE_SLAB.get()));
        this.add(ModBlocks.MARBLE_DOOR.get(),
                block -> createDoorTable(ModBlocks.MARBLE_DOOR.get()));
    }

    /**
     * 农夫乐事式方块食物掉落：
     * <ul>
     *     <li>份数 == 满盘：掉落方块本体；</li>
     *     <li>份数 != 满盘（已经被取过餐）：掉落容器/残留物。</li>
     * </ul>
     */
    private LootTable.Builder createRoastLaowangDrops(Block block, IntegerProperty servings, int maxServings,
                                                       net.minecraft.world.level.ItemLike container) {
        // 烤老王是 HEAD/FOOT 两格结构。只让 HEAD 产生掉落，
        // 这样无论玩家拆哪一格、爆炸还是其他方式同时处理两格，都不会出现双倍战利品。
        LootItemCondition.Builder head = LootItemBlockStatePropertyCondition
                .hasBlockStateProperties(block)
                .setProperties(StatePropertiesPredicate.Builder.properties()
                        .hasProperty(RoastLaowangBlock.PART, BedPart.HEAD));
        LootItemCondition.Builder full = LootItemBlockStatePropertyCondition
                .hasBlockStateProperties(block)
                .setProperties(StatePropertiesPredicate.Builder.properties()
                        .hasProperty(RoastLaowangBlock.PART, BedPart.HEAD)
                        .hasProperty(servings, maxServings));

        return LootTable.lootTable()
                .withPool(LootPool.lootPool()
                        .setRolls(ConstantValue.exactly(1.0F))
                        .add(LootItem.lootTableItem(block).when(full)))
                .withPool(LootPool.lootPool()
                        .setRolls(ConstantValue.exactly(1.0F))
                        .add(LootItem.lootTableItem(container)
                                .when(head)
                                .when(InvertedLootItemCondition.invert(full))));
    }

    private LootTable.Builder createFeastBlockDrops(Block block, IntegerProperty servings, int maxServings,
                                                    net.minecraft.world.level.ItemLike container) {
        LootItemCondition.Builder full = LootItemBlockStatePropertyCondition
                .hasBlockStateProperties(block)
                .setProperties(StatePropertiesPredicate.Builder.properties()
                        .hasProperty(servings, maxServings));

        return LootTable.lootTable()
                .withPool(LootPool.lootPool()
                        .setRolls(ConstantValue.exactly(1.0F))
                        .add(LootItem.lootTableItem(block).when(full)))
                .withPool(LootPool.lootPool()
                        .setRolls(ConstantValue.exactly(1.0F))
                        .add(LootItem.lootTableItem(container).when(InvertedLootItemCondition.invert(full))));
    }

    protected LootTable.Builder createCopperLikeOreDrops(Block pBlock, Item item) {
        return createSilkTouchDispatchTable(pBlock,
                this.applyExplosionDecay(pBlock,
                        LootItem.lootTableItem(item)
                                .apply(SetItemCountFunction.setCount(UniformGenerator.between(2.0F, 5.0F)))
                                .apply(ApplyBonusCount.addOreBonusCount(Enchantments.BLOCK_FORTUNE))));
    }



    protected LootTable.Builder createMetamorphicScentGrassDrops(Block block) {

        LootItemCondition.Builder matureCondition = LootItemBlockStatePropertyCondition
                .hasBlockStateProperties(block)
                .setProperties(StatePropertiesPredicate.Builder.properties()
                        .hasProperty(MetamorphicScentGrassBlock.AGE, 3));


        return this.createCropDrops(
                block,
                ModItems.METAMORPHIC_SCENT_GRASS.get(),
                ModItems.METAMORPHIC_SCENT_GRASS_SEEDS.get(),
                matureCondition
                ).withPool(LootPool.lootPool()
                        .when(matureCondition)
                        .add(LootItem.lootTableItem(ModItems.METAMORPHIC_SCENT_FRUIT.get())
                                .when(LootItemRandomChanceCondition.randomChance(0.025f))
                                .apply(ApplyBonusCount.addUniformBonusCount(Enchantments.BLOCK_FORTUNE))));
    }




    protected LootTable.Builder createFruitBlockDrops(Block block, Item fruitItem, float minDrops, float maxDrops, int maxWithFortune) {
        return LootTable.lootTable()
                .withPool(LootPool.lootPool()
                        .setRolls(ConstantValue.exactly(1.0F))
                        .add(LootItem.lootTableItem(block)
                                .when(MatchTool.toolMatches(
                                        net.minecraft.advancements.critereon.ItemPredicate.Builder.item()
                                                .hasEnchantment(new EnchantmentPredicate(
                                                        Enchantments.SILK_TOUCH,
                                                        MinMaxBounds.Ints.atLeast(1)
                                                ))
                                ))
                                .otherwise(
                                        LootItem.lootTableItem(fruitItem)
                                                .apply(SetItemCountFunction.setCount(
                                                        UniformGenerator.between(minDrops, maxDrops)
                                                ))
                                                .apply(ApplyBonusCount.addUniformBonusCount(
                                                        Enchantments.BLOCK_FORTUNE
                                                ))
                                                .apply(LimitCount.limitCount(
                                                        IntRange.upperBound(maxWithFortune)
                                                ))
                                                .apply(ApplyExplosionDecay.explosionDecay())
                                )
                        )
                );
    }
    @Override
    protected Iterable<Block> getKnownBlocks() {
        return () -> java.util.stream.Stream.concat(
                ModBlocks.BLOCKS.getEntries().stream().map(RegistryObject::get),
                net.v_black_cat.goetydelight.event.ModRegisterEvent.DOLL_BLOCKS.values().stream()
        ).iterator();
    }
}
