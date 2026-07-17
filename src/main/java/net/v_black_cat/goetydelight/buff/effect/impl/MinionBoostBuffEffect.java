package net.v_black_cat.goetydelight.buff.effect.impl;

import com.Polarice3.Goety.api.entities.IOwned;
import com.Polarice3.Goety.utils.LichdomHelper;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.event.entity.EntityJoinLevelEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.v_black_cat.goetydelight.GoetyDelight;
import net.v_black_cat.goetydelight.buff.effect.BuffEffect;
import net.v_black_cat.goetydelight.config.Config;
import net.v_black_cat.goetydelight.init.ModBuffTypes;
import net.v_black_cat.goetydelight.util.BuffUtil;

import java.util.UUID;

@Mod.EventBusSubscriber(modid = GoetyDelight.MODID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class MinionBoostBuffEffect implements BuffEffect {

    private static final UUID ATTACK_DAMAGE_BOOST_UUID = UUID.fromString("a90ad9a8-3776-44d1-b6c8-a464269f4bf5");
    private static final UUID MAX_HEALTH_BOOST_UUID = UUID.fromString("2d43842e-d85a-4590-8b6f-daafe15bcbcc");
    private static final UUID ARMOR_BOOST_UUID = UUID.fromString("f1a869ea-d50f-454b-847b-5b4779873078");
    private static final UUID MOVEMENT_SPEED_BOOST_UUID = UUID.fromString("dc658e47-9850-4675-b940-f1caa5501dc5");
    private static final UUID ARMOR_TOUGHNESS_BOOST_UUID = UUID.fromString("c0c0c0c0-c0c0-c0c0-c0c0-c0c0c0c0c0c0");

    @Override
    public void apply(LivingEntity entity, int amplifier) {
        // amplifier 已由 BuffSystem 计算为 totalAmplifier，直接使用
        if (entity instanceof Player player && !player.level().isClientSide) {
            for (LivingEntity minion : player.level().getEntitiesOfClass(LivingEntity.class,
                    player.getBoundingBox().inflate(64.0D))) {
                if (isPlayerMinion(minion, player)) {
                    applySingleMinionBoost(minion, amplifier);
                }
            }
        }
    }

    @Override
    public void onApply(LivingEntity entity, int amplifier) {}

    @Override
    public void onRemove(LivingEntity entity, int amplifier) {}

    @SubscribeEvent
    public static void onEntityJoinLevel(EntityJoinLevelEvent event) {
        if (!event.getLevel().isClientSide() && event.getEntity() instanceof LivingEntity entity) {
            if (entity instanceof IOwned ownedEntity) {
                LivingEntity owner = ownedEntity.getTrueOwner();
                if (owner instanceof Player player) {
                    int totalAmplifier = BuffUtil.getTotalAmplifier(player, ModBuffTypes.MINION_BOOST.getId());
                    if (totalAmplifier > 0) {
                        applySingleMinionBoost(entity, totalAmplifier);
                    }
                }
            }
        }
    }

    private static boolean isPlayerMinion(LivingEntity entity, Player player) {
        if (entity instanceof IOwned ownedEntity) {
            LivingEntity owner = ownedEntity.getTrueOwner();
            return owner == player;
        }
        return false;
    }

    private static void applySingleMinionBoost(LivingEntity minion, int totalAmplifier) {
        if (minion.level().isClientSide) return;

        removeMinionBoost(minion);

        double boostMultiplier = Config.getLichChaosStewBoostPercentage() * totalAmplifier;

        AttributeInstance attackDamage = minion.getAttribute(Attributes.ATTACK_DAMAGE);
        if (attackDamage != null) {
            double boostValue = attackDamage.getBaseValue() * boostMultiplier;
            attackDamage.addPermanentModifier(new AttributeModifier(
                    ATTACK_DAMAGE_BOOST_UUID, "Minion Attack Boost", boostValue,
                    AttributeModifier.Operation.ADDITION));
        }

        AttributeInstance maxHealth = minion.getAttribute(Attributes.MAX_HEALTH);
        if (maxHealth != null) {
            double boostValue = maxHealth.getBaseValue() * boostMultiplier;
            maxHealth.addPermanentModifier(new AttributeModifier(
                    MAX_HEALTH_BOOST_UUID, "Minion Health Boost", boostValue,
                    AttributeModifier.Operation.ADDITION));
            minion.setHealth(minion.getHealth() + (float) boostValue);
        }

        AttributeInstance armor = minion.getAttribute(Attributes.ARMOR);
        if (armor != null) {
            double boostValue = armor.getBaseValue() * boostMultiplier;
            armor.addPermanentModifier(new AttributeModifier(
                    ARMOR_BOOST_UUID, "Minion Armor Boost", boostValue,
                    AttributeModifier.Operation.ADDITION));
        }

        AttributeInstance movementSpeed = minion.getAttribute(Attributes.MOVEMENT_SPEED);
        if (movementSpeed != null && !(minion instanceof com.Polarice3.Goety.common.entities.ally.golem.RedstoneMonstrosity)) {
            double boostValue = movementSpeed.getBaseValue() * boostMultiplier;
            movementSpeed.addPermanentModifier(new AttributeModifier(
                    MOVEMENT_SPEED_BOOST_UUID, "Minion Speed Boost", boostValue,
                    AttributeModifier.Operation.ADDITION));
        }

        AttributeInstance armorToughness = minion.getAttribute(Attributes.ARMOR_TOUGHNESS);
        if (armorToughness != null) {
            double boostValue = armorToughness.getBaseValue() * boostMultiplier;
            armorToughness.addPermanentModifier(new AttributeModifier(
                    ARMOR_TOUGHNESS_BOOST_UUID, "Minion Armor Toughness Boost", boostValue,
                    AttributeModifier.Operation.ADDITION));
        }
    }

    private static void removeMinionBoost(LivingEntity minion) {
        AttributeInstance attackDamage = minion.getAttribute(Attributes.ATTACK_DAMAGE);
        if (attackDamage != null) attackDamage.removeModifier(ATTACK_DAMAGE_BOOST_UUID);

        AttributeInstance maxHealth = minion.getAttribute(Attributes.MAX_HEALTH);
        if (maxHealth != null) maxHealth.removeModifier(MAX_HEALTH_BOOST_UUID);

        AttributeInstance armor = minion.getAttribute(Attributes.ARMOR);
        if (armor != null) armor.removeModifier(ARMOR_BOOST_UUID);

        AttributeInstance movementSpeed = minion.getAttribute(Attributes.MOVEMENT_SPEED);
        if (movementSpeed != null) movementSpeed.removeModifier(MOVEMENT_SPEED_BOOST_UUID);

        AttributeInstance armorToughness = minion.getAttribute(Attributes.ARMOR_TOUGHNESS);
        if (armorToughness != null) armorToughness.removeModifier(ARMOR_TOUGHNESS_BOOST_UUID);
    }
}
