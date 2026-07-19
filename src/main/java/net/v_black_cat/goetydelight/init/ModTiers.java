package net.v_black_cat.goetydelight.init;

import net.minecraft.tags.BlockTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Tier;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Block;

public class ModTiers {

    public static final Tier SPECIAL = new Tier() {
        public int getUses() { return 256; }
        public float getSpeed() { return 6.0f; }
        public float getAttackDamageBonus() { return 2.0f; }
        public int getLevel() { return 3; }
        public int getEnchantmentValue() { return 15; }
        public Ingredient getRepairIngredient() { return Ingredient.of(Items.NETHERITE_INGOT); }
        public TagKey<Block> getIncorrectBlocksForDrops() { return BlockTags.INCORRECT_FOR_DIAMOND_TOOL; }
    };

    public static final Tier DARK = new Tier() {
        public int getUses() { return 512; }
        public float getSpeed() { return 7.0f; }
        public float getAttackDamageBonus() { return 3.0f; }
        public int getLevel() { return 3; }
        public int getEnchantmentValue() { return 20; }
        public Ingredient getRepairIngredient() { return Ingredient.of(Items.NETHERITE_INGOT); }
        public TagKey<Block> getIncorrectBlocksForDrops() { return BlockTags.INCORRECT_FOR_DIAMOND_TOOL; }
    };

    public static final Tier VOID = new Tier() {
        public int getUses() { return 2031; }
        public float getSpeed() { return 8.0f; }
        public float getAttackDamageBonus() { return 4.0f; }
        public int getLevel() { return 4; }
        public int getEnchantmentValue() { return 25; }
        public Ingredient getRepairIngredient() { return Ingredient.of(Items.NETHERITE_INGOT); }
        public TagKey<Block> getIncorrectBlocksForDrops() { return BlockTags.INCORRECT_FOR_NETHERITE_TOOL; }
    };
}