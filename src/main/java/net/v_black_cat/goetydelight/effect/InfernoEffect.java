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
import net.minecraftforge.event.entity.living.LivingAttackEvent;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber
public class InfernoEffect extends MobEffect {

    private static final int BOILING_RADIUS = 16;
    private static final int BOILING_DAMAGE = 2;
    private static final int BOILING_DAMAGE_INTERVAL = 20;
    private static final int CLEANUP_INTERVAL = 100;

    public InfernoEffect() {
        super(MobEffectCategory.BENEFICIAL, 1203053);
    }

    @Override
    public void applyEffectTick(LivingEntity entity, int amplifier) {
        if (entity.level().isClientSide) return;

        // 清除火焰状态
        if (entity.isOnFire()) {
            entity.clearFire();
            entity.setRemainingFireTicks(0);
        }

        // 水沸腾效果（每20tick触发一次）
        if (entity.level().getGameTime() % BOILING_DAMAGE_INTERVAL == 0) {
            if (entity instanceof Player player && player.level() instanceof ServerLevel level) {
                var pos = player.blockPosition();
                AABB area = new AABB(pos).inflate(BOILING_RADIUS);

                for (LivingEntity e : level.getEntitiesOfClass(LivingEntity.class, area,
                        e -> e != player && (e.isInWater() || e.isInFluidType()))) {
                    e.hurt(e.damageSources().generic(), BOILING_DAMAGE);
                }
            }
        }
    }

    @Override
    public boolean isDurationEffectTick(int duration, int amplifier) {
        // 每tick都执行
        return duration > 0;
    }

    @SubscribeEvent
    public static void onLivingAttack(LivingAttackEvent event) {
        if (!hasInfernoEffect(event.getEntity())) return;
        if (!isFireDamage(event.getSource())) return;

        LivingEntity entity = event.getEntity();
        float healAmount = event.getAmount();
        event.setCanceled(true);
        entity.clearFire();
        entity.setRemainingFireTicks(0);
        if (healAmount > 0) {
            entity.heal(healAmount);
        }
    }

    @SubscribeEvent
    public static void onLivingHurt(LivingHurtEvent event) {
        Entity source = event.getSource().getEntity();
        LivingEntity target = event.getEntity();

        boolean attackerHas = source instanceof LivingEntity att && hasInfernoEffect(att);
        boolean targetHas = hasInfernoEffect(target);

        if (!attackerHas && !targetHas) return;

        boolean targetFireImmune = target.fireImmune();

        if (attackerHas) {
            event.setAmount(event.getAmount() * (targetFireImmune ? 0.8f : 1.2f));
        }

        if (targetHas) {
            if (target.isOnFire()) {
                event.setAmount(event.getAmount() * 1.2f);
            } else if (targetFireImmune) {
                event.setAmount(event.getAmount() * 0.8f);
            }
        }
    }

    private static boolean hasInfernoEffect(LivingEntity entity) {
        return entity != null && entity.hasEffect(ModEffects.INFERNO.get());
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