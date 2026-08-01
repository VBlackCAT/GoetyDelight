package net.v_black_cat.goetydelight.mixin;

import com.Polarice3.Goety.common.blocks.entities.DarkAltarBlockEntity;
import com.Polarice3.Goety.common.crafting.RitualRecipe;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import static net.v_black_cat.goetydelight.item.food.MetamorphicScentGrassItem.metamorphicScentGrassAndFruitReciper;

@Mixin(DarkAltarBlockEntity.class)
public class DarkAltarBlockEntityMixin {

    /**
     * Goety 3.1.0 重构：activate 里移除了 logRitualMatchFailure 调用，
     * "无匹配仪式"失败分支内联为 player.displayClientMessage("info.goety.ritual.itemProblem.fail", true)。
     * 该调用是 activate 中第 10 次（ordinal=9）displayClientMessage，对应 ritualRecipe == null 分支
     * （已用 javap 对照 3.1.0 字节码确认）。若 Goety 后续增删提示消息，需要重新核对 ordinal。
     */
    @Inject(
            method = "activate(Lnet/minecraft/world/level/Level;Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/entity/player/Player;Lnet/minecraft/world/InteractionHand;Lnet/minecraft/core/Direction;)Z",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/entity/player/Player;displayClientMessage(Lnet/minecraft/network/chat/Component;Z)V",
                    ordinal = 9
            ),
            cancellable = true,
            remap = false
    )
    private void onRitualMatchFailure(Level world,
                                      BlockPos pos,
                                      Player player,
                                      InteractionHand hand,
                                      Direction face,
                                      CallbackInfoReturnable<Boolean> cir) {
        // 3.1.0 不再暴露 activationItem 局部变量给注入器，这里等价地重新取一次主手物品
        ItemStack activationItem = player.getItemInHand(hand);

        RitualRecipe custom = metamorphicScentGrassAndFruitReciper(
                world, pos, player,
                activationItem,
                null
        );

        if (custom == null) {
            return;
        }

        DarkAltarBlockEntity self = (DarkAltarBlockEntity) (Object) this;
        self.startRitual(player, activationItem, custom, custom.getId());
        cir.setReturnValue(true);
    }
}
