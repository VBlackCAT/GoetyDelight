package net.v_black_cat.goetydelight.init;

import com.google.common.collect.Maps;
import com.google.common.reflect.TypeToken;
import net.minecraft.client.model.Model;
import net.minecraft.resources.ResourceLocation;
import net.v_black_cat.goetydelight.GoetyDelight;

import javax.annotation.Nullable;
import java.util.Map;

public class CustomDollLoader {

    private static final Map<String, Model> MODELS = Maps.newHashMap();
    private static final Map<String, Map<String, String>> LANGUAGES = Maps.newHashMap();
    private static final Map<String, ResourceLocation> TEXTURES = Maps.newHashMap();

    public static void init() {
        MODELS.clear();
        LANGUAGES.clear();
        TEXTURES.clear();
    }

    public static void putAll(Map<String, Model> models, Map<String, Map<String, String>> languages, Map<String, ResourceLocation> textures) {
        MODELS.putAll(models);
        LANGUAGES.putAll(languages);
        TEXTURES.putAll(textures);
    }

    @Nullable
    public static Model getModel(String id) {
        return MODELS.get(id);
    }

    @Nullable
    public static ResourceLocation getTexture(String name) {
        return TEXTURES.get(name);
    }

    public static String getLanguage(String locale, String key) {
        if (LANGUAGES.containsKey(locale) && LANGUAGES.get(locale).containsKey(key)) {
            return LANGUAGES.get(locale).get(key);
        }
        if (LANGUAGES.containsKey("en_us") && LANGUAGES.get("en_us").containsKey(key)) {
            return LANGUAGES.get("en_us").get(key);
        }
        return key;
    }
}
