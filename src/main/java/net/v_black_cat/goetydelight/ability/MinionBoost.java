package net.v_black_cat.goetydelight.ability;

import com.Polarice3.Goety.api.entities.IOwned;
import com.Polarice3.Goety.utils.LichdomHelper;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.EntityJoinLevelEvent;
import net.v_black_cat.goetydelight.GoetyDelight;

public class MinionBoost {
    private static final String STEW_BOOST_COUNT_TAG = "LichStewBoostCount";
    private static final String SOUP_BOOST_COUNT_TAG = "NightPeaSoupBoostCount";

    private static final ResourceLocation ATTACK_ID = ResourceLocation.fromNamespaceAndPath(GoetyDelight.MODID, "minion_attack");
    private static final ResourceLocation HEALTH_ID = ResourceLocation.fromNamespaceAndPath(GoetyDelight.MODID, "minion_health");
    private static final ResourceLocation ARMOR_ID = ResourceLocation.fromNamespaceAndPath(GoetyDelight.MODID, "minion_armor");
    private static final ResourceLocation SPEED_ID = ResourceLocation.fromNamespaceAndPath(GoetyDelight.MODID, "minion_speed");
    private static final ResourceLocation TOUGHNESS_ID = ResourceLocation.fromNamespaceAndPath(GoetyDelight.MODID, "minion_toughness");

    public static int getStewBoostCount(Player player) {
        return player.getPersistentData().getInt(STEW_BOOST_COUNT_TAG);
    }

    public static int getSoupBoostCount(Player player) {
        return player.getPersistentData().getInt(SOUP_BOOST_COUNT_TAG);
    }

    public static void removeMinionBoost(LivingEntity minion) {
        AttributeInstance ai;
        ai = minion.getAttribute(Attributes.ATTACK_DAMAGE); if (ai != null) ai.removeModifier(ATTACK_ID);
        ai = minion.getAttribute(Attributes.MAX_HEALTH); if (ai != null) ai.removeModifier(HEALTH_ID);
        ai = minion.getAttribute(Attributes.ARMOR); if (ai != null) ai.removeModifier(ARMOR_ID);
        ai = minion.getAttribute(Attributes.MOVEMENT_SPEED); if (ai != null) ai.removeModifier(SPEED_ID);
        ai = minion.getAttribute(Attributes.ARMOR_TOUGHNESS); if (ai != null) ai.removeModifier(TOUGHNESS_ID);
    }

    @EventBusSubscriber(modid = GoetyDelight.MODID)
    public static class MinionBoostHandler {
        @SubscribeEvent
        public static void onEntityJoinLevel(EntityJoinLevelEvent event) {
            if (!event.getLevel().isClientSide() && event.getEntity() instanceof LivingEntity entity) {
                if (entity instanceof IOwned ownedEntity) {
                    LivingEntity owner = ownedEntity.getTrueOwner();
                    if (owner instanceof Player player) {
                        int soup = getSoupBoostCount(player);
                        int stew = getStewBoostCount(player);
                        if (soup > 0 || stew > 0) {
                            removeMinionBoost(entity);
                            applyMinionBoost(entity, stew, soup);
                        }
                    }
                }
            }
        }
    }

    private static void applyMinionBoost(LivingEntity minion, int stewCount, int soupCount) {
        if (minion.level().isClientSide) return;
        double stewPct = LichdomHelper.isLich(null) ? 0.2 * stewCount : 0;
        double soupPct = 0.15 * soupCount;
        double mult = stewPct + soupPct;
        addBoost(minion, Attributes.ATTACK_DAMAGE, ATTACK_ID, mult);
        addBoost(minion, Attributes.MAX_HEALTH, HEALTH_ID, mult);
        addBoost(minion, Attributes.ARMOR, ARMOR_ID, mult);
        addBoost(minion, Attributes.ARMOR_TOUGHNESS, TOUGHNESS_ID, mult);
        if (!(minion instanceof com.Polarice3.Goety.common.entities.ally.golem.RedstoneMonstrosity))
            addBoost(minion, Attributes.MOVEMENT_SPEED, SPEED_ID, soupPct);
    }

    private static void addBoost(LivingEntity e, net.minecraft.core.Holder<net.minecraft.world.entity.ai.attributes.Attribute> attr, ResourceLocation id, double mult) {
        AttributeInstance ai = e.getAttribute(attr);
        if (ai != null && mult > 0) {
            double v = ai.getBaseValue() * mult;
            ai.addPermanentModifier(new AttributeModifier(id, v, AttributeModifier.Operation.ADD_VALUE));
        }
    }
}
