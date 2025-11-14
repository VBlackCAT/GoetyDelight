
package net.v_black_cat.goetydelight.entities;

import com.mojang.logging.LogUtils;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import java.lang.reflect.Field;
import java.util.concurrent.locks.ReadWriteLock;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.event.entity.EntityJoinLevelEvent;
import net.minecraftforge.event.entity.living.LivingEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.slf4j.Logger;

@Mod.EventBusSubscriber(modid = "goetydelight", bus = Mod.EventBusSubscriber.Bus.FORGE)
public class HealthDataInterceptor {

    private static final Logger LOGGER = LogUtils.getLogger(); // 日志记录器

    // 自定义 SynchedEntityData 子类
    public static class CustomSynchedEntityData extends SynchedEntityData {
        private final Entity entity;
        private final SynchedEntityData originalDataToCopy;

        private static EntityDataAccessor<Float> HEALTH_DATA_ACCESSOR = null;

        static {
            try {
                Field healthIdField = LivingEntity.class.getDeclaredField("DATA_HEALTH_ID");
                healthIdField.setAccessible(true);
                HEALTH_DATA_ACCESSOR = (EntityDataAccessor<Float>) healthIdField.get(null);
                LOGGER.debug("Successfully obtained DATA_HEALTH_ID: {}", HEALTH_DATA_ACCESSOR);
            } catch (Exception e) {
                LOGGER.error("Failed to obtain DATA_HEALTH_ID from LivingEntity", e);
            }
        }

        public CustomSynchedEntityData(Entity entity, SynchedEntityData originalDataToCopy) {
            super(entity);
            this.entity = entity;
            this.originalDataToCopy = originalDataToCopy;

            if (this.originalDataToCopy != null) {
                LOGGER.debug("Copying data for entity: {}", entity.getName().getString());
                copyFromOriginal();
            } else {
                LOGGER.warn("No original data to copy for entity: {}", entity.getName().getString());
            }
        }

        private <T> void defineDataItem(SynchedEntityData.DataItem<T> dataItem) {
            super.define(dataItem.getAccessor(), dataItem.getValue());
        }

        private void copyFromOriginal() {
            try {
                Field itemsByIdField = SynchedEntityData.class.getDeclaredField("itemsById");
                itemsByIdField.setAccessible(true);
                Int2ObjectMap<SynchedEntityData.DataItem<?>> originalItemsById =
                        (Int2ObjectMap<SynchedEntityData.DataItem<?>>) itemsByIdField.get(this.originalDataToCopy);

                Field isDirtyField = SynchedEntityData.class.getDeclaredField("isDirty");
                isDirtyField.setAccessible(true);
                boolean originalIsDirty = (boolean) isDirtyField.get(this.originalDataToCopy);

                Field lockField = SynchedEntityData.class.getDeclaredField("lock");
                lockField.setAccessible(true);
                ReadWriteLock originalLock = (ReadWriteLock) lockField.get(this.originalDataToCopy);

                if (originalItemsById != null) {
                    originalLock.readLock().lock();
                    try {
                        for (SynchedEntityData.DataItem<?> originalDataItem : originalItemsById.values()) {
                            @SuppressWarnings("unchecked")
                            SynchedEntityData.DataItem<Object> castedDataItem = (SynchedEntityData.DataItem<Object>) originalDataItem;
                            defineDataItem(castedDataItem);
                        }
                    } finally {
                        originalLock.readLock().unlock();
                    }
                }

                Field thisIsDirtyField = SynchedEntityData.class.getDeclaredField("isDirty");
                thisIsDirtyField.setAccessible(true);
                thisIsDirtyField.set(this, originalIsDirty);

                LOGGER.debug("Data copied successfully for entity: {}", entity.getName().getString());
            } catch (NoSuchFieldException e) {
                LOGGER.error("Field not found in SynchedEntityData", e);
            } catch (IllegalAccessException e) {
                LOGGER.error("Illegal access to field in SynchedEntityData", e);
            } catch (Exception e) {
                LOGGER.error("Unexpected error during data copy", e);
            }
        }

        @Override
        public <T> void set(EntityDataAccessor<T> key, T value) {
            if (HEALTH_DATA_ACCESSOR != null && key.equals(HEALTH_DATA_ACCESSOR) && entity instanceof Player) {
                if (value instanceof Float) {
                    float currentHealth = (Float) value;
                    if (currentHealth < 20.0f) {
                        LOGGER.info("Player {} health intercepted: {} -> 20.0f", entity.getName().getString(), currentHealth);
                        super.set(key, (T) (Float) 20.0f);
                        return;
                    }
                }
            }
            super.set(key, value);
        }
    }

    @Mod.EventBusSubscriber(modid = "goetydelight", bus = Mod.EventBusSubscriber.Bus.FORGE)
    public static class EventHandler {
        @SubscribeEvent
        public static void LivingTickEvent(LivingEvent.LivingTickEvent event) {
            if (event.getEntity() instanceof Player player) {
                boolean hasNotAnything = false;
                for (int i = 0; i < player.getInventory().getContainerSize(); i++) {
                    if (player.getInventory().getItem(i).getItem().getDescriptionId().equals("item.goetydelight.not_anything")) {
                        hasNotAnything = true;
                        break;
                    }
                }

                if (hasNotAnything) {
                    LOGGER.debug("Player {} joined with Not Anything item", player.getName().getString());
                    SynchedEntityData originalEntityData = player.getEntityData();
                    CustomSynchedEntityData customData = new CustomSynchedEntityData(player, originalEntityData);

                    try {
                        Field field = Entity.class.getDeclaredField("entityData");
                        field.setAccessible(true);
                        field.set(player, customData);
                        LOGGER.info("Replaced SynchedEntityData for player {}", player.getName().getString());
                    } catch (NoSuchFieldException e) {
                        LOGGER.error("Field 'entityData' not found in Entity class", e);
                    } catch (IllegalAccessException e) {
                        LOGGER.error("Illegal access to 'entityData' field", e);
                    } catch (Exception e) {
                        LOGGER.error("Unexpected error during SynchedEntityData replacement", e);
                    }
                } else {
                    LOGGER.debug("Player {} joined without Not Anything item", player.getName().getString());
                }
            }
        }
    }
}