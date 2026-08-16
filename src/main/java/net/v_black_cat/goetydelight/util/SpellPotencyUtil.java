package net.v_black_cat.goetydelight.util;

import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.player.Player;
import net.v_black_cat.goetydelight.api.GetSpellAttributeFactory;
import net.v_black_cat.goetydelight.capability.FoodStateCapability;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class SpellPotencyUtil {

    // 统一的属性修改器UUID（每个属性一个）
    private static final Map<Attribute, UUID> ATTRIBUTE_UUIDS = new HashMap<>();

    // 所有法术强度属性
    public static final Attribute SPELL_ATTRIBUTE = GetSpellAttributeFactory.createGetSpellAttributeImplementation().getSpellPotencyAttributeModifier();
    public static final Attribute ABYSS_ATTRIBUTE = GetSpellAttributeFactory.createGetSpellAttributeImplementation().getAbyssPotencyAttributeModifier();
    public static final Attribute FROST_ATTRIBUTE = GetSpellAttributeFactory.createGetSpellAttributeImplementation().getFrostPotencyAttributeModifier();
    public static final Attribute GEOMANCY_ATTRIBUTE = GetSpellAttributeFactory.createGetSpellAttributeImplementation().getGeomancyPotencyAttributeModifier();
    public static final Attribute NECROMANCY_ATTRIBUTE = GetSpellAttributeFactory.createGetSpellAttributeImplementation().getNecromancyPotencyAttributeModifier();
    public static final Attribute NETHER_ATTRIBUTE = GetSpellAttributeFactory.createGetSpellAttributeImplementation().getNetherPotencyAttributeModifier();
    public static final Attribute STORM_ATTRIBUTE = GetSpellAttributeFactory.createGetSpellAttributeImplementation().getStormPotencyAttributeModifier();
    public static final Attribute VOID_ATTRIBUTE = GetSpellAttributeFactory.createGetSpellAttributeImplementation().getVoidPotencyAttributeModifier();
    public static final Attribute WILD_ATTRIBUTE = GetSpellAttributeFactory.createGetSpellAttributeImplementation().getWildPotencyAttributeModifier();

    // 所有属性的数组
    public static final Attribute[] ALL_ATTRIBUTES = {
            SPELL_ATTRIBUTE, ABYSS_ATTRIBUTE, FROST_ATTRIBUTE, GEOMANCY_ATTRIBUTE,
            NECROMANCY_ATTRIBUTE, NETHER_ATTRIBUTE, STORM_ATTRIBUTE, VOID_ATTRIBUTE, WILD_ATTRIBUTE
    };

    static {
        // 使用硬糖原有的UUID（保持向后兼容）
        ATTRIBUTE_UUIDS.put(SPELL_ATTRIBUTE, UUID.fromString("8b4513a0-4e2a-11ee-be56-0242ac120004"));
        ATTRIBUTE_UUIDS.put(ABYSS_ATTRIBUTE, UUID.fromString("294201d2-5085-48ea-89eb-351ffa677ea0"));
        ATTRIBUTE_UUIDS.put(FROST_ATTRIBUTE, UUID.fromString("083c98be-adba-43a1-9d70-689e8adda03f"));
        ATTRIBUTE_UUIDS.put(GEOMANCY_ATTRIBUTE, UUID.fromString("cdda777a-a9e7-4bf7-b13e-04f8c235034f"));
        ATTRIBUTE_UUIDS.put(NECROMANCY_ATTRIBUTE, UUID.fromString("1ad3e5bd-5aaa-44d0-8655-5bcf01c3f0fd"));
        ATTRIBUTE_UUIDS.put(NETHER_ATTRIBUTE, UUID.fromString("a9a1c501-d310-4e2f-93db-cfaf40c2a2b7"));
        ATTRIBUTE_UUIDS.put(STORM_ATTRIBUTE, UUID.fromString("056efc77-e10c-4f5a-b766-74243a4c9e6c"));
        ATTRIBUTE_UUIDS.put(VOID_ATTRIBUTE, UUID.fromString("a628709f-075f-4a66-8bc6-8b16e6f4c822"));
        ATTRIBUTE_UUIDS.put(WILD_ATTRIBUTE, UUID.fromString("59d8c77b-2acb-499c-8178-31eea06c4e2b"));
    }

    /**
     * 获取硬糖的强效等级
     */
    public static int getCandyPotencyLevel(Player player) {
        FoodState state = FoodStateCapability.get(player);
        return state == null ? 0 : state.getCandyPotencyLevel();
    }

    /**
     * 设置硬糖的强效等级
     */
    public static void setCandyPotencyLevel(Player player, int level) {
        FoodState state = FoodStateCapability.get(player);
        if (state != null) {
            state.setCandyPotencyLevel(level);
        }
    }

    /**
     * 获取效果的临时加成值
     */
    public static double getEffectBonus(Player player) {
        FoodState state = FoodStateCapability.get(player);
        return state == null ? 0 : state.getEffectBonus();
    }

    /**
     * 设置效果的临时加成值
     */
    public static void setEffectBonus(Player player, double bonus) {
        FoodState state = FoodStateCapability.get(player);
        if (state != null) {
            state.setEffectBonus(bonus);
        }
    }

    /**
     * 重新计算并应用总加成（硬糖+效果）
     */
    public static void recalculateAndApply(Player player) {
        if (player.level().isClientSide) return;

        // 计算总加成值
        double totalBonus = calculateTotalBonus(player);

        // 应用到所有属性
        for (Attribute attribute : ALL_ATTRIBUTES) {
            if (attribute != null) {
                UUID uuid = ATTRIBUTE_UUIDS.get(attribute);
                if (uuid != null) {
                    applyAttributeModifier(player, attribute, uuid, totalBonus);
                }
            }
        }
    }

    /**
     * 计算总加成值（硬糖永久加成 + 效果临时加成）
     */
    private static double calculateTotalBonus(Player player) {
        int candyLevel = getCandyPotencyLevel(player);
        double effectBonus = getEffectBonus(player);

        // 硬糖：每级+1
        double candyBonus = candyLevel * 1.0;

        // 总加成 = 硬糖加成 + 效果加成
        return candyBonus + effectBonus;
    }

    /**
     * 应用单个属性修改器
     */
    private static void applyAttributeModifier(Player player, Attribute attribute, UUID uuid, double bonus) {
        AttributeInstance attrInstance = player.getAttribute(attribute);
        if (attrInstance == null) return;

        // 移除旧修改器
        attrInstance.removeModifier(uuid);

        // 如果加成值为0，不添加修改器
        if (bonus <= 0) return;

        // 添加新修改器
        AttributeModifier modifier = new AttributeModifier(
                uuid,
                "Spell Potency Bonus",
                bonus,
                AttributeModifier.Operation.ADDITION
        );
        attrInstance.addPermanentModifier(modifier);
    }

    /**
     * 获取当前总加成值
     */
    public static double getTotalBonus(Player player, Attribute attribute) {
        UUID uuid = ATTRIBUTE_UUIDS.get(attribute);
        if (uuid == null) return 0;

        AttributeInstance attrInstance = player.getAttribute(attribute);
        if (attrInstance == null) return 0;

        AttributeModifier modifier = attrInstance.getModifier(uuid);
        return modifier != null ? modifier.getAmount() : 0;
    }
}