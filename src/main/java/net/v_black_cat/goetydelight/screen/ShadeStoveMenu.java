package net.v_black_cat.goetydelight.screen;

import com.Polarice3.Goety.api.items.magic.ITotem;
import com.Polarice3.Goety.common.items.ModItems;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractFurnaceMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.inventory.RecipeBookType;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeType;

public class ShadeStoveMenu extends AbstractFurnaceMenu {



    public ShadeStoveMenu(int pContainerId, Inventory pPlayerInventory) {
        super(ModMenuTypes.SHADE_STOVE.get(), RecipeType.SMOKING, RecipeBookType.SMOKER, pContainerId, pPlayerInventory);
        replaceFuelSlot();
    }

    public ShadeStoveMenu(int pContainerId, Inventory pPlayerInventory, Container pSmokerContainer, ContainerData pSmokerData) {
        super(ModMenuTypes.SHADE_STOVE.get(), RecipeType.SMOKING, RecipeBookType.SMOKER, pContainerId, pPlayerInventory, pSmokerContainer, pSmokerData);
        replaceFuelSlot();
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



    protected boolean moveItemStackTo(ItemStack pStack, int pStartIndex, int pEndIndex, boolean pReverseDirection) {
        boolean flag = false;
        int i = pStartIndex;
        if (pReverseDirection) {
            i = pEndIndex - 1;
        }

        // 检查是否是燃料槽的特殊物品（灵魂图腾或连接宝石）
        boolean isSoulSourceItem = pStack.getItem() instanceof ITotem || pStack.getItem() == ModItems.SOUL_TRANSFER.get();

        if (pStack.isStackable()) {
            while(!pStack.isEmpty()) {
                if (pReverseDirection) {
                    if (i < pStartIndex) {
                        break;
                    }
                } else if (i >= pEndIndex) {
                    break;
                }

                Slot slot = this.slots.get(i);
                ItemStack itemstack = slot.getItem();

                // 特殊处理：如果是燃料槽且物品是灵魂源物品
                if (i == 1 && isSoulSourceItem) {
                    if (slot.mayPlace(pStack)) {
                        int j = Math.min(pStack.getCount(), slot.getMaxStackSize());
                        ItemStack newStack = pStack.split(j);
                        slot.setByPlayer(newStack);
                        flag = true;
                        continue;
                    }
                }

                if (!itemstack.isEmpty() && ItemStack.isSameItemSameTags(pStack, itemstack)) {
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

            while(true) {
                if (pReverseDirection) {
                    if (i < pStartIndex) {
                        break;
                    }
                } else if (i >= pEndIndex) {
                    break;
                }

                Slot slot1 = this.slots.get(i);
                ItemStack itemstack1 = slot1.getItem();

                // 特殊处理：如果是燃料槽且物品是灵魂源物品
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


    public ItemStack quickMoveStack(Player pPlayer, int pIndex) {
        ItemStack itemstack = ItemStack.EMPTY;
        Slot slot = this.slots.get(pIndex);
        if (slot != null && slot.hasItem()) {
            ItemStack itemstack1 = slot.getItem();
            itemstack = itemstack1.copy();

            // 结果槽处理
            if (pIndex == 2) {
                if (!this.moveItemStackTo(itemstack1, 3, 39, true)) {
                    return ItemStack.EMPTY;
                }
                slot.onQuickCraft(itemstack1, itemstack);
            }
            // 非原料槽/燃料槽/结果槽的处理
            else if (pIndex != 1 && pIndex != 0) {
                // 可熔炼物品放入原料槽
                if (this.canSmelt(itemstack1)) {
                    if (!this.moveItemStackTo(itemstack1, 0, 1, false)) {
                        return ItemStack.EMPTY;
                    }
                }
                // 灵魂图腾或连接宝石放入燃料槽
                else if (isSoulSource(itemstack1)) {
                    if (!this.moveItemStackTo(itemstack1, 1, 2, false)) {
                        return ItemStack.EMPTY;
                    }
                }
                // 玩家物品栏内部转移
                else if (pIndex >= 3 && pIndex < 30) {
                    if (!this.moveItemStackTo(itemstack1, 30, 39, false)) {
                        return ItemStack.EMPTY;
                    }
                } else if (pIndex >= 30 && pIndex < 39 && !this.moveItemStackTo(itemstack1, 3, 30, false)) {
                    return ItemStack.EMPTY;
                }
            }
            // 原料槽和燃料槽的处理
            else if (!this.moveItemStackTo(itemstack1, 3, 39, false)) {
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

    private boolean isSoulSource(ItemStack stack) {

        return stack.getItem() instanceof ITotem || stack.getItem() == ModItems.SOUL_TRANSFER.get();
//        // 1. 使用物品实例判断
//         return stack.getItem() ==SOUL_TOTEM || stack.getItem() == CONNECTION_GEM;

//        // 2. 使用标签系统判断
//        // return stack.is(ModTags.SOUL_SOURCES);
//
//        // 3. 使用接口判断
//        return stack.getItem() instanceof ISoulSourceProvider;
    }


    public ShadeStoveMenu(int containerId, Inventory playerInventory, FriendlyByteBuf extraData) {
        this(containerId, playerInventory);
    }
}