package net.v_black_cat.goetydelight.block;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.shapes.BooleanOp;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import vectorwing.farmersdelight.common.block.FeastBlock;

import java.util.function.Supplier;

import static net.v_black_cat.goetydelight.block.RoyalCakeBlock.rotateVoxelShape90Clockwise;

public class BoneLordAshRiceBlock extends FeastBlock {
    public BoneLordAshRiceBlock(Properties properties, Supplier<Item> servingItem, boolean hasLeftovers) {
        super(properties, servingItem, hasLeftovers);
    }
    public int getMaxServings() {
        return 3;
    }


    public VoxelShape makeShape(){
        VoxelShape shape = Shapes.empty();
        shape = Shapes.join(shape, Shapes.box(0.125, 0, 0.125, 0.25, 0.625, 0.875), BooleanOp.OR);
        shape = Shapes.join(shape, Shapes.box(0.125, 0, 0.125, 0.875, 0.625, 0.25), BooleanOp.OR);
        shape = Shapes.join(shape, Shapes.box(0.1875, 0, 0.75, 0.875, 0.625, 0.875), BooleanOp.OR);
        shape = Shapes.join(shape, Shapes.box(0.75, 0, 0.1875, 0.875, 0.625, 0.875), BooleanOp.OR);
        shape = Shapes.join(shape, Shapes.box(0.125, 0, 0.125, 0.875, 0.0625, 0.875), BooleanOp.OR);
        shape = Shapes.join(shape, Shapes.box(0, 0.4375, 0.3125, 0.125, 0.5625, 0.6875), BooleanOp.OR);
        shape = Shapes.join(shape, Shapes.box(0.875, 0.4375, 0.3125, 1, 0.5625, 0.6875), BooleanOp.OR);

        return shape;
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        VoxelShape baseShape = makeShape();
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
}

