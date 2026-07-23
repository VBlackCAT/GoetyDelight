package net.v_black_cat.goetydelight.init.client;

import net.minecraft.client.renderer.entity.player.PlayerRenderer;
import net.minecraft.client.resources.PlayerSkin;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;
import net.neoforged.neoforge.client.event.RegisterMenuScreensEvent;
import net.v_black_cat.goetydelight.GoetyDelight;
import net.v_black_cat.goetydelight.entities.ghostfarmer.GhostFarmerModel;
import net.v_black_cat.goetydelight.entities.ghostfarmer.GhostFarmerRenderer;
import net.v_black_cat.goetydelight.entities.soul_lich.SoulLichModel;
import net.v_black_cat.goetydelight.entities.soul_lich.SoulLichRenderer;
import net.v_black_cat.goetydelight.init.ModEntities;
import net.v_black_cat.goetydelight.init.ModMenuTypes;
import net.v_black_cat.goetydelight.item.FalseProverbsItemModel;
import net.v_black_cat.goetydelight.renderer.FalseProverbsBackLayer;
import net.v_black_cat.goetydelight.screen.CursedIngotPotScreen;
import net.v_black_cat.goetydelight.screen.NightStoveScreen;
import net.v_black_cat.goetydelight.screen.ShadeStoveScreen;

@EventBusSubscriber(modid = GoetyDelight.MODID, value = Dist.CLIENT)
public class ClientSetup {

    @SubscribeEvent
    public static void registerMenuScreens(RegisterMenuScreensEvent event) {
        event.register(ModMenuTypes.NIGHT_STOVE.get(), NightStoveScreen::new);
        event.register(ModMenuTypes.SHADE_STOVE.get(), ShadeStoveScreen::new);
        event.register(ModMenuTypes.CURSED_INGOT_POT.get(), CursedIngotPotScreen::new);
    }

    @SubscribeEvent
    public static void registerLayerDefinitions(EntityRenderersEvent.RegisterLayerDefinitions event) {
        event.registerLayerDefinition(
                ModLayerDefinitions.FALSE_PROVERBS_BACK,
                FalseProverbsItemModel::createBodyLayer
        );
        event.registerLayerDefinition(
                FalseProverbsItemModel.LAYER_LOCATION,
                FalseProverbsItemModel::createBodyLayer
        );
        event.registerLayerDefinition(ModLayerDefinitions.GHOST_FARMER, GhostFarmerModel::createBodyLayer);
        event.registerLayerDefinition(ModLayerDefinitions.SOUL_LICH, SoulLichModel::createBodyLayer);
    }

    @SubscribeEvent
    public static void addLayers(EntityRenderersEvent.AddLayers event) {
        for (PlayerSkin.Model skin : event.getSkins()) {
            var renderer = event.getSkin(skin);
            if (renderer instanceof PlayerRenderer playerRenderer) {
                playerRenderer.addLayer(new FalseProverbsBackLayer(playerRenderer));
            }
        }
    }

    @SubscribeEvent
    public static void registerRenderers(EntityRenderersEvent.RegisterRenderers event) {
        event.registerEntityRenderer(ModEntities.GHOST_FARMER.get(), GhostFarmerRenderer::new);
        event.registerEntityRenderer(ModEntities.SOUL_LICH.get(), SoulLichRenderer::new);
    }
}