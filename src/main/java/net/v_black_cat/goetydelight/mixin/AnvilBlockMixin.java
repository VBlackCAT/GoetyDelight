package net.v_black_cat.goetydelight.mixin;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.item.FallingBlockEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.AnvilBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.common.NeoForge;
import net.v_black_cat.goetydelight.events.AnvilLandInBlockEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(AnvilBlock.class)
public class AnvilBlockMixin {

    @Inject(at = @At("TAIL"), method = "onLand", require = 1)
    public void onAnvilLand(Level level, BlockPos pos, BlockState newState, BlockState oldState, FallingBlockEntity entity, CallbackInfo ci) {

        if (!level.isClientSide) {
            NeoForge.EVENT_BUS.post(new AnvilLandInBlockEvent(level, pos, newState, oldState, entity));
        }
    }
}