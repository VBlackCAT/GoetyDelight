package net.v_black_cat.goetydelight.block;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.phys.shapes.BooleanOp;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.NotNull;
import vectorwing.farmersdelight.common.block.FeastBlock;

import java.util.function.Supplier;

import static net.v_black_cat.goetydelight.block.RoyalCakeBlock.rotateVoxelShape90Clockwise;

public class LichsChaosStewBlock extends FeastBlock {
    public static final IntegerProperty SERVINGS = IntegerProperty.create("servings", 0, 5);

    private static final VoxelShape BASE_SHAPE = Shapes.join(
            Shapes.join(
                    Shapes.box(0.125, 0, 0.125, 0.875, 0.125, 0.875),
                    Shapes.box(0.125, 0.75, 0.125, 0.875, 0.875, 0.875),
                    BooleanOp.OR),
            Shapes.box(0, 0.125, 0, 1, 0.75, 1),
            BooleanOp.OR);

    /** 按 FACING 预计算，避免每次 getShape 都重新 union/旋转。 */
    private static final VoxelShape[] SHAPES = new VoxelShape[4];

    static {
        for (int i = 0; i < SHAPES.length; i++) {
            VoxelShape shape = BASE_SHAPE;
            for (int j = 0; j < i; j++) {
                shape = rotateVoxelShape90Clockwise(shape);
            }
            SHAPES[i] = shape;
        }
    }

    public LichsChaosStewBlock(Properties properties, Supplier<Item> servingItem, boolean hasLeftovers) {
        super(properties, servingItem, hasLeftovers);
        this.registerDefaultState(this.stateDefinition.any().setValue(FACING, Direction.NORTH).setValue(SERVINGS, 5));
    }

    @Override
    public int getMaxServings() {
        return 5;
    }

    @Override
    public @NotNull IntegerProperty getServingsProperty() {
        return SERVINGS;
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<net.minecraft.world.level.block.Block, BlockState> builder) {
        builder.add(FACING, SERVINGS);
    }

    public VoxelShape makeShape() {
        return BASE_SHAPE;
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        // 统一使用同一个碰撞箱，不随 servings 变化。
        return SHAPES[Math.floorMod(state.getValue(FACING).get2DDataValue(), SHAPES.length)];
    }
}