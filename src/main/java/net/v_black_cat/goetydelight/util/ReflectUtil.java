package net.v_black_cat.goetydelight.util;

import net.minecraft.world.entity.LivingEntity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Field;

public class ReflectUtil {
    private static final Logger LOGGER = LoggerFactory.getLogger(ReflectUtil.class);

    /**
     * 使用反射修改实体的当前生命值
     * @param entity 目标实体
     * @param health 新的生命值
     */
    public static void setEntityHealth(LivingEntity entity, float health) {
        try {
            // 获取 LivingEntity 类的 health 字段
            Field healthField = LivingEntity.class.getDeclaredField("f_20961_");
            healthField.setAccessible(true);
            healthField.set(entity, health);
        } catch (NoSuchFieldException e) {
            // 如果混淆名不存在，尝试其他可能的字段名
            try {
                Field healthField = LivingEntity.class.getDeclaredField("health");
                healthField.setAccessible(true);
                healthField.set(entity, health);
            } catch (Exception ex) {
                LOGGER.error("Failed to access health field via reflection", ex);
            }
        } catch (Exception e) {
            LOGGER.error("Failed to set entity health via reflection", e);
        }
    }

    /**
     * 使用反射修改实体的最大生命值
     * @param entity 目标实体
     * @param maxHealth 新的最大生命值
     */
    public static void setEntityMaxHealth(LivingEntity entity, double maxHealth) {
        try {
            // 获取属性实例
            var healthAttribute = entity.getAttribute(net.minecraft.world.entity.ai.attributes.Attributes.MAX_HEALTH);
            if (healthAttribute != null) {
                // 使用反射调用 setBaseValue
                Field baseValueField = healthAttribute.getClass().getDeclaredField("baseValue");
                baseValueField.setAccessible(true);
                baseValueField.setDouble(healthAttribute, maxHealth);

                // 刷新实体健康值
                entity.setHealth((float) maxHealth);
            }
        } catch (Exception e) {
            LOGGER.error("Failed to set entity max health via reflection", e);
        }
    }
}
