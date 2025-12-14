package net.v_black_cat.goetydelight.util;

import com.Polarice3.Goety.common.blocks.entities.DarkAltarBlockEntity;
import com.Polarice3.Goety.common.blocks.entities.PedestalBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import static com.Polarice3.Goety.common.ritual.Ritual.PEDESTAL_RANGE;

public class RitualUtil {
    public static List<PedestalBlockEntity> getPedestals(Level world, BlockPos darkAltarPos) {
        List<PedestalBlockEntity> result = new ArrayList();
        Iterable<BlockPos> blocksToCheck = BlockPos.betweenClosed(darkAltarPos.offset(-PEDESTAL_RANGE, -PEDESTAL_RANGE, -PEDESTAL_RANGE), darkAltarPos.offset(PEDESTAL_RANGE, PEDESTAL_RANGE, PEDESTAL_RANGE));
        Iterator var5 = blocksToCheck.iterator();

        while(var5.hasNext()) {
            BlockPos blockToCheck = (BlockPos)var5.next();
            BlockEntity tileEntity = world.getBlockEntity(blockToCheck);
            if (tileEntity instanceof PedestalBlockEntity && !(tileEntity instanceof DarkAltarBlockEntity)) {
                result.add((PedestalBlockEntity)tileEntity);
            }
        }

        return result;
    }

    public static List<ItemStack> getItemsOnPedestals(Level world, BlockPos darkAltarPos) {
        List<ItemStack> result = new ArrayList();
        List<PedestalBlockEntity> pedestals = getPedestals(world, darkAltarPos);
        Iterator var5 = pedestals.iterator();

        while(var5.hasNext()) {
            PedestalBlockEntity pedestalTile = (PedestalBlockEntity)var5.next();
            pedestalTile.itemStackHandler.ifPresent((handler) -> {
                ItemStack stack = handler.getStackInSlot(0);
                if (!stack.isEmpty()) {
                    result.add(stack);
                }
            });
        }
        return result;
    }
}
