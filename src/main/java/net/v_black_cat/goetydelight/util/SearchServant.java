package net.v_black_cat.goetydelight.util;

import com.Polarice3.Goety.api.entities.IOwned;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.OwnableEntity;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.event.entity.EntityJoinLevelEvent;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.event.TickEvent;
import net.v_black_cat.goetydelight.GoetyDelight;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class SearchServant {
    private static final Map<UUID, ServantData> PLAYER_SERVANT_CACHE = new ConcurrentHashMap<>();
    private static final int SCAN_INTERVAL_TICKS = 7200;
    private static int globalTickCounter = 0;

    private static volatile boolean isScanning = false;
    private static Iterator<ServerLevel> levelIterator = null;
    private static Iterator<LivingEntity> entityIterator = null;
    private static ServerLevel currentLevel = null;
    private static Map<UUID, Set<UUID>> scanningPlayerServants = new HashMap<>();


    public static class ServantData {
        public final UUID playerUUID;
        public final Set<UUID> servantUUIDs;
        public int maxServants;
        public int lastScanTick;

        public ServantData(UUID playerUUID) {
            this.playerUUID = playerUUID;
            this.servantUUIDs = new HashSet<>();
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
            this.entityType = entity.getType().getDescriptionId();
            this.equipment.updateFromEntity(entity);
            this.attributes.updateFromEntity(entity);
            this.lastUpdateTime = System.currentTimeMillis();
        }

        public static class EquipmentData {
            public ItemStack helmet = ItemStack.EMPTY;
            public ItemStack chestplate = ItemStack.EMPTY;
            public ItemStack leggings = ItemStack.EMPTY;
            public ItemStack boots = ItemStack.EMPTY;
            public ItemStack mainHand = ItemStack.EMPTY;
            public ItemStack offHand = ItemStack.EMPTY;

            public void updateFromEntity(LivingEntity entity) {
                ItemStack newHelmet = entity.getItemBySlot(EquipmentSlot.HEAD);
                if (!ItemStack.matches(this.helmet, newHelmet)) {
                    this.helmet = newHelmet.copy();
                }

                ItemStack newChestplate = entity.getItemBySlot(EquipmentSlot.CHEST);
                if (!ItemStack.matches(this.chestplate, newChestplate)) {
                    this.chestplate = newChestplate.copy();
                }

                ItemStack newLeggings = entity.getItemBySlot(EquipmentSlot.LEGS);
                if (!ItemStack.matches(this.leggings, newLeggings)) {
                    this.leggings = newLeggings.copy();
                }

                ItemStack newBoots = entity.getItemBySlot(EquipmentSlot.FEET);
                if (!ItemStack.matches(this.boots, newBoots)) {
                    this.boots = newBoots.copy();
                }

                ItemStack newMainHand = entity.getMainHandItem();
                if (!ItemStack.matches(this.mainHand, newMainHand)) {
                    this.mainHand = newMainHand.copy();
                }

                ItemStack newOffHand = entity.getOffhandItem();
                if (!ItemStack.matches(this.offHand, newOffHand)) {
                    this.offHand = newOffHand.copy();
                }
            }

            public Map<String, Object> toMap() {
                Map<String, Object> map = new HashMap<>();
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
                Map<String, Object> itemData = new HashMap<>();
                itemData.put("item", stack.getItem().toString());
                itemData.put("count", stack.getCount());
                itemData.put("damage", stack.getDamageValue());
                itemData.put("maxDamage", stack.getMaxDamage());
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
                this.currentHealth = entity.getHealth();
                this.maxHealth = getMaxAttributeValue(entity, Attributes.MAX_HEALTH);
                this.attackDamage = getBaseAttributeValue(entity, Attributes.ATTACK_DAMAGE);
                this.armor = getBaseAttributeValue(entity, Attributes.ARMOR);
                this.movementSpeed = getBaseAttributeValue(entity, Attributes.MOVEMENT_SPEED);
                this.knockbackResistance = getBaseAttributeValue(entity, Attributes.KNOCKBACK_RESISTANCE);
            }

            private double getBaseAttributeValue(LivingEntity entity, net.minecraft.world.entity.ai.attributes.Attribute attribute) {
                AttributeInstance instance = entity.getAttribute(attribute);
                return instance != null ? instance.getBaseValue() : 0.0;
            }

            private double getMaxAttributeValue(LivingEntity entity, net.minecraft.world.entity.ai.attributes.Attribute attribute) {
                AttributeInstance instance = entity.getAttribute(attribute);
                return instance != null ? instance.getValue() : 0.0;
            }

            public Map<String, Double> toMap() {
                Map<String, Double> map = new HashMap<>();
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
            Map<String, Object> map = new HashMap<>();
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
            Map<String, Object> map = new HashMap<>();
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

    private static final Map<UUID, EnhancedServantData> ENHANCED_PLAYER_CACHE = new ConcurrentHashMap<>();

    public static Optional<EnhancedServantData> getEnhancedServantData(Player player) {
        return Optional.ofNullable(ENHANCED_PLAYER_CACHE.get(player.getUUID()));
    }

    public static Optional<ServantData> getServantData(Player player) {
        return Optional.ofNullable(PLAYER_SERVANT_CACHE.get(player.getUUID()));
    }

    public static void scanServantsForPlayer(MinecraftServer server, ServerPlayer player) {
        ServantData data = PLAYER_SERVANT_CACHE.computeIfAbsent(player.getUUID(), ServantData::new);
        EnhancedServantData enhancedData = ENHANCED_PLAYER_CACHE.computeIfAbsent(player.getUUID(), EnhancedServantData::new);

        Set<UUID> currentServants = new HashSet<>();

        for (ServerLevel level : server.getAllLevels()) {
            List<LivingEntity> entities = level.getEntitiesOfClass(
                    LivingEntity.class,
                    net.minecraft.world.phys.AABB.ofSize(player.position(), 100, 100, 100),
                    entity -> true
            );

            for (LivingEntity livingEntity : entities) {
                UUID ownerUUID = null;

                if (livingEntity instanceof OwnableEntity ownableEntity) {
                    ownerUUID = ownableEntity.getOwnerUUID();
                } else if (livingEntity instanceof IOwned iOwned) {
                    if (iOwned.getTrueOwner() != null) {
                        ownerUUID = iOwned.getTrueOwner().getUUID();
                    }
                }

                if (ownerUUID != null && ownerUUID.equals(player.getUUID())) {
                    currentServants.add(livingEntity.getUUID());

                    if (!data.hasServant(livingEntity.getUUID())) {
                        data.addServant(livingEntity.getUUID());
                        enhancedData.addOrUpdateServant(livingEntity);
                    } else {
                        enhancedData.addOrUpdateServant(livingEntity);
                    }
                }
            }
        }

        Set<UUID> removedServants = new HashSet<>(data.servantUUIDs);
        removedServants.removeAll(currentServants);

        for (UUID removedUUID : removedServants) {
            data.removeServant(removedUUID);
            enhancedData.removeServant(removedUUID);
        }

        data.lastScanTick = globalTickCounter;
        enhancedData.lastScanTick = globalTickCounter;
    }

    public static void scanAllPlayersOptimized(MinecraftServer server) {
        if (isScanning) {
            return;
        }

        isScanning = true;
        List<ServerLevel> levels = new ArrayList<>();
        for (ServerLevel level : server.getAllLevels()) {
            levels.add(level);
        }
        levelIterator = levels.iterator();
        scanningPlayerServants = new HashMap<>();

        processNextFrame();
    }

    private static void processNextFrame() {
        if (levelIterator == null || !levelIterator.hasNext()) {
            finishScanning();
            return;
        }

        if (currentLevel == null) {
            currentLevel = levelIterator.next();
            List<LivingEntity> entities = currentLevel.getEntitiesOfClass(
                    LivingEntity.class,
                    net.minecraft.world.phys.AABB.ofSize(Vec3.ZERO, 30000000, 30000000, 30000000),
                    entity -> true
            );
            entityIterator = entities.iterator();
        }

        int processedCount = 0;
        int maxPerFrame = 500;

        while (entityIterator != null && entityIterator.hasNext() && processedCount < maxPerFrame) {
            LivingEntity livingEntity = entityIterator.next();
            processedCount++;

            UUID ownerUUID = null;

            if (livingEntity instanceof OwnableEntity ownableEntity) {
                ownerUUID = ownableEntity.getOwnerUUID();
            } else if (livingEntity instanceof IOwned iOwned) {
                if (iOwned.getTrueOwner() != null) {
                    ownerUUID = iOwned.getTrueOwner().getUUID();
                }
            }

            if (ownerUUID != null) {
                scanningPlayerServants.computeIfAbsent(ownerUUID, k -> new HashSet<>())
                        .add(livingEntity.getUUID());

                ServantData data = PLAYER_SERVANT_CACHE.computeIfAbsent(ownerUUID, ServantData::new);
                EnhancedServantData enhancedData = ENHANCED_PLAYER_CACHE.computeIfAbsent(ownerUUID, EnhancedServantData::new);

                if (!data.hasServant(livingEntity.getUUID())) {
                    data.addServant(livingEntity.getUUID());
                    enhancedData.addOrUpdateServant(livingEntity);
                } else {
                    enhancedData.addOrUpdateServant(livingEntity);
                }
            }
        }

        if (entityIterator == null || !entityIterator.hasNext()) {
            currentLevel = null;
            entityIterator = null;
        }
    }

    private static void finishScanning() {
        for (Map.Entry<UUID, Set<UUID>> entry : scanningPlayerServants.entrySet()) {
            UUID playerUUID = entry.getKey();
            Set<UUID> currentServants = entry.getValue();

            ServantData data = PLAYER_SERVANT_CACHE.get(playerUUID);
            EnhancedServantData enhancedData = ENHANCED_PLAYER_CACHE.get(playerUUID);

            if (data != null && enhancedData != null) {
                Set<UUID> removedServants = new HashSet<>(data.servantUUIDs);
                removedServants.removeAll(currentServants);

                for (UUID removedUUID : removedServants) {
                    data.removeServant(removedUUID);
                    enhancedData.removeServant(removedUUID);
                }

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


    public static void scanAllPlayers(MinecraftServer server) {
        for (ServerLevel level : server.getAllLevels()) {
            for (ServerPlayer player : level.players()) {
                scanServantsForPlayer(server, player);
            }
        }
    }

    public static void onServantJoin(Player player, LivingEntity servant) {
        getServantData(player).ifPresent(data -> {
            data.addServant(servant.getUUID());
        });

        getEnhancedServantData(player).ifPresent(data -> {
            data.addOrUpdateServant(servant);
        });
    }

    public static void onServantDeath(Player player, UUID servantUUID) {
        getServantData(player).ifPresent(data -> {
            data.removeServant(servantUUID);
        });

        getEnhancedServantData(player).ifPresent(data -> {
            data.removeServant(servantUUID);
        });
    }

    public static void updateServantData(LivingEntity servant) {
        if (servant instanceof IOwned owned) {
            if (owned.getTrueOwner() instanceof Player ownerPlayer) {
                getEnhancedServantData(ownerPlayer).ifPresent(data -> {
                    data.addOrUpdateServant(servant);
                });
            }
        }
    }

    @Mod.EventBusSubscriber(modid = GoetyDelight.MODID, bus = Mod.EventBusSubscriber.Bus.FORGE)
    public static class EventHandler {

        @SubscribeEvent
        public static void onServerTick(TickEvent.ServerTickEvent event) {
            if (event.phase != TickEvent.Phase.START) return;

            globalTickCounter++;

            if (isScanning) {
                processNextFrame();
            } else if (globalTickCounter % SCAN_INTERVAL_TICKS == 0) {
                scanAllPlayersOptimized(event.getServer());
            }
        }

        @SubscribeEvent
        public static void onPlayerLoggedIn(net.minecraftforge.event.entity.player.PlayerEvent.PlayerLoggedInEvent event) {
            if (!event.getEntity().level().isClientSide() && event.getEntity() instanceof ServerPlayer player) {
                scanAllPlayersOptimized(player.getServer());
            }
        }

        @SubscribeEvent
        public static void onPlayerLoggedOut(net.minecraftforge.event.entity.player.PlayerEvent.PlayerLoggedOutEvent event) {
            PLAYER_SERVANT_CACHE.remove(event.getEntity().getUUID());
            ENHANCED_PLAYER_CACHE.remove(event.getEntity().getUUID());
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
