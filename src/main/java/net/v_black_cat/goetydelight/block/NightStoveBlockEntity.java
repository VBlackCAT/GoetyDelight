package net.v_black_cat.goetydelight.block;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;
import net.minecraft.world.Container;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.WorldlyContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.AbstractCookingRecipe;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeManager;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LightLayer;
import net.minecraft.world.level.block.AbstractFurnaceBlock;
import net.minecraft.world.level.block.entity.BaseContainerBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.v_black_cat.goetydelight.screen.NightStoveMenu;

import javax.annotation.Nullable;

public class NightStoveBlockEntity extends BaseContainerBlockEntity implements WorldlyContainer {
    // 定义常量 - 移除燃料槽位
    private static final int[] SLOTS_FOR_UP = new int[]{0};
    private static final int[] SLOTS_FOR_DOWN = new int[]{1};
    private static final int[] SLOTS_FOR_SIDES = new int[]{0};

    // 燃烧状态变量 - 移除燃料相关变量
    private int cookingProgress;
    private int cookingTotalTime;

    // 物品槽位 - 减少到2个（输入和输出）
    protected NonNullList<ItemStack> items = NonNullList.withSize(2, ItemStack.EMPTY);

    // 数据访问器 - 移除燃料相关数据
    private final ContainerData dataAccess = new ContainerData() {
        @Override
        public int get(int index) {
            return switch (index) {
                case 0 -> cookingProgress;
                case 1 -> cookingTotalTime;
                default -> 0;
            };
        }

        @Override
        public void set(int index, int value) {
            switch (index) {
                case 0 -> cookingProgress = value;
                case 1 -> cookingTotalTime = value;
            }
        }

        @Override
        public int getCount() {
            return 2;
        }
    };

    private final RecipeManager.CachedCheck<Container, ? extends AbstractCookingRecipe> quickCheck;
    private final RecipeType<? extends AbstractCookingRecipe> recipeType;

    public NightStoveBlockEntity(BlockPos pPos, BlockState pBlockState) {
        super(ModBlockEntities.NIGHT_STOVE_BE.get(), pPos, pBlockState);
        this.recipeType = RecipeType.SMOKING;
        this.quickCheck = RecipeManager.createCheck(this.recipeType);
    }

    @Override
    protected Component getDefaultName() {
        return Component.translatable("container.night_stove");
    }

    @Override
    protected AbstractContainerMenu createMenu(int pContainerId, Inventory pPlayerInventory) {
        return new NightStoveMenu(pContainerId, pPlayerInventory, this, this.dataAccess);
    }

    @Override
    public int getContainerSize() {
        return this.items.size();
    }

    @Override
    public boolean isEmpty() {
        for (ItemStack itemstack : this.items) {
            if (!itemstack.isEmpty()) {
                return false;
            }
        }
        return true;
    }

    @Override
    public ItemStack getItem(int pIndex) {
        return this.items.get(pIndex);
    }

    @Override
    public ItemStack removeItem(int pIndex, int pCount) {
        return ContainerHelper.removeItem(this.items, pIndex, pCount);
    }

    @Override
    public ItemStack removeItemNoUpdate(int pIndex) {
        return ContainerHelper.takeItem(this.items, pIndex);
    }

    @Override
    public void setItem(int pIndex, ItemStack pStack) {
        ItemStack itemstack = this.items.get(pIndex);
        boolean flag = !pStack.isEmpty() && ItemStack.isSameItemSameTags(itemstack, pStack);
        this.items.set(pIndex, pStack);

        if (pStack.getCount() > this.getMaxStackSize()) {
            pStack.setCount(this.getMaxStackSize());
        }

        if (pIndex == 0 && !flag) {
            this.cookingTotalTime = getTotalCookTime();
            this.cookingProgress = 0;
            this.setChanged();
        }
    }

    @Override
    public boolean stillValid(Player pPlayer) {
        if (this.level.getBlockEntity(this.worldPosition) != this) {
            return false;
        } else {
            return pPlayer.distanceToSqr(
                    (double)this.worldPosition.getX() + 0.5D,
                    (double)this.worldPosition.getY() + 0.5D,
                    (double)this.worldPosition.getZ() + 0.5D
            ) <= 64.0D;
        }
    }

    @Override
    public void clearContent() {
        this.items.clear();
    }

    @Override
    public void load(CompoundTag pTag) {
        super.load(pTag);
        this.items = NonNullList.withSize(this.getContainerSize(), ItemStack.EMPTY);
        ContainerHelper.loadAllItems(pTag, this.items);
        this.cookingProgress = pTag.getInt("CookTime");
        this.cookingTotalTime = pTag.getInt("CookTimeTotal");
    }

    @Override
    protected void saveAdditional(CompoundTag pTag) {
        super.saveAdditional(pTag);
        pTag.putInt("CookTime", this.cookingProgress);
        pTag.putInt("CookTimeTotal", this.cookingTotalTime);
        ContainerHelper.saveAllItems(pTag, this.items);
    }

