package net.v_black_cat.goetydelight.util;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.server.packs.resources.ResourceManager;

import net.minecraft.server.packs.resources.SimplePreparableReloadListener;
import net.minecraft.tags.TagKey;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;

import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

import net.minecraft.world.level.storage.loot.LootDataManager;
import net.minecraft.world.level.storage.loot.LootPool;
import net.minecraft.world.level.storage.loot.LootTable;

import net.minecraft.world.level.storage.loot.entries.*;

import net.minecraftforge.event.AddReloadListenerEvent;
import net.minecraftforge.event.server.ServerStartedEvent;
import net.minecraftforge.event.server.ServerStoppingEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.io.Reader;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;


@Mod.EventBusSubscriber
public class DelightLootTableCache {
    private static final Map<EntityType<?>, Set<ItemStack>> MOB_DELIGHT_DROPS =
            new ConcurrentHashMap<>();

    private static final Set<ResourceLocation> PROCESSED_TABLES =
            ConcurrentHashMap.newKeySet();

    private static final Map<ResourceLocation, Set<ItemStack>> TABLE_DELIGHT_DROPS =
            new ConcurrentHashMap<>();

    private static volatile boolean isLoaded = false;

    // 保存服务器实例引用
    private static MinecraftServer currentServer = null;

    static {
        System.out.println("========================================");
        System.out.println("[Goety Delight] CACHE CLASS LOADED");
        System.out.println("[Goety Delight] MOB_DELIGHT_DROPS = " + MOB_DELIGHT_DROPS);
        System.out.println("[Goety Delight] PROCESSED_TABLES = " + PROCESSED_TABLES);
        System.out.println("[Goety Delight] TABLE_DELIGHT_DROPS = " + TABLE_DELIGHT_DROPS);
        System.out.println("========================================");
    }

    /**
     * 服务器启动时扫描所有战利品表
     */
    @SubscribeEvent
    public static void onServerStarted(ServerStartedEvent event) {
        System.out.println("[Goety Delight] Server started event fired!");

        MinecraftServer server = event.getServer();
        currentServer = server;

        try {
            System.out.println("[Goety Delight] Server instance: " + server);
            System.out.println("[Goety Delight] LootDataManager: " + server.getLootData());

            clearCache();

            System.out.println("[Goety Delight] Starting scanAllMobLootTables...");
            scanAllMobLootTables(server);

            System.out.println("[Goety Delight] Starting scanDelightLootModifiers...");
            scanDelightLootModifiers(server);

            isLoaded = true;
            System.out.println("[Goety Delight] Scan complete successfully!");
        } catch (Exception e) {
            System.err.println("[Goety Delight] Fatal error during loot table scan!");
            e.printStackTrace();
        }
    }

    /**
     * 服务器停止时清理缓存
     */
    @SubscribeEvent
    public static void onServerStopping(ServerStoppingEvent event) {
        System.out.println("[Goety Delight] Server stopping, clearing cache...");
        clearCache();
        currentServer = null;
    }

    /**
     * 注册资源重载监听器
     */
    @SubscribeEvent
    public static void onAddReloadListener(AddReloadListenerEvent event) {
        System.out.println("[Goety Delight] AddReloadListenerEvent fired");

        event.addListener(new SimplePreparableReloadListener<Void>() {
            @Override
            protected Void prepare(ResourceManager resourceManager, ProfilerFiller profiler) {
                System.out.println("[Goety Delight] Preparing Delight loot cache reload...");
                return null;
            }
            @Override
            protected void apply(Void object, ResourceManager resourceManager, ProfilerFiller profiler
            ) {
                System.out.println("[Goety Delight] Applying Delight loot cache reload...");

                // 如果有服务器实例，重新扫描
                if (currentServer != null) {
                    System.out.println("[Goety Delight] Re-scanning loot tables after reload...");

                    // 清理旧数据
                    clearCache();

                    // 重新扫描
                    scanAllMobLootTables(currentServer);
                    scanDelightLootModifiers(currentServer);

                    // 标记为已加载
                    isLoaded = true;

                    System.out.println("[Goety Delight] Reload scan complete!");
                    printScanResults();
                }
            }
        });
    }

