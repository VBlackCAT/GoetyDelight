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
        // 【修复】移除 getUsedRecipesAndPopExperience 调用：它按 usedRecipeTracker 发放经验但不清空，
        // 每次取走输出都会重复发放全部累计经验（经验刷取漏洞）。经验仅在方块破坏时统一发放一次。
        this.removeCount = 0;
    }
}