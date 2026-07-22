package net.v_black_cat.goetydelight.block;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.stats.Stats;
import net.minecraft.util.RandomSource;
import net.minecraft.world.Containers;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.AbstractFurnaceBlock;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.v_black_cat.goetydelight.init.ModBlockEntities;
import vectorwing.farmersdelight.common.registry.ModSounds;

import javax.annotation.Nullable;

public class ShadeStoveBlock extends AbstractFurnaceBlock {
    public ShadeStoveBlock(BlockBehaviour.Properties pProperties) {
        super(pProperties);
        this.registerDefaultState(this.stateDefinition.any().setValue(LIT, false));
    }

    @Override
    public MapCodec<? extends AbstractFurnaceBlock> codec() {
        return simpleCodec(ShadeStoveBlock::new);
    }

    @Override
    public BlockEntity newBlockEntity(BlockPos pPos, BlockState pState) {
        return new ShadeStoveBlockEntity(pPos, pState);
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level pLevel, BlockState pState, BlockEntityType<T> pBlockEntityType) {
        if (pLevel.isClientSide) return null;
        return createTickerHelper(pBlockEntityType, ModBlockEntities.SHADE_STOVE_BE.get(), ShadeStoveBlockEntity::serverTick);
    }

    @Override
    public void entityInside(BlockState state, Level level, BlockPos pos, Entity entity) {
        if (level.isClientSide) return;
        if (!state.getValue(LIT)) return;

        if (entity instanceof LivingEntity living) {
            if (!living.isInvulnerableTo(level.damageSources().hotFloor())) {
                if (living.tickCount % 20 == 0) {
                    living.hurt(level.damageSources().hotFloor(), 1.0F);
                }
            }
        }
    }

    @Override
    public InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hitResult) {
        if (!level.isClientSide) {
            BlockEntity be = level.getBlockEntity(pos);
            if (be instanceof ShadeStoveBlockEntity) {
                player.openMenu((MenuProvider) be);
                player.awardStat(Stats.INTERACT_WITH_SMOKER);
                return InteractionResult.CONSUME;
            }
        }
        return InteractionResult.SUCCESS;
    }

    @Override
    public ItemInteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hitResult) {
        if (!level.isClientSide) {
            BlockEntity be = level.getBlockEntity(pos);
            if (be instanceof ShadeStoveBlockEntity) {
                player.openMenu((MenuProvider) be);
                player.awardStat(Stats.INTERACT_WITH_SMOKER);
                return ItemInteractionResult.CONSUME;
            }
        }
        return ItemInteractionResult.SUCCESS;
    }

    @Override
    protected void openContainer(Level pLevel, BlockPos pPos, Player pPlayer) {
        BlockEntity be = pLevel.getBlockEntity(pPos);
        if (be instanceof ShadeStoveBlockEntity) {
            pPlayer.openMenu((MenuProvider) be);
            pPlayer.awardStat(Stats.INTERACT_WITH_SMOKER);
        }
    }

    @Override
    public void animateTick(BlockState pState, Level pLevel, BlockPos pPos, RandomSource pRandom) {
        if (pState.getValue(LIT)) {
            double d0 = (double) pPos.getX() + 0.5D;
            double d1 = (double) pPos.getY();
            double d2 = (double) pPos.getZ() + 0.5D;

            if (pRandom.nextDouble() < 0.1D) {
                pLevel.playLocalSound(d0, d1, d2, SoundEvents.SMOKER_SMOKE, SoundSource.BLOCKS, 1.0F, 1.0F, false);
            }
            pLevel.addParticle(ParticleTypes.SMOKE, d0, d1 + 1.1D, d2, 0.0D, 0.0D, 0.0D);

            if (pRandom.nextInt(10) == 0) {
                pLevel.playLocalSound(d0, d1, d2, (SoundEvent) ModSounds.BLOCK_STOVE_CRACKLE.get(), SoundSource.BLOCKS, 1.0F, 1.0F, false);
            }

            Direction direction = pState.getValue(HorizontalDirectionalBlock.FACING);
            Direction.Axis axis = direction.getAxis();
            double horizontalOffset = pRandom.nextDouble() * 0.6 - 0.3;
            double xOffset = axis == Direction.Axis.X ? (double) direction.getStepX() * 0.52 : horizontalOffset;
            double yOffset = pRandom.nextDouble() * 6.0 / 16.0;
            double zOffset = axis == Direction.Axis.Z ? (double) direction.getStepZ() * 0.52 : horizontalOffset;

            pLevel.addParticle(ParticleTypes.SMOKE, d0 + xOffset, d1 + yOffset, d2 + zOffset, 0.0D, 0.0D, 0.0D);
            pLevel.addParticle(ParticleTypes.SOUL_FIRE_FLAME, d0 + xOffset, d1 + yOffset, d2 + zOffset, 0.0D, 0.0D, 0.0D);
        }
    }

    @Override
    public void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean isMoving) {
        if (!state.is(newState.getBlock())) {
            BlockEntity be = level.getBlockEntity(pos);
            if (be instanceof ShadeStoveBlockEntity stoveEntity) {
                Containers.dropContents(level, pos, stoveEntity.getItems());
            }
            super.onRemove(state, level, pos, newState, isMoving);
        }
    }
}
