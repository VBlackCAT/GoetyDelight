package net.v_black_cat.goetydelight.item.food;

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
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.living.LivingAttackEvent;
import net.minecraftforge.event.entity.living.LivingEntityUseItemEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.config.ModConfigEvent;
import net.minecraftforge.registries.ForgeRegistries;
import net.v_black_cat.goetydelight.config.Config;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Random;

@Mod.EventBusSubscriber(modid = "goetydelight")
public class PolariceItem extends BowlFoodItem {
    private static final Logger log = LoggerFactory.getLogger(PolariceItem.class);

    public PolariceItem(Properties properties) {
        super(properties);
    }

    private static final String POARICE_TAG = "ploarice_tag";
    static float polarice_time = 0.0f;
    static int polarice_count;
    private static long polarice_cooldown = 0;
    @SubscribeEvent
    public static void onConfigLoad(ModConfigEvent.Loading event) {
        if (event.getConfig().getModId().equals("goetydelight")) {
            polarice_count = Config.getPolariceCount();
            polarice_cooldown = Config.getPolariceCooldown() * 20L;
        }
    }

    @SubscribeEvent
    public static void onItemUseFinish(LivingEntityUseItemEvent.Finish event) {
        if (event.getItem().getItem() instanceof PolariceItem) {
            polarice_time = 1200.0f;
            LivingEntity entity = event.getEntity();
            CompoundTag tag = entity.getPersistentData();
            tag.putFloat(POARICE_TAG, polarice_time);
            polarice_count = Config.getPolariceCount();
        }
    }
    private long lastEatTime = 0;
    @Override
    public ItemStack finishUsingItem(ItemStack stack, Level level, LivingEntity entity) {
        if (!level.isClientSide && entity instanceof Player player) {
            long currentTime = level.getGameTime();
            if (currentTime- lastEatTime <= polarice_cooldown) {
                return super.finishUsingItem(stack, level, entity);
            }else {
                lastEatTime = level.getGameTime();
            }
        }
        return super.finishUsingItem(stack, level, entity);
    }


    @SubscribeEvent
    public static void onPlayerTick(TickEvent.PlayerTickEvent event) {
        if (event.phase != TickEvent.Phase.END) return;
        Player player = event.player;
        Level level = player.level();
        if (!level.isClientSide) {
            CompoundTag persistentData = player.getPersistentData();
            if (persistentData.contains(POARICE_TAG)) {
                float remainingTime = persistentData.getFloat(POARICE_TAG);
                if (remainingTime > 0) {
                    persistentData.putFloat(POARICE_TAG, remainingTime - 1);
                } else {
                    persistentData.remove(POARICE_TAG);
                }
            }
        }
    }

