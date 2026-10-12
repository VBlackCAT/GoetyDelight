package net.v_black_cat.goetydelight.mixin;

import com.mega.endinglib.api.entity.TimeStopEntity;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.v_black_cat.goetydelight.ability.TimeStopClientHandler;
import net.v_black_cat.goetydelight.entities.display_entity.DisplayEntity;
import net.minecraftforge.fml.loading.FMLEnvironment;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(DisplayEntity.class)
public abstract class DisplayEntityTimeStopMixin implements TimeStopEntity {

    @Shadow
    public abstract void tickTimeStop();

    @Shadow
    public abstract boolean isTimeStopImmune();

    @Override
    public void updateSkill(LivingEntity living, Level level) {
        if (!this.isTimeStopImmune()) {
            return;
        }

        this.tickTimeStop();

        if (level instanceof ServerLevel serverLevel) {
            serverLevel.tickNonPassenger(living);
        }
        else if (level instanceof net.minecraft.client.multiplayer.ClientLevel) {
            if (FMLEnvironment.dist.isClient()) {
                TimeStopClientHandler.tickClientLevel(level, living);
            }
        }
    }
}