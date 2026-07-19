package net.v_black_cat.goetydelight.screen;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.v_black_cat.goetydelight.GoetyDelight;

public class NightStoveScreen extends AbstractContainerScreen<NightStoveMenu> {
    private static final ResourceLocation TEXTURE = ResourceLocation.fromNamespaceAndPath(GoetyDelight.MODID, "textures/gui/night_stove.png");

    public NightStoveScreen(NightStoveMenu menu, Inventory inventory, Component title) {
        super(menu, inventory, title);
        this.imageWidth = 176;
        this.imageHeight = 166;
    }

    @Override
    protected void init() {
        super.init();
        // 居中标题
        this.titleLabelX = (this.imageWidth - this.font.width(this.title)) / 2;
    }

    @Override
    protected void renderLabels(GuiGraphics guiGraphics, int mouseX, int mouseY) {
        super.renderLabels(guiGraphics, mouseX, mouseY);
        // 绘制标题（白色）
        guiGraphics.drawString(this.font, this.title, this.titleLabelX, this.titleLabelY, 0xFFFFFF, false);
        // 绘制玩家物品栏标题
        guiGraphics.drawString(this.font, this.playerInventoryTitle, 8, this.imageHeight - 96 + 2, 0x404040, false);
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        // 渲染背景（传入鼠标坐标）
        this.renderBackground(guiGraphics, mouseX, mouseY, partialTick);
        super.render(guiGraphics, mouseX, mouseY, partialTick);
        this.renderTooltip(guiGraphics, mouseX, mouseY);
    }

    @Override
    protected void renderBg(GuiGraphics guiGraphics, float partialTick, int mouseX, int mouseY) {
        int x = this.leftPos;
        int y = this.topPos;

        // 绘制背景纹理
        guiGraphics.blit(TEXTURE, x, y, 0, 0, this.imageWidth, this.imageHeight);

        // 绘制烹饪进度条
        int progress = this.menu.getCookProgress();
        if (progress > 0) {
            // 进度条位置：x+79, y+34，宽度 progress+1，高度 16
            // 纹理位置：x=176, y=14（对应纹理中进度条区域）
            guiGraphics.blit(TEXTURE, x + 79, y + 34, 176, 14, progress + 1, 16);
        }
    }
}