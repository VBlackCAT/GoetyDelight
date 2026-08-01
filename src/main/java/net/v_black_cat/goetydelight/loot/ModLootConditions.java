package net.v_black_cat.goetydelight.loot;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.registries.Registries;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.minecraft.world.level.storage.loot.predicates.LootItemConditionType;

/**
 * 1.21.1 移植版：实体标签战利品条件（对应 1.20.1 ModLootConditions.EntityTagCondition）。
 */
public class ModLootConditions {

    public static class EntityTagCondition implements LootItemCondition {
        public static final MapCodec<EntityTagCondition> CODEC = RecordCodecBuilder.mapCodec(inst -> inst.group(
                TagKey.codec(Registries.ENTITY_TYPE).fieldOf("tag").forGetter(c -> c.entityTag)
        ).apply(inst, EntityTagCondition::new));

        private final TagKey<EntityType<?>> entityTag;

        public EntityTagCondition(TagKey<EntityType<?>> entityTag) {
            this.entityTag = entityTag;
        }

        @Override
        public LootItemConditionType getType() {
            return RegHelper.ENTITY_TAG_CONDITION.get();
        }

        @Override
        public boolean test(LootContext context) {
            Entity entity = context.getParamOrNull(LootContextParams.THIS_ENTITY);
            return entity != null && entity.getType().is(this.entityTag);
        }
    }
}
