// Source code is decompiled from a .class file using FernFlower decompiler (from Intellij IDEA).
package net.v_black_cat.goetydelight.render;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.event.RenderPlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.v_black_cat.goetydelight.config.Config;

@OnlyIn(Dist.CLIENT)
@EventBusSubscriber(
        modid = "goetydelight",
        value = {Dist.CLIENT}
)
public class PlayerModelRenderScale {
    private static final Map<String, Float> PLAYER_SCALE_MAP = new ConcurrentHashMap();
    private static boolean configLoaded = false;

    public PlayerModelRenderScale() {
    }

    public static void loadPlayerScalesFromConfig() {
        if (!configLoaded) {
            PLAYER_SCALE_MAP.clear();
            PLAYER_SCALE_MAP.putAll(Config.getPlayerModelScales());
            configLoaded = true;
        }

    }

    public static void reloadFromConfig() {
        PLAYER_SCALE_MAP.clear();
        PLAYER_SCALE_MAP.putAll(Config.getPlayerModelScales());
    }

    public static void setPlayerScale(String playerName, float scale) {
        if (scale > 0.0F) {
            PLAYER_SCALE_MAP.put(playerName, scale);
        } else {
            PLAYER_SCALE_MAP.remove(playerName);
        }

    }

    public static void setPlayerScale(Player player, float scale) {
        setPlayerScale(player.getName().getString(), scale);
    }

    public static float getPlayerScale(String playerName) {
        if (!configLoaded) {
            loadPlayerScalesFromConfig();
        }

        return (Float)PLAYER_SCALE_MAP.getOrDefault(playerName, 1.0F);
    }

    public static void clearPlayerScale(String playerName) {
        PLAYER_SCALE_MAP.remove(playerName);
    }

    public static void clearPlayerScale(Player player) {
        clearPlayerScale(player.getName().getString());
    }

    @SubscribeEvent
    public static void onPlayerRenderPre(RenderPlayerEvent.Pre event) {
        Player player = event.getEntity();
        String playerName = player.getName().getString();
        if (PLAYER_SCALE_MAP.containsKey(playerName) || !configLoaded && getPlayerScale(playerName) != 1.0F) {
            float scale = getPlayerScale(playerName);
            if (scale != 1.0F) {
                event.getPoseStack().pushPose();
                event.getPoseStack().scale(scale, scale, scale);
            }
        }

    }

    @SubscribeEvent
    public static void onPlayerRenderPost(RenderPlayerEvent.Post event) {
        Player player = event.getEntity();
        String playerName = player.getName().getString();
        if (PLAYER_SCALE_MAP.containsKey(playerName) || !configLoaded && getPlayerScale(playerName) != 1.0F) {
            float scale = getPlayerScale(playerName);
            if (scale != 1.0F) {
                event.getPoseStack().popPose();
            }
        }

    }
}
