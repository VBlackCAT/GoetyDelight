package net.v_black_cat.goetydelight.entities.ai.customer.preference;

import com.mojang.serialization.Codec;
import com.mojang.serialization.Dynamic;
import com.mojang.serialization.JsonOps;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.advancements.critereon.ItemPredicate;
import net.minecraft.world.item.ItemStack;

import static net.v_black_cat.goetydelight.GoetyDelight.LOGGER;

public record ItemWeight(ItemPredicate predicate, float weight) {
    public static final Codec<ItemPredicate> ITEM_PREDICATE_CODEC = Codec.PASSTHROUGH
            .xmap(
                    dynamic -> {
                        try {
                            return ItemPredicate.fromJson(dynamic.convert(JsonOps.INSTANCE).getValue());
                        } catch (Exception e) {
                            LOGGER.warn(
                                    "Failed to parse ItemPredicate in Preference Data (it might refer to a missing mod item): {}. Root cause: {}",
                                    dynamic.getValue(),
                                    e.getMessage()
                            );
                            return PreferenceManager.NONE;
                        }
                    },
                    predicate -> new Dynamic<>(JsonOps.INSTANCE, predicate.serializeToJson())
            );

    public static final Codec<ItemWeight> CODEC = RecordCodecBuilder.create(instance ->
            instance.group(
                    ITEM_PREDICATE_CODEC.fieldOf("predicate").forGetter(ItemWeight::predicate),
                    Codec.FLOAT.fieldOf("weight").forGetter(ItemWeight::weight)
            ).apply(instance, ItemWeight::new)
    );

    public boolean matches(ItemStack stack) {
        return this.predicate.matches(stack);
    }
}