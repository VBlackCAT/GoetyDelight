package net.v_black_cat.goetydelight.events;

import net.minecraft.server.level.ServerLevel;
import net.neoforged.neoforge.event.tick.LevelTickEvent;
import net.v_black_cat.goetydelight.visual.EntityVisualEffectSystem;

public final class VisualEffectEventHandler {
    private VisualEffectEventHandler() {
    }

    public static void onLevelTick(LevelTickEvent.Post event) {
        if (event.getLevel() instanceof ServerLevel level) {
            EntityVisualEffectSystem.tick(level);
        }
    }
}
