package net.v_black_cat.goetydelight.ability;

import com.Polarice3.Goety.Goety;
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
import net.v_black_cat.goetydelight.config.Config;

import java.util.UUID;

@Mod.EventBusSubscriber(modid = GoetyDelight.MODID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class MinionBoost {
    private static final String STEW_BOOST_COUNT_TAG = "LichStewBoostCount";
    private static final String SOUP_BOOST_COUNT_TAG = "NightPeaSoupBoostCount";

    private static final String STEW_MINION_BOOST_APPLIED_TAG = "LichStewBoostApplied";
    private static final String SOUP_MINION_BOOST_APPLIED_TAG = "NightPeaSoupBoostApplied";

    private static final UUID ATTACK_DAMAGE_BOOST_UUID = UUID.fromString("a90ad9a8-3776-44d1-b6c8-a464269f4bf5");
    private static final UUID MAX_HEALTH_BOOST_UUID = UUID.fromString("2d43842e-d85a-4590-8b6f-daafe15bcbcc");
    private static final UUID ARMOR_BOOST_UUID = UUID.fromString("f1a869ea-d50f-454b-847b-5b4779873078");
    private static final UUID MOVEMENT_SPEED_BOOST_UUID = UUID.fromString("dc658e47-9850-4675-b940-f1caa5501dc5");
    private static final UUID ARMOR_TOUGHNESS_BOOST_UUID = UUID.fromString("c0c0c0c0-c0c0-c0c0-c0c0-c0c0c0c0c0c0");

    public static int getStewBoostCount(Player player) {
        return player.getPersistentData().getInt(STEW_BOOST_COUNT_TAG);
    }

    public static int getSoupBoostCount(Player player) {
        return player.getPersistentData().getInt(SOUP_BOOST_COUNT_TAG);
    }

    public static void increaseStewBoostCount(Player player) {
        int currentCount = getStewBoostCount(player);
        if (currentCount < Config.getLichStewMaxCount()) {
            player.getPersistentData().putInt(STEW_BOOST_COUNT_TAG, currentCount + 1);
        }
    }

    public static void increaseSoupBoostCount(Player player) {
        int currentCount = getSoupBoostCount(player);
        if (currentCount < Config.getNightPeaSoupMaxCount()) {
            player.getPersistentData().putInt(SOUP_BOOST_COUNT_TAG, currentCount + 1);
        }
    }

    public static void applyMinionBoosts(Player player) {
        int stewCount = getStewBoostCount(player);
        int soupCount = getSoupBoostCount(player);

        if (stewCount <= 0 && soupCount <= 0) return;

        for (LivingEntity entity : player.level().getEntitiesOfClass(LivingEntity.class, player.getBoundingBox().inflate(64.0D))) {
            if (isPlayerMinion(entity, player)) {
                applyMinionBoost(entity, player, stewCount, soupCount);
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

    public static void applyMinionBoost(LivingEntity minion, Player owner, int stewBoostCount, int soupBoostCount) {
        if (minion.level().isClientSide) return;

        removeMinionBoost(minion);

        double stewBoost = LichdomHelper.isLich(owner) ? Config.getLichChaosStewBoostPercentage() * stewBoostCount : 0;
        double soupBoost = Config.getNightHeartPeaSoupBoostPercentage() * soupBoostCount;
        double boostMultiplier = stewBoost + soupBoost;

        AttributeInstance attackDamage = minion.getAttribute(Attributes.ATTACK_DAMAGE);
        if (attackDamage != null) {
            double baseValue = attackDamage.getBaseValue();
            double boostValue = baseValue * boostMultiplier;
            attackDamage.addPermanentModifier(new AttributeModifier(
                    ATTACK_DAMAGE_BOOST_UUID,
                    "Minion Attack Boost",
                    boostValue,
                    AttributeModifier.Operation.ADDITION
            ));
        }

        AttributeInstance maxHealth = minion.getAttribute(Attributes.MAX_HEALTH);
        if (maxHealth != null) {
            double baseValue = maxHealth.getBaseValue();
            double boostValue = baseValue * boostMultiplier;
            maxHealth.addPermanentModifier(new AttributeModifier(
                    MAX_HEALTH_BOOST_UUID,
                    "Minion Health Boost",
                    boostValue,
                    AttributeModifier.Operation.ADDITION
            ));

            minion.setHealth(minion.getHealth() + (float)boostValue);
        }

        AttributeInstance armor = minion.getAttribute(Attributes.ARMOR);
        if (armor != null) {
            double baseValue = armor.getBaseValue();
            double boostValue = baseValue * boostMultiplier;
            armor.addPermanentModifier(new AttributeModifier(
                    ARMOR_BOOST_UUID,
                    "Minion Armor Boost",
                    boostValue,
                    AttributeModifier.Operation.ADDITION
            ));
        }

        AttributeInstance movementSpeed = minion.getAttribute(Attributes.MOVEMENT_SPEED);
        if (movementSpeed != null && !(minion instanceof com.Polarice3.Goety.common.entities.ally.golem.RedstoneMonstrosity)) {
            double baseValue = movementSpeed.getBaseValue();
            double boostValue = baseValue * soupBoost;
            movementSpeed.addPermanentModifier(new AttributeModifier(
                    MOVEMENT_SPEED_BOOST_UUID,
                    "Minion Speed Boost",
                    boostValue,
                    AttributeModifier.Operation.ADDITION
            ));
        }

        AttributeInstance armorToughness = minion.getAttribute(Attributes.ARMOR_TOUGHNESS);
        if (armorToughness != null) {
            double baseValue = armorToughness.getBaseValue();
            double boostValue = baseValue * boostMultiplier;
            armorToughness.addPermanentModifier(new AttributeModifier(
                    ARMOR_TOUGHNESS_BOOST_UUID,
                    "Minion Armor Toughness Boost",
                    boostValue,
                    AttributeModifier.Operation.ADDITION
            ));
        }

        minion.getPersistentData().putInt(STEW_MINION_BOOST_APPLIED_TAG, stewBoostCount);
        minion.getPersistentData().putInt(SOUP_MINION_BOOST_APPLIED_TAG, soupBoostCount);
    }

    public static void removeMinionBoost(LivingEntity minion) {
        AttributeInstance attackDamage = minion.getAttribute(Attributes.ATTACK_DAMAGE);
        if (attackDamage != null) {
            attackDamage.removeModifier(ATTACK_DAMAGE_BOOST_UUID);
        }

        AttributeInstance maxHealth = minion.getAttribute(Attributes.MAX_HEALTH);
        if (maxHealth != null) {
            maxHealth.removeModifier(MAX_HEALTH_BOOST_UUID);
        }

        AttributeInstance armor = minion.getAttribute(Attributes.ARMOR);
        if (armor != null) {
            armor.removeModifier(ARMOR_BOOST_UUID);
        }

        AttributeInstance movementSpeed = minion.getAttribute(Attributes.MOVEMENT_SPEED);
        if (movementSpeed != null) {
            movementSpeed.removeModifier(MOVEMENT_SPEED_BOOST_UUID);
        }

        AttributeInstance armorToughness = minion.getAttribute(Attributes.ARMOR_TOUGHNESS);
        if (armorToughness != null) {
            armorToughness.removeModifier(ARMOR_TOUGHNESS_BOOST_UUID);
        }
    }

    @Mod.EventBusSubscriber
    public static class MinionBoostHandler {
        @SubscribeEvent
        public static void onEntityJoinLevel(EntityJoinLevelEvent event) {
            if (!event.getLevel().isClientSide() && event.getEntity() instanceof LivingEntity entity) {
                if (entity instanceof IOwned ownedEntity) {
                    LivingEntity owner = ownedEntity.getTrueOwner();
                    if (owner instanceof Player player) {
                        int soupBoostCount = getSoupBoostCount(player);
                        int stewBoostCount = getStewBoostCount(player);
                        if (soupBoostCount > 0 || stewBoostCount > 0) {
                            applyMinionBoost(entity, player, stewBoostCount, soupBoostCount);
                        }
                    }
                }
            }
        }
    }
}
