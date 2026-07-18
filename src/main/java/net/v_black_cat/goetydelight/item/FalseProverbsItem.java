package net.v_black_cat.goetydelight.item;

import com.Polarice3.Goety.client.particles.ModParticleTypes;
import com.Polarice3.Goety.common.items.ModTiers;
import com.Polarice3.Goety.common.network.ModNetwork;
import com.Polarice3.Goety.common.network.server.SPlayPlayerSoundPacket;
import com.Polarice3.Goety.init.ModSounds;
import com.Polarice3.Goety.utils.MathHelper;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.SwordItem;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.event.RenderArmEvent;
import net.minecraftforge.client.event.RenderPlayerEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.living.LivingDamageEvent;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.v_black_cat.goetydelight.config.Config;
import net.v_black_cat.goetydelight.network.NetworkHandler;
import net.v_black_cat.goetydelight.network.SyncBackModelPacket;

import java.lang.ref.WeakReference;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.IntStream;

@Mod.EventBusSubscriber
public class FalseProverbsItem extends SwordItem {
    private static final UUID IS_SHIFT_KEY_UUID = UUID.fromString("4f5f5f5f-5f5f-5f5f-5f5f-5f5f5f5f5f5f");
    public static final String SHIFT_KEY_TAG = "IsShift";

    // 优化后的玩家数据管理
    private static final Map<UUID, PlayerFalseProverbsData> playerDataMap = new ConcurrentHashMap<>();
    private static final Map<UUID, CachedInventoryResult> inventoryCache = new ConcurrentHashMap<>();
    private static final Map<UUID, Boolean> lastSentBackModelStatus = new ConcurrentHashMap<>();

    private static final int CACHE_DURATION = 20; // tick缓存时间
    private static final int CLEANUP_INTERVAL = 1200; // 60秒清理一次
    private static final int SYNC_DISTANCE_SQR = 4096; // 64格内同步
    private static int cleanupCounter = 0;

    // 玩家数据封装类
    private static class PlayerFalseProverbsData {
        boolean teleportStatus = false;
        boolean backModelStatus = false;
        boolean isBackstab = false; // 新增：背刺状态
        Vec3 originalPosition = null;
        WeakReference<Level> worldLevel = null;

        void clearPosition() {
            originalPosition = null;
            worldLevel = null;
        }

