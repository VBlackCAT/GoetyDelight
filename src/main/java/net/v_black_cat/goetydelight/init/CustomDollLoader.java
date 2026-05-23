package net.v_black_cat.goetydelight.init;

import com.google.common.collect.Maps;
import com.google.common.reflect.TypeToken;
import com.mojang.blaze3d.platform.NativeImage;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.Model;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.fml.loading.FMLPaths;
import net.v_black_cat.goetydelight.GoetyDelight;
import net.v_black_cat.goetydelight.bedrock.BedrockModel;
import net.v_black_cat.goetydelight.bedrock.BedrockModelUtil;
import net.v_black_cat.goetydelight.util.Md5Utils;

import javax.annotation.Nullable;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;
import java.util.regex.Pattern;
import java.util.stream.Stream;
import java.util.zip.ZipFile;

public class CustomDollLoader {
    private static final Path ROOT = FMLPaths.CONFIGDIR.get().resolve(GoetyDelight.MODID).resolve("custom");

    private static final String DIRECTORY_MODELS_NAME = "models";
    private static final String DIRECTORY_LANGUAGES_NAME = "lang";
    private static final String DIRECTORY_TEXTURES_NAME = "textures";

    /**
     * zip 文件有两种情况，一直是直接下面就是 models 和 lang 文件夹
     * 另一种是有一个顶层文件夹，下面才是 models 和 lang 文件夹
     * 我们需要同时考虑这两种情况
     */
    private static final Pattern ZIP_MODELS_PATTERN = Pattern.compile("^(?:[^/]+/)?models/[^/]+\\.json$");
    private static final Pattern ZIP_LANG_PATTERN = Pattern.compile("^(?:[^/]+/)?lang/[^/]+\\.json$");
    private static final Pattern ZIP_TEXTURES_PATTERN = Pattern.compile("^(?:[^/]+/)?textures/.+\\.png$");

    private static final String JSON = ".json";
    private static final String PNG = ".png";
    private static final String ZIP = ".zip";

    /**
     * 缓存数据
     */
    private static final Map<String, Model> MODELS = Maps.newHashMap();
    private static final Map<String, Map<String, String>> LANGUAGES = Maps.newHashMap();
    private static final Map<String, ResourceLocation> TEXTURES = Maps.newHashMap();

    public static void init() throws IOException {
        if (!Files.isDirectory(ROOT)) {
            return;
        }

        MODELS.clear();
        LANGUAGES.clear();
        TEXTURES.clear();

        try (Stream<Path> pathStream = Files.walk(ROOT, 1)) {
            pathStream.filter(p -> !p.equals(ROOT)).forEach(path -> {
                // 文件夹
                if (Files.isDirectory(path)) {
                    CustomDollLoader.loadFromDirectory(path);
                    return;
                }

                // zip 文件
                if (Files.isRegularFile(path) && path.toString().endsWith(ZIP)) {
                    CustomDollLoader.loadFromZip(path);
                }
            });
        }

        // 最后把内部资源包读取的数据存入
        CustomDollResourceLoader.putAll(MODELS, LANGUAGES, TEXTURES);
    }

    private static void loadFromDirectory(Path directory) {
        Path modelsDir = directory.resolve(DIRECTORY_MODELS_NAME);
        if (Files.isDirectory(modelsDir)) {
            try (Stream<Path> pathStream = Files.walk(modelsDir, 1)) {
                pathStream.filter(p -> !p.equals(modelsDir)).forEach(path -> {
                    if (Files.isRegularFile(path) && path.toString().endsWith(JSON)) {
                        try (InputStream stream = Files.newInputStream(path)) {
                            readModel(stream);
                        } catch (IOException e) {
                            GoetyDelight.LOGGER.error("Failed to read model file: {}", path, e);
                        }
                    }
                });
            } catch (IOException e) {
                GoetyDelight.LOGGER.error("Failed to load models from directory: {}", directory, e);
            }
        }

        Path langDir = directory.resolve(DIRECTORY_LANGUAGES_NAME);
        if (Files.isDirectory(langDir)) {
            try (Stream<Path> pathStream = Files.walk(langDir, 1)) {
                pathStream.filter(p -> !p.equals(langDir)).forEach(path -> {
                    if (Files.isRegularFile(path) && path.toString().endsWith(JSON)) {
                        try (InputStream stream = Files.newInputStream(path)) {
                            // 获取不带后缀名的文件名
                            String fileName = path.getFileName().toString();
                            fileName = fileName.substring(0, fileName.length() - JSON.length());
                            readLanguage(fileName, stream);
                        } catch (IOException e) {
                            GoetyDelight.LOGGER.error("Failed to read language file: {}", path, e);
                        }
                    }
                });
            } catch (IOException e) {
                GoetyDelight.LOGGER.error("Failed to load languages from directory: {}", directory, e);
            }
        }

        Path texturesDir = directory.resolve(DIRECTORY_TEXTURES_NAME);
        if (Files.isDirectory(texturesDir)) {
            try (Stream<Path> pathStream = Files.walk(texturesDir, 1)) {
                pathStream.filter(p -> !p.equals(texturesDir)).forEach(path -> {
                    if (Files.isRegularFile(path) && path.toString().endsWith(PNG)) {
                        try (InputStream stream = Files.newInputStream(path)) {
                            // 获取不带后缀名的文件名
                            String fileName = path.getFileName().toString();
                            fileName = fileName.substring(0, fileName.length() - PNG.length());
                            readTexture(fileName, stream);
                        } catch (IOException e) {
                            GoetyDelight.LOGGER.error("Failed to read texture file: {}", path, e);
                        }
                    }
                });
            } catch (IOException e) {
                GoetyDelight.LOGGER.error("Failed to load textures from directory: {}", directory, e);
            }
        }
    }

