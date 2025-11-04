package net.v_black_cat.goetydelight.item;

import com.google.common.collect.ImmutableMultimap;
import com.google.common.collect.Multimap;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.TamableAnimal;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.SwordItem;
import net.minecraft.world.item.Tier;
import net.minecraft.world.phys.AABB;
import net.v_black_cat.goetydelight.config.Config;

import java.util.Comparator;
import java.util.List;
import java.util.UUID;

public class StarlessNightitem extends SwordItem {
    public StarlessNightitem(Tier tier, int attackDamageModifier, float attackSpeedModifier, Item.Properties properties) {
        super(tier, attackDamageModifier, attackSpeedModifier, properties);
    }
    private int attackCount = 0;
    private long lastAttackTime = 0;
    public int getAttackCount() {
        return attackCount;
    }
    private boolean isFriendly(Player player, LivingEntity entity) {
        if (entity instanceof TamableAnimal tamable && tamable.isTame() && tamable.getOwner() == player) {
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
        double extendedRange = 2.0;
        List<Entity> nearbyEntities = player.level().getEntitiesOfClass(Entity.class,
                player.getBoundingBox().inflate(extendedRange),
                e -> e instanceof LivingEntity &&
                        e != entity &&
                        e != player &&
                        player.distanceTo(e) <= (3.0 + extendedRange) &&
                        e.isAlive());
        for (Entity nearbyEntity : nearbyEntities) {
            if (nearbyEntity instanceof LivingEntity livingEntity) {
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
            if (damage >= living.getHealth()) {
                float excessDamage = damage - living.getHealth();
                living.hurt(source, damage);
                if (excessDamage > 0) {
                    applyChainDamage(player, living, excessDamage, source);
                }
            } else {
                living.hurt(source, damage);
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
            Player player = net.minecraft.client.Minecraft.getInstance().player;
            if (player != null && !player.getOffhandItem().isEmpty()) {
                builder.put(Attributes.ATTACK_SPEED, new AttributeModifier(
                        UUID.fromString("2a88e61f-7f5e-4d2d-9f3a-8b1e7e1a8a2f"),
                        "Offhand slowdown",
                        -0.5,
                        AttributeModifier.Operation.MULTIPLY_BASE
                ));
            }
            return builder.build();
        }
        return modifiers;
    }
}