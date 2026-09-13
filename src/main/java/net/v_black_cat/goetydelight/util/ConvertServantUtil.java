package net.v_black_cat.goetydelight.util;

import com.Polarice3.Goety.api.entities.IOwned;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.npc.Villager;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Blocks;
import net.minecraftforge.event.entity.living.LivingAttackEvent;
import net.minecraftforge.registries.ForgeRegistries;
import net.v_black_cat.goetydelight.config.Config;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ThreadLocalRandom;

/**
 * 极地之触相关逻辑的工具类。
 * <p>
 * 承载：效果时长管理、攻击事件处理、限制条件判定、实体转化等全部逻辑。
 */
public final class ConvertServantUtil {

    private static final Logger log = LoggerFactory.getLogger(ConvertServantUtil.class);

    private ConvertServantUtil() {
    }

    /** 玩家持久化数据中记录极地之触剩余时长的 NBT 键 */
    public static final String POLARICE_TAG = "ploarice_tag";

    /** 禁止转化的实体黑名单，静态初始化，避免每次攻击重复创建 ResourceLocation */
    private static final Set<ResourceLocation> BANNED_ENTITIES = Set.of(
            new ResourceLocation("goety:vizier_clone"),
            new ResourceLocation("goety:apostle"),
            new ResourceLocation("minecraft:ender_dragon"),
            new ResourceLocation("goety:ender_keeper"),
            new ResourceLocation("goety:obsidian_monolith"),
            new ResourceLocation("twilightforest:lich"),
            new ResourceLocation("goetyawaken:hostile_mushroom_monstrosity")
    );

    /** 仆从实体查找缓存，key 为 "servantTypeName|entityName" */
    private static final Map<String, Optional<EntityType<?>>> SERVANT_CACHE = new ConcurrentHashMap<>();

    /** 当前极地之触效果总时长（tick） */
    private static float polariceTime = 0.0f;

    /** 剩余可转化次数 */
    private static int polariceCount;

    /** 两次食用之间的冷却（tick） */
    private static long polariceCooldown = 0;

    /** 配置项缓存，避免每次攻击都读取 Config */
    private static boolean cachedAffectsBosses = true;
    private static double cachedHealthThreshold = 0.0;

    // ==================== 配置与状态 ====================

    /**
     * 配置加载/重载时刷新缓存的配置项与冷却、次数。
     */
    public static void onConfigLoad() {
        polariceCount = Config.getPolariceCount();
        polariceCooldown = Config.getPolariceCooldown() * 20L;
        cachedAffectsBosses = Config.getPolariceAffectsBosses();
        cachedHealthThreshold = Config.getPolariceHealthThreshold();
    }

    /**
     * 食用极地之触时触发：重置效果时长并刷新次数。
     *
     * @param entity 食用者
     */
    public static void onEaten(LivingEntity entity) {
        polariceTime = 1200.0f;
        CompoundTag tag = entity.getPersistentData();
        tag.putFloat(POLARICE_TAG, polariceTime);
        polariceCount = Config.getPolariceCount();
    }

    /**
     * 获取当前食用冷却（tick）。
     */
    public static long getPolariceCooldown() {
        return polariceCooldown;
    }

    /**
     * 玩家每 tick 递减极地之触剩余时长。
     *
     * @param player 玩家
     */
    public static void tickPlayer(Player player) {
        CompoundTag persistentData = player.getPersistentData();
        if (persistentData.contains(POLARICE_TAG)) {
            float remainingTime = persistentData.getFloat(POLARICE_TAG);
            if (remainingTime > 0) {
                persistentData.putFloat(POLARICE_TAG, remainingTime - 1);
            } else {
                persistentData.remove(POLARICE_TAG);
            }
        }
    }

    /**
     * 判断攻击者当前是否处于极地之触生效状态。
     */
    private static boolean isPolariceActive(Entity attacker) {
        return attacker != null
                && polariceTime > 0
                && attacker.getPersistentData().contains(POLARICE_TAG);
    }

    // ==================== 攻击事件主流程 ====================

