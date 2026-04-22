package net.v_black_cat.goetydelight.event;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.v_black_cat.goetydelight.GoetyDelight;
import net.v_black_cat.goetydelight.advancements.ModAdvancementsTrigger;

@Mod.EventBusSubscriber(modid = GoetyDelight.MODID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class GhostFarmerAttackEvent {
    @SubscribeEvent
    public static void onPlayerDeath(LivingDeathEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            Entity sourceEntity = event.getSource().getEntity();

            if (sourceEntity != null && player.getTags().contains("ghost_farmer_attack")) {
                ModAdvancementsTrigger.GHOST_FARMER_KILL_PLAYER.trigger(
                        player,
                        sourceEntity,
                        event.getSource()
                );

                player.removeTag("ghost_farmer_attack");
            }
        }
    }
}
