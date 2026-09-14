package net.v_black_cat.goetydelight.item.food;

import com.Polarice3.Goety.api.entities.IOwned;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.npc.Villager;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.LivingEntityUseItemEvent;
import net.neoforged.neoforge.event.entity.living.LivingIncomingDamageEvent;
import net.v_black_cat.goetydelight.init.ModAttachments;
import net.v_black_cat.goetydelight.init.ModServerConfig;
import net.minecraft.util.RandomSource;
import net.v_black_cat.goetydelight.util.FoodState;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@EventBusSubscriber(modid = "goetydelight")
public class PolariceItem extends BowlFoodItem {
    private static final Logger log = LoggerFactory.getLogger(PolariceItem.class);

    public PolariceItem(Properties properties) {
        super(properties);
    }

    static int polarice_count;
    /** 极地冰时效：2 秒 * 60 = 1200 tick（原实现是每 20 tick 从 1200 递减到 0）。 */
    private static final int POLARICE_DURATION_TICKS = 1200;

    // 【优化】下面这些原先都写在 onIncomingDamage 方法体里：每命中一次攻击都要
    // ResourceLocation.parse(...)（字符串校验 + 对象分配）并遍历整个实体类型注册表
    // （每个元素一次 getKey(...).toString()）。改成静态常量 + 懒加载缓存，只解析一次。
    private static final Set<ResourceLocation> BAN_ENTITY_IDS = Set.of(
            ResourceLocation.parse("goety:vizier_clone"),
            ResourceLocation.parse("minecraft:ender_dragon"),
            ResourceLocation.parse("goety:ender_keeper"),
            ResourceLocation.parse("goety:obsidian_monolith"),
            ResourceLocation.parse("twilightforest:lich"),
            ResourceLocation.parse("goetyawaken:hostile_mushroom_monstrosity"));

    /** 把上述 id 解析成 EntityType，命中判定退化成一次 Set 查询（不查注册表、不比字符串）。 */
    private static Set<EntityType<?>> banEntityTypes;

    /** 实体类型注册表的 (id, type) 快照；注册表在加载后不再变化，只建一次。 */
    private static List<Map.Entry<ResourceLocation, EntityType<?>>> entityTypeEntries;

    private static Set<EntityType<?>> banEntityTypes() {
        Set<EntityType<?>> cached = banEntityTypes;
        if (cached == null) {
            Set<EntityType<?>> built = new HashSet<>();
            for (ResourceLocation id : BAN_ENTITY_IDS) {
                BuiltInRegistries.ENTITY_TYPE.getOptional(id).ifPresent(built::add);
            }
            banEntityTypes = cached = Set.copyOf(built);
        }
        return cached;
    }

    private static List<Map.Entry<ResourceLocation, EntityType<?>>> entityTypeEntries() {
        List<Map.Entry<ResourceLocation, EntityType<?>>> cached = entityTypeEntries;
        if (cached == null) {
            List<Map.Entry<ResourceLocation, EntityType<?>>> built = new ArrayList<>();
            for (EntityType<?> type : BuiltInRegistries.ENTITY_TYPE) {
                ResourceLocation id = BuiltInRegistries.ENTITY_TYPE.getKey(type);
                if (id != null) {
                    built.add(Map.entry(id, type));
                }
            }
            entityTypeEntries = cached = List.copyOf(built);
        }
        return cached;
    }
    private long lastEatTime = 0;

    

    @SubscribeEvent
    public static void onItemUseFinish(LivingEntityUseItemEvent.Finish event) {
        if (event.getItem().getItem() instanceof PolariceItem) {
            LivingEntity entity = event.getEntity();
            // 【优化】记录绝对到期时间，配合 FoodState#hasActivePolarice(gameTime) 判定，
            // 不再需要每 20 tick 遍历所有玩家做递减。
            entity.getData(ModAttachments.FOOD_STATE)
                    .setPolariceEndTime(entity.level().getGameTime() + POLARICE_DURATION_TICKS);
            polarice_count = ModServerConfig.getPolariceCount();
        }
    }

    @Override
    public ItemStack finishUsingItem(ItemStack stack, Level level, LivingEntity entity) {
        if (!level.isClientSide && entity instanceof Player player) {
            long currentTime = level.getGameTime();
            long cooldown = ModServerConfig.getPolariceCooldown() * 20L;
            if (currentTime - lastEatTime <= cooldown) {
                return super.finishUsingItem(stack, level, entity);
            } else {
                lastEatTime = currentTime;
            }
        }
        return super.finishUsingItem(stack, level, entity);
    }

