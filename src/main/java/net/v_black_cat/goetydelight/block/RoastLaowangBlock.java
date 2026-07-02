package net.v_black_cat.goetydelight.block;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.shapes.BooleanOp;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import vectorwing.farmersdelight.common.block.FeastBlock;

import javax.annotation.Nullable;
import java.util.List;
import java.util.function.Supplier;

public class RoastLaowangBlock extends FeastBlock {

    public static final IntegerProperty SERVINGS = IntegerProperty.create("servings", 0, 13);
    public static final EnumProperty<Part> PART = EnumProperty.create("part", Part.class);

    // 预先计算所有朝向的碰撞箱 [servings][facing]
    private static final VoxelShape[][][] ROTATED_SHAPES = new VoxelShape[14][4][];
    private final List<Supplier<Item>> servingItems;

    // 定义7个不同的形状，对应不同的阶段 - 从JSON文件转换
    private static final VoxelShape[] SHAPES = new VoxelShape[] {
            makeShape0(),  // stage 0 (servings=13)
            makeShape3(),  // stage 3 (servings=10-12)
            makeShape4(),  // stage 4 (servings=9)
            makeShape5(),  // stage 5 (servings=8)
            makeShape6(),  // stage 6 (servings=7)
            makeShape7(),  // stage 7 (servings=6)
            makeShape13()  // stage 13 (servings=0)
    };

    public enum Part implements StringRepresentable {
        CENTER("center", 0, 0),
        NORTH_WEST("north_west", -1, -1),
        NORTH("north", -1, 0),
        NORTH_EAST("north_east", -1, 1),
        WEST("west", 0, -1),
        EAST("east", 0, 1),
        SOUTH_WEST("south_west", 1, -1),
        SOUTH("south", 1, 0),
        SOUTH_EAST("south_east", 1, 1);

        private final String name;
        public final int dx;
        public final int dz;

        Part(String name, int dx, int dz) {
            this.name = name;
            this.dx = dx;
            this.dz = dz;
        }

        @Override
        public String getSerializedName() {
            return name;
        }
    }

    public RoastLaowangBlock(Properties properties, List<Supplier<Item>> servingItems, boolean hasLeftovers) {
        super(properties, () -> servingItems.get(0).get(), hasLeftovers);
        this.servingItems = servingItems;
        this.registerDefaultState(this.stateDefinition.any()
                .setValue(FACING, Direction.NORTH)
                .setValue(SERVINGS, getMaxServings())
                .setValue(PART, Part.CENTER));
    }

    // 静态初始化块，预计算所有旋转
    static {
        Direction[] facings = {Direction.NORTH, Direction.EAST, Direction.SOUTH, Direction.WEST};

        for (int servings = 0; servings < 14; servings++) {
            for (int i = 0; i < facings.length; i++) {
                Direction facing = facings[i];
                VoxelShape originalShape = getShapeForServings(servings);
                ROTATED_SHAPES[servings][i] = new VoxelShape[]{rotateVoxelShapeStatic(originalShape, facing)};
            }
        }
    }

    private static VoxelShape getShapeForServings(int servings) {
        if (servings == 13) {  // stage 0
            return SHAPES[0];
        } else if (servings == 10) {  // stage 3
            return SHAPES[1];
        } else if (servings == 9) {  // stage 4
            return SHAPES[2];
        } else if (servings == 8) {  // stage 5
            return SHAPES[3];
        } else if (servings == 7) {  // stage 6
            return SHAPES[4];
        } else if (servings >= 1 && servings <= 6) {  // stage 7
            return SHAPES[5];
        } else if (servings == 0) {  // stage 13
            return SHAPES[6];
        }
        // 默认返回空形状
        return Shapes.empty();
    }

    private Part getPartFromOffset(int dx, int dz) {
        if (dx == -1 && dz == -1) return Part.NORTH_WEST;
        if (dx == -1 && dz == 0) return Part.NORTH;
        if (dx == -1 && dz == 1) return Part.NORTH_EAST;
        if (dx == 0 && dz == -1) return Part.WEST;
        if (dx == 0 && dz == 1) return Part.EAST;
        if (dx == 1 && dz == -1) return Part.SOUTH_WEST;
        if (dx == 1 && dz == 0) return Part.SOUTH;
        if (dx == 1 && dz == 1) return Part.SOUTH_EAST;
        return Part.CENTER;
    }