    /**
     * 处理极地之触攻击事件的核心逻辑。
     * 包含：Boss 特殊处理、限制条件判定、概率判定、以及将低血量敌人转化为仆从。
     *
     * @param event 攻击事件
     */
    public static void handleAttack(LivingAttackEvent event) {
        LivingEntity targetEntity = event.getEntity();
        Entity attacker = event.getSource().getEntity();

        // 对 Apostle 的特殊处理：直接移除并生成村民 + 哭泣的黑曜石
        handleApostleSpecialCase(targetEntity, attacker);

        // 限制条件判定：是否允许被转化
        if (!canBeAffectedByPolarice(targetEntity)) {
            return;
        }

        double targetMaxHealth = targetEntity.getMaxHealth();
        double targetHealth = targetEntity.getHealth();

        // 概率判定
        if (!shouldTransform(targetHealth, targetMaxHealth)) {
            return;
        }

        // 执行转化
        if (targetEntity.level() instanceof ServerLevel level && isPolariceActive(attacker)) {
            transformToServant(targetEntity, attacker, level, targetMaxHealth, targetHealth);
        }
    }

    // ==================== 限制条件 ====================

    /**
     * 限制条件判定：判断目标实体是否允许被极地之触影响并转化为仆从。
     *
     * @param targetEntity 被攻击的目标实体
     * @return true 表示允许转化，false 表示禁止转化
     */
    public static boolean canBeAffectedByPolarice(LivingEntity targetEntity) {
        // 配置项：极地之触是否影响 Boss
        if (!cachedAffectsBosses && targetEntity.getPersistentData().contains("forge:bosses")) {
            return false;
        }

        // 血量超过阈值则不受影响
        if (targetEntity.getAttributeBaseValue(Attributes.MAX_HEALTH) > cachedHealthThreshold) {
            return false;
        }

        // 黑名单实体不受影响
        return !isBannedEntity(targetEntity);
    }

    /**
     * 判断目标实体是否属于禁止转化的黑名单。O(1) 复杂度。
     *
     * @param targetEntity 被攻击的目标实体
     * @return true 表示是黑名单实体，禁止转化
     */
    public static boolean isBannedEntity(LivingEntity targetEntity) {
        ResourceLocation entityId = ForgeRegistries.ENTITY_TYPES.getKey(targetEntity.getType());
        return entityId != null && BANNED_ENTITIES.contains(entityId);
    }

    // ==================== 概率与特殊处理 ====================

    /**
     * 概率判定：根据目标当前血量比例决定是否触发转化。
     * 血量越低，触发概率越高。
     *
     * @param targetHealth    目标当前血量
     * @param targetMaxHealth 目标最大血量
     * @return true 表示本次攻击触发转化
     */
    public static boolean shouldTransform(double targetHealth, double targetMaxHealth) {
        float randomchange = (float) (1.1f - targetHealth / targetMaxHealth);
        return ThreadLocalRandom.current().nextFloat() < randomchange;
    }

    /**
     * 对 Apostle 的特殊处理：移除本体，生成村民并掉落哭泣的黑曜石。
     *
     * @param targetEntity 被攻击的目标实体
     * @param attacker     攻击者
     */
    private static void handleApostleSpecialCase(LivingEntity targetEntity, Entity attacker) {
        if (!(targetEntity instanceof com.Polarice3.Goety.common.entities.boss.Apostle)) {
            return;
        }

        if (!(targetEntity.level() instanceof ServerLevel level)) {
            return;
        }

        if (!isPolariceActive(attacker)) {
            return;
        }

        targetEntity.remove(Entity.RemovalReason.DISCARDED);

        Villager villager = EntityType.VILLAGER.create(level);
        if (villager != null) {
            villager.moveTo(targetEntity.getX(), targetEntity.getY(), targetEntity.getZ());
            level.addFreshEntity(villager);
        }

        ItemEntity itemEntity = new ItemEntity(level,
                targetEntity.getX(),
                targetEntity.getY() + 1,
                targetEntity.getZ(),
                new ItemStack(Blocks.CRYING_OBSIDIAN.asItem()));
        level.addFreshEntity(itemEntity);

        polariceCount -= 1;
    }

    // ==================== 转化逻辑 ====================

