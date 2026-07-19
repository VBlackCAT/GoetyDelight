package net.v_black_cat.goetydelight.block;

import com.Polarice3.Goety.api.items.magic.ITotem;
import com.Polarice3.Goety.utils.SEHelper;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.NonNullList;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.WorldlyContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.item.crafting.AbstractCookingRecipe;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.RecipeManager;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.item.crafting.SingleRecipeInput;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.AbstractFurnaceBlock;
import net.minecraft.world.level.block.entity.BaseContainerBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.v_black_cat.goetydelight.init.ModBlockEntities;
import net.v_black_cat.goetydelight.screen.ShadeStoveMenu;

import javax.annotation.Nullable;
import java.util.UUID;

import static com.Polarice3.Goety.common.items.ModItems.SOUL_TRANSFER;

public class ShadeStoveBlockEntity extends BaseContainerBlockEntity implements WorldlyContainer {
    private static final int SOUL_ENERGY_COST = 50;
    private static final int BURN_TIME_GAIN = 100;
    private static final int COOKING_SPEED_MULTIPLIER = 1;
    private static final float FUEL_CONSUMPTION_MULTIPLIER = 1.5f;

    private static final int SLOT_INPUT = 0;
    private static final int SLOT_FUEL = 1;
    private static final int SLOT_RESULT = 2;
    private static final int[] SLOTS_FOR_UP = new int[]{SLOT_INPUT};
    private static final int[] SLOTS_FOR_DOWN = new int[]{SLOT_RESULT, SLOT_FUEL};
    private static final int[] SLOTS_FOR_SIDES = new int[]{SLOT_FUEL};

    private int litTime;
    private int litDuration;
    private int cookingProgress;
    private int cookingTotalTime;

    protected NonNullList<ItemStack> items = NonNullList.withSize(3, ItemStack.EMPTY);

    private final ContainerData dataAccess = new ContainerData() {
        @Override public int get(int index) {
            return switch (index) {
                case 0 -> litTime;
                case 1 -> litDuration;
                case 2 -> cookingProgress;
                case 3 -> cookingTotalTime;
                default -> 0;
            };
        }
        @Override public void set(int index, int value) {
            switch (index) {
                case 0 -> litTime = value;
                case 1 -> litDuration = value;
                case 2 -> cookingProgress = value;
                case 3 -> cookingTotalTime = value;
            }
        }
        @Override public int getCount() { return 4; }
    };

    private final RecipeManager.CachedCheck<SingleRecipeInput, ? extends AbstractCookingRecipe> quickCheck;
    private final RecipeType<? extends AbstractCookingRecipe> recipeType;

    public ShadeStoveBlockEntity(BlockPos pPos, BlockState pBlockState) {
        super(ModBlockEntities.SHADE_STOVE_BE.get(), pPos, pBlockState);
        this.recipeType = RecipeType.SMOKING;
        this.quickCheck = RecipeManager.createCheck(this.recipeType);
    }

    @Override protected Component getDefaultName() {
        return Component.translatable("container.smoker");
    }

    @Override protected AbstractContainerMenu createMenu(int pContainerId, Inventory pPlayerInventory) {
        return new ShadeStoveMenu(pContainerId, pPlayerInventory, this, this.dataAccess);
    }

    @Override public int getContainerSize() { return items.size(); }

    @Override public boolean isEmpty() {
        for (ItemStack s : items) if (!s.isEmpty()) return false;
        return true;
    }

    @Override public ItemStack getItem(int idx) { return items.get(idx); }

    @Override public ItemStack removeItem(int idx, int count) {
        return ContainerHelper.removeItem(items, idx, count);
    }

    @Override public ItemStack removeItemNoUpdate(int idx) {
        return ContainerHelper.takeItem(items, idx);
    }

    @Override public void clearContent() { items.clear(); }

    @Override public void setItem(int idx, ItemStack stack) {
        ItemStack old = items.get(idx);
        boolean same = !stack.isEmpty() && ItemStack.isSameItemSameComponents(old, stack);
        items.set(idx, stack);
        if (stack.getCount() > getMaxStackSize()) stack.setCount(getMaxStackSize());
        if (idx == SLOT_INPUT && !same) {
            cookingTotalTime = getTotalCookTime();
            cookingProgress = 0;
            setChanged();
        }
    }

    @Override public boolean stillValid(Player player) {
        if (level == null || level.getBlockEntity(worldPosition) != this) return false;
        return player.distanceToSqr(worldPosition.getX() + 0.5, worldPosition.getY() + 0.5, worldPosition.getZ() + 0.5) <= 64.0;
    }

    @Override public NonNullList<ItemStack> getItems() { return items; }

    @Override public void setItems(NonNullList<ItemStack> pItems) { items = pItems; }

