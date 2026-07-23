package net.v_black_cat.goetydelight.block;

import com.Polarice3.Goety.api.entities.IOwned;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.WorldlyContainer;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.AbstractCookingRecipe;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.RecipeManager;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.item.crafting.SingleRecipeInput;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LightLayer;
import net.minecraft.world.level.block.entity.BaseContainerBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.v_black_cat.goetydelight.init.ModBlockEntities;
import net.v_black_cat.goetydelight.screen.NightStoveMenu;

import javax.annotation.Nullable;
import java.util.List;

public class NightStoveBlockEntity extends BaseContainerBlockEntity implements WorldlyContainer {
    private static final int[] SLOTS_FOR_UP = {0};
    private static final int[] SLOTS_FOR_DOWN = {1};
    private static final int[] SLOTS_FOR_SIDES = {0};

    private int cookingProgress;
    private int cookingTotalTime;
    private int effectTimer;

    protected NonNullList<ItemStack> items = NonNullList.withSize(2, ItemStack.EMPTY);

    private final ContainerData dataAccess = new ContainerData() {
        @Override
        public int get(int index) {
            return switch (index) {
                case 0 -> cookingProgress;
                case 1 -> cookingTotalTime;
                case 2 -> effectTimer;
                default -> 0;
            };
        }

        @Override
        public void set(int index, int value) {
            switch (index) {
                case 0 -> cookingProgress = value;
                case 1 -> cookingTotalTime = value;
                case 2 -> effectTimer = value;
            }
        }

        @Override
        public int getCount() {
            return 3;
        }
    };

    private final RecipeManager.CachedCheck<
            SingleRecipeInput, ? extends AbstractCookingRecipe> quickCheck;
    private final RecipeType<? extends AbstractCookingRecipe> recipeType;

