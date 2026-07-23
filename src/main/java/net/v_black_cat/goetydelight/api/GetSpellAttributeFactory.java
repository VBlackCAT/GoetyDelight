package net.v_black_cat.goetydelight.api;

import net.neoforged.fml.ModContainer;
import net.neoforged.fml.ModList;
import net.v_black_cat.goetydelight.api.impl.GoetyGetSpellAttributeModifierImpl;
import net.v_black_cat.goetydelight.api.impl.NoSpellAttributeModifierImpl;
// import net.v_black_cat.goetydelight.api.impl.RevelationGetSpellAttributeModifierImpl;
import org.apache.maven.artifact.versioning.DefaultArtifactVersion;

import java.util.Optional;

public class GetSpellAttributeFactory {

    public static IGetSpellAttributeImplementation createGetSpellAttributeImplementation() {

        Optional<? extends ModContainer> depContainer = ModList.get().getModContainerById("goety");
        if (depContainer.isEmpty()) {

            return NoSpellAttributeModifierImpl.getInstance();
        }

        // boolean goetyRevelation = ModList.get().isLoaded("goety_revelation");

        DefaultArtifactVersion loadedVersion = (DefaultArtifactVersion) depContainer.get().getModInfo().getVersion();
        DefaultArtifactVersion targetVersion = new DefaultArtifactVersion("3.0.0"); // 改为 3.0.0

        /*
         * compareTo 返回值说明：
         *   loadedVersion == targetVersion  → 0
         *   loadedVersion <  targetVersion  → 负整数
         *   loadedVersion >  targetVersion  → 正整数
         */
        int compareResult = loadedVersion.compareTo(targetVersion);

        // if (goetyRevelation) {
        //     return RevelationGetSpellAttributeModifierImpl.getInstance();
        // }

        if (compareResult >= 0) {
            return GoetyGetSpellAttributeModifierImpl.getInstance();
        } else {
            return NoSpellAttributeModifierImpl.getInstance();
        }
    }
}