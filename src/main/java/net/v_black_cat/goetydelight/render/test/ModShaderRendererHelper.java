package net.v_black_cat.goetydelight.render.test;

public class ModShaderRendererHelper {
    private static float partialTick = 0;

    public static void updatePartialTick(float partialTick) {
        ModShaderRendererHelper.partialTick = partialTick;
    }

    public static float getPartialTick() {
        return partialTick;
    }
}