package net.v_black_cat.goetydelight.util;

import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.world.entity.LivingEntity;

import java.lang.reflect.Field;
import java.util.*;

/*在使用 AccessTransformer 配置实现直接字段访问
 */
@SuppressWarnings("unused")
public final class EntityUtil {


    // ═══════════════════════════════════════════════════════════════
    //                     EntityData Accessor Cache System
    // ═══════════════════════════════════════════════════════════════

    /**
     * Cache for numeric EntityDataAccessors by entity class
     */
    private static final Map<String, List<EntityDataAccessor<?>>> NUMERIC_ACCESSOR_CACHE =
            new java.util.concurrent.ConcurrentHashMap<>();

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
     * Cache for EntityDataAccessor lookups by entity class
     */
    private static final Map<String, List<EntityDataAccessor<?>>> ACCESSOR_CACHE =
            new java.util.concurrent.ConcurrentHashMap<>();

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

    /**
     * 设置实体生命值方法 - 使用直接字段访问和缓存
     */
    public static void DsSetHealth(LivingEntity entity, float expectedHealth) {
        if (entity == null) return;
        try {
            // 首先直接设置原版血量
            setVanillaHealthDirectly(entity, expectedHealth);

            // 使用缓存的数字访问器进行附近值设置
            List<EntityDataAccessor<?>> nearbyAccessors = findNearbyNumericAccessors(entity);
            for (EntityDataAccessor<?> acc : nearbyAccessors) {
                setAccessorValue(entity, acc, expectedHealth);
            }

            // 验证设置是否成功
            float currentHealth = DsGetHealth(entity);
            if (Math.abs(currentHealth - expectedHealth) > 0.1f) {
                setAllNumericAccessors(entity, expectedHealth);
            }
        } catch (Exception e) {
            entity.setHealth(expectedHealth);
        }
    }

    /**
     * 直接修改原版底层生命值数据 - 使用AT字段访问
     */
    @SuppressWarnings("unchecked")
    private static void setVanillaHealthDirectly(LivingEntity entity, float expectedHealth) {
        try {
            SynchedEntityData entityData = entity.getEntityData();
            Map<Integer, SynchedEntityData.DataItem<?>> map = entityData.itemsById;

            SynchedEntityData.DataItem<?> healthItem = map.get(LivingEntity.DATA_HEALTH_ID.getId());

            if (healthItem != null) {
                // 直接修改字段，避免反射调用
                @SuppressWarnings("unchecked")
                SynchedEntityData.DataItem<Float> floatHealthItem = (SynchedEntityData.DataItem<Float>) healthItem;
                floatHealthItem.value = expectedHealth;
                floatHealthItem.dirty = true;
                entityData.isDirty = true;
            }
        } catch (Exception e) {
            // 静默处理
        }
    }

    /**
     * 寻找所有数字类型的ACCESSOR，它们的数值靠近实体的生命值 - 使用缓存优化
     */
    private static List<EntityDataAccessor<?>> findNearbyNumericAccessors(LivingEntity entity) {
        List<EntityDataAccessor<?>> result = new ArrayList<>();
        float entityHealth = entity.getHealth();

        // 使用缓存的数字访问器，避免重复反射
        List<EntityDataAccessor<?>> numericAccessors = getCachedNumericAccessors(entity);
        for (EntityDataAccessor<?> accessor : numericAccessors) {
            try {
                Object value = entity.getEntityData().get(accessor);
                if (value instanceof Number) {
                    float numericValue = ((Number) value).floatValue();
                    if (Math.abs(numericValue - entityHealth) < 2.0f) {
                        result.add(accessor);
                    }
                }
            } catch (Exception ignored) {
                // 静默处理
            }
        }

        return result;
    }

    /**
     * 设置指定ACCESSOR的值为预期生命值
     */
    @SuppressWarnings("unchecked")
    private static void setAccessorValue(LivingEntity entity, EntityDataAccessor<?> accessor, float expectedHealth) {
        try {
            Object currentValue = entity.getEntityData().get(accessor);

            if (currentValue instanceof Float) {
                EntityDataAccessor<Float> floatAccessor = (EntityDataAccessor<Float>) accessor;
                entity.getEntityData().set(floatAccessor, expectedHealth);
            } else if (currentValue instanceof Integer) {
                EntityDataAccessor<Integer> intAccessor = (EntityDataAccessor<Integer>) accessor;
                entity.getEntityData().set(intAccessor, (int) expectedHealth);
            } else if (currentValue instanceof Double) {
                EntityDataAccessor<Double> doubleAccessor = (EntityDataAccessor<Double>) accessor;
                entity.getEntityData().set(doubleAccessor, (double) expectedHealth);
            }
        } catch (Exception ignored) {
            // 静默处理
        }
    }

    /**
     * 对所有数字类型的ACCESSOR进行设置为预期生命值 - 使用缓存优化
     */
    private static void setAllNumericAccessors(LivingEntity entity, float expectedHealth) {
        List<EntityDataAccessor<?>> numericAccessors = getCachedNumericAccessors(entity);
        for (EntityDataAccessor<?> accessor : numericAccessors) {
            try {
                Object value = entity.getEntityData().get(accessor);
                if (value instanceof Number) {
                    setAccessorValue(entity, accessor, expectedHealth);
                }
            } catch (Exception ignored) {
                // 静默处理
            }
        }
    }
}
