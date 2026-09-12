package net.v_black_cat.goetydelight.compat.curios;


import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;
import net.v_black_cat.goetydelight.compat.CompatManager;

public class CuriosCompat {
    private static final String ID = "curios";
    public static final boolean IS_LOADED;

    static {
        IS_LOADED = CompatManager.isLoaded(ID);
    }

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
