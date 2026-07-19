package net.v_black_cat.goetydelight.screen;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.ImageButton;
import net.minecraft.client.gui.components.WidgetSprites;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.gui.screens.recipebook.RecipeBookComponent;
import net.minecraft.client.gui.screens.recipebook.RecipeUpdateListener;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import vectorwing.farmersdelight.client.gui.CookingPotRecipeBookComponent;
import vectorwing.farmersdelight.common.Configuration;

import javax.annotation.Nonnull;
import javax.annotation.ParametersAreNonnullByDefault;
import java.awt.Rectangle;
import java.util.ArrayList;
import java.util.List;

import static net.v_black_cat.goetydelight.GoetyDelight.MODID;

@ParametersAreNonnullByDefault
public class CursedIngotPotScreen extends AbstractContainerScreen<CursedIngotPotMenu>
        implements RecipeUpdateListener {
    private static final ResourceLocation RECIPE_BUTTON_LOCATION = ResourceLocation.parse(MODID + ":textures/gui/recipe_button.png");
    private static final ResourceLocation BACKGROUND_TEXTURE = ResourceLocation.parse(MODID + ":textures/gui/cursed_ingot_pot.png");
    private static final ResourceLocation EMPTY_SOUL_SOURCE_SLOT = ResourceLocation.parse(MODID + ":textures/gui/soul_slot.png");

    private static final Rectangle HEAT_ICON = new Rectangle(47, 55, 17, 15);
    private static final Rectangle PROGRESS_ARROW = new Rectangle(89, 25, 0, 17);
    private static final Rectangle SOUL_SOURCE_SLOT = new Rectangle(8, 55, 16, 16);

    private final CookingPotRecipeBookComponent recipeBookComponent = new CookingPotRecipeBookComponent();
    private boolean widthTooNarrow;

    public CursedIngotPotScreen(CursedIngotPotMenu menu, Inventory inv, Component title) {
        super(menu, inv, title);
    }

    @Override
    public void init() {
        super.init();
        this.widthTooNarrow = this.width < 379;
        this.titleLabelX = 28;
        this.recipeBookComponent.init(this.width, this.height, this.minecraft, this.widthTooNarrow, this.menu);
        this.leftPos = this.recipeBookComponent.updateScreenPosition(this.width, this.imageWidth);

        if ((Boolean) Configuration.ENABLE_COOKING_POT_RECIPE_BOOK.get()) {
            WidgetSprites sprites = new WidgetSprites(RECIPE_BUTTON_LOCATION, RECIPE_BUTTON_LOCATION);
            this.addRenderableWidget(new ImageButton(
            this.leftPos + 5, this.height / 2 - 49,
            20, 18, sprites,
            button -> {
                this.recipeBookComponent.toggleVisibility();
                this.leftPos = this.recipeBookComponent.updateScreenPosition(this.width, this.imageWidth);
                button.setPosition(this.leftPos + 5, this.height / 2 - 49);
            },
            Component.empty()
            ));
        } else {
            this.recipeBookComponent.hide();
            this.leftPos = this.recipeBookComponent.updateScreenPosition(this.width, this.imageWidth);
        }

        this.addWidget(this.recipeBookComponent);
        this.setInitialFocus(this.recipeBookComponent);
    }

    @Override
    protected void containerTick() {
        super.containerTick();
        this.recipeBookComponent.tick();
    }

    @Override
    public void render(GuiGraphics gui, int mouseX, int mouseY, float partialTicks) {
        super.render(gui, mouseX, mouseY, partialTicks);

        if (this.recipeBookComponent.isVisible() && this.widthTooNarrow) {
            this.recipeBookComponent.render(gui, mouseX, mouseY, partialTicks);
        } else {
            this.recipeBookComponent.render(gui, mouseX, mouseY, partialTicks);
            this.recipeBookComponent.renderGhostRecipe(gui, this.leftPos, this.topPos, false, partialTicks);
        }
        this.renderHeatIndicatorTooltip(gui, mouseX, mouseY);
        this.renderSoulSourceTooltip(gui, mouseX, mouseY);
        this.renderTooltip(gui, mouseX, mouseY);
        this.recipeBookComponent.renderTooltip(gui, this.leftPos, this.topPos, mouseX, mouseY);
    }

    private void renderHeatIndicatorTooltip(GuiGraphics gui, int mouseX, int mouseY) {
        if (this.isHovering(HEAT_ICON.x, HEAT_ICON.y, HEAT_ICON.width, HEAT_ICON.height, mouseX, mouseY)) {
            String key = "container.cursed_ingot_pot." + (this.menu.isHeated() ? "heated" : "not_heated");
            gui.renderTooltip(this.font, net.v_black_cat.goetydelight.util.TextUtils.getTranslation(key), mouseX, mouseY);
        }
    }

    private void renderSoulSourceTooltip(GuiGraphics gui, int mouseX, int mouseY) {
        if (this.isHovering(SOUL_SOURCE_SLOT.x, SOUL_SOURCE_SLOT.y, SOUL_SOURCE_SLOT.width, SOUL_SOURCE_SLOT.height, mouseX, mouseY)) {
            Slot soulSourceSlot = this.menu.slots.get(9);
            if (soulSourceSlot.hasItem()) {
                gui.renderTooltip(this.font, soulSourceSlot.getItem(), mouseX, mouseY);
            } else {
                List<Component> tooltip = new ArrayList<>();
                tooltip.add(net.v_black_cat.goetydelight.util.TextUtils.getTranslation("container.cursed_ingot_pot.soul_source.empty"));
                gui.renderComponentTooltip(this.font, tooltip, mouseX, mouseY);
            }
        }
    }

    @Override
    protected void renderLabels(GuiGraphics gui, int mouseX, int mouseY) {
        super.renderLabels(gui, mouseX, mouseY);
        gui.drawString(this.font, this.title, this.titleLabelX, this.titleLabelY, 12632256, false);
        gui.drawString(this.font, this.playerInventoryTitle, 8, this.imageHeight - 96 + 2, 12632256, false);
    }

    @Override
    protected void renderBg(GuiGraphics gui, float partialTicks, int mouseX, int mouseY) {
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        if (this.minecraft != null) {
            gui.blit(BACKGROUND_TEXTURE, this.leftPos, this.topPos, 0, 0, this.imageWidth, this.imageHeight);

            if (this.menu.isHeated()) {
                gui.blit(BACKGROUND_TEXTURE, this.leftPos + HEAT_ICON.x, this.topPos + HEAT_ICON.y,
                        176, 0, HEAT_ICON.width, HEAT_ICON.height);
            }

            int l = this.menu.getCookProgressionScaled();
            gui.blit(BACKGROUND_TEXTURE, this.leftPos + PROGRESS_ARROW.x, this.topPos + PROGRESS_ARROW.y,
                    176, 15, l + 1, PROGRESS_ARROW.height);
            gui.blit(EMPTY_SOUL_SOURCE_SLOT,
                    this.leftPos + SOUL_SOURCE_SLOT.x - 2,
                    this.topPos + SOUL_SOURCE_SLOT.y - 1,
                    0, 0, 0, 20, 20, 20, 18);
        }
    }

    @Override
    protected boolean isHovering(int x, int y, int width, int height, double mouseX, double mouseY) {
        return (!this.widthTooNarrow || !this.recipeBookComponent.isVisible()) &&
                super.isHovering(x, y, width, height, mouseX, mouseY);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (this.recipeBookComponent.mouseClicked(mouseX, mouseY, button)) {
            this.setFocused(this.recipeBookComponent);
            return true;
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    protected void slotClicked(Slot slot, int slotId, int mouseButton, net.minecraft.world.inventory.ClickType type) {
        super.slotClicked(slot, slotId, mouseButton, type);
        this.recipeBookComponent.slotClicked(slot);
    }

    @Override
    protected boolean hasClickedOutside(double mouseX, double mouseY, int guiLeft, int guiTop, int mouseButton) {
        boolean flag = mouseX < guiLeft || mouseY < guiTop ||
                mouseX >= guiLeft + this.imageWidth || mouseY >= guiTop + this.imageHeight;
        return this.recipeBookComponent.hasClickedOutside(mouseX, mouseY, this.leftPos, this.topPos,
                        this.imageWidth, this.imageHeight, mouseButton) && flag;
    }

    @Override
    public void recipesUpdated() {
        this.recipeBookComponent.recipesUpdated();
    }

    @Nonnull
    @Override
    public RecipeBookComponent getRecipeBookComponent() {
        return this.recipeBookComponent;
    }
}