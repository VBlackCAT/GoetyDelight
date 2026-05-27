package net.v_black_cat.goetydelight.visual.client;

import net.minecraft.client.CameraType;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RenderLevelStageEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.v_black_cat.goetydelight.GoetyDelight;
import net.v_black_cat.goetydelight.visual.ActiveEntityVisualEffect;
import net.v_black_cat.goetydelight.visual.EntityVisualEffectType;
import net.v_black_cat.goetydelight.visual.EntityVisualEffectSystem;
import net.v_black_cat.goetydelight.visual.GDVisualEffects;

@Mod.EventBusSubscriber(modid = GoetyDelight.MODID, value = Dist.CLIENT)
public final class EntityVisualEffectRenderDispatcher {
    private static boolean registeredDefaults;

    private EntityVisualEffectRenderDispatcher() {
    }

    @SubscribeEvent
    public static void onRenderLevelStage(RenderLevelStageEvent event) {
        if (event.getStage() != RenderLevelStageEvent.Stage.AFTER_PARTICLES) {
            return;
        }

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

            entity.getCapability(EntityVisualEffectSystem.ENTITY_VISUAL_EFFECTS).ifPresent(effects -> effects.effects().forEach(effect -> {
                EntityVisualEffectType type = GDVisualEffects.get(effect.id());
                EntityVisualEffectRenderer renderer = EntityVisualEffectRenderers.get(effect.id());
                if (type != null && renderer != null && shouldRender(minecraft, event, entity, effect, type)) {
                    renderer.render(event, entity, effect);
                }
            }));
        }
    }

    private static void ensureDefaultsRegistered() {
        if (!registeredDefaults) {
            EntityVisualEffectRenderers.registerDefaults();
            registeredDefaults = true;
        }
    }

    private static boolean shouldRender(Minecraft minecraft, RenderLevelStageEvent event, Entity entity, ActiveEntityVisualEffect effect, EntityVisualEffectType type) {
        if (entity.isInvisible() && !type.shouldRenderInvisibleEntity()) {
            return false;
        }

        if (type.hasRenderDistanceLimit() && entity.distanceToSqr(event.getCamera().getPosition()) > type.renderDistance() * type.renderDistance()) {
            return false;
        }

        LocalPlayer player = minecraft.player;
        return entity != player || type.renderInFirstPerson() || !minecraft.options.getCameraType().isFirstPerson() || !(entity instanceof Player);
    }
}
