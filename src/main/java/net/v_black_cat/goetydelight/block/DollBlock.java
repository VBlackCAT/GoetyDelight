package net.v_black_cat.goetydelight.block;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.NoteBlockInstrument;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.v_black_cat.goetydelight.init.ModSounds;
import org.jetbrains.annotations.Nullable;

public class DollBlock extends HorizontalDirectionalBlock implements SimpleWaterloggedBlock, EntityBlock {

    // ✅ 1.21 需要 CODEC
    public static final MapCodec<DollBlock> CODEC = simpleCodec(DollBlock::new);

    private static final BooleanProperty WATERLOGGED = BlockStateProperties.WATERLOGGED;
    private static final VoxelShape DOLL_SHAPE = Block.box(2.0d, 0.0d, 2.0d, 14.0d, 12.0d, 14.0d);

    private static final double PARTICLE_OFFSET_RANGE = 0.25;
    private static final double PARTICLE_HEIGHT_OFFSET = 1.0;
    private static final double PARTICLE_HEIGHT_VARIANCE = 0.2;
    private static final float NOTE_COLOR_DIVISOR = 24.0F;
    private static final int MAX_NOTE_COLORS = 4;

    private static final float BASE_VOLUME = 1.0f;
    private static final float PITCH_VARIANCE = 0.5f;
    private static final float BASE_PITCH = 0.75f;

    // ✅ 添加接受 Properties 的构造函数
    public DollBlock(Properties properties) {
        super(properties);
        this.registerDefaultState(this.stateDefinition.any().setValue(FACING, Direction.SOUTH)
                .setValue(WATERLOGGED, false));
    }

    public DollBlock() {
        this(Properties.of()
                .instrument(NoteBlockInstrument.BASEDRUM)
                .sound(SoundType.WOOL)
                .strength(0f, 10f)
                .noOcclusion());
    }

    // ✅ 1.21 需要实现 codec()
    @Override
    protected MapCodec<? extends HorizontalDirectionalBlock> codec() {
        return CODEC;
    }

    // ✅ 修复：只有 custom_doll 需要 BlockEntity
    @Override
    public @Nullable BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        // 检查这个方块是否是 CustomDollBlock
        if (this instanceof CustomDollBlock) {
            return new CustomDollBlockEntity(pos, state);
        }
        // 其他 doll 方块（doll_5152, doll_p0 等）不需要 BlockEntity
        return null;
    }

    @Override
    public BlockState updateShape(BlockState currentState, Direction direction, BlockState neighborState,
                                  LevelAccessor level, BlockPos currentPos, BlockPos neighborPos) {
        if (currentState.getValue(WATERLOGGED)) {
            level.scheduleTick(currentPos, Fluids.WATER, Fluids.WATER.getTickDelay(level));
        }
        return super.updateShape(currentState, direction, neighborState, level, currentPos, neighborPos);
    }

    @Override
    public FluidState getFluidState(BlockState blockState) {
        return blockState.getValue(WATERLOGGED) ? Fluids.WATER.getSource(false) : super.getFluidState(blockState);
    }

    // ✅ 1.21 use -> useItemOn，返回 ItemInteractionResult
    @Override
    protected ItemInteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos,
                                              Player player, InteractionHand hand, BlockHitResult hitResult) {
        if (level instanceof ServerLevel serverLevel) {
            spawnNoteParticles(serverLevel, pos);
            playDollSound(serverLevel, pos);
        }
        return ItemInteractionResult.SUCCESS;
    }

    private void spawnNoteParticles(ServerLevel serverLevel, BlockPos blockPos) {
        Vec3 particlePosition = calculateParticlePosition(serverLevel, blockPos);
        float noteColor = calculateNoteColor(serverLevel);
        serverLevel.sendParticles(ParticleTypes.NOTE,
                particlePosition.x(), particlePosition.y(), particlePosition.z(),
                0, noteColor, 0, 0, 1);
    }

    private Vec3 calculateParticlePosition(ServerLevel serverLevel, BlockPos blockPos) {
        return Vec3.atBottomCenterOf(blockPos).add(
                (serverLevel.getRandom().nextFloat() - 0.5) * PARTICLE_OFFSET_RANGE * 2,
                PARTICLE_HEIGHT_OFFSET + serverLevel.getRandom().nextFloat() * PARTICLE_HEIGHT_VARIANCE,
                (serverLevel.getRandom().nextFloat() - 0.5) * PARTICLE_OFFSET_RANGE * 2
        );
    }

    private float calculateNoteColor(ServerLevel serverLevel) {
        return serverLevel.getRandom().nextInt(MAX_NOTE_COLORS) / NOTE_COLOR_DIVISOR;
    }

    private void playDollSound(ServerLevel serverLevel, BlockPos blockPos) {
        float pitch = BASE_PITCH + serverLevel.random.nextFloat() * PITCH_VARIANCE;
        serverLevel.playSound(null, blockPos, ModSounds.TOUCH_DOLL.get(),
                SoundSource.BLOCKS, BASE_VOLUME, pitch);
    }

    @Override
    public @Nullable BlockState getStateForPlacement(BlockPlaceContext context) {
        FluidState fluidState = context.getLevel().getFluidState(context.getClickedPos());
        boolean isWaterlogged = fluidState.getType() == Fluids.WATER;
        return this.defaultBlockState()
                .setValue(FACING, context.getHorizontalDirection().getOpposite())
                .setValue(WATERLOGGED, isWaterlogged);
    }

    @Override
    public void playerDestroy(Level level, Player player, BlockPos pos, BlockState state, @Nullable BlockEntity blockEntity, ItemStack tool) {
        if (!level.isClientSide && !player.isCreative()) {
            popResource(level, pos, new ItemStack(this.asItem()));
        }
        super.playerDestroy(level, player, pos, state, blockEntity, tool);
    }

    @Override
    public VoxelShape getShape(BlockState blockState, BlockGetter level, BlockPos blockPos, CollisionContext context) {
        return DOLL_SHAPE;
    }

    @Override
    public RenderShape getRenderShape(BlockState state) {
        return RenderShape.ENTITYBLOCK_ANIMATED;
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACING, WATERLOGGED);
    }
}