package net.v_black_cat.goetydelight.compat.goety_revelation.item;

import net.minecraft.core.Holder;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageType;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.entity.projectile.Arrow;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.ProjectileImpactEvent;
import net.minecraftforge.event.entity.living.LivingAttackEvent;
import net.minecraftforge.event.entity.player.ArrowLooseEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@Mod.EventBusSubscriber
public class PiPieItem extends Item {

    private static final String PI_PIE_ACTIVE_TAG = "PiPieActive";
    private static final String PI_PIE_END_TIME_TAG = "PiPieEndTime";
    private static final String RAIN_ARROW_TAG = "RainArrow";
    private static final String SHOOTER_TAG = "Shooter";
    private static final String BYPASS_IMMUNITY_TAG = "BypassImmunity";
    private static final String CUSTOM_DAMAGE_TAG = "CustomDamage";

    private static final Map<UUID, List<ArrowRainTask>> pendingArrowRains = new ConcurrentHashMap<>();

    private static final Set<AbstractArrow> trackedArrows = Collections.newSetFromMap(new ConcurrentHashMap<>());

    public PiPieItem(Properties properties) {
        super(properties);
    }

    @Override
    public ItemStack finishUsingItem(ItemStack stack, Level level, LivingEntity entity) {
        if (entity instanceof Player player && !level.isClientSide) {
            CompoundTag playerData = player.getPersistentData();
            playerData.putBoolean(PI_PIE_ACTIVE_TAG, true);
            playerData.putLong(PI_PIE_END_TIME_TAG, level.getGameTime() + 12000);
        }
        return super.finishUsingItem(stack, level, entity);
    }

    public static boolean isPlayerActive(Player player) {
        CompoundTag playerData = player.getPersistentData();
        if (!playerData.contains(PI_PIE_ACTIVE_TAG) || !playerData.getBoolean(PI_PIE_ACTIVE_TAG)) {
            return false;
        }

        long endTime = playerData.getLong(PI_PIE_END_TIME_TAG);
        if (endTime < player.level().getGameTime()) {
            playerData.remove(PI_PIE_ACTIVE_TAG);
            playerData.remove(PI_PIE_END_TIME_TAG);
            return false;
        }
        return true;
    }

    public static long getRemainingTime(Player player) {
        CompoundTag playerData = player.getPersistentData();
        if (playerData.contains(PI_PIE_END_TIME_TAG)) {
            return Math.max(0, playerData.getLong(PI_PIE_END_TIME_TAG) - player.level().getGameTime());
        }
        return 0;
    }

    public static void removeAbility(Player player) {
        CompoundTag playerData = player.getPersistentData();
        playerData.remove(PI_PIE_ACTIVE_TAG);
        playerData.remove(PI_PIE_END_TIME_TAG);
        pendingArrowRains.remove(player.getUUID());
    }

    @SubscribeEvent
    public static void onPlayerLogout(PlayerEvent.PlayerLoggedOutEvent event) {
        UUID playerId = event.getEntity().getUUID();
        pendingArrowRains.remove(playerId);
    }

    @SubscribeEvent
    public static void onPlayerClone(PlayerEvent.Clone event) {
        if (event.isWasDeath()) {
            CompoundTag original = event.getOriginal().getPersistentData();
            CompoundTag clone = event.getEntity().getPersistentData();

            if (original.contains(PI_PIE_ACTIVE_TAG)) {
                clone.putBoolean(PI_PIE_ACTIVE_TAG, original.getBoolean(PI_PIE_ACTIVE_TAG));
                clone.putLong(PI_PIE_END_TIME_TAG, original.getLong(PI_PIE_END_TIME_TAG));
            }
        }
    }

    private static class ArrowRainTask {
        final int executeTick;
        final Vec3 center;
        final UUID shooterId;
        final ServerLevel level;

        ArrowRainTask(int executeTick, Vec3 center, UUID shooterId, ServerLevel level) {
            this.executeTick = executeTick;
            this.center = center;
            this.shooterId = shooterId;
            this.level = level;
        }
    }

    /**
     * 创建带有自定义标签的玩家攻击伤害源
     */
    private static DamageSource createPlayerAttackDamageSource(Player shooter) {
        DamageSource damageSource = shooter.damageSources().playerAttack(shooter);

        // 修改伤害类型标签以绕过无敌帧
        Holder<DamageType> damageTypeHolder = damageSource.typeHolder();
        if (damageTypeHolder instanceof Holder.Reference<DamageType> reference) {
            reference.bindTags(Set.of(
                    DamageTypeTags.BYPASSES_INVULNERABILITY
            ));
        }

        return damageSource;
    }

    @Mod.EventBusSubscriber
    public static class PiPieEvents {

