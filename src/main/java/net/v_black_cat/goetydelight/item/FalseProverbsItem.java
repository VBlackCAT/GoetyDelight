package net.v_black_cat.goetydelight.item;

import com.Polarice3.Goety.common.items.ModItems;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EquipmentSlotGroup;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.SwordItem;
import net.minecraft.world.item.Tier;
import net.minecraft.world.item.component.ItemAttributeModifiers;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.neoforge.client.extensions.common.IClientItemExtensions;
import net.v_black_cat.goetydelight.renderer.FalseProverbsItemRender;

import java.lang.ref.WeakReference;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.IntStream;

public class FalseProverbsItem extends SwordItem {

    // 优化的玩家数据管理
    private static final Map<UUID, PlayerFalseProverbsData> playerDataMap = new ConcurrentHashMap<>();
    private static final Map<UUID, CachedInventoryResult> inventoryCache = new ConcurrentHashMap<>();
    private static final Map<UUID, Boolean> lastSentBackModelStatus = new ConcurrentHashMap<>();

    private static final int CACHE_DURATION = 20; // tick缓存时间
    private static final int CLEANUP_INTERVAL = 1200; // 60秒清理一次

    private static FalseProverbsItemRender renderer = null;
    private static final float ADDED_DAMAGE = 0.0f;

    // 玩家数据封装类
    private static class PlayerFalseProverbsData {
        boolean teleportStatus = false;
        boolean backModelStatus = false;
        Vec3 originalPosition = null;
        WeakReference<Level> worldLevel = null;

        void clearPosition() {
            originalPosition = null;
            worldLevel = null;
        }

        void clear() {
            teleportStatus = false;
            backModelStatus = false;
            clearPosition();
        }
    }

    // 缓存背包检查结果
    private static class CachedInventoryResult {
        boolean shouldShowBack;
        long lastCheckTick;
        int inventoryHash;

        boolean isValid(Player player) {
            int currentHash = calculateInventoryHash(player.getInventory());
            return lastCheckTick + CACHE_DURATION > player.tickCount &&
                    inventoryHash == currentHash;
        }
    }

    public FalseProverbsItem(Tier tier, Properties properties) {
        super(tier, properties.attributes(
                ItemAttributeModifiers.builder()
                        .add(Attributes.ATTACK_DAMAGE,
                                new AttributeModifier(
                                        ResourceLocation.withDefaultNamespace("base_attack_damage"),
                                        tier.getAttackDamageBonus(),
                                        AttributeModifier.Operation.ADD_VALUE
                                ),
                                EquipmentSlotGroup.MAINHAND)
                        .add(Attributes.ATTACK_SPEED,
                                new AttributeModifier(
                                        ResourceLocation.withDefaultNamespace("base_attack_speed"),
                                        -2.0,
                                        AttributeModifier.Operation.ADD_VALUE
                                ),
                                EquipmentSlotGroup.MAINHAND)
                        .add(Attributes.ATTACK_DAMAGE,
                                new AttributeModifier(
                                        ResourceLocation.withDefaultNamespace("false_proverbs_boost"),
                                        ADDED_DAMAGE,
                                        AttributeModifier.Operation.ADD_VALUE
                                ),
                                EquipmentSlotGroup.MAINHAND)
                        .build()
        ));
    }

    public FalseProverbsItem(Tier tier, int attackDamageModifier, float attackSpeed, Properties properties) {
        this(tier, properties);
    }

    @Override
    public boolean isValidRepairItem(ItemStack toRepair, ItemStack repair) {
        return repair.is(ModItems.DARK_ALLOY_INGOT.get());
    }

    // 玩家数据访问方法
    private static PlayerFalseProverbsData getPlayerData(UUID uuid) {
        return playerDataMap.computeIfAbsent(uuid, k -> new PlayerFalseProverbsData());
    }

    public static boolean getPlayerTeleportStatus(UUID playerUUID) {
        return getPlayerData(playerUUID).teleportStatus;
    }

    public static void setPlayerTeleportStatus(UUID playerUUID, boolean status) {
        getPlayerData(playerUUID).teleportStatus = status;
    }

    public static void removePlayerTeleportStatus(UUID playerUUID) {
        PlayerFalseProverbsData data = playerDataMap.get(playerUUID);
        if (data != null) {
            data.teleportStatus = false;
            data.clearPosition();
        }
    }

