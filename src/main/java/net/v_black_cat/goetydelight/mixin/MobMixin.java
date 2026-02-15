package net.v_black_cat.goetydelight.mixin;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.player.Player;
import net.v_black_cat.goetydelight.item.FalseProverbsItem;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Mob.class)
public abstract class MobMixin {
    @Inject(method = "setTarget", at = @At("HEAD"), cancellable = true)
    private void onSetTarget(LivingEntity target, CallbackInfo ci) {
        if (target instanceof Player player){
            if (player.getMainHandItem().getItem() instanceof FalseProverbsItem item) {
                if(FalseProverbsItem.getPlayerTeleportStatus(player.getUUID())){
                if(player.isShiftKeyDown()){
                ci.cancel();}}
            }
        }
    }
}
