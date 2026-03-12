package net.v_black_cat.goetydelight.util;

import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.world.entity.LivingEntity;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@SuppressWarnings("unused")
public final class EntityUtil {

    // ═══════════════════════════════════════════════════════════════
    //                     EntityData Accessor Cache System
    // ═══════════════════════════════════════════════════════════════

    /**
     * Cache for EntityDataAccessor lookups by entity class
     */
    private static final Map<String, List<EntityDataAccessor<?>>> ACCESSOR_CACHE =
            new java.util.concurrent.ConcurrentHashMap<>();

    /**
     * Cache for numeric EntityDataAccessors by entity class
     */
    private static final Map<String, List<EntityDataAccessor<?>>> NUMERIC_ACCESSOR_CACHE =
            new java.util.concurrent.ConcurrentHashMap<>();

    /**
     * Get all EntityDataAccessors for an entity class with caching
     */
    private static List<EntityDataAccessor<?>> getCachedAccessors(Class<?> entityClass) {
        String className = entityClass.getName();
        return ACCESSOR_CACHE.computeIfAbsent(className, key -> {
            List<EntityDataAccessor<?>> accessors = new ArrayList<>();
            for (Class<?> clazz = entityClass; clazz != null && clazz != Object.class; clazz = clazz.getSuperclass()) {
                for (Field field : clazz.getDeclaredFields()) {
                    try {
                        if (!EntityDataAccessor.class.isAssignableFrom(field.getType())) continue;
                        if (!java.lang.reflect.Modifier.isStatic(field.getModifiers())) continue;
                        field.setAccessible(true);
                        EntityDataAccessor<?> accessor = (EntityDataAccessor<?>) field.get(null);
                        if (accessor != null) {
                            accessors.add(accessor);
                        }
                    } catch (Exception ignored) {
                    }
                }
            }
            return accessors;
        });
    }

    /**
     * Get all numeric EntityDataAccessors for an entity class with caching
     */
    private static List<EntityDataAccessor<?>> getCachedNumericAccessors(LivingEntity entity) {
        String className = entity.getClass().getName();
        return NUMERIC_ACCESSOR_CACHE.computeIfAbsent(className, key -> {
            List<EntityDataAccessor<?>> numericAccessors = new ArrayList<>();
            List<EntityDataAccessor<?>> allAccessors = getCachedAccessors(entity.getClass());
            for (EntityDataAccessor<?> accessor : allAccessors) {
                try {
                    Object value = entity.getEntityData().get(accessor);
                    if (value instanceof Number) {
                        numericAccessors.add(accessor);
                    }
                } catch (Exception ignored) {
                }
            }
            return numericAccessors;
        });
    }

    // ═══════════════════════════════════════════════════════════════
    //                        实体相关操作
    // ═══════════════════════════════════════════════════════════════

    /**
     * 获取实体生命值方法 - 使用直接字段访问
     */
    public static float DsGetHealth(LivingEntity entity) {
        if (entity == null) return 0;
        try {
            // 直接访问字段，避免反射开销
            SynchedEntityData entityData = entity.getEntityData();
            Map<Integer, SynchedEntityData.DataItem<?>> map = entityData.itemsById;
            SynchedEntityData.DataItem<?> healthItem = map.get(LivingEntity.DATA_HEALTH_ID.getId());
            if (healthItem != null) {
                Object value = healthItem.value;
                if (value instanceof Float) {
                    return (Float) value;
                }
            }
            return entity.getHealth();
        } catch (Exception e) {
            return entity.getHealth();
        }
    }
}
