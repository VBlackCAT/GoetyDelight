package net.v_black_cat.goetydelight.events;

import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;
import net.v_black_cat.goetydelight.item.food.RejectedDarkMeatSoupItem;

public class PlayerInteractEventHandler {
    public static void onLeftClickEmpty(PlayerInteractEvent.LeftClickEmpty event) {
        RejectedDarkMeatSoupItem.onLeftClickEmpty(event);
    }
}
