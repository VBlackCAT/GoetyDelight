package net.v_black_cat.goetydelight.ability;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ExperienceOrb;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.event.entity.living.LivingDropsEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.v_black_cat.goetydelight.GoetyDelight;

import java.util.Objects;

@Mod.EventBusSubscriber(modid = GoetyDelight.MODID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class NoDropHandler {
    @SubscribeEvent
    public static void onEntityDrop(LivingDropsEvent event) {
        if (net.v_black_cat.goetydelight.item.food.HiddenPancakeItem.isIsHiddenPancakeCopy(EntityType.getKey(event.getEntity().getType()))){
            if(event.getEntity().getTags().contains("HiddenPancake")){
                event.setCanceled( true);
                Objects.requireNonNull(event.getEntity().level().getServer()).execute(() -> {
                    event.getEntity().level().getEntitiesOfClass(ItemEntity.class, event.getEntity().getBoundingBox().inflate(0.5D)).forEach(Entity::discard);
                    event.getEntity().level().getEntitiesOfClass(ExperienceOrb.class, event.getEntity().getBoundingBox().inflate(0.5D)).forEach(Entity::discard);
                });
            }
        }
    }
}
