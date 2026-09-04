package net.v_black_cat.goetydelight.compat.goety_revelation.item;

import com.mega.revelationfix.Revelationfix;
import com.mega.revelationfix.common.entity.boss.ApostleServant;
import com.mega.revelationfix.common.init.ModEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.living.LivingDropsEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.registries.ForgeRegistries;

import java.lang.ref.WeakReference;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class ApocalyptiumCodItem extends Item {

    private static final String IS_APOLLYON_TAG = "isApollyon";
    private static final String PREVENT_DROPS_TAG = "PreventDrops";

    private static final int SERVANT_LIFETIME = 30 * 60 * 20;
    private static final int APOLLYON_DURATION = 5 * 60 * 20;

    private static final Map<UUID, Long> SERVANT_EXPIRY_MAP = new ConcurrentHashMap<>();
    private static final Map<UUID, Long> APOLLYON_EXPIRY_MAP = new ConcurrentHashMap<>();
    private static final List<UUID> PREVENT_DROPS_LIST = Collections.synchronizedList(new ArrayList<>());

    private static final Set<WeakReference<LivingEntity>> TRACKED_ENTITIES = ConcurrentHashMap.newKeySet();

    private static boolean isTrackingActive = false;

    public ApocalyptiumCodItem(Properties properties) {
        super(properties);
        MinecraftForge.EVENT_BUS.register(this);
    }

    @Override
    public ItemStack finishUsingItem(ItemStack stack, Level level, LivingEntity entity) {
        if (!level.isClientSide && entity instanceof Player player) {
            summonApostleServant(player);
            if (!player.getAbilities().instabuild) {
                stack.shrink(1);
            }
        }
        return super.finishUsingItem(stack, level, entity);
    }

    @Override
    public InteractionResult interactLivingEntity(ItemStack stack, Player player, LivingEntity target, InteractionHand hand) {
        Level level = player.level();

        if (!level.isClientSide && target != null) {
            ResourceLocation targetType = ForgeRegistries.ENTITY_TYPES.getKey(target.getType());

            if (targetType != null) {
                String entityId = targetType.toString();

                if (entityId.equals("goety:apostle") || entityId.equals("revelationfix:apostle_servant")) {
                    convertToApollyon(target);
                    if (!player.getAbilities().instabuild) {
                        stack.shrink(1);
                    }
                    return InteractionResult.SUCCESS;
                }
            }
        }

        return super.interactLivingEntity(stack, player, target, hand);
    }

    private void summonApostleServant(Player player) {
        ServerLevel serverLevel = (ServerLevel) player.level();
        BlockPos spawnPos = player.blockPosition().offset(1, 0, 1);

        ApostleServant servant = new ApostleServant(ModEntities.APOSTLE_SERVANT.get(), serverLevel);
        servant.setPos(spawnPos.getX() + 0.5, spawnPos.getY(), spawnPos.getZ() + 0.5);
        servant.setTrueOwner(player);

        UUID servantUUID = servant.getUUID();

        long expiryTime = serverLevel.getGameTime() + SERVANT_LIFETIME;
        SERVANT_EXPIRY_MAP.put(servantUUID, expiryTime);

        if (!PREVENT_DROPS_LIST.contains(servantUUID)) {
            PREVENT_DROPS_LIST.add(servantUUID);
        }

        servant.getPersistentData().putBoolean(PREVENT_DROPS_TAG, true);

        serverLevel.addFreshEntity(servant);
        TRACKED_ENTITIES.add(new WeakReference<>(servant));
        activateTracking();

        player.sendSystemMessage(Component.literal("§6使徒仆从已召唤，将持续30分钟"));
    }

    private void convertToApollyon(LivingEntity target) {
        UUID entityUUID = target.getUUID();
        long expiryTime = target.level().getGameTime() + APOLLYON_DURATION;

        APOLLYON_EXPIRY_MAP.put(entityUUID, expiryTime);

        CompoundTag entityNbt = target.saveWithoutId(new CompoundTag());
        entityNbt.putByte(IS_APOLLYON_TAG, (byte) 1);
        target.load(entityNbt);

        if (PREVENT_DROPS_LIST.contains(entityUUID)) {
            target.getPersistentData().putBoolean(PREVENT_DROPS_TAG, true);
        }

        TRACKED_ENTITIES.add(new WeakReference<>(target));
        activateTracking();

        if (target.level() instanceof ServerLevel serverLevel) {
            serverLevel.sendParticles(
                    net.minecraft.core.particles.ParticleTypes.ENCHANTED_HIT,
                    target.getX(), target.getY() + 1, target.getZ(),
                    50, 0.5, 0.5, 0.5, 0.1
            );
        }

        if (!target.level().isClientSide) {
            Player nearestPlayer = target.level().getNearestPlayer(target, 10);
            if (nearestPlayer != null) {
                nearestPlayer.sendSystemMessage(Component.literal("§c使徒已暂时转化为亚形态，将持续5分钟"));
            }
        }
    }

    private void restoreFromApollyon(LivingEntity target) {
        UUID entityUUID = target.getUUID();
        APOLLYON_EXPIRY_MAP.remove(entityUUID);
        CompoundTag entityNbt = target.saveWithoutId(new CompoundTag());
        entityNbt.putByte(IS_APOLLYON_TAG, (byte) 0);
        target.load(entityNbt);
        if (PREVENT_DROPS_LIST.contains(entityUUID)) {
            target.getPersistentData().putBoolean(PREVENT_DROPS_TAG, true);
        }
        if (!target.level().isClientSide) {
            Player nearestPlayer = target.level().getNearestPlayer(target, 10);
            if (nearestPlayer != null) {
                nearestPlayer.sendSystemMessage(Component.literal("§a使徒已恢复原形态"));
            }
        }
    }
    private void activateTracking() {
        if (!isTrackingActive) {
            isTrackingActive = true;
        }
    }

    private void deactivateTracking() {
        if (isTrackingActive) {
            isTrackingActive = false;
        }
    }

    @SubscribeEvent
    public void onLivingDrops(LivingDropsEvent event) {
        LivingEntity entity = event.getEntity();
        UUID entityUUID = entity.getUUID();

        if (PREVENT_DROPS_LIST.contains(entityUUID)) {
            event.setCanceled(true);
        }
        cleanupEntityData(entityUUID);
        cleanupDeadReferences();
    }

    /**
     * 玩家登录时检查是否需要重新激活追踪
     */
    @SubscribeEvent
    public void onPlayerLogin(PlayerEvent.PlayerLoggedInEvent event) {
        if (event.getEntity().level() instanceof ServerLevel) {
            cleanupDeadReferences();
        }
    }

    /**
     * 清理无效的实体引用
     */
    private void cleanupDeadReferences() {
        Iterator<WeakReference<LivingEntity>> iterator = TRACKED_ENTITIES.iterator();
        while (iterator.hasNext()) {
            WeakReference<LivingEntity> ref = iterator.next();
            LivingEntity entity = ref.get();
            if (entity == null || entity.isRemoved() || !entity.isAlive()) {
                UUID entityUUID = null;
                if (entity != null) {
                    entityUUID = entity.getUUID();
                }
                iterator.remove();
                if (entityUUID != null) {
                    cleanupEntityData(entityUUID);
                }
            }
        }

        if (TRACKED_ENTITIES.isEmpty() && SERVANT_EXPIRY_MAP.isEmpty() && APOLLYON_EXPIRY_MAP.isEmpty()) {
            deactivateTracking();
        }
    }

    /**
     * 清理实体的所有数据
     */
    private void cleanupEntityData(UUID entityUUID) {
        SERVANT_EXPIRY_MAP.remove(entityUUID);
        APOLLYON_EXPIRY_MAP.remove(entityUUID);
        PREVENT_DROPS_LIST.remove(entityUUID);
    }

    @SubscribeEvent
    public void onEntityTick(net.minecraftforge.event.TickEvent.LevelTickEvent event) {
        if (!isTrackingActive) {
            return;
        }

        if (event.phase != net.minecraftforge.event.TickEvent.Phase.END || event.level.isClientSide) {
            return;
        }

        ServerLevel serverLevel = (ServerLevel) event.level;
        long currentTime = serverLevel.getGameTime();

        Iterator<Map.Entry<UUID, Long>> servantIterator = SERVANT_EXPIRY_MAP.entrySet().iterator();
        while (servantIterator.hasNext()) {
            Map.Entry<UUID, Long> entry = servantIterator.next();
            if (currentTime >= entry.getValue()) {
                UUID entityUUID = entry.getKey();
                Entity entity = serverLevel.getEntity(entityUUID);
                if (entity != null) {
                    entity.remove(Entity.RemovalReason.DISCARDED);
                }
                servantIterator.remove();
                PREVENT_DROPS_LIST.remove(entityUUID);
            }
        }

        Iterator<Map.Entry<UUID, Long>> apollyonIterator = APOLLYON_EXPIRY_MAP.entrySet().iterator();
        while (apollyonIterator.hasNext()) {
            Map.Entry<UUID, Long> entry = apollyonIterator.next();
            if (currentTime >= entry.getValue()) {
                UUID entityUUID = entry.getKey();
                Entity entity = serverLevel.getEntity(entityUUID);
                if (entity instanceof LivingEntity livingEntity) {
                    restoreFromApollyon(livingEntity);
                    serverLevel.sendParticles(
                            net.minecraft.core.particles.ParticleTypes.SMOKE,
                            livingEntity.getX(), livingEntity.getY() + 1, livingEntity.getZ(),
                            30, 0.5, 0.5, 0.5, 0.05
                    );
                }
                apollyonIterator.remove();
            }
        }

        Iterator<WeakReference<LivingEntity>> iterator = TRACKED_ENTITIES.iterator();
        while (iterator.hasNext()) {
            WeakReference<LivingEntity> ref = iterator.next();
            LivingEntity livingEntity = ref.get();

            if (livingEntity == null || livingEntity.isRemoved() || !livingEntity.isAlive()) {
                UUID entityUUID = null;
                if (livingEntity != null) {
                    entityUUID = livingEntity.getUUID();
                }
                iterator.remove();
                if (entityUUID != null) {
                    cleanupEntityData(entityUUID);
                }
            }
        }

        if (TRACKED_ENTITIES.isEmpty() && SERVANT_EXPIRY_MAP.isEmpty() && APOLLYON_EXPIRY_MAP.isEmpty()) {
            deactivateTracking();
        }
    }
}