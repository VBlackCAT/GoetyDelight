package net.v_black_cat.goetydelight.proxy;

import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.event.EntityRenderersEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.v_black_cat.goetydelight.GoetyDelight;
import net.v_black_cat.goetydelight.item.FalseProverbsItemModel;

@Mod.EventBusSubscriber(modid = GoetyDelight.MODID, bus = Mod.EventBusSubscriber.Bus.MOD , value = Dist.CLIENT)
public class ClientProxy implements Modproxy{
    private static ClientProxy INSTANCE;
    public ClientProxy(FMLJavaModLoadingContext context) {
        try {
            IEventBus modBus = context.getModEventBus();
            modBus.addListener(this::onRegisterLayers);
            INSTANCE = this;
        } catch (Throwable throwable) {
            throwable.printStackTrace();
        }
    }

    @OnlyIn(Dist.CLIENT)
    @SubscribeEvent
    public void onRegisterLayers(EntityRenderersEvent.RegisterLayerDefinitions event) {
        event.registerLayerDefinition(FalseProverbsItemModel.LAYER_LOCATION, FalseProverbsItemModel::createBodyLayer);
    }
}
