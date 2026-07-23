package net.v_black_cat.goetydelight.visual.client;

import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.neoforged.neoforge.client.event.RenderLevelStageEvent;
import net.v_black_cat.goetydelight.compat.OculusCompat;
import net.v_black_cat.goetydelight.visual.*;

public final class EntityVisualEffectRenderDispatcher {

    private static boolean registeredDefaults;


    private EntityVisualEffectRenderDispatcher() {
    }


    public static void onRenderLevelStage(RenderLevelStageEvent event) {

        if (event.getStage() == RenderLevelStageEvent.Stage.AFTER_LEVEL) {

            RenderLevelStageEvent cachedEvent =
                    LateShaderPackRenderContext.consumeAfterParticles();


            if (cachedEvent != null
                    && OculusCompat.isShaderPackInUse()) {

                Minecraft.getInstance()
                        .getMainRenderTarget()
                        .bindWrite(false);

                render(cachedEvent);
            }


            return;
        }


        if (event.getStage() != RenderLevelStageEvent.Stage.AFTER_PARTICLES) {
            return;
        }


        if (OculusCompat.isShaderPackInUse()) {

            LateShaderPackRenderContext.captureAfterParticles(event);

            return;
        }


        render(event);
    }


    private static void render(RenderLevelStageEvent event) {

        Minecraft minecraft = Minecraft.getInstance();

        ClientLevel level = minecraft.level;


        if (level == null || minecraft.player == null) {
            return;
        }


        ensureDefaultsRegistered();


        for (Entity entity : level.entitiesForRendering()) {


            if (entity.isRemoved()) {
                continue;
            }


            EntityVisualEffects effects = EntityVisualEffectSystem.getEffects(entity);


            if (effects == null || effects.isEmpty()) {
                continue;
            }


            renderEffects(
                    minecraft,
                    event,
                    entity,
                    effects
            );
        }
    }


    private static void renderEffects(
            Minecraft minecraft,
            RenderLevelStageEvent event,
            Entity entity,
            EntityVisualEffects effects
    ) {


        for (ActiveEntityVisualEffect effect : effects.effects()) {


            EntityVisualEffectType type =
                    GDVisualEffects.get(effect.id());


            EntityVisualEffectRenderer renderer =
                    EntityVisualEffectRenderers.get(effect.id());


            if (type == null || renderer == null) {
                continue;
            }


            if (!shouldRender(
                    minecraft,
                    event,
                    entity,
                    type
            )) {
                continue;
            }


            renderer.render(
                    event,
                    entity,
                    effect
            );
        }
    }


    private static void ensureDefaultsRegistered() {

        if (!registeredDefaults) {

            EntityVisualEffectRenderers.registerDefaults();

            registeredDefaults = true;
        }
    }


    private static boolean shouldRender(
            Minecraft minecraft,
            RenderLevelStageEvent event,
            Entity entity,
            EntityVisualEffectType type
    ) {


        if (entity.isInvisible()
                && !type.shouldRenderInvisibleEntity()) {

            return false;
        }


        if (type.hasRenderDistanceLimit()) {


            double distance =
                    entity.distanceToSqr(
                            event.getCamera().getPosition()
                    );


            double limit =
                    type.renderDistance()
                            * type.renderDistance();


            if (distance > limit) {
                return false;
            }
        }


        LocalPlayer player = minecraft.player;


        return entity != player
                || type.renderInFirstPerson()
                || !minecraft.options.getCameraType().isFirstPerson()
                || !(entity instanceof Player);
    }
}
