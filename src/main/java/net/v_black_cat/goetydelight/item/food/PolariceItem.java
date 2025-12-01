package net.v_black_cat.goetydelight.item.food;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.Level;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.living.LivingAttackEvent;
import net.minecraftforge.event.entity.living.LivingEntityUseItemEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.ForgeRegistries;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Objects;
import java.util.Optional;

@Mod.EventBusSubscriber(modid = "goetydelight")
public class PolariceItem extends Item {
    private static final Logger log = LoggerFactory.getLogger(PolariceItem.class);

    public PolariceItem(Item.Properties properties) {
        super(properties);
    }

    private static final String POARICE_TAG = "ploarice_tag";
    static float ploarice_time= 0.0f;

    @SubscribeEvent
    public static void onItemUseFinish(LivingEntityUseItemEvent.Finish event) {
        if (event.getItem().getItem() instanceof PolariceItem) {
            ploarice_time = 600.0f;
            LivingEntity entity = event.getEntity();
            CompoundTag tag = entity.getPersistentData();
            tag.putFloat(POARICE_TAG, ploarice_time);
        }
    }

    @SubscribeEvent
    public static void onPlayerTick(TickEvent.PlayerTickEvent event) {
        if (event.phase != TickEvent.Phase.END) return;
        Player player = event.player;
        Level level = player.level();
        if (!level.isClientSide) {
            CompoundTag persistentData = player.getPersistentData();
            if (persistentData.getBoolean(POARICE_TAG)) {
                long activationTime = persistentData.getLong(POARICE_TAG);
                long currentTime = level.getGameTime();
                if (currentTime - activationTime >=ploarice_time) {
                    persistentData.remove(POARICE_TAG);
                }
            }
        }
    }

    @SubscribeEvent
    public static void onAttackEvent(LivingAttackEvent event) {
//        log.info("Current thread name: {}", Thread.currentThread().getName());
        Entity attacker = event.getSource().getEntity();
        if (attacker != null && attacker.getPersistentData().contains(POARICE_TAG) && ploarice_time > 0) {
            LivingEntity targetEntity = event.getEntity();
            Level level = targetEntity.level();
            log.info("Level:{}", level);
//                log.info("Server Level:{}", serverLevel);
                String entityTypeName = EntityType.getKey(targetEntity.getType()).toString();
                String entityName = entityTypeName.substring(entityTypeName.indexOf(":") + 1);
                String servantTypeName = "entity.goety." + entityName + "_servant";
                log.info("Entity Servant Name: {}", servantTypeName);

                for (EntityType<?> entityType : ForgeRegistries.ENTITY_TYPES.getValues()) {
                    String registryName = entityType.toString();
                    if (registryName.equals(servantTypeName)) {
                        // 找到匹配的实体类型
                        LivingEntity servant = (LivingEntity) entityType.create(level);
                        if (servant != null) {
                            servant.moveTo(targetEntity.getX(), targetEntity.getY(), targetEntity.getZ());
                            targetEntity.remove(Entity.RemovalReason.DISCARDED);
                            level.addFreshEntity(servant);
                        }
                        break;
                    }
                }
        }
    }
}