    @SubscribeEvent
    public static void onAttackEvent(LivingAttackEvent event) {
        LivingEntity targetEntity = event.getEntity();
        Entity attacker = event.getSource().getEntity();
        boolean ischange = false;
        boolean isAffectedByPolarice = true;
        boolean isBanEntity = false;
        boolean whetherchange = false;
        double targetMaxHealth = targetEntity.getMaxHealth();
        double targetHealth = targetEntity.getHealth();
        float randomchange = (float) (0.1f + targetHealth/targetMaxHealth);
        ResourceLocation entityId = ForgeRegistries.ENTITY_TYPES.getKey(targetEntity.getType());
        if (
            entityId.equals(new ResourceLocation("goety:vizier_clone")) ||
            entityId.equals(new ResourceLocation("minecraft:ender_dragon")) ||
            entityId.equals(new ResourceLocation("goety:ender_keeper")) ||
            entityId.equals(new ResourceLocation("goety:obsidian_monolith")) ||
            entityId.equals(new ResourceLocation("twilightforest:lich"))
        ) {
            isBanEntity = true;
        }
        if(targetEntity instanceof com.Polarice3.Goety.common.entities.boss.Apostle){
            if ((targetEntity.level() instanceof ServerLevel level)){
                if (attacker != null && polarice_time > 0 && attacker.getPersistentData().contains(POARICE_TAG)) {
                    Random random = new Random();
                    if (random.nextFloat() < randomchange) {
                        whetherchange = true;
                    }
                    if (targetEntity instanceof com.Polarice3.Goety.common.entities.boss.Apostle) {
                        targetEntity.remove(Entity.RemovalReason.DISCARDED);
                        Villager villager = EntityType.VILLAGER.create(level);
                        if (villager != null) {
                            villager.moveTo(targetEntity.getX(), targetEntity.getY(), targetEntity.getZ());
                            level.addFreshEntity(villager);
                        }
                        ItemEntity itemEntity = new ItemEntity(level,
                                targetEntity.getX(),
                                targetEntity.getY()+1,
                                targetEntity.getZ(),
                                new ItemStack(Blocks.CRYING_OBSIDIAN.asItem()));
                        level.addFreshEntity(itemEntity);
                        polarice_count-=1;
                    }
                }
            }
        }
        if (!Config.getPolariceAffectsBosses() && targetEntity.getPersistentData().contains("forge:bosses")) {
            isAffectedByPolarice = false;
        }
        else if (targetEntity.getAttributeBaseValue(Attributes.MAX_HEALTH) > Config.getPolariceHealthThreshold()) {
            isAffectedByPolarice = false;
        }
        else if (isBanEntity){
            isAffectedByPolarice = false;
        }
        else if ((targetEntity.level() instanceof ServerLevel level && isAffectedByPolarice && whetherchange)){
            if (attacker != null && polarice_time > 0 && attacker.getPersistentData().contains(POARICE_TAG)) {
                String entityTypeName = EntityType.getKey(targetEntity.getType()).toString();
                String entityName = entityTypeName.substring(entityTypeName.indexOf(":") + 1);
                String servantTypeName_1 = "entity.goety." + entityName + "_servant";
//                String servantTypeName_2 = "entity.goety_revelation." + entityName + "_servant";
//                String servantTypeName_3 = "entity.goety_iron." + entityName + "_servant";
//                String servantTypeName_4 = "entity.goety_cataclysm." + entityName;
//                String servantTypeName_5 = "entity.goety_spillage." + entityName + "_servant";
//                String servantTypeName_6 = "entity.goety_twilight." + entityName + "_servant";
//                String servantTypeName_7 = "entity.goetyawaken." + entityName + "_servant";
//                String servantTypeName_8 = "entity.vividerusmoregoetysummons." + "servant_"+entityName;

                if (entityName.contains("hostile_")) {
                    entityName = entityName.replace("hostile_", "");
                    servantTypeName_1 = "entity.goety." + entityName;
                }
                log.info("Entity Servant Name: {}", servantTypeName_1);

                if (targetEntity instanceof IOwned owned && owned.getTrueOwner() != attacker && !entityName.equals("lich")) {
                    boolean isOwnerPlayer = owned.getTrueOwner() instanceof Player;
                    if (!isOwnerPlayer) {
                        owned.setTrueOwner((LivingEntity) attacker);
                    }
                }
                LivingEntity servant = null;
                for (EntityType<?> entityType : ForgeRegistries.ENTITY_TYPES.getValues()) {
                    String registryName = entityType.toString();
                    if (registryName.equals(servantTypeName_1)) {
                        ischange = true;
                        servant = (LivingEntity) entityType.create(level);
                        break;
                    }
                }
                if (!ischange) {
                    servantTypeName_1 = entityName + "_servant";
                    for (EntityType<?> entityType : ForgeRegistries.ENTITY_TYPES.getValues()) {
                        String registryName = entityType.toString();
                        if (registryName.contains(servantTypeName_1)) {
                            servant = (LivingEntity) entityType.create(level);
                            ischange = true;
                            break;
                        }
                    }
                    if(!ischange){
                        servantTypeName_1 = entityName;
                        for (EntityType<?> entityType : ForgeRegistries.ENTITY_TYPES.getValues()) {
                            ResourceLocation registryName = ForgeRegistries.ENTITY_TYPES.getKey(entityType);
                            if (registryName != null && registryName.getNamespace().contains("goety") &&
                                    registryName.getPath().contains(servantTypeName_1)) {
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
                    servant.setHealth((float) (servantMaxHealth*targetHealth/targetMaxHealth));
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
                    polarice_count-=1;
                    ischange = false;
                }
            }
        }
    }
}