    private static boolean hasActivePolarice(Entity attacker) {
        if (!(attacker instanceof LivingEntity living)) return false;
        return living.getData(ModAttachments.FOOD_STATE).hasActivePolarice(living.level().getGameTime());
    }

    @SubscribeEvent
    public static void onIncomingDamage(LivingIncomingDamageEvent event) {
        LivingEntity targetEntity = event.getEntity();
        RandomSource random = targetEntity.getRandom();
        Entity attacker = event.getSource().getEntity();
        boolean ischange = false;
        boolean isAffectedByPolarice = true;
        boolean isBanEntity = false;
        boolean whetherchange = false;
        double targetMaxHealth = targetEntity.getMaxHealth();
        double targetHealth = targetEntity.getHealth();
        float randomchange = (float) (1.1f - targetHealth / targetMaxHealth);
        if (random.nextFloat() < randomchange) {
            whetherchange = true;
        }
        if (banEntityTypes().contains(targetEntity.getType())) {
            isBanEntity = true;
        }
        if (targetEntity instanceof com.Polarice3.Goety.common.entities.boss.Apostle) {
            if ((targetEntity.level() instanceof ServerLevel level)) {
                if (hasActivePolarice(attacker)) {
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
                    polarice_count -= 1;
                }
            }
        }
        if (!ModServerConfig.getPolariceAffectsBosses() && targetEntity.getPersistentData().contains("c:bosses")) {
            isAffectedByPolarice = false;
        } else if (targetEntity.getAttributeBaseValue(Attributes.MAX_HEALTH) > ModServerConfig.getPolariceHealthThreshold()) {
            isAffectedByPolarice = false;
        } else if (isBanEntity) {
            isAffectedByPolarice = false;
        } else if ((targetEntity.level() instanceof ServerLevel level && isAffectedByPolarice && whetherchange)) {
            if (hasActivePolarice(attacker)) {
                String entityTypeName = EntityType.getKey(targetEntity.getType()).toString();
                String entityName = entityTypeName.substring(entityTypeName.indexOf(":") + 1);
                String servantTypeName_1 = "entity.goety." + entityName + "_servant";

                if (entityName.contains("hostile_")) {
                    entityName = entityName.replace("hostile_", "");
                    servantTypeName_1 = "entity.goety." + entityName;
                }
                log.info("Entity Servant Name: {}", servantTypeName_1);

                if (targetEntity instanceof IOwned owned && owned.getTrueOwner() != attacker) {
                    boolean isOwnerPlayer = owned.getTrueOwner() instanceof Player;
                    if (!isOwnerPlayer) {
                        owned.setTrueOwner((LivingEntity) attacker);
                    }
                }
                LivingEntity servant = null;
                for (Map.Entry<ResourceLocation, EntityType<?>> entry : entityTypeEntries()) {
                    if (entry.getKey().toString().equals(servantTypeName_1)) {
                        ischange = true;
                        servant = (LivingEntity) entry.getValue().create(level);
                        break;
                    }
                }
                if (!ischange) {
                    servantTypeName_1 = entityName + "_servant";
                    for (Map.Entry<ResourceLocation, EntityType<?>> entry : entityTypeEntries()) {
                        if (entry.getKey().toString().contains(servantTypeName_1)) {
                            servant = (LivingEntity) entry.getValue().create(level);
                            ischange = true;
                            break;
                        }
                    }
                    if (!ischange) {
                        servantTypeName_1 = entityName;
                        for (Map.Entry<ResourceLocation, EntityType<?>> entry : entityTypeEntries()) {
                            ResourceLocation registryName = entry.getKey();
                            EntityType<?> entityType = entry.getValue();
                            if (registryName.getNamespace().contains("goety") &&
                                    registryName.getPath().contains(servantTypeName_1) && !entityName.equals("lich")) {
                                LivingEntity tempEntity = (LivingEntity) entityType.create(level);
                                if (tempEntity != null) {
                                    double servantMaxHealth = tempEntity.getAttributeBaseValue(Attributes.MAX_HEALTH);
                                    tempEntity.setRemoved(Entity.RemovalReason.DISCARDED);
                                    if (servantMaxHealth <= targetMaxHealth * 1.2 && servantMaxHealth >= targetMaxHealth * 0.7) {
                                        servant = (LivingEntity) entityType.create(level);
                                        ischange = true;
                                        break;
                                    }
                                }
                            }
                        }
                    }
                }
                if (servant != null) {
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
                    polarice_count -= 1;
                    ischange = false;
                }
            }
        }
    }
}