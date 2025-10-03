package net.v_black_cat.goetydelight.block;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import vectorwing.farmersdelight.common.block.entity.CookingPotBlockEntity;
import vectorwing.farmersdelight.common.block.entity.container.CookingPotMenu;

public class CursedIngotPotBlockEntity extends CookingPotBlockEntity {
    public CursedIngotPotBlockEntity(BlockPos pos, BlockState state) {
        super(pos, state);
    }


    @Override
    public BlockEntityType<?> getType() {
        return ModBlockEntities.CURSED_INGOT_POT_BE.get(); // 下一步注册
    }





}