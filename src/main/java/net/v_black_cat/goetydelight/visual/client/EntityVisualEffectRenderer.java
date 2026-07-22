package net.v_black_cat.goetydelight.visual.client;

import net.minecraft.world.entity.Entity;
import net.neoforged.neoforge.client.event.RenderLevelStageEvent;
import net.v_black_cat.goetydelight.visual.ActiveEntityVisualEffect;

@FunctionalInterface
public interface EntityVisualEffectRenderer {
    void render(RenderLevelStageEvent event, Entity entity, ActiveEntityVisualEffect effect);
}

