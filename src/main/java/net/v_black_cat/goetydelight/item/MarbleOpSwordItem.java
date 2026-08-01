package net.v_black_cat.goetydelight.item;

import com.Polarice3.Goety.api.entities.IOwned;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlotGroup;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.SwordItem;
import net.minecraft.world.item.Tier;
import net.minecraft.world.item.component.ItemAttributeModifiers;
import net.minecraft.world.phys.AABB;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.AttackEntityEvent;
import net.v_black_cat.goetydelight.GoetyDelight;

import java.util.Comparator;
import java.util.List;

/**
 * 1.21.1 移植版：大理石 OP 剑（范围攻击 + 连锁伤害）。
 * 1.21.1 移除了 Item.onLeftClickEntity，改用 PlayerInteractEvent.LeftClickEntity 事件。
 */
@EventBusSubscriber(modid = GoetyDelight.MODID)
public class MarbleOpSwordItem extends SwordItem {
    private int attackCount = 0;
    private long lastAttackTime = 0;
    private final float entityReach = 30.0f;
    private long lastHealthSetTick = 0;

    public MarbleOpSwordItem(Tier tier, Item.Properties properties) {
        super(tier, properties.attributes(ItemAttributeModifiers.builder()
                .add(Attributes.ATTACK_DAMAGE,
                        new AttributeModifier(ResourceLocation.withDefaultNamespace("base_attack_damage"),
                                1.0F + tier.getAttackDamageBonus(), AttributeModifier.Operation.ADD_VALUE),
                        EquipmentSlotGroup.MAINHAND)
                .add(Attributes.ATTACK_SPEED,
                        new AttributeModifier(ResourceLocation.withDefaultNamespace("base_attack_speed"),
                                2.0F, AttributeModifier.Operation.ADD_VALUE),
                        EquipmentSlotGroup.MAINHAND)
                .add(Attributes.ENTITY_INTERACTION_RANGE,
                        new AttributeModifier(ResourceLocation.fromNamespaceAndPath(GoetyDelight.MODID, "marble_op_reach"),
                                30.0F, AttributeModifier.Operation.ADD_VALUE),
                        EquipmentSlotGroup.MAINHAND)
                .build()));
    }

    @SubscribeEvent
    public static void onAttackEntity(AttackEntityEvent event) {
        Player player = event.getEntity();
        if (!(player.getMainHandItem().getItem() instanceof MarbleOpSwordItem sword)) {
            return;
        }
        sword.handleLeftClick(player, event.getTarget());
    }

    private void handleLeftClick(Player player, Entity entity) {
        if (player == null || player.level() == null || !player.isAlive()) {
            return;
        }
        if (player.level().isClientSide) {
            return;
        }

        double chainRange = 16.0;
        List<Entity> nearbyEntities = player.level().getEntitiesOfClass(Entity.class,
                player.getBoundingBox().inflate(chainRange),
                e -> e instanceof LivingEntity &&
                        e != entity &&
                        e != player &&
                        player.distanceTo(e) <= chainRange &&
                        e.isAlive());

        for (Entity nearbyEntity : nearbyEntities) {
            if (nearbyEntity instanceof LivingEntity livingEntity) {
                if (!isFriendly(player, livingEntity)) {
                    continue;
                }
                if (!livingEntity.isAlive()) {
                    continue;
                }

                int currentTick = (int) player.level().getGameTime();
                if (currentTick - lastAttackTime > 100) {
                    attackCount = 0;
                }
                attackCount++;
                if (attackCount > 10) {
                    attackCount = 10;
                }
                DamageSource source = new DamageSource(player.damageSources().genericKill().typeHolder(), player);
                float attackDamageModifier = player.getAttackStrengthScale(0.5f)
                        * (float) player.getAttributeValue(Attributes.ATTACK_DAMAGE);
                float damage = attackDamageModifier * attackCount;
                livingEntity.hurt(source, damage);
            }
        }

        if (entity instanceof LivingEntity living) {
            if (player.hasLineOfSight(entity)) {
                if (isFriendly(player, living)) {
                    return;
                }
                if (!living.isAlive()) {
                    return;
                }

                int currentTick = (int) player.level().getGameTime();
                if (currentTick - lastAttackTime > 100) {
                    attackCount = 0;
                }
                attackCount++;
                if (attackCount > 10) {
                    attackCount = 10;
                }
                lastAttackTime = currentTick;
                DamageSource source = new DamageSource(player.damageSources().genericKill().typeHolder(), player);
                float attackDamageModifier = player.getAttackStrengthScale(0.5f)
                        * (float) player.getAttributeValue(Attributes.ATTACK_DAMAGE);
                float damage = attackDamageModifier * attackCount;
                float originalHealth = living.getHealth();
                living.hurt(source, damage);
                if (living.getHealth() == originalHealth && !living.isInvulnerable()) {
                    if (damage < living.getHealth()) {
                        long nextTick = player.level().getGameTime();
                        if (nextTick - lastHealthSetTick >= 10) {
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
        return false;
    }

    private void applyChainDamage(Player player, LivingEntity originalTarget, float excessDamage, DamageSource source) {
        double searchRange = 3.0f;
        AABB searchBox = originalTarget.getBoundingBox().inflate(searchRange);
        int maxChainTargets = 10;
        List<LivingEntity> nearbyEntities = player.level().getEntitiesOfClass(LivingEntity.class, searchBox)
                .stream()
                .filter(entity -> entity != originalTarget && entity != player)
                .filter(entity -> !isFriendly(player, entity))
                .filter(LivingEntity::isAlive)
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

}
