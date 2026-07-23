package net.v_black_cat.goetydelight.screen;

import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.items.IItemHandler;
import net.neoforged.neoforge.items.SlotItemHandler;
import net.v_black_cat.goetydelight.block.CursedIngotPotBlockEntity;

import javax.annotation.Nonnull;
import javax.annotation.ParametersAreNonnullByDefault;

@ParametersAreNonnullByDefault
public class CursedPotResultSlot extends SlotItemHandler {
    public final CursedIngotPotBlockEntity tileEntity;
    private final Player player;
    private int removeCount;

    public CursedPotResultSlot(Player player, CursedIngotPotBlockEntity tile, IItemHandler inventoryIn, int index, int xPosition, int yPosition) {
        super(inventoryIn, index, xPosition, yPosition);
        this.tileEntity = tile;
        this.player = player;
    }

    @Override
    public boolean mayPlace(ItemStack stack) {
        return false;
    }

    @Override
    @Nonnull
    public ItemStack remove(int amount) {
        if (this.hasItem()) {
            this.removeCount += Math.min(amount, this.getItem().getCount());
        }
        return super.remove(amount);
    }

    @Override
    public void onTake(Player thePlayer, ItemStack stack) {
        this.checkTakeAchievements(stack);
        super.onTake(thePlayer, stack);
    }

    @Override
    protected void onQuickCraft(ItemStack stack, int amount) {
        this.removeCount += amount;
        this.checkTakeAchievements(stack);
    }

    protected void checkTakeAchievements(ItemStack stack) {
        stack.onCraftedBy(this.player.level(), this.player, this.removeCount);
        if (!this.player.level().isClientSide) {
            // 临时注释掉 awardUsedRecipes，待 blockEntity 实现此方法
            // this.tileEntity.awardUsedRecipes(this.player, this.tileEntity.getDroppableInventory());
            // 或者调用现有方法
            this.tileEntity.getUsedRecipesAndPopExperience(this.player.level(), this.player.position());
        }
        this.removeCount = 0;
    }
}