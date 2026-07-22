package net.v_black_cat.goetydelight.events;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
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

    public static void onStartTracking(PlayerEvent.StartTracking event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            EntityVisualEffectSystem.sendToPlayer(event.getTarget(), player);
        }
    }

    public static void onPlayerLoggedIn(PlayerEvent.PlayerLoggedInEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            EntityVisualEffectSystem.sendTrackedEffectsTo(player);
        }
    }

    public static void onPlayerClone(PlayerEvent.Clone event) {
        if (event.getEntity().level().isClientSide) return;
        var original = EntityVisualEffectSystem.getEffects(event.getOriginal());
        var replacement = EntityVisualEffectSystem.getEffects(event.getEntity());
        if (original != null && replacement != null) {
            replacement.deserializeNBT(original.serializeNBT());
        }
    }
}
