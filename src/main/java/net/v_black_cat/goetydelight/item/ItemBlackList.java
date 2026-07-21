package net.v_black_cat.goetydelight.item;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.item.Item;

import java.util.HashSet;
import java.util.Set;

/**
 * 物品黑名单（1.21.1 迁移版，事件处理已移至 events/ 包）
 */
public class ItemBlackList {
    private static final Set<String> BLACK_LIST_SET = new HashSet<>();

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
}
