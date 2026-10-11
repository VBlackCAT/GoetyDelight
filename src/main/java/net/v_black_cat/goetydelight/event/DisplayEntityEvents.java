package net.v_black_cat.goetydelight.event;

import net.minecraftforge.event.entity.living.LivingAttackEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.v_black_cat.goetydelight.GoetyDelight;
import net.v_black_cat.goetydelight.entities.display_entity.DisplayEntity;

@Mod.EventBusSubscriber(modid = GoetyDelight.MODID)
public class DisplayEntityEvents {

    @SubscribeEvent
    public static void onLivingAttack(LivingAttackEvent event) {
        if (event.getEntity() instanceof DisplayEntity) {
            event.setCanceled(true);
        }
    }
}