    /**
     * 将目标实体转化为对应的仆从实体，并继承血量比例与装备。
     *
     * @param targetEntity    被攻击的目标实体
     * @param attacker        攻击者
     * @param level           服务端世界
     * @param targetMaxHealth 目标最大血量
     * @param targetHealth    目标当前血量
     */
    public static void transformToServant(LivingEntity targetEntity, Entity attacker, ServerLevel level,
                                          double targetMaxHealth, double targetHealth) {
        String entityTypeName = EntityType.getKey(targetEntity.getType()).toString();
        String entityName = entityTypeName.substring(entityTypeName.indexOf(":") + 1);
        String servantTypeName = "entity.goety." + entityName + "_servant";

        if (entityName.contains("hostile_")) {
            entityName = entityName.replace("hostile_", "");
            servantTypeName = "entity.goety." + entityName;
        }

        // 若目标已被其他非玩家拥有者拥有，则转移拥有权
        if (targetEntity instanceof IOwned owned && owned.getTrueOwner() != attacker) {
            boolean isOwnerPlayer = owned.getTrueOwner() instanceof Player;
            if (!isOwnerPlayer) {
                owned.setTrueOwner((LivingEntity) attacker);
            }
        }

        LivingEntity servant = findServantEntity(level, entityName, servantTypeName, targetMaxHealth);

        if (servant == null) {
            return;
        }

        // 生成仆从并继承目标血量比例与装备
        double servantMaxHealth = servant.getMaxHealth();
        servant.moveTo(targetEntity.getX(), targetEntity.getY(), targetEntity.getZ());
        servant.setHealth((float) (servantMaxHealth * targetHealth / targetMaxHealth));

        if (servant instanceof IOwned ownedServant) {
            ownedServant.setTrueOwner((LivingEntity) attacker);
        }

        for (EquipmentSlot slot : EquipmentSlot.values()) {
            ItemStack equipment = targetEntity.getItemBySlot(slot);
            if (!equipment.isEmpty()) {
                servant.setItemSlot(slot, equipment.copy());
            }
        }

        targetEntity.setRemoved(Entity.RemovalReason.DISCARDED);
        level.addFreshEntity(servant);
        polariceCount -= 1;
    }

    /**
     * 按优先级查找目标对应的仆从实体类型，并带缓存。
     * 优先级：
     * 1. 精确匹配 "entity.goety.xxx_servant"
     * 2. 模糊匹配包含 "xxx_servant" 的注册名
     * 3. 匹配 goety 命名空间下名称包含目标名、且血量在目标 0.7~1.2 倍之间的实体
     *
     * @param level           服务端世界
     * @param entityName      目标实体名（已去除 hostile_ 前缀）
     * @param servantTypeName 第一优先级使用的仆从翻译键
     * @param targetMaxHealth 目标最大血量
     * @return 找到的仆从实体，未找到返回 null
     */
    public static LivingEntity findServantEntity(ServerLevel level, String entityName,
                                                 String servantTypeName, double targetMaxHealth) {
        // 第一、二优先级：合并为一次遍历，缓存结果
        String cacheKey = servantTypeName + "|" + entityName;

        EntityType<?> cachedType = SERVANT_CACHE
                .computeIfAbsent(cacheKey, k -> findServantTypeByKey(entityName, servantTypeName))
                .orElse(null);

        if (cachedType != null) {
            return (LivingEntity) cachedType.create(level);
        }

        // 第三优先级：按名称匹配且血量在目标 0.7~1.2 倍之间（依赖 targetMaxHealth，无法缓存）
        for (EntityType<?> entityType : ForgeRegistries.ENTITY_TYPES.getValues()) {
            ResourceLocation registryName = ForgeRegistries.ENTITY_TYPES.getKey(entityType);
            if (registryName != null && registryName.getNamespace().contains("goety") &&
                    registryName.getPath().contains(entityName) && !entityName.equals("lich")) {
                LivingEntity tempEntity = (LivingEntity) entityType.create(level);
                if (tempEntity != null) {
                    double servantMaxHealth = tempEntity.getAttributeBaseValue(Attributes.MAX_HEALTH);
                    tempEntity.setRemoved(Entity.RemovalReason.DISCARDED);
                    if (servantMaxHealth <= targetMaxHealth * 1.2 && servantMaxHealth >= targetMaxHealth * 0.7) {
                        return (LivingEntity) entityType.create(level);
                    }
                }
            }
        }

        return null;
    }

    /**
     * 执行第一、二优先级的查找（不依赖目标血量，可缓存）。
     *
     * @param entityName      目标实体名
     * @param servantTypeName 第一优先级使用的仆从翻译键
     * @return 查找到的实体类型，未找到返回 Optional.empty()
     */
    private static Optional<EntityType<?>> findServantTypeByKey(String entityName, String servantTypeName) {
        // 第一优先级：按 "entity.goety.xxx_servant" 格式查找
        for (EntityType<?> entityType : ForgeRegistries.ENTITY_TYPES.getValues()) {
            if (entityType.toString().equals(servantTypeName)) {
                return Optional.of(entityType);
            }
        }

        // 第二优先级：按 "xxx_servant" 模糊匹配
        String fuzzyName = entityName + "_servant";
        for (EntityType<?> entityType : ForgeRegistries.ENTITY_TYPES.getValues()) {
            if (entityType.toString().contains(fuzzyName)) {
                return Optional.of(entityType);
            }
        }

        return Optional.empty();
    }
}