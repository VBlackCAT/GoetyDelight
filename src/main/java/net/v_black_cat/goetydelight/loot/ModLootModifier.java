package net.v_black_cat.goetydelight.loot;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.minecraftforge.common.loot.IGlobalLootModifier;
import net.minecraftforge.common.loot.LootModifier;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import org.jetbrains.annotations.NotNull;

import static net.v_black_cat.goetydelight.GoetyDelight.MODID;


public class ModLootModifier extends LootModifier {

    private final Item itemToAdd;
    private final float chance;
    
    private final int minCount;
    private final int maxCount;
    private final float lootingMultiplier; 

    public static final DeferredRegister<Codec<? extends IGlobalLootModifier>> GLOBAL_LOOT_MODIFIER_CODECS =
            DeferredRegister.create(ForgeRegistries.Keys.GLOBAL_LOOT_MODIFIER_SERIALIZERS, MODID);

    
    public static final RegistryObject<Codec<ModLootModifier>> LOOT_MODIFIER_CODEC = GLOBAL_LOOT_MODIFIER_CODECS.register("goetydelight_loot_modifier", () ->
            RecordCodecBuilder.create(inst -> LootModifier.codecStart(inst).and(
                    inst.group(
                            ForgeRegistries.ITEMS.getCodec().fieldOf("itemToAdd").forGetter(m -> m.itemToAdd),
                            Codec.FLOAT.fieldOf("chance").forGetter(m -> m.chance),
                            Codec.INT.fieldOf("min").forGetter(m -> m.minCount),
                            Codec.INT.fieldOf("max").forGetter(m -> m.maxCount),
                            Codec.FLOAT.optionalFieldOf("looting_multiplier", 0.0F).forGetter(m -> m.lootingMultiplier)
                    )).apply(inst, ModLootModifier::new)
            ));

    
    public ModLootModifier(LootItemCondition[] conditionsIn, Item itemToAdd, float chance, int minCount, int maxCount, float lootingMultiplier) {
        super(conditionsIn);
        this.itemToAdd = itemToAdd;
        this.chance = chance;
        this.minCount = minCount;
        this.maxCount = maxCount;
        this.lootingMultiplier = lootingMultiplier;
    }

    @Override
    public Codec<? extends IGlobalLootModifier> codec() {
        return LOOT_MODIFIER_CODEC.get();
    }

    @Override
    protected @NotNull ObjectArrayList<ItemStack> doApply(ObjectArrayList<ItemStack> generatedLoot, LootContext context) {
        
        float finalChance = this.chance;

        
        if (this.lootingMultiplier > 0) {
            int lootingLevel = context.getLootingModifier(); 
            if (lootingLevel > 0) {
                
                finalChance += lootingLevel * this.lootingMultiplier;
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
}