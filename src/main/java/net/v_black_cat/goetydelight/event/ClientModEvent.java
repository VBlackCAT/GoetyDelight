package net.v_black_cat.goetydelight.event;


import net.minecraft.client.renderer.entity.player.PlayerRenderer;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.EntityRenderersEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.v_black_cat.goetydelight.GoetyDelight;
import net.v_black_cat.goetydelight.item.ModItems;
import net.v_black_cat.goetydelight.render.FalseProverbsRanderLayer;

@Mod.EventBusSubscriber(modid = GoetyDelight.MODID, bus = Mod.EventBusSubscriber.Bus.MOD , value = Dist.CLIENT)
public class ClientModEvent {
    @SubscribeEvent
    public static void onAddLayers(EntityRenderersEvent.AddLayers event) {
        PlayerRenderer defaultRenderer = event.getSkin("default");
        if (defaultRenderer != null){
            defaultRenderer.addLayer(new FalseProverbsRanderLayer(defaultRenderer));
        }

        PlayerRenderer slimRenderer = event.getSkin("slim");
        if (slimRenderer != null){
            slimRenderer.addLayer(new FalseProverbsRanderLayer(slimRenderer));
        }
    }
}
