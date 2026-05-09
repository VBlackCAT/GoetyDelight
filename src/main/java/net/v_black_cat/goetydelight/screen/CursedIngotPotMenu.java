
package net.v_black_cat.goetydelight.screen;

import com.Polarice3.Goety.api.items.magic.ITotem;
import com.Polarice3.Goety.common.items.ModItems;
import com.mojang.datafixers.util.Pair;
import java.util.Objects;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.player.StackedContents;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.inventory.InventoryMenu;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.inventory.RecipeBookMenu;
import net.minecraft.world.inventory.RecipeBookType;
import net.minecraft.world.inventory.SimpleContainerData;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.items.ItemStackHandler;
import net.minecraftforge.items.SlotItemHandler;
import net.minecraftforge.items.wrapper.RecipeWrapper;
import net.v_black_cat.goetydelight.GoetyDelight;
import net.v_black_cat.goetydelight.block.CursedIngotPotBlockEntity;
import net.v_black_cat.goetydelight.block.ModBlocks;
import vectorwing.farmersdelight.FarmersDelight;

import vectorwing.farmersdelight.common.block.entity.container.CookingPotMealSlot;
import vectorwing.farmersdelight.common.tag.ModTags;

import static net.v_black_cat.goetydelight.GoetyDelight.MODID;

public class CursedIngotPotMenu extends RecipeBookMenu<RecipeWrapper> {
    public static final ResourceLocation EMPTY_CONTAINER_SLOT_BOWL = new ResourceLocation("farmersdelight", "item/empty_container_slot_bowl");

    public final CursedIngotPotBlockEntity blockEntity;
    public final ItemStackHandler inventory;
    private final ContainerData cookingPotData;
    private final ContainerLevelAccess canInteractWithCallable;
    protected final Level level;

    public CursedIngotPotMenu(int windowId, Inventory playerInventory, FriendlyByteBuf data) {
        this(windowId, playerInventory, getTileEntity(playerInventory, data), new SimpleContainerData(4));
    }

    public CursedIngotPotMenu(int windowId, Inventory playerInventory, CursedIngotPotBlockEntity blockEntity, ContainerData cookingPotDataIn) {
        super((MenuType) ModMenuTypes.CURSED_INGOT_POT.get(), windowId);
        this.blockEntity = blockEntity;
        this.inventory = blockEntity.getInventory();
        this.cookingPotData = cookingPotDataIn;
        this.level = playerInventory.player.level();
        this.canInteractWithCallable = ContainerLevelAccess.create(blockEntity.getLevel(), blockEntity.getBlockPos());
        int startX = 8;
        int startY = 18;
        int inputStartX = 30;
        int inputStartY = 17;
        int borderSlotSize = 18;

        // 原料插槽 (0-5)
        for(int row = 0; row < 2; ++row) {
            for(int col = 0; col < 3; ++col) {
                this.addSlot(new SlotItemHandler(this.inventory, row * 3 + col,
                        inputStartX + col * borderSlotSize,
                        inputStartY + row * borderSlotSize));
            }
        }

        // 成品显示插槽 (6)
        this.addSlot(new CookingPotMealSlot(this.inventory, 6, 124, 26));

        // 容器输入插槽 (7)
        this.addSlot(new SlotItemHandler(this.inventory, 7, 92, 55) {
            public Pair<ResourceLocation, ResourceLocation> getNoItemIcon() {
                return Pair.of(InventoryMenu.BLOCK_ATLAS, CursedIngotPotMenu.EMPTY_CONTAINER_SLOT_BOWL);
            }
        });

        // 输出插槽 (8)
        this.addSlot(new CursedPotResultSlot(playerInventory.player, blockEntity, this.inventory, 8, 124, 55));

        // 新增：灵魂源插槽 (9)
        this.addSlot(new SlotItemHandler(this.inventory, 9, 8, 55) {
            @Override
            public boolean mayPlace(ItemStack stack) {
                return stack.getItem() instanceof ITotem || stack.getItem() == ModItems.SOUL_TRANSFER.get();
            }

        });

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

        this.addDataSlots(cookingPotDataIn);
    }

    private static CursedIngotPotBlockEntity getTileEntity(Inventory playerInventory, FriendlyByteBuf data) {
        Objects.requireNonNull(playerInventory, "playerInventory cannot be null");
        Objects.requireNonNull(data, "data cannot be null");
        BlockEntity tileAtPos = playerInventory.player.level().getBlockEntity(data.readBlockPos());
        if (tileAtPos instanceof CursedIngotPotBlockEntity) {
            return (CursedIngotPotBlockEntity)tileAtPos;
        } else {
            throw new IllegalStateException("Tile entity is not correct! " + tileAtPos);
        }
    }

