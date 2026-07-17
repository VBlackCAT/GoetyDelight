package net.v_black_cat.goetydelight.mixin;

import net.minecraft.world.entity.Entity;
import net.v_black_cat.goetydelight.visual.EntityVisualEffects;
import net.v_black_cat.goetydelight.visual.IVisualEffectHolder;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Entity.class)
public abstract class EntityVisualEffectMixin implements IVisualEffectHolder {

    @Unique
    private EntityVisualEffects goetydelight$effects;

    @Override
    public EntityVisualEffects goetydelight$getVisualEffects() {
        return goetydelight$effects;
    }

    @Override
    public void goetydelight$setVisualEffects(EntityVisualEffects effects) {
        this.goetydelight$effects = effects;
    }

    @Inject(method = "remove", at = @At("HEAD"))
    private void onRemove(CallbackInfo ci) {
        this.goetydelight$effects = null;
    }
}