package net.v_black_cat.goetydelight.compat.goety_revelation.item;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.*;
import net.minecraft.world.level.Level;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.living.LivingAttackEvent;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.event.entity.living.LivingHealEvent;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;

@Mod.EventBusSubscriber
public class StoneSwordSkewerItem extends SwordItem {
    private static final UUID ATTACK_DAMAGE_MODIFIER_UUID = UUID.fromString("a1b2c3d4-e5f6-7890-abcd-ef1234567890");
    private static final UUID MAX_HEALTH_MODIFIER_UUID = UUID.fromString("b2c3d4e5-f6a7-8901-bcde-f23456789012");
    private static final String EFFECT_DURATION_TAG = "StoneSwordSkewerEffectDuration";
    private static final String DEATH_IMMUNITY_TAG = "StoneSwordSkewerDeathImmunity";
    private static final int EFFECT_DURATION = 10 * 60 * 20;

    public StoneSwordSkewerItem(Tier tier, int attackDamageModifier, float attackSpeedModifier, Properties properties) {
        super(tier, attackDamageModifier, attackSpeedModifier, properties);
        MinecraftForge.EVENT_BUS.register(this);
    }

    @Override
    public ItemStack finishUsingItem(ItemStack stack, Level level, LivingEntity entity) {
        if (entity instanceof Player player) {
            if (!level.isClientSide) {
                applyEffects(player);
                if (!player.getAbilities().instabuild) {
                    stack.shrink(1);
                }
            }
        }
        return stack;
    }

    private void applyEffects(Player player) {
        CompoundTag persistentData = player.getPersistentData();
        persistentData.putInt(EFFECT_DURATION_TAG, EFFECT_DURATION);
        persistentData.putBoolean(DEATH_IMMUNITY_TAG, true);
        updateAttackDamageModifier(player);
    }

    private void updateAttackDamageModifier(Player player) {
        AttributeInstance attackAttribute = player.getAttribute(Attributes.ATTACK_DAMAGE);
        if (attackAttribute != null) {
            attackAttribute.removeModifier(ATTACK_DAMAGE_MODIFIER_UUID);
            float healthPercent = player.getHealth() / player.getMaxHealth();
            AttributeModifier modifier = getAttributeModifier(player, healthPercent);
            attackAttribute.addPermanentModifier(modifier);
        }
    }

    private static @NotNull AttributeModifier getAttributeModifier(Player player, float healthPercent) {
        float multiplier;
        if (healthPercent >= 1.0f && player.getHealth() != 1.0f) {
            multiplier = 1.0f;
        } else if (player.getHealth() <= 1.0f) {
            multiplier = 5.0f;
        } else {
            multiplier = 1.0f + (1.0f - healthPercent) * 4.0f;
        }
        AttributeModifier modifier = new AttributeModifier(
                ATTACK_DAMAGE_MODIFIER_UUID,
                "Stone Sword Skewer Attack Boost",
                multiplier,
                AttributeModifier.Operation.ADDITION
        );
        return modifier;
    }

    private void applyMaxHealthLimitModifier(Player player) {
        AttributeInstance maxHealthAttribute = player.getAttribute(Attributes.MAX_HEALTH);
        if (maxHealthAttribute != null) {
            maxHealthAttribute.removeModifier(MAX_HEALTH_MODIFIER_UUID);
            AttributeModifier modifier = new AttributeModifier(
                    MAX_HEALTH_MODIFIER_UUID,
                    "Stone Sword Skewer Max Health Limit",
                    0.0,
                    AttributeModifier.Operation.MULTIPLY_TOTAL
            );

            maxHealthAttribute.addPermanentModifier(modifier);
        }
    }

    private static void removeMaxHealthLimitModifier(Player player) {
        AttributeInstance maxHealthAttribute = player.getAttribute(Attributes.MAX_HEALTH);
        if (maxHealthAttribute != null) {
            maxHealthAttribute.removeModifier(MAX_HEALTH_MODIFIER_UUID);
        }
    }

