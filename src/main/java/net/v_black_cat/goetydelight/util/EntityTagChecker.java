package net.v_black_cat.goetydelight.util;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraftforge.registries.ForgeRegistries;

public class EntityTagChecker {
    // 检查实体是否在指定标签中
    public static boolean isEntityInTag(Entity entity, String tagId) {
        // 创建标签键
        TagKey<EntityType<?>> tagKey = TagKey.create(
                ForgeRegistries.ENTITY_TYPES.getRegistryKey(),
                new ResourceLocation(tagId)
        );

        // 获取实体类型
        EntityType<?> entityType = entity.getType();

        // 检查是否在标签中
        return entityType.is(tagKey);
    }

    // 使用示例
    public static void checkRaider(Entity entity) {
        if (isEntityInTag(entity, "minecraft:raiders")) {
            System.out.println(entity.getName().getString() + " 是袭击者!");
        }
    }
}