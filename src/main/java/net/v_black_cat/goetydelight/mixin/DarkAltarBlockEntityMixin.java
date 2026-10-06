package net.v_black_cat.goetydelight.mixin;

import com.Polarice3.Goety.common.blocks.entities.DarkAltarBlockEntity;
import com.Polarice3.Goety.common.crafting.RitualRecipe;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.v_black_cat.goetydelight.compat.goetyrevelation.GoetyRevelationBridge;
import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import static net.v_black_cat.goetydelight.item.MetamorphicScentGrassItem.metamorphicScentGrassAndFruitReciper;

@Mixin(DarkAltarBlockEntity.class)
public class DarkAltarBlockEntityMixin {

    // ==================== 已有：替换配方 ====================

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
        return metamorphicScentGrassAndFruitReciper(world, pos, player,
                player.getItemInHand(hand), value);
    }

    // ==================== 新增：仪式完成激活蛋糕（桥接） ====================

    @Inject(method = "stopRitual(Z)V", at = @At("HEAD"), remap = false)
    private void goetydelight$onRitualStop(boolean finished, CallbackInfo ci) {
        if (!finished) {
            return;
        }
        // 只调用桥接类，绝不在这里 import goetyrevelation 的类
        GoetyRevelationBridge.onRitualStop((DarkAltarBlockEntity) (Object) this);
    }
}