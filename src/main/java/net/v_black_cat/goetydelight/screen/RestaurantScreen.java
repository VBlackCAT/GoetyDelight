package net.v_black_cat.goetydelight.screen;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.Slot;
import net.v_black_cat.goetydelight.block.RestaurantBlockEntity;

import static net.v_black_cat.goetydelight.screen.ModMenuTypes.getTranslatedString;

public class RestaurantScreen extends AbstractContainerScreen<RestaurantMenu> {
    public RestaurantScreen(RestaurantMenu menu, Inventory playerInventory, Component title) {
        super(menu, playerInventory, title);
    }

    @Override
    protected void renderBg(GuiGraphics guiGraphics, float v, int i, int i1) {

    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        super.render(guiGraphics, mouseX, mouseY, partialTick);

        for (Slot slot : this.menu.slots) {
            int x = slot.x + this.leftPos;
            int y = slot.y + this.topPos;
            guiGraphics.fill(RenderType.guiOverlay(), x, y, x + 16, y + 16, 0x44FF0000);
        }

        this.renderCustomLabels(guiGraphics, mouseX, mouseY);
    }

    protected void renderCustomLabels(GuiGraphics guiGraphics, int mouseX, int mouseY) {

        RestaurantBlockEntity blockEntity = this.menu.blockEntity;
        int restaurantLevel = blockEntity.getRestaurantLevel();
        float totalExpForLevel = RestaurantBlockEntity.getTotalExpForLevel(restaurantLevel);
        float totalExpForNextLevel = RestaurantBlockEntity.getTotalExpForLevel(restaurantLevel+1);
        float MaxExp = totalExpForNextLevel - totalExpForLevel;
        float currentExp = blockEntity.getRestaurantExperience()-totalExpForLevel;
        String s = currentExp+"/"+MaxExp;
        int guiLeft = (this.width - this.imageWidth) / 2+60;
        int guiTop = (this.height - this.imageHeight) / 2-20;
        int size = blockEntity.getDishesList().size();
        guiGraphics.drawString(this.font, getTranslatedString("restaurant_level") + ": " + String.valueOf(restaurantLevel), guiLeft, guiTop, 6666666, false);
        guiGraphics.drawString(this.font, getTranslatedString("restaurant_exp") + ": " + s, guiLeft, guiTop + 10, 6666666, false);
        guiGraphics.drawString(this.font, getTranslatedString("restaurant_dishes") + ": " + String.valueOf(size), guiLeft, guiTop + 20, 6666666, false);

        boolean open = blockEntity.getOpen();
        if(open){
            guiGraphics.drawString(this.font, getTranslatedString("open"), guiLeft,guiTop+30, 6666666, false);
        }else{
            guiGraphics.drawString(this.font, getTranslatedString("closed"), guiLeft,guiTop+30, 6666666, false);
        }
    }

    @Override
    protected void init() {
        super.init();
        initButton();

    }


    void initButton(){

        int guiLeft = (this.width - this.imageWidth) / 2;
        int guiTop = (this.height - this.imageHeight) / 2-20;


        int xPos = this.menu.slots.get(0).x + 40;
        int yPos = this.menu.slots.get(0).y;

        Button b1 = Button.builder(Component.literal(getTranslatedString("update_restaurant_info")), button -> {
                    this.minecraft.gameMode.handleInventoryButtonClick(this.menu.containerId, RestaurantMenu.UPDATE_RESTAURANT_BUTTON_ID);
        }).pos(guiLeft + xPos + 60, guiTop + yPos)
                .size(30, 20)
                .tooltip(Tooltip.create(Component.literal(getTranslatedString("update_restaurant_info"))))
                .build();
        Button b2 = Button.builder(Component.literal(getTranslatedString("switch_render_area")), button -> {
                    this.minecraft.gameMode.handleInventoryButtonClick(this.menu.containerId, RestaurantMenu.SWITCH_RENDER_AREA_BUTTON_ID);
        }).pos(guiLeft + xPos + 60, guiTop + yPos+20)
                .size(30, 20)
                .tooltip(Tooltip.create(Component.literal(getTranslatedString("switch_render_area"))))
                .build();

        Button b3 = Button.builder(Component.literal(getTranslatedString("switch_restaurant")), button -> {
                    this.minecraft.gameMode.handleInventoryButtonClick(this.menu.containerId, RestaurantMenu.SWITCH_RESTAURANT_BUTTON_ID);
        }).pos(guiLeft + xPos + 60, guiTop + yPos+40)
                .size(30, 20)
                .tooltip(Tooltip.create(Component.literal(getTranslatedString("switch_restaurant"))))
                .build();

        Button b4 = Button.builder(Component.literal(getTranslatedString("soul_lure")), button -> {
                    this.minecraft.gameMode.handleInventoryButtonClick(this.menu.containerId, RestaurantMenu.SOUL_LURE_BUTTON_ID);
        }).pos(guiLeft + xPos + 60, guiTop + yPos+60)
                .size(30, 20)
                .tooltip(Tooltip.create(Component.literal(getTranslatedString("soul_lure"))))
                .build();

        this.addRenderableWidget(b1);
        this.addRenderableWidget(b2);
        this.addRenderableWidget(b3);
        this.addRenderableWidget(b4);
    }
}
