package net.v_black_cat.goetydelight.item.food;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.living.LivingAttackEvent;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.*;

@Mod.EventBusSubscriber
public class UnholySauceItem extends GlassBottleFoodItem {

    private static final String TAG_UNHOLY_REMAINING_TICKS = "UnholySauceRemainingTicks";
    private static final int DURATION_TICKS = 20 * 60 * 5;
    private static final int BOILING_RADIUS = 16;
    private static final int BOILING_DAMAGE = 2;
    private static final int BOILING_DAMAGE_INTERVAL = 20;

    // 缓存：所有拥有该效果的实体UUID
    private static final Set<UUID> activeEntities = new HashSet<>();
    // 非玩家实体的剩余tick
    private static final Map<UUID, Integer> entityRemainingTicks = new HashMap<>();

    public UnholySauceItem(Properties properties) {
        super(properties);
    }

    @Override
    public ItemStack finishUsingItem(ItemStack stack, Level level, LivingEntity entity) {
        if (!level.isClientSide) {
            applyUnholyEffect(entity);
        }
        return super.finishUsingItem(stack, level, entity);
    }

    private void applyUnholyEffect(LivingEntity entity) {
        UUID uuid = entity.getUUID();
        activeEntities.add(uuid);

        if (entity instanceof Player player) {
            player.getPersistentData().putInt(TAG_UNHOLY_REMAINING_TICKS, DURATION_TICKS);
        } else {
            entityRemainingTicks.put(uuid, DURATION_TICKS);
        }
    }

    /**
     * 高性能检查 - 纯内存查找，不读NBT
     */
    public static boolean hasUnholyEffect(LivingEntity entity) {
        return entity != null && activeEntities.contains(entity.getUUID());
    }

    private static int getRemainingTicks(LivingEntity entity) {
        if (entity instanceof Player player) {
            return player.getPersistentData().getInt(TAG_UNHOLY_REMAINING_TICKS);
        }
        return entityRemainingTicks.getOrDefault(entity.getUUID(), 0);
    }

    private static void removeEffect(LivingEntity entity) {
        UUID uuid = entity.getUUID();
        activeEntities.remove(uuid);
        entityRemainingTicks.remove(uuid);
        if (entity instanceof Player player) {
            player.getPersistentData().remove(TAG_UNHOLY_REMAINING_TICKS);
        }
    }

    // ==================== 事件处理 ====================

    @SubscribeEvent
    public static void onLivingAttack(LivingAttackEvent event) {
        if (!hasUnholyEffect(event.getEntity())) return;
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
        // 快速检查：攻击者和目标都没有效果就直接返回
        Entity source = event.getSource().getEntity();
        LivingEntity target = event.getEntity();
        boolean attackerHas = source instanceof LivingEntity att && hasUnholyEffect(att);
        boolean targetHas = hasUnholyEffect(target);
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

    @SubscribeEvent
    public static void onServerTick(TickEvent.ServerTickEvent event) {
        if (event.phase != TickEvent.Phase.END) return;
        if (activeEntities.isEmpty()) return;

        long gameTime = event.getServer().overworld().getGameTime();
        boolean shouldDamage = gameTime % BOILING_DAMAGE_INTERVAL == 0;
        boolean doCleanup = gameTime % 100 == 0;

        Iterator<UUID> it = activeEntities.iterator();
        while (it.hasNext()) {
            UUID uuid = it.next();
            LivingEntity entity = findEntity(event.getServer().getAllLevels(), uuid);

            // 实体不存在或已死亡
            if (entity == null || !entity.isAlive()) {
                it.remove();
                entityRemainingTicks.remove(uuid);
                continue;
            }

            // 减少tick
            int remaining = getRemainingTicks(entity) - 1;
            if (remaining <= 0) {
                removeEffect(entity);
                continue;
            }

            // 更新剩余时间
            if (entity instanceof Player player) {
                player.getPersistentData().putInt(TAG_UNHOLY_REMAINING_TICKS, remaining);
            } else {
                entityRemainingTicks.put(uuid, remaining);
            }

            // 清除火焰状态
            entity.clearFire();
            entity.setRemainingFireTicks(0);

            // 水沸腾（仅玩家触发）
            if (shouldDamage && entity instanceof Player player && player.level() instanceof ServerLevel level) {
                BlockPos pos = player.blockPosition();
                AABB area = new AABB(pos).inflate(BOILING_RADIUS);

                for (LivingEntity e : level.getEntitiesOfClass(LivingEntity.class, area,
                        e -> e != player && (e.isInWater() || e.isInFluidType()))) {
                    e.hurt(e.damageSources().generic(), BOILING_DAMAGE);
                }
            }
        }
    }

    private static LivingEntity findEntity(Iterable<ServerLevel> levels, UUID uuid) {
        for (ServerLevel level : levels) {
            Entity e = level.getEntity(uuid);
            if (e instanceof LivingEntity le) return le;
        }
        return null;
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