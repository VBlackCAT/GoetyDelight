package net.v_black_cat.goetydelight.block;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.level.pathfinder.PathComputationType;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.BooleanOp;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.v_black_cat.goetydelight.item.ModItems;

public class RoyalCakeBlock extends Block {
    public static final DirectionProperty FACING = net.minecraft.world.level.block.state.properties.BlockStateProperties.HORIZONTAL_FACING;
    public static final IntegerProperty SERVINGS = IntegerProperty.create("servings", 0, 8);
    protected static final VoxelShape[] BASE_SHAPES = new VoxelShape[]{
            rotateVoxelShape90Clockwise(makeShape0()),
            rotateVoxelShape90Clockwise(makeShape1()),
            rotateVoxelShape90Clockwise(makeShape2()),
            rotateVoxelShape90Clockwise(makeShape3()),
            rotateVoxelShape90Clockwise(makeShape4()),
            rotateVoxelShape90Clockwise(makeShape5()),
            rotateVoxelShape90Clockwise(makeShape6()),
            rotateVoxelShape90Clockwise(makeShape7()),
            rotateVoxelShape90Clockwise(makeShape8())
    };
    
    private static VoxelShape rotateVoxelShape90Clockwise(VoxelShape originalShape) {
        
        return originalShape.toAabbs().stream()
                .map(aabb -> rotateAABB90Clockwise(aabb))
                .reduce(Shapes.empty(), Shapes::or);
    }

    
    private static VoxelShape rotateAABB90Clockwise(AABB aabb) {


        double newMinX = 1 - aabb.maxZ;
        double newMinZ = aabb.minX;
        double newMaxX = 1 - aabb.minZ;
        double newMaxZ = aabb.maxX;

        
        double newMinY = aabb.minY;
        double newMaxY = aabb.maxY;

        return Shapes.create(new AABB(newMinX, newMinY, newMinZ, newMaxX, newMaxY, newMaxZ));
    }
    static VoxelShape makeShape8(){
        VoxelShape shape = Shapes.empty();
        shape = Shapes.join(shape, Shapes.box(0.0625, 0.0625, 0.0625, 0.9375, 0.5, 0.9375), BooleanOp.OR);
        shape = Shapes.join(shape, Shapes.box(0.1875, 0.5, 0.1875, 0.8125, 0.875, 0.8125), BooleanOp.OR);
        shape = Shapes.join(shape, Shapes.box(0, 0, 0, 1, 0.0625, 1), BooleanOp.OR);

        return shape;
    }
    static VoxelShape makeShape7(){
        VoxelShape shape = Shapes.empty();
        shape = Shapes.join(shape, Shapes.box(0.0625, 0.0625, 0.0625, 0.9375, 0.5, 0.9375), BooleanOp.OR);
        shape = Shapes.join(shape, Shapes.box(0, 0, 0, 1, 0.0625, 1), BooleanOp.OR);
               shape = Shapes.join(shape, Shapes.box(0.5, 0.5, 0.1875, 0.8125, 0.875, 0.5), BooleanOp.OR);
        shape = Shapes.join(shape, Shapes.box(0.1875, 0.4375, 0.1875, 0.5, 0.875, 0.5), BooleanOp.OR);
        shape = Shapes.join(shape, Shapes.box(0.1875, 0.5, 0.5, 0.5, 0.875, 0.8125), BooleanOp.OR);

        return shape;
    }
    static VoxelShape makeShape6(){
        VoxelShape shape = Shapes.empty();
        shape = Shapes.join(shape, Shapes.box(0.0625, 0.0625, 0.0625, 0.9375, 0.5, 0.9375), BooleanOp.OR);
        shape = Shapes.join(shape, Shapes.box(0, 0, 0, 1, 0.0625, 1), BooleanOp.OR);
               shape = Shapes.join(shape, Shapes.box(0.1875, 0.4375, 0.1875, 0.5, 0.875, 0.5), BooleanOp.OR);
        shape = Shapes.join(shape, Shapes.box(0.1875, 0.5, 0.5, 0.5, 0.875, 0.8125), BooleanOp.OR);

        return shape;
    }
    static VoxelShape makeShape5(){
        VoxelShape shape = Shapes.empty();
        shape = Shapes.join(shape, Shapes.box(0.0625, 0.0625, 0.0625, 0.9375, 0.5, 0.9375), BooleanOp.OR);
        shape = Shapes.join(shape, Shapes.box(0, 0, 0, 1, 0.0625, 1), BooleanOp.OR);
               shape = Shapes.join(shape, Shapes.box(0.1875, 0.4375, 0.1875, 0.5, 0.875, 0.5), BooleanOp.OR);

        return shape;
    }
    static VoxelShape makeShape4(){
        VoxelShape shape = Shapes.empty();
        shape = Shapes.join(shape, Shapes.box(0.0625, 0.0625, 0.0625, 0.9375, 0.5, 0.9375), BooleanOp.OR);
        shape = Shapes.join(shape, Shapes.box(0, 0, 0, 1, 0.0625, 1), BooleanOp.OR);

        return shape;
    }
    static VoxelShape makeShape3(){
        VoxelShape shape = Shapes.empty();
        shape = Shapes.join(shape, Shapes.box(0, 0, 0, 1, 0.0625, 1), BooleanOp.OR);
        shape = Shapes.join(shape, Shapes.box(0.5, 0.0625, 0.0625, 0.9375, 0.5, 0.5), BooleanOp.OR);
        shape = Shapes.join(shape, Shapes.box(0.0625, 0.0625, 0.0625, 0.5, 0.5, 0.5), BooleanOp.OR);
        shape = Shapes.join(shape, Shapes.box(0.0625, 0.0625, 0.5, 0.5, 0.5, 0.9375), BooleanOp.OR);

        return shape;
    }
    static VoxelShape makeShape2(){
        VoxelShape shape = Shapes.empty();
        shape = Shapes.join(shape, Shapes.box(0, 0, 0, 1, 0.0625, 1), BooleanOp.OR);
        shape = Shapes.join(shape, Shapes.box(0.5, 0.0625, 0.0625, 0.9375, 0.5, 0.5), BooleanOp.OR);
        shape = Shapes.join(shape, Shapes.box(0.0625, 0.0625, 0.0625, 0.5, 0.5, 0.5), BooleanOp.OR);

        return shape;
    }
    static VoxelShape makeShape1(){
        VoxelShape shape = Shapes.empty();
        shape = Shapes.join(shape, Shapes.box(0, 0, 0, 1, 0.0625, 1), BooleanOp.OR);
        shape = Shapes.join(shape, Shapes.box(0.5, 0.0625, 0.0625, 0.9375, 0.5, 0.5), BooleanOp.OR);

        return shape;
    }
    static VoxelShape makeShape0(){
        VoxelShape shape = Shapes.empty();
        shape = Shapes.join(shape, Shapes.box(0, 0, 0, 1, 0.0625, 1), BooleanOp.OR);

        return shape;
    }
    public RoyalCakeBlock(BlockBehaviour.Properties properties) {
        super(properties);
        this.registerDefaultState(this.stateDefinition.any()
                .setValue(FACING, Direction.NORTH)
                .setValue(SERVINGS, 8));
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        VoxelShape baseShape = BASE_SHAPES[state.getValue(SERVINGS)];
        Direction facing = state.getValue(FACING);

        
        int rotations = 0;
        switch (facing) {
            case EAST: rotations = 1; break;  
            case SOUTH: rotations = 2; break; 
            case WEST: rotations = 3; break; 
            case NORTH: 
            default: rotations = 0;
        }

        
        VoxelShape rotatedShape = baseShape;
        for (int i = 0; i < rotations; i++) {
            rotatedShape = rotateVoxelShape90Clockwise(rotatedShape);
        }

        return rotatedShape;
    }
    @Override
    public InteractionResult use(BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit) {
        ItemStack heldStack = player.getItemInHand(hand);

        
        if (!isKnife(heldStack)) {
            return InteractionResult.PASS;
        }

        if (level.isClientSide) {
            return InteractionResult.SUCCESS;
        }

        return takeServing(level, pos, state, player, hand);
    }