    @Override
    public ItemStack getServingItem(BlockState state) {
        int servings = state.getValue(SERVINGS);
        int takenCount = getMaxServings() - servings;
        int itemIndex = takenCount;
        // 确保索引在范围内
        if (itemIndex >= servingItems.size()) {
            itemIndex = servingItems.size() - 1;
        }
        return new ItemStack(servingItems.get(itemIndex).get());
    }

    @Override
    public void setPlacedBy(Level level, BlockPos pos, BlockState state, @Nullable LivingEntity placer, ItemStack stack) {
        super.setPlacedBy(level, pos, state, placer, stack);

        if (!level.isClientSide) {
            for (int dx = -1; dx <= 1; dx++) {
                for (int dz = -1; dz <= 1; dz++) {
                    if (dx == 0 && dz == 0) continue;

                    BlockPos partPos = pos.offset(dx, 0, dz);
                    Part part = getPartFromOffset(dx, dz);

                    BlockState partState = this.defaultBlockState()
                            .setValue(PART, part)
                            .setValue(FACING, state.getValue(FACING))
                            .setValue(SERVINGS, state.getValue(SERVINGS));

                    level.setBlock(partPos, partState, 3);
                }
            }
        }
    }

    private BlockPos getCenterPos(BlockPos pos, Part part) {
        return pos.offset(-part.dx, 0, -part.dz);
    }

    @Override
    public void playerDestroy(Level level, Player player, BlockPos pos, BlockState state, @Nullable BlockEntity blockEntity, ItemStack tool) {
        if (state.getValue(PART) == Part.CENTER) {
            super.playerDestroy(level, player, pos, state, blockEntity, tool);
        }
    }

    @Override
    public IntegerProperty getServingsProperty() {
        return SERVINGS;
    }

    @Override
    public int getMaxServings() {
        return 13;
    }

    @Nullable
    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        BlockPos pos = context.getClickedPos();
        Level level = context.getLevel();

        // 检查3x3区域是否都可放置
        for (int dx = -1; dx <= 1; dx++) {
            for (int dz = -1; dz <= 1; dz++) {
                if (dx == 0 && dz == 0) continue;

                BlockPos partPos = pos.offset(dx, 0, dz);
                if (!level.getBlockState(partPos).canBeReplaced(context)) {
                    return null;
                }
            }
        }

