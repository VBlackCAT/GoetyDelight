package net.v_black_cat.goetydelight.mixin;

import com.mega.endinglib.api.entity.TimeStopEntity;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.v_black_cat.goetydelight.compat.EndingLibrary.EndingLibraryCompat;
import net.v_black_cat.goetydelight.entities.display_entity.DisplayEntity;

import org.spongepowered.asm.mixin.Mixin;

@Mixin(DisplayEntity.class)
public abstract class DisplayEntityTimeStopMixin implements TimeStopEntity {

    @Override
    public void updateSkill(LivingEntity living, Level level) {
        if (!EndingLibraryCompat.isAtLeastMinVersion()) {
            return;
        }

        if (level instanceof ServerLevel serverLevel) {
            serverLevel.tickNonPassenger(living);
        } else if (level.getClass().getName().equals("net.minecraft.client.multiplayer.ClientLevel")) {
        // 反射调用，避免符号引用
        try {
            Class<?> handler = Class.forName("net.v_black_cat.goetydelight.ability.TimeStopClientHandler");
            handler.getMethod("tickClientLevel", Level.class, LivingEntity.class)
                    .invoke(null, level, living);
        } catch (Exception ignored) {}
    }
    }

    @Override
    public boolean smoothPartialTicks() {
        if (!EndingLibraryCompat.isAtLeastMinVersion()) {
            return false;
        }
        return true;
    }
}