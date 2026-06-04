package net.v_black_cat.goetydelight.visual;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.monster.AbstractSkeleton;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.event.entity.living.LivingChangeTargetEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.v_black_cat.goetydelight.GoetyDelight;

import static net.v_black_cat.goetydelight.visual.GDVisualEffects.RED_EYE_FLASH_KEY;

@Mod.EventBusSubscriber(modid = GoetyDelight.MODID)
public final class VisualHandler {


    @SubscribeEvent
    public static void onSkeletonChangeTarget(LivingChangeTargetEvent event) {
        // 1. 检查目标发起者是否为骷髅类生物 (AbstractSkeleton 涵盖了普通骷髅、凋零骷髅和流浪者)
        // 如果你只想限定为原版普通骷髅，可将 AbstractSkeleton 替换为 Skeleton
        if (event.getEntity() instanceof AbstractSkeleton skeleton) {

            // 2. 获取骷髅即将锁定的新目标，并判断是否为玩家
            if (event.getNewTarget() instanceof Player player) {

                // 3. 检查玩家当前的生命值是否小于 6.0F (即低于 3 颗心)
                if (player.getHealth() < 6.0F) {

                    // 4. 调用你的视觉效果系统，为骷髅添加对应的特效
                    // 持续时间暂设为 0 (或根据你的系统设定传入自定义刻数/无限时间常量)，附带空的 CompoundTag
                    EntityVisualEffectSystem.addEffect(skeleton, RED_EYE_FLASH_KEY, 50, new CompoundTag());
                }
            }
        }
    }
}