        @SubscribeEvent
        public static void onArrowLoose(ArrowLooseEvent event) {
            Player player = event.getEntity();
            if (!player.level().isClientSide && isPlayerActive(player)) {
                int charge = event.getCharge();
                float velocity = getArrowVelocity(charge);

                for (int i = 0; i < 2; i++) {
                    if (player.isAlive()) {
                        Arrow arrow = new Arrow(player.level(), player);
                        arrow.shootFromRotation(player, player.getXRot(), player.getYRot(), 0.0F, velocity, 1.0F);
                        arrow.setCritArrow(true);
                        arrow.setOwner(player);
                        arrow.pickup = AbstractArrow.Pickup.DISALLOWED; // 禁止拾取

                        CompoundTag tag = arrow.getPersistentData();
                        tag.putBoolean(RAIN_ARROW_TAG, false);
                        tag.putUUID(SHOOTER_TAG, player.getUUID());
                        tag.putBoolean(BYPASS_IMMUNITY_TAG, true);
                        tag.putDouble(CUSTOM_DAMAGE_TAG, arrow.getBaseDamage());

                        player.level().addFreshEntity(arrow);
                        trackedArrows.add(arrow);
                    }
                }
            }
        }

        private static float getArrowVelocity(int charge) {
            float f = (float)charge / 20.0F;
            f = (f * f + f * 2.0F) / 3.0F;
            if (f > 1.0F) {
                f = 1.0F;
            }
            return f * 3.0F;
        }

        @SubscribeEvent
        public static void onProjectileImpact(ProjectileImpactEvent event) {
            if (event.getProjectile() instanceof AbstractArrow arrow) {
                CompoundTag tag = arrow.getPersistentData();
                if (tag.hasUUID(SHOOTER_TAG) && tag.getBoolean(BYPASS_IMMUNITY_TAG)) {
                    HitResult hitResult = event.getRayTraceResult();
                    Vec3 impactPos = hitResult.getLocation();
                    UUID shooterId = tag.getUUID(SHOOTER_TAG);
                    Player shooter = arrow.level().getPlayerByUUID(shooterId);

                    if (shooter == null || !shooter.isAlive()) {
                        arrow.discard();
                        trackedArrows.remove(arrow);
                        return;
                    }
                    boolean isRainArrow = tag.getBoolean(RAIN_ARROW_TAG);
                    if (hitResult instanceof EntityHitResult entityHitResult) {
                        Entity target = entityHitResult.getEntity();
                        if (target instanceof LivingEntity livingTarget) {
                            if (livingTarget instanceof Player targetPlayer &&
                                    (targetPlayer.isCreative() || targetPlayer.isSpectator())) {
                                arrow.discard();
                                trackedArrows.remove(arrow);
                                return;
                            }
                            float actualDamage = calculateArrowDamage(arrow, livingTarget);
                            event.setImpactResult(ProjectileImpactEvent.ImpactResult.STOP_AT_CURRENT_NO_DAMAGE);
                            DamageSource damageSource = createPlayerAttackDamageSource(shooter);
                            livingTarget.hurt(damageSource, actualDamage);
                            if (shooter.isAlive() && isPlayerActive(shooter)) {
                                float healAmount = isRainArrow ? actualDamage * 0.5f : actualDamage;
                                shooter.heal(healAmount);
                            }
                            if (!isRainArrow) {
                                createArrowRainTasks((ServerLevel) arrow.level(), impactPos, shooter);
                            }
                            arrow.discard();
                            trackedArrows.remove(arrow);
                        }
                    } else {
                        if (!isRainArrow) {
                            createArrowRainTasks((ServerLevel) arrow.level(), impactPos, shooter);
                        }
                        arrow.discard();
                        trackedArrows.remove(arrow);
                    }
                }
            }
        }

        private static float calculateArrowDamage(AbstractArrow arrow, LivingEntity target) {
            float baseDamage = (float) arrow.getPersistentData().getDouble(CUSTOM_DAMAGE_TAG);

            Vec3 velocity = arrow.getDeltaMovement();
            float speed = (float) velocity.length();

            float damage = baseDamage * speed;

            if (arrow.isCritArrow()) {
                damage += arrow.level().random.nextInt((int)(damage / 2 + 2));
            }

            return Math.max(1.0f, damage);
        }

        private static void createArrowRainTasks(ServerLevel level, Vec3 center, Player shooter) {
            List<ArrowRainTask> tasks = new ArrayList<>();
            int currentTick = level.getServer().getTickCount();
            UUID shooterId = shooter.getUUID();

            for (int i = 0; i < 10; i++) {
                tasks.add(new ArrowRainTask(
                        currentTick + i * 10,
                        center,
                        shooterId,
                        level
                ));
            }

            pendingArrowRains.computeIfAbsent(shooterId, k -> new ArrayList<>()).addAll(tasks);
        }

