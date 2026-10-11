package net.v_black_cat.goetydelight.mixin;

import com.mega.endinglib.util.mixin.level.ServerExpandedContext;
import com.mega.endinglib.util.time.TimeStopUtils;
import net.v_black_cat.goetydelight.compat.goetyrevelation.item.ApollyonCakeSliceItem;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * 监听服务端时停状态的下降沿（true -> false），触发清除。
 * <p>
 * EL 每 tick 在 MinecraftServer.tickServer 的 HEAD 调 ServerExpandedContext.update()，
 * 这里 Inject 到它的 RETURN，只在 isTimeStop 从 true 变 false 时触发一次。
 */
@Mixin(ServerExpandedContext.class)
public class ServerExpandedContextMixin {

    @Unique
    private boolean goetydelight$lastTimeStop = false;

    @Inject(method = "update", at = @At("RETURN"), remap = false)
    private void goetydelight$onUpdate(CallbackInfo ci) {
        boolean now = TimeStopUtils.isTimeStop;
        if (goetydelight$lastTimeStop && !now) {
            // 时停从 true -> false，结束
            ApollyonCakeSliceItem.onTimeStopEnd();
        }
        goetydelight$lastTimeStop = now;
    }
}