package net.v_black_cat.goetydelight.compat.curios;

import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.event.EntityRenderersEvent;
import net.v_black_cat.goetydelight.compat.CompatManager;

/**
 * Curios 联动。模组存在性判断统一走 {@link CompatManager}。
 */
public class CuriosCompat {
    private static final String ID = "curios";
    public static final boolean IS_LOADED = CompatManager.isLoaded(ID);

    @OnlyIn(Dist.CLIENT)
    public static void addEntityLayers(EntityRenderersEvent.AddLayers event) {
        if (IS_LOADED) {
            CuriosCompatInner.registerRenderer(event);
        }
    }

    public static void commonSetup() {
        if (IS_LOADED) {
            CuriosCompatInner.registerDollItemPredicate();
        }
    }
}
