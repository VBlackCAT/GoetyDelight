package net.v_black_cat.goetydelight.item.food;

import com.Polarice3.Goety.common.network.ModNetwork;
import com.Polarice3.Goety.common.network.server.SPlayPlayerSoundPacket;
import com.Polarice3.Goety.init.ModSounds;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.event.entity.living.LivingAttackEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
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

            // 添加免疫能力（持续20秒）
            BuffUtil.applyBuff(entity, ModBuffTypes.SUGAR_SCEPTER_IMMUNITY.getId(), COOLDOWN_TICKS, 0);
            player.getCooldowns().addCooldown(this, COOLDOWN_TICKS);
        }

        return super.finishUsingItem(stack, level, entity);
    }

    @Mod.EventBusSubscriber
    public static class DamageImmunityHandler {

        @SubscribeEvent
        public static void onLivingAttack(LivingAttackEvent event) {
            LivingEntity entity = event.getEntity();

            // 只在服务端处理
            if (entity.level().isClientSide) return;

            // 检查实体是否有免疫能力
            boolean hasImmunity = BuffUtil.hasBuff(
                    entity,
                    ModBuffTypes.SUGAR_SCEPTER_IMMUNITY.getId()
            );

            // 如果有免疫能力，取消伤害并移除能力
            if (hasImmunity) {
                // 添加击退效果（类似于示例代码中的击退逻辑）
                if (event.getSource().getEntity() instanceof LivingEntity attacker) {
                    // 计算击退方向
                    double dx =   attacker.getX()-entity.getX();
                    double dz =   attacker.getZ()-entity.getZ();

                    // 标准化方向向量
                    double length = Math.sqrt(dx * dx + dz * dz);
                    if (length > 0) {
                        dx /= length;
                        dz /= length;
                    }

                    // 应用击退
                    attacker.push(dx * 5.0, 0.2, dz * 5.0);
                    attacker.hurtMarked = true;
                }

                // 取消伤害事件
                event.setCanceled(true);

                // 移除免疫能力（一次性使用）
                BuffUtil.removeBuff(entity, ModBuffTypes.SUGAR_SCEPTER_IMMUNITY.getId());
                Vec3 pos = entity.position();
                entity.level().playSound(
                        null, // 无特定播放者
                        pos.x, pos.y, pos.z, // 实体位置
                        SoundEvents.TURTLE_EGG_CRACK, // 海龟蛋裂开音效
                        SoundSource.PLAYERS, // 音源分类
                        8.0F, // 音量
                        1F // 音调
                );
            }
        }
    }
}