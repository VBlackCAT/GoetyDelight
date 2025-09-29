package net.v_black_cat.goetydelight.mixin;

import com.Polarice3.Goety.utils.BlockFinder;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.player.Player;
import net.v_black_cat.goetydelight.effect.ModEffects;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(BlockFinder.class)
public class BlockFinderMixin {

    @Inject(method = "findIllagerWard", at = @At("HEAD"), cancellable = true, remap = false)
    private static void onFindIllagerWard(ServerLevel level, Player player, int soulEnergy, CallbackInfoReturnable<Boolean> cir) {
        if (player.hasEffect(ModEffects.HUNTING_DENIAL.get())) {
            cir.setReturnValue(true);
        }
    }
}