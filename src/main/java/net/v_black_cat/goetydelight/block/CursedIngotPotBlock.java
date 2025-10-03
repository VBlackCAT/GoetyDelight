package net.v_black_cat.goetydelight.block;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraftforge.network.NetworkHooks;
import vectorwing.farmersdelight.common.block.CookingPotBlock;
import vectorwing.farmersdelight.common.block.entity.CookingPotBlockEntity;
import vectorwing.farmersdelight.common.block.state.CookingPotSupport;
import vectorwing.farmersdelight.common.tag.ModTags;

import javax.annotation.Nullable;

public class CursedIngotPotBlock extends CookingPotBlock {
    public CursedIngotPotBlock(Properties properties) {
        super(properties);
    }

    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return ModBlockEntities.CURSED_INGOT_POT_BE.get().create(pos, state);
    }
    @Override
    public InteractionResult use(BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult result) {
        ItemStack heldStack = player.getItemInHand(hand);
        if (heldStack.isEmpty() && player.isShiftKeyDown()) {
            level.setBlockAndUpdate(pos, (BlockState)state.setValue(SUPPORT, ((CookingPotSupport)state.getValue(SUPPORT)).equals(CookingPotSupport.HANDLE) ? this.getTrayState(level, pos) : CookingPotSupport.HANDLE));
            level.playSound((Player)null, pos, SoundEvents.LANTERN_PLACE, SoundSource.BLOCKS, 0.7F, 1.0F);
        } else if (!level.isClientSide) {
            BlockEntity tileEntity = level.getBlockEntity(pos);
            if (tileEntity instanceof CookingPotBlockEntity) {
                CursedIngotPotBlockEntity cookingPotEntity = (CursedIngotPotBlockEntity)tileEntity;
                ItemStack servingStack = cookingPotEntity.useHeldItemOnMeal(heldStack);
                if (servingStack != ItemStack.EMPTY) {
                    if (!player.getInventory().add(servingStack)) {
                        player.drop(servingStack, false);
                    }

                    level.playSound((Player)null, pos, SoundEvents.ARMOR_EQUIP_GENERIC, SoundSource.BLOCKS, 1.0F, 1.0F);
                } else {
                    NetworkHooks.openScreen((ServerPlayer)player, cookingPotEntity, pos);
                }
            }

            return InteractionResult.SUCCESS;
        }

        return InteractionResult.SUCCESS;
    }


    private CookingPotSupport getTrayState(LevelAccessor level, BlockPos pos) {
        return level.getBlockState(pos.below()).is(ModTags.TRAY_HEAT_SOURCES) ? CookingPotSupport.TRAY : CookingPotSupport.NONE;
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        return level.isClientSide ?
                createTickerHelper(type, ModBlockEntities.CURSED_INGOT_POT_BE.get(), CookingPotBlockEntity::animationTick) :
                createTickerHelper(type, ModBlockEntities.CURSED_INGOT_POT_BE.get(), CookingPotBlockEntity::cookingTick);
    }
}