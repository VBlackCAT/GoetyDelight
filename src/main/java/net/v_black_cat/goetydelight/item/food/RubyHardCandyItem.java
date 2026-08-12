package net.v_black_cat.goetydelight.item.food;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.v_black_cat.goetydelight.init.ModBuffTypes;
import net.v_black_cat.goetydelight.util.BuffUtil;
import net.v_black_cat.goetydelight.util.SpellPotencyUtil;

public class RubyHardCandyItem extends Item {
    private static final int DAMAGE_REDUCTION_DURATION = 20 * 60 * 10;
    private static final int MAX_POTENCY_LEVEL = 3;

    public RubyHardCandyItem(Properties pProperties) {
        super(pProperties);
    }

    @Override
    public ItemStack finishUsingItem(ItemStack stack, Level level, LivingEntity entity) {
        stack = super.finishUsingItem(stack, level, entity);
        if (!level.isClientSide && entity instanceof Player player) {
            int currentLevel = SpellPotencyUtil.getCandyPotencyLevel(player);
            if (currentLevel < MAX_POTENCY_LEVEL) {
                // 增加硬糖等级
                SpellPotencyUtil.setCandyPotencyLevel(player, currentLevel + 1);

                // 重新计算并应用总加成
                SpellPotencyUtil.recalculateAndApply(player);
            }

            // 应用免伤buff
            BuffUtil.applyBuff(
                    entity,
                    ModBuffTypes.RUBY_HARD_CANDY_DAMAGE_REDUCTION.getId(),
                    DAMAGE_REDUCTION_DURATION,
                    0
            );
        }
        return stack;
    }

    @SubscribeEvent
    public static void onPlayerLoggedIn(PlayerEvent.PlayerLoggedInEvent event) {
        Player player = event.getEntity();
        if (player.level().isClientSide) return;

        // 重新应用总加成（使用旧的NBT数据）
        SpellPotencyUtil.recalculateAndApply(player);
    }

    @SubscribeEvent
    public static void onPlayerClone(PlayerEvent.Clone event) {
        if (!event.getEntity().level().isClientSide()) {
            Player original = event.getOriginal();
            Player newPlayer = event.getEntity();

            // 复制旧的硬糖等级数据
            int candyLevel = SpellPotencyUtil.getCandyPotencyLevel(original);
            if (candyLevel > 0) {
                SpellPotencyUtil.setCandyPotencyLevel(newPlayer, candyLevel);
            }

            // 重新应用总加成
            if (!event.isWasDeath()) {
                SpellPotencyUtil.recalculateAndApply(newPlayer);
            }
        }
    }
}