package net.v_black_cat.goetydelight.init;

import net.minecraft.client.model.Model;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.ResourceManagerReloadListener;
import net.v_black_cat.goetydelight.GoetyDelight;
import net.v_black_cat.goetydelight.bedrock.BedrockModel;

import java.io.IOException;
import java.io.InputStream;

public class CustomDollReloadListener implements ResourceManagerReloadListener {
    public static Model DFAULT_DOLL_MODEL;
    public static final ResourceLocation DEFAULT_TEXTURE_ID = new ResourceLocation(GoetyDelight.MODID, "textures/block/doll/doll_5152.png");

    @Override
    public void onResourceManagerReload(ResourceManager manager) {
        try {
            if (DFAULT_DOLL_MODEL == null) {
                readDefaultModel(manager);
            }
            CustomDollResourceLoader.init(manager);
            CustomDollLoader.init();
            CustomDollLoader.putAll(
                    CustomDollResourceLoader.getModels(),
                    CustomDollResourceLoader.getLanguages(),
                    CustomDollResourceLoader.getTextures()
            );
        } catch (IOException e) {
            GoetyDelight.LOGGER.error("Failed to reload custom dolls", e);
        }
    }

    private static void readDefaultModel(ResourceManager manager) {
        manager.getResource(new ResourceLocation(GoetyDelight.MODID, "models/block/doll/custom_doll.json")).ifPresent(res -> {
            try (InputStream stream = res.open()) {
                DFAULT_DOLL_MODEL = new BedrockModel(stream);
            } catch (Exception e) {
                GoetyDelight.LOGGER.error("Failed to load default custom doll model", e);
            }
        });
    }
}
