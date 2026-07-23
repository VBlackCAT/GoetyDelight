package net.v_black_cat.goetydelight.block;

import com.Polarice3.Goety.api.items.magic.ITotem;
import com.Polarice3.Goety.utils.SEHelper;
import com.google.common.collect.Lists;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.NonNullList;
import net.minecraft.core.component.DataComponentMap;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.Nameable;
import net.minecraft.world.entity.ExperienceOrb;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.RecipeManager;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.items.ItemStackHandler;
import net.neoforged.neoforge.items.wrapper.RecipeWrapper;
import net.v_black_cat.goetydelight.init.ModBlockEntities;
import net.v_black_cat.goetydelight.screen.CursedIngotPotMenu;
import net.v_black_cat.goetydelight.util.TextUtils;
import vectorwing.farmersdelight.common.block.CookingPotBlock;
import vectorwing.farmersdelight.common.block.entity.HeatableBlockEntity;
import vectorwing.farmersdelight.common.block.entity.SyncedBlockEntity;
import vectorwing.farmersdelight.common.crafting.CookingPotRecipe;
import vectorwing.farmersdelight.common.item.component.ItemStackWrapper;
import vectorwing.farmersdelight.common.registry.ModDataComponents;
import vectorwing.farmersdelight.common.registry.ModParticleTypes;
import vectorwing.farmersdelight.common.registry.ModRecipeTypes;
import vectorwing.farmersdelight.common.tag.ModTags;
import vectorwing.farmersdelight.common.utility.ItemUtils;

import javax.annotation.Nullable;
import java.util.*;

import static com.Polarice3.Goety.common.items.ModItems.SOUL_TRANSFER;

