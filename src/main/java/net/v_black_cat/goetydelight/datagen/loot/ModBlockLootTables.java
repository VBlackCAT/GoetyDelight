package net.v_black_cat.goetydelight.datagen.loot;

import net.minecraft.world.item.Items;
import net.v_black_cat.goetydelight.block.ModBlocks;
import net.minecraft.data.loot.BlockLootSubProvider;
import net.minecraft.world.flag.FeatureFlags;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.entries.LootItem;
import net.minecraft.world.level.storage.loot.functions.ApplyBonusCount;
import net.minecraft.world.level.storage.loot.functions.SetItemCountFunction;
import net.minecraft.world.level.storage.loot.providers.number.UniformGenerator;
import net.minecraftforge.registries.RegistryObject;
import net.v_black_cat.goetydelight.item.ModItems;

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

        this.dropOther(ModBlocks.ROTTEN_CORPSE_MAGGOT_FEAST_BLOCK.get(), Items.BOWL);
        this.dropOther(ModBlocks.ATTACHED_ECTOPLASMIC_MELON_STEM.get(), ModItems.ECTOPLASMIC_MELON_SEEDS.get());
        this.dropOther(ModBlocks.ECTOPLASMIC_MELON_STEM.get(), ModItems.ECTOPLASMIC_MELON_SEEDS.get());
        this.dropOther(ModBlocks.ROTTEN_CORPSE_MAGGOT_FEAST_BLOCK.get(), Items.BOWL);
        this.dropOther(ModBlocks.VOID_GEL_JELLY_BLOCK.get(), Items.BOWL);
        this.add(ModBlocks.MARBLE_SLAB.get(),
                block -> createSlabItemTable(ModBlocks.MARBLE_SLAB.get()));
        this.add(ModBlocks.MARBLE_DOOR.get(),
                block -> createDoorTable(ModBlocks.MARBLE_DOOR.get()));
    }

    protected LootTable.Builder createCopperLikeOreDrops(Block pBlock, Item item) {
        return createSilkTouchDispatchTable(pBlock,
                this.applyExplosionDecay(pBlock,
                        LootItem.lootTableItem(item)
                                .apply(SetItemCountFunction.setCount(UniformGenerator.between(2.0F, 5.0F)))
                                .apply(ApplyBonusCount.addOreBonusCount(Enchantments.BLOCK_FORTUNE))));
    }

    @Override
    protected Iterable<Block> getKnownBlocks() {
        return ModBlocks.BLOCKS.getEntries().stream().map(RegistryObject::get)::iterator;
    }
}
