package net.v_black_cat.goetydelight.events;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.extensions.common.IClientItemExtensions;
import net.neoforged.neoforge.client.extensions.common.RegisterClientExtensionsEvent;
import net.v_black_cat.goetydelight.GoetyDelight;
import net.v_black_cat.goetydelight.init.ModItems;
import net.v_black_cat.goetydelight.render.item.DollEntityItemRender;

public class InitializeClientEventHandler {
    public static void onRegisterClientExtensions(RegisterClientExtensionsEvent event) {
        event.registerItem(new IClientItemExtensions() {
            private DollEntityItemRender render = null;

            @Override
            public BlockEntityWithoutLevelRenderer getCustomRenderer() {
                if (render == null) {
                    Minecraft minecraft = Minecraft.getInstance();
                    render = new DollEntityItemRender(
                            minecraft.getBlockEntityRenderDispatcher(),
                            minecraft.getEntityModels()
                    );
                }
                return render;
            }
        }, ModItems.DOLL_ITEM.get());
    }
}
