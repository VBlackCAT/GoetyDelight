package net.v_black_cat.goetydelight.ability;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class TimeStopClientHandler {

    public static void tickClientLevel(Level level, LivingEntity living) {
        if (level instanceof ClientLevel clientLevel) {
            clientLevel.tickNonPassenger(living);
        }
    }
}
