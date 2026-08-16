package net.v_black_cat.goetydelight.buff.effect.impl;

import net.minecraft.core.Holder;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.v_black_cat.goetydelight.buff.ActiveBuffs;
import net.v_black_cat.goetydelight.buff.effect.BuffEffect;
import net.v_black_cat.goetydelight.init.ModAttachments;
import net.v_black_cat.goetydelight.init.ModBuffTypes;

public class MinionBoostBuffEffect implements BuffEffect {

    // 使用 ResourceLocation 作为修饰符的唯一标识
    private static final ResourceLocation ATTACK_DAMAGE_BOOST_ID =
            ResourceLocation.parse("goetydelight:minion_boost_attack_damage");
    private static final ResourceLocation MAX_HEALTH_BOOST_ID =
            ResourceLocation.parse("goetydelight:minion_boost_max_health");
    private static final ResourceLocation ARMOR_BOOST_ID =
            ResourceLocation.parse("goetydelight:minion_boost_armor");
    private static final ResourceLocation MOVEMENT_SPEED_BOOST_ID =
            ResourceLocation.parse("goetydelight:minion_boost_movement_speed");
    private static final ResourceLocation ARMOR_TOUGHNESS_BOOST_ID =
            ResourceLocation.parse("goetydelight:minion_boost_armor_toughness");

    @Override
    public void apply(LivingEntity entity, int amplifier) {
        // 每 tick 无需额外操作，属性在 onApply/onRemove 时一次性修改
    }

    @Override
    public void onApply(LivingEntity entity, int amplifier) {
        if (!(entity instanceof Player player)) return;
        refreshMinionBoosts(player);
    }

    @Override
    public void onRemove(LivingEntity entity, int amplifier) {
        if (!(entity instanceof Player player)) return;
        refreshMinionBoosts(player);
    }

    private void refreshMinionBoosts(Player player) {
        ActiveBuffs buffs = player.getData(ModAttachments.ACTIVE_BUFFS);
        if (buffs == null) return;
        int totalAmplifier = buffs.getTotalAmplifier(ModBuffTypes.MINION_BOOST.getId());

        // 遍历附近仆从，重新应用属性
        for (LivingEntity minion : player.level().getEntitiesOfClass(
                LivingEntity.class,
                player.getBoundingBox().inflate(64.0D),
                minion -> isPlayerMinion(minion, player)
        )) {
            applySingleMinionBoost(minion, totalAmplifier);
        }
    }

    private boolean isPlayerMinion(LivingEntity entity, Player player) {
        if (entity instanceof com.Polarice3.Goety.api.entities.IOwned owned) {
            return owned.getTrueOwner() == player;
        }
        return false;
    }

    private void applySingleMinionBoost(LivingEntity minion, int totalAmplifier) {
        // 先移除所有旧修饰符
        removeModifiers(minion);

        if (totalAmplifier <= 0) return;

        // 每层 10% 加成（纯乘算，使用 ADD_MULTIPLIED_BASE）
        double boostMultiplier = totalAmplifier * 0.1;

        addModifier(minion, Attributes.ATTACK_DAMAGE, ATTACK_DAMAGE_BOOST_ID, boostMultiplier);
        addModifier(minion, Attributes.MAX_HEALTH, MAX_HEALTH_BOOST_ID, boostMultiplier);
        addModifier(minion, Attributes.ARMOR, ARMOR_BOOST_ID, boostMultiplier);
        addModifier(minion, Attributes.ARMOR_TOUGHNESS, ARMOR_TOUGHNESS_BOOST_ID, boostMultiplier);

        // 移动速度仅非红石巨兽
        if (!(minion instanceof com.Polarice3.Goety.common.entities.ally.golem.RedstoneMonstrosity)) {
            addModifier(minion, Attributes.MOVEMENT_SPEED, MOVEMENT_SPEED_BOOST_ID, boostMultiplier);
        }
    }

    /**
     * 使用 ADD_MULTIPLIED_BASE 操作实现百分比乘算。
     * amount 即为乘数（例如 0.1 表示 +10%），不再手动计算 base * multiplier。
     */
    private void addModifier(LivingEntity entity, Holder<Attribute> attr, ResourceLocation id, double multiplier) {
        AttributeInstance instance = entity.getAttribute(attr);
        if (instance != null) {
            instance.addPermanentModifier(
                    new AttributeModifier(id, multiplier, AttributeModifier.Operation.ADD_MULTIPLIED_BASE)
            );
        }
    }

    private void removeModifiers(LivingEntity entity) {
        removeModifier(entity, Attributes.ATTACK_DAMAGE, ATTACK_DAMAGE_BOOST_ID);
        removeModifier(entity, Attributes.MAX_HEALTH, MAX_HEALTH_BOOST_ID);
        removeModifier(entity, Attributes.ARMOR, ARMOR_BOOST_ID);
        removeModifier(entity, Attributes.ARMOR_TOUGHNESS, ARMOR_TOUGHNESS_BOOST_ID);
        removeModifier(entity, Attributes.MOVEMENT_SPEED, MOVEMENT_SPEED_BOOST_ID);
    }

    private void removeModifier(LivingEntity entity, Holder<Attribute> attr, ResourceLocation id) {
        AttributeInstance instance = entity.getAttribute(attr);
        if (instance != null) {
            instance.removeModifier(id);
        }
    }
}