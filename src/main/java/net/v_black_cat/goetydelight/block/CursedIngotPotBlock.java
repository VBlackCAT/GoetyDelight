package net.v_black_cat.goetydelight.block;

import net.minecraft.core.BlockPos;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.Containers;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
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
        
        // 【修复1】：空手潜行直接走父类逻辑取出食物，并立刻返回，防止后续触发打开GUI菜单
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

    // 破坏方块时爆出物品与经验（与农夫乐事原版一致：松散物品走 dropContents，
    // 锅方块物品走原版战利品表掉落，暂存餐数据组件随之保留在掉落物品上）
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

    // 创意模式拾取时保留暂存数据（农夫乐事原版同款）
    @Override
    public ItemStack getCloneItemStack(LevelReader level, BlockPos pos, BlockState state) {
        ItemStack stack = super.getCloneItemStack(level, pos, state);
        if (level.getBlockEntity(pos) instanceof CursedIngotPotBlockEntity cookingPot) {
            stack = cookingPot.getAsItem();
        }
        return stack;
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

    // 【修复2】：删除了完全继承自父类且无额外操作的 setPlacedBy 方法，保持代码整洁高效
}