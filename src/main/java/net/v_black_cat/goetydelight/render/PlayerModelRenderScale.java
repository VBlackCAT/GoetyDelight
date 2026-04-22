package net.v_black_cat.goetydelight.render;

import net.minecraft.world.entity.player.Player;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.event.RenderPlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.v_black_cat.goetydelight.config.Config;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@OnlyIn(Dist.CLIENT)
@Mod.EventBusSubscriber(modid = "goetydelight", value = Dist.CLIENT)
public class PlayerModelRenderScale {

    private static final Map<String, Float> PLAYER_SCALE_MAP = new ConcurrentHashMap<>();

    private static boolean configLoaded = false;

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
        if (scale > 0) {
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
        return PLAYER_SCALE_MAP.getOrDefault(playerName, 1.0f);
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

        if (PLAYER_SCALE_MAP.containsKey(playerName) || (!configLoaded && getPlayerScale(playerName) != 1.0f)) {
            float scale = getPlayerScale(playerName);
            if (scale != 1.0f) {
                event.getPoseStack().pushPose();
                event.getPoseStack().scale(scale, scale, scale);
            }
        }
    }

    @SubscribeEvent
    public static void onPlayerRenderPost(RenderPlayerEvent.Post event) {
        Player player = event.getEntity();
        String playerName = player.getName().getString();

        if (PLAYER_SCALE_MAP.containsKey(playerName) || (!configLoaded && getPlayerScale(playerName) != 1.0f)) {
            float scale = getPlayerScale(playerName);
            if (scale != 1.0f) {
                event.getPoseStack().popPose();
            }
        }
    }
}