    /**
     * 打印扫描结果用于调试
     */
    private static void printScanResults() {
        System.out.println("[Goety Delight] ====== Scan Results ======");
        System.out.println("[Goety Delight] Mobs with Delight drops: " + MOB_DELIGHT_DROPS.size());

        for (Map.Entry<EntityType<?>, Set<ItemStack>> entry : MOB_DELIGHT_DROPS.entrySet()) {
            EntityType<?> entityType = entry.getKey();
            Set<ItemStack> drops = entry.getValue();

            ResourceLocation entityId = BuiltInRegistries.ENTITY_TYPE.getKey(entityType);
            System.out.println("[Goety Delight] Entity: " + entityId + " has " + drops.size() + " Delight drops:");

            for (ItemStack stack : drops) {
                ResourceLocation itemId = BuiltInRegistries.ITEM.getKey(stack.getItem());
                System.out.println("[Goety Delight]   - " + itemId + " x" + stack.getCount());
            }
        }

        System.out.println("[Goety Delight] Processed loot tables: " + PROCESSED_TABLES.size());
        System.out.println("[Goety Delight] Tables with Delight drops: " + TABLE_DELIGHT_DROPS.size());
        System.out.println("[Goety Delight] ==============================");
    }

    /**
     * 扫描所有生物的掉落表
     */
    private static void scanAllMobLootTables(MinecraftServer server) {
        System.out.println("[Goety Delight] Scanning all mob loot tables...");

        LootDataManager lootDataManager = server.getLootData();
        int scannedCount = 0;

        for (EntityType<?> entityType : BuiltInRegistries.ENTITY_TYPE) {
            if (!LivingEntity.class.isAssignableFrom(entityType.getBaseClass())) {
                continue;
            }

            ResourceLocation lootTableLocation = entityType.getDefaultLootTable();

            try {
                Set<ItemStack> drops = getOrParseLootTable(lootDataManager, lootTableLocation);
                addDropsToEntity(entityType, drops);
                scannedCount++;
            } catch (Exception e) {
                System.err.println("[Goety Delight] Failed to scan LootTable: " + lootTableLocation);
                e.printStackTrace();
            }
        }

        System.out.println("[Goety Delight] Scanned " + scannedCount + " mob loot tables");
    }

    /**
     * 获取或解析战利品表
     */
    private static Set<ItemStack> getOrParseLootTable(
            LootDataManager lootDataManager,
            ResourceLocation lootTableLocation
    ) {
        Set<ItemStack> cached = TABLE_DELIGHT_DROPS.get(lootTableLocation);
        if (cached != null) {
            return cached;
        }

        synchronized (TABLE_DELIGHT_DROPS) {
            cached = TABLE_DELIGHT_DROPS.get(lootTableLocation);
            if (cached != null) {
                return cached;
            }

            LootTable lootTable = lootDataManager.getLootTable(lootTableLocation);
            Set<ItemStack> possibleDrops = extractDelightItems(lootTable);

            TABLE_DELIGHT_DROPS.put(lootTableLocation, possibleDrops);
            PROCESSED_TABLES.add(lootTableLocation);

            return possibleDrops;
        }
    }

