package net.v_black_cat.goetydelight.ability;

import java.util.UUID;

public class AbilityRegistry {
    // Sugar Scepter 免疫能力的ID
    public static final String SUGAR_SCEPTER_IMMUNITY = "sugar_scepter_immunity";

    public static void registerAbilities() {
        // 注册Sugar Scepter免疫能力
        TimedAbilitySystem.registerAbility(
                SUGAR_SCEPTER_IMMUNITY,
                // 能力应用器
                entity -> {
                    if (entity != null) {
                        // 这里可以添加免疫效果的应用逻辑
                        // 例如设置一个标记表示实体处于免疫状态
                        entity.getPersistentData().putBoolean("hasSugarScepterImmunity", true);
                    }
                },
                // 能力移除器
                entity -> {
                    if (entity != null) {
                        // 移除免疫效果
                        entity.getPersistentData().putBoolean("hasSugarScepterImmunity", false);
                    }
                }
        );
    }
}