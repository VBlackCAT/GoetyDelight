package net.v_black_cat.goetydelight.item.food;

import com.Polarice3.Goety.api.entities.IOwned;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
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
import net.neoforged.neoforge.event.tick.ServerTickEvent;
import net.v_black_cat.goetydelight.init.ModAttachments;
import net.v_black_cat.goetydelight.init.ModConfig;
import net.v_black_cat.goetydelight.init.ModItems;
import net.minecraft.util.RandomSource;
import net.v_black_cat.goetydelight.util.FoodState;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@EventBusSubscriber(modid = "goetydelight")
public class PolariceItem extends BowlFoodItem {
    private static final Logger log = LoggerFactory.getLogger(PolariceItem.class);

    public PolariceItem(Properties properties) {
        super(properties);
    }

    static int polarice_count;
    private long lastEatTime = 0;

    

    @SubscribeEvent
    public static void onItemUseFinish(LivingEntityUseItemEvent.Finish event) {
        if (event.getItem().getItem() instanceof PolariceItem) {
            LivingEntity entity = event.getEntity();
            entity.getData(ModAttachments.FOOD_STATE).setPolariceTime(1200.0f);
            polarice_count = ModConfig.getPolariceCount();
        }
    }

    @Override
    public ItemStack finishUsingItem(ItemStack stack, Level level, LivingEntity entity) {
        if (!level.isClientSide && entity instanceof Player player) {
            long currentTime = level.getGameTime();
            long cooldown = ModConfig.getPolariceCooldown() * 20L;
            if (currentTime - lastEatTime <= cooldown) {
                return super.finishUsingItem(stack, level, entity);
            } else {
                lastEatTime = currentTime;
            }
        }
        return super.finishUsingItem(stack, level, entity);
    }

    @SubscribeEvent
    public static void onServerTickPost(ServerTickEvent.Post event) {
        MinecraftServer server = event.getServer();
        // 【优化】每 20 tick 批量衰减 20，避免每 tick 遍历全部玩家（衰减速率不变）
        if (server.getTickCount() % 20 != 0) return;
        for (Player player : server.getPlayerList().getPlayers()) {
            Level level = player.level();
            if (level.isClientSide) continue;

            FoodState state = player.getData(ModAttachments.FOOD_STATE);
            if (state.getPolariceTime() > 0) {
                state.setPolariceTime(state.getPolariceTime() - 20);
            }
        }
    }

    private static boolean hasActivePolarice(Entity attacker) {
        if (!(attacker instanceof LivingEntity living)) return false;
        return living.getData(ModAttachments.FOOD_STATE).getPolariceTime() > 0;
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
        ResourceLocation entityId = BuiltInRegistries.ENTITY_TYPE.getKey(targetEntity.getType());
        if (random.nextFloat() < randomchange) {
            whetherchange = true;
        }
        if (
                entityId.equals(ResourceLocation.parse("goety:vizier_clone")) ||
                entityId.equals(ResourceLocation.parse("minecraft:ender_dragon")) ||
                entityId.equals(ResourceLocation.parse("goety:ender_keeper")) ||
                entityId.equals(ResourceLocation.parse("goety:obsidian_monolith")) ||
                entityId.equals(ResourceLocation.parse("twilightforest:lich")) ||
                entityId.equals(ResourceLocation.parse("goetyawaken:hostile_mushroom_monstrosity"))
        ) {
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
        if (!ModConfig.getPolariceAffectsBosses() && targetEntity.getPersistentData().contains("c:bosses")) {
            isAffectedByPolarice = false;
        } else if (targetEntity.getAttributeBaseValue(Attributes.MAX_HEALTH) > ModConfig.getPolariceHealthThreshold()) {
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
                for (EntityType<?> entityType : BuiltInRegistries.ENTITY_TYPE) {
                    String registryName = BuiltInRegistries.ENTITY_TYPE.getKey(entityType).toString();
                    if (registryName.equals(servantTypeName_1)) {
                        ischange = true;
                        servant = (LivingEntity) entityType.create(level);
                        break;
                    }
                }
                if (!ischange) {
                    servantTypeName_1 = entityName + "_servant";
                    for (EntityType<?> entityType : BuiltInRegistries.ENTITY_TYPE) {
                        String registryName = BuiltInRegistries.ENTITY_TYPE.getKey(entityType).toString();
                        if (registryName.contains(servantTypeName_1)) {
                            servant = (LivingEntity) entityType.create(level);
                            ischange = true;
                            break;
                        }
                    }
                    if (!ischange) {
                        servantTypeName_1 = entityName;
                        for (EntityType<?> entityType : BuiltInRegistries.ENTITY_TYPE) {
                            ResourceLocation registryName = BuiltInRegistries.ENTITY_TYPE.getKey(entityType);
                            if (registryName != null && registryName.getNamespace().contains("goety") &&
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