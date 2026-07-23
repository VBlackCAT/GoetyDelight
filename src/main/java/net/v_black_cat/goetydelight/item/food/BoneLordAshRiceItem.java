package net.v_black_cat.goetydelight.item.food;

import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.v_black_cat.goetydelight.init.ModBuffTypes;
import net.v_black_cat.goetydelight.util.BuffUtil;
import org.jetbrains.annotations.NotNull;

public class BoneLordAshRiceItem extends Item {

    public BoneLordAshRiceItem(Properties properties) {
        super(properties);
    }

    @Override
    public @NotNull ItemStack finishUsingItem(@NotNull ItemStack stack, @NotNull Level level, @NotNull LivingEntity entity) {
        ItemStack resultStack = super.finishUsingItem(stack, level, entity);

        if (!level.isClientSide && entity instanceof Player player) {
            // 施加 Buff，持续 6000 ticks（5 分钟），amplifier = 0
            BuffUtil.applyBuff(player, ModBuffTypes.BONE_LORD_ASH_RICE.getId(), 6000, 0);
        }

        if (entity instanceof Player player) {
            if (player.getAbilities().instabuild) {
                return resultStack;
            }
            if (resultStack.isEmpty()) {
                return new ItemStack(Items.BOWL);
            } else if (!player.getInventory().add(new ItemStack(Items.BOWL))) {
                player.drop(new ItemStack(Items.BOWL), false);
            }
        }

        return resultStack;
    }
}