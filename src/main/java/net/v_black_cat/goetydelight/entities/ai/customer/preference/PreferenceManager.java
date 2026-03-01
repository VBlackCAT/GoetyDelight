package net.v_black_cat.goetydelight.entities.ai.customer.preference;
import net.minecraft.advancements.critereon.EnchantmentPredicate;
import net.minecraft.advancements.critereon.ItemPredicate;
import net.minecraft.advancements.critereon.MinMaxBounds;
import net.minecraft.advancements.critereon.NbtPredicate;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraftforge.registries.ForgeRegistries;
import net.v_black_cat.goetydelight.register.ModRegistries;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

public class PreferenceManager {
    public static final ItemPredicate NONE = new ItemPredicate(
            null,
            Collections.emptySet(),
            MinMaxBounds.Ints.ANY,
            MinMaxBounds.Ints.ANY,
            EnchantmentPredicate.NONE,
            EnchantmentPredicate.NONE,
            null,
            NbtPredicate.ANY
    );

    public static float getWeight(LivingEntity entity, ItemStack stack) {

        if (stack.isEmpty()) return 0.0f;

        return entity.level().registryAccess().registry(ModRegistries.PREFERENCE_KEY)
                .map(registry -> {
                    for (EntityPreference preference : registry) {
                        if (preference.matches(entity)) {
                            for (ItemWeight iw : preference.preferences()) {
                                if (iw.matches(stack)) {
                                    return iw.weight();
                                }
                            }
                        }
                    }
                    return 0.0f;
                }).orElse(0.0f);
    }
}