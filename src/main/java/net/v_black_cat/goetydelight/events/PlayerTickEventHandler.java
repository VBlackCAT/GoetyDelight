package net.v_black_cat.goetydelight.events;

import net.minecraft.world.entity.player.Player;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;
import net.v_black_cat.goetydelight.init.ModAttachments;
import net.v_black_cat.goetydelight.item.food.SevenLeafPuddingItem;
import net.v_black_cat.goetydelight.item.food.SundaeOfThePhilosophersPotionItem;
import net.v_black_cat.goetydelight.util.FoodState;

public class PlayerTickEventHandler {

    /**
     * 处理七叶布丁过期检查和贤者圣代挖掘速度加成
     */
    public static void onPlayerTick(PlayerTickEvent.Post event) {
        Player player = event.getEntity();
        if (player.level().isClientSide) return;

        // 过期检查降频为每 20 tick（1秒）一次，避免每 tick 读 NBT
        if (player.tickCount % 20 != 0) return;

        // 七叶布丁过期检查
        SevenLeafPuddingItem.checkAndRemoveExpired(player, player.level());
    }

    /**
     * 贤者圣代挖掘速度加成
     */
    public static void onBreakSpeed(PlayerEvent.BreakSpeed event) {
        Player player = event.getEntity();
        float boosted = SundaeOfThePhilosophersPotionItem.applyMiningSpeedBoost(player, event.getOriginalSpeed());
        if (boosted != event.getOriginalSpeed()) {
            event.setNewSpeed(boosted);
        }
    }

    /**
     * 玩家死亡时清理七叶布丁加成
     */
    public static void onPlayerDeath(net.neoforged.neoforge.event.entity.living.LivingDeathEvent event) {
        if (event.getEntity() instanceof Player player) {
            SevenLeafPuddingItem.onPlayerDeath(player);
        }
    }

    /**
     * 玩家重生时清理七叶布丁数据
     */
    public static void onPlayerRespawn(PlayerEvent.PlayerRespawnEvent event) {
        Player player = event.getEntity();
        FoodState state = player.getData(ModAttachments.FOOD_STATE);
        state.setSevenLeafPuddingActive(false);
        state.setSevenLeafPuddingActivationTime(0);
    }
}
