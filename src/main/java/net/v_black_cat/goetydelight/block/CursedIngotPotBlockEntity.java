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

    // 【优化1】自动触发内部变化的 Handler
    // 【优化3】批量修改库存时挂起逐槽事件：避免一次烹饪/加载触发多次配方查询与发包
    private final ItemStackHandler inventory = new ItemStackHandler(INVENTORY_SIZE) {
        @Override
        protected void onContentsChanged(int slot) {
            if (CursedIngotPotBlockEntity.this.deferInventoryEvents) {
                CursedIngotPotBlockEntity.this.setChanged();
                return;
            }
            CursedIngotPotBlockEntity.this.inventoryChanged();
        }
    };

    private float cookTime;
    private int cookTimeTotal;
    private ItemStack mealContainerStack = ItemStack.EMPTY;
    private Component customName;
    protected final ContainerData cookingPotData;
    private final Object2IntOpenHashMap<ResourceLocation> usedRecipeTracker = new Object2IntOpenHashMap<>();

    @Nullable
    private RecipeHolder<CookingPotRecipe> cachedRecipe = null;

    // 【优化2】缓存输入状态
    private boolean hasInputCache = false;

    // 【优化3】配方缓存衍生结果：与 cachedRecipe 一起刷新，避免每 tick 拷贝结果栈/重复计算灵魂消耗
    private ItemStack cachedResultStack = ItemStack.EMPTY;
    private int cachedSoulCost = 0;
    // 【优化3】批量修改挂起标志（加载 / 一次烹饪完成时只刷新一次）
    private boolean deferInventoryEvents = false;

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
        // 【优化3】加载期间挂起逐槽刷新，反序列化完成后统一刷新一次
        this.deferInventoryEvents = true;
        try {
            if (compound.contains("Inventory")) {
                this.inventory.deserializeNBT(registries, compound.getCompound("Inventory"));
            }
        } finally {
            this.deferInventoryEvents = false;
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
        // 加载期间逐槽事件被挂起，这里显式重建输入缓存后再刷新配方
        this.hasInputCache = this.hasInputInternal();
        this.refreshCurrentRecipe();
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

    // ======================== 灵魂系统（最终 NBT 优化版） ========================
    
    // 【优化终版】使用 getUnsafe() 避免 NBT 深度拷贝，只读操作极其安全且省性能
    public boolean hasSoulEnergy() {
        ItemStack soulSource = this.inventory.getStackInSlot(SOUL_SOURCE_SLOT);
        if (soulSource.isEmpty()) return false;
        var customData = soulSource.get(DataComponents.CUSTOM_DATA);
        CompoundTag tag = customData != null ? customData.getUnsafe() : null;
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

    // 【优化终版】只读取 NBT，绝对安全使用 getUnsafe()
    private boolean isSoulInfused(ItemStack stack) {
        if (stack.isEmpty()) return false;
        var customData = stack.get(DataComponents.CUSTOM_DATA);
        CompoundTag tag = customData != null ? customData.getUnsafe() : null;
        return tag != null && tag.getBoolean("SoulInfused");
    }

    // 【优化终版】只读取 NBT，绝对安全使用 getUnsafe()
    public int getRemainingSoulEnergy() {
        ItemStack soulSource = this.inventory.getStackInSlot(SOUL_SOURCE_SLOT);
        if (soulSource.isEmpty()) return 0;
        var customData = soulSource.get(DataComponents.CUSTOM_DATA);
        CompoundTag tag = customData != null ? customData.getUnsafe() : null;
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

    // 注：需要修改 NBT，保留原 copyTag() 代码
    private boolean consumeSoulEnergy(int amount) {
        if (amount <= 0) return false;
        ItemStack soulSource = this.inventory.getStackInSlot(SOUL_SOURCE_SLOT);
        if (soulSource.isEmpty() || this.level == null) return false;
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

    // 性能优化重载方法：直接读取已提取好的物品，内部同样使用 getUnsafe()
    private boolean hasSoulEnergy(ItemStack soulSource) {
        if (soulSource.isEmpty()) return false;
        var customData = soulSource.get(DataComponents.CUSTOM_DATA);
        CompoundTag tag = customData != null ? customData.getUnsafe() : null;
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

    private int getRemainingSoulEnergy(ItemStack soulSource) {
        if (soulSource.isEmpty()) return 0;
        var customData = soulSource.get(DataComponents.CUSTOM_DATA);
        CompoundTag tag = customData != null ? customData.getUnsafe() : null;
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

    // 【优化3】合并每 tick 的灵魂查询：一次调用同时完成"是否有可用灵魂"与"剩余数量"判定
    private int getSoulEnergyAvailable() {
        ItemStack soulSource = this.inventory.getStackInSlot(SOUL_SOURCE_SLOT);
        if (soulSource.isEmpty()) return 0;
        var customData = soulSource.get(DataComponents.CUSTOM_DATA);
        CompoundTag tag = customData != null ? customData.getUnsafe() : null;
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

    // ======================== 核心Tick逻辑 ========================
    public static void cookingTick(Level level, BlockPos pos, BlockState state, CursedIngotPotBlockEntity cookingPot) {
        boolean isHeated = cookingPot.isHeated(level, pos);
        boolean didInventoryChange = false;

        ItemStack currentMeal = cookingPot.getMeal();
        ItemStack outputStack = cookingPot.inventory.getStackInSlot(OUTPUT_SLOT);
        boolean anyHasMark = cookingPot.isSoulInfused(currentMeal) || cookingPot.isSoulInfused(outputStack);

        boolean canCook = false;
        float speedMultiplier = 1.0f;
        RecipeHolder<CookingPotRecipe> currentRecipe = cookingPot.cachedRecipe;

        // 【优化4】配方缓存自愈：每 5 秒重新匹配一次（仅在有原料时执行），避免数据包重载后缓存过期
        if (level.getGameTime() % 100 == 0) {
            cookingPot.refreshCurrentRecipe();
            currentRecipe = cookingPot.cachedRecipe;
        }

        if (cookingPot.hasInput() && currentRecipe != null && cookingPot.canCook(currentRecipe.value())) {
            // 【优化3】灵魂消耗与结果栈均为缓存值，不再每 tick 拷贝结果栈/查食物属性
            int estimatedCost = cookingPot.cachedSoulCost;
            int remainingSoul = cookingPot.getSoulEnergyAvailable();
            boolean hasSoulEnergy = remainingSoul > 0;

            boolean needSoul;
            if (currentMeal.isEmpty() && outputStack.isEmpty()) {
                needSoul = hasSoulEnergy && (remainingSoul >= estimatedCost);
            } else {
                needSoul = anyHasMark;
            }

            boolean soulAvailable = hasSoulEnergy && (remainingSoul >= estimatedCost);

            if (anyHasMark && !soulAvailable) {
                canCook = false;
                cookingPot.cookTime = 0;
            } else {
                if (isHeated && needSoul && soulAvailable) {
                    canCook = true;
                    speedMultiplier = 2.0f;
                } else if (isHeated && !needSoul) {
                    canCook = true;
                    speedMultiplier = 1.0f;
                } else if (needSoul && soulAvailable) {
                    canCook = true;
                    speedMultiplier = 1.2f;
                } else {
                    canCook = false;
                    speedMultiplier = 0f;
                }
            }
        }

        if (canCook && cookingPot.hasInput() && currentRecipe != null) {
            cookingPot.cookTime += 1.0f * speedMultiplier;
            if (cookingPot.cookTime >= cookingPot.cookTimeTotal) {
                boolean success = cookingPot.processCooking(currentRecipe);
                if (!success) {
                    cookingPot.cookTime = 0;
                }
            }
        } else if (cookingPot.cookTime > 0) {
            // 【优化3】未烹饪时降温，每 tick 恰好一次（修复此前空锅/无配方时双倍衰减）
            cookingPot.cookTime = Mth.clamp(cookingPot.cookTime - 1.0f, 0.0f, cookingPot.cookTimeTotal);
        }

        ItemStack mealStack = cookingPot.getMeal();
        if (!mealStack.isEmpty()) {
            // 【优化3】批量取餐处理：挂起逐槽事件，结束时统一刷新一次
            cookingPot.deferInventoryEvents = true;
            try {
                if (!cookingPot.doesMealHaveContainer(mealStack)) {
                    // 【修复】仅在真正移动了物品时才标记变更：输出槽满/物品不同时 moveMealToOutput 是空操作，
                    // 此前每 tick 都会 inventoryChanged → 完整配方扫描 + 置脏（spark 采样证实占据 ~3% 服务端时间）
                    didInventoryChange = cookingPot.moveMealToOutput();
                } else if (!cookingPot.inventory.getStackInSlot(CONTAINER_SLOT).isEmpty()) {
                    didInventoryChange = cookingPot.useStoredContainersOnMeal();
                }
            } finally {
                cookingPot.deferInventoryEvents = false;
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

    // ======================== 配方、缓存与辅助方法 ========================
    
    private boolean hasInputInternal() {
        for (int i = 0; i < 6; i++) {
            if (!this.inventory.getStackInSlot(i).isEmpty()) return true;
        }
        return false;
    }

    private boolean hasInput() {
        return this.hasInputCache;
    }

    private void refreshCurrentRecipe() {
        // 【优化3】缓存结果与配方一同刷新/失效
        this.cachedRecipe = null;
        this.cachedResultStack = ItemStack.EMPTY;
        this.cachedSoulCost = 0;
        if (this.level == null || this.level.isClientSide) {
            return;
        }
        if (this.hasInputCache) { 
            RecipeWrapper wrapper = new RecipeWrapper(this.inventory);
            this.cachedRecipe = this.level.getRecipeManager().getRecipeFor(ModRecipeTypes.COOKING.get(), wrapper, this.level).orElse(null);
            if (this.cachedRecipe != null) {
                this.cachedResultStack = this.cachedRecipe.value().getResultItem(this.level.registryAccess());
                this.cachedSoulCost = this.calculateSoulCost(this.cachedResultStack);
                this.cookTimeTotal = this.cachedRecipe.value().getCookTime();
                if (this.cookTime > this.cookTimeTotal) {
                    this.cookTime = this.cookTimeTotal;
                }
            }
        }
        if (this.cachedRecipe == null && this.cookTime > 0) {
            this.cookTime = 0;
        }
    }

    private boolean canCook(CookingPotRecipe recipe) {
        if (!hasInput() || this.level == null) return false;
        // 【优化3】使用缓存的配方结果栈，避免每 tick 拷贝
        ItemStack resultStack = this.cachedResultStack;
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

        ItemStack currentMeal = this.inventory.getStackInSlot(MEAL_DISPLAY_SLOT);
        ItemStack outputStack = this.inventory.getStackInSlot(OUTPUT_SLOT);
        boolean anyHasMark = isSoulInfused(currentMeal) || isSoulInfused(outputStack);
        ItemStack resultStack = recipe.getResultItem(this.level.registryAccess());

        resultStack.remove(DataComponents.CUSTOM_DATA);

        int soulCost = calculateSoulCost(resultStack);
        // 【优化3】合并灵魂查询：一次调用同时判断"是否有可用灵魂源"与"剩余数量"
        int remainingSoul = getSoulEnergyAvailable();
        boolean hasSoulSource = remainingSoul > 0;

        boolean shouldUseSoul;
        if (currentMeal.isEmpty() && outputStack.isEmpty()) {
            shouldUseSoul = hasSoulSource && (remainingSoul >= soulCost);
        } else {
            shouldUseSoul = anyHasMark && hasSoulSource && (remainingSoul >= soulCost);
        }

        if (shouldUseSoul) {
            if (hasSoulSource && remainingSoul >= soulCost) {
                boolean soulInfused = this.consumeSoulEnergy(soulCost);
                if (soulInfused) {
                    CompoundTag tag = new CompoundTag();
                    tag.putBoolean("SoulInfused", true);
                    resultStack.set(DataComponents.CUSTOM_DATA, net.minecraft.world.item.component.CustomData.of(tag));
                } else {
                    this.cookTime = 0.0f;
                    return false;
                }
            } else {
                this.cookTime = 0.0f;
                return false;
            }
        }

        // 【优化3】批量修改库存：挂起逐槽事件，最后统一刷新一次配方缓存并同步
        this.deferInventoryEvents = true;
        try {
            if (currentMeal.isEmpty()) {
                this.inventory.setStackInSlot(MEAL_DISPLAY_SLOT, resultStack.copy());
            } else if (ItemStack.isSameItem(currentMeal, resultStack)) {
                currentMeal.grow(resultStack.getCount());
            } else {
                return false;
            }

            this.cookTime = 0.0f;
            this.mealContainerStack = recipe.getOutputContainer();
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
        } finally {
            this.deferInventoryEvents = false;
        }
        this.inventoryChanged();
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
                    if (expFraction != 0.0F && level.random.nextDouble() < expFraction) expTotal++;
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

    private boolean moveMealToOutput() {
        ItemStack mealStack = this.inventory.getStackInSlot(MEAL_DISPLAY_SLOT);
        ItemStack outputStack = this.inventory.getStackInSlot(OUTPUT_SLOT);
        int mealCount = Math.min(mealStack.getCount(), mealStack.getMaxStackSize() - outputStack.getCount());
        if (mealCount <= 0) return false;
        if (outputStack.isEmpty()) {
            this.inventory.setStackInSlot(OUTPUT_SLOT, mealStack.split(mealCount));
            return true;
        } else if (ItemStack.isSameItem(outputStack, mealStack)) {
            mealStack.shrink(mealCount);
            outputStack.grow(mealCount);
            return true;
        }
        return false;
    }

    private boolean useStoredContainersOnMeal() {
        ItemStack mealStack = this.inventory.getStackInSlot(MEAL_DISPLAY_SLOT);
        ItemStack containerInput = this.inventory.getStackInSlot(CONTAINER_SLOT);
        ItemStack outputStack = this.inventory.getStackInSlot(OUTPUT_SLOT);
        if (isContainerValid(containerInput) && outputStack.getCount() < outputStack.getMaxStackSize()) {
            int smaller = Math.min(mealStack.getCount(), containerInput.getCount());
            int mealCount = Math.min(smaller, mealStack.getMaxStackSize() - outputStack.getCount());
            if (mealCount <= 0) return false;
            if (outputStack.isEmpty()) {
                containerInput.shrink(mealCount);
                this.inventory.setStackInSlot(OUTPUT_SLOT, mealStack.split(mealCount));
                return true;
            } else if (ItemStack.isSameItem(outputStack, mealStack)) {
                mealStack.shrink(mealCount);
                containerInput.shrink(mealCount);
                outputStack.grow(mealCount);
                return true;
            }
        }
        return false;
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
        // 【优化】不再在打开菜单时做完整配方匹配：缓存已由 inventoryChanged / 每 5 秒自愈刷新维护
        return new CursedIngotPotMenu(id, inv, this, this.cookingPotData);
    }

    @Override
    protected void inventoryChanged() {
        setChanged();
        if (this.level != null) {
            this.level.sendBlockUpdated(this.worldPosition, this.getBlockState(), this.getBlockState(), 3);
        }
        this.hasInputCache = this.hasInputInternal();
        this.refreshCurrentRecipe();
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