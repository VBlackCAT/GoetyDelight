package net.v_black_cat.goetydelight.loot;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.Serializer;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.minecraft.world.level.storage.loot.predicates.LootItemConditionType;
import net.v_black_cat.goetydelight.loot.RegHelper;

public class ModLootConditions {

    // 实体标签条件实现
    public static class EntityTagCondition implements LootItemCondition {
        private final TagKey<EntityType<?>> entityTag;

        public EntityTagCondition(TagKey<EntityType<?>> entityTag) {
            this.entityTag = entityTag;
        }

        @Override
        public boolean test(LootContext context) {
            Entity entity = context.getParamOrNull(LootContextParams.THIS_ENTITY);
            return entity != null && entity.getType().is(this.entityTag);
        }

        @Override
        public LootItemConditionType getType() {
            return RegHelper.ENTITY_TAG_CONDITION.get();
        }

        // 序列化器
        public static class Serializer implements net.minecraft.world.level.storage.loot.Serializer<EntityTagCondition> {
            @Override
            public void serialize(JsonObject json, EntityTagCondition condition, JsonSerializationContext context) {
                json.addProperty("tag", condition.entityTag.location().toString());
            }

            @Override
            public EntityTagCondition deserialize(JsonObject json, JsonDeserializationContext context) {
                String tagName = GsonHelper.getAsString(json, "tag");
                ResourceLocation tagId = new ResourceLocation(tagName);
                TagKey<EntityType<?>> tag = TagKey.create(Registries.ENTITY_TYPE, tagId);
                return new EntityTagCondition(tag);
            }
        }
    }
}