    @SubscribeEvent
    public void onPlayerTick(TickEvent.PlayerTickEvent event) {
        if (event.phase != TickEvent.Phase.END) return;
        if (event.player.level().isClientSide) return;

        Player player = event.player;
        CompoundTag persistentData = player.getPersistentData();
        if (persistentData.contains(EFFECT_DURATION_TAG)) {
            int remainingDuration = persistentData.getInt(EFFECT_DURATION_TAG);
            if (remainingDuration > 0) {
                persistentData.putInt(EFFECT_DURATION_TAG, remainingDuration - 1);
                if (persistentData.contains(DEATH_IMMUNITY_TAG) && !persistentData.getBoolean(DEATH_IMMUNITY_TAG)) {
                    applyMaxHealthLimitModifier(player);
                }
                if (remainingDuration <= 1) {
                    if (!hasHostileMobsNearby(player)) {
                        removeAllEffects(player);
                    } else {
                        persistentData.putInt(EFFECT_DURATION_TAG, 5);
                    }
                }
                if (player.tickCount % 10 == 0) {
                    updateAttackDamageModifier(player);
                }
            }
        }
    }

    @SubscribeEvent(priority = EventPriority.HIGH)
    public void onLivingAttack(LivingAttackEvent event) {
        if (event.getEntity() instanceof Player player) {
            CompoundTag persistentData = player.getPersistentData();
            if (persistentData.contains(EFFECT_DURATION_TAG)) {
                int remainingDuration = persistentData.getInt(EFFECT_DURATION_TAG);
                if (remainingDuration > 0) {
                    if (player.getRandom().nextFloat() < 0.37f) {
                        event.setCanceled(true);
                    }
                }
            }
        }
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public void onLivingDeath(LivingDeathEvent event) {
        if (event.getEntity() instanceof Player player) {
            CompoundTag persistentData = player.getPersistentData();
            if (persistentData.contains(DEATH_IMMUNITY_TAG) && persistentData.getBoolean(DEATH_IMMUNITY_TAG)) {
                event.setCanceled(true);
                player.setHealth(1.0f);
                persistentData.putBoolean(DEATH_IMMUNITY_TAG, false);
                applyMaxHealthLimitModifier(player);
                updateAttackDamageModifier(player);
            }
        }
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public void onLivingHeal(LivingHealEvent event) {
        if (event.getEntity() instanceof Player player) {
            CompoundTag persistentData = player.getPersistentData();
            if (persistentData.contains(EFFECT_DURATION_TAG)) {
                int remainingDuration = persistentData.getInt(EFFECT_DURATION_TAG);
                if (remainingDuration > 0) {
                    event.setCanceled(true);
                }
            }
        }
    }

    @SubscribeEvent
    public void onLivingHurt(LivingHurtEvent event) {
        if (event.getEntity() instanceof Player player) {
            CompoundTag persistentData = player.getPersistentData();

            if (persistentData.contains(EFFECT_DURATION_TAG)) {
                int remainingDuration = persistentData.getInt(EFFECT_DURATION_TAG);
                if (remainingDuration > 0) {
                    updateAttackDamageModifier(player);
                }
            }
        }
    }

    private static boolean hasHostileMobsNearby(Player player) {
        return !player.level().getEntitiesOfClass(
                LivingEntity.class,
                player.getBoundingBox().inflate(16.0),
                entity -> entity != player &&
                        entity.isAlive() &&
                        isHostileToPlayer(entity, player)
        ).isEmpty();
    }

    private static boolean isHostileToPlayer(LivingEntity entity, Player player) {
        if (entity instanceof Player) return false;
        return entity.getType().getCategory() == net.minecraft.world.entity.MobCategory.MONSTER &&
                entity.canAttack(player);
    }

    private static void removeAllEffects(Player player) {
        CompoundTag persistentData = player.getPersistentData();

        persistentData.remove(EFFECT_DURATION_TAG);
        persistentData.remove(DEATH_IMMUNITY_TAG);

        AttributeInstance attackAttribute = player.getAttribute(Attributes.ATTACK_DAMAGE);
        if (attackAttribute != null) {
            attackAttribute.removeModifier(ATTACK_DAMAGE_MODIFIER_UUID);
        }

        removeMaxHealthLimitModifier(player);
    }
}