package net.v_black_cat.goetydelight.ability;

public class AbilityRegistry {
    public static final int INFINITE_DURATION = -1;
    public static final String FREEZE_IMMUNITY = "freeze_immunity";
    public static final String SUGAR_SCEPTER_IMMUNITY = "sugar_scepter_immunity";
    public static final String RUBY_HARD_CANDY_DAMAGE_REDUCTION = "ruby_hard_candy_damage_reduction";
    public static final String NIGHT_STOVE = "night_stove";
    public static final String CRIMSON_MEMORIES = "crimson_memories";
    public static final String PERMANENT_FIRE_RESISTANCE = "permanent_fire_resistance";
    public static final String PERMANENT_SAVE_EFFECTS = "permanent_save_effects";
    public static final String FOR_WARDEN_EFFECT = "for_warden_effect";
    public static final String WARDEN_DETECTED = "warden_detected";

    public static void registerAbilities() {

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
                        // 恢复客户端同步
                    }
                }
        );


        // 注册红宝石硬糖免伤能力
        TimedAbilitySystem.registerAbility(
                RUBY_HARD_CANDY_DAMAGE_REDUCTION,
                // 能力应用器
                entity -> {
                    if (entity != null) {
                        entity.getPersistentData().putBoolean("hasRubyCandyDamageReduction", true);
                    }
                },
                // 能力移除器
                entity -> {
                    if (entity != null) {
                        entity.getPersistentData().putBoolean("hasRubyCandyDamageReduction", false);
                    }
                }
        );


        //暗夜炉灶隐藏能力
        TimedAbilitySystem.registerAbility(
                NIGHT_STOVE,
                // 能力应用器
                entity -> {
                    if (entity != null) {
                        // 设置标记表示实体具有 NightStove 能力
                        entity.getPersistentData().putBoolean("hasNightStove", true);
                    }
                },
                // 能力移除器
                entity -> {
                    if (entity != null) {
                        // 移除 NightStove 能力标记
                        entity.getPersistentData().putBoolean("hasNightStove", false);
                        // 恢复客户端同步
                    }
                }
        );

        // 注册冰冻免疫能力
        TimedAbilitySystem.registerAbility(
                FREEZE_IMMUNITY,
                // 能力应用器
                entity -> {
                    if (entity != null) {
                        entity.getPersistentData().putBoolean("hasFreezeImmunity", true);
                    }
                },
                // 能力移除器
                entity -> {
                    if (entity != null) {
                        entity.getPersistentData().putBoolean("hasFreezeImmunity", false);
                    }
                }
        );

        TimedAbilitySystem.registerAbility(
                CRIMSON_MEMORIES,
                // 能力应用器
                entity -> {
                    if (entity != null) {
                        entity.getPersistentData().putBoolean("hasCrimsonMemories", true);
                    }
                },
                // 能力移除器
                entity -> {
                    if (entity != null) {
                        entity.getPersistentData().putBoolean("hasCrimsonMemories", false);
                    }
                }
        );


        TimedAbilitySystem.registerAbility(
                PERMANENT_FIRE_RESISTANCE,
                // 能力应用器
                entity -> {
                    if (entity != null) {
                        entity.getPersistentData().putBoolean("hasPermanentFireResistance", true);
                    }
                },
                // 能力移除器
                entity -> {
                    if (entity != null) {
                        entity.getPersistentData().putBoolean("hasPermanentFireResistance", false);
                    }
                }
        );

        TimedAbilitySystem.registerAbility(
                PERMANENT_SAVE_EFFECTS,
                // 能力应用器
                entity -> {
                    if (entity != null) {
                        entity.getPersistentData().putBoolean("hasPermanentSaveEffects", true);
                    }
                },
                // 能力移除器
                entity -> {
                    if (entity != null) {
                        entity.getPersistentData().putBoolean("hasPermanentSaveEffects", false);
                    }
                }
        );

        TimedAbilitySystem.registerAbility(
                PERMANENT_SAVE_EFFECTS,
                // 能力应用器
                entity -> {
                    if (entity != null) {
                        entity.getPersistentData().putBoolean("hasPermanentSaveEffects", true);
                    }
                },
                // 能力移除器
                entity -> {
                    if (entity != null) {
                        entity.getPersistentData().putBoolean("hasPermanentSaveEffects", false);
                    }
                }
        );

        TimedAbilitySystem.registerAbility(
                FOR_WARDEN_EFFECT,
                // 能力应用器
                entity -> {
                    if (entity != null) {
                        entity.getPersistentData().putBoolean("hasForWardenEffect", true);
                    }
                },
                // 能力移除器
                entity -> {
                    if (entity != null) {
                        entity.getPersistentData().putBoolean("hasForWardenEffect", false);
                    }
                }
        );
        
        TimedAbilitySystem.registerAbility(
                WARDEN_DETECTED,
                // 能力应用器
                entity -> {
                    if (entity != null) {
                        entity.getPersistentData().putBoolean("hasWardenDetected", true);
                    }
                },
                // 能力移除器
                entity -> {
                    if (entity != null) {
                        entity.getPersistentData().putBoolean("hasWardenDetected", false);
                    }
                }
        );
    }
}