    public static boolean getPlayerBackModelStatus(UUID playerUUID) {
        return getPlayerData(playerUUID).backModelStatus;
    }

    public static void setPlayerBackModelStatus(UUID playerUUID, boolean status) {
        getPlayerData(playerUUID).backModelStatus = status;
    }

    public static void removePlayerBackModelStatus(UUID playerUUID) {
        PlayerFalseProverbsData data = playerDataMap.get(playerUUID);
        if (data != null) {
            data.backModelStatus = false;
        }
    }

    public static Vec3 getOriginalPosition(UUID playerUUID) {
        return getPlayerData(playerUUID).originalPosition;
    }

    public static void setOriginalPosition(UUID playerUUID, Vec3 position) {
        getPlayerData(playerUUID).originalPosition = position;
    }

    public static void setWorldLevel(UUID playerUUID, Level level) {
        getPlayerData(playerUUID).worldLevel = new WeakReference<>(level);
    }

    public static Level getWorldLevel(UUID playerUUID) {
        PlayerFalseProverbsData data = playerDataMap.get(playerUUID);
        return data != null && data.worldLevel != null ? data.worldLevel.get() : null;
    }

    // 优化后的背包检查方法
    private static int calculateInventoryHash(Inventory inventory) {
        int hash = 0;
        for (int i = 0; i < inventory.getContainerSize(); i++) {
            ItemStack stack = inventory.getItem(i);
            if (stack.getItem() instanceof FalseProverbsItem) {
                hash = 31 * hash + i;
            }
        }
        return hash;
    }

    public static boolean shouldShowBackModel(Player player) {
        UUID uuid = player.getUUID();
        CachedInventoryResult cached = inventoryCache.get(uuid);

        if (cached != null && cached.isValid(player)) {
            return cached.shouldShowBack;
        }

        // 计算新结果
        Inventory inventory = player.getInventory();
        boolean hasInMainHand = player.getMainHandItem().getItem() instanceof FalseProverbsItem;
        boolean hasInOffHand = player.getOffhandItem().getItem() instanceof FalseProverbsItem;

        // 使用Stream API优化遍历
        int falseProverbsCount = (hasInMainHand ? 1 : 0) + (hasInOffHand ? 1 : 0);
        falseProverbsCount += IntStream.range(0, inventory.getContainerSize())
                .filter(i -> i != inventory.selected)
                .mapToObj(inventory::getItem)
                .filter(stack -> stack.getItem() instanceof FalseProverbsItem)
                .count();

        CachedInventoryResult result = new CachedInventoryResult();
        result.shouldShowBack = hasInOffHand ? false :
                (falseProverbsCount > 1 || (falseProverbsCount == 1 && !hasInMainHand));
        result.lastCheckTick = player.tickCount;
        result.inventoryHash = calculateInventoryHash(inventory);

        inventoryCache.put(uuid, result);
        return result.shouldShowBack;
    }

    // 清理方法
    public static void clearPlayerData(UUID uuid) {
        playerDataMap.remove(uuid);
        inventoryCache.remove(uuid);
        lastSentBackModelStatus.remove(uuid);
    }

    public static void cleanupExpiredData(long currentTick, java.util.function.Predicate<UUID> isPlayerOnline) {
        // 清理过期的缓存数据
        inventoryCache.entrySet().removeIf(entry ->
                entry.getValue().lastCheckTick + CACHE_DURATION < currentTick
        );

        // 清理离线玩家的数据
        lastSentBackModelStatus.keySet().removeIf(uuid -> !isPlayerOnline.test(uuid));
        playerDataMap.keySet().removeIf(uuid -> !isPlayerOnline.test(uuid));
    }

    public static Map<UUID, Boolean> getLastSentBackModelStatus() {
        return lastSentBackModelStatus;
    }

    @OnlyIn(Dist.CLIENT)
    public IClientItemExtensions getClientExtensions() {
        return new IClientItemExtensions() {
            @Override
            public BlockEntityWithoutLevelRenderer getCustomRenderer() {
                if (renderer == null) {
                    var mc = Minecraft.getInstance();
                    renderer = new FalseProverbsItemRender(
                            mc.getBlockEntityRenderDispatcher(),
                            mc.getEntityModels()
                    );
                }
                return renderer;
            }
        };
    }

    @Override
    public boolean isEnchantable(ItemStack stack) {
        return true;
    }
}