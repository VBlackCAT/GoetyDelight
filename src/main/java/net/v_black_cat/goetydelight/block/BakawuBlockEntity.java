package net.v_black_cat.goetydelight.block;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;


public class BakawuBlockEntity extends CustomDollBlockEntity {

    public BakawuBlockEntity(BlockPos pos, BlockState state) {
        this(ModBlockEntities.BAKAWU_BE.get(), pos, state);
    }

    public BakawuBlockEntity(BlockEntityType<?> entityType, BlockPos pos, BlockState state) {
        super(entityType, pos, state);
    }
}
