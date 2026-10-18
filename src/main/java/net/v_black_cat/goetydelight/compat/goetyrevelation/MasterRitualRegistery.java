package net.v_black_cat.goetydelight.compat.goetyrevelation;

import com.Polarice3.Goety.api.ritual.IRitualType;
import com.Polarice3.Goety.api.ritual.RitualType;
import net.minecraftforge.fml.ModContainer;
import net.minecraftforge.fml.ModList;
import net.v_black_cat.goetydelight.GoetyDelight;
import net.v_black_cat.goetydelight.block.ModBlocks;
import org.apache.maven.artifact.versioning.DefaultArtifactVersion;

import java.util.Optional;

/**
 * 大师烹饪仪式的注册入口，位于联动包内。
 * 只有 goety_revelation 加载时，本类才会被加载/调用，
 * 因此主包 DelightRitualType 完全不需要引用任何联动类。
 */
public final class MasterRitualRegistery {

    private static final String RITUAL_KEY = "master_culinary";
    private static final String OLD_API_KEY = "MASTER_DELIGHT";

    private MasterRitualRegistery() {
    }

    /**
     * 注册大师烹饪仪式。
     * 必须在联动方块注册表冻结之后调用（FMLCommonSetupEvent#enqueueWork 内）。
     */
    public static void register() {
        if (!GoetyRevelationCompat.IS_LOADED) {
            return;
        }
        // 保险：联动方块确实注册成功
        if (RevelationCompatRegistry.APOCALYPTIUM_POT == null
                || ModBlocks.NIGHT_STOVE == null) {
            GoetyDelight.LOGGER.warn("[compat] 大师烹饪仪式未注册：联动方块尚未就绪");
            return;
        } else {
            RevelationCompatRegistry.APOCALYPTIUM_POT.get();
            ModBlocks.NIGHT_STOVE.get();
        }

        MasterDelightRitualType ritualType = new MasterDelightRitualType();

        Optional<? extends ModContainer> goetyContainer = ModList.get().getModContainerById("goety");
        if (!goetyContainer.isPresent()) {
            return;
        }
        DefaultArtifactVersion loadedVersion =
                (DefaultArtifactVersion) goetyContainer.get().getModInfo().getVersion();
        DefaultArtifactVersion requiredVersion = new DefaultArtifactVersion("2.5.38.0");

        if (loadedVersion.compareTo(requiredVersion) >= 0) {
            RitualType.addRitualType(RITUAL_KEY, ritualType);
        } else {
            registerOldApi(OLD_API_KEY, ritualType);
        }
        GoetyDelight.LOGGER.info("[compat] 大师烹饪仪式已注册");
    }

    /** 旧版 Goety（< 2.5.38.0）的反射注册分支 */
    private static void registerOldApi(String key, IRitualType type) {
        try {
            java.lang.reflect.Method createMethod = RitualType.class.getDeclaredMethod(
                    "create", String.class, IRitualType.class);
            createMethod.setAccessible(true);
            createMethod.invoke(null, key, type);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}