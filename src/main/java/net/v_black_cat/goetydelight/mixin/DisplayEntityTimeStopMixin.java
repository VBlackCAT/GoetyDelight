package net.v_black_cat.goetydelight.mixin;

import com.mega.endinglib.api.entity.TimeStopEntity;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.v_black_cat.goetydelight.entities.display_entity.DisplayEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;

@Mixin(DisplayEntity.class)
public abstract class DisplayEntityTimeStopMixin implements TimeStopEntity {

    @Shadow(aliases = "getPersistentData")
    public abstract CompoundTag getPersistentData();

    @Unique
    private boolean goetydelight$updating = false;

    @Override
    public void updateSkill(LivingEntity entity, Level level) {
        if (goetydelight$updating) return;
        goetydelight$updating = true;
        try {
            if (!this.getPersistentData().getBoolean("TimeStopImmune")) return;
            level.guardEntityTick(Entity::tick, entity);
        } finally {
            goetydelight$updating = false;
        }
    }
}