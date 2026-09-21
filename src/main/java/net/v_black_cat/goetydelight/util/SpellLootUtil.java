package net.v_black_cat.goetydelight.util;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraftforge.event.ForgeEventFactory;
import org.jetbrains.annotations.Nullable;

public final class SpellLootUtil {

    private SpellLootUtil() {
    }

    /** 发放一份掉落物；磁引时按 Goety 的拾取流程直接加入玩家背包。 */
    public static void giveOrDrop(ServerLevel level, BlockPos pos, @Nullable LivingEntity caster,
                                  ItemStack stack, boolean magnet) {
        if (stack.isEmpty()) {
            return;
        }

        if (magnet && caster instanceof Player player) {
            ItemEntity pickupProbe = new ItemEntity(level,
                    pos.getX() + 0.5D, pos.getY() + 0.5D, pos.getZ() + 0.5D, stack.copy());
            int pickupResult = ForgeEventFactory.onItemPickup(pickupProbe, player);
            if (pickupResult != 0 || player.addItem(stack)) {
                return;
            }
        }

        Block.popResource(level, pos, stack);
    }
}