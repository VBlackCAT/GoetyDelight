package net.v_black_cat.goetydelight.mixin;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.Entity;
import net.v_black_cat.goetydelight.visual.EntityVisualEffects;
import net.v_black_cat.goetydelight.visual.IVisualEffectHolder;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Entity.class)
public abstract class EntityVisualEffectMixin implements IVisualEffectHolder {

    @Unique
    private static final String GOETYDELIGHT_VISUAL_EFFECTS = "GoetyDelightVisualEffects";

    @Unique
    private EntityVisualEffects goetydelight$effects = new EntityVisualEffects();

    @Override
    public EntityVisualEffects goetydelight$getVisualEffects() {
        return goetydelight$effects;
    }

    @Override
    public void goetydelight$setVisualEffects(EntityVisualEffects effects) {
        this.goetydelight$effects = effects;
    }

    @Inject(method = "saveWithoutId", at = @At("RETURN"))
    private void goetydelight$saveVisualEffects(CompoundTag tag,
                                                CallbackInfoReturnable<CompoundTag> callback) {
        CompoundTag serialized = goetydelight$effects.serializeNBT();
        if (!serialized.isEmpty()) {
            tag.put(GOETYDELIGHT_VISUAL_EFFECTS, serialized);
        }
    }

    @Inject(method = "load", at = @At("RETURN"))
    private void goetydelight$loadVisualEffects(CompoundTag tag, CallbackInfo callback) {
        if (tag.contains(GOETYDELIGHT_VISUAL_EFFECTS)) {
            goetydelight$effects.deserializeNBT(tag.getCompound(GOETYDELIGHT_VISUAL_EFFECTS));
        }
    }
}
