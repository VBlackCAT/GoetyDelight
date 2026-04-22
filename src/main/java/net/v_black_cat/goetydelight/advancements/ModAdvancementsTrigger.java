package net.v_black_cat.goetydelight.advancements;

import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.resources.ResourceLocation;
import net.v_black_cat.goetydelight.GoetyDelight;

public class ModAdvancementsTrigger {
    public static final PlayerBeKilledTrigger GHOST_FARMER_KILL_PLAYER = new PlayerBeKilledTrigger(
            new ResourceLocation(GoetyDelight.MODID, "ghost_farmer_kill_player")
    );

    public static void init() {
        CriteriaTriggers.register(GHOST_FARMER_KILL_PLAYER);
    }
}
