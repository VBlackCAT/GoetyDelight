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

    @Inject(
            method = "activate",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/entity/player/Player;displayClientMessage(Lnet/minecraft/network/chat/Component;Z)V",
                    ordinal = 11,      
                    shift = At.Shift.BEFORE
            ),
            cancellable = true
    )
    private void onNoMatchingRitual(Level world, BlockPos pos, Player player,
                                    InteractionHand hand, Direction face,
                                    CallbackInfoReturnable<Boolean> cir) {
        ItemStack activationItem = player.getItemInHand(hand);
        RitualRecipe custom = metamorphicScentGrassAndFruitReciper(
                world, pos, player,
                activationItem,
                null
        );

        if (custom != null) {
            DarkAltarBlockEntity self = (DarkAltarBlockEntity) (Object) this;
            self.startRitual(player, activationItem, custom, custom.getId());
            cir.setReturnValue(true);
            cir.cancel();
        }
    }
}