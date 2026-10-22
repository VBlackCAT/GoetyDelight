package net.v_black_cat.goetydelight.compat.goetyrevelation.block;

import com.mojang.blaze3d.systems.RenderSystem;
import java.awt.Rectangle;
import java.util.ArrayList;
import java.util.List;
import javax.annotation.Nonnull;
import javax.annotation.ParametersAreNonnullByDefault;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.ImageButton;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.gui.screens.recipebook.RecipeBookComponent;
import net.minecraft.client.gui.screens.recipebook.RecipeUpdateListener;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.ClickType;
import net.minecraft.world.inventory.RecipeBookMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import vectorwing.farmersdelight.client.gui.CookingPotRecipeBookComponent;
import vectorwing.farmersdelight.common.Configuration;

import static net.v_black_cat.goetydelight.GoetyDelight.MODID;

@ParametersAreNonnullByDefault
public class ApocalyptiumCookingPotScreen extends AbstractContainerScreen<ApocalyptiumCookingPotMenu>
        implements RecipeUpdateListener {

    // 与内部名同名：assets/goetydelight/textures/gui/apocalyptium_cooking_pot.png
    private static final ResourceLocation RECIPE_BUTTON_LOCATION =
            new ResourceLocation(MODID, "textures/gui/recipe_button.png");
    private static final ResourceLocation BACKGROUND_TEXTURE =
            new ResourceLocation(MODID, "textures/gui/apocalyptium_cooking_pot.png");

    private static final Rectangle HEAT_ICON = new Rectangle(47, 55, 17, 15);
    private static final Rectangle PROGRESS_ARROW = new Rectangle(89, 25, 0, 17);
    // 不再有 SOUL_SOURCE_SLOT

    private final CookingPotRecipeBookComponent recipeBookComponent = new CookingPotRecipeBookComponent();
    private boolean widthTooNarrow;

    public ApocalyptiumCookingPotScreen(ApocalyptiumCookingPotMenu menu, Inventory inv, Component titleIn) {
        super(menu, inv, titleIn);
    }

    @Override
    public void init() {
        super.init();
        this.widthTooNarrow = this.width < 379;
        this.titleLabelX = 28;
        this.recipeBookComponent.init(this.width, this.height, this.minecraft, this.widthTooNarrow,
                (RecipeBookMenu) this.menu);
        this.leftPos = this.recipeBookComponent.updateScreenPosition(this.width, this.imageWidth);
        if (Configuration.ENABLE_COOKING_POT_RECIPE_BOOK.get()) {
            this.addRenderableWidget(new ImageButton(this.leftPos + 5, this.height / 2 - 49, 20, 18,
                    0, 0, 19, RECIPE_BUTTON_LOCATION, (button) -> {
                this.recipeBookComponent.toggleVisibility();
                this.leftPos = this.recipeBookComponent.updateScreenPosition(this.width, this.imageWidth);
                ((ImageButton) button).setPosition(this.leftPos + 5, this.height / 2 - 49);
            }));
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
        this.renderBackground(gui);
        if (this.recipeBookComponent.isVisible() && this.widthTooNarrow) {
            this.renderBg(gui, partialTicks, mouseX, mouseY);
            this.recipeBookComponent.render(gui, mouseX, mouseY, partialTicks);
        } else {
            this.recipeBookComponent.render(gui, mouseX, mouseY, partialTicks);
            super.render(gui, mouseX, mouseY, partialTicks);
            this.recipeBookComponent.renderGhostRecipe(gui, this.leftPos, this.topPos, false, partialTicks);
        }

        this.renderMealDisplayTooltip(gui, mouseX, mouseY);
        this.renderHeatIndicatorTooltip(gui, mouseX, mouseY);
        this.recipeBookComponent.renderTooltip(gui, this.leftPos, this.topPos, mouseX, mouseY);
    }

    private void renderHeatIndicatorTooltip(GuiGraphics gui, int mouseX, int mouseY) {
        if (this.isHovering(HEAT_ICON.x, HEAT_ICON.y, HEAT_ICON.width, HEAT_ICON.height, mouseX, mouseY)) {
            String key = "container.apocalyptium_cooking_pot."
                    + (this.menu.isHeated() ? "heated" : "not_heated");
            gui.renderTooltip(this.font,
                    net.v_black_cat.goetydelight.util.TextUtils.getTranslation(key, this.menu),
                    mouseX, mouseY);
        }
    }

    protected void renderMealDisplayTooltip(GuiGraphics gui, int mouseX, int mouseY) {
        if (this.minecraft != null && this.minecraft.player != null
                && this.menu.getCarried().isEmpty()
                && this.hoveredSlot != null && this.hoveredSlot.hasItem()) {
            if (this.hoveredSlot.index == 6) {
                List<Component> tooltip = new ArrayList<>();
                ItemStack mealStack = this.hoveredSlot.getItem();
                tooltip.add(((MutableComponent) mealStack.getItem().getDescription())
                        .withStyle(mealStack.getRarity().color));
                ItemStack containerStack = this.menu.blockEntity.getContainer();
                String container = !containerStack.isEmpty()
                        ? containerStack.getItem().getDescription().getString() : "";
                tooltip.add(net.v_black_cat.goetydelight.util.TextUtils.getTranslation(
                                "container.apocalyptium_cooking_pot.served_on", container)
                        .withStyle(ChatFormatting.GRAY));

                // 检查是否有灵魂注入，并显示灵魂强度等级
                if (mealStack.hasTag() && mealStack.getTag().getBoolean("SoulInfused")) {
                    int level = mealStack.getTag().contains("SoulInfusedLevel")
                            ? mealStack.getTag().getInt("SoulInfusedLevel") : 1;
                    if (level < 1) {
                        level = 1;
                    }
                    tooltip.add(net.v_black_cat.goetydelight.util.TextUtils
                            .getTranslation("container.apocalyptium_cooking_pot.soul_infused", level)
                            .withStyle(ChatFormatting.DARK_PURPLE));
                }
                gui.renderComponentTooltip(this.font, tooltip, mouseX, mouseY);
            } else {
                gui.renderTooltip(this.font, this.hoveredSlot.getItem(), mouseX, mouseY);
            }
        }
    }

    @Override
    protected void renderLabels(GuiGraphics guiGraphics, int mouseX, int mouseY) {
        super.renderLabels(guiGraphics, mouseX, mouseY);
        guiGraphics.drawString(this.font, this.title, this.titleLabelX, this.titleLabelY, 12632256, false);
        guiGraphics.drawString(this.font, this.playerInventoryTitle, 8, this.imageHeight - 96 + 2, 12632256, false);
    }

    @Override
    protected void renderBg(GuiGraphics gui, float partialTicks, int mouseX, int mouseY) {
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        if (this.minecraft != null) {
            gui.blit(BACKGROUND_TEXTURE, this.leftPos, this.topPos, 0, 0, this.imageWidth, this.imageHeight);

            if (this.menu.isHeated()) {
                gui.blit(BACKGROUND_TEXTURE,
                        this.leftPos + HEAT_ICON.x, this.topPos + HEAT_ICON.y,
                        176, 0, HEAT_ICON.width, HEAT_ICON.height);
            }

            int progress = this.menu.getCookProgressionScaled();
            gui.blit(BACKGROUND_TEXTURE,
                    this.leftPos + PROGRESS_ARROW.x, this.topPos + PROGRESS_ARROW.y,
                    176, 15, progress + 1, PROGRESS_ARROW.height);

            // 不再绘制灵魂源插槽
        }
    }

    @Override
    protected boolean isHovering(int x, int y, int width, int height, double mouseX, double mouseY) {
        return (!this.widthTooNarrow || !this.recipeBookComponent.isVisible())
                && super.isHovering(x, y, width, height, mouseX, mouseY);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int buttonId) {
        if (this.recipeBookComponent.mouseClicked(mouseX, mouseY, buttonId)) {
            this.setFocused(this.recipeBookComponent);
            return true;
        }
        return this.widthTooNarrow && this.recipeBookComponent.isVisible()
                || super.mouseClicked(mouseX, mouseY, buttonId);
    }

    @Override
    protected boolean hasClickedOutside(double mouseX, double mouseY, int x, int y, int buttonIdx) {
        boolean flag = mouseX < x || mouseY < y || mouseX >= x + this.imageWidth || mouseY >= y + this.imageHeight;
        return flag && this.recipeBookComponent.hasClickedOutside(mouseX, mouseY,
                this.leftPos, this.topPos, this.imageWidth, this.imageHeight, buttonIdx);
    }

    @Override
    protected void slotClicked(Slot slot, int mouseX, int mouseY, ClickType clickType) {
        super.slotClicked(slot, mouseX, mouseY, clickType);
        this.recipeBookComponent.slotClicked(slot);
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