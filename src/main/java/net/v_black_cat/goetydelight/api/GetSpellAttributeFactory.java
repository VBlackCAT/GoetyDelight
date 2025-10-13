package net.v_black_cat.goetydelight.api;

import net.minecraftforge.fml.ModContainer;
import net.minecraftforge.fml.ModList;
import net.v_black_cat.goetydelight.api.impl.GoetyGetSpellAttributeModifierImpl;
import net.v_black_cat.goetydelight.api.impl.NoSpellAttributeModifierImpl;
import net.v_black_cat.goetydelight.api.impl.RevelationGetSpellAttributeModifierImpl;
import org.apache.maven.artifact.versioning.DefaultArtifactVersion;

import java.util.Optional;

public class GetSpellAttributeFactory {


    public static IGetSpellAttributeImplementation createGetSpellAttributeImplementation() {

        Optional<? extends ModContainer> depContainer = ModList.get().getModContainerById("goety");
        boolean goetyRevelation = ModList.get().isLoaded("goety_revelation");

        DefaultArtifactVersion loadedVersion = (DefaultArtifactVersion) depContainer.get().getModInfo().getVersion();
        DefaultArtifactVersion otherVersion = new DefaultArtifactVersion("2.5.37.0");
        /*
        如果loadedVersion等于otherVersion，则返回0；
        如果loadedVersion小于otherVersion，则返回负整数；
        如果loadedVersion大于otherVersion，则返回正整数。
        */
        int i = loadedVersion.compareTo(otherVersion);

        if(goetyRevelation){
            return RevelationGetSpellAttributeModifierImpl.getInstance();
        }
        if (i >= 0) {
            return GoetyGetSpellAttributeModifierImpl.getInstance();
        } else {
            return NoSpellAttributeModifierImpl.getInstance();
        }

    }
}