package net.v_black_cat.goetydelight.init;

import com.google.common.collect.Maps;
import com.google.common.reflect.TypeToken;
import com.mojang.blaze3d.platform.NativeImage;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.Model;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.v_black_cat.goetydelight.GoetyDelight;
import net.v_black_cat.goetydelight.bedrock.BedrockModel;
import net.v_black_cat.goetydelight.bedrock.BedrockModelUtil;
import net.v_black_cat.goetydelight.bedrock.pojo.BedrockModelPOJO;
import net.v_black_cat.goetydelight.util.Md5Utils;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.Map;

public class CustomDollResourceLoader {
    private static final String MODELS_PATH = "models/block/doll";
    private static final String LANGUAGES_PATH = "custom_dolls/lang";
    private static final String TEXTURES_PATH = "textures/block/doll";

    private static final String JSON = ".json";
    private static final String PNG = ".png";

    private static final Map<String, Model> MODELS = Maps.newHashMap();
    private static final Map<String, Map<String, String>> LANGUAGES = Maps.newHashMap();
    private static final Map<String, ResourceLocation> TEXTURES = Maps.newHashMap();

    public static void init(ResourceManager manager) throws IOException {
        MODELS.clear();
        LANGUAGES.clear();
        TEXTURES.clear();

        // 只扫描 goetydelight 命名空间自己的资源，避免误读其它模组（如森罗玩偶）放在相同路径下的资源
        manager.listResources(MODELS_PATH, path -> GoetyDelight.MODID.equals(path.getNamespace()) && path.getPath().endsWith(JSON)).forEach((name, resource) -> {
            try (InputStream stream = resource.open()) {
                readModel(name, stream);
            } catch (IOException e) {
                GoetyDelight.LOGGER.error("Failed to load model resource: {}", name, e);
            }
        });

        manager.listResources(LANGUAGES_PATH, path -> GoetyDelight.MODID.equals(path.getNamespace()) && path.getPath().endsWith(JSON)).forEach((name, resource) -> {
            String langName = name.getPath().substring(LANGUAGES_PATH.length() + 1, name.getPath().length() - JSON.length());
            try (InputStream stream = resource.open()) {
                readLanguage(langName, stream);
            } catch (IOException e) {
                GoetyDelight.LOGGER.error("Failed to load language resource: {}", name, e);
            }
        });

        manager.listResources(TEXTURES_PATH, path -> GoetyDelight.MODID.equals(path.getNamespace()) && path.getPath().endsWith(PNG)).forEach((name, resource) -> {
            String textureName = name.getPath().substring(TEXTURES_PATH.length() + 1, name.getPath().length() - PNG.length());
            try (InputStream stream = resource.open()) {
                readTexture(textureName, stream);
            } catch (IOException e) {
                GoetyDelight.LOGGER.error("Failed to load texture resource: {}", name, e);
            }
        });
    }

    public static Map<String, Model> getModels() {
        return Collections.unmodifiableMap(MODELS);
    }

    public static Map<String, Map<String, String>> getLanguages() {
        return Collections.unmodifiableMap(LANGUAGES);
    }

    public static Map<String, ResourceLocation> getTextures() {
        return Collections.unmodifiableMap(TEXTURES);
    }

    private static void readModel(ResourceLocation name, InputStream stream) {
        try {
            BedrockModelPOJO pojo = BedrockModelUtil.GSON.fromJson(
                    new InputStreamReader(stream, StandardCharsets.UTF_8), BedrockModelPOJO.class);
            // 只有真正的基岩版模型才包含 format_version 与 minecraft:geometry 字段；
            // 若某个资源包在 goetydelight 命名空间下放置了非基岩版文件，则跳过以免解析出错。
            if (pojo.getFormatVersion() == null || pojo.getFirstGeometryModel() == null) {
                return;
            }
            BedrockModel model = new BedrockModel(pojo);
            String identifier = model.getIdentifier();
            if (identifier != null) {
                MODELS.put(identifier, model);
            } else {
                GoetyDelight.LOGGER.error("Model identifier is null: {}", name);
            }
        } catch (Exception e) {
            GoetyDelight.LOGGER.error("Failed to read model: {}", name, e);
        }
    }

    private static void readLanguage(String name, InputStream stream) {
        InputStreamReader reader = new InputStreamReader(stream, StandardCharsets.UTF_8);
        Type type = new TypeToken<Map<String, String>>() {
        }.getType();
        Map<String, String> languages = BedrockModelUtil.GSON.fromJson(reader, type);
        LANGUAGES.computeIfAbsent(name, k -> Maps.newHashMap()).putAll(languages);
    }

    private static void readTexture(String name, InputStream stream) {
        String md5Name = Md5Utils.md5Hex(name);
        ResourceLocation id = new ResourceLocation(GoetyDelight.MODID, "textures/block/doll/" + md5Name + ".png");
        try {
            NativeImage image = NativeImage.read(stream);
            DynamicTexture texture = new DynamicTexture(image);
            Minecraft.getInstance().getTextureManager().register(id, texture);
            TEXTURES.put(name, id);
        } catch (Exception e) {
            GoetyDelight.LOGGER.error("Failed to read texture: {}", name, e);
        }
    }
}
