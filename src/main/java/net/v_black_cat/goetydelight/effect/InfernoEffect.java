package net.v_black_cat.goetydelight.effect;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.AABB;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.LivingDamageEvent;
import net.neoforged.neoforge.event.entity.living.LivingIncomingDamageEvent;
import net.v_black_cat.goetydelight.GoetyDelight;
import net.v_black_cat.goetydelight.init.ModEffects;

/**
 * 地狱火效果：免疫火焰伤害（改为回复），并让周围水/岩浆中的生物持续受沸煮伤害；
 * 攻击方/受击方持有该效果时按火焰状态调整伤害倍率。
 */
@EventBusSubscriber(modid = GoetyDelight.MODID)
public class InfernoEffect extends MobEffect {

    private static final int BOILING_RADIUS = 16;
    private static final int BOILING_DAMAGE = 2;
    private static final int BOILING_DAMAGE_INTERVAL = 20;

    public InfernoEffect() {
        super(MobEffectCategory.BENEFICIAL, 1203053);
    }

    @Override
    public boolean applyEffectTick(LivingEntity entity, int amplifier) {
        if (entity.level().isClientSide) return true;

        // 清除火焰状态
        if (entity.isOnFire()) {
            entity.clearFire();
            entity.setRemainingFireTicks(0);
        }

        // 水/岩浆沸腾效果（每20tick触发一次）
        if (entity.level().getGameTime() % BOILING_DAMAGE_INTERVAL == 0) {
            if (entity instanceof Player player && player.level() instanceof ServerLevel level) {
                var pos = player.blockPosition();
                AABB area = new AABB(pos).inflate(BOILING_RADIUS);

                for (LivingEntity e : level.getEntitiesOfClass(LivingEntity.class, area,
                        e -> e != player && (e.isInWater() || e.isInLava()))) {
                    e.hurt(e.damageSources().generic(), BOILING_DAMAGE);
                }
            }
        }
        return true;
    }

    @Override
    public boolean shouldApplyEffectTickThisTick(int duration, int amplifier) {
        // 每tick都执行
        return duration > 0;
    }

    // 旧 LivingAttackEvent（Forge 1.20.1）→ LivingIncomingDamageEvent（伤害结算前，可取消）
    @SubscribeEvent
    public static void onLivingAttack(LivingIncomingDamageEvent event) {
        LivingEntity entity = event.getEntity();
        if (!hasInfernoEffect(entity)) return;
        if (!isFireDamage(event.getSource())) return;

        float healAmount = event.getAmount();
        event.setCanceled(true);
        entity.clearFire();
        entity.setRemainingFireTicks(0);
        if (healAmount > 0) {
            entity.heal(healAmount);
        }
    }

    // 旧 LivingHurtEvent（Forge 1.20.1）→ LivingDamageEvent.Pre（护甲结算后）
    @SubscribeEvent
    public static void onLivingHurt(LivingDamageEvent.Pre event) {
        Entity source = event.getSource().getEntity();
        LivingEntity target = event.getEntity();

        boolean attackerHas = source instanceof LivingEntity att && hasInfernoEffect(att);
        boolean targetHas = hasInfernoEffect(target);

        if (!attackerHas && !targetHas) return;

        boolean targetFireImmune = target.fireImmune();

        float newAmount = event.getOriginalDamage();
        if (attackerHas) {
            newAmount *= targetFireImmune ? 0.8f : 1.2f;
        }

        if (targetHas) {
            if (target.isOnFire()) {
                newAmount *= 1.2f;
            } else if (targetFireImmune) {
                newAmount *= 0.8f;
            }
        }

        event.setNewDamage(newAmount);
    }

    private static boolean hasInfernoEffect(LivingEntity entity) {
        return entity != null && entity.hasEffect(ModEffects.INFERNO);
    }

    private static boolean isFireDamage(DamageSource source) {
        return source.is(DamageTypes.ON_FIRE)
                || source.is(DamageTypes.IN_FIRE)
                || source.is(DamageTypes.LAVA)
                || source.is(DamageTypes.HOT_FLOOR)
                || source.is(DamageTypes.FIREBALL)
                || source.is(DamageTypes.UNATTRIBUTED_FIREBALL);
    }
}
