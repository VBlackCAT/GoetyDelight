package net.v_black_cat.goetydelight.mixin;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.v_black_cat.goetydelight.util.SoulEnchantUtil;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * 让「物品自己申报」附魔效果，替代玩家级的每 tick 全槽位扫描。
 *
 * <p>{@code ItemStack#inventoryTick} 由 {@code Inventory#tick()} 对玩家背包里每个非空格子调用
 * （主背包 0-35、盔甲 36-39、副手 40），因此这里能拿到「哪一格、属于谁」，而无需
 * {@code player.getAllSlots()} 快照。真正的附魔判定与节流在 {@link SoulEnchantUtil} 里，
 * 没有相关附魔的物品会在最廉价的判断处直接返回。
 */
@Mixin(ItemStack.class)
public abstract class ItemStackMixin {

    @Inject(method = "inventoryTick", at = @At("HEAD"))
    private void goetydelight$soulEnchantTick(Level level, Entity entity, int slotId, boolean isSelected,
            CallbackInfo ci) {
        if (entity instanceof LivingEntity living) {
            SoulEnchantUtil.onInventoryTick((ItemStack) (Object) this, living);
        }
    }
}