        private static void spawnArrowBatch(ServerLevel level, Vec3 center, Player shooter) {
            int arrowCount = 15 + level.random.nextInt(6);

            for (int j = 0; j < arrowCount; j++) {
                double angle = level.random.nextDouble() * 360 * Math.PI / 180;
                double radius = level.random.nextDouble() * 2;

                Vec3 spawnPos = new Vec3(
                        center.x + Math.cos(angle) * radius,
                        center.y + 25,
                        center.z + Math.sin(angle) * radius
                );

                Arrow rainArrow = new Arrow(level, spawnPos.x, spawnPos.y, spawnPos.z);
                rainArrow.setDeltaMovement(
                        (level.random.nextDouble() - 0.5) * 0.5,
                        -2.0,
                        (level.random.nextDouble() - 0.5) * 0.5
                );
                rainArrow.setOwner(shooter);
                rainArrow.pickup = AbstractArrow.Pickup.DISALLOWED; // 禁止拾取

                CompoundTag tag = rainArrow.getPersistentData();
                tag.putUUID(SHOOTER_TAG, shooter.getUUID());
                tag.putBoolean(RAIN_ARROW_TAG, true);
                tag.putBoolean(BYPASS_IMMUNITY_TAG, true);
                tag.putDouble(CUSTOM_DAMAGE_TAG, 6.0);

                rainArrow.setBaseDamage(6.0);
                level.addFreshEntity(rainArrow);
                trackedArrows.add(rainArrow);
            }
        }

        @SubscribeEvent
        public static void onLivingAttack(LivingAttackEvent event) {
            if (event.getEntity() instanceof Player targetPlayer &&
                    (targetPlayer.isCreative() || targetPlayer.isSpectator())) {
                event.setCanceled(true);
                return;
            }

            if (event.getSource().getDirectEntity() instanceof AbstractArrow arrow) {
                CompoundTag tag = arrow.getPersistentData();
                if (tag.hasUUID(SHOOTER_TAG)) {
                    Player shooter = null;
                    if (arrow.getOwner() instanceof Player p) {
                        shooter = p;
                    } else {
                        UUID shooterId = tag.getUUID(SHOOTER_TAG);
                        shooter = arrow.level().getPlayerByUUID(shooterId);
                    }

                    if (shooter != null && event.getEntity() == shooter) {
                        event.setCanceled(true);
                        return;
                    }
                }
            }
        }

        @SubscribeEvent
        public static void onServerTick(TickEvent.ServerTickEvent event) {
            if (event.phase != TickEvent.Phase.END) return;

            ServerLevel overworld = event.getServer().overworld();
            int currentTick = event.getServer().getTickCount();

            processArrowRainTasks(overworld, currentTick);
            cleanupArrows();
        }

        private static void processArrowRainTasks(ServerLevel level, int currentTick) {
            Iterator<Map.Entry<UUID, List<ArrowRainTask>>> iterator = pendingArrowRains.entrySet().iterator();

            while (iterator.hasNext()) {
                Map.Entry<UUID, List<ArrowRainTask>> entry = iterator.next();
                List<ArrowRainTask> tasks = entry.getValue();

                Iterator<ArrowRainTask> taskIterator = tasks.iterator();
                while (taskIterator.hasNext()) {
                    ArrowRainTask task = taskIterator.next();

                    if (currentTick >= task.executeTick) {
                        Player shooter = task.level.getPlayerByUUID(task.shooterId);
                        if (shooter != null && shooter.isAlive() && isPlayerActive(shooter)) {
                            spawnArrowBatch(task.level, task.center, shooter);
                        }
                        taskIterator.remove();
                    }
                }

                if (tasks.isEmpty()) {
                    iterator.remove();
                }
            }
        }

        private static void cleanupArrows() {
            Iterator<AbstractArrow> iterator = trackedArrows.iterator();

            while (iterator.hasNext()) {
                AbstractArrow arrow = iterator.next();

                if (arrow.isRemoved()) {
                    iterator.remove();
                    continue;
                }
                boolean shouldRemove = arrow.inGround;
                if (!shouldRemove && arrow.getPersistentData().hasUUID(SHOOTER_TAG)) {
                    if (arrow.getPersistentData().getBoolean(RAIN_ARROW_TAG)) {
                        if (arrow.tickCount > 100) {
                            shouldRemove = true;
                        }
                    } else {
                        if (arrow.tickCount > 100) {
                            shouldRemove = true;
                        }
                    }
                }

                if (shouldRemove) {
                    arrow.discard();
                    iterator.remove();
                }
            }
        }
    }
}