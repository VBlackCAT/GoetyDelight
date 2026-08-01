package net.v_black_cat.goetydelight.loot;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.neoforged.neoforge.common.loot.IGlobalLootModifier;
import net.neoforged.neoforge.common.loot.LootModifier;
import org.jetbrains.annotations.NotNull;

/**
 * 全局战利品修饰符（1.21.1 移植版）。
 * 与 1.20.1 的 loot/ModLootModifier 保持相同的 JSON 字段与行为：
 * conditions / itemToAdd / chance / min / max / looting_multiplier。
 */
public class ModLootModifier extends LootModifier {
    public static final MapCodec<ModLootModifier> CODEC = RecordCodecBuilder.mapCodec(inst -> inst.group(
            LOOT_CONDITIONS_CODEC.fieldOf("conditions").forGetter(lm -> lm.conditions),
            BuiltInRegistries.ITEM.byNameCodec().fieldOf("itemToAdd").forGetter(lm -> lm.itemToAdd),
            com.mojang.serialization.Codec.FLOAT.fieldOf("chance").forGetter(lm -> lm.chance),
            com.mojang.serialization.Codec.INT.fieldOf("min").forGetter(lm -> lm.minCount),
            com.mojang.serialization.Codec.INT.fieldOf("max").forGetter(lm -> lm.maxCount),
            com.mojang.serialization.Codec.FLOAT.optionalFieldOf("looting_multiplier", 0.0F)
                    .forGetter(lm -> lm.lootingMultiplier)
    ).apply(inst, ModLootModifier::new));

    private final Item itemToAdd;
    private final float chance;
    private final int minCount;
    private final int maxCount;
    private final float lootingMultiplier;

    public ModLootModifier(LootItemCondition[] conditionsIn, Item itemToAdd, float chance,
                           int minCount, int maxCount, float lootingMultiplier) {
        super(conditionsIn);
        this.itemToAdd = itemToAdd;
        this.chance = chance;
        this.minCount = minCount;
        this.maxCount = maxCount;
        this.lootingMultiplier = lootingMultiplier;
    }

    @Override
    protected @NotNull ObjectArrayList<ItemStack> doApply(ObjectArrayList<ItemStack> generatedLoot, LootContext context) {
        float finalChance = this.chance;

        if (this.lootingMultiplier > 0) {
            Entity attacker = context.getParamOrNull(LootContextParams.ATTACKING_ENTITY);
            if (attacker instanceof LivingEntity livingKiller) {
                Holder<Enchantment> looting = context.getLevel().registryAccess()
                        .lookupOrThrow(Registries.ENCHANTMENT).getOrThrow(Enchantments.LOOTING);
                int lootingLevel = EnchantmentHelper.getEnchantmentLevel(looting, livingKiller);
                if (lootingLevel > 0) {
                    finalChance += lootingLevel * this.lootingMultiplier;
                }
            }
        }

        if (context.getRandom().nextFloat() < finalChance) {
            int randomCount = minCount + context.getRandom().nextInt(maxCount - minCount + 1);
            if (randomCount > 0) {
                generatedLoot.add(new ItemStack(itemToAdd, randomCount));
            }
        }
        return generatedLoot;
    }

    @Override
    public MapCodec<? extends IGlobalLootModifier> codec() {
        return CODEC;
    }
}
