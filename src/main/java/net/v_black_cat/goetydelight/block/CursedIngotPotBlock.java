package net.v_black_cat.goetydelight.block;

import net.minecraft.core.BlockPos;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.Containers;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.v_black_cat.goetydelight.init.ModBlockEntities;
import vectorwing.farmersdelight.common.block.CookingPotBlock;

import javax.annotation.Nullable;

public class CursedIngotPotBlock extends CookingPotBlock {

    public CursedIngotPotBlock(Properties properties) {
        super(properties);
    }

    @Override
    public ItemInteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hitResult) {
        if (level.isClientSide) {
            return ItemInteractionResult.SUCCESS;
        }

        BlockEntity tileEntity = level.getBlockEntity(pos);
        if (!(tileEntity instanceof CursedIngotPotBlockEntity cookingPotEntity)) {
            return ItemInteractionResult.SUCCESS;
        }
        
        if (stack.isEmpty() && player.isShiftKeyDown()) {
            return super.useItemOn(stack, state, level, pos, player, hand, hitResult);
        }

        // 尝试用容器取食
        ItemStack servingStack = cookingPotEntity.useHeldItemOnMeal(stack);
        if (!servingStack.isEmpty()) {
            if (!player.getInventory().add(servingStack)) {
                player.drop(servingStack, false);
            }
            level.playSound(null, pos, SoundEvents.ARMOR_EQUIP_GENERIC.value(), SoundSource.BLOCKS, 1.0F, 1.0F);
            return ItemInteractionResult.SUCCESS;
        }

        // 打开 GUI
        player.openMenu(cookingPotEntity, pos);
        return ItemInteractionResult.SUCCESS;
    }

    //爆物品
    @Override
    public void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean isMoving) {
        if (state.getBlock() != newState.getBlock()) {
            BlockEntity tileEntity = level.getBlockEntity(pos);
            if (tileEntity instanceof CursedIngotPotBlockEntity cookingPotEntity) {
                
                Containers.dropContents(level, pos, cookingPotEntity.getDroppableInventory());
                
                cookingPotEntity.getUsedRecipesAndPopExperience(level, Vec3.atCenterOf(pos));
                level.updateNeighbourForOutputSignal(pos, this);
            }
            super.onRemove(state, level, pos, newState, isMoving);
        }
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return ModBlockEntities.CURSED_INGOT_POT_BE.get().create(pos, state);
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> blockEntityType) {
        if (level.isClientSide) {
            return createTickerHelper(blockEntityType, ModBlockEntities.CURSED_INGOT_POT_BE.get(),
                    CursedIngotPotBlockEntity::animationTick);
        } else {
            return createTickerHelper(blockEntityType, ModBlockEntities.CURSED_INGOT_POT_BE.get(),
                    CursedIngotPotBlockEntity::cookingTick);
        }
    }

    @Override
    public void setPlacedBy(Level level, BlockPos pos, BlockState state, @Nullable LivingEntity placer, ItemStack stack) {
        super.setPlacedBy(level, pos, state, placer, stack);
    }
}