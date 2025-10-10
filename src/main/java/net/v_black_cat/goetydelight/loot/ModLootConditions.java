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
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.minecraft.world.level.storage.loot.predicates.LootItemConditionType;

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

    // 特殊装备击杀条件实现
/*
    {
        "conditions": [
        {
            "condition": "goetydelight:killed_with_equipment",
                "equipment_tag": "forge:swords",
                "slot": "mainhand"
        }
        ],
        "itemToAdd": "minecraft:diamond",
            "chance": 0.5,
            "min": 1,
            "max": 3,
            "looting_multiplier": 0.1
    }
    */
    public static class KilledWithEquipmentCondition implements LootItemCondition {
        private final TagKey<Item> requiredEquipment;
        private final EquipmentSlot slot;

        public KilledWithEquipmentCondition(TagKey<Item> requiredEquipment, EquipmentSlot slot) {
            this.requiredEquipment = requiredEquipment;
            this.slot = slot;
        }

        @Override
        public boolean test(LootContext context) {
            // 获取造成伤害的实体
            Entity killer = context.getParamOrNull(LootContextParams.KILLER_ENTITY);
            //间接伤害获取实际攻击者
            Entity directKiller = context.getParamOrNull(LootContextParams.DIRECT_KILLER_ENTITY);

            // 检查直接攻击者
            if (directKiller instanceof LivingEntity) {
                LivingEntity livingKiller = (LivingEntity) directKiller;
                if (checkEquipment(livingKiller)) {
                    return true;
                }
            }

            // 检查最终攻击者
            if (killer instanceof LivingEntity) {
                LivingEntity livingKiller = (LivingEntity) killer;
                return checkEquipment(livingKiller);
            }

            return false;
        }

        private boolean checkEquipment(LivingEntity entity) {
            ItemStack equipment;
            switch (slot) {
                case MAINHAND:
                    equipment = entity.getMainHandItem();
                    break;
                case OFFHAND:
                    equipment = entity.getOffhandItem();
                    break;
                case HEAD:
                    equipment = entity.getItemBySlot(EquipmentSlot.HEAD);
                    break;
                case CHEST:
                    equipment = entity.getItemBySlot(EquipmentSlot.CHEST);
                    break;
                case LEGS:
                    equipment = entity.getItemBySlot(EquipmentSlot.LEGS);
                    break;
                case FEET:
                    equipment = entity.getItemBySlot(EquipmentSlot.FEET);
                    break;
                default:
                    return false;
            }

            return equipment.is(requiredEquipment);
        }

        @Override
        public LootItemConditionType getType() {
            return RegHelper.KILLED_WITH_EQUIPMENT_CONDITION.get();
        }

        public static class Serializer implements net.minecraft.world.level.storage.loot.Serializer<KilledWithEquipmentCondition> {
            @Override
            public void serialize(JsonObject json, KilledWithEquipmentCondition condition, JsonSerializationContext context) {
                json.addProperty("equipment_tag", condition.requiredEquipment.location().toString());
                json.addProperty("slot", condition.slot.getName());
            }

            @Override
            public KilledWithEquipmentCondition deserialize(JsonObject json, JsonDeserializationContext context) {
                String tagName = GsonHelper.getAsString(json, "equipment_tag");
                String slotName = GsonHelper.getAsString(json, "slot");

                ResourceLocation tagId = new ResourceLocation(tagName);
                TagKey<Item> tag = TagKey.create(Registries.ITEM, tagId);
                EquipmentSlot slot = EquipmentSlot.byName(slotName);

                return new KilledWithEquipmentCondition(tag, slot);
            }
        }
    }
}