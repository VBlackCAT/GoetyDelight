package net.v_black_cat.goetydelight.screen;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.game.ServerboundSetBeaconPacket;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.*;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.items.ItemStackHandler;
import net.minecraftforge.items.SlotItemHandler;
import net.v_black_cat.goetydelight.block.RestaurantBlockEntity;
import net.v_black_cat.goetydelight.block.ModBlocks;

import java.util.Objects;

public class RestaurantMenu extends AbstractContainerMenu {
    public final RestaurantBlockEntity blockEntity;
    public final ItemStackHandler inventory;
    private final ContainerLevelAccess canInteractWithCallable;
    private ContainerData dataAccess;
    public static final int UPDATE_AREA_BUTTON_ID=1;
    public static final int SWITCH_RENDER_AREA_BUTTON_ID=2;
    public static final int UPDATE_DISHES_LIST_BUTTON_ID=3;

    public RestaurantMenu(int pContainerId, Inventory pPlayerInventory, FriendlyByteBuf data) {
        this(pContainerId, pPlayerInventory, getTileEntity(pPlayerInventory, data));
    }


    private static RestaurantBlockEntity getTileEntity(Inventory playerInventory, FriendlyByteBuf data) {
        Objects.requireNonNull(playerInventory, "playerInventory cannot be null");
        Objects.requireNonNull(data, "data cannot be null");
        BlockEntity tileAtPos = playerInventory.player.level().getBlockEntity(data.readBlockPos());
        if (tileAtPos instanceof RestaurantBlockEntity) {
            return (RestaurantBlockEntity)tileAtPos;
        } else {
            throw new IllegalStateException("Tile entity is not correct! " + tileAtPos);
        }
    }

    public RestaurantMenu(int windowId, Inventory playerInventory, RestaurantBlockEntity blockEntity) {
        super((MenuType) ModMenuTypes.RESTAURANT.get(), windowId);
        this.blockEntity = blockEntity;
        this.inventory = blockEntity.getInventory();
        this.dataAccess = blockEntity.getDataAccess();
        this.addDataSlots(dataAccess);
        this.canInteractWithCallable = ContainerLevelAccess.create(blockEntity.getLevel(), blockEntity.getBlockPos());  // 初始化访问权限

        int startX = 8;
        int startY = 18;
        int inputStartX = 30;
        int inputStartY = 17;
        int borderSlotSize = 18;

        // 添加3行*2列的格子，从inventory获取
        for (int row = 0; row < 5; ++row) {
            for (int col = 0; col < 2; ++col) {
                int slotIndex = col + row * 2;
                int xPos = inputStartX + col * borderSlotSize;
                int yPos = inputStartY + row * borderSlotSize;

                if (row > 2 ){
                    xPos+=18*3;
                    yPos-=18*3;
                }

                this.addSlot(new SlotItemHandler(this.inventory, slotIndex, xPos, yPos));
            }
        }

        // 玩家物品栏
        int startPlayerInvY = startY * 4 + 12;
        for(int row = 0; row < 3; ++row) {
            for(int col = 0; col < 9; ++col) {
                int slotIndex = 9 + col + row * 9;
                int xPos = startX + col * borderSlotSize;
                int yPos = startPlayerInvY + row * borderSlotSize;
                this.addSlot(new Slot(playerInventory, slotIndex, xPos, yPos));
            }
        }

        // 玩家快捷栏
        for(int col = 0; col < 9; ++col) {
            this.addSlot(new Slot(playerInventory, col, startX + col * borderSlotSize, 142));
        }
    }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        ItemStack itemstack = ItemStack.EMPTY;
        Slot slot = this.slots.get(index);
        if (slot != null && slot.hasItem()) {
            ItemStack itemstack1 = slot.getItem();
            itemstack = itemstack1.copy();

            if (index < 6) {
                if (!this.moveItemStackTo(itemstack1, 6, this.slots.size(), true)) {
                    return ItemStack.EMPTY;
                }
            } else {
                if (!this.moveItemStackTo(itemstack1, 0, 6, false)) {
                    return ItemStack.EMPTY;
                }
            }

            if (itemstack1.isEmpty()) {
                slot.set(ItemStack.EMPTY);
            } else {
                slot.setChanged();
            }
        }

        return itemstack;
    }

    @Override
    public boolean stillValid(Player player) {
        return stillValid(this.canInteractWithCallable, player, (net.minecraft.world.level.block.Block) ModBlocks.RESTAURANT.get());
    }

    @Override
    public boolean clickMenuButton(Player player, int id) {
        switch (id){
            case UPDATE_AREA_BUTTON_ID:
                this.blockEntity.updateRangesFromInventory();
                break;
            case SWITCH_RENDER_AREA_BUTTON_ID:
                this.blockEntity.switchRenderArea();
                break;
            case UPDATE_DISHES_LIST_BUTTON_ID:
                this.blockEntity.updateDishesList();
                break;
        }

        return super.clickMenuButton(player, id);
    }


}
