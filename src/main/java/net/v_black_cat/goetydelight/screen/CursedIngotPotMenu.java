package net.v_black_cat.goetydelight.screen;

import com.Polarice3.Goety.api.items.magic.ITotem;
import com.Polarice3.Goety.common.items.ModItems;
import com.mojang.datafixers.util.Pair;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.player.StackedContents;
import net.minecraft.world.inventory.*;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.neoforged.neoforge.items.ItemStackHandler;
import net.neoforged.neoforge.items.SlotItemHandler;
import net.neoforged.neoforge.items.wrapper.RecipeWrapper;
import net.v_black_cat.goetydelight.block.CursedIngotPotBlockEntity;
import net.v_black_cat.goetydelight.init.ModBlocks;
import net.v_black_cat.goetydelight.init.ModMenuTypes;
import vectorwing.farmersdelight.common.block.entity.container.CookingPotMealSlot;
import vectorwing.farmersdelight.common.crafting.CookingPotRecipe;
import vectorwing.farmersdelight.common.tag.ModTags;

public class CursedIngotPotMenu extends RecipeBookMenu<RecipeWrapper, CookingPotRecipe> {
    public static final ResourceLocation EMPTY_CONTAINER_SLOT_BOWL = ResourceLocation.parse("farmersdelight:item/empty_container_slot_bowl");

    public final CursedIngotPotBlockEntity blockEntity;
    public final ItemStackHandler inventory;
    private final ContainerData cookingPotData;
    private final ContainerLevelAccess canInteractWithCallable;
    protected final Level level;

    // 服务端创建菜单时使用这个构造器
    public CursedIngotPotMenu(int windowId, Inventory playerInventory, CursedIngotPotBlockEntity blockEntity, ContainerData cookingPotDataIn) {
        super((MenuType<?>) ModMenuTypes.CURSED_INGOT_POT.get(), windowId);
        this.blockEntity = blockEntity;
        this.inventory = blockEntity.getInventory();
        this.cookingPotData = cookingPotDataIn;
        this.level = playerInventory.player.level();
        this.canInteractWithCallable = ContainerLevelAccess.create(blockEntity.getLevel(), blockEntity.getBlockPos());

        int startX = 8, startY = 18, inputStartX = 30, inputStartY = 17, border = 18;

        // 原料插槽 (0-5)
        for (int row = 0; row < 2; ++row) {
            for (int col = 0; col < 3; ++col) {
                this.addSlot(new SlotItemHandler(this.inventory, row * 3 + col,
                inputStartX + col * border, inputStartY + row * border));
            }
        }

        // 成品显示插槽 (6)
        this.addSlot(new CookingPotMealSlot(this.inventory, 6, 124, 26));

        // 容器输入插槽 (7)
        this.addSlot(new SlotItemHandler(this.inventory, 7, 92, 55) {
            @Override
            public Pair<ResourceLocation, ResourceLocation> getNoItemIcon() {
                return Pair.of(InventoryMenu.BLOCK_ATLAS, CursedIngotPotMenu.EMPTY_CONTAINER_SLOT_BOWL);
            }
        });

        // 输出插槽 (8)
        this.addSlot(new CursedPotResultSlot(playerInventory.player, blockEntity, this.inventory, 8, 124, 55));

        // 灵魂源插槽 (9)
        this.addSlot(new SlotItemHandler(this.inventory, 9, 8, 55) {
            @Override
            public boolean mayPlace(ItemStack stack) {
                return stack.getItem() instanceof ITotem || stack.getItem() == ModItems.SOUL_TRANSFER.get();
            }
        });

        // 玩家物品栏
        int startPlayerInvY = startY * 4 + 12;
        for (int row = 0; row < 3; ++row) {
            for (int col = 0; col < 9; ++col) {
                this.addSlot(new Slot(playerInventory, 9 + col + row * 9,
                startX + col * border, startPlayerInvY + row * border));
            }
        }

        // 玩家快捷栏
        for (int col = 0; col < 9; ++col) {
            this.addSlot(new Slot(playerInventory, col, startX + col * border, 142));
        }

        this.addDataSlots(cookingPotDataIn);
    }

    // 【优化1】 网络构造器 - 数据包尺寸明确为 2，与服务端方块实体 `getCount()` 严格对齐
    public CursedIngotPotMenu(int windowId, Inventory playerInventory, FriendlyByteBuf data) {
        this(windowId, playerInventory, getTileEntity(playerInventory, data), new SimpleContainerData(2));
    }

