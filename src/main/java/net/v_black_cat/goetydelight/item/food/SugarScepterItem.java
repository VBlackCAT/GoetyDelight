package net.v_black_cat.goetydelight.item.food;

import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.v_black_cat.goetydelight.init.ModBuffTypes;
import net.v_black_cat.goetydelight.util.BuffUtil;

public class SugarScepterItem extends Item {
    // 冷却时间（20秒，以tick为单位）
    private static final int COOLDOWN_TICKS = 20 * 20;

    public SugarScepterItem(Properties pProperties) {
        super(pProperties);
    }

    @Override
    public ItemStack finishUsingItem(ItemStack stack, Level level, LivingEntity entity) {
        if (!level.isClientSide && entity instanceof Player player) {
            // 检查冷却时间
            if (player.getCooldowns().isOnCooldown(this)) {
                return super.finishUsingItem(stack, level, entity);
            }

            // 添加免疫 Buff（持续20秒）
            BuffUtil.applyBuff(entity, ModBuffTypes.SUGAR_SCEPTER_IMMUNITY.getId(), COOLDOWN_TICKS, 0);
            player.getCooldowns().addCooldown(this, COOLDOWN_TICKS);
        }

        return super.finishUsingItem(stack, level, entity);
    }

    /**
     * 由 SugarScepterImmunityBuffEffect 调用，处理伤害免疫逻辑。
     * 此处保留静态方法供外部事件处理器调用。
     */
    public static void playImmunitySound(Level level, Vec3 pos) {
        level.playSound(
                null,
                pos.x, pos.y, pos.z,
                SoundEvents.TURTLE_EGG_CRACK,
                SoundSource.PLAYERS,
                8.0F,
                1F
        );
    }
}