    public boolean stillValid(Player playerIn) {
        return stillValid(this.canInteractWithCallable, playerIn, (Block) ModBlocks.CURSED_INGOT_POT.get());
    }

    public ItemStack quickMoveStack(Player playerIn, int index) {
        int indexMealDisplay = 6;
        int indexContainerInput = 7;
        int indexOutput = 8;
        int indexSoulSource = 9; // 新增灵魂源插槽索引
        int startPlayerInv = indexOutput + 1;
        int endPlayerInv = startPlayerInv + 36;
        ItemStack slotStackCopy = ItemStack.EMPTY;
        Slot slot = (Slot)this.slots.get(index);
        if (slot.hasItem()) {
            ItemStack slotStack = slot.getItem();
            slotStackCopy = slotStack.copy();
            if (index == indexOutput) {
                if (!this.moveItemStackTo(slotStack, startPlayerInv, endPlayerInv, true)) {
                    return ItemStack.EMPTY;
                }
            } else if (index <= indexOutput) {
                if (!this.moveItemStackTo(slotStack, startPlayerInv, endPlayerInv, false)) {
                    return ItemStack.EMPTY;
                }
            } else if (index == indexSoulSource) {
                // 灵魂源插槽的物品只能移动到玩家物品栏
                if (!this.moveItemStackTo(slotStack, startPlayerInv, endPlayerInv, false)) {
                    return ItemStack.EMPTY;
                }
            } else {
                boolean isValidContainer = slotStack.is(ModTags.Items.SERVING_CONTAINERS) || slotStack.is(this.blockEntity.getContainer().getItem());
                if (isValidContainer && !this.moveItemStackTo(slotStack, indexContainerInput, indexContainerInput + 1, false)) {
                    return ItemStack.EMPTY;
                }

                // 检查是否为图腾或链接宝石，如果是则移动到灵魂源插槽
                boolean isSoulSource = slotStack.getItem() instanceof ITotem || slotStack.getItem() == ModItems.SOUL_TRANSFER.get();
                if (isSoulSource && !this.moveItemStackTo(slotStack, indexSoulSource, indexSoulSource + 1, false)) {
                    return ItemStack.EMPTY;
                }

                if (!this.moveItemStackTo(slotStack, 0, indexMealDisplay, false)) {
                    return ItemStack.EMPTY;
                }

                if (!this.moveItemStackTo(slotStack, indexContainerInput, indexOutput, false)) {
                    return ItemStack.EMPTY;
                }
            }

            if (slotStack.isEmpty()) {
                slot.set(ItemStack.EMPTY);
            } else {
                slot.setChanged();
            }

            if (slotStack.getCount() == slotStackCopy.getCount()) {
                return ItemStack.EMPTY;
            }

            slot.onTake(playerIn, slotStack);
        }

        return slotStackCopy;
    }

    public int getCookProgressionScaled() {
        int i = this.cookingPotData.get(0);
        int j = this.cookingPotData.get(1);
        return j != 0 && i != 0 ? i * 24 / j : 0;
    }

    public boolean isHeated() {
        return this.blockEntity.isHeated();
    }

    public void fillCraftSlotsStackedContents(StackedContents helper) {
        for(int i = 0; i < this.inventory.getSlots(); ++i) {
            helper.accountSimpleStack(this.inventory.getStackInSlot(i));
        }

    }

    public void clearCraftingContent() {
        for(int i = 0; i < 6; ++i) {
            this.inventory.setStackInSlot(i, ItemStack.EMPTY);
        }

    }

    public boolean recipeMatches(Recipe<? super RecipeWrapper> recipe) {
        return recipe.matches(new RecipeWrapper(this.inventory), this.level);
    }

    public int getResultSlotIndex() {
        return 7;
    }

    public int getGridWidth() {
        return 3;
    }

    public int getGridHeight() {
        return 2;
    }

    public int getSize() {
        return 7;
    }

    public RecipeBookType getRecipeBookType() {
        return FarmersDelight.RECIPE_TYPE_COOKING;
    }

    public boolean shouldMoveToInventory(int slot) {
        return slot < this.getGridWidth() * this.getGridHeight();
    }
}
