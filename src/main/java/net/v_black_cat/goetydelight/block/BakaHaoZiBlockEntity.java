package net.v_black_cat.goetydelight.block;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;


public class BakaHaoZiBlockEntity extends CustomDollBlockEntity {

    public BakaHaoZiBlockEntity(BlockPos pos, BlockState state) {
        this(ModBlockEntities.BAKAHAOZI_BE.get(), pos, state);
    }

    public BakaHaoZiBlockEntity(BlockEntityType<?> entityType, BlockPos pos, BlockState state) {
        super(entityType, pos, state);
    }
}
