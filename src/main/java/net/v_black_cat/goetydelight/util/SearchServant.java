package net.v_black_cat.goetydelight.util;

import com.Polarice3.Goety.api.entities.IOwned;
import net.minecraft.core.Holder;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.OwnableEntity;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.event.entity.EntityJoinLevelEvent;
import net.neoforged.neoforge.event.entity.living.LivingDeathEvent;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.tick.ServerTickEvent;
import net.v_black_cat.goetydelight.GoetyDelight;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class SearchServant {
    // 缓存
    private static final Map<UUID, ServantData> PLAYER_SERVANT_CACHE = new ConcurrentHashMap<>();
    private static final Map<UUID, EnhancedServantData> ENHANCED_PLAYER_CACHE = new ConcurrentHashMap<>();

    // 扫描配置常量
    private static final int SCAN_INTERVAL_TICKS = 7200; // 6分钟（20 ticks/秒 * 60秒 * 6）
    private static final int MAX_ENTITIES_PER_FRAME = 500;
    private static final double PLAYER_SCAN_RANGE = 100.0;
    private static final double WORLD_SCAN_RANGE = 30000000.0;

    private static int globalTickCounter = 0;

    // 扫描状态管理
    private static volatile boolean isScanning = false;
    private static Iterator<ServerLevel> levelIterator = null;
    private static Iterator<LivingEntity> entityIterator = null;
    private static ServerLevel currentLevel = null;
    private static final Map<UUID, Set<UUID>> scanningPlayerServants = new ConcurrentHashMap<>();


    // ==================== 数据类定义 ====================

    public static class ServantData {
        public final UUID playerUUID;
        public final Set<UUID> servantUUIDs;
        public int maxServants;
        public int lastScanTick;

        public ServantData(UUID playerUUID) {
            this.playerUUID = playerUUID;
            this.servantUUIDs = ConcurrentHashMap.newKeySet();
            this.maxServants = 512;
            this.lastScanTick = 0;
        }

        public void addServant(UUID servantUUID) {
            servantUUIDs.add(servantUUID);
        }

        public void removeServant(UUID servantUUID) {
            servantUUIDs.remove(servantUUID);
        }

        public boolean hasServant(UUID servantUUID) {
            return servantUUIDs.contains(servantUUID);
        }

        public int getServantCount() {
            return servantUUIDs.size();
        }

        public void clear() {
            servantUUIDs.clear();
        }
    }

    public static class ServantDetailData {
        public final UUID servantUUID;
        public String entityType;
        public EquipmentData equipment;
        public AttributeData attributes;
        public long lastUpdateTime;

        public ServantDetailData(UUID servantUUID) {
            this.servantUUID = servantUUID;
            this.equipment = new EquipmentData();
            this.attributes = new AttributeData();
            this.lastUpdateTime = System.currentTimeMillis();
        }

        public void updateFromEntity(LivingEntity entity) {
            if (entity == null || entity.isRemoved()) {
                return;
            }

            try {
                this.entityType = entity.getType().getDescriptionId();
                this.equipment.updateFromEntity(entity);
                this.attributes.updateFromEntity(entity);
                this.lastUpdateTime = System.currentTimeMillis();
            } catch (Exception e) {
                GoetyDelight.LOGGER.error("Failed to update servant data for UUID: {}", servantUUID, e);
            }
        }

        public static class EquipmentData {
            public ItemStack helmet = ItemStack.EMPTY;
            public ItemStack chestplate = ItemStack.EMPTY;
            public ItemStack leggings = ItemStack.EMPTY;
            public ItemStack boots = ItemStack.EMPTY;
            public ItemStack mainHand = ItemStack.EMPTY;
            public ItemStack offHand = ItemStack.EMPTY;

            public void updateFromEntity(LivingEntity entity) {
                if (entity == null) return;

                this.helmet = entity.getItemBySlot(EquipmentSlot.HEAD).copy();
                this.chestplate = entity.getItemBySlot(EquipmentSlot.CHEST).copy();
                this.leggings = entity.getItemBySlot(EquipmentSlot.LEGS).copy();
                this.boots = entity.getItemBySlot(EquipmentSlot.FEET).copy();
                this.mainHand = entity.getMainHandItem().copy();
                this.offHand = entity.getOffhandItem().copy();
            }

            public Map<String, Object> toMap() {
                Map<String, Object> map = new LinkedHashMap<>();
                map.put("helmet", serializeItemStack(helmet));
                map.put("chestplate", serializeItemStack(chestplate));
                map.put("leggings", serializeItemStack(leggings));
                map.put("boots", serializeItemStack(boots));
                map.put("mainHand", serializeItemStack(mainHand));
                map.put("offHand", serializeItemStack(offHand));
                return map;
            }

            private Map<String, Object> serializeItemStack(ItemStack stack) {
                if (stack.isEmpty()) {
                    return Map.of();
                }
                Map<String, Object> itemData = new LinkedHashMap<>();
                itemData.put("item", stack.getItem().toString());
                itemData.put("count", stack.getCount());
                itemData.put("damage", stack.getDamageValue());
                itemData.put("maxDamage", stack.getMaxDamage());
                if (!stack.getComponentsPatch().isEmpty()) {
                    itemData.put("customData", stack.getComponentsPatch().toString());
                }
                itemData.put("hasCustomComponents", !stack.getComponentsPatch().isEmpty());

                return itemData;
            }
        }

        public static class AttributeData {
            public double maxHealth;
            public double currentHealth;
            public double attackDamage;
            public double armor;
            public double movementSpeed;
            public double knockbackResistance;

            public void updateFromEntity(LivingEntity entity) {
                if (entity == null) return;

                this.currentHealth = entity.getHealth();
                this.maxHealth = getMaxAttributeValue(entity, Attributes.MAX_HEALTH);
                this.attackDamage = getBaseAttributeValue(entity, Attributes.ATTACK_DAMAGE);
                this.armor = getBaseAttributeValue(entity, Attributes.ARMOR);
                this.movementSpeed = getBaseAttributeValue(entity, Attributes.MOVEMENT_SPEED);
                this.knockbackResistance = getBaseAttributeValue(entity, Attributes.KNOCKBACK_RESISTANCE);
            }

            private double getBaseAttributeValue(LivingEntity entity, Holder<Attribute> attribute) {
                AttributeInstance instance = entity.getAttribute(attribute);
                return instance != null ? instance.getBaseValue() : 0.0;
            }

            private double getMaxAttributeValue(LivingEntity entity, Holder<Attribute> attribute) {
                AttributeInstance instance = entity.getAttribute(attribute);
                return instance != null ? instance.getValue() : 0.0;
            }

            public Map<String, Double> toMap() {
                Map<String, Double> map = new LinkedHashMap<>();
                map.put("maxHealth", maxHealth);
                map.put("currentHealth", currentHealth);
                map.put("attackDamage", attackDamage);
                map.put("armor", armor);
                map.put("movementSpeed", movementSpeed);
                map.put("knockbackResistance", knockbackResistance);
                return map;
            }
        }

        public Map<String, Object> toMap() {
            Map<String, Object> map = new LinkedHashMap<>();
            map.put("servantUUID", servantUUID.toString());
            map.put("entityType", entityType);
            map.put("equipment", equipment.toMap());
            map.put("attributes", attributes.toMap());
            map.put("lastUpdateTime", lastUpdateTime);
            return map;
        }
    }

    public static class EnhancedServantData {
        public final UUID playerUUID;
        public final Map<UUID, ServantDetailData> servantDetails;
        public int maxServants;
        public int lastScanTick;

        public EnhancedServantData(UUID playerUUID) {
            this.playerUUID = playerUUID;
            this.servantDetails = new ConcurrentHashMap<>();
            this.maxServants = 100;
            this.lastScanTick = 0;
        }

        public void addOrUpdateServant(LivingEntity entity) {
            if (entity == null) return;

            ServantDetailData detail = servantDetails.computeIfAbsent(
                    entity.getUUID(),
                    uuid -> new ServantDetailData(uuid)
            );
            detail.updateFromEntity(entity);
        }

        public void removeServant(UUID servantUUID) {
            servantDetails.remove(servantUUID);
        }

        public Optional<ServantDetailData> getServantDetail(UUID servantUUID) {
            return Optional.ofNullable(servantDetails.get(servantUUID));
        }

        public Collection<ServantDetailData> getAllServantDetails() {
            return Collections.unmodifiableCollection(servantDetails.values());
        }

        public int getServantCount() {
            return servantDetails.size();
        }

        public void clear() {
            servantDetails.clear();
        }

        public Map<String, Object> toMap() {
            Map<String, Object> map = new LinkedHashMap<>();
            map.put("playerUUID", playerUUID.toString());
            map.put("maxServants", maxServants);
            map.put("lastScanTick", lastScanTick);

            List<Map<String, Object>> servantsList = new ArrayList<>();
            for (ServantDetailData detail : servantDetails.values()) {
                servantsList.add(detail.toMap());
            }
            map.put("servants", servantsList);

            return map;
        }
    }


    // ==================== 公共API方法 ====================

    public static Optional<EnhancedServantData> getEnhancedServantData(Player player) {
        if (player == null) return Optional.empty();
        return Optional.ofNullable(ENHANCED_PLAYER_CACHE.get(player.getUUID()));
    }

    public static Optional<ServantData> getServantData(Player player) {
        if (player == null) return Optional.empty();
        return Optional.ofNullable(PLAYER_SERVANT_CACHE.get(player.getUUID()));
    }

    public static void onServantJoin(Player player, LivingEntity servant) {
        if (player == null || servant == null) return;

        getServantData(player).ifPresent(data -> data.addServant(servant.getUUID()));
        getEnhancedServantData(player).ifPresent(data -> data.addOrUpdateServant(servant));
    }

    public static void onServantDeath(Player player, UUID servantUUID) {
        if (player == null || servantUUID == null) return;

        getServantData(player).ifPresent(data -> data.removeServant(servantUUID));
        getEnhancedServantData(player).ifPresent(data -> data.removeServant(servantUUID));
    }

    public static void updateServantData(LivingEntity servant) {
        if (servant instanceof IOwned owned) {
            if (owned.getTrueOwner() instanceof Player ownerPlayer) {
                getEnhancedServantData(ownerPlayer).ifPresent(data -> data.addOrUpdateServant(servant));
            }
        }
    }


    // ==================== 扫描方法 ====================

    /**
     * 为特定玩家扫描其仆人
     */
    public static void scanServantsForPlayer(MinecraftServer server, ServerPlayer player) {
        if (server == null || player == null || !player.isAlive()) {
            return;
        }

        ServantData data = PLAYER_SERVANT_CACHE.computeIfAbsent(player.getUUID(), ServantData::new);
        EnhancedServantData enhancedData = ENHANCED_PLAYER_CACHE.computeIfAbsent(player.getUUID(), EnhancedServantData::new);

        Set<UUID> currentServants = new HashSet<>();

        for (ServerLevel level : server.getAllLevels()) {
            try {
                List<LivingEntity> entities = level.getEntitiesOfClass(
                        LivingEntity.class,
                        AABB.ofSize(player.position(), PLAYER_SCAN_RANGE, PLAYER_SCAN_RANGE, PLAYER_SCAN_RANGE),
                        entity -> entity != null && entity.isAlive()
                );

                for (LivingEntity livingEntity : entities) {
                    UUID ownerUUID = getOwnerUUID(livingEntity);
                    if (ownerUUID != null && ownerUUID.equals(player.getUUID())) {
                        currentServants.add(livingEntity.getUUID());
                        updateServantCache(data, enhancedData, livingEntity);
                    }
                }
            } catch (Exception e) {
                GoetyDelight.LOGGER.warn("Error scanning level {} for player {}",
                        level.dimension().location(), player.getName().getString(), e);
            }
        }

        removeStaleServants(data, enhancedData, currentServants);
        data.lastScanTick = globalTickCounter;
        enhancedData.lastScanTick = globalTickCounter;
    }

    /**
     * 扫描所有在线玩家的仆人（一次性）
     */
    public static void scanAllPlayers(MinecraftServer server) {
        if (server == null) return;

        for (ServerLevel level : server.getAllLevels()) {
            for (ServerPlayer player : level.players()) {
                scanServantsForPlayer(server, player);
            }
        }
    }

    /**
     * 分帧优化的全服扫描
     */
    public static void scanAllPlayersOptimized(MinecraftServer server) {
        if (server == null || isScanning) {
            return;
        }

        isScanning = true;
        levelIterator = server.getAllLevels().iterator();
        scanningPlayerServants.clear();
        processNextFrame();
    }

    /**
     * 处理下一帧的扫描任务
     */
    private static void processNextFrame() {
        if (levelIterator == null || !levelIterator.hasNext()) {
            finishScanning();
            return;
        }

        try {
            if (currentLevel == null) {
                currentLevel = levelIterator.next();
                if (currentLevel == null) {
                    processNextFrame();
                    return;
                }

                List<LivingEntity> entities = currentLevel.getEntitiesOfClass(
                        LivingEntity.class,
                        AABB.ofSize(Vec3.ZERO, WORLD_SCAN_RANGE, WORLD_SCAN_RANGE, WORLD_SCAN_RANGE),
                        entity -> entity != null && entity.isAlive()
                );
                entityIterator = entities.iterator();
            }

            int processedCount = 0;

            while (entityIterator != null && entityIterator.hasNext() && processedCount < MAX_ENTITIES_PER_FRAME) {
                LivingEntity livingEntity = entityIterator.next();
                processedCount++;

                UUID ownerUUID = getOwnerUUID(livingEntity);
                if (ownerUUID != null) {
                    scanningPlayerServants
                            .computeIfAbsent(ownerUUID, k -> ConcurrentHashMap.newKeySet())
                            .add(livingEntity.getUUID());

                    ServantData data = PLAYER_SERVANT_CACHE.computeIfAbsent(ownerUUID, ServantData::new);
                    EnhancedServantData enhancedData = ENHANCED_PLAYER_CACHE.computeIfAbsent(ownerUUID, EnhancedServantData::new);

                    updateServantCache(data, enhancedData, livingEntity);
                }
            }

            if (entityIterator == null || !entityIterator.hasNext()) {
                currentLevel = null;
                entityIterator = null;
            }
        } catch (Exception e) {
            GoetyDelight.LOGGER.error("Error during optimized scan frame processing", e);
            // 出错时安全退出扫描
            isScanning = false;
            levelIterator = null;
            currentLevel = null;
            entityIterator = null;
            scanningPlayerServants.clear();
        }
    }

    /**
     * 完成扫描，清理过期数据
     */
    private static void finishScanning() {
        for (Map.Entry<UUID, Set<UUID>> entry : scanningPlayerServants.entrySet()) {
            UUID playerUUID = entry.getKey();
            Set<UUID> currentServants = entry.getValue();

            ServantData data = PLAYER_SERVANT_CACHE.get(playerUUID);
            EnhancedServantData enhancedData = ENHANCED_PLAYER_CACHE.get(playerUUID);

            if (data != null && enhancedData != null) {
                removeStaleServants(data, enhancedData, currentServants);
                data.lastScanTick = globalTickCounter;
                enhancedData.lastScanTick = globalTickCounter;
            }
        }

        isScanning = false;
        levelIterator = null;
        currentLevel = null;
        entityIterator = null;
        scanningPlayerServants.clear();
    }


    // ==================== 辅助方法 ====================

    /**
     * 获取实体的主人UUID
     */
    private static UUID getOwnerUUID(LivingEntity entity) {
        if (entity instanceof OwnableEntity ownableEntity) {
            return ownableEntity.getOwnerUUID();
        } else if (entity instanceof IOwned iOwned) {
            return iOwned.getTrueOwner() != null ? iOwned.getTrueOwner().getUUID() : null;
        }
        return null;
    }

    /**
     * 更新仆人缓存
     */
    private static void updateServantCache(ServantData data, EnhancedServantData enhancedData, LivingEntity entity) {
        if (!data.hasServant(entity.getUUID())) {
            data.addServant(entity.getUUID());
        }
        enhancedData.addOrUpdateServant(entity);
    }

    /**
     * 移除已不存在的仆人
     */
    private static void removeStaleServants(ServantData data, EnhancedServantData enhancedData, Set<UUID> currentServants) {
        Set<UUID> removedServants = new HashSet<>(data.servantUUIDs);
        removedServants.removeAll(currentServants);

        for (UUID removedUUID : removedServants) {
            data.removeServant(removedUUID);
            enhancedData.removeServant(removedUUID);
        }
    }


    // ==================== 事件处理 ====================

    @EventBusSubscriber(modid = GoetyDelight.MODID)
    public static class EventHandler {

        @SubscribeEvent
        public static void onServerTick(ServerTickEvent.Post event) {
            globalTickCounter++;

            if (isScanning) {
                processNextFrame();
            } else if (globalTickCounter % SCAN_INTERVAL_TICKS == 0) {
                scanAllPlayersOptimized(event.getServer());
            }
        }

        @SubscribeEvent
        public static void onPlayerLoggedIn(net.neoforged.neoforge.event.entity.player.PlayerEvent.PlayerLoggedInEvent event) {
            if (!event.getEntity().level().isClientSide() && event.getEntity() instanceof ServerPlayer player) {
                PLAYER_SERVANT_CACHE.computeIfAbsent(player.getUUID(), ServantData::new);
                ENHANCED_PLAYER_CACHE.computeIfAbsent(player.getUUID(), EnhancedServantData::new);
                scanAllPlayersOptimized(player.getServer());
            }
        }

        @SubscribeEvent
        public static void onPlayerLoggedOut(net.neoforged.neoforge.event.entity.player.PlayerEvent.PlayerLoggedOutEvent event) {
            UUID playerUUID = event.getEntity().getUUID();
            PLAYER_SERVANT_CACHE.remove(playerUUID);
            ENHANCED_PLAYER_CACHE.remove(playerUUID);
        }

        @SubscribeEvent
        public static void onEntityJoinLevel(EntityJoinLevelEvent event) {
            if (event.getLevel().isClientSide() || !(event.getEntity() instanceof LivingEntity livingEntity)) {
                return;
            }

            if (livingEntity instanceof IOwned owned) {
                if (owned.getTrueOwner() instanceof Player ownerPlayer) {
                    onServantJoin(ownerPlayer, livingEntity);
                }
            }
        }

        @SubscribeEvent
        public static void onLivingDeath(LivingDeathEvent event) {
            if (event.getEntity().level().isClientSide()) return;

            LivingEntity deadEntity = event.getEntity();

            if (deadEntity instanceof IOwned owned) {
                if (owned.getTrueOwner() instanceof Player ownerPlayer) {
                    onServantDeath(ownerPlayer, deadEntity.getUUID());
                }
            }
        }
    }
}