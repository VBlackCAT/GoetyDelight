package net.v_black_cat.goetydelight.item;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.LivingDropsEvent;
import net.v_black_cat.goetydelight.GoetyDelight;
import net.v_black_cat.goetydelight.init.ModConfig;

import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

/**
 * 物品黑名单（1.21.1 移植版，事件处理已移至 events/ 包）。
 * 与 1.20.1 对齐：从配置读取黑名单，屏蔽掉落过滤 + Patchouli 图标过滤。
 */
public class ItemBlackList {
    private static final Set<String> BLACK_LIST_SET = new HashSet<>();

    static {
        ModConfig.registerBlackListUpdateListener(v -> updateBlackListFromConfig());
        updateBlackListFromConfig();
    }

    public static void updateBlackListFromConfig() {
        BLACK_LIST_SET.clear();
        if (ModConfig.blacklistedItems != null) {
            ModConfig.blacklistedItems.stream()
                    .map(item -> BuiltInRegistries.ITEM.getKey(item).toString())
                    .forEach(BLACK_LIST_SET::add);
        }
    }

    public static Set<String> getBlackList() {
        return BLACK_LIST_SET;
    }

    public static boolean isBlackListed(String itemName) {
        return BLACK_LIST_SET.contains(itemName);
    }

    public static boolean isBlackListed(Item item) {
        String itemName = BuiltInRegistries.ITEM.getKey(item).toString();
        return isBlackListed(itemName);
    }

    public static void addToBlackList(String itemName) {
        BLACK_LIST_SET.add(itemName);
    }

    public static boolean isIconBlackListed(String iconString) {
        if (iconString == null || iconString.isEmpty()) return false;
        if (BLACK_LIST_SET.contains(iconString)) return true;
        if (iconString.contains(":")) {
            String[] parts = iconString.split(":");
            if (parts.length >= 2) {
                if (BLACK_LIST_SET.contains(parts[0] + ":" + parts[1])) return true;
            }
        }
        return false;
    }

    /**
     * 掉落过滤（对应 1.20.1 ItemBlackList.ItemDropHandler）。
     */
    @EventBusSubscriber(modid = GoetyDelight.MODID)
    public static class ItemDropHandler {
        @SubscribeEvent
        public static void onLivingDrops(LivingDropsEvent event) {
            Collection<ItemEntity> drops = event.getDrops();
            if (drops.isEmpty()) return;

            Iterator<ItemEntity> iterator = drops.iterator();
            while (iterator.hasNext()) {
                ItemStack itemStack = iterator.next().getItem();
                if (isBlackListed(itemStack.getItem())) {
                    iterator.remove();
                }
            }
        }
    }
}
