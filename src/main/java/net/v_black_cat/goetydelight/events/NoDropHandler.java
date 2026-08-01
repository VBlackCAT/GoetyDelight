package net.v_black_cat.goetydelight.events;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ExperienceOrb;
import net.minecraft.world.entity.item.ItemEntity;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.LivingDropsEvent;
import net.v_black_cat.goetydelight.GoetyDelight;
import net.v_black_cat.goetydelight.item.food.HiddenPancakeItem;

import java.util.Objects;

/**
 * 1.21.1 移植版（对应 1.20.1 ability/NoDropHandler）：
 * 隐藏煎饼生成的复制体死亡时不掉落任何物品/经验。
 */
@EventBusSubscriber(modid = GoetyDelight.MODID)
public class NoDropHandler {

    @SubscribeEvent
    public static void onEntityDrop(LivingDropsEvent event) {
        if (!HiddenPancakeItem.isIsHiddenPancakeCopy(EntityType.getKey(event.getEntity().getType()))) {
            return;
        }
        if (!event.getEntity().getTags().contains("HiddenPancake")) {
            return;
        }

        event.setCanceled(true);
        Objects.requireNonNull(event.getEntity().level().getServer()).execute(() -> {
            event.getEntity().level().getEntitiesOfClass(ItemEntity.class,
                            event.getEntity().getBoundingBox().inflate(0.5D))
                    .forEach(Entity::discard);
            event.getEntity().level().getEntitiesOfClass(ExperienceOrb.class,
                            event.getEntity().getBoundingBox().inflate(0.5D))
                    .forEach(Entity::discard);
        });
    }
}
