package net.v_black_cat.goetydelight.mixin;

import net.minecraft.world.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import static net.v_black_cat.goetydelight.item.FalseProverbsItem.SHIFT_KEY_TAG;

@Mixin(LivingEntity.class)
public abstract class LivingEntityMixin {
    @Inject(method = "updateInvisibilityStatus", at = @At(value = "TAIL"))
    public void updateInvisibilityStatus(CallbackInfo ci) {
        LivingEntity entity = (LivingEntity) (Object) this;
        if (entity.getPersistentData().getBoolean(SHIFT_KEY_TAG))
            entity.setInvisible(true);
    }

    @Inject(method = "getArmorCoverPercentage", at = @At(value = "HEAD"), cancellable = true)
    public void getArmorCoverPercentage(CallbackInfoReturnable<Float> cir) {
        LivingEntity entity = (LivingEntity) (Object) this;
        if (entity.getPersistentData().getBoolean(SHIFT_KEY_TAG)) {
            cir.setReturnValue(0f);
        }
    }
}
