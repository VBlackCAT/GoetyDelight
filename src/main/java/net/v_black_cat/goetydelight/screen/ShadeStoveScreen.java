package net.v_black_cat.goetydelight.screen;

import net.minecraft.client.gui.screens.inventory.AbstractFurnaceScreen;
import net.minecraft.client.gui.screens.recipebook.SmokingRecipeBookComponent;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.SmokerMenu;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class ShadeStoveScreen extends AbstractFurnaceScreen<ShadeStoveMenu> {
    private static final ResourceLocation TEXTURE = new ResourceLocation("textures/gui/container/smoker.png");

    public ShadeStoveScreen(ShadeStoveMenu pMenu, Inventory pPlayerInventory, Component pTitle) {
        super(pMenu, new SmokingRecipeBookComponent(), pPlayerInventory, pTitle, TEXTURE);
    }
}