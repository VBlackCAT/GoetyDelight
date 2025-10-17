package net.v_black_cat.goetydelight.mixin;

import com.Polarice3.Goety.common.events.WightSpawner;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.player.Player;
import net.v_black_cat.goetydelight.effect.ModEffects;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(WightSpawner.class)
public class WightSpawnerMixin {

    @Inject(
            method = "summonWight(Lnet/minecraft/server/level/ServerLevel;Lnet/minecraft/world/entity/player/Player;I)Z",
            at = @At("HEAD"),
            cancellable = true,
            remap = false
    )
    private static void checkWightDenialEffect(ServerLevel serverLevel, Player player, int sePercent, CallbackInfoReturnable<Boolean> cir) {
        // 检查玩家是否有尸鬼拒止效果
        if (player != null && player.hasEffect(ModEffects.WIGHT_DENIAL.get())) {
            // 取消尸鬼生成
            cir.setReturnValue(false);
        }
    }
}