package net.v_black_cat.goetydelight.item;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.event.BuildCreativeModeTabContentsEvent;
import net.minecraftforge.event.entity.living.LivingDropsEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.v_black_cat.goetydelight.GoetyDelight;
import net.v_black_cat.goetydelight.config.Config;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

public class ItemBlackList {


    private static final Set<String> BLACK_LIST_SET = new HashSet<>();

    public static void updateBlackListFromConfig() {
        BLACK_LIST_SET.clear();
        if (Config.blacklistedItems != null) {
            Config.blacklistedItems.stream()
                    .map(item -> BuiltInRegistries.ITEM.getKey(item).toString())
                    .forEach(BLACK_LIST_SET::add);
        }

        // 强制添加黑名单
        /*
        Collections.addAll(BLACK_LIST_SET,
                "goetydelight:"
        );
        */
    }

    static {
        Config.registerBlackListUpdateListener(v -> updateBlackListFromConfig());
        updateBlackListFromConfig();

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

    public static void removeFromBlackList(String itemName) {
        BLACK_LIST_SET.remove(itemName);
    }

    public static boolean isIconBlackListed(String iconString) {
        if (iconString == null || iconString.isEmpty()) {
            return false;
        }

        // 直接检查完整字符串
        if (BLACK_LIST_SET.contains(iconString)) {
            return true;
        }

        // 处理可能的不同格式
        if (iconString.contains(":")) {
            String[] parts = iconString.split(":");
            if (parts.length >= 2) {
                // 提取物品ID部分
                String itemId = parts[0] + ":" + parts[1];
                if (BLACK_LIST_SET.contains(itemId)) {
                    return true;
                }
            }
        }

        return false;
    }

    @Mod.EventBusSubscriber(modid = GoetyDelight.MODID, bus = Mod.EventBusSubscriber.Bus.FORGE)
    public static class ItemDropHandler {

        @SubscribeEvent
        public static void onLivingDrops(LivingDropsEvent event) {
            Collection<ItemEntity> drops = event.getDrops();
            if (drops.isEmpty()) {
                return;
            }

            Iterator<ItemEntity> iterator = drops.iterator();
            while (iterator.hasNext()) {
                ItemEntity itemEntity = iterator.next();
                ItemStack itemStack = itemEntity.getItem();

                if (isBlackListed(itemStack.getItem())) {
                    iterator.remove();
                }
            }
        }
    }

    @Mod.EventBusSubscriber(modid = GoetyDelight.MODID, bus = Mod.EventBusSubscriber.Bus.MOD)
    public static class CreativeTabHandler {

        @SubscribeEvent
        public static void onBuildCreativeModeTabContents(BuildCreativeModeTabContentsEvent event) {
            var tab = event.getTab();
            if (tab == null) return;

            var entries = event.getEntries();
            var iterator = entries.iterator();

            while (iterator.hasNext()) {
                Map.Entry<ItemStack, CreativeModeTab.TabVisibility> entry = iterator.next();
                ItemStack stack = entry.getKey();

                if (!stack.isEmpty()) {
                    Item item = stack.getItem();
                    if (isBlackListed(item)) {
                        iterator.remove();
                    }
                }
            }
        }
    }
}