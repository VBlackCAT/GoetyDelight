package net.v_black_cat.goetydelight.compat.goetyrevelation.block;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.v_black_cat.goetydelight.compat.goetyrevelation.RevelationCompatRegistry;

public class ApollyonCakeBlockEntity extends BlockEntity {

    public ApollyonCakeBlockEntity(BlockPos pos, BlockState state) {
        super(RevelationCompatRegistry.APOLLYON_CAKE_BE.get(), pos, state);
    }
}