    /**
     * 提取战利品表中的 Delight 物品
     */
    private static Set<ItemStack> extractDelightItems(LootTable lootTable) {
        Set<ItemStack> possibleDrops = new LinkedHashSet<>();

        try {
            List<LootPool> pools = lootTable.pools;

            for (LootPool pool : pools) {
                extractItemsFromPool(pool, possibleDrops);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        Set<ItemStack> delightItems = new LinkedHashSet<>();
        for (ItemStack itemStack : possibleDrops) {
            if (isDelightItem(itemStack)) {
                delightItems.add(itemStack.copy());
            }
        }

        return delightItems;
    }

    /**
     * 从战利品池中提取物品
     */
    private static void extractItemsFromPool(LootPool pool, Set<ItemStack> possibleDrops) {
        try {
            LootPoolEntryContainer[] entries = pool.entries;

            for (LootPoolEntryContainer entry : entries) {
                extractItemsFromEntry(entry, possibleDrops);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 从战利品条目中提取物品
     */
    private static void extractItemsFromEntry(LootPoolEntryContainer entry, Set<ItemStack> possibleDrops) {
        try {
            if (entry instanceof LootItem lootItem) {
                Item item = lootItem.item.asItem();
                possibleDrops.add(new ItemStack(item));
            } else if (entry instanceof TagEntry tagEntry) {
                TagKey<Item> tagKey = tagEntry.tag;
                extractItemsFromTag(tagKey, possibleDrops);
            } else if (entry instanceof CompositeEntryBase compositeEntry) {
                LootPoolEntryContainer[] children = compositeEntry.children;

                for (LootPoolEntryContainer child : children) {
                    extractItemsFromEntry(child, possibleDrops);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 从标签中提取物品
     */
    private static void extractItemsFromTag(TagKey<Item> tagKey, Set<ItemStack> possibleDrops) {
        try {
            BuiltInRegistries.ITEM
                    .getTagOrEmpty(tagKey)
                    .forEach(itemHolder ->
                            possibleDrops.add(new ItemStack(itemHolder.value()))
                    );
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 扫描 Delight 相关的战利品修改器
     */
    private static void scanDelightLootModifiers(MinecraftServer server) {
        System.out.println("[Goety Delight] Scanning Delight loot modifiers...");

        ResourceManager resourceManager = server.getResourceManager();
        Map<ResourceLocation, List<Resource>> resources;

        try {
            resources = resourceManager.listResourceStacks(
                    "loot_modifiers",
                    location -> location.getPath().endsWith(".json")
            );
        } catch (Exception e) {
            System.err.println("[Goety Delight] Failed to list loot modifiers.");
            e.printStackTrace();
            return;
        }

        int modifierCount = 0;
        for (Map.Entry<ResourceLocation, List<Resource>> entry : resources.entrySet()) {
            ResourceLocation location = entry.getKey();
            String modId = location.getNamespace();

            if (!modId.toLowerCase(Locale.ROOT).contains("delight")) {
                continue;
            }

            if (!location.getPath().startsWith("loot_modifiers/")) {
                continue;
            }

            for (Resource resource : entry.getValue()) {
                scanLootModifierJson(resource, location);
                modifierCount++;
            }
        }

        System.out.println("[Goety Delight] Scanned " + modifierCount + " Delight loot modifiers");
    }

    /**
     * 扫描战利品修改器 JSON
     */
    private static void scanLootModifierJson(Resource resource, ResourceLocation fileLocation) {
        try (Reader reader = resource.openAsReader()) {
            JsonElement root = JsonParser.parseReader(reader);
            if (!root.isJsonObject()) {
                return;
            }

            ResourceLocation itemId = findItem(root);
            if (itemId == null) {
                return;
            }

            scanEntityProperties(root, itemId);
        } catch (Exception e) {
            System.err.println("[Goety Delight] Failed to read Loot Modifier: " + fileLocation);
            e.printStackTrace();
        }
    }

    /**
     * 在 JSON 中查找物品
     */
    private static ResourceLocation findItem(JsonElement element) {
        if (element == null) {
            return null;
        }

        if (element.isJsonObject()) {
            JsonObject object = element.getAsJsonObject();

            if (object.has("item")) {
                ResourceLocation item = parseConcreteItem(object.get("item"));
                if (item != null) {
                    return item;
                }
            }

            for (Map.Entry<String, JsonElement> entry : object.entrySet()) {
                String key = entry.getKey();
                if ("item".equals(key)) {
                    continue;
                }

                if (!key.toLowerCase(Locale.ROOT).contains("item")) {
                    continue;
                }

                ResourceLocation item = parseConcreteItem(entry.getValue());
                if (item != null) {
                    return item;
                }
            }

            for (JsonElement child : object.entrySet().stream().map(Map.Entry::getValue).toList()) {
                ResourceLocation item = findItem(child);
                if (item != null) {
                    return item;
                }
            }
        } else if (element.isJsonArray()) {
            JsonArray array = element.getAsJsonArray();
            for (JsonElement child : array) {
                ResourceLocation item = findItem(child);
                if (item != null) {
                    return item;
                }
            }
        }

        return null;
    }

    /**
     * 解析具体物品
     */
    private static ResourceLocation parseConcreteItem(JsonElement element) {
        if (element == null || !element.isJsonPrimitive() || !element.getAsJsonPrimitive().isString()) {
            return null;
        }

        String value = element.getAsString().trim();
        if (value.isEmpty() || value.startsWith("#")) {
            return null;
        }

        ResourceLocation itemId;
        try {
            itemId = ResourceLocation.parse(value);
        } catch (Exception ignored) {
            return null;
        }

        if (!BuiltInRegistries.ITEM.containsKey(itemId)) {
            return null;
        }

        return itemId;
    }

    /**
     * 扫描实体属性
     */
    private static void scanEntityProperties(JsonElement element, ResourceLocation itemId) {
        if (element == null) {
            return;
        }

        if (element.isJsonObject()) {
            JsonObject object = element.getAsJsonObject();

            if (isEntityPropertiesCondition(object)) {
                JsonObject predicate = object.getAsJsonObject("predicate");
                JsonElement typeElement = predicate.get("type");

                if (typeElement != null && typeElement.isJsonPrimitive() && typeElement.getAsJsonPrimitive().isString()) {
                    String type = typeElement.getAsString();
                    try {
                        ResourceLocation entityId = ResourceLocation.parse(type);
                        if (BuiltInRegistries.ENTITY_TYPE.containsKey(entityId)) {
                            EntityType<?> entityType = BuiltInRegistries.ENTITY_TYPE.get(entityId);
                            addDrop(entityType, itemId);
                        }
                    } catch (Exception e) {
                        System.err.println("[Goety Delight] Failed to parse entity type: " + type);
                        e.printStackTrace();
                    }
                }
            }

            for (JsonElement child : object.entrySet().stream().map(Map.Entry::getValue).toList()) {
                scanEntityProperties(child, itemId);
            }
        } else if (element.isJsonArray()) {
            for (JsonElement child : element.getAsJsonArray()) {
                scanEntityProperties(child, itemId);
            }
        }
    }

    /**
     * 检查是否为实体属性条件
     */
    private static boolean isEntityPropertiesCondition(JsonObject object) {
        JsonElement condition = object.get("condition");
        if (condition == null || !condition.isJsonPrimitive() || !condition.getAsJsonPrimitive().isString()) {
            return false;
        }

        if (!"minecraft:entity_properties".equals(condition.getAsString())) {
            return false;
        }

        JsonElement entity = object.get("entity");
        if (entity == null || !entity.isJsonPrimitive() || !entity.getAsJsonPrimitive().isString()) {
            return false;
        }

        if (!"this".equals(entity.getAsString())) {
            return false;
        }

        JsonElement predicate = object.get("predicate");
        if (predicate == null || !predicate.isJsonObject()) {
            return false;
        }

        JsonElement type = predicate.getAsJsonObject().get("type");
        return type != null && type.isJsonPrimitive() && type.getAsJsonPrimitive().isString();
    }

    /**
     * 添加掉落物
     */
    private static void addDrop(EntityType<?> entityType, ResourceLocation itemId) {
        Item item = BuiltInRegistries.ITEM.get(itemId);

        addDrop(entityType, new ItemStack(item));
    }

    private static void addDrop(EntityType<?> entityType, ItemStack itemStack) {
        if (itemStack == null || itemStack.isEmpty()) {
            return;
        }

        MOB_DELIGHT_DROPS
                .computeIfAbsent(entityType, ignored -> ConcurrentHashMap.newKeySet())
                .add(itemStack.copy());
    }

    private static void addDropsToEntity(EntityType<?> entityType, Collection<ItemStack> drops) {
        for (ItemStack itemStack : drops) {
            addDrop(entityType, itemStack);
        }
    }

    /**
     * 检查是否为 Delight 物品
     */
    private static boolean isDelightItem(ItemStack itemStack) {
        if (itemStack == null || itemStack.isEmpty()) {
            return false;
        }

        ResourceLocation itemId = BuiltInRegistries.ITEM.getKey(itemStack.getItem());

        return itemId.getNamespace().toLowerCase(Locale.ROOT).contains("delight");
    }

    /**
     * 获取生物的 Delight 掉落物
     */
    public static List<ItemStack> getDelightDropsForMob(EntityType<?> entityType) {
        Set<ItemStack> drops = MOB_DELIGHT_DROPS.get(entityType);
        if (drops == null || drops.isEmpty()) {
            return Collections.emptyList();
        }

        List<ItemStack> result = new ArrayList<>(drops.size());
        for (ItemStack stack : drops) {
            result.add(stack.copy());
        }

        return Collections.unmodifiableList(result);
    }

    /**
     * 获取所有生物的 Delight 掉落物
     */
    public static Map<EntityType<?>, List<ItemStack>> getAllDelightDrops() {
        Map<EntityType<?>, List<ItemStack>> result = new HashMap<>();

        for (Map.Entry<EntityType<?>, Set<ItemStack>> entry : MOB_DELIGHT_DROPS.entrySet()) {
            List<ItemStack> drops = new ArrayList<>();
            for (ItemStack stack : entry.getValue()) {
                drops.add(stack.copy());
            }

            result.put(entry.getKey(), Collections.unmodifiableList(drops));
        }

        return Collections.unmodifiableMap(result);
    }

    /**
     * 检查生物是否有 Delight 掉落物
     */
    public static boolean hasDelightDrops(EntityType<?> entityType) {
        Set<ItemStack> drops = MOB_DELIGHT_DROPS.get(entityType);
        return drops != null && !drops.isEmpty();
    }

    /**
     * 检查生物是否有特定的 Delight 掉落物
     */
    public static boolean hasDelightDrop(EntityType<?> entityType, Item item) {
        Set<ItemStack> drops = MOB_DELIGHT_DROPS.get(entityType);
        if (drops == null) {
            return false;
        }

        for (ItemStack stack : drops) {
            if (stack.is(item)) {
                return true;
            }
        }

        return false;
    }

    /**
     * 检查缓存是否已加载
     */
    public static boolean isLoaded() {
        return isLoaded;
    }

    /**
     * 清理缓存
     */
    public static void clearCache() {
        MOB_DELIGHT_DROPS.clear();
        PROCESSED_TABLES.clear();
        TABLE_DELIGHT_DROPS.clear();
        isLoaded = false;

        System.out.println("[Goety Delight] Cache cleared");
    }
}