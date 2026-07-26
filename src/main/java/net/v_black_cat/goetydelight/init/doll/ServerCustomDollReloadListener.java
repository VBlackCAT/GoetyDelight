package net.v_black_cat.goetydelight.init.doll;

import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.ResourceManagerReloadListener;
import net.v_black_cat.goetydelight.GoetyDelight;

import java.io.IOException;

public class ServerCustomDollReloadListener implements ResourceManagerReloadListener {
    @Override
    public void onResourceManagerReload(ResourceManager resourceManager) {
        try {
            net.v_black_cat.goetydelight.init.doll.ServerCustomDollResourceLoader.init(resourceManager);
            ServerCustomDollLoader.init();
        } catch (IOException e) {
            GoetyDelight.LOGGER.error("Failed to reload custom dolls", e);
        }
    }
}
