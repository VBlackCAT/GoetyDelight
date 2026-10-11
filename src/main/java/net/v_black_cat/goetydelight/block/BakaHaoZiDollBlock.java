package net.v_black_cat.goetydelight.block;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.fluids.FluidUtil;
import net.minecraftforge.fluids.capability.IFluidHandler;

import javax.annotation.Nullable;

public class BakaHaoZiDollBlock extends DollBlock {

    @Override
    public @Nullable BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new BakaHaoZiBlockEntity(pos, state);
    }

    @Override
    public InteractionResult use(BlockState state, Level level, BlockPos pos, Player player,
                                 InteractionHand hand, BlockHitResult hitResult) {
        if (!level.isClientSide) {
            BlockEntity blockEntity = level.getBlockEntity(pos);
            if (blockEntity != null) {
                Direction side = hitResult.getDirection();
                IFluidHandler handler = blockEntity
                        .getCapability(ForgeCapabilities.FLUID_HANDLER, side)
                        .orElse(null);
                if (handler != null && FluidUtil.interactWithFluidHandler(player, hand, handler)) {
                    return InteractionResult.SUCCESS;
                }
            }
        }
        // 未持有流体容器：走普通玩偶交互（音符粒子 + 音效）
        return super.use(state, level, pos, player, hand, hitResult);
    }
}
