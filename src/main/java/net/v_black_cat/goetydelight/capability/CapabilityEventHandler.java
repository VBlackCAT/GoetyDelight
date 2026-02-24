package net.v_black_cat.goetydelight.capability;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.v_black_cat.goetydelight.GoetyDelight;
import net.v_black_cat.goetydelight.entities.ai.customer.ICustomerEntity;

@Mod.EventBusSubscriber(modid = GoetyDelight.MODID)
public class CapabilityEventHandler {

    @SubscribeEvent
    public static void onAttachCapabilities(AttachCapabilitiesEvent<Entity> event) {
        if (event.getObject() instanceof PathfinderMob mob) {
            if (mob instanceof ICustomerEntity) {
                event.addCapability(
                        new ResourceLocation(GoetyDelight.MODID, "customer_order_item_list"),
                        new CustomerOrderItemProvider()
                );
            }
        }
    }
}