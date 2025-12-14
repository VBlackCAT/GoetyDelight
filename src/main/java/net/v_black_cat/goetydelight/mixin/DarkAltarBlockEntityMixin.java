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
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.Slice;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

import static net.v_black_cat.goetydelight.item.MetamorphicScentGrassItem.metamorphicScentGrassReciper;

@Mixin(DarkAltarBlockEntity.class)
public class DarkAltarBlockEntityMixin {
    @ModifyVariable(
            method = "activate",
            at = @At(
                    value = "STORE",
                    opcode = Opcodes.ASTORE,
                    ordinal = 0
            ),
            name = "ritualRecipe",
            remap = false
    )
    private RitualRecipe modifyRecipe(RitualRecipe value,
                                      Level world,
                                      BlockPos pos,
                                      Player player,
                                      InteractionHand hand,
                                      Direction face) {
        return metamorphicScentGrassReciper(world, pos, player,
                player.getItemInHand(hand), value);
    }


}