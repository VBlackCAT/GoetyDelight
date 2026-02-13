package net.v_black_cat.goetydelight.item.food;

import com.Polarice3.Goety.api.entities.IOwned;
import com.Polarice3.Goety.utils.LichdomHelper;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.EntityJoinLevelEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.v_black_cat.goetydelight.GoetyDelight;
import net.v_black_cat.goetydelight.ability.AbilityRegistry;
import net.v_black_cat.goetydelight.ability.TimedAbilitySystem;
import net.v_black_cat.goetydelight.config.Config;

import java.util.UUID;

@Mod.EventBusSubscriber(modid = GoetyDelight.MODID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class LichsChaosStewItem extends Item {

    private static final int MAX_BOOST_COUNT = 6;

    private static final double BOOST_PERCENTAGE = 0.15;

    private static final String STEW_BOOST_COUNT_TAG = "LichStewBoostCount";

    private static final String MINION_BOOST_APPLIED_TAG = "LichStewBoostApplied";

    private static final int FIRE_RESISTANCE_DURATION = -1;

    private static final UUID ATTACK_DAMAGE_BOOST_UUID = UUID.fromString("a90ad9a8-3776-44d1-b6c8-a464269f4bf5");
    private static final UUID MAX_HEALTH_BOOST_UUID = UUID.fromString("2d43842e-d85a-4590-8b6f-daafe15bcbcc");
    private static final UUID ARMOR_BOOST_UUID = UUID.fromString("f1a869ea-d50f-454b-847b-5b4779873078");
    private static final UUID ARMOR_TOUGHNESS_BOOST_UUID = UUID.fromString("c0c0c0c0-c0c0-c0c0-c0c0-c0c0c0c0c0c0");

    public LichsChaosStewItem(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand usedHand) {
        if (LichdomHelper.isLich(player)) {
            return super.use(level, player, usedHand);
        } else {
            return InteractionResultHolder.fail(player.getItemInHand(usedHand));
        }
    }

    @Override
    public ItemStack finishUsingItem(ItemStack stack, Level level, LivingEntity livingEntity) {
        if (LichdomHelper.isLich(livingEntity)) {
            ItemStack result = super.finishUsingItem(stack, level, livingEntity);

            if (!level.isClientSide && livingEntity instanceof Player player) {

                increaseStewBoostCount(player);

                applyMinionBoosts(player, getStewBoostCount(player));


                TimedAbilitySystem.addAbilityToEntity(player, AbilityRegistry.PERMANENT_FIRE_RESISTANCE, FIRE_RESISTANCE_DURATION);
                TimedAbilitySystem.addAbilityToEntity(player, AbilityRegistry.PERMANENT_SAVE_EFFECTS, FIRE_RESISTANCE_DURATION);

            }

            return result;
        }

        return stack;
    }


    public static int getStewBoostCount(Player player) {
        return player.getPersistentData().getInt(STEW_BOOST_COUNT_TAG);
    }


    private void increaseStewBoostCount(Player player) {
        int currentCount = getStewBoostCount(player);
        if (currentCount < MAX_BOOST_COUNT) {
            player.getPersistentData().putInt(STEW_BOOST_COUNT_TAG, currentCount + 1);
        }
    }


    private void applyMinionBoosts(Player player, int boostCount) {
        if (boostCount <= 0) return;

        for (LivingEntity entity : player.level().getEntitiesOfClass(LivingEntity.class, player.getBoundingBox().inflate(64.0D))) {
            if (isPlayerMinion(entity, player)) {
                applyMinionBoost(entity, boostCount);
            }
        }
    }

    // 移除原有的 onPlayerTick 方法，因为抗火效果现在由 TimedAbilitySystem 处理

    private boolean isPlayerMinion(LivingEntity entity, Player player) {
        if (entity instanceof IOwned ownedEntity) {
            LivingEntity owner = ownedEntity.getTrueOwner();
            return owner == player;
        }
        return false;
    }


    public static void applyMinionBoost(LivingEntity minion, int boostCount) {
        if (minion.level().isClientSide) return;


        removeMinionBoost(minion);


        double boostMultiplier = BOOST_PERCENTAGE * boostCount;


        AttributeInstance attackDamage = minion.getAttribute(Attributes.ATTACK_DAMAGE);
        if (attackDamage != null) {
            double baseValue = attackDamage.getBaseValue();
            double boostValue = baseValue * boostMultiplier;
            attackDamage.addPermanentModifier(new AttributeModifier(
                    ATTACK_DAMAGE_BOOST_UUID,
                    "Lich Stew Attack Boost",
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
                    "Lich Stew Health Boost",
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
                    "Lich Stew Armor Boost",
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
                    "Lich Stew Armor Toughness Boost",
                    boostValue,
                    AttributeModifier.Operation.ADDITION
            ));
        }


        minion.getPersistentData().putInt(MINION_BOOST_APPLIED_TAG, boostCount);
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


                    if (owner instanceof Player player && LichdomHelper.isLich(player)) {
                        int boostCount = getStewBoostCount(player);
                        if (boostCount > 0) {
                            applyMinionBoost(entity, boostCount);
                        }
                    }
                }
            }
        }
    }
}