    private static void loadFromZip(Path zipFile) {
        try (ZipFile zip = new ZipFile(zipFile.toFile())) {
            zip.stream().forEach(entry -> {
                String entryName = entry.getName().replace('\\', '/');
                if (entryName.startsWith("/")) {
                    entryName = entryName.substring(1);
                }

                if (ZIP_MODELS_PATTERN.matcher(entryName).matches()) {
                    try (InputStream stream = zip.getInputStream(entry)) {
                        readModel(stream);
                    } catch (IOException e) {
                        GoetyDelight.LOGGER.error("Failed to read model file from zip: {} in {}", entryName, zipFile, e);
                    }
                    return;
                }

                if (ZIP_LANG_PATTERN.matcher(entryName).matches()) {
                    try (InputStream stream = zip.getInputStream(entry)) {
                        // 获取不带后缀名的文件名
                        String fileName = entryName.substring(entryName.lastIndexOf('/') + 1);
                        fileName = fileName.substring(0, fileName.length() - JSON.length());
                        readLanguage(fileName, stream);
                    } catch (IOException e) {
                        GoetyDelight.LOGGER.error("Failed to read language file from zip: {} in {}", entryName, zipFile, e);
                    }
                    return;
                }

                if (ZIP_TEXTURES_PATTERN.matcher(entryName).matches()) {
                    try (InputStream stream = zip.getInputStream(entry)) {
                        // 获取不带后缀名的文件名
                        String fileName = entryName.substring(entryName.lastIndexOf('/') + 1);
                        fileName = fileName.substring(0, fileName.length() - PNG.length());
                        readTexture(fileName, stream);
                    } catch (IOException e) {
                        GoetyDelight.LOGGER.error("Failed to read texture file from zip: {} in {}", entryName, zipFile, e);
                    }
                }
            });
        } catch (IOException e) {
            GoetyDelight.LOGGER.error("Failed to load models from zip file: {}", zipFile, e);
        }
    }

    private static void readModel(InputStream stream) {
        try {
            BedrockModel model = new BedrockModel(stream);
            String identifier = model.getIdentifier();
            if (identifier != null) {
                MODELS.put(identifier, model);

                String modelName = extractModelNameFromIdentifier(identifier);
                ResourceLocation texture = TEXTURES.get(modelName);
                if (texture == null) {
                    GoetyDelight.LOGGER.warn("No texture found for model: {}, expected texture name: {}", identifier, modelName);
                }
            } else {
                GoetyDelight.LOGGER.error("Model identifier is null");
            }
        } catch (Exception e) {
            GoetyDelight.LOGGER.error("Failed to read model", e);
        }
    }

    private static String extractModelNameFromIdentifier(String identifier) {
        if (identifier == null || !identifier.contains(".")) {
            return identifier;
        }
        String[] parts = identifier.split("\\.");
        return parts[parts.length - 1];
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
        // 回退到英文
        if (LANGUAGES.containsKey("en_us") && LANGUAGES.get("en_us").containsKey(key)) {
            return LANGUAGES.get("en_us").get(key);
        }
        // 最后回退到 key 自身
        return key;
    }
}
