package net.v_black_cat.goetydelight.events;

import net.neoforged.neoforge.client.extensions.common.RegisterClientExtensionsEvent;
import net.v_black_cat.goetydelight.item.food.BoatStuffedRoastedWardenItem;

public class RegisterClientExtensionsEventHandler {
    public static void onRegisterClientExtensions(RegisterClientExtensionsEvent event) {
        BoatStuffedRoastedWardenItem.onRegisterClientExtensions(event);
    }
}
