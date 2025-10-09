package net.v_black_cat.goetydelight.screen;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractFurnaceScreen;
import net.minecraft.client.gui.screens.recipebook.SmokingRecipeBookComponent;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.SmokerMenu;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import static net.v_black_cat.goetydelight.GoetyDelight.MODID;

@OnlyIn(Dist.CLIENT)
public class ShadeStoveScreen extends AbstractFurnaceScreen<ShadeStoveMenu> {
    private static final ResourceLocation TEXTURE = new ResourceLocation(MODID, "textures/gui/shade_stove.png");

    public ShadeStoveScreen(ShadeStoveMenu pMenu, Inventory pPlayerInventory, Component pTitle) {
        super(pMenu, new SmokingRecipeBookComponent(), pPlayerInventory,
                Component.translatable("container.goetydelight.shade_stove"), TEXTURE);
    }


}