    @Override protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        tag.putInt("BurnTime", litTime);
        tag.putInt("CookTime", cookingProgress);
        tag.putInt("CookTimeTotal", cookingTotalTime);
        ContainerHelper.saveAllItems(tag, items, registries);
    }

    @Override protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        items = NonNullList.withSize(getContainerSize(), ItemStack.EMPTY);
        ContainerHelper.loadAllItems(tag, items, registries);
        litTime = tag.getInt("BurnTime");
        cookingProgress = tag.getInt("CookTime");
        cookingTotalTime = tag.getInt("CookTimeTotal");
        litDuration = getBurnDuration(items.get(SLOT_FUEL));
    }

    // ===== 数据同步 =====
    @Override
    public CompoundTag getUpdateTag(HolderLookup.Provider registries) {
        CompoundTag tag = super.getUpdateTag(registries);
        tag.putInt("BurnTime", this.litTime);
        tag.putInt("CookTime", this.cookingProgress);
        tag.putInt("CookTimeTotal", this.cookingTotalTime);
        return tag;
    }

    @Override
    public void handleUpdateTag(CompoundTag tag, HolderLookup.Provider registries) {
        super.handleUpdateTag(tag, registries);
        this.litTime = tag.getInt("BurnTime");
        this.cookingProgress = tag.getInt("CookTime");
        this.cookingTotalTime = tag.getInt("CookTimeTotal");
    }

    @Override public int[] getSlotsForFace(Direction side) {
        if (side == Direction.DOWN) return SLOTS_FOR_DOWN;
        return side == Direction.UP ? SLOTS_FOR_UP : SLOTS_FOR_SIDES;
    }

    @Override public boolean canPlaceItemThroughFace(int idx, ItemStack stack, @Nullable Direction dir) {
        return canPlaceItem(idx, stack);
    }

    @Override public boolean canTakeItemThroughFace(int idx, ItemStack stack, Direction dir) {
        if (dir == Direction.DOWN && idx == SLOT_FUEL) {
            return stack.is(net.minecraft.world.item.Items.WATER_BUCKET) || stack.is(net.minecraft.world.item.Items.BUCKET);
        }
        return true;
    }

    @Override public boolean canPlaceItem(int idx, ItemStack stack) {
        if (idx == SLOT_RESULT) return false;
        if (idx != SLOT_FUEL) return true;
        ItemStack current = items.get(SLOT_FUEL);
        return isFuel(stack) || (stack.is(net.minecraft.world.item.Items.BUCKET) && !current.is(net.minecraft.world.item.Items.BUCKET));
    }

    // ===== 灵魂能量消耗 =====
    private boolean consumeSoulEnergy(int amount) {
        ItemStack soulSource = items.get(SLOT_FUEL);
        if (soulSource.isEmpty()) return false;
        if (level == null) return false;

        CustomData customData = soulSource.get(DataComponents.CUSTOM_DATA);
        CompoundTag tag = customData != null ? customData.copyTag() : new CompoundTag();

        if (soulSource.getItem() == SOUL_TRANSFER.get()) {
            if (tag.contains("owner")) {
                UUID ownerUuid = tag.getUUID("owner");
                Player owner = level.getPlayerByUUID(ownerUuid);
                if (owner == null) return false;
                if (!SEHelper.getSEActive(owner) || SEHelper.getSESouls(owner) < amount) return false;

                SEHelper.decreaseSESouls(owner, amount);
                SEHelper.sendSEUpdatePacket(owner);
                spawnSoulParticles();
                return true;
            }
        } else if (soulSource.getItem() instanceof ITotem) {
            int souls = tag.getInt("Souls");
            if (souls >= amount) {
                tag.putInt("Souls", souls - amount);
                soulSource.set(DataComponents.CUSTOM_DATA, CustomData.of(tag));
                spawnSoulParticles();
                return true;
            }
        }
        return false;
    }

    private void spawnSoulParticles() {
        if (level instanceof ServerLevel serverLevel) {
            serverLevel.sendParticles(ParticleTypes.SOUL,
                    worldPosition.getX() + 0.5, worldPosition.getY() + 1.0, worldPosition.getZ() + 0.5,
                    5, 0.2, 0.0, 0.2, 0.05);
        }
    }

    public static boolean isFuel(ItemStack stack) {
        return stack.getItem() instanceof ITotem ||
                stack.getItem() == SOUL_TRANSFER.get() ||
                stack.getBurnTime(RecipeType.SMOKING) > 0;
    }

    protected int getBurnDuration(ItemStack fuel) {
        if (fuel.isEmpty()) return 0;
        if (fuel.getItem() instanceof ITotem || fuel.getItem() == SOUL_TRANSFER.get()) return 0;
        return (int) (fuel.getBurnTime(recipeType) / FUEL_CONSUMPTION_MULTIPLIER);
    }

    private boolean isLit() { return litTime > 0; }

    private boolean canBurn(@Nullable RecipeHolder<? extends AbstractCookingRecipe> holder) {
        if (items.get(SLOT_INPUT).isEmpty() || holder == null || level == null) return false;
        SingleRecipeInput input = new SingleRecipeInput(items.get(SLOT_INPUT));
        ItemStack result = holder.value().assemble(input, level.registryAccess());
        if (result.isEmpty()) return false;
        ItemStack current = items.get(SLOT_RESULT);
        if (current.isEmpty()) return true;
        if (!ItemStack.isSameItem(current, result)) return false;
        return current.getCount() + result.getCount() <= getMaxStackSize() &&
                current.getCount() + result.getCount() <= current.getMaxStackSize();
    }

    private boolean burn(@Nullable RecipeHolder<? extends AbstractCookingRecipe> holder) {
        if (holder == null || !canBurn(holder) || level == null) return false;
        SingleRecipeInput input = new SingleRecipeInput(items.get(SLOT_INPUT));
        ItemStack result = holder.value().assemble(input, level.registryAccess());
        ItemStack current = items.get(SLOT_RESULT);
        if (current.isEmpty()) items.set(SLOT_RESULT, result.copy());
        else if (current.is(result.getItem())) current.grow(result.getCount());

        if (input.getItem(0).is(net.minecraft.world.level.block.Blocks.WET_SPONGE.asItem()) &&
                !items.get(SLOT_FUEL).isEmpty() &&
                items.get(SLOT_FUEL).is(net.minecraft.world.item.Items.BUCKET)) {
            items.set(SLOT_FUEL, new ItemStack(net.minecraft.world.item.Items.WATER_BUCKET));
        }
        items.get(SLOT_INPUT).shrink(1);
        return true;
    }

    private int getTotalCookTime() {
        if (level == null) return 200;
        SingleRecipeInput input = new SingleRecipeInput(items.get(SLOT_INPUT));
        int base = quickCheck.getRecipeFor(input, level)
                .map(h -> h.value().getCookingTime()).orElse(200);
        return Math.max(1, base / COOKING_SPEED_MULTIPLIER);
    }

    // ★★★ 修改后的 serverTick ★★★
    public static void serverTick(Level level, BlockPos pos, BlockState state, ShadeStoveBlockEntity be) {
        boolean wasLit = be.isLit();
        boolean changed = false;

        if (be.isLit()) {
            be.litTime = Math.max(0, be.litTime - (int) be.FUEL_CONSUMPTION_MULTIPLIER);
        }

        ItemStack fuel = be.items.get(SLOT_FUEL);
        boolean hasFuel = !fuel.isEmpty() && isFuel(fuel);
        boolean hasInput = !be.items.get(SLOT_INPUT).isEmpty();

        if (be.isLit() || (hasFuel && hasInput)) {
            RecipeHolder<? extends AbstractCookingRecipe> recipe = null;
            if (hasInput) {
                SingleRecipeInput input = new SingleRecipeInput(be.items.get(SLOT_INPUT));
                recipe = be.quickCheck.getRecipeFor(input, level).orElse(null);
            }

            // ★★★ 灵魂能量点火：只有存在有效配方且可烹饪时才消耗 ★★★
            if (!be.isLit() && hasInput && recipe != null && be.canBurn(recipe) && be.consumeSoulEnergy(SOUL_ENERGY_COST)) {
                be.litTime = (int)(BURN_TIME_GAIN / be.FUEL_CONSUMPTION_MULTIPLIER);
                be.litDuration = be.litTime;
                changed = true;
                be.cookingTotalTime = be.getTotalCookTime();
            }

            // ★★★ 普通燃料点火（保留兼容，但阴影炉灶不使用） ★★★
            else if (!be.isLit() && be.canBurn(recipe)) {
                be.litTime = be.getBurnDuration(fuel);
                be.litDuration = be.litTime;
                if (be.isLit()) {
                    changed = true;
                    be.cookingTotalTime = be.getTotalCookTime();
                    if (fuel.hasCraftingRemainingItem()) {
                        be.items.set(SLOT_FUEL, fuel.getCraftingRemainingItem());
                    } else if (hasFuel) {
                        fuel.shrink(1);
                        if (fuel.isEmpty()) {
                            be.items.set(SLOT_FUEL, fuel.getCraftingRemainingItem());
                        }
                    }
                }
            }

            // 烹饪进度
            if (be.isLit() && be.canBurn(recipe)) {
                be.cookingProgress += 1;
                changed = true;
                if (be.cookingProgress >= be.cookingTotalTime) {
                    be.cookingProgress = 0;
                    be.cookingTotalTime = be.getTotalCookTime();
                    if (be.burn(recipe)) changed = true;
                }
            } else {
                if (be.cookingProgress != 0) {
                    be.cookingProgress = 0;
                    changed = true;
                }
            }
        } else if (!be.isLit() && be.cookingProgress > 0) {
            be.cookingProgress = Mth.clamp(be.cookingProgress - 2, 0, be.cookingTotalTime);
            changed = true;
        }

        if (wasLit != be.isLit()) {
            changed = true;
            state = state.setValue(AbstractFurnaceBlock.LIT, be.isLit());
            level.setBlock(pos, state, 3);
        }

        if (changed) {
            be.setChanged();
            level.sendBlockUpdated(pos, state, state, 3);
        }
    }
}