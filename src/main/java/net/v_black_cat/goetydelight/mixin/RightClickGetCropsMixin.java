package net.v_black_cat.goetydelight.mixin;

import cofh.rightclickgetcrops.RCGCEvents;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.v_black_cat.goetydelight.compat.goetyrevelation.item.ApocalyptiumKnifeItem;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Right Click, Get Crops 在 HIGHEST 优先级直接处理右键作物，会抢在
 * ApocalyptiumKnifeItem 的右键逻辑之前把作物收掉。
 * 手持天启镰刀时跳过它的处理，交给镰刀自身或 FTB Ultimine 连锁逻辑。
 */
@Mixin(value = RCGCEvents.class, remap = false)
public class RightClickGetCropsMixin {

    @Inject(method = "handleRightClickBlockEvent", at = @At("HEAD"), cancellable = true, remap = false)
    private static void goetydelight$skipKnifeHarvest(PlayerInteractEvent.RightClickBlock event,
                                                      CallbackInfo ci) {
        ItemStack held = event.getEntity().getMainHandItem();
        if (held.getItem() instanceof ApocalyptiumKnifeItem) {
            ci.cancel();
        }
    }
}
