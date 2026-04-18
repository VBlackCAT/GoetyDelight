package net.v_black_cat.goetydelight.event;

import net.minecraft.client.renderer.blockentity.BlockEntityRenderers;
import net.minecraft.client.renderer.entity.EntityRenderers;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.EntityRenderersEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.v_black_cat.goetydelight.GoetyDelight;
import net.v_black_cat.goetydelight.block.ModBlockEntities;
import net.v_black_cat.goetydelight.entities.ModEntities;
import net.v_black_cat.goetydelight.render.item.CustomDollRender;
import net.v_black_cat.goetydelight.render.item.DollEntityRender;

@Mod.EventBusSubscriber(bus = Mod.EventBusSubscriber.Bus.MOD, modid = GoetyDelight.MODID, value = Dist.CLIENT)
public class ModEntitiesRender {
    @SubscribeEvent
    public static void onEntityRenderers(EntityRenderersEvent.RegisterRenderers event) {
        EntityRenderers.register(ModEntities.DOLL_ENTITY.get(), DollEntityRender::new);

        BlockEntityRenderers.register(ModBlockEntities.CUSTOM_DOLL_BE.get(), CustomDollRender::new);
    }
}
