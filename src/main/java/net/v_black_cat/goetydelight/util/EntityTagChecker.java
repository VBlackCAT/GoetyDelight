package net.v_black_cat.goetydelight.util;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;

public class EntityTagChecker {
    public static boolean isEntityInTag(Entity entity, String tagId) {
        TagKey<EntityType<?>> tagKey = TagKey.create(
                BuiltInRegistries.ENTITY_TYPE.key(),
                ResourceLocation.parse(tagId)  // 使用 parse
        );
        return entity.getType().is(tagKey);
    }
}