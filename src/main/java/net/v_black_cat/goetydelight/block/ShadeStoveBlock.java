package net.v_black_cat.goetydelight.block;


import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.stats.Stats;
import net.minecraft.util.RandomSource;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.AbstractFurnaceBlock;
import net.minecraft.world.level.block.CampfireBlock;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import vectorwing.farmersdelight.common.registry.ModSounds;

public class ShadeStoveBlock extends AbstractFurnaceBlock {
    public ShadeStoveBlock(BlockBehaviour.Properties pProperties) {
        super(pProperties);
        this.registerDefaultState(this.stateDefinition.any()
                .setValue(LIT, true));
    }

    public BlockEntity newBlockEntity(BlockPos pPos, BlockState pState) {
        return new ShadeStoveBlockEntity(pPos, pState);
    }

    @Nullable
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level pLevel, BlockState pState, BlockEntityType<T> pBlockEntityType) {
        return pLevel.isClientSide ? null : createTickerHelper(pBlockEntityType, ModBlockEntities.SHADE_STOVE_BE.get(), ShadeStoveBlockEntity::serverTick);
    }
    /**
     * Called to open this furnace's container.
     *
     * @see #use
     */
    protected void openContainer(Level pLevel, BlockPos pPos, Player pPlayer) {
        BlockEntity blockentity = pLevel.getBlockEntity(pPos);
        if (blockentity instanceof ShadeStoveBlockEntity) {
            pPlayer.openMenu((MenuProvider)blockentity);
            pPlayer.awardStat(Stats.INTERACT_WITH_SMOKER);
        }

    }

    /**
     * Called periodically clientside on blocks near the player to show effects (like furnace fire particles).
     */
    public void animateTick(BlockState pState, Level pLevel, BlockPos pPos, RandomSource pRandom) {
        if (pState.getValue(LIT)) {
            double d0 = (double)pPos.getX() + 0.5D;
            double d1 = (double)pPos.getY();
            double d2 = (double)pPos.getZ() + 0.5D;
            if (pRandom.nextDouble() < 0.1D) {
                pLevel.playLocalSound(d0, d1, d2, SoundEvents.SMOKER_SMOKE, SoundSource.BLOCKS, 1.0F, 1.0F, false);
            }

            pLevel.addParticle(ParticleTypes.SMOKE, d0, d1 + 1.1D, d2, 0.0D, 0.0D, 0.0D);
        }
        if ((Boolean)pState.getValue(CampfireBlock.LIT)) {
            double x = (double)pPos.getX() + 0.5;
            double y = (double)pPos.getY();
            double z = (double)pPos.getZ() + 0.5;
            if (pRandom.nextInt(10) == 0) {
                pLevel.playLocalSound(x, y, z, (SoundEvent) ModSounds.BLOCK_STOVE_CRACKLE.get(), SoundSource.BLOCKS, 1.0F, 1.0F, false);
            }

            Direction direction = (Direction)pState.getValue(HorizontalDirectionalBlock.FACING);
            Direction.Axis direction$axis = direction.getAxis();
            double horizontalOffset = pRandom.nextDouble() * 0.6 - 0.3;
            double xOffset = direction$axis == Direction.Axis.X ? (double)direction.getStepX() * 0.52 : horizontalOffset;
            double yOffset = pRandom.nextDouble() * 6.0 / 16.0;
            double zOffset = direction$axis == Direction.Axis.Z ? (double)direction.getStepZ() * 0.52 : horizontalOffset;
            pLevel.addParticle(ParticleTypes.SMOKE, x + xOffset, y + yOffset, z + zOffset, 0.0, 0.0, 0.0);
            pLevel.addParticle(ParticleTypes.FLAME, x + xOffset, y + yOffset, z + zOffset, 0.0, 0.0, 0.0);
        }

    }
}