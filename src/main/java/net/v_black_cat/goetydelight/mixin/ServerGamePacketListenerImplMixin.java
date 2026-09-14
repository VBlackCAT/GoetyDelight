package net.v_black_cat.goetydelight.mixin;

import net.minecraft.network.protocol.game.ServerboundSetCarriedItemPacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import net.v_black_cat.goetydelight.item.FalseProverbsItem;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ServerGamePacketListenerImpl.class)
public abstract class ServerGamePacketListenerImplMixin {

    @Shadow public ServerPlayer player;

    @Inject(method = "handleSetCarriedItem", at = @At("TAIL"))
    private void goetydelight$refreshBackSlotOnHotbarSwitch(ServerboundSetCarriedItemPacket packet, CallbackInfo ci) {
        FalseProverbsItem.refreshBackSlot(this.player);
    }
}
