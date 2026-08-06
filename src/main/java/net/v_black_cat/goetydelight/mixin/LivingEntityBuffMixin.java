package net.v_black_cat.goetydelight.mixin;

import net.minecraft.world.entity.LivingEntity;
import net.v_black_cat.goetydelight.buff.ActiveBuffs;
import net.v_black_cat.goetydelight.buff.IBuffHolder;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LivingEntity.class)
public abstract class LivingEntityBuffMixin implements IBuffHolder {

    @Unique
    private ActiveBuffs goetydelight$activeBuffs;

    @Override
    public ActiveBuffs goetydelight$getActiveBuffs() {
        return goetydelight$activeBuffs;
    }

    @Override
    public void goetydelight$setActiveBuffs(ActiveBuffs buffs) {
        this.goetydelight$activeBuffs = buffs;
    }

    @Inject(method = "remove", at = @At("HEAD"))
    private void onRemove(CallbackInfo ci) {
        if (this.goetydelight$activeBuffs != null) {
            this.goetydelight$activeBuffs.clear();
            this.goetydelight$activeBuffs = null;
        }
    }
}