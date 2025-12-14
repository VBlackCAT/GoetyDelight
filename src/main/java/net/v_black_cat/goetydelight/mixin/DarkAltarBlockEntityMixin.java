package net.v_black_cat.goetydelight.mixin;

import com.Polarice3.Goety.common.blocks.entities.DarkAltarBlockEntity;
import com.Polarice3.Goety.common.crafting.RitualRecipe;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Slice;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

import static net.v_black_cat.goetydelight.item.MetamorphicScentGrassItem.metamorphicScentGrassReciper;

@Mixin(DarkAltarBlockEntity.class)
public class DarkAltarBlockEntityMixin {

    @Inject(
            method = "activate",
            at = @At(
                    value = "JUMP",
                    opcode = Opcodes.IFNULL,
                    ordinal = 0,
                    remap = false),
            slice = @Slice(
                from = @At(value = "INVOKE",
                        target = "Ljava/util/Optional;orElse(Ljava/lang/Object;)Ljava/lang/Object;",
                        shift = At.Shift.AFTER
                ),
                to = @At(value = "INVOKE",
                        target = "Lnet/minecraft/world/entity/player/Player;displayClientMessage(Lnet/minecraft/network/chat/Component;Z)V",
                        ordinal = 0)
                )
            ,
            locals = LocalCapture.CAPTURE_FAILHARD,
            remap = false
    )
    private void afterRecipe(Level world, BlockPos pos, Player player,
                                              InteractionHand hand, Direction face,
                                              CallbackInfoReturnable<Boolean> cir,
                                              ItemStack activationItem, RitualRecipe ritualRecipe) {
        metamorphicScentGrassReciper(world, pos, player, hand, face, activationItem, ritualRecipe);
    }

}