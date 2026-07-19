package net.v_black_cat.goetydelight.screen;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.ImageButton;
import net.minecraft.client.gui.components.WidgetSprites;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.gui.screens.recipebook.RecipeBookComponent;
import net.minecraft.client.gui.screens.recipebook.RecipeUpdateListener;
import net.minecraft.client.gui.screens.recipebook.SmokingRecipeBookComponent;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.ClickType;
import net.minecraft.world.inventory.Slot;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.v_black_cat.goetydelight.GoetyDelight;

@OnlyIn(Dist.CLIENT)
public class ShadeStoveScreen extends AbstractContainerScreen<ShadeStoveMenu>
        implements RecipeUpdateListener {
    private static final ResourceLocation RECIPE_BUTTON_LOCATION = ResourceLocation.fromNamespaceAndPath(GoetyDelight.MODID, "textures/gui/recipe_button.png");
    private static final ResourceLocation TEXTURE = ResourceLocation.fromNamespaceAndPath(GoetyDelight.MODID, "textures/gui/shade_stove.png");

    private final SmokingRecipeBookComponent recipeBook = new SmokingRecipeBookComponent();
    private boolean widthTooNarrow;

    public ShadeStoveScreen(ShadeStoveMenu menu, Inventory playerInventory, Component title) {
        super(menu, playerInventory, Component.translatable("goetydelight.container.shade_stove"));
        this.imageWidth = 176;
        this.imageHeight = 166;
    }

    @Override
    public void init() {
        super.init();
        this.widthTooNarrow = this.width < 379;
        this.recipeBook.init(this.width, this.height, this.minecraft, this.widthTooNarrow, this.menu);
        this.leftPos = this.recipeBook.updateScreenPosition(this.width, this.imageWidth);

        WidgetSprites buttonSprites = new WidgetSprites(RECIPE_BUTTON_LOCATION, RECIPE_BUTTON_LOCATION);
        this.addRenderableWidget(new ImageButton(
        this.leftPos + 20,
        this.height / 2 - 49,
        20, 18,
        buttonSprites,
        (button) -> {
            this.recipeBook.toggleVisibility();
            this.leftPos = this.recipeBook.updateScreenPosition(this.width, this.imageWidth);
            button.setPosition(this.leftPos + 20, this.height / 2 - 49);
        }
        ));

        this.titleLabelX = (this.imageWidth - this.font.width(this.title)) / 2;
    }

    @Override
    protected void renderLabels(GuiGraphics guiGraphics, int mouseX, int mouseY) {
        super.renderLabels(guiGraphics, mouseX, mouseY);
        guiGraphics.drawString(this.font, this.title, this.titleLabelX, this.titleLabelY, 12632256, false);
        guiGraphics.drawString(this.font, this.playerInventoryTitle, 8, this.imageHeight - 96 + 2, 12632256, false);
    }

    @Override
    public void containerTick() {
        super.containerTick();
        this.recipeBook.tick();
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        this.renderBackground(guiGraphics, mouseX, mouseY, partialTick);

        if (this.recipeBook.isVisible() && this.widthTooNarrow) {
            this.renderBg(guiGraphics, partialTick, mouseX, mouseY);
            this.recipeBook.render(guiGraphics, mouseX, mouseY, partialTick);
        } else {
            this.recipeBook.render(guiGraphics, mouseX, mouseY, partialTick);
            super.render(guiGraphics, mouseX, mouseY, partialTick);
            this.recipeBook.renderGhostRecipe(guiGraphics, this.leftPos, this.topPos, true, partialTick);
        }

        this.renderTooltip(guiGraphics, mouseX, mouseY);
        this.recipeBook.renderTooltip(guiGraphics, this.leftPos, this.topPos, mouseX, mouseY);
    }

    @Override
    protected void renderBg(GuiGraphics guiGraphics, float partialTick, int mouseX, int mouseY) {
        int i = this.leftPos;
        int j = this.topPos;
        guiGraphics.blit(TEXTURE, i, j, 0, 0, this.imageWidth, this.imageHeight);

        // ★★★ 燃烧进度：父类 getLitProgress() 返回 0.0 ~ 1.0，乘以 13 得到像素 ★★★
        if (this.menu.isLit()) {
            int k = (int) (this.menu.getLitProgress() * 13);
            k = Mth.clamp(k, 0, 13);
            guiGraphics.blit(TEXTURE, i + 56, j + 36 + 12 - k, 177, 256 + 12 - k, 14, k + 1);
        }

        // ★★★ 烹饪进度：父类 getBurnProgress() 返回 0.0 ~ 1.0，乘以 24 得到像素 ★★★
        int l = (int) (this.menu.getBurnProgress() * 24);
        l = Mth.clamp(l, 0, 24);
        guiGraphics.blit(TEXTURE, i + 79, j + 34, 176, 14, l + 1, 16);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (this.recipeBook.mouseClicked(mouseX, mouseY, button)) {
            return true;
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    protected void slotClicked(Slot slot, int slotId, int mouseButton, ClickType type) {
        super.slotClicked(slot, slotId, mouseButton, type);
        this.recipeBook.slotClicked(slot);
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        return this.recipeBook.keyPressed(keyCode, scanCode, modifiers) || super.keyPressed(keyCode, scanCode, modifiers);
    }

    @Override
    protected boolean hasClickedOutside(double mouseX, double mouseY, int guiLeft, int guiTop, int mouseButton) {
        boolean flag = mouseX < (double) guiLeft || mouseY < (double) guiTop ||
                mouseX >= (double) (guiLeft + this.imageWidth) || mouseY >= (double) (guiTop + this.imageHeight);
        return this.recipeBook.hasClickedOutside(mouseX, mouseY, this.leftPos, this.topPos,
                        this.imageWidth, this.imageHeight, mouseButton) && flag;
    }

    @Override
    public boolean charTyped(char codePoint, int modifiers) {
        return this.recipeBook.charTyped(codePoint, modifiers) || super.charTyped(codePoint, modifiers);
    }

    @Override
    public void recipesUpdated() {
        this.recipeBook.recipesUpdated();
    }

    @Override
    public RecipeBookComponent getRecipeBookComponent() {
        return this.recipeBook;
    }
}