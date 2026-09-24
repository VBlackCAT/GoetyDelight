package net.v_black_cat.goetydelight.item.food;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.living.LivingAttackEvent;
import net.minecraftforge.event.entity.living.LivingEntityUseItemEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.config.ModConfigEvent;
import net.v_black_cat.goetydelight.util.ConvertServantUtil;

@Mod.EventBusSubscriber(modid = "goetydelight")
public class PolariceItem extends BowlFoodItem {

    public PolariceItem(Properties properties) {
        super(properties);
    }

    private long lastEatTime = 0;

    @Override
    public ItemStack finishUsingItem(ItemStack stack, Level level, LivingEntity entity) {
        if (!level.isClientSide && entity instanceof Player) {
            long currentTime = level.getGameTime();
            if (currentTime - lastEatTime <= ConvertServantUtil.getPolariceCooldown()) {
                return super.finishUsingItem(stack, level, entity);
            } else {
                lastEatTime = level.getGameTime();
            }
        }
        return super.finishUsingItem(stack, level, entity);
    }

    // ==================== 事件订阅（仅转发） ====================

    @SubscribeEvent
    public static void onItemUseFinish(LivingEntityUseItemEvent.Finish event) {
        if (event.getItem().getItem() instanceof PolariceItem) {
            ConvertServantUtil.onEaten(event.getEntity());
        }
    }

    @SubscribeEvent
    public static void onPlayerTick(TickEvent.PlayerTickEvent event) {
        if (event.phase != TickEvent.Phase.END) return;
        Player player = event.player;
        if (!player.level().isClientSide) {
            ConvertServantUtil.tickPlayer(player);
        }
    }

    @SubscribeEvent
    public static void onAttackEvent(LivingAttackEvent event) {
        ConvertServantUtil.handleAttack(event);
    }
}