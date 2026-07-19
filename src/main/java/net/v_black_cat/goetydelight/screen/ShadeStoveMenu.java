package net.v_black_cat.goetydelight.screen;

import com.Polarice3.Goety.api.items.magic.ITotem;
import com.Polarice3.Goety.common.items.ModItems;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractFurnaceMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.inventory.RecipeBookType;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeType;
import net.v_black_cat.goetydelight.init.ModMenuTypes;

public class ShadeStoveMenu extends AbstractFurnaceMenu {

    public ShadeStoveMenu(int pContainerId, Inventory pPlayerInventory) {
        super(ModMenuTypes.SHADE_STOVE.get(), RecipeType.SMOKING, RecipeBookType.SMOKER, pContainerId, pPlayerInventory);
        replaceFuelSlot();
    }

    public ShadeStoveMenu(int pContainerId, Inventory pPlayerInventory, Container pSmokerContainer, ContainerData pSmokerData) {
        super(ModMenuTypes.SHADE_STOVE.get(), RecipeType.SMOKING, RecipeBookType.SMOKER, pContainerId, pPlayerInventory, pSmokerContainer, pSmokerData);
        replaceFuelSlot();
    }

    public ShadeStoveMenu(int containerId, Inventory playerInventory, FriendlyByteBuf extraData) {
        this(containerId, playerInventory);
    }

    private void replaceFuelSlot() {
        for (int i = 0; i < this.slots.size(); i++) {
            Slot slot = this.slots.get(i);
            if (slot.index == 1) {
                SoulFuelSlot newSlot = new SoulFuelSlot(slot.container, 1, slot.x, slot.y);
                newSlot.index = 1;
                this.slots.set(i, newSlot);
                break;
            }
        }
    }

    @Override
    protected boolean isFuel(ItemStack pStack) {
        return pStack.getItem() instanceof ITotem ||
                pStack.getItem() == ModItems.SOUL_TRANSFER.get();
    }

    private boolean isSoulSource(ItemStack stack) {
        return stack.getItem() instanceof ITotem || stack.getItem() == ModItems.SOUL_TRANSFER.get();
    }

    @Override
    protected boolean moveItemStackTo(ItemStack pStack, int pStartIndex, int pEndIndex, boolean pReverseDirection) {
        boolean flag = false;
        int i = pStartIndex;
        if (pReverseDirection) {
            i = pEndIndex - 1;
        }

        boolean isSoulSourceItem = pStack.getItem() instanceof ITotem || pStack.getItem() == ModItems.SOUL_TRANSFER.get();

        if (pStack.isStackable()) {
            while (!pStack.isEmpty()) {
                if (pReverseDirection) {
                    if (i < pStartIndex) break;
                } else if (i >= pEndIndex) break;

                Slot slot = this.slots.get(i);
                ItemStack itemstack = slot.getItem();

                if (i == 1 && isSoulSourceItem) {
                    if (slot.mayPlace(pStack)) {
                        int j = Math.min(pStack.getCount(), slot.getMaxStackSize());
                        ItemStack newStack = pStack.split(j);
                        slot.setByPlayer(newStack);
                        flag = true;
                        continue;
                    }
                }

                if (!itemstack.isEmpty() && ItemStack.isSameItemSameComponents(itemstack, pStack)) {
                    int j = itemstack.getCount() + pStack.getCount();
                    int maxSize = Math.min(slot.getMaxStackSize(), pStack.getMaxStackSize());
                    if (j <= maxSize) {
                        pStack.setCount(0);
                        itemstack.setCount(j);
                        slot.setChanged();
                        flag = true;
                    } else if (itemstack.getCount() < maxSize) {
                        pStack.shrink(maxSize - itemstack.getCount());
                        itemstack.setCount(maxSize);
                        slot.setChanged();
                        flag = true;
                    }
                }

                if (pReverseDirection) {
                    --i;
                } else {
                    ++i;
                }
            }
        }

        if (!pStack.isEmpty()) {
            if (pReverseDirection) {
                i = pEndIndex - 1;
            } else {
                i = pStartIndex;
            }

            while (true) {
                if (pReverseDirection) {
                    if (i < pStartIndex) break;
                } else if (i >= pEndIndex) break;

                Slot slot1 = this.slots.get(i);
                ItemStack itemstack1 = slot1.getItem();

                if (i == 1 && isSoulSourceItem) {
                    if (slot1.mayPlace(pStack)) {
                        if (pStack.getCount() > slot1.getMaxStackSize()) {
                            slot1.setByPlayer(pStack.split(slot1.getMaxStackSize()));
                        } else {
                            slot1.setByPlayer(pStack.split(pStack.getCount()));
                        }
                        slot1.setChanged();
                        flag = true;
                        break;
                    }
                }

                if (itemstack1.isEmpty() && slot1.mayPlace(pStack)) {
                    if (pStack.getCount() > slot1.getMaxStackSize()) {
                        slot1.setByPlayer(pStack.split(slot1.getMaxStackSize()));
                    } else {
                        slot1.setByPlayer(pStack.split(pStack.getCount()));
                    }
                    slot1.setChanged();
                    flag = true;
                    break;
                }

                if (pReverseDirection) {
                    --i;
                } else {
                    ++i;
                }
            }
        }

        return flag;
    }

