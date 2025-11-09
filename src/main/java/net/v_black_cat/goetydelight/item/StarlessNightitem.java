package net.v_black_cat.goetydelight.item;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.ImmutableMultimap;
import com.google.common.collect.Multimap;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.SwordItem;
import net.minecraft.world.item.Tier;
import net.minecraft.world.phys.AABB;
import net.minecraftforge.common.ForgeMod;
import net.v_black_cat.goetydelight.config.Config;
import com.Polarice3.Goety.api.entities.IOwned;


import java.util.Comparator;
import java.util.List;
import java.util.UUID;

public class StarlessNightitem extends SwordItem {
    public StarlessNightitem(Tier tier, int attackDamageModifier, float attackSpeedModifier, Item.Properties properties) {
        super(tier, attackDamageModifier, attackSpeedModifier, properties);
    }
    private int attackCount = 0;
    private long lastAttackTime = 0;
    private float entityReach = 3.0f;
    private long lastHealthSetTick = 0;
    public int getAttackCount() {
        return attackCount;
    }

    private boolean isFriendly(Player player, LivingEntity entity) {
        if (entity == player) {
            return true;
        }

        if (entity instanceof Player targetPlayer && targetPlayer.isCreative()) {
            return true;
        }

        if (player.getTeam() != null && entity.getTeam() != null) {
            return player.getTeam().isAlliedTo(entity.getTeam());
        }
        if (entity instanceof IOwned owned && owned.getTrueOwner() == player) {
            return true;
        }
        if (Config.getStarlessNightWhitelist().contains(entity.getType())) {
                return true;
        }
        return false;
    }
    private void applyChainDamage(Player player, LivingEntity originalTarget, float excessDamage, DamageSource source) {
        double searchRange = Config.getStarlessNightSearchRange();
        AABB searchBox = originalTarget.getBoundingBox().inflate(searchRange);
        int maxChainTargets = Config.getStarlessNightMaxChainTargets();
        List<LivingEntity> nearbyEntities = player.level().getEntitiesOfClass(LivingEntity.class, searchBox)
                .stream()
                .filter(entity -> entity != originalTarget && entity != player)
                .filter(entity -> !isFriendly(player, entity))
                .filter(entity -> entity.isAlive())
                .sorted(Comparator.comparingDouble(entity -> entity.distanceToSqr(originalTarget)))
                .limit(maxChainTargets)
                .toList();

        float remainingDamage = excessDamage;
        for (LivingEntity nearbyEntity : nearbyEntities) {
            if (remainingDamage <= 0) break;
            float entityHealth = nearbyEntity.getHealth();
            if (remainingDamage >= entityHealth) {
                nearbyEntity.hurt(source, remainingDamage);
                remainingDamage = remainingDamage - entityHealth;
            } else {
                nearbyEntity.hurt(source, remainingDamage);
                remainingDamage = 0;
                break;
            }
        }
    }
    @Override
    public boolean onLeftClickEntity(ItemStack stack, Player player, Entity entity) {
        if (player == null || player.level() == null || !player.isAlive()) {
            return super.onLeftClickEntity(stack, player, entity);
        }
        if (player.level().isClientSide) {
            return super.onLeftClickEntity(stack, player, entity);
        }
        double chainRange = 16.0;
        List<Entity> nearbyEntities = player.level().getEntitiesOfClass(Entity.class,
                player.getBoundingBox().inflate(chainRange),
                e -> e instanceof LivingEntity &&
                        e != entity &&
                        e != player &&
                        player.distanceTo(e) <= (chainRange) &&
                        e.isAlive() &&
                        e.level() != null); // 添加level非空检查

        for (Entity nearbyEntity : nearbyEntities) {
            if (nearbyEntity instanceof LivingEntity livingEntity) {
                if (!isFriendly(player, livingEntity)) {
                    continue;
                }
                // 检查实体是否仍然有效
                if (livingEntity.level() == null || !livingEntity.isAlive()) {
                    continue;
                }

                int currentTick = (int) player.level().getGameTime();
                if (currentTick - lastAttackTime > 100) {
                    attackCount = 0;
                }
                attackCount++;
                if (attackCount > Config.getMaxAttackCount()) {
                    attackCount = Config.getMaxAttackCount();
                }
                DamageSource source = new DamageSource(player.damageSources().genericKill().typeHolder(), player);
                float attackDamageModifier = (float) getTier().getAttackDamageBonus();
                float damage = attackDamageModifier * (attackCount + 1);
                livingEntity.hurt(source, damage);
            }
        }

        if (entity instanceof LivingEntity living) {
            if(player.hasLineOfSight(entity)) {
                if (isFriendly(player, living)) {
                    return super.onLeftClickEntity(stack, player, entity);
                }
                // 检查实体是否仍然有效
                if (living.level() == null || !living.isAlive()) {
                    return super.onLeftClickEntity(stack, player, entity);
                }

                int currentTick = (int) player.level().getGameTime();
                if (currentTick - lastAttackTime > 100) {
                    attackCount = 0;
                }
                attackCount++;
                if (attackCount > Config.getMaxAttackCount()) {
                    attackCount = Config.getMaxAttackCount();
                }
                lastAttackTime = currentTick;
                DamageSource source = new DamageSource(player.damageSources().genericKill().typeHolder(), player);
                float attackDamageModifier = (float) getTier().getAttackDamageBonus();
                float damage = attackDamageModifier * (attackCount + 1);
                float originalHealth = living.getHealth();
                living.hurt(source, damage);
                if (living.getHealth() == originalHealth && !living.isInvulnerable()) {
                    if (damage < living.getHealth()) {
                        long nextTick = player.level().getGameTime();
                        if (nextTick - lastHealthSetTick >= 10) { // 10 tick 间隔
                            living.setHealth(originalHealth - damage);
                            lastHealthSetTick = nextTick;
                        }
                    }
                    if (damage >= living.getHealth()) {
                            living.setHealth(0);
                            living.die(player.damageSources().playerAttack(player));

                    }
                } else {
                    if (damage >= originalHealth) {
                        float excessDamage = damage - originalHealth;
                        if (excessDamage > 0) {
                            applyChainDamage(player, living, excessDamage, source);
                        }
                    }
                }
            }
        }
        return super.onLeftClickEntity(stack, player, entity);
    }
    @Override
    public Multimap<Attribute, AttributeModifier> getAttributeModifiers(EquipmentSlot slot, ItemStack stack) {
        Multimap<Attribute, AttributeModifier> modifiers = super.getAttributeModifiers(slot, stack);
        if (slot == EquipmentSlot.MAINHAND) {
            ImmutableMultimap.Builder<Attribute, AttributeModifier> builder = ImmutableMultimap.builder();
            builder.putAll(modifiers);
            builder.put(ForgeMod.ENTITY_REACH.get(), new AttributeModifier(
                    UUID.fromString("f1e6a1e2-b1c3-d1e4-f1a5-b1c6d1e7f1a8"),
                    "Weapon attack range",
                    entityReach,
                    AttributeModifier.Operation.ADDITION
            ));
            return builder.build();
        }
        return modifiers;
    }
}