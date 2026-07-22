package net.v_black_cat.goetydelight.util;

import com.Polarice3.Goety.common.blocks.entities.DarkAltarBlockEntity;
import com.Polarice3.Goety.common.blocks.entities.PedestalBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.neoforged.neoforge.items.ItemStackHandler;

import java.util.ArrayList;
import java.util.List;

public class RitualUtil {
    // 祭坛基座搜索半径（根据原 Goety 默认值）
    private static final int PEDESTAL_RANGE = 4;

    public static List<PedestalBlockEntity> getPedestals(Level world, BlockPos darkAltarPos) {
        List<PedestalBlockEntity> result = new ArrayList<>();
        Iterable<BlockPos> blocksToCheck = BlockPos.betweenClosed(
                darkAltarPos.offset(-PEDESTAL_RANGE, -PEDESTAL_RANGE, -PEDESTAL_RANGE),
                darkAltarPos.offset(PEDESTAL_RANGE, PEDESTAL_RANGE, PEDESTAL_RANGE)
        );
        for (BlockPos blockToCheck : blocksToCheck) {
            BlockEntity tileEntity = world.getBlockEntity(blockToCheck);
            if (tileEntity instanceof PedestalBlockEntity && !(tileEntity instanceof DarkAltarBlockEntity)) {
                result.add((PedestalBlockEntity) tileEntity);
            }
        }
        return result;
    }

    public static List<ItemStack> getItemsOnPedestals(Level world, BlockPos darkAltarPos) {
        List<ItemStack> result = new ArrayList<>();
        List<PedestalBlockEntity> pedestals = getPedestals(world, darkAltarPos);
        for (PedestalBlockEntity pedestalTile : pedestals) {
            // 直接访问公开字段 itemStackHandler（不是 Optional）
            ItemStackHandler handler = pedestalTile.itemStackHandler;
            if (handler != null) {
                ItemStack stack = handler.getStackInSlot(0);
                if (!stack.isEmpty()) {
                    result.add(stack);
                }
            }
        }
        return result;
    }
}