        void clear() {
            teleportStatus = false;
            backModelStatus = false;
            isBackstab = false;
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

    public FalseProverbsItem(ModTiers tier, float attackDamage, float attackSpeed, Properties properties) {
        super(tier, (int) attackDamage, attackSpeed, properties);
    }

    // 玩家数据访问方法
    private static PlayerFalseProverbsData getPlayerData(UUID uuid) {
        return playerDataMap.computeIfAbsent(uuid, k -> new PlayerFalseProverbsData());
    }

    public static boolean getPlayerTeleportStatus(UUID playerUUID) {
        return getPlayerData(playerUUID).teleportStatus;
    }

    public static boolean getPlayerBackModelStatus(UUID playerUUID) {
        return getPlayerData(playerUUID).backModelStatus;
    }

    public static void setPlayerTeleportStatus(UUID playerUUID, boolean status) {
        getPlayerData(playerUUID).teleportStatus = status;
    }

    public static void setPlayerBackModelStatus(UUID playerUUID, boolean status) {
        getPlayerData(playerUUID).backModelStatus = status;
    }

    public static void removePlayerTeleportStatus(UUID playerUUID) {
        PlayerFalseProverbsData data = playerDataMap.get(playerUUID);
        if (data != null) {
            data.teleportStatus = false;
            data.clearPosition();
        }
    }

    public static void removePlayerBackModelStatus(UUID playerUUID) {
        PlayerFalseProverbsData data = playerDataMap.get(playerUUID);
        if (data != null) {
            data.backModelStatus = false;
        }
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

    @Override
    public boolean canApplyAtEnchantingTable(ItemStack stack, Enchantment enchantment) {
        if (enchantment instanceof vectorwing.farmersdelight.common.item.enchantment.BackstabbingEnchantment) {
            return true;
        }
        return super.canApplyAtEnchantingTable(stack, enchantment);
    }

    @Override
    public boolean isEnchantable(ItemStack stack) {
        return true;
    }

    // 优化的粒子效果方法
    private static void spawnShiftParticles(Player player) {
        if (!(player.level() instanceof ServerLevel serverLevel)) return;
        if (player.tickCount % 20 != 0) return; // 每20tick生成一次粒子

        SimpleParticleType particleType = (SimpleParticleType) ModParticleTypes.CULT_SPELL.get();
        for (int i = 0; i < 2; ++i) { // 减少粒子数量
            double d0 = MathHelper.rgbToSpeed(96.0F);
            double d1 = MathHelper.rgbToSpeed(62.0F);
            double d2 = MathHelper.rgbToSpeed(92.0F);
            serverLevel.sendParticles(particleType,
                    player.getRandomX(1.0F), player.getRandomY(), player.getRandomZ(1.0F),
                    0, d0, d1, d2, 0.5F);
        }
    }

    // 统一的伤害计算逻辑
    private static float calculateDamageMultiplier(Player player, LivingEntity target, float originalDamage) {
        PlayerFalseProverbsData data = getPlayerData(player.getUUID());
        boolean isShiftKey = player.isShiftKeyDown();

        if (data.teleportStatus && isShiftKey) {
            // 在LivingHurt中判断并存储背刺状态
            data.isBackstab = vectorwing.farmersdelight.common.item.enchantment.BackstabbingEnchantment
                    .isLookingBehindTarget(target, player.getEyePosition());

            // 统一应用通用倍数
            return (float) (originalDamage * Config.getLivingDamageGeneralMultiplier());
        }
        return (float) (isShiftKey ? originalDamage * Config.getLivingDamageGeneralMultiplier()
                        : originalDamage * Config.getLivingHurtDamageMultiplier());
    }

    @SubscribeEvent
    public static void onPlayerTick(TickEvent.PlayerTickEvent event) {
        Player player = event.player;
        if (player.level().isClientSide) return;

        UUID playerUUID = player.getUUID();
        PlayerFalseProverbsData data = getPlayerData(playerUUID);

        if (player.getMainHandItem().getItem() instanceof FalseProverbsItem) {
            CompoundTag persistentData = player.getPersistentData();

            if (player.isShiftKeyDown()) {
                if (!persistentData.getBoolean(SHIFT_KEY_TAG)) {
                    // 添加属性加成
                    addBonusAttributes(player);
                    persistentData.putBoolean(SHIFT_KEY_TAG, true);

                    // 设置传送状态
                    data.originalPosition = player.position();
                    data.worldLevel = new WeakReference<>(player.level());
                    data.teleportStatus = true;

                    // 视觉效果
                    player.setInvisible(true);
                    spawnShiftParticles(player);
                    ModNetwork.sendTo(player, new SPlayPlayerSoundPacket(
                            (SoundEvent) ModSounds.END_WALK.get(), 0.5F, 1.0F));
                }

                // 检查维度变化
                if (data.worldLevel != null && player.level() != data.worldLevel.get()) {
                    data.clearPosition();
                }
            } else {
                if (persistentData.getBoolean(SHIFT_KEY_TAG)) {
                    resetShiftState(player, persistentData, data);
                }
            }
        } else {
            if (player.getPersistentData().getBoolean(SHIFT_KEY_TAG)) {
                resetShiftState(player, player.getPersistentData(), data);
            }
        }

        // 优化后的背部模型同步
        syncBackModelStatus(player, playerUUID);
    }

    private static void resetShiftState(Player player, CompoundTag persistentData, PlayerFalseProverbsData data) {
        player.getPersistentData().remove(SHIFT_KEY_TAG);
        removeBonusAttributes(player);
        data.clearPosition();
        data.teleportStatus = false;
        player.setInvisible(false);
    }

    private static void syncBackModelStatus(Player player, UUID playerUUID) {
        boolean newBackStatus = shouldShowBackModel(player);
        Boolean lastStatus = lastSentBackModelStatus.get(playerUUID);

        if (lastStatus == null || lastStatus != newBackStatus) {
            getPlayerData(playerUUID).backModelStatus = newBackStatus;
            lastSentBackModelStatus.put(playerUUID, newBackStatus);

            SyncBackModelPacket packet = new SyncBackModelPacket(player.getId(), newBackStatus);
            ServerLevel serverLevel = (ServerLevel) player.level();

            for (ServerPlayer serverPlayer : serverLevel.players()) {
                // 距离优化，减少不必要的网络传输
                if (serverPlayer.distanceToSqr(player) < SYNC_DISTANCE_SQR) {
                    NetworkHandler.sendToClient(packet, serverPlayer);
                }
            }
        }
    }

    private static void addBonusAttributes(Player player) {
        AttributeInstance speedAttribute = player.getAttribute(Attributes.MOVEMENT_SPEED);
        if (speedAttribute != null && speedAttribute.getModifier(IS_SHIFT_KEY_UUID) == null) {
            AttributeModifier modifier = new AttributeModifier(
                    IS_SHIFT_KEY_UUID,
                    "Shift speed",
                    Config.getShiftSpeedMultiplier(),
                    AttributeModifier.Operation.MULTIPLY_TOTAL
            );
            speedAttribute.addTransientModifier(modifier);
        }
    }

    private static void removeBonusAttributes(Player player) {
        AttributeInstance speedAttribute = player.getAttribute(Attributes.MOVEMENT_SPEED);
        if (speedAttribute != null && speedAttribute.getModifier(IS_SHIFT_KEY_UUID) != null) {
            speedAttribute.removeModifier(IS_SHIFT_KEY_UUID);
        }
    }

    @SubscribeEvent
    public static void onLivingHurt(LivingHurtEvent event) {
        if (!(event.getSource().getEntity() instanceof Player player)) return;
        if (!(player.getMainHandItem().getItem() instanceof FalseProverbsItem)) return;
        if (player.isUsingItem()) return;

        event.setAmount(calculateDamageMultiplier(player, event.getEntity(), event.getAmount()));
    }

    @SubscribeEvent
    public static void onLivingDamage(LivingDamageEvent event) {
        if (!(event.getSource().getEntity() instanceof Player player)) return;
        if (!(player.getMainHandItem().getItem() instanceof FalseProverbsItem)) return;
        if (player.isUsingItem()) return;

        PlayerFalseProverbsData data = getPlayerData(player.getUUID());
        if (!data.teleportStatus) return;

        if (player.isShiftKeyDown()) {
            // 使用存储的背刺状态
            if (data.isBackstab && event.getAmount() > 0.0F) {
                // 背刺额外增幅
                float backstabMultiplier = (float) Config.getLivingDamageBackstabMultiplier();
                float generalMultiplier = (float) Config.getLivingDamageGeneralMultiplier();
                event.setAmount(event.getAmount() * (backstabMultiplier / generalMultiplier));
            }

            // 传送回原位
            if (data.originalPosition != null) {
                player.teleportTo(data.originalPosition.x, data.originalPosition.y, data.originalPosition.z);
            }

            // 清除传送状态
            data.clear();
            player.setInvisible(false);
        }
    }

    @OnlyIn(Dist.CLIENT)
    @SubscribeEvent
    public static void onPlayerRenderPre(RenderPlayerEvent.Pre event) {
        if (!(event.getEntity().level() instanceof ClientLevel)) return;

        Player player = event.getEntity();
        if (!(player.getMainHandItem().getItem() instanceof FalseProverbsItem)) return;
        if (!player.isShiftKeyDown()) return;

        event.setCanceled(true);
    }

    @OnlyIn(Dist.CLIENT)
    @SubscribeEvent
    public static void renderArm(RenderArmEvent event) {
        AbstractClientPlayer player = event.getPlayer();
        if (!(player.getMainHandItem().getItem() instanceof FalseProverbsItem)) return;
        if (!player.isShiftKeyDown()) return;

        if (player.getMainHandItem().isEmpty() && event.getArm() == player.getMainArm()) {
            event.setCanceled(true);
        } else if (player.getOffhandItem().isEmpty() && event.getArm() != player.getMainArm()) {
            event.setCanceled(true);
        }
    }

    // 清理事件
    @SubscribeEvent
    public static void onPlayerLogout(PlayerEvent.PlayerLoggedOutEvent event) {
        UUID uuid = event.getEntity().getUUID();
        playerDataMap.remove(uuid);
        inventoryCache.remove(uuid);
        lastSentBackModelStatus.remove(uuid);
    }

    // 定期清理过期数据
    @SubscribeEvent
    public static void onServerTick(TickEvent.ServerTickEvent event) {
        if (event.phase != TickEvent.Phase.END) return;

        cleanupCounter++;
        if (cleanupCounter >= CLEANUP_INTERVAL) {
            cleanupCounter = 0;

            // 清理过期的缓存数据
            long currentTick = event.getServer().getTickCount();
            inventoryCache.entrySet().removeIf(entry ->
                    entry.getValue().lastCheckTick + CACHE_DURATION < currentTick
            );

            // 清理离线玩家的数据
            lastSentBackModelStatus.keySet().removeIf(uuid ->
                    event.getServer().getPlayerList().getPlayer(uuid) == null
            );
            playerDataMap.keySet().removeIf(uuid ->
                    event.getServer().getPlayerList().getPlayer(uuid) == null
            );
        }
    }
}