package net.v_black_cat.goetydelight.screen;

import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import vectorwing.farmersdelight.client.gui.CookingPotScreen;
import vectorwing.farmersdelight.common.block.entity.container.CookingPotMenu;

public class CursedIngotPotScreen extends CookingPotScreen {
    public CursedIngotPotScreen(CookingPotMenu screenContainer, Inventory inv, Component titleIn) {
        super(screenContainer, inv, titleIn);
    }
}