    @Override
    public ItemStack quickMoveStack(Player pPlayer, int pIndex) {
        ItemStack itemstack = ItemStack.EMPTY;
        Slot slot = this.slots.get(pIndex);
        if (slot != null && slot.hasItem()) {
            ItemStack itemstack1 = slot.getItem();
            itemstack = itemstack1.copy();

            if (pIndex == 2) {
                if (!this.moveItemStackTo(itemstack1, 3, 39, true)) {
                    return ItemStack.EMPTY;
                }
                slot.onQuickCraft(itemstack1, itemstack);
            } else if (pIndex != 1 && pIndex != 0) {
                if (this.canSmelt(itemstack1)) {
                    if (!this.moveItemStackTo(itemstack1, 0, 1, false)) {
                        return ItemStack.EMPTY;
                    }
                } else if (isSoulSource(itemstack1)) {
                    if (!this.moveItemStackTo(itemstack1, 1, 2, false)) {
                        return ItemStack.EMPTY;
                    }
                } else if (pIndex >= 3 && pIndex < 30) {
                    if (!this.moveItemStackTo(itemstack1, 30, 39, false)) {
                        return ItemStack.EMPTY;
                    }
                } else if (pIndex >= 30 && pIndex < 39 && !this.moveItemStackTo(itemstack1, 3, 30, false)) {
                    return ItemStack.EMPTY;
                }
            } else if (!this.moveItemStackTo(itemstack1, 3, 39, false)) {
                return ItemStack.EMPTY;
            }

            if (itemstack1.isEmpty()) {
                slot.setByPlayer(ItemStack.EMPTY);
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

    protected boolean canSmelt(ItemStack pStack) {
        return !pStack.isEmpty() &&
                !(pStack.getItem() instanceof ITotem) &&
                pStack.getItem() != ModItems.SOUL_TRANSFER.get();
    }

    // 不再重写 stillValid，使用父类默认实现

    // ===== 自定义灵魂燃料槽 =====
    private static class SoulFuelSlot extends Slot {
        public SoulFuelSlot(Container pContainer, int pSlot, int pX, int pY) {
            super(pContainer, pSlot, pX, pY);
        }

        @Override
        public boolean mayPlace(ItemStack pStack) {
            return pStack.getItem() instanceof ITotem || pStack.getItem() == ModItems.SOUL_TRANSFER.get();
        }

        @Override
        public int getMaxStackSize() {
            return 1;
        }

        @Override
        public boolean mayPickup(Player pPlayer) {
            return true;
        }
    }
}