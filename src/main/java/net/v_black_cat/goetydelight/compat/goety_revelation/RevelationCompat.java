package net.v_black_cat.goetydelight.compat.goety_revelation;

import net.minecraftforge.fml.ModList;

public class RevelationCompat {
    public static final String ID = "goety_revelation";
    public static final boolean IS_REVELATION_LOADED;
    static {
        IS_REVELATION_LOADED = ModList.get().isLoaded(ID);
    }
}