    private boolean isKnife(ItemStack stack) {
        return stack.is(net.minecraft.tags.TagKey.create(net.minecraft.core.registries.Registries.ITEM,
                new net.minecraft.resources.ResourceLocation("farmersdelight", "tools/knives")));
    }

    protected InteractionResult takeServing(Level level, BlockPos pos, BlockState state, Player player, InteractionHand hand) {
        int servings = state.getValue(SERVINGS);

        if (servings == 0) {
            
            level.removeBlock(pos, false);
            level.playSound(null, pos, SoundEvents.WOOD_BREAK, SoundSource.BLOCKS, 0.8F, 0.8F);
            return InteractionResult.SUCCESS;
        }

        
        ItemStack cakeSlice = new ItemStack(ModItems.CAKE.get()); 
        if (!player.getInventory().add(cakeSlice)) {
            player.drop(cakeSlice, false);
        }

        
        int newServings = servings - 1;
        BlockState newState = state.setValue(SERVINGS, newServings);

        if (newServings < 0) {
            
            level.removeBlock(pos, false);
        } else {
            
            level.setBlock(pos, newState, 3);
        }

        
        level.playSound(null, pos, SoundEvents.WOOL_PLACE, SoundSource.BLOCKS, 1.0F, 1.0F);

        
        ItemStack tool = player.getItemInHand(hand);
        if (!player.getAbilities().instabuild) {
            tool.hurtAndBreak(1, player, (user) -> {
                user.broadcastBreakEvent(hand);
            });
        }

        return InteractionResult.SUCCESS;
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        return this.defaultBlockState().setValue(FACING, context.getHorizontalDirection().getOpposite());
    }

    @Override
    public BlockState updateShape(BlockState state, Direction direction, BlockState neighborState, LevelAccessor level, BlockPos currentPos, BlockPos neighborPos) {
        return direction == Direction.DOWN && !state.canSurvive(level, currentPos)
                ? Blocks.AIR.defaultBlockState()
                : super.updateShape(state, direction, neighborState, level, currentPos, neighborPos);
    }

    @Override
    public boolean canSurvive(BlockState state, LevelReader level, BlockPos pos) {
        return level.getBlockState(pos.below()).isSolid();
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACING, SERVINGS);
    }

    @Override
    public int getAnalogOutputSignal(BlockState state, Level level, BlockPos pos) {
        return state.getValue(SERVINGS);
    }

    @Override
    public boolean hasAnalogOutputSignal(BlockState state) {
        return true;
    }

    @Override
    public boolean isPathfindable(BlockState state, BlockGetter level, BlockPos pos, PathComputationType type) {
        return false;
    }
}