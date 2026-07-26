package net.v_black_cat.goetydelight.mixin;

import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.resources.PlayerSkin;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@OnlyIn(Dist.CLIENT)
@Mixin(AbstractClientPlayer.class)
public abstract class PlayerSkinMixin {

    @Unique
    // 皮肤
    private static final ResourceLocation BAKA_SKIN_TEX = ResourceLocation.fromNamespaceAndPath("goetydelight", "textures/player/5152.png");

    @Unique
    // 披风
    private static final ResourceLocation BAKA_CAPE_TEX = ResourceLocation.fromNamespaceAndPath("goetydelight", "textures/player/5152_cape.png");

    @Unique
    private static final PlayerSkin BAKA_SKIN = new PlayerSkin(
    BAKA_SKIN_TEX, null, BAKA_CAPE_TEX, null, PlayerSkin.Model.SLIM, false
    );

    @Inject(method = "getSkin", at = @At("HEAD"), cancellable = true)
    private void replaceSkinIfEquipped(CallbackInfoReturnable<PlayerSkin> cir) {
        AbstractClientPlayer player = (AbstractClientPlayer) (Object) this;
        if (!player.level().isClientSide) return;
        // 玩家名判断
        String playerName = player.getName().getString();
        if ("dev".equalsIgnoreCase(playerName)) {
            cir.setReturnValue(BAKA_SKIN);
        }
    }
}