    public NightStoveBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.NIGHT_STOVE_BE.get(), pos, state);
        this.recipeType = RecipeType.SMOKING;
        this.quickCheck = RecipeManager.createCheck(this.recipeType);
    }

    @Override
    protected Component getDefaultName() {
        return Component.translatable("goetydelight.container.night_stove");
    }

    @Override
    protected AbstractContainerMenu createMenu(int id, Inventory inv) {
        return new NightStoveMenu(id, inv, this, this.dataAccess);
    }

    @Override
    public int getContainerSize() {
        return items.size();
    }

    @Override
    public boolean isEmpty() {
        for (ItemStack s : items) if (!s.isEmpty()) return false;
        return true;
    }

    @Override
    public ItemStack getItem(int index) {
        return items.get(index);
    }

    @Override
    public ItemStack removeItem(int index, int count) {
        return ContainerHelper.removeItem(items, index, count);
    }

    @Override
    public ItemStack removeItemNoUpdate(int index) {
        return ContainerHelper.takeItem(items, index);
    }

    @Override
    public void clearContent() {
        items.clear();
    }

    @Override
    public void setItem(int index, ItemStack stack) {
        ItemStack old = items.get(index);
        boolean same = !stack.isEmpty() && ItemStack.isSameItemSameComponents(old, stack);
        items.set(index, stack);
        if (stack.getCount() > getMaxStackSize()) stack.setCount(getMaxStackSize());
        if (index == 0 && !same) {
            cookingTotalTime = getTotalCookTime();
            cookingProgress = 0;
            setChanged();
        }
    }

    @Override
    public boolean stillValid(Player player) {
        if (level == null || level.getBlockEntity(worldPosition) != this) return false;
        return player.distanceToSqr(worldPosition.getX() + 0.5, worldPosition.getY() + 0.5, worldPosition.getZ() + 0.5) <= 64.0;
    }

    // ========== BaseContainerBlockEntity 抽象方法实现 ==========
    @Override
    public NonNullList<ItemStack> getItems() {
        return this.items;
    }

    @Override
    public void setItems(NonNullList<ItemStack> items) {
        this.items = items;
    }

    // ========== 持久化（正确签名） ==========
    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        tag.putInt("CookTime", cookingProgress);
        tag.putInt("CookTimeTotal", cookingTotalTime);
        tag.putInt("EffectTimer", effectTimer);
        ContainerHelper.saveAllItems(tag, this.items, registries);
    }

    // 注意：不重写 load，而使用 loadAdditional（由父类在 load 中调用）
    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        this.items = NonNullList.withSize(getContainerSize(), ItemStack.EMPTY);
        ContainerHelper.loadAllItems(tag, this.items, registries);
        this.cookingProgress = tag.getInt("CookTime");
        this.cookingTotalTime = tag.getInt("CookTimeTotal");
        this.effectTimer = tag.getInt("EffectTimer");
    }

    // ========== 容器槽位接口 ==========
    @Override
    public int[] getSlotsForFace(Direction side) {
        if (side == Direction.DOWN) return SLOTS_FOR_DOWN;
        return side == Direction.UP ? SLOTS_FOR_UP : SLOTS_FOR_SIDES;
    }

    @Override
    public boolean canPlaceItemThroughFace(int index, ItemStack stack, @Nullable Direction dir) {
        return canPlaceItem(index, stack);
    }

    @Override
    public boolean canTakeItemThroughFace(int index, ItemStack stack, Direction dir) {
        return true;
    }

    @Override
    public boolean canPlaceItem(int index, ItemStack stack) {
        return index == 0;
    }

    // ========== 烹饪逻辑 ==========
    private boolean isWorking() {
        return cookingProgress > 0;
    }

    private boolean canBurn(@Nullable RecipeHolder<? extends AbstractCookingRecipe> holder) {
        if (items.get(0).isEmpty() || holder == null || level == null) return false;
        SingleRecipeInput input = new SingleRecipeInput(items.get(0));
        ItemStack result = holder.value().assemble(input, level.registryAccess());
        if (result.isEmpty()) return false;
        ItemStack current = items.get(1);
        if (current.isEmpty()) return true;
        if (!ItemStack.isSameItem(current, result)) return false;
        int total = current.getCount() + result.getCount();
        return total <= getMaxStackSize() && total <= current.getMaxStackSize();
    }

    private boolean burn(@Nullable RecipeHolder<? extends AbstractCookingRecipe> holder) {
        if (holder == null || !canBurn(holder) || level == null) return false;
        SingleRecipeInput input = new SingleRecipeInput(items.get(0));
        ItemStack result = holder.value().assemble(input, level.registryAccess());
        ItemStack current = items.get(1);
        if (current.isEmpty()) items.set(1, result.copy());
        else if (ItemStack.isSameItem(current, result)) current.grow(result.getCount());
        items.get(0).shrink(1);
        return true;
    }

    private int getTotalCookTime() {
        if (level == null) return 200;
        ItemStack input = items.get(0);
        if (input.isEmpty()) return 200;
        SingleRecipeInput recipeInput = new SingleRecipeInput(input);
        int base = quickCheck.getRecipeFor(recipeInput, level)
                .map(h -> h.value().getCookingTime())
                .orElse(200);
        float speed = getCookingSpeedMultiplier();
        return (int) (base / speed);
    }

    private float getCookingSpeedMultiplier() {
        if (level == null) return 2.0f;
        int sky = level.getBrightness(LightLayer.SKY, worldPosition);
        int block = level.getBrightness(LightLayer.BLOCK, worldPosition);
        return (sky <= 10 && block <= 10) ? 4.0f : 2.0f;
    }

    // ========== 范围效果 ==========
    private void applyEffectsToNearbyEntities() {
        if (level == null || level.isClientSide) return;
        AABB area = new AABB(
        worldPosition.getX() - 32, worldPosition.getY() - 32, worldPosition.getZ() - 32,
        worldPosition.getX() + 32, worldPosition.getY() + 32, worldPosition.getZ() + 32
        );
        List<Player> players = level.getEntitiesOfClass(Player.class, area);
        for (Player player : players) {
            player.addEffect(new MobEffectInstance(MobEffects.FIRE_RESISTANCE, 200, 0, false, false));
            player.addEffect(new MobEffectInstance(MobEffects.REGENERATION, 200, 0, false, false));
            List<LivingEntity> allies = level.getEntitiesOfClass(LivingEntity.class, area,
                    e -> e != player && isAlliedTo(player, e));
            for (LivingEntity ally : allies) {
                ally.addEffect(new MobEffectInstance(MobEffects.FIRE_RESISTANCE, 200, 0, false, false));
                ally.addEffect(new MobEffectInstance(MobEffects.REGENERATION, 200, 0, false, false));
            }
        }
    }

    private boolean isAlliedTo(Player player, LivingEntity entity) {
        if (entity instanceof IOwned owned && owned.getTrueOwner() == player) return true;
        return player.getTeam() != null && entity.getTeam() == player.getTeam();
    }

    // ========== 服务端 Tick ==========
    public static void serverTick(Level level, BlockPos pos, BlockState state, NightStoveBlockEntity be) {
        boolean wasWorking = be.isWorking();
        boolean changed = false;

        if (!be.items.get(0).isEmpty()) {
            SingleRecipeInput input = new SingleRecipeInput(be.items.get(0));
            RecipeHolder<
                    ? extends
                            AbstractCookingRecipe> recipe = be.quickCheck.getRecipeFor(input, level).orElse(null);

            if (be.canBurn(recipe)) {
                float speed = be.getCookingSpeedMultiplier();
                be.cookingProgress += speed;
                if (be.cookingProgress >= be.cookingTotalTime) {
                    be.cookingProgress = 0;
                    be.cookingTotalTime = be.getTotalCookTime();
                    if (be.burn(recipe)) changed = true;
                }
            } else {
                be.cookingProgress = 0;
            }
        } else if (be.cookingProgress > 0) {
            be.cookingProgress = Mth.clamp(be.cookingProgress - 2, 0, be.cookingTotalTime);
        }

        be.effectTimer++;
        if (be.effectTimer >= 100) {
            be.effectTimer = 0;
            be.applyEffectsToNearbyEntities();
        }

        if (wasWorking != be.isWorking()) {
            changed = true;
            // 如需更新 LIT 状态，取消下面注释（需在 Block 类中定义 LIT 属性，父类已有）
            // state = state.setValue(AbstractFurnaceBlock.LIT, be.isWorking());
            // level.setBlock(pos, state, 3);
        }

        if (changed) be.setChanged();
    }
}