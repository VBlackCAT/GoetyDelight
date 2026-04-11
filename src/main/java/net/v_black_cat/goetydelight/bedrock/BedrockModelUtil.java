package net.v_black_cat.goetydelight.bedrock;


import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.v_black_cat.goetydelight.bedrock.pojo.CubesItem;
import net.v_black_cat.goetydelight.bedrock.pojo.FaceItem;
import net.v_black_cat.goetydelight.bedrock.pojo.FaceUVsItem;

@OnlyIn(Dist.CLIENT)
public final class BedrockModelUtil {
    public static final Gson GSON = new GsonBuilder()
            .registerTypeAdapter(ResourceLocation.class, new ResourceLocation.Serializer())
            .registerTypeAdapter(CubesItem.class, new CubesItem.Deserializer())
            .create();

    public static FaceUVsItem singleSouthFace() {
        return new FaceUVsItem(emptyFace(), emptyFace(), emptyFace(), single16xFace(), emptyFace(), emptyFace());
    }

    public static FaceItem single16xFace() {
        return new FaceItem(new float[]{0, 0}, new float[]{16, 16});
    }

    public static FaceItem emptyFace() {
        return new FaceItem(new float[]{0, 0}, new float[]{0, 0});
    }
}
