package net.v_black_cat.goetydelight.event;

import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.EntityRenderersEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.v_black_cat.goetydelight.GoetyDelight;
import net.v_black_cat.goetydelight.compat.curios.CuriosCompat;

@Mod.EventBusSubscriber(modid = GoetyDelight.MODID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public class AddEntityLayersEvent {
    @SubscribeEvent
    public static void addEntityLayers(EntityRenderersEvent.AddLayers event) {
        CuriosCompat.addEntityLayers(event);
    }
}
