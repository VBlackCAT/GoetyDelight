package net.v_black_cat.goetydelight.mixin;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.p3pp3rf1y.sophisticatedcore.upgrades.feeding.FeedingUpgradeWrapper;
import net.v_black_cat.goetydelight.compat.goetyrevelation.item.AscensionLollipopItem;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * 精妙背包（Sophisticated Backpacks / Sophisticated Core）喂食插件兼容。
 * <p>
 * 其 {@code FeedingUpgradeWrapper.isEdible(ItemStack, LivingEntity)} 只把
 * {@code nutrition >= 1} 的食物当作可喂食，而登神棒棒糖的饱食度故意设为 -1，
 * 因此会被识别为食物却永远不会自动喂食。这里对登神棒棒糖放行，并在冷却中跳过。
 */
@Mixin(FeedingUpgradeWrapper.class)
public class SophisticatedFeedingUpgradeMixin {

    @Inject(method = "isEdible", at = @At("HEAD"), cancellable = true, remap = false)
    private static void goetydelight$allowAscensionLollipop(ItemStack stack, LivingEntity entity,
                                                            CallbackInfoReturnable<Boolean> cir) {
        if (stack.getItem() instanceof AscensionLollipopItem) {
            if (entity instanceof Player player) {
                if (AscensionLollipopItem.isFeedOnCooldown(player)) {
                    cir.setReturnValue(false);
                    return;
                }
                // 这里直接开始冷却：不依赖 tryFeedingStack 的注入是否成功。
                // isEdible 在喂食插件里只会于真正尝试喂食前被调用，等 5 秒后才会再次放行。
                AscensionLollipopItem.startFeedCooldown(player);
            }
            cir.setReturnValue(true);
        }
    }
}
