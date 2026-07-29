package net.v_black_cat.goetydelight.compat.curios;


import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.fml.ModList;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;

public class CuriosCompat {
    private static final String ID = "curios";
    public static final boolean IS_LOADED;

    static {
        IS_LOADED = ModList.get().isLoaded(ID);
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
