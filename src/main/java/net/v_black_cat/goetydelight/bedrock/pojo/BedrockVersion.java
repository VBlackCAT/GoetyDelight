package net.v_black_cat.goetydelight.bedrock.pojo;

import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import org.apache.maven.artifact.versioning.DefaultArtifactVersion;
import org.apache.maven.artifact.versioning.InvalidVersionSpecificationException;
import org.apache.maven.artifact.versioning.VersionRange;

@OnlyIn(Dist.CLIENT)
public enum BedrockVersion {
    /**
     * 旧版本基岩版模型，仅限 1.10.0
     */
    LEGACY("[1.10.0]"),
    /**
     * 新版本基岩版模型，往后的 1.14.0，1.16.0 1.21.0 通通用此版本读取
     */
    NEW("[1.12.0,)");

    private final VersionRange versionRange;

    BedrockVersion(String version) {
        this.versionRange = createFromVersionSpec(version);
    }

    public static boolean isNewVersion(BedrockModelPOJO bedrockModel) {
        String formatVersion = bedrockModel.getFormatVersion();
        if (formatVersion == null) {
            return false;
        }
        DefaultArtifactVersion inputVersion = new DefaultArtifactVersion(formatVersion);
        return NEW.versionRange.containsVersion(inputVersion);
    }

    public static boolean isLegacyVersion(BedrockModelPOJO bedrockModel) {
        String formatVersion = bedrockModel.getFormatVersion();
        if (formatVersion == null) {
            return false;
        }
        DefaultArtifactVersion inputVersion = new DefaultArtifactVersion(formatVersion);
        return LEGACY.versionRange.containsVersion(inputVersion);
    }

    private static VersionRange createFromVersionSpec(final String spec) {
        try {
            return VersionRange.createFromVersionSpec(spec);
        } catch (InvalidVersionSpecificationException e) {
            throw new RuntimeException("Failed to parse spec", e);
        }
    }

    public static BedrockVersion getVersion(BedrockModelPOJO pojo) throws InvalidVersionSpecificationException {
        if (isNewVersion(pojo)) {
            return NEW;
        } else if (isLegacyVersion(pojo)) {
            return LEGACY;
        }
        throw new InvalidVersionSpecificationException("Invalid version for model: " + pojo);
    }
}