public class CursedIngotPotBlockEntity extends SyncedBlockEntity
        implements MenuProvider, HeatableBlockEntity, Nameable {
    public static final int MEAL_DISPLAY_SLOT = 6;
    public static final int CONTAINER_SLOT = 7;
    public static final int OUTPUT_SLOT = 8;
    public static final int SOUL_SOURCE_SLOT = 9;
    public static final int INVENTORY_SIZE = 10;
    public static final Map<Item, Item> INGREDIENT_REMAINDER_OVERRIDES;

    private final ItemStackHandler inventory = new ItemStackHandler(INVENTORY_SIZE);
    private float cookTime;
    private int cookTimeTotal;
    private ItemStack mealContainerStack = ItemStack.EMPTY;
    private Component customName;
    protected final ContainerData cookingPotData;
    private final Object2IntOpenHashMap<ResourceLocation> usedRecipeTracker = new Object2IntOpenHashMap<>();

    public CursedIngotPotBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.CURSED_INGOT_POT_BE.get(), pos, state);
        this.cookingPotData = new ContainerData() {
            @Override
            public int get(int index) {
                return switch (index) {
                    case 0 -> (int) CursedIngotPotBlockEntity.this.cookTime;
                    case 1 -> CursedIngotPotBlockEntity.this.cookTimeTotal;
                    default -> 0;
                };
            }

            @Override
            public void set(int index, int value) {
                switch (index) {
                    case 0 -> CursedIngotPotBlockEntity.this.cookTime = value;
                    case 1 -> CursedIngotPotBlockEntity.this.cookTimeTotal = value;
                }
            }

            @Override
            public int getCount() {
                return 2;
            }
        };
    }

    @Override
    protected void applyImplicitComponents(BlockEntity.DataComponentInput componentInput) {
        super.applyImplicitComponents(componentInput);
        this.customName = componentInput.get(DataComponents.CUSTOM_NAME);
        ItemStackWrapper mealWrapper = componentInput.get(ModDataComponents.MEAL.get());
        if (mealWrapper != null) {
            this.inventory.setStackInSlot(MEAL_DISPLAY_SLOT, mealWrapper.getStack());
        }
        ItemStackWrapper containerWrapper = componentInput.get(ModDataComponents.CONTAINER.get());
        if (containerWrapper != null) {
            this.mealContainerStack = containerWrapper.getStack();
        }
    }

    @Override
    protected void collectImplicitComponents(DataComponentMap.Builder components) {
        super.collectImplicitComponents(components);
        ItemStack meal = this.getMeal();
        if (!meal.isEmpty()) {
            components.set(ModDataComponents.MEAL.get(), new ItemStackWrapper(meal));
        }
        if (!this.mealContainerStack.isEmpty()) {
            components.set(ModDataComponents.CONTAINER.get(), new ItemStackWrapper(this.mealContainerStack));
        }
        if (this.customName != null) {
            components.set(DataComponents.CUSTOM_NAME, this.customName);
        }
    }

    @Override
    protected void loadAdditional(CompoundTag compound, HolderLookup.Provider registries) {
        super.loadAdditional(compound, registries);
        if (compound.contains("Inventory")) {
            this.inventory.deserializeNBT(registries, compound.getCompound("Inventory"));
        }
        this.cookTime = compound.getFloat("CookTime");
        this.cookTimeTotal = compound.getInt("CookTimeTotal");
        if (compound.contains("Container")) {
            ItemStack.parse(registries, compound.getCompound("Container")).ifPresent(stack -> this.mealContainerStack = stack);
        }
        if (compound.contains("CustomName", 8)) {
            this.customName = Component.Serializer.fromJson(compound.getString("CustomName"), registries);
        }
        if (compound.contains("RecipesUsed")) {
            CompoundTag recipesTag = compound.getCompound("RecipesUsed");
            for (String key : recipesTag.getAllKeys()) {
                this.usedRecipeTracker.put(ResourceLocation.parse(key), recipesTag.getInt(key));
            }
        }
    }

    @Override
    protected void saveAdditional(CompoundTag compound, HolderLookup.Provider registries) {
        super.saveAdditional(compound, registries);
        compound.putFloat("CookTime", this.cookTime);
        compound.putInt("CookTimeTotal", this.cookTimeTotal);
        if (!this.mealContainerStack.isEmpty()) {
            compound.put("Container", this.mealContainerStack.save(registries));
        }
        compound.put("Inventory", this.inventory.serializeNBT(registries));
        if (this.customName != null) {
            compound.putString("CustomName", Component.Serializer.toJson(this.customName, registries));
        }
        CompoundTag recipesTag = new CompoundTag();
        this.usedRecipeTracker.forEach((id, count) -> recipesTag.putInt(id.toString(), count));
        compound.put("RecipesUsed", recipesTag);
    }

    public ItemStack getContainer() {
        return this.mealContainerStack;
    }

    public CompoundTag writeMeal(CompoundTag compound) {
        if (getMeal().isEmpty()) return compound;
        ItemStackHandler drops = new ItemStackHandler(INVENTORY_SIZE);
        for (int i = 0; i < INVENTORY_SIZE; i++) {
            if (i == MEAL_DISPLAY_SLOT) {
                drops.setStackInSlot(i, this.inventory.getStackInSlot(i));
            } else {
                drops.setStackInSlot(i, ItemStack.EMPTY);
            }
        }
        if (this.customName != null) {
            compound.putString("CustomName", Component.Serializer.toJson(this.customName, null));
        }
        if (!this.mealContainerStack.isEmpty()) {
            compound.put("Container", this.mealContainerStack.save(null));
        }
        compound.put("Inventory", drops.serializeNBT(null));
        return compound;
    }

    public boolean hasSoulEnergy() {
        ItemStack soulSource = this.inventory.getStackInSlot(SOUL_SOURCE_SLOT);
        if (soulSource.isEmpty()) return false;
        var customData = soulSource.get(DataComponents.CUSTOM_DATA);
        CompoundTag tag = customData != null ? customData.copyTag() : null;
        if (soulSource.getItem() == SOUL_TRANSFER.get()) {
            if (tag != null && tag.contains("owner")) {
                UUID ownerUuid = tag.getUUID("owner");
                Player owner = this.level != null ? this.level.getPlayerByUUID(ownerUuid) : null;
                return owner != null && SEHelper.getSEActive(owner) && SEHelper.getSESouls(owner) > 0;
            }
        } else if (soulSource.getItem() instanceof ITotem) {
            if (tag != null && tag.contains("Souls")) {
                return tag.getInt("Souls") > 0;
            }
        }
        return false;
    }

    private int getRemainingSoulEnergy() {
        ItemStack soulSource = this.inventory.getStackInSlot(SOUL_SOURCE_SLOT);
        if (soulSource.isEmpty()) return 0;
        var customData = soulSource.get(DataComponents.CUSTOM_DATA);
        CompoundTag tag = customData != null ? customData.copyTag() : null;
        if (soulSource.getItem() == SOUL_TRANSFER.get()) {
            if (tag != null && tag.contains("owner")) {
                UUID ownerUuid = tag.getUUID("owner");
                Player owner = this.level != null ? this.level.getPlayerByUUID(ownerUuid) : null;
                if (owner != null && SEHelper.getSEActive(owner)) {
                    return (int) SEHelper.getSESouls(owner);
                }
            }
        } else if (soulSource.getItem() instanceof ITotem) {
            if (tag != null && tag.contains("Souls")) {
                return tag.getInt("Souls");
            }
        }
        return 0;
    }

    private boolean consumeSoulEnergy(int amount) {
        ItemStack soulSource = this.inventory.getStackInSlot(SOUL_SOURCE_SLOT);
        if (soulSource.isEmpty()) return false;
        if (this.level == null) return false;
        var customData = soulSource.get(DataComponents.CUSTOM_DATA);
        CompoundTag tag = customData != null ? customData.copyTag() : null;
        if (soulSource.getItem() == SOUL_TRANSFER.get()) {
            if (tag != null && tag.contains("owner")) {
                UUID ownerUuid = tag.getUUID("owner");
                Player owner = this.level.getPlayerByUUID(ownerUuid);
                if (owner != null && SEHelper.getSEActive(owner) && SEHelper.getSESouls(owner) >= amount) {
                    SEHelper.decreaseSESouls(owner, amount);
                    SEHelper.sendSEUpdatePacket(owner);
                    spawnSoulParticles();
                    return true;
                }
            }
        } else if (soulSource.getItem() instanceof ITotem) {
            if (tag != null && tag.contains("Souls")) {
                int souls = tag.getInt("Souls");
                if (souls >= amount) {
                    tag.putInt("Souls", souls - amount);
                    soulSource.set(DataComponents.CUSTOM_DATA, net.minecraft.world.item.component.CustomData.of(tag));
                    spawnSoulParticles();
                    return true;
                }
            }
        }
        return false;
    }

    private void spawnSoulParticles() {
        if (this.level instanceof ServerLevel serverLevel) {
            BlockPos pos = this.getBlockPos();
            serverLevel.sendParticles(ParticleTypes.SOUL,
                    pos.getX() + 0.5, pos.getY() + 1.0, pos.getZ() + 0.5,
                    5, 0.2, 0.0, 0.2, 0.05);
        }
    }

    // ===================== 烹饪逻辑 =====================
    public static void cookingTick(Level level, BlockPos pos, BlockState state, CursedIngotPotBlockEntity cookingPot) {
        boolean isHeated = cookingPot.isHeated(level, pos);
        boolean hasSoulEnergy = cookingPot.hasSoulEnergy();
        boolean didInventoryChange = false;

        boolean canCook = false;
        float speedMultiplier = 1.0f;

        int remainingSoul = cookingPot.getRemainingSoulEnergy();
        boolean soulEnoughForCooking = true;

        if (hasSoulEnergy && cookingPot.hasInput()) {
            RecipeWrapper wrapper = new RecipeWrapper(cookingPot.inventory);
            RecipeManager recipeManager = level.getRecipeManager();
            var recipeHolder = recipeManager.getRecipeFor(ModRecipeTypes.COOKING.get(), wrapper, level).orElse(null);
            if (recipeHolder != null && cookingPot.canCook(recipeHolder.value())) {
                ItemStack resultStack = recipeHolder.value().getResultItem(level.registryAccess());
                int estimatedCost = cookingPot.calculateSoulCost(resultStack);
                soulEnoughForCooking = remainingSoul >= estimatedCost;
            } else {
                soulEnoughForCooking = true;
            }
        }

        //速度决策（结合灵魂能量预判）
        if (isHeated && hasSoulEnergy && soulEnoughForCooking) {
            canCook = true;
            speedMultiplier = 2.0f;//两者皆有
        } else if (isHeated) {
            canCook = true;
            speedMultiplier = 1.0f;//仅热源
        } else if (hasSoulEnergy && soulEnoughForCooking) {
            canCook = true;
            speedMultiplier = 1.2f;   //仅灵魂能量
        }
        //两者都没有
        if (!isHeated && !(hasSoulEnergy && soulEnoughForCooking)) {
            canCook = false;
            speedMultiplier = 0f;
        }

        if (canCook && cookingPot.hasInput()) {
            RecipeWrapper wrapper = new RecipeWrapper(cookingPot.inventory);
            RecipeManager recipeManager = level.getRecipeManager();
            var recipeHolder = recipeManager.getRecipeFor(ModRecipeTypes.COOKING.get(), wrapper, level).orElse(null);

            if (recipeHolder != null && cookingPot.canCook(recipeHolder.value())) {
                cookingPot.cookTime += 1.0f * speedMultiplier;
                if (cookingPot.cookTime >= cookingPot.cookTimeTotal) {
                    didInventoryChange = cookingPot.processCooking(recipeHolder);
                }
            } else {
                cookingPot.cookTime = Mth.clamp(cookingPot.cookTime - 1.0f, 0.0f, cookingPot.cookTimeTotal);
            }
        } else if (cookingPot.cookTime > 0) {
            cookingPot.cookTime = Mth.clamp(cookingPot.cookTime - 1.0f, 0.0f, cookingPot.cookTimeTotal);
        }

        // 自动输出与容器处理
        ItemStack mealStack = cookingPot.getMeal();
        if (!mealStack.isEmpty()) {
            if (!cookingPot.doesMealHaveContainer(mealStack)) {
                cookingPot.moveMealToOutput();
                didInventoryChange = true;
            } else if (!cookingPot.inventory.getStackInSlot(CONTAINER_SLOT).isEmpty()) {
                cookingPot.useStoredContainersOnMeal();
                didInventoryChange = true;
            }
        }

        if (didInventoryChange) {
            cookingPot.inventoryChanged();
        }
    }

    public static void animationTick(Level level, BlockPos pos, BlockState state, CursedIngotPotBlockEntity cookingPot) {
        if (cookingPot.isHeated(level, pos)) {
            RandomSource random = level.random;
            double x, y, z;
            if (random.nextFloat() < 0.2F) {
                x = pos.getX() + 0.5 + (random.nextDouble() * 0.6 - 0.3);
                y = pos.getY() + 0.7;
                z = pos.getZ() + 0.5 + (random.nextDouble() * 0.6 - 0.3);
                level.addParticle(ParticleTypes.BUBBLE_POP, x, y, z, 0.0, 0.0, 0.0);
            }
            if (random.nextFloat() < 0.05F) {
                x = pos.getX() + 0.5 + (random.nextDouble() * 0.4 - 0.2);
                y = pos.getY() + 0.5;
                z = pos.getZ() + 0.5 + (random.nextDouble() * 0.4 - 0.2);
                double motionY = random.nextBoolean() ? 0.015 : 0.005;
                level.addParticle(ModParticleTypes.STEAM.get(), x, y, z, 0.0, motionY, 0.0);
            }
        }
    }

    private boolean hasInput() {
        for (int i = 0; i < 6; i++) {
            if (!this.inventory.getStackInSlot(i).isEmpty()) return true;
        }
        return false;
    }

    private boolean canCook(CookingPotRecipe recipe) {
        if (!hasInput() || this.level == null) return false;
        ItemStack resultStack = recipe.getResultItem(this.level.registryAccess());
        if (resultStack.isEmpty()) return false;
        ItemStack storedMeal = this.inventory.getStackInSlot(MEAL_DISPLAY_SLOT);
        if (storedMeal.isEmpty()) return true;
        if (!ItemStack.isSameItem(storedMeal, resultStack)) return false;
        int total = storedMeal.getCount() + resultStack.getCount();
        return total <= storedMeal.getMaxStackSize() && total <= 64;
    }

    private int calculateSoulCost(ItemStack resultStack) {
        int baseCost = 50;
        int nutritionCost = 0;
        FoodProperties food = resultStack.getFoodProperties(null);
        if (food != null) {
            nutritionCost = 10 * food.nutrition();
        }
        return Math.min(baseCost + nutritionCost, 2000);
    }

    private boolean processCooking(RecipeHolder<CookingPotRecipe> holder) {
        if (this.level == null) return false;
        CookingPotRecipe recipe = holder.value();
        this.cookTimeTotal = recipe.getCookTime();
        if (this.cookTime < this.cookTimeTotal) return false;
        this.cookTime = 0.0f;
        this.mealContainerStack = recipe.getOutputContainer();
        ItemStack resultStack = recipe.getResultItem(this.level.registryAccess());

        //尝试消耗灵魂能量
        boolean hasSoul = this.hasSoulEnergy();
        if (hasSoul) {
            int soulCost = calculateSoulCost(resultStack);
            boolean soulInfused = this.consumeSoulEnergy(soulCost);
            if (soulInfused) {
                CompoundTag tag = new CompoundTag();
                tag.putBoolean("SoulInfused", true);
                resultStack.set(DataComponents.CUSTOM_DATA, net.minecraft.world.item.component.CustomData.of(tag));
            }
            // 如果能量不足，则不添加 SoulInfused 标记，食物正常产出但不含灵魂注入
        }

        ItemStack storedMeal = this.inventory.getStackInSlot(MEAL_DISPLAY_SLOT);
        if (storedMeal.isEmpty()) {
            this.inventory.setStackInSlot(MEAL_DISPLAY_SLOT, resultStack.copy());
        } else if (ItemStack.isSameItem(storedMeal, resultStack)) {
            storedMeal.grow(resultStack.getCount());
        }
        this.usedRecipeTracker.addTo(holder.id(), 1);
        for (int i = 0; i < 6; i++) {
            ItemStack slotStack = this.inventory.getStackInSlot(i);
            if (slotStack.hasCraftingRemainingItem()) {
                ejectIngredientRemainder(slotStack.getCraftingRemainingItem().copy().split(1));
            } else if (INGREDIENT_REMAINDER_OVERRIDES.containsKey(slotStack.getItem())) {
                ejectIngredientRemainder(INGREDIENT_REMAINDER_OVERRIDES.get(slotStack.getItem()).getDefaultInstance());
            }
            if (!slotStack.isEmpty()) {
                slotStack.shrink(1);
            }
        }
        return true;
    }

    private void ejectIngredientRemainder(ItemStack remainder) {
        Direction direction = this.getBlockState().getValue(CookingPotBlock.FACING).getCounterClockWise();
        double x = this.worldPosition.getX() + 0.5 + direction.getStepX() * 0.25;
        double y = this.worldPosition.getY() + 0.7;
        double z = this.worldPosition.getZ() + 0.5 + direction.getStepZ() * 0.25;
        ItemUtils.spawnItemEntity(this.level, remainder, x, y, z,
                direction.getStepX() * 0.08F, 0.25, direction.getStepZ() * 0.08F);
    }

    public List<Recipe<?>> getUsedRecipesAndPopExperience(Level level, Vec3 pos) {
        List<Recipe<?>> list = Lists.newArrayList();
        for (Object2IntMap.Entry<ResourceLocation> entry : this.usedRecipeTracker.object2IntEntrySet()) {
            level.getRecipeManager().byKey(entry.getKey()).ifPresent(holder -> {
                Recipe<?> recipe = holder.value();
                list.add(recipe);
                if (recipe instanceof CookingPotRecipe cookingRecipe) {
                    int expTotal = Mth.floor(entry.getIntValue() * cookingRecipe.getExperience());
                    float expFraction = Mth.frac(entry.getIntValue() * cookingRecipe.getExperience());
                    if (expFraction != 0.0F && Math.random() < expFraction) expTotal++;
                    if (level instanceof ServerLevel serverLevel) {
                        ExperienceOrb.award(serverLevel, pos, expTotal);
                    }
                }
            });
        }
        return list;
    }

    public ItemStackHandler getInventory() {
        return this.inventory;
    }

    public ItemStack getMeal() {
        return this.inventory.getStackInSlot(MEAL_DISPLAY_SLOT);
    }

    public NonNullList<ItemStack> getDroppableInventory() {
        NonNullList<ItemStack> drops = NonNullList.create();
        for (int i = 0; i < INVENTORY_SIZE; i++) {
            if (i != MEAL_DISPLAY_SLOT) {
                drops.add(this.inventory.getStackInSlot(i));
            }
        }
        return drops;
    }

    public int getAnalogOutputSignal() {
        ItemStack outputStack = this.inventory.getStackInSlot(OUTPUT_SLOT);
        if (outputStack.isEmpty()) return 0;
        return (int) Math.floor((double) outputStack.getCount() / outputStack.getMaxStackSize() * 15.0);
    }

    private void moveMealToOutput() {
        ItemStack mealStack = this.inventory.getStackInSlot(MEAL_DISPLAY_SLOT);
        ItemStack outputStack = this.inventory.getStackInSlot(OUTPUT_SLOT);
        int mealCount = Math.min(mealStack.getCount(), mealStack.getMaxStackSize() - outputStack.getCount());
        if (outputStack.isEmpty()) {
            this.inventory.setStackInSlot(OUTPUT_SLOT, mealStack.split(mealCount));
        } else if (ItemStack.isSameItem(outputStack, mealStack)) {
            mealStack.shrink(mealCount);
            outputStack.grow(mealCount);
        }
    }

    private void useStoredContainersOnMeal() {
        ItemStack mealStack = this.inventory.getStackInSlot(MEAL_DISPLAY_SLOT);
        ItemStack containerInput = this.inventory.getStackInSlot(CONTAINER_SLOT);
        ItemStack outputStack = this.inventory.getStackInSlot(OUTPUT_SLOT);
        if (isContainerValid(containerInput) && outputStack.getCount() < outputStack.getMaxStackSize()) {
            int smaller = Math.min(mealStack.getCount(), containerInput.getCount());
            int mealCount = Math.min(smaller, mealStack.getMaxStackSize() - outputStack.getCount());
            if (outputStack.isEmpty()) {
                containerInput.shrink(mealCount);
                this.inventory.setStackInSlot(OUTPUT_SLOT, mealStack.split(mealCount));
            } else if (ItemStack.isSameItem(outputStack, mealStack)) {
                mealStack.shrink(mealCount);
                containerInput.shrink(mealCount);
                outputStack.grow(mealCount);
            }
        }
    }

    public ItemStack useHeldItemOnMeal(ItemStack container) {
        ItemStack mealStack = getMeal();
        if (mealStack.isEmpty()) return ItemStack.EMPTY;
        if (!doesMealHaveContainer(mealStack)) {
            if (container.isEmpty() || (ItemStack.isSameItem(mealStack, container)
                            && container.getCount() < container.getMaxStackSize())) {
                ItemStack result = mealStack.split(1);
                inventoryChanged();
                return result;
            }
            return ItemStack.EMPTY;
        } else if (isContainerValid(container)) {
            container.shrink(1);
            inventoryChanged();
            return mealStack.split(1);
        }
        return ItemStack.EMPTY;
    }

    private boolean doesMealHaveContainer(ItemStack meal) {
        return !this.mealContainerStack.isEmpty() || meal.hasCraftingRemainingItem();
    }

    public boolean isContainerValid(ItemStack containerItem) {
        if (containerItem.isEmpty()) return false;
        return !this.mealContainerStack.isEmpty()
                ? ItemStack.isSameItem(this.mealContainerStack, containerItem)
                : ItemStack.isSameItem(getMeal(), containerItem);
    }

    @Override
    public Component getName() {
        return this.customName != null ? this.customName : TextUtils.getTranslation("container.cursed_ingot_pot");
    }

    @Override
    public Component getDisplayName() {
        return getName();
    }

    @Nullable
    public Component getCustomName() {
        return this.customName;
    }

    public void setCustomName(Component name) {
        this.customName = name;
    }

    @Override
    public AbstractContainerMenu createMenu(int id, Inventory inv, Player player) {
        return new CursedIngotPotMenu(id, inv, this, this.cookingPotData);
    }

    @Override
    protected void inventoryChanged() {
        setChanged();
        if (this.level != null) {
            this.level.sendBlockUpdated(this.worldPosition, this.getBlockState(), this.getBlockState(), 3);
        }
    }
    
    public boolean isHeated() {
        return this.level != null && this.isHeated(this.level, this.worldPosition);
    }

    public boolean isHeated(Level level, BlockPos pos) {
        BlockState belowState = level.getBlockState(pos.below());
        if (belowState.is(ModTags.Blocks.HEAT_SOURCES)) {
            return true;
        }
        Block block = belowState.getBlock();
        return block == net.minecraft.world.level.block.Blocks.CAMPFIRE ||
                block == net.minecraft.world.level.block.Blocks.SOUL_CAMPFIRE ||
                block == net.minecraft.world.level.block.Blocks.FURNACE ||
                block == net.minecraft.world.level.block.Blocks.BLAST_FURNACE ||
                block == net.minecraft.world.level.block.Blocks.SMOKER ||
                block == net.minecraft.world.level.block.Blocks.MAGMA_BLOCK;
    }

    static {
        Map<Item, Item> map = new HashMap<>();
        map.put(Items.POWDER_SNOW_BUCKET, Items.BUCKET);
        map.put(Items.AXOLOTL_BUCKET, Items.BUCKET);
        map.put(Items.COD_BUCKET, Items.BUCKET);
        map.put(Items.PUFFERFISH_BUCKET, Items.BUCKET);
        map.put(Items.SALMON_BUCKET, Items.BUCKET);
        map.put(Items.TROPICAL_FISH_BUCKET, Items.BUCKET);
        map.put(Items.SUSPICIOUS_STEW, Items.BOWL);
        map.put(Items.MUSHROOM_STEW, Items.BOWL);
        map.put(Items.RABBIT_STEW, Items.BOWL);
        map.put(Items.BEETROOT_SOUP, Items.BOWL);
        map.put(Items.POTION, Items.GLASS_BOTTLE);
        map.put(Items.SPLASH_POTION, Items.GLASS_BOTTLE);
        map.put(Items.LINGERING_POTION, Items.GLASS_BOTTLE);
        map.put(Items.EXPERIENCE_BOTTLE, Items.GLASS_BOTTLE);
        INGREDIENT_REMAINDER_OVERRIDES = Collections.unmodifiableMap(map);
    }
}