        return this.defaultBlockState().setValue(FACING, context.getHorizontalDirection());
    }

    @Override
    public BlockState updateShape(BlockState state, Direction facing, BlockState facingState, LevelAccessor level, BlockPos currentPos, BlockPos facingPos) {
        if (!level.isClientSide() && shouldBreakStructure(level, currentPos, state)) {
            level.scheduleTick(currentPos, this, 1);
        }
        return state;
    }

    @Override
    public VoxelShape getInteractionShape(BlockState state, BlockGetter level, BlockPos pos) {
        return state.getValue(PART) == Part.CENTER ? getShape(state, level, pos, CollisionContext.empty()) : Shapes.empty();
    }

    @Override
    public void playerWillDestroy(Level level, BlockPos pos, BlockState state, Player player) {
        if (!level.isClientSide) {
            if (player.isCreative()) {
                breakEntireStructure(level, pos, state, player);
            }
        }
        super.playerWillDestroy(level, pos, state, player);
    }

    @Override
    public void tick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        if (shouldBreakStructure(level, pos, state)) {
            breakEntireStructure(level, pos, state, null);
        }
    }

    private boolean shouldBreakStructure(LevelAccessor level, BlockPos pos, BlockState state) {
        Part part = state.getValue(PART);
        BlockPos centerPos = getCenterPos(pos, part);

        for (int dx = -1; dx <= 1; dx++) {
            for (int dz = -1; dz <= 1; dz++) {
                BlockPos checkPos = centerPos.offset(dx, 0, dz);
                BlockState checkState = level.getBlockState(checkPos);

                if (!(checkState.getBlock() instanceof RoastLaowangBlock)) {
                    return true;
                }

                Part expectedPart = getPartFromOffset(dx, dz);
                if (checkState.getValue(PART) != expectedPart) {
                    return true;
                }
            }
        }
        return false;
    }

    private void breakEntireStructure(Level level, BlockPos pos, BlockState state, @Nullable Player player) {
        Part part = state.getValue(PART);
        BlockPos centerPos = getCenterPos(pos, part);

        for (int dx = -1; dx <= 1; dx++) {
            for (int dz = -1; dz <= 1; dz++) {
                BlockPos partPos = centerPos.offset(dx, 0, dz);
                BlockState partState = level.getBlockState(partPos);

                if (partState.getBlock() instanceof RoastLaowangBlock) {
                    level.setBlock(partPos, Blocks.AIR.defaultBlockState(), 3);
                    if (player != null) {
                        level.levelEvent(player, 2001, partPos, Block.getId(partState));
                    }
                }
            }
        }
    }

    @Override
    public RenderShape getRenderShape(BlockState state) {
        return state.getValue(PART) == Part.CENTER ? RenderShape.MODEL : RenderShape.INVISIBLE;
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        if (state.getValue(PART) != Part.CENTER) {
            return Shapes.empty();
        }

        int servings = state.getValue(SERVINGS);
        if (servings < 0 || servings >= ROTATED_SHAPES.length) {
            servings = 0;
        }

        Direction facing = state.getValue(FACING);
        int facingIndex = getFacingIndex(facing);

        return ROTATED_SHAPES[servings][facingIndex][0];
    }

    @Override
    public VoxelShape getOcclusionShape(BlockState state, BlockGetter level, BlockPos pos) {
        return state.getValue(PART) == Part.CENTER ? super.getOcclusionShape(state, level, pos) : Shapes.empty();
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACING, SERVINGS, PART);
    }

    public int getFacingIndex(Direction facing) {
        switch (facing) {
            case NORTH: return 0;
            case EAST: return 1;
            case SOUTH: return 2;
            case WEST: return 3;
            default: return 0;
        }
    }

    private static VoxelShape rotateVoxelShapeStatic(VoxelShape shape, Direction facing) {
        if (facing == Direction.NORTH) {
            return shape;
        }

        return shape.toAabbs().stream()
                .map(aabb -> rotateAABBStatic(aabb, facing))
                .map(Shapes::create)
                .reduce(Shapes.empty(), Shapes::or);
    }

    private static AABB rotateAABBStatic(AABB aabb, Direction facing) {
        double minX = aabb.minX;
        double minY = aabb.minY;
        double minZ = aabb.minZ;
        double maxX = aabb.maxX;
        double maxY = aabb.maxY;
        double maxZ = aabb.maxZ;

        switch (facing) {
            case EAST:
                return new AABB(1 - maxZ, minY, minX, 1 - minZ, maxY, maxX);
            case SOUTH:
                return new AABB(1 - maxX, minY, 1 - maxZ, 1 - minX, maxY, 1 - minZ);
            case WEST:
                return new AABB(minZ, minY, 1 - maxX, maxZ, maxY, 1 - minX);
            default:
                return aabb;
        }
    }

    // ================ 从JSON文件转换的形状定义方法 ================

    // stage 0 - 完整形状
    static VoxelShape makeShape0() {
        VoxelShape shape = Shapes.empty();
        // 从 roast_laowang_block_stage0.json 转换
        // body (3,2,10) to (13,10,16)
        shape = Shapes.join(shape, Shapes.box(0.1875, 0.125, 0.625, 0.8125, 0.625, 1.0), BooleanOp.OR);
        // body (3,2,16) to (13,10,26)
        shape = Shapes.join(shape, Shapes.box(0.1875, 0.125, 1.0, 0.8125, 0.625, 1.625), BooleanOp.OR);
        // tail (8,6,26) to (8,10,30)
        shape = Shapes.join(shape, Shapes.box(0.5, 0.375, 1.625, 0.5, 0.625, 1.875), BooleanOp.OR);
        // head (4,5,4) to (12,12,12)
        shape = Shapes.join(shape, Shapes.box(0.25, 0.3125, 0.25, 0.75, 0.75, 0.75), BooleanOp.OR);
        // head horn right (10,4,4) to (12,5,8)
        shape = Shapes.join(shape, Shapes.box(0.625, 0.25, 0.25, 0.75, 0.3125, 0.5), BooleanOp.OR);
        // head horn left (4,4,4) to (6,5,8)
        shape = Shapes.join(shape, Shapes.box(0.25, 0.25, 0.25, 0.375, 0.3125, 0.5), BooleanOp.OR);
        // honghong (6,5,3) to (10,8,4)
        shape = Shapes.join(shape, Shapes.box(0.375, 0.3125, 0.1875, 0.625, 0.5, 0.25), BooleanOp.OR);
        // ear right (12,6,5) to (13,12,10)
        shape = Shapes.join(shape, Shapes.box(0.75, 0.375, 0.3125, 0.8125, 0.75, 0.625), BooleanOp.OR);
        // ear left (3,6,5) to (4,12,10)
        shape = Shapes.join(shape, Shapes.box(0.1875, 0.375, 0.3125, 0.25, 0.75, 0.625), BooleanOp.OR);
        // jaw (6,4,4.5) to (10,5,10.5)
        shape = Shapes.join(shape, Shapes.box(0.375, 0.25, 0.28125, 0.625, 0.3125, 0.65625), BooleanOp.OR);
        // leg back left (2,1,21) to (6,5,27)
        shape = Shapes.join(shape, Shapes.box(0.125, 0.0625, 1.3125, 0.375, 0.3125, 1.6875), BooleanOp.OR);
        // leg front left (2,1,8) to (6,5,14)
        shape = Shapes.join(shape, Shapes.box(0.125, 0.0625, 0.5, 0.375, 0.3125, 0.875), BooleanOp.OR);
        // leg front right (10,1,8) to (14,5,14)
        shape = Shapes.join(shape, Shapes.box(0.625, 0.0625, 0.5, 0.875, 0.3125, 0.875), BooleanOp.OR);
        // leg back right (10,1,21) to (14,5,27)
        shape = Shapes.join(shape, Shapes.box(0.625, 0.0625, 1.3125, 0.875, 0.3125, 1.6875), BooleanOp.OR);
        // nose (7,4,4) to (9,6,6)
        shape = Shapes.join(shape, Shapes.box(0.4375, 0.25, 0.25, 0.5625, 0.375, 0.375), BooleanOp.OR);
        // horn (7.5,2.1,1) to (7.5,5.1,4)
        shape = Shapes.join(shape, Shapes.box(0.46875, 0.13125, 0.0625, 0.46875, 0.31875, 0.25), BooleanOp.OR);
        // base plate 1 (6.5,1,6) to (9.5,4,9)
        shape = Shapes.join(shape, Shapes.box(0.40625, 0.0625, 0.375, 0.59375, 0.25, 0.5625), BooleanOp.OR);
        // horn 2 (7.3,0.4,3) to (7.3,3.4,6)
        shape = Shapes.join(shape, Shapes.box(0.45625, 0.025, 0.1875, 0.45625, 0.2125, 0.375), BooleanOp.OR);
        // right arm (11.2,1,7.4) to (14.2,5,15.4)
        shape = Shapes.join(shape, Shapes.box(0.7, 0.0625, 0.4625, 0.8875, 0.3125, 0.9625), BooleanOp.OR);
        // left arm (1.8,1,7.5) to (4.8,5,16.5)
        shape = Shapes.join(shape, Shapes.box(0.1125, 0.0625, 0.46875, 0.3, 0.3125, 1.03125), BooleanOp.OR);
        // left arm back (1.8,1,19.5) to (4.8,5,27.5)
        shape = Shapes.join(shape, Shapes.box(0.1125, 0.0625, 1.21875, 0.3, 0.3125, 1.71875), BooleanOp.OR);
        // right arm back (11.2,1,19.5) to (14.2,5,27.5)
        shape = Shapes.join(shape, Shapes.box(0.7, 0.0625, 1.21875, 0.8875, 0.3125, 1.71875), BooleanOp.OR);
        // plate 1 (0,0,2) to (16,1,16)
        shape = Shapes.join(shape, Shapes.box(0, 0, 0.125, 1, 0.0625, 1.0), BooleanOp.OR);
        // plate 2 (0,0,16) to (16,1,30)
        shape = Shapes.join(shape, Shapes.box(0, 0, 1.0, 1, 0.0625, 1.875), BooleanOp.OR);
        return shape;
    }

    // stage 3 - 从 roast_laowang_block_stage3.json 转换
    static VoxelShape makeShape3() {
        VoxelShape shape = Shapes.empty();
        // 主体部分
        shape = Shapes.join(shape, Shapes.box(0.1875, 0.125, 0.625, 0.8125, 0.625, 1.0), BooleanOp.OR);
        shape = Shapes.join(shape, Shapes.box(0.1875, 0.125, 1.0, 0.8125, 0.625, 1.625), BooleanOp.OR);
        // 尾巴
        shape = Shapes.join(shape, Shapes.box(0.5, 0.375, 1.625, 0.5, 0.625, 1.875), BooleanOp.OR);
        // 头部残留 (stage 3 只有骨架头部)
        shape = Shapes.join(shape, Shapes.box(0.375, 0.25, 0.5625, 0.625, 0.4375, 0.625), BooleanOp.OR);
        // 四条腿
        shape = Shapes.join(shape, Shapes.box(0.125, 0.0625, 1.3125, 0.375, 0.3125, 1.6875), BooleanOp.OR);
        shape = Shapes.join(shape, Shapes.box(0.125, 0.0625, 0.5, 0.375, 0.3125, 0.875), BooleanOp.OR);
        shape = Shapes.join(shape, Shapes.box(0.625, 0.0625, 0.5, 0.875, 0.3125, 0.875), BooleanOp.OR);
        shape = Shapes.join(shape, Shapes.box(0.625, 0.0625, 1.3125, 0.875, 0.3125, 1.6875), BooleanOp.OR);
        // 右臂
        shape = Shapes.join(shape, Shapes.box(0.625, 0.0625, 0.3125, 0.75, 0.1875, 0.4375), BooleanOp.OR);
        // 左臂前
        shape = Shapes.join(shape, Shapes.box(0.5, 0.125, 0.125, 0.6875, 0.125, 0.3125), BooleanOp.OR);
        // 左臂前
        shape = Shapes.join(shape, Shapes.box(0.40625, 0.0625, 0.375, 0.59375, 0.25, 0.5625), BooleanOp.OR);
        // 角
        shape = Shapes.join(shape, Shapes.box(0.45625, 0.025, 0.1875, 0.45625, 0.2125, 0.375), BooleanOp.OR);
        // 右臂前
        shape = Shapes.join(shape, Shapes.box(0.7, 0.0625, 0.4625, 0.8875, 0.3125, 0.9625), BooleanOp.OR);
        // 左臂后
        shape = Shapes.join(shape, Shapes.box(0.1125, 0.0625, 0.46875, 0.3, 0.3125, 1.03125), BooleanOp.OR);
        // 左臂后
        shape = Shapes.join(shape, Shapes.box(0.1125, 0.0625, 1.21875, 0.3, 0.3125, 1.71875), BooleanOp.OR);
        // 右臂后
        shape = Shapes.join(shape, Shapes.box(0.7, 0.0625, 1.21875, 0.8875, 0.3125, 1.71875), BooleanOp.OR);
        // 盘子
        shape = Shapes.join(shape, Shapes.box(0, 0, 0.125, 1, 0.0625, 1.0), BooleanOp.OR);
        shape = Shapes.join(shape, Shapes.box(0, 0, 1.0, 1, 0.0625, 1.875), BooleanOp.OR);
        return shape;
    }

    // stage 4 - 从 roast_laowang_block_stage4.json 转换
    static VoxelShape makeShape4() {
        VoxelShape shape = Shapes.empty();
        // 身体
        shape = Shapes.join(shape, Shapes.box(0.1875, 0.0625, 0.625, 0.8125, 0.5625, 1.0), BooleanOp.OR);
        shape = Shapes.join(shape, Shapes.box(0.1875, 0.0625, 1.0, 0.8125, 0.5625, 1.625), BooleanOp.OR);
        // 尾巴
        shape = Shapes.join(shape, Shapes.box(0.5, 0.3125, 1.625, 0.5, 0.5625, 1.875), BooleanOp.OR);
        // 头部
        shape = Shapes.join(shape, Shapes.box(0.375, 0.1875, 0.5625, 0.625, 0.375, 0.625), BooleanOp.OR);
        // 腿
        shape = Shapes.join(shape, Shapes.box(0.125, 0.0625, 1.3125, 0.375, 0.3125, 1.6875), BooleanOp.OR);
        shape = Shapes.join(shape, Shapes.box(0.125, 0.0625, 0.5, 0.375, 0.3125, 0.875), BooleanOp.OR);
        shape = Shapes.join(shape, Shapes.box(0.625, 0.0625, 1.3125, 0.875, 0.3125, 1.6875), BooleanOp.OR);
        // 右臂
        shape = Shapes.join(shape, Shapes.box(0.625, 0.0625, 0.3125, 0.75, 0.1875, 0.4375), BooleanOp.OR);
        // 左臂
        shape = Shapes.join(shape, Shapes.box(0.5, 0.125, 0.125, 0.6875, 0.125, 0.3125), BooleanOp.OR);
        shape = Shapes.join(shape, Shapes.box(0.40625, 0.0625, 0.375, 0.59375, 0.25, 0.5625), BooleanOp.OR);
        shape = Shapes.join(shape, Shapes.box(0.45625, 0.025, 0.1875, 0.45625, 0.2125, 0.375), BooleanOp.OR);
        // 左臂
        shape = Shapes.join(shape, Shapes.box(0.1125, 0.0625, 0.46875, 0.3, 0.3125, 1.03125), BooleanOp.OR);
        shape = Shapes.join(shape, Shapes.box(0.1125, 0.0625, 1.21875, 0.3, 0.3125, 1.71875), BooleanOp.OR);
        // 右臂
        shape = Shapes.join(shape, Shapes.box(0.7, 0.0625, 1.21875, 0.8875, 0.3125, 1.71875), BooleanOp.OR);
        // 盘子
        shape = Shapes.join(shape, Shapes.box(0, 0, 0.125, 1, 0.0625, 1.0), BooleanOp.OR);
        shape = Shapes.join(shape, Shapes.box(0, 0, 1.0, 1, 0.0625, 1.875), BooleanOp.OR);
        // 骨头 (从stage4开始出现)
        shape = Shapes.join(shape, Shapes.box(0.8125, 0.0625, 0.375, 0.9375, 0.1875, 0.75), BooleanOp.OR);
        return shape;
    }

    // stage 5 - 从 roast_laowang_block_stage5.json 转换
    static VoxelShape makeShape5() {
        VoxelShape shape = Shapes.empty();
        // 身体
        shape = Shapes.join(shape, Shapes.box(0.1875, 0.0625, 0.625, 0.8125, 0.5625, 1.0), BooleanOp.OR);
        shape = Shapes.join(shape, Shapes.box(0.1875, 0.0625, 1.0, 0.8125, 0.5625, 1.625), BooleanOp.OR);
        // 尾巴
        shape = Shapes.join(shape, Shapes.box(0.5, 0.3125, 1.625, 0.5, 0.5625, 1.875), BooleanOp.OR);
        // 头部
        shape = Shapes.join(shape, Shapes.box(0.375, 0.1875, 0.5625, 0.625, 0.375, 0.625), BooleanOp.OR);
        // 腿
        shape = Shapes.join(shape, Shapes.box(0.125, 0.0625, 1.3125, 0.375, 0.3125, 1.6875), BooleanOp.OR);
        shape = Shapes.join(shape, Shapes.box(0.625, 0.0625, 1.3125, 0.875, 0.3125, 1.6875), BooleanOp.OR);
        // 右臂
        shape = Shapes.join(shape, Shapes.box(0.625, 0.0625, 0.3125, 0.75, 0.1875, 0.4375), BooleanOp.OR);
        // 左臂
        shape = Shapes.join(shape, Shapes.box(0.5, 0.125, 0.125, 0.6875, 0.125, 0.3125), BooleanOp.OR);
        shape = Shapes.join(shape, Shapes.box(0.40625, 0.0625, 0.375, 0.59375, 0.25, 0.5625), BooleanOp.OR);
        shape = Shapes.join(shape, Shapes.box(0.45625, 0.025, 0.1875, 0.45625, 0.2125, 0.375), BooleanOp.OR);
        // 左臂
        shape = Shapes.join(shape, Shapes.box(0.1125, 0.0625, 1.21875, 0.3, 0.3125, 1.71875), BooleanOp.OR);
        // 右臂
        shape = Shapes.join(shape, Shapes.box(0.7, 0.0625, 1.21875, 0.8875, 0.3125, 1.71875), BooleanOp.OR);
        // 盘子
        shape = Shapes.join(shape, Shapes.box(0, 0, 0.125, 1, 0.0625, 1.0), BooleanOp.OR);
        shape = Shapes.join(shape, Shapes.box(0, 0, 1.0, 1, 0.0625, 1.875), BooleanOp.OR);
        // 骨头 (更多骨头)
        shape = Shapes.join(shape, Shapes.box(0.0625, 0.0625, 0.1875, 0.1875, 0.1875, 0.5625), BooleanOp.OR);
        shape = Shapes.join(shape, Shapes.box(0.0625, 0.125, 0.19375, 0.4375, 0.25, 0.31875), BooleanOp.OR);
        return shape;
    }

    // stage 6 - 从 roast_laowang_block_stage6.json 转换
    static VoxelShape makeShape6() {
        VoxelShape shape = Shapes.empty();
        // 身体
        shape = Shapes.join(shape, Shapes.box(0.1875, 0.0625, 0.625, 0.8125, 0.5625, 1.0), BooleanOp.OR);
        shape = Shapes.join(shape, Shapes.box(0.1875, 0.0625, 1.0, 0.8125, 0.5625, 1.625), BooleanOp.OR);
        // 尾巴
        shape = Shapes.join(shape, Shapes.box(0.5, 0.3125, 1.625, 0.5, 0.5625, 1.875), BooleanOp.OR);
        // 头部
        shape = Shapes.join(shape, Shapes.box(0.375, 0.1875, 0.5625, 0.625, 0.375, 0.625), BooleanOp.OR);
        // 腿
        shape = Shapes.join(shape, Shapes.box(0.125, 0.0625, 1.3125, 0.375, 0.3125, 1.6875), BooleanOp.OR);
        // 右臂
        shape = Shapes.join(shape, Shapes.box(0.625, 0.0625, 0.3125, 0.75, 0.1875, 0.4375), BooleanOp.OR);
        // 左臂
        shape = Shapes.join(shape, Shapes.box(0.5, 0.125, 0.125, 0.6875, 0.125, 0.3125), BooleanOp.OR);
        shape = Shapes.join(shape, Shapes.box(0.40625, 0.0625, 0.375, 0.59375, 0.25, 0.5625), BooleanOp.OR);
        shape = Shapes.join(shape, Shapes.box(0.45625, 0.025, 0.1875, 0.45625, 0.2125, 0.375), BooleanOp.OR);
        // 左臂
        shape = Shapes.join(shape, Shapes.box(0.1125, 0.0625, 1.21875, 0.3, 0.3125, 1.71875), BooleanOp.OR);
        // 盘子
        shape = Shapes.join(shape, Shapes.box(0, 0, 0.125, 1, 0.0625, 1.0), BooleanOp.OR);
        shape = Shapes.join(shape, Shapes.box(0, 0, 1.0, 1, 0.0625, 1.875), BooleanOp.OR);
        // 骨头 (更多)
        shape = Shapes.join(shape, Shapes.box(0.0625, 0.0625, 0.1875, 0.1875, 0.1875, 0.5625), BooleanOp.OR);
        shape = Shapes.join(shape, Shapes.box(0.0625, 0.125, 0.19375, 0.4375, 0.25, 0.31875), BooleanOp.OR);
        shape = Shapes.join(shape, Shapes.box(0.0625, 0.125, 0.38125, 0.4375, 0.25, 0.50625), BooleanOp.OR);
        return shape;
    }

    // stage 7 - 从 roast_laowang_block_stage7.json 转换
    static VoxelShape makeShape7() {
        VoxelShape shape = Shapes.empty();
        // 身体
        shape = Shapes.join(shape, Shapes.box(0.1875, 0.0625, 0.625, 0.8125, 0.5625, 1.0), BooleanOp.OR);
        shape = Shapes.join(shape, Shapes.box(0.1875, 0.0625, 1.0, 0.8125, 0.5625, 1.625), BooleanOp.OR);
        // 尾巴
        shape = Shapes.join(shape, Shapes.box(0.5, 0.3125, 1.625, 0.5, 0.5625, 1.875), BooleanOp.OR);
        // 头部
        shape = Shapes.join(shape, Shapes.box(0.375, 0.1875, 0.5625, 0.625, 0.375, 0.625), BooleanOp.OR);
        // 右臂
        shape = Shapes.join(shape, Shapes.box(0.75, 0.0625, 0.3125, 0.875, 0.1875, 0.4375), BooleanOp.OR);
        // 左臂
        shape = Shapes.join(shape, Shapes.box(0.625, 0.125, 0.125, 0.8125, 0.125, 0.3125), BooleanOp.OR);
        shape = Shapes.join(shape, Shapes.box(0.46875, 0.0625, 0.375, 0.65625, 0.25, 0.5625), BooleanOp.OR);
        shape = Shapes.join(shape, Shapes.box(0.51875, 0.025, 0.1875, 0.51875, 0.2125, 0.375), BooleanOp.OR);
        // 盘子
        shape = Shapes.join(shape, Shapes.box(0, 0, 0.125, 1, 0.0625, 1.0), BooleanOp.OR);
        shape = Shapes.join(shape, Shapes.box(0, 0, 1.0, 1, 0.0625, 1.875), BooleanOp.OR);
        // 骨头 (更多骨头)
        shape = Shapes.join(shape, Shapes.box(0.0625, 0.0625, 0.1625, 0.1875, 0.1875, 0.5375), BooleanOp.OR);
        shape = Shapes.join(shape, Shapes.box(0.25, 0.0625, 0.13125, 0.375, 0.1875, 0.50625), BooleanOp.OR);
        shape = Shapes.join(shape, Shapes.box(0.0375, 0.125, 0.19375, 0.4125, 0.25, 0.31875), BooleanOp.OR);
        shape = Shapes.join(shape, Shapes.box(0.0375, 0.125, 0.35, 0.4125, 0.25, 0.475), BooleanOp.OR);
        return shape;
    }

    // stage 13 - 从 roast_laowang_block_stage13.json 转换 (只剩骨架)
    static VoxelShape makeShape13() {
        VoxelShape shape = Shapes.empty();
        // 骨架身体部分
        shape = Shapes.join(shape, Shapes.box(0.4375, 0.4375, 0.6875, 0.5625, 0.5, 1.0), BooleanOp.OR);
        shape = Shapes.join(shape, Shapes.box(0.4375, 0.4375, 1.0, 0.5625, 0.5, 1.5625), BooleanOp.OR);
        // 身体骨架
        shape = Shapes.join(shape, Shapes.box(0.25, 0.0625, 0.6875, 0.75, 0.5, 1.0), BooleanOp.OR);
        shape = Shapes.join(shape, Shapes.box(0.25, 0.0625, 1.0, 0.75, 0.5, 1.5625), BooleanOp.OR);
        // 骨头装饰
        shape = Shapes.join(shape, Shapes.box(0.0375, 0.125, 0.19375, 0.4125, 0.25, 0.31875), BooleanOp.OR);
        shape = Shapes.join(shape, Shapes.box(0.0375, 0.125, 0.35, 0.4125, 0.25, 0.475), BooleanOp.OR);
        shape = Shapes.join(shape, Shapes.box(0.25, 0.0625, 0.13125, 0.375, 0.1875, 0.50625), BooleanOp.OR);
        shape = Shapes.join(shape, Shapes.box(0.0625, 0.0625, 0.1625, 0.1875, 0.1875, 0.5375), BooleanOp.OR);
        // 盘子
        shape = Shapes.join(shape, Shapes.box(0, 0, 0.125, 1, 0.0625, 1.0), BooleanOp.OR);
        shape = Shapes.join(shape, Shapes.box(0, 0, 1.0, 1, 0.0625, 1.875), BooleanOp.OR);
        return shape;
    }
}