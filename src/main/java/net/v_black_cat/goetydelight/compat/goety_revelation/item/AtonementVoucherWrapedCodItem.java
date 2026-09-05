package net.v_black_cat.goetydelight.compat.goety_revelation.item;

import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class AtonementVoucherWrapedCodItem extends Item {
    private static final Map<UUID, Integer> PLAYER_USAGE_COUNT = new ConcurrentHashMap<>();

    public AtonementVoucherWrapedCodItem(Properties properties) {
        super(properties);
    }

    public static int getUsageCount(Player player) {
        return PLAYER_USAGE_COUNT.getOrDefault(player.getUUID(), 0);
    }

    public static int getUsageCount(UUID playerUUID) {
        return PLAYER_USAGE_COUNT.getOrDefault(playerUUID, 0);
    }

    public static int incrementUsageCount(Player player) {
        UUID playerUUID = player.getUUID();
        return PLAYER_USAGE_COUNT.merge(playerUUID, 1, Integer::sum);
    }

    public static int incrementUsageCount(UUID playerUUID) {
        return PLAYER_USAGE_COUNT.merge(playerUUID, 1, Integer::sum);
    }

    public static void resetUsageCount(Player player) {
        PLAYER_USAGE_COUNT.remove(player.getUUID());
    }

    public static void setUsageCount(Player player, int count) {
        UUID playerUUID = player.getUUID();
        if (count <= 0) {
            PLAYER_USAGE_COUNT.remove(playerUUID);
        } else {
            PLAYER_USAGE_COUNT.put(playerUUID, count);
        }
    }

    public static Map<UUID, Integer> getAllUsageCounts() {
        return Map.copyOf(PLAYER_USAGE_COUNT);
    }

    public static void clearAllUsageCounts() {
        PLAYER_USAGE_COUNT.clear();
    }

    public static boolean hasReachedCount(Player player, int threshold) {
        return getUsageCount(player) >= threshold;
    }

    @Override
    public ItemStack finishUsingItem(ItemStack stack, Level level, LivingEntity entity) {
        if (!level.isClientSide && entity instanceof Player player) {
            incrementUsageCount(player);
        }
        return super.finishUsingItem(stack, level, entity);
    }
}