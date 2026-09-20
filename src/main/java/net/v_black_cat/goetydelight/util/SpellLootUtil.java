package net.v_black_cat.goetydelight.util;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import org.jetbrains.annotations.Nullable;

public final class SpellLootUtil {

    private SpellLootUtil() {
    }

    /** 发放一份掉落物 */
    public static void giveOrDrop(ServerLevel level, BlockPos pos, @Nullable LivingEntity caster,
                                  ItemStack stack, boolean magnet) {
        if (stack.isEmpty()) {
            return;
        }
        if (magnet && caster instanceof Player player) {
            ItemEntity itemEntity = new ItemEntity(level,
                    pos.getX() + 0.5D, pos.getY() + 0.5D, pos.getZ() + 0.5D, stack.copy());
            itemEntity.setNoPickUpDelay();
            itemEntity.playerTouch(player);
            if (!itemEntity.getItem().isEmpty()) {
                Block.popResource(level, pos, itemEntity.getItem()); // 背包满则落地
            }
            return;
        }
        Block.popResource(level, pos, stack);
    }
}
