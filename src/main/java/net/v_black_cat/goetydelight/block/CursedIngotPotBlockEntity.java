package net.v_black_cat.goetydelight.block;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.inventory.SimpleContainerData;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import vectorwing.farmersdelight.common.block.entity.CookingPotBlockEntity;
import net.v_black_cat.goetydelight.screen.CursedIngotPotMenu;

public class CursedIngotPotBlockEntity extends CookingPotBlockEntity {
    private final ContainerData cookingData = new SimpleContainerData(4);

    public CursedIngotPotBlockEntity(BlockPos pos, BlockState state) {
        super(pos, state);
    }

    @Override
    public BlockEntityType<?> getType() {
        return ModBlockEntities.CURSED_INGOT_POT_BE.get();
    }

    @Override
    public AbstractContainerMenu createMenu(int containerId, Inventory playerInventory, Player player) {
        return new CursedIngotPotMenu(containerId, playerInventory, this, this.cookingData);
    }

}