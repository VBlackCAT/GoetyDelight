package net.v_black_cat.goetydelight.events;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.LivingDeathEvent;
import net.v_black_cat.goetydelight.GoetyDelight;
import net.v_black_cat.goetydelight.advancements.ModAdvancementsTrigger;

@EventBusSubscriber(modid = GoetyDelight.MODID)
public class GhostFarmerAttackEvent {
    @SubscribeEvent
    public static void onPlayerDeath(LivingDeathEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            Entity sourceEntity = event.getSource().getEntity();

            if (sourceEntity != null && player.getTags().contains("ghost_farmer_attack")) {
                ModAdvancementsTrigger.GHOST_FARMER_KILL_PLAYER.get().trigger(
                        player,
                        sourceEntity,
                        event.getSource()
                );

                player.removeTag("ghost_farmer_attack");
            }
        }
    }
}
