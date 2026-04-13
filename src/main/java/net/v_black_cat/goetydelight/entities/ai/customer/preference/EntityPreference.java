package net.v_black_cat.goetydelight.entities.ai.customer.preference;

import com.mojang.serialization.Codec;
import com.mojang.serialization.Dynamic;
import com.mojang.serialization.JsonOps;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.advancements.critereon.EntityPredicate;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;

import java.util.List;

public record EntityPreference(EntityPredicate entityPredicate, List<ItemWeight> preferences) {
    public static final Codec<EntityPredicate> ENTITY_PREDICATE_CODEC = Codec.PASSTHROUGH
            .xmap(
                    dynamic -> {
                        return EntityPredicate.fromJson(dynamic.convert(JsonOps.INSTANCE).getValue());
                    },
                    predicate -> new Dynamic<>(JsonOps.INSTANCE, predicate.serializeToJson())
            );

    public static final Codec<EntityPreference> CODEC = RecordCodecBuilder.create(instance ->
            instance.group(
                    ENTITY_PREDICATE_CODEC.fieldOf("entity").forGetter(EntityPreference::entityPredicate),
                    ItemWeight.CODEC.listOf().fieldOf("preferences").forGetter(EntityPreference::preferences)
            ).apply(instance, EntityPreference::new)
    );

    public boolean matches(Entity entity) {
        if (entity.level() instanceof ServerLevel serverLevel) {
            return this.entityPredicate.matches(serverLevel, entity.position(), entity);
        }
        return false;
    }
}