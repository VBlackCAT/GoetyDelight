package net.v_black_cat.goetydelight.block;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.stats.Stats;
import net.minecraft.util.RandomSource;
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

import javax.annotation.Nullable;

public class NightStoveBlock extends AbstractFurnaceBlock {

    public NightStoveBlock(BlockBehaviour.Properties properties) {
        super(properties);
        this.registerDefaultState(this.stateDefinition.any().setValue(LIT, true));
    }

    @Override
    public MapCodec<? extends AbstractFurnaceBlock> codec() {
        return simpleCodec(NightStoveBlock::new);
    }

    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new NightStoveBlockEntity(pos, state);
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        if (level.isClientSide) return null;
        return createTickerHelper(type, ModBlockEntities.NIGHT_STOVE_BE.get(), NightStoveBlockEntity::serverTick);
    }

    // ====== 热源伤害（参考农夫乐事 AbstractStoveBlock） ======
    @Override
    public void entityInside(BlockState state, Level level, BlockPos pos, Entity entity) {
        if (level.isClientSide) return;
        if (!state.getValue(LIT)) return;          // 未点亮不伤害

        if (entity instanceof LivingEntity living) {
            // 只有非无敌、非抗火的生物才受伤
            if (!living.isInvulnerableTo(level.damageSources().hotFloor())) {
                // 每 20 tick 触发一次，避免每 tick 伤害（与以太炉灶效果一致）
                if (living.tickCount % 20 == 0) {
                    living.hurt(level.damageSources().hotFloor(), 1.0F);
                    // 可选：同时点燃生物（模仿岩浆块），如果需要则取消注释
                    // living.setSecondsOnFire(1);
                }
            }
        }
    }

    // ====== 交互（空手/手持物品均打开容器） ======
    @Override
    public InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hitResult) {
        if (!level.isClientSide) {
            BlockEntity be = level.getBlockEntity(pos);
            if (be instanceof NightStoveBlockEntity) {
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
            if (be instanceof NightStoveBlockEntity) {
                player.openMenu((MenuProvider) be);
                player.awardStat(Stats.INTERACT_WITH_SMOKER);
                return ItemInteractionResult.CONSUME;
            }
        }
        return ItemInteractionResult.SUCCESS;
    }

    @Override
    protected void openContainer(Level level, BlockPos pos, Player player) {
        BlockEntity be = level.getBlockEntity(pos);
        if (be instanceof NightStoveBlockEntity) {
            player.openMenu((MenuProvider) be);
            player.awardStat(Stats.INTERACT_WITH_SMOKER);
        }
    }

    // ====== 客户端粒子（烟雾+灵魂火） ======
    @Override
    public void animateTick(BlockState state, Level level, BlockPos pos, RandomSource random) {
        if (state.getValue(LIT)) {
            double x = pos.getX() + 0.5;
            double y = pos.getY();
            double z = pos.getZ() + 0.5;

            if (random.nextDouble() < 0.1D) {
                level.playLocalSound(x, y, z, SoundEvents.SMOKER_SMOKE, SoundSource.BLOCKS, 1.0F, 1.0F, false);
            }
            level.addParticle(ParticleTypes.SMOKE, x, y + 1.1, z, 0.0, 0.0, 0.0);

            Direction direction = state.getValue(HorizontalDirectionalBlock.FACING);
            Direction.Axis axis = direction.getAxis();
            double hOffset = random.nextDouble() * 0.6 - 0.3;
            double xOff = (axis == Direction.Axis.X) ? direction.getStepX() * 0.52 : hOffset;
            double yOff = random.nextDouble() * 6.0 / 16.0;
            double zOff = (axis == Direction.Axis.Z) ? direction.getStepZ() * 0.52 : hOffset;

            level.addParticle(ParticleTypes.SMOKE, x + xOff, y + yOff, z + zOff, 0.0, 0.0, 0.0);
            level.addParticle(ParticleTypes.SOUL_FIRE_FLAME, x + xOff, y + yOff, z + zOff, 0.0, 0.0, 0.0);
        }
    }
}