    private static CursedIngotPotBlockEntity getTileEntity(Inventory playerInventory, FriendlyByteBuf data) {
        BlockPos pos = data.readBlockPos();
        BlockEntity be = playerInventory.player.level().getBlockEntity(pos);
        if (be instanceof CursedIngotPotBlockEntity) {
            return (CursedIngotPotBlockEntity) be;
        }
        throw new IllegalStateException("Tile entity is not correct! " + be);
    }

    // 【修复】恢复方块校验：仅距离检查时，方块被破坏后仍能操作"幽灵"库存（物品丢失/疑似复制风险）
    @Override
    public boolean stillValid(Player player) {
        return this.canInteractWithCallable.evaluate((level, pos) ->
                level.getBlockState(pos).is(ModBlocks.CURSED_INGOT_POT.get()) &&
                        player.distanceToSqr(pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5) <= 64.0, true);
    }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        int meal = 6, container = 7, output = 8, soul = 9;
        int startPlayerInv = output + 1;
        int endPlayerInv = startPlayerInv + 36;
        ItemStack copy = ItemStack.EMPTY;
        Slot slot = this.slots.get(index);
        if (slot.hasItem()) {
            ItemStack stack = slot.getItem();
            copy = stack.copy();
            if (index == output) {
                if (!this.moveItemStackTo(stack, startPlayerInv, endPlayerInv, true))
                    return ItemStack.EMPTY;
            } else if (index <= output) {
                if (!this.moveItemStackTo(stack, startPlayerInv, endPlayerInv, false))
                    return ItemStack.EMPTY;
            } else if (index == soul) {
                if (!this.moveItemStackTo(stack, startPlayerInv, endPlayerInv, false))
                    return ItemStack.EMPTY;
            } else {
                boolean validContainer = stack.is(ModTags.Items.SERVING_CONTAINERS) ||
                        stack.is(this.blockEntity.getContainer().getItem());
                if (validContainer && !this.moveItemStackTo(stack, container, container + 1, false))
                    return ItemStack.EMPTY;
                boolean isSoul = stack.getItem() instanceof ITotem || stack.getItem() == ModItems.SOUL_TRANSFER.get();
                if (isSoul && !this.moveItemStackTo(stack, soul, soul + 1, false))
                    return ItemStack.EMPTY;
                if (!this.moveItemStackTo(stack, 0, meal, false)) return ItemStack.EMPTY;
                if (!this.moveItemStackTo(stack, container, output, false)) return ItemStack.EMPTY;
            }
            if (stack.isEmpty()) slot.set(ItemStack.EMPTY);
            else slot.setChanged();
            if (stack.getCount() == copy.getCount()) return ItemStack.EMPTY;
            slot.onTake(player, stack);
        }
        return copy;
    }

    public int getCookProgressionScaled() {
        int i = this.cookingPotData.get(0), j = this.cookingPotData.get(1);
        return j != 0 && i != 0 ? i * 24 / j : 0;
    }

    public boolean isHeated() {
        return this.blockEntity.isHeated();
    }

    @Override
    public void fillCraftSlotsStackedContents(StackedContents helper) {
        for (int i = 0; i < this.inventory.getSlots(); ++i) {
            helper.accountSimpleStack(this.inventory.getStackInSlot(i));
        }
    }

    @Override
    public void clearCraftingContent() {
        for (int i = 0; i < 6; ++i) {
            this.inventory.setStackInSlot(i, ItemStack.EMPTY);
        }
    }

    @Override
    public boolean recipeMatches(RecipeHolder<CookingPotRecipe> recipe) {
        return recipe.value().matches(new RecipeWrapper(this.inventory), this.level);
    }

    @Override
    public int getResultSlotIndex() {
        // 成品显示槽(6)才是食谱书对应用的结果槽（此前写 7 指向容器输入槽）
        return 6;
    }

    @Override
    public int getGridWidth() {
        return 3;
    }

    @Override
    public int getGridHeight() {
        return 2;
    }

    @Override
    public int getSize() {
        return 7;
    }

    @Override
    public RecipeBookType getRecipeBookType() {
        return RecipeBookType.valueOf("FARMERSDELIGHT_COOKING");
    }

    @Override
    public boolean shouldMoveToInventory(int slot) {
        return slot < this.getGridWidth() * this.getGridHeight();
    }
}