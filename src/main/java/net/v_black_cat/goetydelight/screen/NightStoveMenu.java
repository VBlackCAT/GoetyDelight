package net.v_black_cat.goetydelight.screen;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.*;
import net.minecraft.world.item.ItemStack;
import net.v_black_cat.goetydelight.init.ModMenuTypes; // <-- 关键导入

public class NightStoveMenu extends AbstractContainerMenu {
    private final Container container;
    private final ContainerData data;

    public NightStoveMenu(int pContainerId, Inventory pPlayerInventory) {
        this(pContainerId, pPlayerInventory, new SimpleContainer(2), new SimpleContainerData(3)); // 改为3个数据
    }

    public NightStoveMenu(int pContainerId, Inventory pPlayerInventory, Container pContainer, ContainerData pData) {
        super(ModMenuTypes.NIGHT_STOVE.get(), pContainerId);
        this.container = pContainer;
        this.data = pData;

        // 输入槽位（索引0）
        this.addSlot(new Slot(pContainer, 0, 56, 17));
        // 输出槽位（索引1）
        this.addSlot(new FurnaceResultSlot(pPlayerInventory.player, pContainer, 1, 116, 35));

        // 玩家主背包（3行）
        for(int i = 0; i < 3; ++i) {
            for(int j = 0; j < 9; ++j) {
                this.addSlot(new Slot(pPlayerInventory, j + i * 9 + 9, 8 + j * 18, 84 + i * 18));
            }
        }
        // 玩家快捷栏
        for(int i = 0; i < 9; ++i) {
            this.addSlot(new Slot(pPlayerInventory, i, 8 + i * 18, 142));
        }

        this.addDataSlots(pData);
    }

    // 客户端网络构造器
    public NightStoveMenu(int containerId, Inventory playerInventory, FriendlyByteBuf extraData) {
        this(containerId, playerInventory);
    }

    @Override
    public boolean stillValid(Player pPlayer) {
        return this.container.stillValid(pPlayer);
    }

    @Override
    public ItemStack quickMoveStack(Player pPlayer, int pIndex) {
        ItemStack itemstack = ItemStack.EMPTY;
        Slot slot = this.slots.get(pIndex);
        if (slot != null && slot.hasItem()) {
            ItemStack itemstack1 = slot.getItem();
            itemstack = itemstack1.copy();
            if (pIndex == 1) { // 输出槽
                if (!this.moveItemStackTo(itemstack1, 2, 38, true)) {
                    return ItemStack.EMPTY;
                }
                slot.onQuickCraft(itemstack1, itemstack);
            } else if (pIndex != 0) { // 输入槽以外
                if (canCook(itemstack1)) {
                    if (!this.moveItemStackTo(itemstack1, 0, 1, false)) {
                        return ItemStack.EMPTY;
                    }
                } else if (pIndex >= 2 && pIndex < 29) {
                    if (!this.moveItemStackTo(itemstack1, 29, 38, false)) {
                        return ItemStack.EMPTY;
                    }
                } else if (pIndex >= 29 && pIndex < 38 && !this.moveItemStackTo(itemstack1, 2, 29, false)) {
                    return ItemStack.EMPTY;
                }
            } else if (!this.moveItemStackTo(itemstack1, 2, 38, false)) {
                return ItemStack.EMPTY;
            }

            if (itemstack1.isEmpty()) {
                slot.set(ItemStack.EMPTY);
            } else {
                slot.setChanged();
            }

            if (itemstack1.getCount() == itemstack.getCount()) {
                return ItemStack.EMPTY;
            }

            slot.onTake(pPlayer, itemstack1);
        }
        return itemstack;
    }

    protected boolean canCook(ItemStack pStack) {
        // 实际应根据配方检查（可调用 BlockEntity 的辅助方法，暂简单返回 true）
        return true;
    }

    public int getCookProgress() {
        int progress = this.data.get(0);
        int total = this.data.get(1);
        return total != 0 && progress != 0 ? progress * 24 / total : 0;
    }
}