    @Override
    public int[] getSlotsForFace(Direction pSide) {
        if (pSide == Direction.DOWN) {
            return SLOTS_FOR_DOWN;
        } else {
            return pSide == Direction.UP ? SLOTS_FOR_UP : SLOTS_FOR_SIDES;
        }
    }

    @Override
    public boolean canPlaceItemThroughFace(int pIndex, ItemStack pItemStack, @Nullable Direction pDirection) {
        return this.canPlaceItem(pIndex, pItemStack);
    }

    @Override
    public boolean canTakeItemThroughFace(int pIndex, ItemStack pStack, Direction pDirection) {
        return true;
    }

    @Override
    public boolean canPlaceItem(int pIndex, ItemStack pStack) {
        return pIndex == 0; // 只能在输入槽位放置物品
    }

    // 检查是否正在工作
    private boolean isWorking() {
        return this.cookingProgress > 0;
    }

    // 检查是否可以烹饪
    private boolean canBurn(@Nullable Recipe<?> pRecipe) {
        if (this.items.get(0).isEmpty() || pRecipe == null) {
            return false;
        }

        ItemStack result = ((Recipe<Container>) pRecipe).assemble(this, this.level.registryAccess());
        if (result.isEmpty()) {
            return false;
        }

        ItemStack currentResult = this.items.get(1);
        if (currentResult.isEmpty()) {
            return true;
        } else if (!ItemStack.isSameItem(currentResult, result)) {
            return false;
        } else {
            return currentResult.getCount() + result.getCount() <= this.getMaxStackSize() &&
                    currentResult.getCount() + result.getCount() <= currentResult.getMaxStackSize();
        }
    }

    // 执行烹饪
    private boolean burn(@Nullable Recipe<?> pRecipe) {
        if (pRecipe != null && this.canBurn(pRecipe)) {
            ItemStack input = this.items.get(0);
            ItemStack result = ((Recipe<Container>) pRecipe).assemble(this, this.level.registryAccess());
            ItemStack currentResult = this.items.get(1);

            if (currentResult.isEmpty()) {
                this.items.set(1, result.copy());
            } else if (currentResult.is(result.getItem())) {
                currentResult.grow(result.getCount());
            }

            input.shrink(1);
            return true;
        }
        return false;
    }

    // 获取总烹饪时间（考虑昼夜速度）
    private int getTotalCookTime() {
        int baseCookTime = this.quickCheck.getRecipeFor(this, this.level)
                .map(AbstractCookingRecipe::getCookingTime)
                .orElse(200);

        // 根据昼夜调整烹饪速度
        float speedMultiplier = getCookingSpeedMultiplier();
        return (int)(baseCookTime / speedMultiplier);
    }

    // 获取烹饪速度倍率（常态2倍，夜晚4倍）
    private float getCookingSpeedMultiplier() {
        if (level == null) return 2.0f;

        // 获取天空光照等级（0-15）
        int skyLight = level.getBrightness(LightLayer.SKY, worldPosition);
        // 获取方块光照等级（0-15）
        int blockLight = level.getBrightness(LightLayer.BLOCK, worldPosition);

        // 如果天空光照低（夜晚）且没有人工光源，则为夜晚模式
        boolean isNighttime = skyLight <= 10 && blockLight <= 10;

        return isNighttime ? 4.0f : 2.0f;
    }

    // Tick方法
    public static void serverTick(Level level, BlockPos pos, BlockState state, NightStoveBlockEntity blockEntity) {
        boolean isWorkingBefore = blockEntity.isWorking();
        boolean changed = false;

        boolean hasInput = !blockEntity.items.get(0).isEmpty();

        if (hasInput) {
            Recipe<?> recipe = blockEntity.quickCheck.getRecipeFor(blockEntity, level).orElse(null);

            if (blockEntity.canBurn(recipe)) {
                // 根据昼夜调整烹饪速度
                float speedMultiplier = blockEntity.getCookingSpeedMultiplier();
                blockEntity.cookingProgress += speedMultiplier;

                if (blockEntity.cookingProgress >= blockEntity.cookingTotalTime) {
                    blockEntity.cookingProgress = 0;
                    blockEntity.cookingTotalTime = blockEntity.getTotalCookTime();
                    if (blockEntity.burn(recipe)) {
                        // 记录配方使用
                        // blockEntity.setRecipeUsed(recipe);
                    }
                    changed = true;
                }
            } else {
                blockEntity.cookingProgress = 0;
            }
        } else if (blockEntity.cookingProgress > 0) {
            blockEntity.cookingProgress = Mth.clamp(blockEntity.cookingProgress - 2, 0, blockEntity.cookingTotalTime);
        }

        if (isWorkingBefore != blockEntity.isWorking()) {
            changed = true;
            state = state.setValue(AbstractFurnaceBlock.LIT, blockEntity.isWorking());
            level.setBlock(pos, state, 3);
        }

        if (changed) {
            blockEntity.setChanged();
        }
    }
}