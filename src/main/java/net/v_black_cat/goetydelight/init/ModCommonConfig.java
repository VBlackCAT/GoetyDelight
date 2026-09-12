package net.v_black_cat.goetydelight.init;

import java.util.*;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import net.minecraft.world.item.Item;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.event.config.ModConfigEvent;
import net.neoforged.neoforge.common.ModConfigSpec;
import net.v_black_cat.goetydelight.GoetyDelight;

/**
 * 通用配置：客户端与服务端都必须一致、且不随存档变化的条目（否则会出现两端表现不一致）。
 * <p>
 * 对应配置文件：goetydelight-common.toml
 */
@EventBusSubscriber(modid = GoetyDelight.MODID)
public class ModCommonConfig {
    private static final ModConfigSpec.Builder BUILDER = new ModConfigSpec.Builder();

    // ==================== 黑名单物品 ====================
    private static final ModConfigSpec.ConfigValue<List<? extends String>> BLACKLISTED_ITEMS = BUILDER
            .comment("A list of blacklisted items that will be hidden from creative tabs and prevent drops\n物品黑名单列表，这些物品将从创造模式标签页隐藏并阻止掉落")
            .defineListAllowEmpty("blacklistedItems", List.of(
                    "goetydelight:roasted_corpse_maggots",
                    "goetydelight:corpse_maggot",
                    "goetydelight:rotten_corpse_maggot_feast",
                    "goetydelight:rotten_corpse_maggot_feast_block"
            ), ModCommonConfig::validateItemName);


    // ==================== 构建 Spec ====================
    public static final ModConfigSpec SPEC = BUILDER.build();

    // ==================== 缓存字段 ====================
    /** 黑名单物品缓存：由 onLoad 在配置加载/重载后刷新，ItemBlackList 读取 */
    public static Set<Item> blacklistedItems;
    private static Consumer<Void> blackListUpdateListener;

    // ==================== Getter / 辅助方法 ====================


    private static boolean validateItemName(final Object obj) {
        return obj instanceof final String itemName && BuiltInRegistries.ITEM.containsKey(ResourceLocation.parse(itemName));
    }

    // ==================== 事件监听 ====================
    @SubscribeEvent
    static void onLoad(final ModConfigEvent event) {
        // 只处理本配置的事件：客户端/服务端/启动项各有自己的配置，混在一起会在未加载时读值报错
        if (event.getConfig().getSpec() != SPEC) {
            return;
        }
        blacklistedItems = BLACKLISTED_ITEMS.get().stream()
                .map(itemName -> BuiltInRegistries.ITEM.get(ResourceLocation.parse(itemName)))
                .collect(Collectors.toSet());

        if (blackListUpdateListener != null) {
            blackListUpdateListener.accept(null);
        }
    }

    // ==================== 回调注册 ====================
    public static void registerBlackListUpdateListener(Consumer<Void> listener) {
        blackListUpdateListener = listener;
    }
}
