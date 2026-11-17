package net.v_black_cat.goetydelight.mixin;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.v_black_cat.goetydelight.compat.goetyrevelation.item.AscensionLollipopItem;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * 旅行者背包（Traveler's Backpack）喂食升级兼容。
 * <p>
 * 其 {@code FeedingUpgrade.isEdible(ItemStack, LivingEntity)} 同样只接受
 * {@code nutrition >= 1} 的食物，登神棒棒糖（饱食度 -1）因此不会被自动喂食。
 * 这里对该物品放行，并在冷却中跳过。
 * <p>
 * 旅行者背包是可选联动，编译期不强制依赖；{@link Pseudo} 允许目标类在编译期不存在，
 * 运行时由 {@code GoetyDelightMixinPlugin} 根据模组是否加载来决定是否应用。
 */
@Pseudo
@Mixin(targets = "com.tiviacz.travelersbackpack.inventory.upgrades.feeding.FeedingUpgrade")
public class TravelersBackpackFeedingUpgradeMixin {

    @Inject(method = "isEdible", at = @At("HEAD"), cancellable = true, remap = false)
    private static void goetydelight$allowAscensionLollipop(ItemStack stack, LivingEntity entity,
                                                            CallbackInfoReturnable<Boolean> cir) {
        if (stack.getItem() instanceof AscensionLollipopItem) {
            if (entity instanceof Player player) {
                if (AscensionLollipopItem.isFeedOnCooldown(player)) {
                    cir.setReturnValue(false);
                    return;
                }
                // 不依赖 tryFeedingStack 注入：isEdible 返回 true 时直接开始 5 秒冷却。
                AscensionLollipopItem.startFeedCooldown(player);
            }
            cir.setReturnValue(true);
        }
    }
}
